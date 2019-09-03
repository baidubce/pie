package com.baidu.acu.pie.model.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;

/**
 * 服务端返回结果
 */
@Data
public class ServerResponse<T> extends CommonResponse {

    @JsonUnwrapped
    private T result;

    /**
     * 返回通用结果
     */
    private static <T> ServerResponse<T> response(int code,  String message, T result) {
        ServerResponse<T> response = new ServerResponse<>();
        response.setCode(code);
        response.setResult(result);
        response.setMessage(message);
        return response;
    }

    /**
     * 返回成功结果
     */
    public static <T> ServerResponse<T> successResponse(T result) {
        return response(0, "", result);
    }
}
