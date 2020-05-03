package com.goboomtown.supportsdk.application;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

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
    private Context     mContext;

    private boolean isReadyToUpdate=true;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
//        mActivity = getActivity(mContext);
//        initPaxStoreSdk();
    }


//    public Activity getActivity(Context context) {
//        if ( context == null ) {
//            return null;
//        }
//        else if ( context instanceof ContextWrapper) {
//            if (context instanceof Activity) {
//                return (Activity) context;
//            } else {
//                return getActivity(((ContextWrapper) context).getBaseContext());
//            }
//        }
//        return null;
//    }
//
//
//    private void initPaxStoreSdk() {
//        //todo Init AppKey，AppSecret and SN, make sure the appKey and appSecret is corret.
//        StoreSdk.getInstance().init(mContext, APP_KEY_TEST, APP_SECRET_TEST, SN, new BaseApiService.Callback() {
//            @Override
//            public void initSuccess() {
//                //TODO Do your business here
//                getTerminalInfo();
//                getTerminalLocation();
//            }
//
//            @Override
//            public void initFailed(RemoteException e) {
//                //TODO Do failed logic here
//                warn("Cannot get API URL from PAXSTORE, Please install PAXSTORE first.");
//            }
//        });
//    }
//
//    private void getTerminalInfo() {
//        StoreSdk.getInstance().getBaseTerminalInfo(mContext,new BaseApiService.ICallBack() {
//            @Override
//            public void onSuccess(Object obj) {
//                TerminalInfo terminalInfo = (TerminalInfo) obj;
//                Log.i("onSuccess: ",terminalInfo.toString());
//                warn(terminalInfo.toString());
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.i("onError: ",e.toString());
//                warn("getTerminalInfo Error:"+e.toString());
//
//            }
//        });
//    }
//
//
//    private void getTerminalLocation() {
//        final Context myContext = mContext;
//        StoreSdk.getInstance().startLocate(mContext, new LocationService.LocationCallback() {
//            @Override
//            public void locationResponse(LocationInfo locationInfo) {
//                Log.d("MainActivity", "Get Location Result：" + locationInfo.toString());
//                warn("Get Location Result：" + locationInfo.toString());
//            }
//        });
//    }
//
//    private void warn(String message) {
//        Log.d("POSConnector", message);
//        if ( mActivity != null ) {
//            mActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }

}