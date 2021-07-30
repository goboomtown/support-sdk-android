package com.goboomtown.supportsdk.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.model.Issue;

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
public class CallMeView {

    public SupportButton    supportButton;
    public SupportSDK       supportSDK;
    public Activity         mActivity;

    private Context     mContext;
    public  PopupWindow mPopupWindow;

    private String      mCallbackNumber;

    private EditText    mCallbackNumberEditText;
    private EditText    mCallbackDescriptionEditText;
    private Button      mOkButton;
    private androidx.appcompat.app.AlertDialog dialog;

    public CallMeView(Context context, String callbackNumber) {
        mContext = context;
        mCallbackNumber = callbackNumber;
    }

    private void enableOkButton(String phoneNumber) {
        if ( dialog == null ) {
            return;
        }
        Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if ( button != null ) {
            boolean validated = validatePhoneNumber(phoneNumber);
            button.setEnabled(validated);
            button.setFocusable(validated);
            button.setClickable(validated);
        }
    }

    public void show() {
        androidx.appcompat.app.AlertDialog.Builder dialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.call_me_dialog, null);
        dialogBuilder.setView(dialogView);

        TextView callbackNumberLabel = dialogView.findViewById(R.id.callbackNumberLabel);
        if ( callbackNumberLabel != null ) {
           callbackNumberLabel.setTextColor(supportSDK.appearance.callMeLabelTextColor());
        }

        TextView callbackDescriptionLabel = dialogView.findViewById(R.id.callbackDescriptionLabel);
        if ( callbackDescriptionLabel != null ) {
            callbackDescriptionLabel.setTextColor(supportSDK.appearance.callMeLabelTextColor());
        }

        mCallbackNumberEditText = dialogView.findViewById(R.id.callbackNumber);
        if ( mCallbackNumberEditText != null ) {
            mCallbackNumberEditText.setTextColor(supportSDK.appearance.callMeLabelTextColor());
            mCallbackNumberEditText.setEnabled(true);
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
            if ( mCallbackNumber!=null && !mCallbackNumber.equalsIgnoreCase("null") ) {
                mCallbackNumberEditText.setText(mCallbackNumber);
                enableOkButton(mCallbackNumber);
            }
        }

        mCallbackDescriptionEditText = dialogView.findViewById(R.id.callbackDescription);
        if ( mCallbackDescriptionEditText != null ) {
            mCallbackDescriptionEditText.setTextColor(supportSDK.appearance.callMeLabelTextColor());
            mCallbackDescriptionEditText.setEnabled(true);
            mCallbackDescriptionEditText.setSingleLine(false);
        }

        dialogBuilder.setTitle(mContext.getResources().getString(R.string.label_call_me));
//        dialogBuilder.setMessage("please send me to your feedback.");
        dialogBuilder.setPositiveButton(mContext.getResources().getString(R.string.text_submit), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        dialogBuilder.setNegativeButton(mContext.getResources().getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if ( supportButton.mListener != null ) {
                    supportButton.mListener.supportButtonDidCompleteTask();
                }
                //pass
            }
        });

        dialog = dialogBuilder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button negativeButton = ((androidx.appcompat.app.AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                Button positiveButton = ((androidx.appcompat.app.AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setText(supportSDK.callMeButtonText);
                positiveButton.setTextColor(supportSDK.appearance.callMeLabelTextColor());
                negativeButton.setTextColor(supportSDK.appearance.callMeLabelTextColor());
                positiveButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if ( validatePhoneNumber(mCallbackNumberEditText.getText().toString()) ) {
                            dialog.dismiss();
                            createIssue(supportSDK.memberID,
                                    supportSDK.memberUserID,
                                    supportSDK.memberLocationID,
                                    mCallbackNumberEditText.getText().toString(),
                                    mCallbackDescriptionEditText.getText().toString());
                        } else {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.error_bad_phone_number), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
        dialog.show();
    }


    private boolean validatePhoneNumber(String phoneNumber) {
        String pattern = "^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$";
//        pattern = "^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]\\d{3}[\\s.-]\\d{4}$";
        Matcher m;
        Pattern r = Pattern.compile(pattern);
        if ( phoneNumber!=null && !phoneNumber.isEmpty() ) {
            m = r.matcher(phoneNumber.trim());
            boolean found = m.find();
            return found;
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

        String uri = String.format("%s/issues/create", SupportSDK.SDK_V1_ENDPOINT);

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
                Issue issue = null;
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
                                    issue = new Issue(issueJSON);
                                    success = true;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (issue != null) {
                                    Issue.saveCurrentIssue(mContext, issue);
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
                if ( supportButton.mListener != null ) {
                    supportButton.mListener.supportButtonDidCompleteTask();
                }

            }
        });
    }


    private void showConfirmation(String message) {
         mActivity.runOnUiThread(() -> {
             AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
             final Runnable runnable = () -> {
                 if (alert.isShowing()) {
                     alert.dismiss();
                 }
             };

             alert.setOnDismissListener(dialog -> handler.removeCallbacks(runnable));

             handler.postDelayed(runnable, 3000);
         });
    }

}
