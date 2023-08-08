package com.baidu.acu.pie.util;

/**
 * 类<code>ArrayUtil</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 **/
public class ArrayUtil {
    //System.arraycopy()方法
    public static byte[] byteMerger(byte[] bt1, byte[] bt2) {
        if (bt1 == null) {
            bt1 = new byte[0];
        }
        if (bt2 == null) {
            bt2 = new byte[0];
        }
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }
}
