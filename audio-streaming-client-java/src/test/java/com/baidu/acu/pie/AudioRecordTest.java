package com.baidu.acu.pie;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.io.File;
import java.io.FileOutputStream;

/**
 * AudioRecordTest java录音
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2020/4/26 4:27 下午
 */
public class AudioRecordTest {


    private static FileOutputStream os;
    //采样率
    private static float RATE = 16000f;
    //编码格式PCM
    private static AudioFormat.Encoding ENCODING = AudioFormat.Encoding.PCM_SIGNED;
    //帧大小 16
    private static int SAMPLE_SIZE = 16;
    //是否大端
    private static boolean BIG_ENDIAN = false;
    //通道数
    private static int CHANNELS = 1;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            save("/Users/xiashuai01/Documents/123.pcm");
        } else {
            save(args[0]);
        }
    }

    public static void save(String path) throws Exception {
        File file = new File(path);

        if (file.isDirectory()) {
            if (!file.exists()) {
                file.mkdirs();
            }
            file.createNewFile();
        }

        AudioFormat audioFormat = new AudioFormat(ENCODING, RATE, SAMPLE_SIZE, CHANNELS, (SAMPLE_SIZE / 8) * CHANNELS,
                RATE, BIG_ENDIAN);
        TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
        targetDataLine.open();
        targetDataLine.start();
        byte[] b = new byte[256];
        int flag = 0;
        os = new FileOutputStream(file);
        while ((flag = targetDataLine.read(b, 0, b.length)) > 0) {//从声卡中采集数据
            os.write(b);
            System.out.println(flag);
        }
    }
}
