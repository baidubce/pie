package com.baidu.acu.pie.grpc;

import com.baidu.acu.pie.TtsServiceGrpc;
import com.baidu.acu.pie.TtsStreaming;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.client.TtsClient;
import com.baidu.acu.pie.exception.GlobalClientException;
import com.baidu.acu.pie.exception.GlobalException;
import com.baidu.acu.pie.model.ChannelConfig;
import com.baidu.acu.pie.model.FinishLatchImpl;
import com.baidu.acu.pie.model.ObjectWrapper;
import com.baidu.acu.pie.model.TtsConfig;
import com.baidu.acu.pie.model.TtsRequest;
import com.baidu.acu.pie.model.TtsStreamContext;
import com.baidu.acu.pie.util.ArrayUtil;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 类<code>TtsClientGrpcImpl</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 **/
@Slf4j
public class TtsClientGrpcImpl implements TtsClient {
    private final ManagedChannel managedChannel;
    private final TtsServiceGrpc.TtsServiceStub asyncStub;
    private TtsConfig ttsConfig;

    public TtsClientGrpcImpl(TtsConfig ttsConfig) {
        this(ttsConfig, ChannelConfig.builder().build());
    }

    public TtsClientGrpcImpl(TtsConfig ttsConfig, ChannelConfig channelConfig) {
        this.ttsConfig = ttsConfig;

        if (ttsConfig.isSslUseFlag()) {
            managedChannel = initSslManagedChannel(ttsConfig, channelConfig);
        } else {
            managedChannel = initManagedChannel(ttsConfig, channelConfig);
        }

        asyncStub = TtsServiceGrpc.newStub(managedChannel);
    }

    @Override
    public byte[] syncToSpeech(String text) {
        return syncToSpeech(text, new TtsRequest());
    }

    @Override
    @SneakyThrows({InterruptedException.class})
    public byte[] syncToSpeech(String text, TtsRequest request) {
        ObjectWrapper<byte[]> wrapper = new ObjectWrapper<>();
        TtsStreamContext ttsStreamContext = this.asyncToSpeech(text, e -> {
            wrapper.set(ArrayUtil.byteMerger(wrapper.get(), e));
        }, request);
        if (!ttsStreamContext.await(request.getTimeoutMinutes(), TimeUnit.MINUTES)) {
            log.error("Speech request not finish within {} minutes, maybe the audio is too large", 30);
        }
        return wrapper.get();
    }


    @Override
    public void syncToSpeech(String text, String filepath) {
        syncToSpeech(text, filepath, new TtsRequest());
    }

    @Override
    public void syncToSpeech(String text, String filepath, TtsRequest request) {
        try {
            Path path = Paths.get(filepath);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            byte[] bytes = syncToSpeech(text, request);
            Files.write(path, bytes);
        } catch (Exception e) {
            log.error("fail to write file", e);
            throw new GlobalClientException("fail to write file");
        }
    }

    @Override
    public TtsStreamContext asyncToSpeech(String text, Consumer<byte[]> resultConsumer) {
        return asyncToSpeech(text, resultConsumer, new TtsRequest());
    }

    @Override
    public TtsStreamContext asyncToSpeech(String text, final Consumer<byte[]> resultConsumer, TtsRequest request) {
        final FinishLatchImpl finishLatch = new FinishLatchImpl();
        Context.current().fork().run(() -> {
            TtsStreaming.TtsFragmentRequest ttsFragmentRequest = prepareRequest(text, request);

            asyncStub.getAudio(ttsFragmentRequest, new StreamObserver<TtsStreaming.TtsFragmentResponse>() {
                @Override
                public void onNext(TtsStreaming.TtsFragmentResponse response) {
                    if (response.getErrorCode() == 0) {
                        log.info("get response data: {}, data len: {}, real data len: {}",
                                response.getErrorMessage(),
                                response.getAudioFragment().getLength(),
                                response.getAudioFragment().getAudioData().toByteArray().length);
                        resultConsumer.accept(response.getAudioFragment().getAudioData().toByteArray());
                    } else {
                        finishLatch.fail(new GlobalException(
                                response.getTraceId(),
                                response.getErrorCode(),
                                response.getErrorMessage()));
                    }
                }

                @Override
                public void onError(Throwable t) {
                    finishLatch.fail(new GlobalException(-3000, "error in grpc response observer", t));
                }

                @Override
                public void onCompleted() {
                    log.info("response observer complete");
                    finishLatch.finish();
                }
            });
        });

        return TtsStreamContext.builder().finishLatch(finishLatch).build();
    }


    @Override
    public void shutdown() {
        try {
            managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("shutdown failed: ", e);
        }
    }

    private ManagedChannel initManagedChannel(TtsConfig ttsConfig, ChannelConfig channelConfig) {

        return ManagedChannelBuilder
                .forAddress(ttsConfig.getServerIp(), ttsConfig.getServerPort())
                .usePlaintext()
                .keepAliveTime(channelConfig.getKeepAliveTime().getTime(),
                        channelConfig.getKeepAliveTime().getTimeUnit())
                .keepAliveTimeout(channelConfig.getKeepAliveTimeout().getTime(),
                        channelConfig.getKeepAliveTimeout().getTimeUnit())
                .build();
    }

    private ManagedChannel initSslManagedChannel(TtsConfig ttsConfig, ChannelConfig channelConfig) {
        try {
            return NettyChannelBuilder
                    .forAddress(ttsConfig.getServerIp(), ttsConfig.getServerPort())
                    .keepAliveTime(channelConfig.getKeepAliveTime().getTime(),
                            channelConfig.getKeepAliveTime().getTimeUnit())
                    .keepAliveTimeout(channelConfig.getKeepAliveTimeout().getTime(),
                            channelConfig.getKeepAliveTimeout().getTimeUnit())
                    .negotiationType(NegotiationType.TLS)
                    .sslContext(GrpcSslContexts.forClient()
                            .trustManager(new File(ttsConfig.getSslPath())).build())
                    .build();
        } catch (SSLException e) {
            throw new GlobalClientException("build ssl client failed");
        }
    }


    private TtsStreaming.TtsFragmentRequest prepareRequest(String text, TtsRequest request) {

        TtsStreaming.TtsFragmentRequest initRequest = TtsStreaming.TtsFragmentRequest.newBuilder()
                .setAue(request.getAue())
                .setCtp(request.getCtp())
                .setCuid(request.getCuid())
                .setLan(request.getLan())
                .setPdt(request.getPdt())
                .setPit(request.getPit())
                .setPer(request.getPer())
                .setSk(request.getSk())
                .setTex(text)
                .setVol(request.getVol())
                .setSpd(request.getSpd())
                .setXml(request.getXml())
                .setSendTimestamp(request.getSendTimestamp())
                .setSequenceNum(request.getSequenceNum())
                .putAllExtraParams(request.getExtraParams())
                .build();

        log.info("request:\n {}", initRequest.toString());
        return initRequest;
    }
}
