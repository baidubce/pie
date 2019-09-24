package com.baidu.acu.pie.service;

import com.baidu.acu.pie.constant.RequestType;
import com.baidu.acu.pie.model.info.TicketData;
import com.baidu.acu.pie.model.response.ServerResponse;
import com.baidu.acu.pie.utils.WebSocketUtil;
import com.baidu.acu.pie.utils.WxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.Session;


@Slf4j
@Component
public class TicketHandlerService {
    /**
     * 处理获取ticket请求
     * TODO 接口直接暴露有风险，看后期情况是否需要
     */
    public void handle(Session session) {
        try {
            String accessToken = WxUtil.getAccessToken();
            String ticket = WxUtil.getJsApiTicket();
            TicketData ticketData = new TicketData();
            ticketData.setAccessToken(accessToken);
            ticketData.setTicket(ticket);
            WebSocketUtil.sendMsgToClient(session, ServerResponse.successStrResponse(ticketData, RequestType.TICKET));
        } catch (Exception e) {
            log.error(e.getMessage());
            WebSocketUtil.sendMsgToClient(session, "get ticket exception:" + e.getMessage());
        }
    }
}
