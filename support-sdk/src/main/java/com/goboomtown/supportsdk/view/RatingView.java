package com.goboomtown.supportsdk.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import androidx.annotation.NonNull;

import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

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
public class RatingView {

    public SupportButton    supportButton;
    public SupportSDK       supportSDK;
    public Activity         mActivity;

    private Context     mContext;
    public  PopupWindow mPopupWindow;

    private TextView    mRatingLabel;
    private TextView    mRatingDescriptionLabel;
    private EditText    mRatingDescriptionEditText;
    private Button      mOkButton;

    private ImageView rate1;
    private ImageView rate2;
    private ImageView rate3;
    private ImageView rate4;
    private ImageView rate5;

    private int rating;


    public RatingView(Context context) {
        mContext = context;
    }

    public void show() {
        androidx.appcompat.app.AlertDialog.Builder dialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.rating_dialog, null);
        dialogBuilder.setView(dialogView);

        rating = 0;

        rate1 = dialogView.findViewById(R.id.rate1);
        rate2 = dialogView.findViewById(R.id.rate2);
        rate3 = dialogView.findViewById(R.id.rate3);
        rate4 = dialogView.findViewById(R.id.rate4);
        rate5 = dialogView.findViewById(R.id.rate5);
        setUpRatingsButtons();

        mRatingLabel      = dialogView.findViewById(R.id.ratingLabel);
        if ( mRatingLabel != null ) {
            mRatingLabel.setTextColor(supportSDK.appearance.ratingLabelTextColor());
        }
        mRatingDescriptionLabel =dialogView.findViewById(R.id.ratingDescriptionLabel);
        if ( mRatingDescriptionLabel != null ) {
            mRatingDescriptionLabel.setTextColor(supportSDK.appearance.ratingLabelTextColor());
        }
        mRatingDescriptionEditText = dialogView.findViewById(R.id.ratingDescription);
        if ( mRatingDescriptionEditText != null ) {
            mRatingDescriptionEditText.setTextColor(supportSDK.appearance.ratingLabelTextColor());
            mRatingDescriptionEditText.setEnabled(true);
            mRatingDescriptionEditText.setSingleLine(false);
        }

        dialogBuilder.setTitle(mContext.getResources().getString(R.string.label_rate));
        dialogBuilder.setPositiveButton(mContext.getResources().getString(R.string.text_submit), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String desc = mRatingDescriptionEditText.getText().toString();
                rateIssue(supportSDK.rateableIssueId, rating, desc);
            }
        });
        dialogBuilder.setNegativeButton(mContext.getResources().getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                if ( supportButton.mListener != null ) {
                    supportButton.mListener.supportButtonDidCompleteTask();
                }

            }
        });

        androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button negativeButton = ((androidx.appcompat.app.AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                Button positiveButton = ((androidx.appcompat.app.AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setTextColor(supportButton.appearance.ratingButtonTextColor());
                negativeButton.setTextColor(supportButton.appearance.ratingLabelTextColor());
            }
        });
        dialog.show();
    }


    private void setUpRatingsButtons() {

        if ( rate1==null || rate2==null || rate3==null || rate4==null || rate5==null ) {
            return;
        }
        rate1.setColorFilter(supportSDK.appearance.ratingLabelTextColor());
        rate2.setColorFilter(supportSDK.appearance.ratingLabelTextColor());
        rate3.setColorFilter(supportSDK.appearance.ratingLabelTextColor());
        rate4.setColorFilter(supportSDK.appearance.ratingLabelTextColor());
        rate5.setColorFilter(supportSDK.appearance.ratingLabelTextColor());
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
//        mOkButton.setEnabled(shouldEnable);
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
                if ( supportButton.mListener != null ) {
                    supportButton.mListener.supportButtonDidCompleteTask();
                }

            }
        });
    }



}
