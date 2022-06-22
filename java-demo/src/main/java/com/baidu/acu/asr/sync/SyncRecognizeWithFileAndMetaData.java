package com.baidu.acu.asr.sync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;

/**
 * 同步识别: 通过传入音频文件以及RequestMetaData对象,用来控制请求时候的数据发送速度等参数.
 * 识别开始后线程会进入等待,直到识别完毕,一次性返回所有结果.
 * 使用场景: 通常用于对实时性要求不高的场景，如离线语音分析
 *
 * @author xutengchao
 * @create 2019-05-05 17:23
 */
public class SyncRecognizeWithFileAndMetaData {
    private static String appName = "";
    private static String ip = "";          // asr服务的ip地址
    private static Integer port = 8050;     // asr服务的端口
    private static AsrProduct pid = AsrProduct.SPEECH_SERVICE;     // asr模型编号(不同的模型在不同的场景下asr识别的最终结果可能会存在很大差异)
    private static String userName = "";    // 用户名, 请联系百度相关人员进行申请
    private static String passWord = "";    // 密码, 请联系百度相关人员进行申请
    private static String audioPath = ""; // 音频文件路径

    public static void main(String[] args) throws IOException {
        syncRecognizeWithFileAndMetaData();
    }

    private static void syncRecognizeWithFileAndMetaData() throws IOException {
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
        AsrClient asrClient = AsrClientFactory.buildClient(asrConfig);
        // 创建RequestMetaData
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setEnableFlushData(false);// 是否返回中间翻译结果
        List<RecognitionResult> results = asrClient.syncRecognize(Files.readAllBytes(Paths.get(audioPath)), requestMetaData);
        // don't forget to shutdown !!!
        asrClient.shutdown();
        printResult(results);
    }

    private static void printResult(List<RecognitionResult> results) {
        for (RecognitionResult result : results) {
            System.out.println(String.format(AsrConfig.TITLE_FORMAT,
                    "file_name",
                    "trace_id",
                    "serial_num",
                    "start_time",
                    "end_time",
                    "result"));
            System.out.println(String.format(AsrConfig.TITLE_FORMAT,
                    audioPath,
                    result.getTraceId(),
                    result.getSerialNum(),
                    result.getStartTime(),
                    result.getEndTime(),
                    result.getResult()
            ));
        }
    }
}
