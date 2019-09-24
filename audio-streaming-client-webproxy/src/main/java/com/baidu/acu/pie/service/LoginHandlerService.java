package com.baidu.acu.pie.service;

import com.baidu.acu.pie.constant.RequestType;
import com.baidu.acu.pie.model.info.LoginData;
import com.baidu.acu.pie.model.response.ServerResponse;
import com.baidu.acu.pie.utils.JsonUtil;
import com.baidu.acu.pie.utils.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.hash.Hashing.sha256;

/**
 * LoginHandler
 * 登录处理
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoginHandlerService {

    private Map<String, String> loginUsers = new ConcurrentHashMap<>();

    public void handle(Session session, String data) {

        LoginData loginData = null;
        try {
            loginData = JsonUtil.objectMapper.readValue(data, LoginData.class);
        } catch (IOException e) {
            WebSocketUtil.sendMsgToClient(session, ServerResponse.failureStrResponse(e.getMessage(), RequestType.LOGIN));
            return;
        }
        //TODO 现在只初始用户名与密码为 test test，后期需要与streaming server一致，或者单独密码
        String rawToken = loginData.getUserName() + "test" + loginData.getDateTime();
        String token =  sha256().hashString(rawToken, StandardCharsets.UTF_8).toString();
        if (token.equals(loginData.getToken())) {
            userLogin(session);
            WebSocketUtil.sendMsgToClient(session, ServerResponse.successStrResponse(RequestType.LOGIN));
            return;
        }
        WebSocketUtil.sendMsgToClient(session,
                ServerResponse.failureStrResponse("Error in username or password", RequestType.LOGIN));
    }

    /**
     * 用户登录
     */
    private void userLogin(Session session) {
        loginUsers.put(session.getId(), "success");
    }


    /**
     * 取消登录
     */
    public void cancelLogin(Session session) {
        loginUsers.remove(session.getId());
    }


    /**
     * 检测用户是否登录
     */
    public boolean userExists(Session session) {
        return loginUsers.containsKey(session.getId());
    }

}
