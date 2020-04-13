package com.goboomtown.supportsdk.api;

import android.content.Context;


public class POSConnectorBase {

    public interface POSConnectorListener {
        void posConnectorDidRetrieveAccount(Object merchant);
        void posConnectorDidToFailRetrieveAccount(String message);
    }

    public  Context                 mContext;
    public  POSConnectorListener    mListener;

    POSConnectorBase() {

    }

    POSConnectorBase(Context context, POSConnectorListener listener) {
        mContext = context;
        mListener = listener;
    }


    public void getAccount() {

    }
}
