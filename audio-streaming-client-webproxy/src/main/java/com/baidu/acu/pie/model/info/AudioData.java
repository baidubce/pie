package com.baidu.acu.pie.model.info;

import lombok.Data;

import java.io.InputStream;

/**
 * AudioData
 * 音频数据（客户端可能传输音频二进制流，或者音频id两种形式）
 */
@Data
public class AudioData {
    //音频二进制流
    private byte[] audioBytes;
    private boolean close;
    private String audioId;
    private InputStream inputStream;

    public AudioData(byte[] audioBytes, boolean close) {
        this.audioBytes = audioBytes;
        this.close = close;
    }
}
