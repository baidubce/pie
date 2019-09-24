package com.baidu.acu.pie.model.result;

import lombok.Data;

/**
 * 微信权限验证config接口返回
 */
@Data
public class WxConfigResult {
    //随机时间戳
    String timestamp;
    //随机串
    String nonceStr;
    //签名
    String signature;
}
