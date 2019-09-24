package com.baidu.acu.pie.service;

import com.baidu.acu.pie.constant.RequestType;
import com.baidu.acu.pie.exception.WebProxyException;
import com.baidu.acu.pie.model.response.ServerResponse;
import com.baidu.acu.pie.model.result.WxConfigResult;
import com.baidu.acu.pie.utils.WsUtil;
import com.baidu.acu.pie.utils.WxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.Session;

/**
 * ConfigHandler
 * url权限处理类,供客户端调用
 */
@Slf4j
@Component
public class ConfigHandlerService {

    public void handler(Session session, String url) {

        WxConfigResult result;
        try {
            result = WxUtil.getWxConfigResult(url);
        } catch (WebProxyException e) {
            log.info(e.getMessage());
            WsUtil.sendMsgToClient(session, ServerResponse.failureStrResponse(e.getMessage(), RequestType.CONFIG));
            return;
        }
        WsUtil.sendMsgToClient(session, ServerResponse.successStrResponse(result, RequestType.CONFIG));
    }
}
