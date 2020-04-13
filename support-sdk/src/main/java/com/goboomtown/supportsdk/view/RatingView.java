package com.goboomtown.supportsdk.view;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * TODO: document your custom view class.
 */
public class RatingView extends LinearLayout {

    public SupportButton    supportButton;
    public SupportSDK       supportSDK;
    public Activity         mActivity;

    private Context     mContext;
    public  PopupWindow mPopupWindow;

    private EditText    mCallbackDescriptionEditText;
    private Button      mOkButton;

    private ImageView rate1;
    private ImageView rate2;
    private ImageView rate3;
    private ImageView rate4;
    private ImageView rate5;

    private int rating;


    public RatingView(Context context) {
        super(context);
        mContext = context;

        View view = inflate(mContext, R.layout.rating_view, this);

        rating = 0;

        rate1 = view.findViewById(R.id.rate1);
        rate2 = view.findViewById(R.id.rate2);
        rate3 = view.findViewById(R.id.rate3);
        rate4 = view.findViewById(R.id.rate4);
        rate5 = view.findViewById(R.id.rate5);
        setUpRatingsButtons();

        mCallbackDescriptionEditText = view.findViewById(R.id.callbackDescription);

        mCallbackDescriptionEditText.setEnabled(true);
        mCallbackDescriptionEditText.setSingleLine(false);

        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            if ( mPopupWindow != null ) {
                mPopupWindow.dismiss();
            }
        });
        mOkButton = view.findViewById(R.id.okButton);
//        enableOkButton(callbackNumber);
        mOkButton.setEnabled(true);
        mOkButton.setOnClickListener(v -> {
            if ( mPopupWindow != null ) {
                mPopupWindow.dismiss();
                String desc = mCallbackDescriptionEditText.getText().toString();
                rateIssue(supportSDK.rateableIssueId, rating, desc);
            }
        });
    }


    private void setUpRatingsButtons() {

        rate1.setOnClickListener(v -> {
            makeSelection(rate1);
            clearOthers(rate2, rate3, rate4, rate5);
        });
        rate2.setOnClickListener(v -> {
            makeSelection(rate1, rate2);
            clearOthers(rate3, rate4, rate5);
        });
        rate3.setOnClickListener(v -> {
            makeSelection(rate1, rate2, rate3);
            clearOthers(rate4, rate5);
        });
        rate4.setOnClickListener(v -> {
            makeSelection(rate1, rate2, rate3, rate4);
            clearOthers(rate5);
        });
        rate5.setOnClickListener(v -> makeSelection(rate1, rate2, rate3, rate4, rate5));
    }

//    private void setOnClickListenersForRatings(final Button... buttons) {
//
//        for (final Button button : buttons) {
//            button.setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    button.setSelected(true);
//                    clearSelection(button.getId(), buttons);
//                    enableSubmitButtonIfPossible();
//                }
//            });
//        }
//    }

//    private void clearSelection(int id, Button... buttons) {
//        for (Button button : buttons) {
//            if (button.getId() != id) {
//                button.setSelected(false);
//            }
//        }
//    }

    private void clearOthers(ImageView... imageViews) {
        for (ImageView imageView : imageViews) {
            imageView.setImageResource(R.drawable.star_off_sm);
        }
    }

    private void makeSelection(ImageView... imageViews) {
        for (ImageView imageView : imageViews) {
            imageView.setImageResource(R.drawable.star_on_sm);
        }
        rating = imageViews.length;
        enableSubmitButtonIfPossible();
    }

    private void enableSubmitButtonIfPossible() {
        boolean shouldEnable = rating != 0;
        mOkButton.setEnabled(shouldEnable);
    }

    private void rateIssue(String issue_id, int rating, String description) {
        JSONObject params = new JSONObject();
        try {
            params.put("nps_rating", 0);
            params.put("tech_rating", rating);
            if ( description != null ) {
                params.put("notes", description);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = String.format("%s/issues/rate/%s", SupportSDK.kSDKV1Endpoint, issue_id);

        supportSDK.post(uri, params, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (supportButton.mListener != null) {
                    supportButton.mListener.supportButtonDidFailWithError(mActivity.getString(R.string.error_unable_to_rate_issue), e.getLocalizedMessage());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean success = false;
                String message = "";

                JSONObject jsonObject;
                try {
                    ResponseBody responseBody = response.body();
                    String responseBodyString = Objects.requireNonNull(responseBody).string();
                    jsonObject = new JSONObject(responseBodyString);
                    success = jsonObject.optBoolean("success");
                    if (success) {
                        JSONArray results = jsonObject.optJSONArray("results");
                        if (results != null && results.length() > 0) {
                            supportSDK.rateableIssueId = null;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if ( !success ) {
                    if (supportButton.mListener != null) {
                        supportButton.mListener.supportButtonDidFailWithError(mContext.getString(R.string.error_unable_to_rate_issue), message);
                    }
                }
            }
        });
    }



}
