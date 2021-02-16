package com.goboomtown.supportsdk.fragment;

import com.goboomtown.activity.KBActivity;
import com.goboomtown.forms.fragment.FormFragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goboomtown.forms.activity.BaseActivity;
import com.goboomtown.forms.model.BoomtownField;
import com.goboomtown.forms.model.FormModel;
import com.goboomtown.forms.view.DateCompositeView;
import com.goboomtown.forms.view.SeekbarCompositeView;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.view.SupportButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SupportFormFragment extends com.goboomtown.forms.fragment.FormFragment
    implements SupportSDK.SupportSDKFormsListener {

    public  Context         mContext = null;
    public  SupportButton   mSupportButton = null;
    public  SupportSDK      supportSDK = null;
    public  boolean         isFromList = false;
    private SupportSDK.SupportSDKFormsListener  formsListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        formsListener = supportSDK.mFormsListener;
        supportSDK.mFormsListener = this;
        if ( supportSDK != null ) {
            textColor = supportSDK.appearance.homeTextColor();
            backgroundColor = supportSDK.appearance.loginBackgroundColor;

            formEntryBorderColor            = supportSDK.appearance.formEntryBorderColor();
            formEntryBorderWidth            = supportSDK.appearance.formEntryBorderWidth();
            formEntryTextColor              = supportSDK.appearance.formEntryTextColor();
            formEntryTextSize               = supportSDK.appearance.formEntryTextSize();
            formEntryTextStyle              = supportSDK.appearance.formEntryTextStyle();
            formLabelRequiredIndicatorColor = supportSDK.appearance.formLabelRequiredIndicatorColor();
            formLabelRequiredTextColor      = supportSDK.appearance.formLabelRequiredTextColor();
            formLabelTextColor              = supportSDK.appearance.formLabelTextColor();
            formLabelTextSize               = supportSDK.appearance.formLabelTextSize();
            formLabelTextStyle              = supportSDK.appearance.formLabelTextStyle();

            refresh();
        }

//        setupRecyclerView(mRecyclerView);
//        if (context instanceof OnListFragmentInteractionListener) {
//            mListener = (OnListFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnListFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        supportSDK.mFormsListener = formsListener;
        mListener = null;
    }


//    public void handleFocusChangeOnField(com.goboomtown.forms.fragment.FormFragment.SimpleItemRecyclerViewAdapter.ViewHolder holder, boolean hasFocus, View view) {
//        if (!hasFocus && holder.mField.isValueUpdated()) {
//            if (mParent != null) {
//                mParent.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if ( mFormModel.refresh() ) {
//                            refreshViewAdapter();
//                        }
//                    }
//                });
//            }
//        }
//    }

//    public void handleUpdatedField(BoomtownField field) {
//        if ( !field.isValueUpdated() ) {
//            return;
//        }
//        Activity activity = getActivity();
//        if ( activity != null ) {
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if ( mFormModel.refresh() ) {
//                        refreshViewAdapter();
//                    }
//                }
//            });
//        }
//    }


    @Override
    public boolean save() {
        super.save();

        for (BoomtownField field : mFormModel.fields) {
            switch (field.type) {
                case image:
                case file:
                    break;
                case signature:
                    break;
                default:
                    break;
            }
        }

        if ( mFormModel.isComplete() ) {
            supportSDK.putForm(this, mFormModel.checklist_id, mFormModel.toJSON());
        } else {
            Activity activity = getActivity();
            if ( activity != null ) {
                String message = getResources().getString(R.string.warn_required_fields_empty);
                for ( String fieldName : mFormModel.missingRequiredFields ) {
                    message += "\n" + fieldName;
                }
                final String theMessage = message;
                activity.runOnUiThread(() -> Toast.makeText(getContext(), theMessage, Toast.LENGTH_LONG).show());
            }
        }

        return true;
    }


    private void refresh() {
//        mFormModel = supportSDK.currentForm();
        if ( mFormModel.fields.size() == 0 ) {
            supportSDK.getForms(this);
        }
        mFormModel.sortFields();
        mFormModel.refresh();
        refreshForm();
    }


    public void warn(final String title, final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), title + ": " + message, Toast.LENGTH_LONG).show();
            }
        });
    }


    public void retrieveKB(String id) {
        String url = SupportSDK.kSDKV1Endpoint + "/kb/get?id=" + id;

        supportSDK.get(url, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                boolean success = false;

                JSONObject jsonObject = SupportSDK.successJSONObject(response);
                if ( jsonObject != null ) {
                    if ( jsonObject.has("results") ) {
                        try {
                            JSONArray resultsJSON = jsonObject.getJSONArray("results");
                            JSONObject result = resultsJSON.getJSONObject(0);
                            success = true;
                            String title = result.getString("title");
                            String url = result.getString("url");
                            Intent intent = new Intent(mContext, KBActivity.class);
                            intent.putExtra(KBActivity.ARG_URL, url);
                            intent.putExtra(KBActivity.ARG_HTML, "");
                            intent.putExtra(KBActivity.ARG_TITLE, title);
                            startActivity(intent);
                        } catch (JSONException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                }
                if ( !success ) {
                    warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
                }
            }
        });
    }


    @Override
    public void supportSDKDidRetrieveForms() {
//        refresh();
    }

    @Override
    public void supportSDKDidRetrieveForm(FormModel form) {

    }

    @Override
    public void supportSDKDidUpdateForm() {
        FragmentActivity activity = getActivity();
        if ( activity != null ) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), getResources().getString(R.string.warn_submission_successful), Toast.LENGTH_LONG).show();
                    if ( isFromList ) {
                        FragmentManager fragmentManager = activity.getSupportFragmentManager();
                        fragmentManager.popBackStackImmediate();
                    } else {
                        activity.runOnUiThread(() -> mSupportButton.removeForm());
                    }
                }
            });
        }
    }

    @Override
    public void supportSDKFailedToUpdateForm() {
        Activity activity = getActivity();
        if ( activity != null ) {
            final String message = getResources().getString(R.string.warn_unable_to_update_form);
            activity.runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show());
        }
    }


}
