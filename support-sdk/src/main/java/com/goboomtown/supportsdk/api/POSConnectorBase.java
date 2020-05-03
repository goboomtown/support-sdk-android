package com.goboomtown.supportsdk.api;

import android.content.Context;

import com.goboomtown.supportsdk.model.BTMerchant;


public class POSConnectorBase {

    public interface POSConnectorListener {
        void posConnectorDidRetrieveAccount(BTMerchant merchant);
        void posConnectorDidToFailRetrieveAccount(String message);
    }

//    public  Activity                mActivity;
    public  Context                 mContext;
    public  POSConnectorListener    mListener;

    POSConnectorBase() {

    }

    POSConnectorBase(Context context, POSConnectorListener listener) {
        mContext    = context;
        mListener   = listener;
    }


    public void getAccount() {

    }
}
