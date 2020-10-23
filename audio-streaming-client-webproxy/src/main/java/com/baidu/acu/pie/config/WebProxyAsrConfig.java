package com.baidu.acu.pie.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AsrConfig
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2020/10/22 9:13 下午
 */
@Configuration
@ConfigurationProperties(prefix = "asr")
@Data
public class WebProxyAsrConfig {
    private String server;
    private int port;
}
