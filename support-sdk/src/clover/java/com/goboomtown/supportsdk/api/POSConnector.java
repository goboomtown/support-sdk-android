package com.goboomtown.supportsdk.api;

import android.accounts.Account;
import android.content.Context;
import android.os.IInterface;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v1.merchant.MerchantConnector;


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
        merchantConnector.getMerchant(new MerchantConnector.MerchantCallback<Merchant>() {
            @Override
            public void onServiceSuccess(Merchant result, ResultStatus status) {
                super.onServiceSuccess(result, status);

                if ( mListener != null ) {
                    mListener.posConnectorDidRetrieveAccount(result);
                }

//                updateMerchant("get merchant success", status, result);
//
//                address1Edit.setText(result.getAddress().getAddress1());
//                address2Edit.setText(result.getAddress().getAddress2());
//                address3Edit.setText(result.getAddress().getAddress3());
//                cityEdit.setText(result.getAddress().getCity());
//                stateEdit.setText(result.getAddress().getState());
//                zipEdit.setText(result.getAddress().getZip());
//                countryEdit.setText(result.getAddress().getCountry());
//
//                phoneEdit.setText(result.getPhoneNumber());
            }

            @Override
            public void onServiceFailure(ResultStatus status) {
                super.onServiceFailure(status);
//                updateMerchant("get merchant failure", status, null);
                if ( mListener != null ) {
                    mListener.posConnectorDidToFailRetrieveAccount(status.getStatusMessage());
                }
            }

            @Override
            public void onServiceConnectionFailure() {
                super.onServiceConnectionFailure();
//                updateMerchant("get merchant bind failure", null, null);
                if ( mListener != null ) {
                    mListener.posConnectorDidToFailRetrieveAccount("");
                }
            }
        });
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
