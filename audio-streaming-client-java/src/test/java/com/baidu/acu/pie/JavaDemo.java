// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;

/**
 * JavaDemo
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
public class JavaDemo {

    @Test
    public void testSendFile() {
        String audioFilePath = "testaudio/bj8k.wav";

        // asrConfig构造后就不可修改
        AsrConfig asrConfig = new AsrConfig()
                .serverIp("180.76.107.131")
                .serverPort(8050)
                .appName("simple demo")
                .product(AsrProduct.CUSTOMER_SERVICE);

        AsrClient asrClient = AsrClientFactory.buildClient(asrConfig);
        List<RecognitionResult> results = asrClient.syncRecognize(Paths.get(audioFilePath));

        // don't forget to shutdown !!!
        asrClient.shutdown();

        for (RecognitionResult result : results) {
            System.out.println(String.format(AsrConfig.TITLE_FORMAT,
                    "serial_num",
                    "err_no",
                    "err_message",
                    "start_time",
                    "end_time",
                    "result"));
            System.out.println(String.format(AsrConfig.TITLE_FORMAT,
                    result.getSerialNum(),
                    result.getErrorCode(),
                    result.getErrorMessage(),
                    result.getStartTime(),
                    result.getEndTime(),
                    result.getResult()
            ));
        }
    }
}
