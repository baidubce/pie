package com.baidu.acu.asr.Async;

import java.io.FileInputStream;
import org.joda.time.DateTime;
import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.StreamContext;

/**
 * AsyncRecognizeWithStream
 *
 * @author xutengchao
 * @create 2019-05-06 16:58
 */
public class AsyncRecognizeWithStream {

    private static String appName = "test";
    private static String ip = "180.76.107.131";    // asr服务的ip地址
    private static Integer port = 8050;             // asr服务的端口
    private static String pid = "1906";             // asr模型编号(不同的模型在不同的场景下asr识别的最终结果可能会存在很大差异)
    private static String userName = "user1";       // 用户名
    private static String passWord = "password1";   // 密码
    private static String audioPath = "/Users/v_xutengchao/Desktop/data-audios/60s.wav"; // 音频文件路径

    public static void main(String[] args) {
        asyncRecognizeWithStream(createAsrClient());
    }

    private static AsrClient createAsrClient() {
        // 创建调用asr服务的客户端
        // asrConfig构造后就不可修改
        AsrConfig asrConfig = new AsrConfig()
                .appName(appName)
                .serverIp(ip)
                .serverPort(port)
                .productId(pid)
                .userName(userName)
                .password(passWord);
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
            // 使用 sender.onNext 方法，将 InputStream 中的数据不断地发送到 asr 后端，发送的最小单位是 AudioFragment
            while ((readSize = audioStream.read(data)) != -1 && !streamContext.getFinishLatch().finished()) {
                streamContext.send(data);
                // 主动休眠一段时间，来模拟人说话场景下的音频产生速率
                // 在对接麦克风等设备的时候，可以去掉这个 sleep
                Thread.sleep(20);
            }
            System.out.println(new DateTime().toString() + "\t" + Thread.currentThread().getId() + " send finish");
            streamContext.complete();
            // wait to ensure to receive the last response
            streamContext.getFinishLatch().await();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            asrClient.shutdown();
        }

        System.out.println("all task finished");
    }

}
