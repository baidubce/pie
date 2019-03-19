// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.model;

import lombok.Getter;

/**
 * AsrServerLogLevel
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
public enum AsrServerLogLevel {
    OFF(0),
    FATAL(1),
    ERROR(2),
    WARN(3),
    INFO(4),
    DEBUG(5),
    TRACE(6);

    @Getter
    private int code;

    AsrServerLogLevel(int code) {
        this.code = code;
    }}
