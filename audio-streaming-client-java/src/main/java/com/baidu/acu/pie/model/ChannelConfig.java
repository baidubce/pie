package com.baidu.acu.pie.model;

import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@Builder
@ToString
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
