package com.baidu.acu.pie.utils;

import com.baidu.acu.pie.exception.WebProxyException;
import com.baidu.acu.pie.model.result.WxConfigResult;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.UUID;

/**
 * 微信上获取资源的工具
 */
public class WxUtil {

    private static volatile String accessToken = "";
    private static volatile String jsApiTicket = "";
    private static volatile long updateTime = 0;
//    private static final String appId = "wx740e018024415c1e";
//    private static final String secret = "3dd88a8dfd457f367d02285e0c1a2362";

    private static final String appId = "wx740e018024415c1e";
    private static final String secret = "3dd88a8dfd457f367d02285e0c1a2362";


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
     * 通过url，获得wx.config权限签名相关信息
     */
    public static WxConfigResult getWxConfigResult(String url) throws WebProxyException{

        String nonce_str = createNonceStr();
        String timestamp = createTimestamp();
        String signature = "";
        String string = "";

        //注意这里参数名必须全部小写，且必须有序
        string = "jsapi_ticket=" + getJsApiTicket() + "&noncestr=" + nonce_str +
                "×tamp=" + timestamp + "&url=" + url;
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string.getBytes(StandardCharsets.UTF_8)); //对string1 字符串进行SHA-1加密处理
            signature = byteToHex(crypt.digest());  //对加密后字符串转成16进制
        } catch (Exception e) {
            throw new WebProxyException("crypt exception " + e.getMessage());
        }

        WxConfigResult result = new WxConfigResult();
        result.setNonceStr(nonce_str);
        result.setTimestamp(timestamp);
        result.setSignature(signature);
        return result;
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

        if (JsonUtil.keyExist(json, "errcode") && !JsonUtil.parseJson(json, "errcode").equals("0")) {
            String errorCode = JsonUtil.parseJson(json, "errcode");
            String errmsg = JsonUtil.parseJson(json, "errmsg");
            throw new WebProxyException("get access error code:" + errorCode + " errmsg:" + errmsg);
        }
        return JsonUtil.parseJson(json, key);
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String createNonceStr() {
        return UUID.randomUUID().toString();
    }

    private static String createTimestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
}
