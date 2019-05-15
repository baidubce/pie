package com.baidu.acu.asr.async;

import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.StreamContext;

/**
 * 异步识别: 输入一个语音流,会实时返回每一句话识别的结果（在指定时间最后音频流识别没有返回就结束）
 * 使用场景: 用于对实时性要求较高的场景,如会议记录
 *
 * @author xutengchao
 * @create 2019-05-15 10:03
 */
public class AsyncRecognize {
    private static String appName = "";     // 根据自己需求命名
    private static String ip = "";          // asr服务的ip地址
    private static Integer port = 8050;     // asr服务的端口
    private static AsrProduct pid = AsrProduct.CUSTOMER_SERVICE_FINANCE;     // asr模型(不同的模型在不同的场景下asr识别的最终结果可能会存在很大差异)
    private static String userName = "";    // 用户名, 请联系百度相关人员进行申请
    private static String passWord = "";    // 密码, 请联系百度相关人员进行申请
    private static String audioPath = ""; // 音频文件路径
    private static Long awaitTime = 10L; // 设置最后音频流识别时间限制，单位秒

    public static void main(String[] args) {
        asyncRecognizeWithStream(createAsrClient());
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

    private static void asyncRecognizeWithStream(AsrClient asrClient) {
        StreamContext streamContext = asrClient.asyncRecognize(new Consumer<RecognitionResult>() {
            public void accept(RecognitionResult recognitionResult) {
                System.out.println(
                        DateTime.now().toString() + "\t" + Thread.currentThread().getId() +
                                " receive fragment: " + recognitionResult);
            }
        });
        // 这里从文件中得到一个InputStream，实际场景下，也可以从麦克风或者其他音频源来得到InputStream
        try {
            FileInputStream audioStream = new FileInputStream(audioPath);
            byte[] data = new byte[asrClient.getFragmentSize()];
            int readSize;
            System.out.println(new DateTime().toString() + "\t" + Thread.currentThread().getId() + " start to send");
            // 使用 send 方法，将 InputStream 中的数据不断地发送到 asr 后端，发送的最小单位是 AudioFragment
            while ((readSize = audioStream.read(data)) != -1 && !streamContext.getFinishLatch().finished()) {
                streamContext.send(data);
                // 主动休眠一段时间，来模拟人说话场景下的音频产生速率
                // 在对接麦克风等设备的时候，可以去掉这个 sleep
                Thread.sleep(20);
            }
            streamContext.complete();
            System.out.println(new DateTime().toString() + "\t" + Thread.currentThread().getId() + " send finish");
            // 等待最后输入的音频流识别的结果返回完毕（如果略掉这行代码会造成音频识别不完整!）(等待最长的时间)
            if (!streamContext.await(awaitTime, TimeUnit.SECONDS)) {
                System.out.println("error : The last audio stream recognized timeout");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            asrClient.shutdown();
        }

        System.out.println("all task finished");
    }
}
