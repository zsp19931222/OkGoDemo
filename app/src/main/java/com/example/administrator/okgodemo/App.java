package com.example.administrator.okgodemo;

import android.app.Application;

import com.lzy.okgo.OkGo;

/**
 * Created by Administrator on 2017/11/24 0024.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OkGo.getInstance().init(this);
    }
}
