package com.example.d_bee_7;

import android.app.Application;
import android.content.Context;

import androidx.fragment.app.FragmentTransaction;

import org.litepal.LitePal;

public class MyApplication extends Application {
    public static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePal.initialize(this);
        LitePal.getDatabase();
    }
}
