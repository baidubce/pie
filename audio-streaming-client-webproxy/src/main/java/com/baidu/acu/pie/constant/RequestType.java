package com.baidu.acu.pie.constant;


/**
 * 后端请求类型
 */
public enum RequestType {
    TICKET("ticket"),
    CONFIG("config"),
    LOGIN("login"),
    ASR("asr"),
    UNKNOWN("unknown");


    RequestType(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }

    /**
     * 通过type查找枚举对象
     */
    public static RequestType getRequestType(String type) {
        for (RequestType object : RequestType.values()) {
            if (object.getType().equals(type)) {
                return object;
            }
        }
        return UNKNOWN;
    }
}
