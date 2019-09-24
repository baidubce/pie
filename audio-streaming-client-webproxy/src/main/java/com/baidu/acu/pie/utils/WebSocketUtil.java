package com.baidu.acu.pie.utils;

import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;

/**
 * 针对webSocket相关的封装
 */
@Slf4j
public class WebSocketUtil {

    /**
     * 向客户端发送消息
     */
    public static void sendMsgToClient(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
