package com.baidu.acu.pie.utils;

import com.baidu.acu.pie.model.response.ServerResponse;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.io.IOException;

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
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 向客户端发送消息
     */
    public static void sendFailureMsgToClient(Session session, String message) {
        try {
            sendMsgToClient(session,
                    ServerResponse.failureStrResponse(message));
            WebSocketUtil.close(session);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 断开websocket链接
     */
    public static void close(Session session) {
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
