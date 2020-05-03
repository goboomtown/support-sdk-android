package com.goboomtown.supportsdk.api;

import android.accounts.Account;
import android.content.Context;
import android.os.IInterface;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v1.merchant.MerchantAddress;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.goboomtown.supportsdk.model.BTMerchant;


public class POSConnector extends POSConnectorBase
        implements MerchantConnector.OnMerchantChangedListener, ServiceConnector.OnServiceConnectedListener {

    private Account account;
    private MerchantConnector   merchantConnector;

    POSConnector(Context context, POSConnectorListener listener) {
        mContext = context;
        mListener = listener;
    }


    @Override
    public void getAccount() {
        account = CloverAccount.getAccount(mContext);
        connect(mContext);
        getMerchant();
    }

    private void connect(Context context) {
//        disconnect();
        if (account != null) {
            merchantConnector = new MerchantConnector(context, account, null);
//            merchantConnector.setOnMerchantChangedListener(this);
//            merchantConnector.connect();
        }
    }

    private void disconnect() {
        if (merchantConnector != null) {
            merchantConnector.disconnect();
            merchantConnector = null;
        }
    }


    private void getMerchant() {
        try {
            merchantConnector.getMerchant(new MerchantConnector.MerchantCallback<Merchant>() {
                @Override
                public void onServiceSuccess(Merchant merchant, ResultStatus status) {
                    super.onServiceSuccess(merchant, status);

                    BTMerchant btMerchant = new BTMerchant();
                    btMerchant.mid          = merchant.getMid();
                    btMerchant.deviceId     = merchant.getDeviceId();
                    btMerchant.name         = merchant.getName();
                    MerchantAddress address = merchant.getAddress();
                    btMerchant.address1     = address.getAddress1();
                    btMerchant.city         = address.getCity();
                    btMerchant.state        = address.getState();
                    btMerchant.zip          = address.getZip();
                    btMerchant.country      = address.getCountry();
                    btMerchant.latitude     = address.getLatitude();
                    btMerchant.longitude    = address.getLongitude();
                    btMerchant.supportEmail = merchant.getSupportEmail();
                    Account account         = merchant.getAccount();
                    if (mListener != null) {
                        mListener.posConnectorDidRetrieveAccount(btMerchant);
                    }
                }

                @Override
                public void onServiceFailure(ResultStatus status) {
                    super.onServiceFailure(status);
                    if (mListener != null) {
                        mListener.posConnectorDidToFailRetrieveAccount(status.getStatusMessage());
                    }
                }

                @Override
                public void onServiceConnectionFailure() {
                    super.onServiceConnectionFailure();
                    if (mListener != null) {
                        mListener.posConnectorDidToFailRetrieveAccount("");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onMerchantChanged(Merchant merchant) {

    }

    @Override
    public void onServiceConnected(ServiceConnector<? extends IInterface> connector) {

    }

    @Override
    public void onServiceDisconnected(ServiceConnector<? extends IInterface> connector) {

    }


}
