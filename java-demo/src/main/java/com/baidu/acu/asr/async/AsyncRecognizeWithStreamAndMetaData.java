package com.baidu.acu.asr.async;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.exception.GlobalException;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 异步识别: 会准实时返回每个句子的结果.输入一个语音流以及自定义RequestMetaData对象,用来控制请求时候的数据发送速度等参数
 * 使用场景: 用于对实时性要求较高的场景,如会议记录
 *
 * @author xutengchao
 * @literal create 2019-05-06 16:59
 */
public class AsyncRecognizeWithStreamAndMetaData {

    private static String appName = "test";
    private static String ip = "";          // asr服务的ip地址
    private static Integer port = 8051;     // asr服务的端口
    private static AsrProduct pid = AsrProduct.SPEECH_SERVICE;     // asr模型编号(不同的模型在不同的场景下asr识别的最终结果可能会存在很大差异)
    private static String userName = "admin";    // 用户名, 请联系百度相关人员进行申请
    private static String passWord = "1234567809";    // 密码, 请联系百度相关人员进行申请
    private static String audioPath = "testaudio/xeq16k.wav"; // 音频文件路径
    private static boolean enableFlushData = true;
    private static Integer sleepRatio = 0;
    private static String sslPath = ""; // 证书信息，不传递或者留空则使用http 协议，不为空使用该证书协议(https协议)
    private static boolean enableVadPause = false;
    private static int vadPauseFrame = 70;
    private static boolean pureResult = false;
    private static Logger logger = LoggerFactory.getLogger(AsyncRecognizeWithStream.class);

    public static void main(String[] args) {
        // java -jar java-demo-1.0-SNAPSHOT-jar-with-dependencies.jar -app-name hello -ip 127.0.0.1 -port 8051 -pid 1912 -username username -password password -enable-flush-data true -audio-path /path/of/audio.wav
        parseArgs(args);
        asyncRecognizeWithStreamAndMetaData(createAsrClient());
    }

    private static void parseArgs(String[] args) {

        Options options = new Options();
        options.addOption("h", "help", false, "list help");
        options.addOption("a", "app-name", true, "set app name");
        options.addOption("i", "ip", true, "set asr server ip");
        options.addOption("p", "port", true, "set asr server port");
        options.addOption("d", "pid", true, "set product id, like 1903");
        options.addOption("u", "username", true, "set username");
        options.addOption("w", "password", true, "set password");
        options.addOption("e", "enable-flush-data", true, "set enable flush data，true or false");
        options.addOption("t", "audio-path", true, "set audio path");
        options.addOption("r", "sleep-ratio", true, "set sleep ratio");
        options.addOption("s", "ssl-path", true, "set ssl path, like: ca/server.crt");
        options.addOption("v", "enable-vad-pause", true, "set enable vad pause, true or false");
        options.addOption("f", "vad-pause-frame", true, "set vad pause frame");
        options.addOption("l", "pure-result", true, "set pure result, true or false");


        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (Exception e) {
            e.printStackTrace();
            formatter.printHelp("RandomGenerator", options, true);
            System.exit(0);
        }

        if (cmd.hasOption('h') || cmd.hasOption("help")) {
            formatter.printHelp("RandomGenerator", options, true);
            System.exit(0);
        }

        if (cmd.hasOption('a')) {
            appName = cmd.getOptionValue("a");
        }
        if (cmd.hasOption("app-name")) {
            appName = cmd.getOptionValue("app-name");
        }

        if (cmd.hasOption('i')) {
            ip = cmd.getOptionValue("i");
        }
        if (cmd.hasOption("ip")) {
            ip = cmd.getOptionValue("ip");
        }

        if (cmd.hasOption('p')) {
            port = Integer.parseInt(cmd.getOptionValue("p"));
        }
        if (cmd.hasOption("port")) {
            port = Integer.parseInt(cmd.getOptionValue("p"));
        }

        if (cmd.hasOption('d')) {
            pid = getAsrProduct(cmd.getOptionValue("d"));
        }
        if (cmd.hasOption("product-id")) {
            pid = getAsrProduct(cmd.getOptionValue("product-id"));
        }

        if (cmd.hasOption('u')) {
            userName = cmd.getOptionValue("u");
        }
        if (cmd.hasOption("username")) {
            userName = cmd.getOptionValue("username");
        }

        if (cmd.hasOption('w')) {
            passWord = cmd.getOptionValue("w");
        }
        if (cmd.hasOption("password")) {
            passWord = cmd.getOptionValue("password");
        }

        if (cmd.hasOption('t')) {
            audioPath = cmd.getOptionValue("t");
        }
        if (cmd.hasOption("audio-path")) {
            audioPath = cmd.getOptionValue("audio-path");
        }

        if (cmd.hasOption('e')) {
            enableFlushData = Boolean.parseBoolean(cmd.getOptionValue("e"));
        }
        if (cmd.hasOption("enable-flush-data")) {
            enableFlushData = Boolean.parseBoolean(cmd.getOptionValue("enable-flush-data"));
        }

        if (cmd.hasOption('r')) {
            sleepRatio = Integer.parseInt(cmd.getOptionValue("r"));
        }
        if (cmd.hasOption("sleep-ratio")) {
            sleepRatio = Integer.parseInt(cmd.getOptionValue("sleep-ratio"));
        }
        if (cmd.hasOption("s")) {
            sslPath = cmd.getOptionValue("s");
        }
        if (cmd.hasOption("ssl-path")) {
            sslPath = cmd.getOptionValue("ssl-path");
        }

        if (cmd.hasOption("v")) {
            enableVadPause = Boolean.parseBoolean(cmd.getOptionValue("enable-vad-pause"));
        }
        if (cmd.hasOption("enable-vad-pause")) {
            enableVadPause = Boolean.parseBoolean(cmd.getOptionValue("enable-vad-pause"));
        }
        if (cmd.hasOption("f")) {
            vadPauseFrame = Integer.parseInt(cmd.getOptionValue("vad-pause-frame"));
        }
        if (cmd.hasOption("vad-pause-frame")) {
            vadPauseFrame = Integer.parseInt(cmd.getOptionValue("vad-pause-frame"));
        }
        if (cmd.hasOption("l")) {
            pureResult = Boolean.parseBoolean(cmd.getOptionValue("l"));
        }
        if (cmd.hasOption("pure-result")) {
            pureResult = Boolean.parseBoolean(cmd.getOptionValue("pure-result"));
        }
    }

