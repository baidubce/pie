// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.model;

import lombok.Getter;

/**
 * AsrProduct
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
public class AsrProduct {
    // 设置一些product兼容旧版本使用enum的情况
    public static AsrProduct CUSTOMER_SERVICE = new AsrProduct("客服模型", "1903", 8000);
    public static AsrProduct CUSTOMER_SERVICE_TOUR = new AsrProduct("客服模型：旅游领域", "1904", 8000);
    public static AsrProduct CUSTOMER_SERVICE_STOCK = new AsrProduct("客服模型", "1903", 8000);
    public static AsrProduct CUSTOMER_SERVICE_FINANCE = new AsrProduct("客服模型：股票领域", "1905", 8000);
    public static AsrProduct CUSTOMER_SERVICE_ENERGY = new AsrProduct("客服模型：能源领域", "1907", 8000);
    public static AsrProduct INPUT_METHOD = new AsrProduct("输入法模型", "888", 16000);
    public static AsrProduct FAR_FIELD = new AsrProduct("远场模型", "1888", 16000);
    public static AsrProduct FAR_FIELD_ROBOT = new AsrProduct("远场模型：机器人领域", "1889", 16000);
    public static AsrProduct SPEECH_SERVICE = new AsrProduct("演讲模型", "1912", 16000);

    @Getter
    private String name;

    @Getter
    private String code;

    @Getter
    private int sampleRate;

    public AsrProduct(String name, String code, int sampleRate) {
        this.name = name;
        this.code = code;
        this.sampleRate = sampleRate;
    }

    public AsrProduct(String code, int sampleRate) {
        this("自定义模型", code, sampleRate);
    }
}
