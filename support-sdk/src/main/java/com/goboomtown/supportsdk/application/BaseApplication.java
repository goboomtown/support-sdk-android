package com.goboomtown.supportsdk.application;


import android.app.Activity;
import android.app.Application;

//import com.pax.market.android.app.sdk.BaseApiService;
//import com.pax.market.android.app.sdk.LocationService;
//import com.pax.market.android.app.sdk.StoreSdk;
//import com.pax.market.android.app.sdk.dto.LocationInfo;
//import com.pax.market.android.app.sdk.dto.TerminalInfo;
//import android.content.Context;
//import android.graphics.BitmapFactory;
//import android.os.Build;
//import android.os.RemoteException;
//import android.util.Log;
//import android.widget.Toast;

//import com.pax.market.android.app.sdk.BaseApiService;
//import com.pax.market.android.app.sdk.LocationService;
//import com.pax.market.android.app.sdk.Notifications;
//import com.pax.market.android.app.sdk.StoreSdk;
//import com.pax.market.android.app.sdk.dto.LocationInfo;
//import com.pax.market.android.app.sdk.dto.TerminalInfo;

/**
 * Created by fojut on 2017/8/24.
 */

public class BaseApplication extends Application {

    private String APP_KEY_GLOBAL = "QWYIOZY6UXK7BVY3E6C6";
    private String APP_SECRET_GLOBAL = "ECC3C7QH8CQKR8Y2F5QVMXY9AR4GTR9IY0Z1C6G1";
    private String APP_KEY_TEST = "R4V7I98B3JOUGFIRUBXR";
    private String APP_SECRET_TEST = "RYW1QIBJB298V7C137FZC3IX7TVWJA79JOYEXAJZ";
    private String SN = "0820611245"; // Build.SERIAL;

    private Activity    mActivity;

    private boolean isReadyToUpdate=true;

    @Override
    public void onCreate() {
        super.onCreate();
    }

}