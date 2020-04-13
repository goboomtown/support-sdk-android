package com.goboomtown.supportsdk.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.Toast;
import com.goboomtown.video.BTSupportView;

public class SupportOverlayService extends Service {

    private BTSupportView mSupportView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        addSupportView();
    }


    public void addSupportView() {
        mSupportView = BTSupportView.sharedInstance(getBaseContext());
        mSupportView.setAlpha(1);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        try {
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            windowManager.addView(mSupportView, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getBaseContext(),"onDestroy", Toast.LENGTH_LONG).show();
        if ( mSupportView != null ) {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mSupportView);
            mSupportView = null;
        }
    }

}
