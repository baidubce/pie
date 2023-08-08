package com.baidu.acu.pie.client;

import java.io.IOException;

public interface Consumer<T> {
    void accept(T t);
}