    private static AsrProduct getAsrProduct(String pid) {
        return new AsrProduct(pid, 16000);
    }

    private static AsrClient createAsrClient() {
        // 创建调用asr服务的客户端
        // asrConfig构造后就不可修改
        AsrConfig asrConfig;
        if (sslPath.length() > 0) {
            asrConfig = AsrConfig.builder()
                .appName(appName)
                .serverIp(ip)
                .serverPort(port)
                .product(pid)
                .userName(userName)
                .password(passWord)
                .sslPath(sslPath)
                .sslUseFlag(true)
                .build();
        } else {
            asrConfig = AsrConfig.builder()
                .appName(appName)
                .serverIp(ip)
                .serverPort(port)
                .product(pid)
                .userName(userName)
                .password(passWord)
                .build();
    }
        return AsrClientFactory.buildClient(asrConfig);
    }

    private static void asyncRecognizeWithStreamAndMetaData(AsrClient asrClient) {
        // 创建RequestMetaData
        RequestMetaData requestMetaData = new RequestMetaData();

        requestMetaData.setSendPerSeconds(0.02); // 指定每次发送的音频数据包大小，数值越大，识别越快，但准确率可能下降
        requestMetaData.setSendPackageRatio(1);  // 用来控制发包大小的倍率，一般不需要修改
        requestMetaData.setSleepRatio(sleepRatio);        // 指定asr服务的识别间隔，数值越小，识别越快，但准确率可能下降
        requestMetaData.setTimeoutMinutes(120);  // 识别单个文件的最大等待时间，默认10分，最长不能超过120分
        requestMetaData.setEnableFlushData(enableFlushData);// 是否返回中间翻译结果
        requestMetaData.setEnableVadPause(enableVadPause);
        requestMetaData.setVadPauseFrame(vadPauseFrame);

        final AtomicReference<DateTime> beginSend = new AtomicReference<>();
        final StreamContext streamContext = asrClient.asyncRecognize(recognitionResult -> {
            if (pureResult ) {
                if (recognitionResult.isCompleted()) {
                    // 这里仅仅展示识别结果文本，不展示额外信息
                    System.out.println(recognitionResult.getResult());
                }
            } else {
                DateTime now = DateTime.now();
                System.out.println(now +
                        "\ttime_used=" + (now.getMillis() - beginSend.get().getMillis()) + "ms" +
                        "\tfragment=" + recognitionResult +
                        "\tthread_id=" + Thread.currentThread().getId());
            }
        }, requestMetaData);
        // 异常回调
        streamContext.enableCallback(e -> {
            if (e != null) {
                logger.error("Exception recognition for asr ： ", e);
            }
        });

        try {
            // 这里从文件中得到一个输入流InputStream，实际场景下，也可以从麦克风或者其他音频源来得到InputStream
            final FileInputStream audioStream = new FileInputStream(audioPath);
            // 实时音频流的情况下，8k音频用320， 16k音频用640
            final byte[] data = new byte[asrClient.getFragmentSize(requestMetaData)];
            // 创建延时精确的定时任务
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);
            final CountDownLatch sendFinish = new CountDownLatch(1);
            // 控制台打印每次发包大小
            System.out.println(new DateTime() + "\t" + Thread.currentThread().getId() +
                    " start to send with package size=" + asrClient.getFragmentSize(requestMetaData));
            // 设置发送开始时间
            beginSend.set(DateTime.now());
            // 开始执行定时任务
            executor.scheduleAtFixedRate(() -> {
                try {
                    int count = 0;
                    // 判断音频有没有发送和处理完成
                    if (audioStream.read(data) != -1 && !streamContext.getFinishLatch().finished()) {
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
            }, 0, 20, TimeUnit.MILLISECONDS); // 0:第一次发包延时； 20:每次任务间隔时间; 单位：ms
            // 阻塞主线程，直到CountDownLatch的值为0时停止阻塞
            sendFinish.await();
            System.out.println(new DateTime() + "\t" + Thread.currentThread().getId() + " send finish");

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
}
