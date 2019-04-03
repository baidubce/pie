package com.baidu.acu.pie.client;

public interface Consumer<T> {
    void accept(T t);
}
