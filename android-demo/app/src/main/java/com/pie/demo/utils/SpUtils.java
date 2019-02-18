package com.pie.demo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.pie.demo.App;

public class SpUtils {

    private final SharedPreferences sp;

    private static SpUtils instance;

    public static SpUtils getInstance() {
        if (null == instance) {
            synchronized (SpUtils.class) {
                if (null == instance) {
                    instance = new SpUtils();
                }
            }
        }
        return instance;
    }

    private SpUtils() {
        sp = App.getmContext().getSharedPreferences("config", Context.MODE_PRIVATE);
    }

    public void putString(String key, String value) {
        sp.edit().putString(key, value).commit();
    }

    public String getString(String key) {
        return sp.getString(key, null);
    }

    public void putInt(String key, int value) {
        sp.edit().putInt(key, value).commit();
    }

    public int getInt(String key) {
        return sp.getInt(key, -1);
    }
}
