package com.baidu.acu.pie.service;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.config.WebProxyAsrConfig;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.request.AsrInitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * MessageHandlerService
 * 接收消息处理类
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageHandlerService {

    private final WebProxyAsrConfig webProxyAsrConfig;

    private AsrConfig getAsrConfig(AsrInitRequest asrInitRequest) {
        AsrConfig asrConfig = AsrConfig.builder()
                .serverIp(webProxyAsrConfig.getServer())
                .serverPort(webProxyAsrConfig.getPort())
                .userName(asrInitRequest.getUserName())
                .password(asrInitRequest.getPassword())
                .appName(asrInitRequest.getAppName())
                .product(getAsrProduct(asrInitRequest.getProductId()))
                .build();

        return asrConfig;
    }

    public AsrClient getAsrClient(AsrInitRequest asrInitRequest) {
        return AsrClientFactory.buildClient(getAsrConfig(asrInitRequest));
    }


    public RequestMetaData getRequestMetaData(AsrInitRequest asrInitRequest) {
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setSendPackageRatio(asrInitRequest.getSendPerSeconds());
        requestMetaData.setSleepRatio(asrInitRequest.getSleepRatio());
        requestMetaData.setTimeoutMinutes(120);
        requestMetaData.setEnableFlushData(asrInitRequest.isEnableFlushData());

        return requestMetaData;
    }

    public AsrProduct getAsrProduct(String productId) {
        for (AsrProduct asrProduct : AsrProduct.values()) {
            if (productId.equals(asrProduct.getCode())) {
                return asrProduct;
            }
        }
        return null;
    }
}
