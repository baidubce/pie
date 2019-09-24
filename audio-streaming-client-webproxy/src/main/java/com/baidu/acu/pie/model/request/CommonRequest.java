package com.baidu.acu.pie.model.request;

import lombok.Data;

/**
 * CommonRequest
 * 公共请求类
 */
@Data
public class CommonRequest<T> {
    private String type;
    private T data;
}
