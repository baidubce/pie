package com.baidu.acu.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 类<code>TtsPlay</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 * @date 2023-08-15
 **/
public class TtsPlay {
    public static String encodingString = "PCM_SIGNED";
    public static String mp3EncodingString = "MPEG2L3";
    private SourceDataLine audioLine = null;

    public TtsPlay() throws LineUnavailableException {
        this(16000, 16, 1, 2);
    }

    public TtsPlay(int sampleRate) throws LineUnavailableException {
        this(sampleRate, 16, 1, 2);
    }

    public TtsPlay(int sampleRate, int sampleSizeBits, int channels, int frameSize)
            throws LineUnavailableException {
        this(sampleRate, sampleSizeBits, channels, frameSize, mp3EncodingString);
    }


    public TtsPlay(int sampleRate, int sampleSizeBits, int channels, int frameSize, String encodingFormat)
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
        TtsPlay audioPlayUtil = null;
        try {
            audioPlayUtil = new TtsPlay(16000, 16, 1, 2, mp3EncodingString);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        }

        audioPlayUtil.playPcmFile("./temp/audio.pcm");
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

    public void playStream(byte[] b) {
        audioLine.write(b, 0, b.length);
    }

    public void close() {
        audioLine.close();
    }
}
