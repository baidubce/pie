// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.model;

import lombok.Getter;

/**
 * AsrProduct
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
public enum AsrProduct {
    CUSTOMER_SERVICE("客服模型", "1903", 8000),
    CUSTOMER_SERVICE_TOUR("客服模型：旅游领域", "1904", 8000),
    CUSTOMER_SERVICE_STOCK("客服模型：股票领域", "1905", 8000),
    CUSTOMER_SERVICE_FINANCE("客服模型：金融领域", "1906", 8000),
    CUSTOMER_SERVICE_ENERGY("客服模型：能源领域", "1907", 8000),
    INPUT_METHOD("输入法模型", "888", 16000),
    FAR_FIELD("远场模型", "1888", 16000);

    @Getter
    private String name;

    @Getter
    private String code;

    @Getter
    private int sampleRate;

    AsrProduct(String name, String code, int sampleRate) {
        this.name = name;
        this.code = code;
        this.sampleRate = sampleRate;
    }
}
