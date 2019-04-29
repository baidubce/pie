package com.baidu.acu.pie.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.concurrent.TimeUnit;

@Getter
@Builder
public class ChannelConfig {
    @Builder.Default
    private final TimeSpan keepAliveTime = new TimeSpan(30, TimeUnit.SECONDS);
    @Builder.Default
    private final TimeSpan keepAliveTimeout = new TimeSpan(5, TimeUnit.SECONDS);

    @AllArgsConstructor
    @Getter
    @ToString
    public static class TimeSpan {
        private final long time;
        private final TimeUnit timeUnit;
    }
}
