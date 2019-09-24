package com.baidu.acu.pie.model.info;

import lombok.Data;

/**
 * 微信accessToken以及ticket
 */
@Data
public class TicketData {
    private String accessToken;
    private String ticket;
}
