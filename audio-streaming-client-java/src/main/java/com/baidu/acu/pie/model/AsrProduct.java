// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.model;

import lombok.Getter;

/**
 * AsrProduct
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
public enum AsrProduct {
    CUSTOMER_SERVICE("客服模型", "1903", 2560),
    CUSTOMER_SERVICE_FINANCE("客服模型：金融领域", "1906", 2560),
    INPUT_METHOD("输入法模型", "888", 5120),
    FAR_FIELD("远场模型", "1888", 5120);

    @Getter
    private String name;

    @Getter
    private String code;

    @Getter
    private int fragmentSize;

    AsrProduct(String name, String code, int fragmentSize) {
        this.name = name;
        this.code = code;
        this.fragmentSize = fragmentSize;
    }
}
