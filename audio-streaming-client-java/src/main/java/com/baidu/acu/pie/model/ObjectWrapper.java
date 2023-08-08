package com.baidu.acu.pie.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 类<code>ObjectWapper</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 **/
public class ObjectWrapper<T> {
    private T t;

    public T get() {
        return t;
    }

    public void set(T t) {
        this.t = t;
    }
}
