package com.pie.demo;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    private static Context mContext = null;

    public static Context getmContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
    }
}
