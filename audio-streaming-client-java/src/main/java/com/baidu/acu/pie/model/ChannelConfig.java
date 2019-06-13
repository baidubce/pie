package com.baidu.acu.pie.model;

import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChannelConfig {
    @Builder.Default
    private final TimeSpan keepAliveTime = new TimeSpan(30, TimeUnit.SECONDS);
    @Builder.Default
    private final TimeSpan keepAliveTimeout = new TimeSpan(20, TimeUnit.SECONDS);

    @Data
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSpan {
        private long time;
        private TimeUnit timeUnit;
    }
}
