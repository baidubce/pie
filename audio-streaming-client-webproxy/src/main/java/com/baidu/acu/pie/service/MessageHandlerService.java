package com.baidu.acu.pie.service;

import com.baidu.acu.pie.constant.Constant;
import com.baidu.acu.pie.constant.RequestType;
import com.baidu.acu.pie.model.response.ServerResponse;
import com.baidu.acu.pie.utils.JsonUtil;
import com.baidu.acu.pie.utils.WsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;

/**
 * MessageHandlerService
 * 接收消息处理类
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageHandlerService {

    private final ConfigHandlerService configHandlerService;
    private final LoginHandlerService loginHandlerService;
    private final AudioHandlerService audioHandlerService;
    private final TicketHandlerService ticketHandlerService;

    public void handler(Session session, String jsonMessage) {

        if (!JsonUtil.keyExist(jsonMessage, Constant.REQUEST_TYPE)) {
            WsUtil.sendMsgToClient(session, ServerResponse.failureStrResponse(
                    "post data does not meet the requirements", RequestType.UNKNOWN));
            return;
        }

        String type = JsonUtil.parseJson(jsonMessage, Constant.REQUEST_TYPE);
        String data = JsonUtil.parseJson(jsonMessage, Constant.REQUEST_DATA);

        RequestType requestType = RequestType.getRequestType(type);

        //按照type选择对应的处理器
        switch (requestType) {
            case CONFIG:
                configHandlerService.handler(session,data); return;
            case LOGIN :
               loginHandlerService.handler(session, data); return;
            case ASR:
                audioHandlerService.handler(session, data); return;
            case TICKET:
                ticketHandlerService.handler(session); return;
            default:
                WsUtil.sendMsgToClient(session, ServerResponse.failureStrResponse(
                        "type:" + type + " can not handler", RequestType.UNKNOWN));
        }

    }
}
