package com.baidu.acu.pie.utils;

import org.joda.time.LocalTime;

/**
 * LocalTimeUtil
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2020/10/23 5:06 下午
 */
public class LocalTimeUtil {
    public static float parseLocalTimeToSeconds(LocalTime localTime) {
        int hour = localTime.getHourOfDay();
        int minute = localTime.getMinuteOfHour();
        int second = localTime.getSecondOfMinute();
        int millis = localTime.getMillisOfSecond();

        return hour * 3600 + minute * 60 + second + (float) millis / 1000;

    }
}
