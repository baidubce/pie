package com.baidu.acu.pie.model;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.exception.AsrException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FinishLatchImpl implements FinishLatch {
    private CountDownLatch latch = new CountDownLatch(1);
    private transient AsrException throwable;
    private Consumer<AsrException> callback;

    @Override
    public boolean await() throws AsrException, InterruptedException {
        return this.await(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws AsrException, InterruptedException {
        boolean result = latch.await(timeout, unit);
        if (throwable != null) {
            throw throwable;
        }
        return result;
    }

    @Override
    public boolean finished() throws AsrException {
        if (throwable != null) {
            throw throwable;
        }
        return latch.getCount() == 0;
    }

    @Override
    public void enableCallback(Consumer<AsrException> callback) {
        this.callback = callback;
    }

    /**
     * 内部接口， 勿使用
     */
    public void finish() {
        this.latch.countDown();
        if (callback != null) {
            callback.accept(this.throwable);
        }
    }

    /**
     * 内部接口， 勿使用
     */
    public void fail(AsrException throwable) {
        this.throwable = throwable;
        this.finish();
        log.error("Failed and finish");
    }
}
