package com.goboomtown.supportsdkdemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements SupportButton.SupportButtonListener {

    public static final String TAG = "MainActivity";

    private FrameLayout     mFragmentContainer;
    private LinearLayout    mSupportMenuContainer;
    private View mView;
    private SupportButton mSupportButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle(R.string.app_name);

        mFragmentContainer = findViewById(R.id.fragment_container);
        mSupportMenuContainer = findViewById(R.id.supportMenuContainer);

        mSupportButton = findViewById(R.id.supportButton);
        mSupportButton.setVisibility(View.GONE);
        mSupportButton.setListener(this);

        mSupportButton.appearance.setIconColor(Color.RED);
        mSupportButton.appearance.setTextColor(Color.BLACK);

        int configResource = R.raw.support_sdk_preprod; // R.raw.support_sdk;
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
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if ( count > 0 ) {
            getSupportFragmentManager().popBackStack();
            if ( count == 1 ) {
                mFragmentContainer.setVisibility(View.GONE);
                setTitle(getString(R.string.app_name));
            }
            count = getSupportFragmentManager().getBackStackEntryCount();
        } else {
            mFragmentContainer.setVisibility(View.GONE);
            setTitle(getString(R.string.app_name));
            super.onBackPressed();
        }
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
//        mSupportButton.menuStyle = SupportButton.MenuStyle.BUTTON;
        mSupportButton.menuStyle = SupportButton.MenuStyle.ICON_LIST;
        mSupportButton.click();
    }

    @Override
    public void supportButtonDidFailToGetSettings() {

    }

    @Override
    public void supportButtonDisplayView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                view.setLayoutParams(params);
                mSupportMenuContainer.addView(view);
            }
        });

    }


    @Override
    public void supportButtonDisplayFragment(final Fragment fragment, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
                if ( title != null ) {
                    setTitle(title);
                }
                mFragmentContainer.setVisibility(View.VISIBLE);
            }
        });
//        startService(new Intent(this, SupportOverlayService.class));
    }

    @Override
    public void supportButtonSetTitle(String title) {
        setTitle(title);
    }


    @Override
    public void supportButtonRemoveFragment(Fragment fragment) {
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
