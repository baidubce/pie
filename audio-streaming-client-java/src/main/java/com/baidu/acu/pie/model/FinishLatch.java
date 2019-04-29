package com.baidu.acu.pie.model;

import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.exception.AsrException;

import java.util.concurrent.TimeUnit;

public interface FinishLatch {
    /**
     * 同步等待，直到会话结束, 或者发生了异常情况
     *
     * @return true 结束； false 不可能
     * @throws AsrException 会话发生异常中断
     */
    boolean await() throws AsrException, InterruptedException;

    /**
     * 同步等待，直到会话结束 或者超时发生
     *
     * @return true 结束； false 在指定的超时时间内， 会话还没有结束
     * @throws AsrException 会话发生异常中断
     */
    boolean await(long timeout, TimeUnit unit) throws AsrException, InterruptedException;

    /**
     * 非阻塞测试会话是否已经结束, 该调用会立即返回;
     * 会话被异常中断时，调用该方法会抛出异常;
     *
     * @return true 结束； false 没有结束
     * @throws AsrException 会话被异常中断时，抛出异常
     */
    boolean finished() throws AsrException;

    /**
     * 一次asr stream 结束的回调，可以不设置;
     * 当会话因为失败回调的时候， consume 传入的asrException != null;
     *
     * @param callback asr结束的回调，传入的asrException表示这次asr调用是否发生了异常。如果!=null，表示异常结束；
     */
    void enableCallback(Consumer<AsrException> callback);

}
