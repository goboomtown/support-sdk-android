package com.goboomtown.supportsdk.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.model.BTConnectIssue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * TODO: document your custom view class.
 */
public class CallMeView extends LinearLayout {

    public SupportButton    supportButton;
    public SupportSDK       supportSDK;
    public Activity         mActivity;

    private Context     mContext;
    public  PopupWindow mPopupWindow;

    private EditText    mCallbackNumberEditText;
    private EditText    mCallbackDescriptionEditText;
    private Button      mOkButton;

    public CallMeView(Context context, String callbackNumber) {
        super(context);
        mContext = context;

        View view = inflate(mContext, R.layout.call_me_view, this);

        mCallbackNumberEditText = view.findViewById(R.id.callbackNumber);
        mCallbackDescriptionEditText = view.findViewById(R.id.callbackDescription);

        if ( callbackNumber!=null && !callbackNumber.equalsIgnoreCase("null") ) {
            mCallbackNumberEditText.setText(callbackNumber);
        }
        mCallbackNumberEditText.setEnabled(true);
        mCallbackDescriptionEditText.setEnabled(true);
        mCallbackDescriptionEditText.setSingleLine(false);

        mCallbackNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                enableOkButton(s.toString());
            }
        });

        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            if ( mPopupWindow != null ) {
                mPopupWindow.dismiss();
            }
        });
        mOkButton = view.findViewById(R.id.okButton);
        enableOkButton(callbackNumber);
        mOkButton.setOnClickListener(v -> {
            if ( mPopupWindow != null ) {
                mPopupWindow.dismiss();
                createIssue(supportSDK.memberID, supportSDK.memberUserID, supportSDK.memberLocationID,
                        mCallbackNumberEditText.getText().toString(), mCallbackDescriptionEditText.getText().toString());
            }
        });
    }

    private void enableOkButton(String phoneNumber) {
        if ( mOkButton != null ) {
            boolean validated = validatePhoneNumber(phoneNumber);
            mOkButton.setEnabled(validated);
            mOkButton.setClickable(validated);
        }
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        String pattern = "^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$";
        Matcher m;
        Pattern r = Pattern.compile(pattern);
        if ( phoneNumber!=null && !phoneNumber.isEmpty() ) {
            m = r.matcher(phoneNumber.trim());
            return m.find();
        }
        return false;
    }


    private void createIssue(String members_id, String members_users_id, String members_locations_id, String callbackNumber, String description) {
        JSONObject params = new JSONObject();
        JSONObject issuesJSON = new JSONObject();
        try {
            issuesJSON.put("members_id", members_id);
            issuesJSON.put("members_users_id", members_users_id);
            issuesJSON.put("members_locations_id", members_locations_id);
            issuesJSON.put("user_agent", supportSDK.clientAppIdentifier()); // redundant - already in headers
            params.put("issues", issuesJSON);
            if ( callbackNumber != null ) {
                params.put("callbackNumber", callbackNumber);
            }
            if ( description != null ) {
                params.put("description", description);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = String.format("%s/issues/create", SupportSDK.kSDKV1Endpoint);

        supportSDK.post(uri, params, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (supportButton.mListener != null) {
                    supportButton.mListener.supportButtonDidFailWithError("Unable to create issue", e.getLocalizedMessage());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean success = false;
                BTConnectIssue issue = null;
                String message = "";

                JSONObject jsonObject;
                try {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String responseBodyString = responseBody.string();
                        jsonObject = new JSONObject(responseBodyString);
                        success = jsonObject.optBoolean("success");
                        if (success) {
                            JSONArray results = jsonObject.optJSONArray("results");
                            if (results != null && results.length() > 0) {
                                JSONObject issueJSON;
                                try {
                                    issueJSON = results.getJSONObject(0);
                                    issue = new BTConnectIssue(issueJSON);
                                    success = true;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (issue != null) {
                                    BTConnectIssue.saveCurrentIssue(mContext, issue);
                                }
                                showConfirmation(supportSDK.callMeButtonConfirmation);
                            }
                        } else {
                            message = jsonObject.optString("message");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!success) {
                    if (supportButton.mListener != null) {
                        supportButton.mListener.supportButtonDidFailWithError(mContext.getString(R.string.error_unable_to_create_issue), message);
                    }
                }
            }
        });
    }


    private void showConfirmation(String message) {
         mActivity.runOnUiThread(() -> {
             AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, AlertDialog.THEME_HOLO_LIGHT);
             builder.setTitle(null);
             builder.setMessage(message);
             builder.setCancelable(false);

             builder.setPositiveButton("OK", null);

             final AlertDialog alert = builder.create();
             if (alert != null) {
                 alert.show();
             }
             // Hide after some seconds
             final Handler handler  = new Handler();
             final Runnable runnable = new Runnable() {
                 @Override
                 public void run() {
                     if (alert.isShowing()) {
                         alert.dismiss();
                     }
                 }
             };

             alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                 @Override
                 public void onDismiss(DialogInterface dialog) {
                     handler.removeCallbacks(runnable);
                 }
             });

             handler.postDelayed(runnable, 3000);
         });
    }

}
