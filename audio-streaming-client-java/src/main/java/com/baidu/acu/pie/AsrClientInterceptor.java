// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie;

import java.util.Base64;

import com.baidu.acu.pie.AudioStreaming.InitRequest;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * AsrClientInterpretor
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
public class AsrClientInterceptor implements ClientInterceptor {
    private final InitRequest initRequest = InitRequest.newBuilder()
            .setEnableLongSpeech(true)
            .setEnableChunk(true)
            .setEnableFlushData(true)
            .setProductId("1903")
            .setSamplePointBytes(2)
            .setSendPerSeconds(0.16)
            .setSleepRatio(1)
            .setAppName("cynric_asr_client")
            .setLogLevel(4)
            .build();

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel next) {

        return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(
                        Metadata.Key.of("audio_meta", Metadata.ASCII_STRING_MARSHALLER),
                        Base64.getEncoder().encodeToString(initRequest.toByteArray()));

                super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        /**
                         * if you don't need receive header from server,
                         * you can use {@link io.grpc.stub.MetadataUtils#attachHeaders}
                         * directly to send header
                         */
                        System.out.println(("header received from server:" + headers));
                        super.onHeaders(headers);
                    }
                }, headers);
            }
        };
    }
}