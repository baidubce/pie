package com.baidu.acu.asr.sync;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;

/**
 * 同步识别: 输入一个音频文件,线程会进入等待,直到识别完毕,一次性返回所有结果
 * 使用场景: 通常用于对实时性要求不高的场景,如离线语音分析
 *
 * @author xutengchao
 * @create 2019-05-05 17:24
 */
public class SyncRecognizeWithStreamAndMetaData {
    private static String appName = "test";
    private static String ip = "";          // asr服务的ip地址
    private static Integer port = 8050;     // asr服务的端口
    private static String pid = "1906";     // asr模型编号(不同的模型在不同的场景下asr识别的最终结果可能会存在很大差异)
    private static String userName = "";    // 用户名, 请联系百度相关人员进行申请
    private static String passWord = "";    // 密码, 请联系百度相关人员进行申请
    private static String audioPath = "/Users/v_xutengchao/Desktop/data-audios/60s.wav"; // 音频文件路径

    public static void main(String[] args) {
        syncRecognizeWithStream();
    }

    private static void syncRecognizeWithStream() {
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
        // 创建metaData
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.sendPerSeconds(0.05); //指定每次发送的音频数据包大小，数值越大，识别越快，但准确率可能下降
        requestMetaData.sendPackageRatio(1);  //用来控制发包大小的倍率，一般不需要修改
        requestMetaData.sleepRatio(1);        //指定asr服务的识别间隔，数值越小，识别越快，但准确率可能下降
        List<RecognitionResult> results = null;
        try {
            results = asrClient.syncRecognize(new FileInputStream(audioPath), requestMetaData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
