package com.goboomtown.supportsdk.sample;

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
import android.provider.Settings;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.goboomtown.supportsdk.proactive.SupportSDKProactive;
import com.goboomtown.supportsdk.view.SupportButton;
import com.goboomtown.supportsdk.service.SupportOverlayService;
//import com.goboomtown.supportsdk.activity.ScreenCaptureActivity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements SupportButton.SupportButtonListener {

    public static final String TAG = "MainActivity";

    public static final boolean kDemoMode   = true;

    private LinearLayout        mDemoContainer;
    private LinearLayout        mSupportMenuContainer;
    private FrameLayout         mFragmentContainer;
    private View                mView;
    private SupportButton       mSupportButton;
    private SupportSDKProactive mProactiveAgent;

    private int             mConfigResource = R.raw.support_sdk;
//    private int             mConfigResource = R.raw.support_sdk_integ;
//    private int             mConfigResource = R.raw.support_sdk_preprod;

    private boolean         proactiveEnabled = true;
    private boolean         emailPromptEnabled = true;

    @Override
    protected void onStart() {
        super.onStart();
        if ( proactiveEnabled ) {
            mProactiveAgent = new SupportSDKProactive(getBaseContext(), mConfigResource, null);
        }
//        checkOverlayPermission();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setConfigType();

        mDemoContainer          = findViewById(R.id.demoContainer);
        mSupportMenuContainer   = findViewById(R.id.supportMenuContainer);
        mFragmentContainer      = findViewById(R.id.fragment_container);

        mSupportButton = findViewById(R.id.supportButton);
        mSupportButton.setListener(this);

        mSupportButton.loadConfiguration(mConfigResource, null);

        Map<String, String> myPubData = new HashMap<>();
        myPubData.put("public", "fooData");
        Map<String, String> myPrivData = new HashMap<>();
        myPrivData.put("private", "someEncryptedData");

        mSupportButton.advertiseServiceWithPublicData(myPubData, myPrivData);

        if ( !kDemoMode ) {
            if (emailPromptEnabled) {
                promptForCustomerInformation();
            }
        } else {
            hideActionBar();
            mSupportButton.setVisibility(View.GONE);
            mDemoContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if ( count > 0 ) {
            getSupportFragmentManager().popBackStack();
            if ( count == 1 ) {
                hideActionBar();
            }
            count = getSupportFragmentManager().getBackStackEntryCount();
        } else {
            hideActionBar();
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

    protected void setConfigType() {
        mConfigResource = R.raw.support_sdk;
        String flavor = null;
        try {
            Context context = getApplicationContext();
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            flavor = bundle.getString("flavor");
            if ( flavor.toLowerCase().contentEquals("stage") ) {
                mConfigResource = R.raw.support_sdk_preprod;
//                mConfigResource = R.raw.support_sdk_integ;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( proactiveEnabled ) {
            mProactiveAgent.putAppHealthCheck("app-running", SupportSDKProactive.Check.CheckStatus.OK, "");
            mProactiveAgent.putAppHardwareCheck("cc-scanner", SupportSDKProactive.Check.CheckStatus.CRITICAL, "device offline");
            mProactiveAgent.putAppCloudCheck("merchant-ep", SupportSDKProactive.Check.CheckStatus.WARNING, "HTTP status code 401");
            mProactiveAgent.putAppDumpCheck(new File("dump_crash.log"), SupportSDKProactive.Check.CheckStatus.OK, "");
        }

//        checkOverlayPermission();
        Log.v(TAG, "#onResume complete");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "#onPause complete");
    }


    protected void promptForCustomerInformation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

                alertDialog.setTitle("Login with Email Address");
                alertDialog.setMessage("This is optional. Just hit cancel if you wish.");
                final EditText input = new EditText(MainActivity.this);
                input.setHint("Enter email address");
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input); // uncomment this line
                alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        HashMap<String, String> customerInfo = new HashMap<String, String>();
                        customerInfo.put(SupportButton.kUserEmail, input.getText().toString());
//                        mSupportButton.loadConfiguration(mConfigResource, customerInfo);
                        mSupportButton.getCustomerInformation(customerInfo);
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
//                        mSupportButton.loadConfiguration(mConfigResource, null);
                    }
                });
                AlertDialog b = alertDialog.create();
                b.show();
            }
        });
    }


    public static void requestSystemAlertPermission(Activity context, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        final String packageName = context == null ? context.getPackageName() : context.getPackageName();
        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName));
        if (context != null)
            context.startActivityForResult(intent, requestCode);
        else
            context.startActivityForResult(intent, requestCode);
    }

    @TargetApi(23)
    public static boolean isSystemAlertPermissionGranted(Context context) {
        final boolean result = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
        return result;
    }

    private void checkOverlayPermission() {
        if ( Build.VERSION.SDK_INT > 22 ) {
            if (!Settings.canDrawOverlays(this)) {
//                Handler checkOverlayPermissionHandler = new Handler();
//                Runnable checkOverlaySetting = new Runnable() {
//                    @Override
//                    @TargetApi(23)
//                    public void run() {
//                        if (Settings.canDrawOverlays(MainActivity.this)) {
//                            //You have the permission, re-launch MainActivity
//                            Intent i = new Intent(MainActivity.this, MainActivity.class);
//                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(i);
//                            return;
//                        }
//                        checkOverlayPermissionHandler.postDelayed(this, 650);
//                    }
//                };
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
//                checkOverlayPermissionHandler.postDelayed(checkOverlaySetting, 650);
            }
        } else {
            startService(new Intent(MainActivity.this,
                    SupportOverlayService.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSupportButton.checkScreenCapturePermission(requestCode, resultCode, data);
        if (requestCode == 1234) {
            startService(new Intent(MainActivity.this,
                    SupportOverlayService.class));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        mSupportButton.checkScreenCapturePermission(requestCode, resultCode, data);
//        super.onActivityResult(requestCode, resultCode, data);
//    }


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
        if ( kDemoMode ) {
            mSupportButton.menuStyle = SupportButton.MenuStyle.BUTTON;
            mSupportButton.click();
        }
    }

    @Override
    public void supportButtonDidFailToGetSettings() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Unable to retrieve settings", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    @Override
//    public void supportButtonDisplaySupportMenu(final View supportMenu) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
////                final PopupWindow popupWindow = new PopupWindow(supportMenu,
////                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
//
//                PopupWindow popupWindow = new PopupWindow();
//                popupWindow.setWindowLayoutMode(
//                        WindowManager.LayoutParams.WRAP_CONTENT,
//                        WindowManager.LayoutParams.WRAP_CONTENT);
//                popupWindow.setHeight(250);
//                popupWindow.setWidth(350);
//                popupWindow.setContentView(supportMenu);
//
//                //set content and background
//
//                popupWindow.setTouchable(true);
//                popupWindow.setFocusable(true);
//
////                RelativeLayout relativeLayout = findViewById(R.id.relativeLayout);
////                relativeLayout.addView(supportMenu);
////                popupWindow.showAsDropDown(mSupportButton, 0, 0);
//                mFragmentContainer.setVisibility(View.VISIBLE);
//                popupWindow.showAtLocation(mFragmentContainer, Gravity.CENTER, 0, 0);
//           }
//        });
//
//    }

    @Override
    public void supportButtonDisplayView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSupportMenuContainer.addView(view);
            }
        });

    }

    @Override
    public void supportButtonDisplayFragment(final Fragment fragment, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int count = getSupportFragmentManager().getBackStackEntryCount();

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
//                getSupportFragmentManager().beginTransaction()
//                        .add(R.id.fragment_container, fragment)
//                        .commit();
                showActionBar();
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
                hideActionBar();
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
