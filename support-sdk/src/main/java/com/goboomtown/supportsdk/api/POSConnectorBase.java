package com.goboomtown.supportsdk.api;

import android.content.Context;

import com.goboomtown.supportsdk.model.BTMerchant;
import com.goboomtown.supportsdk.view.SupportButton;


public class POSConnectorBase {

    public interface POSConnectorListener {
        void posConnectorDidRetrieveAccount(BTMerchant merchant);
        void posConnectorDidToFailRetrieveAccount(String message);
    }

//    public  Activity                mActivity;
    public  Context                 mContext;
    public  POSConnectorListener    mListener;

    public POSConnectorBase() {

    }

    public POSConnectorBase(Context context, POSConnectorListener listener) {
        mContext    = context;
        mListener   = listener;
    }


    public POSConnectorBase(SupportButton supportButton) {
        mContext    = supportButton.mContext.get();
        mListener   = supportButton;
    }

    public void getAccount() {

    }
}
