package com.goboomtown.supportsdk.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * TODO: document your custom view class.
 */
public class SupportMenuView extends FrameLayout {

    private static final String TAG = SupportMenuView.class.getSimpleName();

    public SupportButton    supportButton;
    public SupportSDK       supportSDK;
    public Activity         mActivity;

    private Context         mContext;

    private LinearLayout        mEmailEntryView;
    private LinearLayout        mMenuView;
    private EditText            mEmailEditText;
    private GridView            mGridView;
    public  ArrayList<SupportMenuButton> mButtons = new ArrayList<>();

    public SupportMenuView(Context context) {
        super(context);
        mContext = context;

        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));


        View view = inflate(mContext, R.layout.support_menu_view, this);

        mEmailEntryView = view.findViewById(R.id.emailEntryView);
        mMenuView       = view.findViewById(R.id.menuView);

        mEmailEditText = view.findViewById(R.id.emailEditText);
        Button submitButton = view.findViewById(R.id.submitButton);
        submitButton.setBackgroundColor(Color.RED);
        submitButton.setOnClickListener(v -> {
            String emailAddress = mEmailEditText.getText().toString();
            if ( !emailAddress.isEmpty() ) {
                getCustomerInfo(emailAddress);
            } else {
                showMenu();
            }
        });
        mGridView = view.findViewById(R.id.gridView);
        mGridView.setOnItemClickListener((parent, view1, position, id) -> {
            SupportMenuButton button = mButtons.get(position);
        });

        mMenuView.setVisibility(View.GONE);
    }


    public void refresh() {
        CustomGridAdapter gridAdapter = new CustomGridAdapter(mContext, mButtons);
        mGridView.setAdapter(gridAdapter);
        gridAdapter.notifyDataSetChanged();
    }

    private void showMenu() {
        mActivity.runOnUiThread(() -> {
            mEmailEntryView.setVisibility(View.GONE);
            mMenuView.setVisibility(View.VISIBLE);
        });
    }

    private void getCustomerInfo(String emailAddress) {
        HashMap<String, String> customerInfo = new HashMap<>();
        customerInfo.put(SupportButton.kUserEmail, emailAddress);
        supportSDK.restGetCustomerInformationWithInfo(customerInfo, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showMenu();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                showMenu();
                if (response.code() > 199 && response.code() < 300) {
                    try {
                        ResponseBody responseBody = response.body();
                        String responseBodyString = Objects.requireNonNull(responseBody).string();
                        JSONObject jsonObject = new JSONObject(responseBodyString);
                        supportSDK.memberID = jsonObject.optString("members_id");
                        supportSDK.memberUserID = jsonObject.optString("members_users_id");
                        supportSDK.memberLocationID = jsonObject.optString("members_locations_id");
                        supportSDK.memberDeviceID = jsonObject.optString("members_devices_id");
                    } catch (JSONException e) {
                        Log.w(TAG, Log.getStackTraceString(e));
                    } catch (Exception e) {
                        Log.w(TAG, Log.getStackTraceString(e));
                    }
                }
            }
        });
    }



}
