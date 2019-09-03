package com.baidu.acu.pie.service;

import com.baidu.acu.pie.handler.AudioAsrHandler;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * session管理
 */
@Component
public class SessionManager {

    private Map<String, AudioAsrHandler> map = new ConcurrentHashMap<>();

    /**
     * 将接收到的信息放入对应的
     */
    public void put(Session session, byte[] data) {
        String id = session.getId();
        if (!map.containsKey(id)) {
            register(session);
        }
        AudioAsrHandler asr = map.get(id);
        asr.offer(data);
    }

    /**
     * 将session注册至map中
     */
    private synchronized void register(Session session) {
        String id = session.getId();
        if (map.containsKey(id)) {
            return;
        }
        AudioAsrHandler asrHandler = new AudioAsrHandler(session, this);
        map.put(session.getId(), asrHandler);
        new Thread(asrHandler).start();
    }

    /**
     * 取消注册
     */
    public void unRegister(Session session) {
        map.remove(session.getId());
    }
}
