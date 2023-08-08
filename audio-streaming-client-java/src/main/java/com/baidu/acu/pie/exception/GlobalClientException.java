// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.exception;

/**
 * AsrClientException 客户端报错， 通常由于用户使用不当或者本地环境问题造成
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
public class GlobalClientException extends RuntimeException {
    public GlobalClientException(String message) {
        super(message);
    }
}
