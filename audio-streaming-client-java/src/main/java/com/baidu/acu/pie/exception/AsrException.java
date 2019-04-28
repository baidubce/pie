// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.exception;

import lombok.Getter;

/**
 * 服务端返回报错
 */
@Getter
public class AsrException extends RuntimeException {
    private int errorCode;

    public AsrException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AsrException(int errorCode, Throwable t) {
        super(t);
        this.errorCode = errorCode;
    }
}
