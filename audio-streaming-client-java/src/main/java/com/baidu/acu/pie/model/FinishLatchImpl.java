package com.baidu.acu.pie.model;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.exception.GlobalException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FinishLatchImpl implements FinishLatch {
    private CountDownLatch latch = new CountDownLatch(1);
    private transient GlobalException throwable;
    private Consumer<GlobalException> callback;

    @Override
    public boolean await() throws GlobalException, InterruptedException {
        return this.await(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws GlobalException, InterruptedException {
        boolean result = latch.await(timeout, unit);
        if (throwable != null) {
            throw throwable;
        }
        return result;
    }

    @Override
    public boolean finished() throws GlobalException {
        if (throwable != null) {
            throw throwable;
        }
        return latch.getCount() == 0;
    }

    @Override
    public void enableCallback(Consumer<GlobalException> callback) {
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
     *
     * @param throwable 错误信息
     */
    public void fail(GlobalException throwable) {
        this.throwable = throwable;
        this.finish();
        log.error("Failed and finish");
    }
}
