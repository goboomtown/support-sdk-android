package com.goboomtown.supportsdkdemo;

import com.goboomtown.supportsdk.api.POSConnectorBase;
import com.goboomtown.supportsdk.model.BTMerchant;
import com.goboomtown.supportsdk.view.SupportButton;


public class POSConnector extends POSConnectorBase {

    POSConnector(SupportButton supportButton) {
        super(supportButton);
    }

}
