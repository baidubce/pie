package com.baidu.acu.pie.retrofit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * KafkaHttpConfig
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2019/11/25 5:26 下午
 */
@Builder
@Data
@Slf4j
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class KafkaHttpConfig {
    private String server;
    private int port;
}
