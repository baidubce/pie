package com.baidu.acu.pie.demo;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 类<code>TtsPlayDemo</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 **/
public class TtsPlayDemo {
    private static String encodingString = "PCM_SIGNED";
    private static String mp3EncodingString = "MPEG2L3";
    private SourceDataLine audioLine = null;

    public TtsPlayDemo() throws LineUnavailableException {
        this(16000, 16, 1, 2);
    }

    public TtsPlayDemo(int sampleRate) throws LineUnavailableException {
        this(sampleRate, 16, 1, 2);
    }

    public TtsPlayDemo(int sampleRate, int sampleSizeBits, int channels, int frameSize)
            throws LineUnavailableException {
        this(sampleRate, sampleSizeBits, channels, frameSize, mp3EncodingString);
    }


    public TtsPlayDemo(int sampleRate, int sampleSizeBits, int channels, int frameSize, String encodingFormat)
            throws LineUnavailableException {

        AudioFormat.Encoding encoding = new AudioFormat.Encoding(encodingFormat);

        // 编码格式，采样率，每个样本的位数，声道，帧长（字节），帧数，是否按big-endian字节顺序存储
        AudioFormat format = new AudioFormat(encoding, sampleRate, sampleSizeBits, channels, frameSize,
                (float) (sampleRate * sampleSizeBits * channels / (frameSize * 8)), false);

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        audioLine = (SourceDataLine) AudioSystem.getLine(info);

        audioLine.open(format);

        audioLine.start();
    }

    public static void main(String[] args) {
        TtsPlayDemo audioPlayUtil = null;
        try {
            audioPlayUtil = new TtsPlayDemo(16000, 16, 1, 2, mp3EncodingString);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        }

//        audioPlayUtil.playPcmFile("./temp/audio.pcm");
        audioPlayUtil.playMp3File("/Users/xiashuai01/MyProject/multi/pie/audio-streaming-client-java/temp/audio.mp3");
        audioPlayUtil.close();
    }

    public void playPcmFile(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            byte[] b = new byte[256];
            try {
                while (fis.read(b) > 0) {
                    playStream(b);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void playMp3File(String path) {
        try {

            FileInputStream fis = new FileInputStream(path);
//            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path));
//            System.out.println(audioInputStream.getFormat());
            byte[] b = new byte[256];
            try {
                while (fis.read(b) > 0) {
                    playStream(b);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void playStream(byte[] b) {
        audioLine.write(b, 0, b.length);
    }

    public void close() {
        audioLine.close();
    }
}
