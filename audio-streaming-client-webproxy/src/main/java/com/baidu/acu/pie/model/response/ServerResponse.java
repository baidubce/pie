package com.baidu.acu.pie.model.response;

import com.baidu.acu.pie.utils.JsonUtil;
import lombok.Data;

/**
 * 服务端返回结果
 */
@Data
public class ServerResponse<T> extends CommonResponse {

    private T result;

    /**
     * 返回通用结果
     */
    private static <T> ServerResponse<T> response(boolean success,  String message, T result) {
        ServerResponse<T> response = new ServerResponse<>();
        response.setResult(result);
        response.setMessage(message);
        response.setSuccess(success);
        return response;
    }

    /**
     * 返回成功结果
     */
    public static <T> ServerResponse<T> successResponse(T result) {
        return response(true, "success", result);
    }

    /**
     * 返回成功结果
     */
    public static <T> ServerResponse<T> successResponse() {
        return response(true, "success", null);
    }

    /**
     * 返回成功字符串结果
     */
    public static<T> String successStrResponse(T result) {
        return JsonUtil.transObjectToStr(successResponse(result));
    }

    /**
     * 返回成功字符串结果
     */
    public static<T> String successStrResponse() {
        return JsonUtil.transObjectToStr(successResponse(null));
    }

    /**
     * 返回失败结果
     */
    public static ServerResponse failureResponse(String error) {
        return response(false, error, null);
    }

    /**
     * 返回失败字符串结果
     */
    public static String failureStrResponse(String error) {
        return JsonUtil.transObjectToStr(failureResponse(error));
    }
}
