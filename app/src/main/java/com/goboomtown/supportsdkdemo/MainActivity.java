package com.goboomtown.supportsdkdemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.goboomtown.supportsdk.view.SupportButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements SupportButton.SupportButtonListener {

    public static final String TAG = "MainActivity";

    private FrameLayout mFragmentContainer;
    private View mView;
    private SupportButton mSupportButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        mFragmentContainer = findViewById(R.id.fragment_container);

        mSupportButton = findViewById(R.id.supportButton);
        mSupportButton.setListener(this);

        int configResource = R.raw.support_sdk;
        mSupportButton.loadConfiguration(configResource, null);

        Map<String, String> myPubData = new HashMap<>();
        myPubData.put("public", "fooData");
        Map<String, String> myPrivData = new HashMap<>();
        myPrivData.put("private", "someEncryptedData");

        mSupportButton.advertiseServiceWithPublicData(myPubData, myPrivData);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void supportButtonDidFailWithError(final String description, final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                builder.setTitle(description);
                builder.setMessage(reason);
                builder.setCancelable(false);

                builder.setPositiveButton("OK", null);

                final AlertDialog dlg = builder.create();
                if (dlg != null) {
                    dlg.show();
                }
            }
        });
    }

    @Override
    public void supportButtonDidGetSettings() {
        Log.d(TAG, "#helpButtonDidGetSettings");
    }

    @Override
    public void supportButtonDisplaySupportMenu(final View supportMenu) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PopupWindow popupWindow = new PopupWindow();
                popupWindow.setWindowLayoutMode(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setHeight(250);
                popupWindow.setWidth(350);
                popupWindow.setContentView(supportMenu);

                //set content and background

                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true);

                mFragmentContainer.setVisibility(View.VISIBLE);
                popupWindow.showAtLocation(mFragmentContainer, Gravity.CENTER, 0, 0);
            }
        });

    }

    @Override
    public void supportButtonDisplayChatFragment(final Fragment chatFragment) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, chatFragment)
                        .addToBackStack(null)
                        .commit();
                mFragmentContainer.setVisibility(View.VISIBLE);
            }
        });
//        startService(new Intent(this, SupportOverlayService.class));
    }

    @Override
    public void supportButtonSetChatTitle(String title) {
        setTitle(title);
    }

    @Override
    public void supportButtonRemoveChatFragment() {
        getSupportFragmentManager().popBackStack();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFragmentContainer.setVisibility(View.GONE);
                setTitle(getString(R.string.app_name));
            }
        });
    }

    @Override
    public void supportButtonDidAdvertiseService() {
        Log.i(TAG, "service advertised successfully");
    }

    @Override
    public void supportButtonDidFailToAdvertiseService() {
        Log.i(TAG, "error when advertising service");
    }
}
