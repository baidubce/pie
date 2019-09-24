package com.baidu.acu.pie.service;

import com.baidu.acu.pie.constant.RequestType;
import com.baidu.acu.pie.model.info.TicketData;
import com.baidu.acu.pie.model.response.ServerResponse;
import com.baidu.acu.pie.utils.WsUtil;
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
    public void handler(Session session) {
        try {
            String accessToken = WxUtil.getAccessToken();
            String ticket = WxUtil.getJsApiTicket();
            TicketData ticketData = new TicketData();
            ticketData.setAccessToken(accessToken);
            ticketData.setTicket(ticket);
            WsUtil.sendMsgToClient(session, ServerResponse.successStrResponse(ticketData, RequestType.TICKET));
        } catch (Exception e) {
            log.error(e.getMessage());
            WsUtil.sendMsgToClient(session, "get ticket exception:" + e.getMessage());
        }
    }
}
