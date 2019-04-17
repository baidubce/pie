package com.baidu.acu.pie.model;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FinishLatch {
    private CountDownLatch latch = new CountDownLatch(1);
    private transient Throwable throwable;

    /**
     * 同步等待，直到会话结束
     *
     * @return true 结束； false 不可能
     * @throws Throwable 会话发生异常中断
     */
    public boolean await() throws Throwable {
        return this.await(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    /**
     * 同步等待，直到会话结束 或者超时发生
     *
     * @return true 结束； false 在指定的超时时间内， 会话还没有结束
     * @throws Throwable 会话发生异常中断
     */
    public boolean await(long timeout, TimeUnit unit) throws Throwable {
        boolean result = latch.await(timeout, unit);
        if (throwable != null) {
            throw throwable;
        }
        return result;
    }

    /**
     * 测试会话是否已经结束
     *
     * @return true 结束； false 没有结束
     * @throws Throwable 会话被异常中断
     */
    public boolean finished() throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
        return latch.getCount() == 0;
    }

    /**
     * 内部接口， 勿使用
     */
    public void finish() {
        this.latch.countDown();
    }

    /**
     * 内部接口， 勿使用
     */
    public void fail(Throwable throwable) {
        this.throwable = throwable;
        this.latch.countDown();
        System.out.println("Failed and finish");
    }
}
