package com.goboomtown.supportsdk.activity;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.goboomtown.supportsdk.api.SupportSDK;

public class ScreenCaptureActivity extends AppCompatActivity {

    public SupportSDK supportSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // supportSDK.checkScreenCapturePermission(requestCode, resultCode, data);  -- not instantiated yet
        super.onActivityResult(requestCode, resultCode, data);
    }

}
