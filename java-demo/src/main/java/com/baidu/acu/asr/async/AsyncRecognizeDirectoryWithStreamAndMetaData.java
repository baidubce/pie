package com.baidu.acu.asr.async;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 识别文件夹内的所有wav和pcm音频数据
 *
 * @author xutengchao
 * @literal create 2019-05-06 16:59
 */
public class AsyncRecognizeDirectoryWithStreamAndMetaData {
    private static String appName = "test";
    private static String ip = "127.0.0.1";          // asr服务的ip地址
    private static Integer port = 8051;     // asr服务的端口
    private static AsrProduct pid = AsrProduct.INPUT_METHOD;     // asr模型编号(不同的模型在不同的场景下asr识别的最终结果可能会存在很大差异)
    private static String userName = "admin";    // 用户名, 请联系百度相关人员进行申请
    private static String passWord = "password";    // 密码, 请联系百度相关人员进行申请
    private static String audioFileDir = "dir"; // 音频文件夹路径
    private static boolean enableFlushData = false;
    private static double sleepRatio = 0.5;
    private static String outputFile = "asr_result.txt";
    private static Logger logger = LoggerFactory.getLogger(AsyncRecognizeWithStream.class);

    public static void main(String[] args) {
        //  java -jar java-demo-1.0-SNAPSHOT-jar-with-dependencies.jar -a java -i 127.0.0.1 -p 8051 -d 1903 -u username -w password -e false -t /audio/path -r 0.2 -o asr_result1.txt
        parseArgs(args);
        recognizeDirectory();
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
        options.addOption("o", "output-result", true, "set asr output result file");

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
            audioFileDir = cmd.getOptionValue("t");
        }
        if (cmd.hasOption("audio-directory")) {
            audioFileDir = cmd.getOptionValue("audio-directory");
        }

        if (cmd.hasOption('e')) {
            enableFlushData = Boolean.parseBoolean(cmd.getOptionValue("e"));
        }
        if (cmd.hasOption("enable-flush-data")) {
            enableFlushData = Boolean.parseBoolean(cmd.getOptionValue("enable-flush-data"));
        }

        if (cmd.hasOption('r')) {
            String r = cmd.getOptionValue("r");
            sleepRatio = Double.parseDouble(cmd.getOptionValue("r"));
        }
        if (cmd.hasOption("sleep-ratio")) {
            sleepRatio = Double.parseDouble(cmd.getOptionValue("sleep-ratio"));
        }

        if (cmd.hasOption('o')) {
            outputFile = cmd.getOptionValue("o");
        }
        if (cmd.hasOption("output-result")) {
            outputFile = cmd.getOptionValue("output-result");
        }
    }

    private static AsrProduct getAsrProduct(String pid) {
        // 默认采样率为16000
        return new AsrProduct(pid, 16000);
    }

    private static AsrProduct getAsrProduct(String pid, int sampleRate) {

        return new AsrProduct(pid, sampleRate);
    }

    private static AsrClient createAsrClient() {
        // 创建调用asr服务的客户端
        // asrConfig构造后就不可修改
        AsrConfig asrConfig = AsrConfig.builder()
                .appName(appName)
                .serverIp(ip)
                .serverPort(port)
                .product(pid)
                .userName(userName)
                .password(passWord)
                .build();
        return AsrClientFactory.buildClient(asrConfig);
    }

    private static RequestMetaData createRequestMeta() {
        // 创建RequestMetaData
        RequestMetaData requestMetaData = new RequestMetaData();

        requestMetaData.setSendPerSeconds(0.02); // 指定每次发送的音频数据包大小，数值越大，识别越快，但准确率可能下降
        requestMetaData.setSendPackageRatio(1);  // 用来控制发包大小的倍率，一般不需要修改
        requestMetaData.setSleepRatio(sleepRatio);        // 指定asr服务的识别间隔，数值越小，识别越快，但准确率可能下降
        requestMetaData.setTimeoutMinutes(120);  // 识别单个文件的最大等待时间，默认10分，最长不能超过120分
        requestMetaData.setEnableFlushData(enableFlushData);// 是否返回中间翻译结果

        return requestMetaData;
    }


    public static void recognizeDirectory() {
        AsrClient asrClient = createAsrClient();

        RequestMetaData requestMetaData = createRequestMeta();

        File dir = new File(audioFileDir);
        File[] files = dir.listFiles(pathname -> pathname.getName().endsWith("wav") || pathname.getName().endsWith("pcm"));
        int count = 0;
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(outputFile));

            assert files != null;
            for (File file : files) {
                Instant from = Instant.now();
                List<RecognitionResult> results = asrClient.syncRecognize(file, requestMetaData);
                Instant to = Instant.now();
                Duration between = Duration.between(from, to);
                double time = between.toMillis() / 1000.0;
                String asrResult = results.stream().map(RecognitionResult::getResult).collect(Collectors.joining());
                out.write(String.format("%S:%s ===> %.3fs\n", file.getName(), asrResult, time));
                out.flush();
                count += 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert out != null;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            asrClient.shutdown();
        }
        System.out.println("***********************  count: " + count);
    }
}
