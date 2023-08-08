package com.baidu.acu.pie.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

/**
 * 类<code>TtsConfig</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 **/
@Builder
@Data
@Slf4j
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TtsConfig {
    /**
     * asr流式服务器的地址，私有化版本请咨询供应商
     */
    @NonNull
    private String serverIp;

    /**
     * asr流式服务的端口，私有化版本请咨询供应商
     */
    private int serverPort;

    /**
     * 服务端的日志输出级别
     */
    @Builder.Default
    private AsrServerLogLevel logLevel = AsrServerLogLevel.INFO;


    /**
     * ssl使用标志
     */
    @Builder.Default
    private boolean sslUseFlag = false;

    /**
     * ssl客户端根证书路径
     */
    private String sslPath;

}
