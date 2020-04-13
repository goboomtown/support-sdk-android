package com.goboomtown.supportsdk.api;

import android.content.Context;


public class POSConnector extends POSConnectorBase {

    POSConnector(Context context, POSConnectorListener listener) {
        mContext = context;
        mListener = listener;
    }
}
