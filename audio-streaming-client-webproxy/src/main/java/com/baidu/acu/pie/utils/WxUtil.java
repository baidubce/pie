package com.baidu.acu.pie.utils;

import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;

/**
 * 微信上获取资源的工具
 */
public class WxUtil {

    private static volatile String accessToken = "";
    private static volatile String jsApiTicket = "";
    private static volatile long updateTime = 0;
    private static final String appId = "";
    private static final String secret = "";


    /**
     * 通过mediaId获得inputStream，供后期使用
     */
    public static InputStream getAudioStream(String mediaId) {
        String url = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token="
                + getAccessToken() + "&media_id=" + mediaId;
        return HttpUtil.syncHttpGetInputStream(url);
    }


    /**
     * 获得accessToken
     */
    public static String getAccessToken() {
        if (System.currentTimeMillis() <= updateTime + 6000000) {
            return accessToken;
        }
        syncUpdateJsTicket();
        return accessToken;
    }

    /**
     * 获得jsApiTicket
     *  @throws: RuntimeException
     */
    public static String getJsApiTicket() {
        if (System.currentTimeMillis() <= updateTime + 6000000) {
            return jsApiTicket;
        }
        syncUpdateJsTicket();
        return jsApiTicket;

    }

    /**
     * 同步更新ticket
     * @throws: RuntimeException
     */
    private static synchronized void syncUpdateJsTicket() {
        // 再加时间判断，防止高并发下多次请求
        if (System.currentTimeMillis() >= updateTime + 6000000) {
            updateJsTicket();
        }
    }


    /**
     * 在线更新accessToken
     * @throws: RuntimeException
     */
    private static void updateAccessToken() {
        String requestUrl = "https://api.weixin.qq.com/cgi-bin/token?";
        String params = "grant_type=client_credential&appid=" + appId + "&secret=" + secret + "";
        String result = HttpUtil.syncHttpGetStringResult(requestUrl+params);
        accessToken =  getResult(result, "access_token");
    }

    /**
     * 在线更新jsApiTicket（同时更新accessToken）
     * @throws: RuntimeException
     */
    private static void updateJsTicket() {
        updateAccessToken();
        String requestUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?";
        String params = "access_token=" + accessToken + "&type=jsapi";
        String result = HttpUtil.syncHttpGetStringResult(requestUrl+params);
        jsApiTicket =  getResult(result, "ticket");
        updateTime = System.currentTimeMillis();
    }

    /**
     * 从返回的数据中获取有效信息
     * @throws: RuntimeException
     */
    private static String getResult(String json, String key) {
        JSONObject jsonResult = JSONObject.parseObject(json);
        if (jsonResult.containsKey("errcode") && !jsonResult.getString("errcode").equals("0")) {
            String errorCode = jsonResult.getString("errcode");
            String errmsg = jsonResult.getString("errmsg");
            throw new RuntimeException("get access error code:" + errorCode + " errmsg:" + errmsg);
        }
        return JSONObject.parseObject(json).getString(key);
    }
}
