package com.goboomtown.supportsdkdemo;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.goboomtown.supportsdk.api.POSConnectorBase;
import com.goboomtown.supportsdk.model.BTMerchant;
import com.goboomtown.supportsdk.view.SupportButton;
import com.pax.market.android.app.sdk.BaseApiService;
import com.pax.market.android.app.sdk.LocationService;
import com.pax.market.android.app.sdk.StoreSdk;
import com.pax.market.android.app.sdk.dto.LocationInfo;
import com.pax.market.android.app.sdk.dto.TerminalInfo;


public class POSConnector extends POSConnectorBase {

    //todo make sure to replace with your own app's appKey and appSecret
    private String APP_KEY_GLOBAL = "QWYIOZY6UXK7BVY3E6C6";
    private String APP_SECRET_GLOBAL = "ECC3C7QH8CQKR8Y2F5QVMXY9AR4GTR9IY0Z1C6G1";
    private String APP_KEY_TEST = "R4V7I98B3JOUGFIRUBXR";
    private String APP_SECRET_TEST = "RYW1QIBJB298V7C137FZC3IX7TVWJA79JOYEXAJZ";
    private String SN = Build.SERIAL; //"0820611245";

    private BTMerchant  mMerchant;
    private boolean     infoSuccess = false;
    private boolean     locationSuccess = false;

    POSConnector(SupportButton supportButton) {
        super(supportButton);
        initPaxStoreSdk();
    }

    private void initPaxStoreSdk() {
        //todo Init AppKey，AppSecret and SN, make sure the appKey and appSecret is corret.
        StoreSdk.getInstance().init(mContext, APP_KEY_TEST, APP_SECRET_TEST, SN, new BaseApiService.Callback() {
            @Override
            public void initSuccess() {
                mMerchant = new BTMerchant();
                getTerminalInfo();
                getTerminalLocation();
            }

            @Override
            public void initFailed(RemoteException e) {
                Log.i("onError: ",e.toString());
                e.printStackTrace();
                warn("initPaxStoreSdk Error:"+e.toString());
                if (mListener != null) {
                    mListener.posConnectorDidToFailRetrieveAccount(e.getMessage());
                }
            }
        });
    }

    private void checkDone() {
        if ( infoSuccess && locationSuccess ) {
            if (mListener != null) {
                mListener.posConnectorDidRetrieveAccount(mMerchant);
            }
        }
    }

    private void getTerminalInfo() {
        StoreSdk.getInstance().getBaseTerminalInfo(mContext,new BaseApiService.ICallBack() {
            @Override
            public void onSuccess(Object obj) {
                TerminalInfo terminalInfo = (TerminalInfo) obj;
                Log.i("onSuccess: ",terminalInfo.toString());
                warn(terminalInfo.toString());
                mMerchant.serialNumber = terminalInfo.getSerialNo();
                mMerchant.deviceId     = terminalInfo.getTid();
                mMerchant.name         = terminalInfo.getMerchantName();
                infoSuccess = true;
                checkDone();
            }

            @Override
            public void onError(Exception e) {
                Log.i("onError: ",e.toString());
                e.printStackTrace();
                warn("getTerminalInfo Error:"+e.toString());
                if (mListener != null) {
                    mListener.posConnectorDidToFailRetrieveAccount(e.getMessage());
                }
            }
        });
    }


    private void getTerminalLocation() {
        final Context myContext = mContext;
        StoreSdk.getInstance().startLocate(mContext, new LocationService.LocationCallback() {
            @Override
            public void locationResponse(LocationInfo locationInfo) {
                Log.d("MainActivity", "Get Location Result：" + locationInfo.toString());
                warn("Get Location Result：" + locationInfo.toString());
                mMerchant.latitude = Double.valueOf(locationInfo.getLatitude());
                mMerchant.longitude = Double.valueOf(locationInfo.getLongitude());
                locationSuccess = true;
                checkDone();
            }
        });
    }

    private void warn(String message) {
        Log.d("POSConnector", message);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
//                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
