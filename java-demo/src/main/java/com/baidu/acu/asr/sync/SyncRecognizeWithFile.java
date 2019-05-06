package com.baidu.acu.asr.sync;

import java.io.File;
import java.util.List;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.RecognitionResult;

/**
 * 同步识别，输入一个音频文件，线程会进入等待，直到识别完毕，返回结果
 * 通常用于对实时性要求不高的场景，如离线语音分析
 *
 * @author xutengchao
 * @create 2019-05-05 17:14
 */
public class SyncRecognizeWithFile {

    private static String appName = "test";
    private static String ip = "180.76.107.131";    // asr服务的ip地址
    private static Integer port = 8050;             // asr服务的端口
    private static String pid = "1906";             // asr模型编号(不同的模型在不同的场景下asr识别的最终结果可能会存在很大差异)
    private static String userName = "user1";       // 用户名
    private static String passWord = "password1";   // 密码
    private static String audioPath = "/Users/v_xutengchao/Desktop/data-audios/60s.wav"; // 音频文件路径

    public static void main(String[] args) {
        syncRecognizeWithFile();
    }

    private static void syncRecognizeWithFile() {
        // 创建调用asr服务的客户端
        // asrConfig构造后就不可修改
        AsrConfig asrConfig = new AsrConfig()
                .appName(appName)
                .serverIp(ip)
                .serverPort(port)
                .productId(pid)
                .userName(userName)
                .password(passWord);
        AsrClient asrClient = AsrClientFactory.buildClient(asrConfig);
        List<RecognitionResult> results = asrClient.syncRecognize(new File(audioPath));
        // don't forget to shutdown !!!
        asrClient.shutdown();
        printResult(results);
    }

    private static void printResult(List<RecognitionResult> results) {
        for (RecognitionResult result : results) {
            System.out.println(String.format(AsrConfig.TITLE_FORMAT,
                    "file_name",
                    "serial_num",
                    "start_time",
                    "end_time",
                    "result"));
            System.out.println(String.format(AsrConfig.TITLE_FORMAT,
                    audioPath,
                    result.getSerialNum(),
                    result.getStartTime(),
                    result.getEndTime(),
                    result.getResult()
            ));
        }
    }
}
