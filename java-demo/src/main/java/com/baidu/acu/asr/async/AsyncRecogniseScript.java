package com.baidu.acu.asr.async;

import com.baidu.acu.asr.model.AsrArgs;
import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.exception.GlobalException;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.util.JacksonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AsyncRecogniseScript created at 2023/2/13 15:53
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 */
@Slf4j
public class AsyncRecogniseScript {
    public static Args args;


    public static void main(String[] args) {
        AsyncRecogniseScript.args = Args.parse(args);
        if ("file".equals(AsyncRecogniseScript.args.getType()) ||
                "".equals(AsyncRecogniseScript.args.getType())) {
            asyncRecognizeWithStream(createAsrClient());
        } else if ("microphone".equals(AsyncRecogniseScript.args.getType())) {
            asyncRecognitionWithMicrophone();
        } else {
            log.warn("type 参数不正确，离线文件：file，麦克风：microphone");
        }

    }

    private static AsrClient createAsrClient() {
        // 创建调用asr服务的客户端
        // asrConfig构造后就不可修改
        return AsrClientFactory.buildClient(buildAsrConfig());
    }

    private static AsrConfig buildAsrConfig() {
        // asrConfig构造后就不可修改
        // 当使用ssl client时，需要配置字段sslUseFlag以及sslPath
        return AsrConfig.builder()
                .appName(args.getAppName())
                .serverIp(args.getIp())
                .serverPort(args.getPort())
                .product(AsrArgs.parseProduct(args.getProductId()))
                .userName(args.getUsername())
                .password(args.getPassword())
                .build();
    }

    private static RequestMetaData createRequestMeta() {
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setSendPackageRatio(1);
        requestMetaData.setSleepRatio(0);
        requestMetaData.setTimeoutMinutes(120);
        requestMetaData.setEnableFlushData(args.getEnableFlushData());
        // 随路信息根据需要设置
        Map<String, Object> extra_info = new HashMap<>();
        extra_info.put("demo", "java");
        requestMetaData.setExtraInfo(JacksonUtil.objectToString(extra_info));

        return requestMetaData;
    }

    /**
     * 识别麦克风音频流
     */
    public static void asyncRecognitionWithMicrophone() {
        AsrClient asrClient = createAsrClient();

        RequestMetaData requestMetaData = createRequestMeta();

        StreamContext streamContext = asrClient.asyncRecognize(it -> System.out.println(
                DateTime.now() + "\t" + Thread.currentThread().getId() +
                        " receive fragment: " + it), requestMetaData);

        streamContext.enableCallback(e -> {
            if (e != null) {
                e.printStackTrace();
            }
        });

        AsrConfig asrConfig = buildAsrConfig();

        TargetDataLine line = null;
        AudioFormat audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                asrConfig.getProduct().getSampleRate(),
                16,
                1,
                2,    // (sampleSizeBits / 8) * channels
                asrConfig.getProduct().getSampleRate(),
                false);

