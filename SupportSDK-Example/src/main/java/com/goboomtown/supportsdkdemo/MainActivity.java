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

import com.goboomtown.supportsdk.service.SupportOverlayService;
import com.goboomtown.supportsdk.view.SupportButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
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
import android.widget.Toast;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements SupportButton.SupportButtonListener {

    public static final String TAG = "MainActivity";

    private FrameLayout     mFragmentContainer;
    private LinearLayout    mSupportMenuContainer;
    private View            mView;
    private SupportButton   mSupportButton;
    private View            displayView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentContainer = findViewById(R.id.fragment_container);
        mSupportMenuContainer = findViewById(R.id.supportMenuContainer);

        mSupportButton = findViewById(R.id.supportButton);
        mSupportButton.setVisibility(View.GONE);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSupportButton.checkScreenCapturePermission(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if ( count > 0 ) {
            getSupportFragmentManager().popBackStack();
            if ( count == 1 ) {
//                hideActionBar();
                mFragmentContainer.setVisibility(View.GONE);
                setActionBarTitle(getString(R.string.app_name));

                mSupportButton.click();
            }
            count = getSupportFragmentManager().getBackStackEntryCount();
        } else {
//            mFragmentContainer.setVisibility(View.GONE);
//            hideActionBar();
            mFragmentContainer.setVisibility(View.GONE);
            setActionBarTitle(getString(R.string.app_name));

            mSupportButton.click();
            super.onBackPressed();
        }
    }

    private void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if ( actionBar != null ) {
            actionBar.show();
        }
    }

    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if ( actionBar != null ) {
            actionBar.hide();
        }
    }

    private void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if ( actionBar == null ) {
            return;
        }
        mSupportButton.appearance.configureActionBarTitle(actionBar, title);
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
        POSConnector posConnector = new POSConnector(mSupportButton);
        mSupportButton.menuStyle = SupportButton.MenuStyle.ICON_LIST;
        mSupportButton.click();
    }

    @Override
    public void supportButtonDidFailToGetSettings() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), getString(R.string.warn_settings), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void supportButtonDidRetrieveAccount(HashMap<String, String> accountInfo) {

    }

    @Override
    public void supportButtonDidFailToRetrieveAccount(String message) {
        Log.v(TAG, message);
    }

    @Override
    public void supportButtonDisplayView(final View view) {
        if ( view == null ) {
            return;
        }
        displayView = view;
        mSupportButton.showLoginPrompt = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                displayView.setLayoutParams(params);
                mSupportMenuContainer.addView(displayView);
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
                if ( displayView != null ) {
                    mSupportMenuContainer.removeView(displayView);
                    displayView = null;
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
    public void supportButtonDidCompleteTask() {
        if ( displayView != null ) {
            mSupportMenuContainer.removeView(displayView);
            displayView = null;
        }
        mSupportButton.click();
    }

    @Override
    public void supportButtonDidRequestExit() {
        if ( displayView != null ) {
            mSupportMenuContainer.removeView(displayView);
            displayView = null;
        }
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
