package com.baidu.acu.pie.util;

import static com.baidu.acu.pie.model.Constants.ASR_RECOGNITION_RESULT_TIME_FORMAT;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;

/**
 * DateTimeParser
 *
 * @author Shu Lingjie
 */
@Slf4j
public class DateTimeParser {
    public static LocalTime parseLocalTime(String time) {
        String toBeParsed;

        if (time.matches("\\d{2}:\\d{2}\\.\\d{2}")) { // mm:ss.SS like 01:00.40
            toBeParsed = "00:" + time + "0";
        } else if (time.matches("\\d{2}:\\d{2}\\.\\d{3}")) { // mm:ss.SSS without HH:
            toBeParsed = "00:" + time;
        } else {
            toBeParsed = time;
        }

        DateTimeFormatter asrRecognitionResultTimeFormatter =
                DateTimeFormat.forPattern(ASR_RECOGNITION_RESULT_TIME_FORMAT);

        LocalTime ret = LocalTime.MIDNIGHT;

        try {
            ret = LocalTime.parse(toBeParsed, asrRecognitionResultTimeFormatter);
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            log.warn("parse time failed, the time string from asr sdk is : {}, exception: ", time, e);
        }
        return ret;
    }
}
