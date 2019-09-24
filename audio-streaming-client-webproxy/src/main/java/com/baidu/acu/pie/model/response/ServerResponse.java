package com.baidu.acu.pie.model.response;

import com.baidu.acu.pie.constant.RequestType;
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
    private static <T> ServerResponse<T> response(boolean success,  String message, T result, RequestType type) {
        ServerResponse<T> response = new ServerResponse<>();
        response.setResult(result);
        response.setMessage(message);
        response.setSuccess(success);
        response.setType(type.getType());
        return response;
    }

    /**
     * 返回成功结果
     */
    public static <T> ServerResponse<T> successResponse(T result, RequestType type) {
        return response(true, "success", result, type);
    }

    /**
     * 返回成功结果
     */
    public static <T> ServerResponse<T> successResponse(RequestType type) {
        return response(true, "success", null, type);
    }

    /**
     * 返回成功字符串结果
     */
    public static<T> String successStrResponse(T result, RequestType type) {
        return JsonUtil.transObjectToStr(successResponse(result, type));
    }

    /**
     * 返回成功字符串结果
     */
    public static<T> String successStrResponse(RequestType type) {
        return JsonUtil.transObjectToStr(successResponse(null, type));
    }

    /**
     * 返回失败结果
     */
    public static ServerResponse failureResponse(String error, RequestType type) {
        return response(false, error, null, type);
    }

    /**
     * 返回失败字符串结果
     */
    public static String failureStrResponse(String error, RequestType type) {
        return JsonUtil.transObjectToStr(failureResponse(error, type));
    }
}
