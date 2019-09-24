package com.baidu.acu.pie.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公共返回类
 */
@Data
public class CommonResponse {
    private boolean success;
    private Message message;
    private String type;

    @JsonSetter
    public void setMessage(Message message) {
        this.message = message;
    }

    @JsonIgnore
    public void setMessage(String message) {
        this.message = new Message(message);
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        @Builder.Default
        private String global = "success";
    }
}