        DataLine.Info info = new DataLine.Info(
                TargetDataLine.class,
                audioFormat);

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("line not support");
            return;
        }

        FileOutputStream fop = null;
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);

            line.open(audioFormat, line.getBufferSize());
            line.start();

            int bufferLengthInBytes = asrClient.getFragmentSize();
            byte[] data = new byte[bufferLengthInBytes];
            System.out.println("start to record");
            while ((line.read(data, 0, bufferLengthInBytes)) != -1L &&
                    !streamContext.getFinishLatch().finished()) {
                streamContext.send(data);
                if (fop != null) {
                    fop.write(data);
                    fop.flush();
                }
            }

            System.out.println(new DateTime() + "\t" + Thread.currentThread().getId() + " send finish");
            streamContext.complete();

            // wait to ensure to receive the last response
            streamContext.getFinishLatch().await();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (line != null) {
                line.close();
            }
            asrClient.shutdown();
            if (fop != null) {
                try {
                    fop.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("all task finished");
    }

    private static String generateAudioName() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String time = dateTimeFormatter.format(LocalDateTime.now());
        return time + UUID.randomUUID() + ".pcm";
    }


    private static void asyncRecognizeWithStream(AsrClient asrClient) {
        final AtomicReference<DateTime> beginSend = new AtomicReference<DateTime>();
        final StreamContext streamContext = asrClient.asyncRecognize(new Consumer<RecognitionResult>() {
            public void accept(RecognitionResult recognitionResult) {
                DateTime now = DateTime.now();
                System.out.println(now.toString() +
                        "\ttime_used=" + (now.getMillis() - beginSend.get().getMillis()) + "ms" +
                        "\tfragment=" + recognitionResult +
                        "\tthread_id=" + Thread.currentThread().getId());
            }
        }, createRequestMeta());
        // 异常回调
        streamContext.enableCallback(new Consumer<GlobalException>() {
            public void accept(GlobalException e) {
                log.error("Exception recognition for asr ：", e);
            }
        });

        try {
            // 这里从文件中得到一个输入流InputStream，实际场景下，也可以从麦克风或者其他音频源来得到InputStream
            final FileInputStream audioStream = new FileInputStream(args.getAudioPath());
            // 实时音频流的情况下，8k音频用320， 16k音频用640
            final byte[] data = new byte[asrClient.getFragmentSize()];
            // 创建延时精确的定时任务
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);
            final CountDownLatch sendFinish = new CountDownLatch(1);
            // 控制台打印每次发包大小
            System.out.println(new DateTime().toString() + "\t" + Thread.currentThread().getId() +
                    " start to send with package size=" + asrClient.getFragmentSize());
            // 设置发送开始时间
            beginSend.set(DateTime.now());
            // 开始执行定时任务
            executor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    try {
                        int count = 0;
                        // 判断音频有没有发送和处理完成
                        if ((count = audioStream.read(data)) != -1 && !streamContext.getFinishLatch().finished()) {
                            // 发送音频数据包
                            streamContext.send(data);
                        } else {
                            // 音频处理完成，置0标记，结束所有线程任务
                            sendFinish.countDown();
                        }
                    } catch (GlobalException | IOException e) {
                        e.printStackTrace();
                        // 异常时，置0标记，结束所有线程任务
                        sendFinish.countDown();
                    }

                }
            }, 0, 20, TimeUnit.MILLISECONDS); // 0:第一次发包延时； 20:每次任务间隔时间; 单位：ms
            // 阻塞主线程，直到CountDownLatch的值为0时停止阻塞
            sendFinish.await();
            System.out.println(new DateTime().toString() + "\t" + Thread.currentThread().getId() + " send finish");

            // 结束定时任务
            executor.shutdown();
            streamContext.complete();
            // 等待最后输入的音频流识别的结果返回完毕（如果略掉这行代码会造成音频识别不完整!）
            streamContext.await();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            asrClient.shutdown();
        }
        System.out.println("all task finished");
    }


    @Data
    public static class Args {
        @Option(name = "-ip", required = true, usage = "asr server ip")
        private String ip = "127.0.0.1";
        @Option(name = "-port", usage = "asr server port")
        private int port = 8051;
        @Option(name = "-pid", usage = "asr product id")
        private String productId = "1912";
        @Option(name = "-username", usage = "username")
        private String username = "admin";
        @Option(name = "-password", usage = "password")
        private String password = "***";
        @Option(name = "-enable-flush-data", handler = BooleanOptionHandler.class, usage = "enable flush data")
        private Boolean enableFlushData = false;
        @Option(name = "-audio-path", usage = "audio save path")
        private String audioPath = "";

        @Option(name = "-app-name", usage = "app name")
        private String appName = "java";

        @Option(name = "-type", usage = "type, file or microphone")
        private String type = "";


        public static Args parse(String[] args) {
            Args iArgs = new Args();
            CmdLineParser parser = new CmdLineParser(iArgs);
            try {
                parser.parseArgument(args);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }
            return iArgs;
        }

        public static AsrProduct parseProduct(String pid) {
            return new AsrProduct(pid, 16000);
        }
    }
}
