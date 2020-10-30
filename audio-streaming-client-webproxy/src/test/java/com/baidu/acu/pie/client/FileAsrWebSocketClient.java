package com.baidu.acu.pie.client;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

/**
 * AsrWebSocketClient
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2020/10/23 2:15 下午
 */
@ClientEndpoint
public class FileAsrWebSocketClient {
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to endpoint: " + session.getBasicRemote());
        FileClientTest.session = session;
    }
    @OnMessage
    public void processMessage(String message) {
        System.out.println("Received message in client: " + message);
    }
    @OnClose
    public void processClose() {
        System.out.println("close");
        System.exit(0);
    }
    @OnError
    public void processError(Throwable t) {
        t.printStackTrace();
    }

}
