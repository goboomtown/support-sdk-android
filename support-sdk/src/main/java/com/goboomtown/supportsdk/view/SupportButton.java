package com.goboomtown.supportsdk.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.goboomtown.activity.KBActivity;
import com.goboomtown.chat.BoomtownChat;
import com.goboomtown.forms.model.FormModel;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.activity.ScreenCaptureActivity;
import com.goboomtown.supportsdk.api.Appearance;
import com.goboomtown.supportsdk.api.EventManager;
import com.goboomtown.supportsdk.api.POSConnectorBase;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.dnssd.BTConnectPresenceService;
import com.goboomtown.supportsdk.fragment.ChatFragment;
import com.goboomtown.supportsdk.fragment.FormListFragment;
import com.goboomtown.supportsdk.fragment.HistoryListFragment;
import com.goboomtown.supportsdk.fragment.JourneysFragment;
import com.goboomtown.supportsdk.fragment.SettingsFragment;
import com.goboomtown.supportsdk.fragment.SupportFormFragment;
import com.goboomtown.supportsdk.fragment.KBListFragment;
import com.goboomtown.supportsdk.model.Issue;
import com.goboomtown.supportsdk.model.BTMerchant;
import com.goboomtown.supportsdk.model.JourneyModel;
import com.goboomtown.supportsdk.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * TODO: document your custom view class.
 */
public class SupportButton extends View
        implements SupportSDK.SupportSDKListener,
        SupportSDK.SupportSDKFormsListener,
        POSConnectorBase.POSConnectorListener,
        EventManager.EventManagerListener {

    private static final String TAG = SupportButton.class.getSimpleName();

    public final static String chatMenuText                = "chatMenuText";
    public final static String callMeMenuText              = "callmeMenuText";
    public final static String knowledgeMenuText           = "knowledgeMenuText";
    public final static String webMenuText                 = "webMenuText";
    public final static String emailMenuText               = "emailMenuText";
    public final static String phoneMenuText               = "phoneMenuText";
    public final static String formsMenuText               = "formsMenuText";
    public final static String historyMenuText             = "historyMenuText";
    public final static String exitMenuText                = "exitMenuText";


    @Override
    public void supportSDKDidRetrieveForms() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void supportSDKDidRetrieveForm(FormModel form) {

    }

    @Override
    public     void supportSDKDidUpdateForm() {
    }

    @Override
    public     void supportSDKFailedToUpdateForm() {
    }

    @Override
    public void event(String name, String type, HashMap<String, String> userInfo) {
        if ( type.equals(EventManager.kRequestSupportSDKExit) ) {

        }
    }


    public enum MenuStyle {
        NO_MENU(0),
        MENU(1),
        BUTTON(2),
        ICON_LIST(3),
        ICON_LIST_EXIT(4),
        ICON_GRID(5);

        MenuStyle(int code) {}
    };

    // TODO: remove unused constants and/or rename with all-caps per Java convention for constants
    private static final String SupportSDKErrorDomain = "com.goboomtown.supportsdk";


    /* Customer information keys */
    public  static final String KEY_CUSTOMER_ID                     = "members_id";
    public  static final String KEY_CUSTOMER_EXTERNAL_ID            = "members_external_id";
    public  static final String KEY_CUSTOMER_LOCATION_ID            = "members_locations_id";
    public  static final String KEY_CUSTOMER_LOCATION_EXTERNAL_ID   = "members_locations_external_id";
    public  static final String KEY_CUSTOMER_LOCATION_MID           = "members_locations_mid";
    public  static final String KEY_CUSTOMER_USER_ID                = "members_users_id";
    public  static final String KEY_CUSTOMER_USER_EXTERNAL_ID       = "members_users_external_id";
    public  static final String KEY_CUSTOMER_USER_EMAIL             = "members_users_email";
    public  static final String KEY_CUSTOMER_USER_PHONE             = "members_users_phone";


    public  boolean     useSupportView;
    public  MenuStyle   menuStyle;
    public  boolean     dismissMenuOnClick = false;
    public  boolean     showLoginPrompt;

    private SupportSDK          supportSDK;
    private SupportMenuView     supportMenuView;
    private ChatFragment        chatFragment;
    private SupportFormFragment formFragment;
    private HistoryListFragment mHistoryListFragment;
    private KBListFragment      kbListFragment;
    public  ArrayList<SupportMenuEntry> mEntries = new ArrayList<>();

    public WeakReference<Context> mContext;

    private BTConnectPresenceService mDNSSvc;
    private boolean mDNSSvcEnabled;
    private BTConnectPresenceService.ServiceAdvertisementListener mDNSSvcListener;
    private WeakReference<Activity> mActivity;
    private boolean clickedMethodRequeued;

    private PopupWindow mPopupWindow;

    public Appearance appearance;

    private String  xmppdata;

    public  String  appVersion;

    public interface SupportButtonListener {
        void supportButtonDidFailWithError(String description, String reason);
        void supportButtonDidGetSettings();
        void supportButtonDidFailToGetSettings();
        void supportButtonDidRetrieveAccount(HashMap<String, String> accountInfo);
        void supportButtonDidFailToRetrieveAccount(String message);
        void supportButtonDisplayView(View view);
        void supportButtonDisplayFragment(Fragment fragment, String title);
        void supportButtonRemoveFragment(Fragment fragment);
        void supportButtonSetTitle(String title);
        void supportButtonDidAdvertiseService();
        void supportButtonDidFailToAdvertiseService();
        void supportButtonDidRequestExit();
        void supportButtonDidCompleteTask();
    }

    public SupportButtonListener mListener;

    public SupportButton(Context context) {
        super(context);
        mContext = new WeakReference<>(context);
        init(null, 0);
    }

    public SupportButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = new WeakReference<>(context);
        init(attrs, 0);
    }

    public SupportButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = new WeakReference<>(context);
        init(attrs, defStyle);
    }

    public void enableTLSv13(boolean enable) {
        if ( supportSDK != null ) {
            supportSDK.isTLSv13Supported = enable;
        }
    }

    private void init(AttributeSet attrs, int defStyle) {
        mActivity = new WeakReference<>(Utils.getActivity(getContext()));

        BoomtownChat.sharedInstance().context = mActivity.get();

        if ( ScreenCaptureActivity.class.isAssignableFrom(mActivity.getClass()) &&
                Utils.doesObjectContainField(mActivity.get(), "supportSDK") ) {
            ((ScreenCaptureActivity) mActivity.get()).supportSDK = supportSDK;
        }

        setOnClickListener(v -> clicked());

        EventManager.setContext(getContext());
        EventManager.addObserver(mEventReceiver);
        EventManager.notify(EventManager.kEventSDKStarted, null);
    }


    /**
     * @param listener {@link SupportButtonListener} to set
     */
    @SuppressWarnings({"unused"})
    public void setListener(SupportButtonListener listener) {
        mListener = listener;
    }


    public void reset()
    {
        supportSDK.cloudConfigComplete = false;
    }

    private void getMenuStyle() {
        supportSDK.appearance.menuStyle();
        List<String> menuStyleOptions = Arrays.asList("nomenu", "menu", "button", "iconlist", "iconlistexit");
        List<MenuStyle> menuStyles = Arrays.asList(MenuStyle.NO_MENU,
                MenuStyle.MENU, MenuStyle.BUTTON, MenuStyle.ICON_LIST, MenuStyle.ICON_LIST_EXIT);
        int style = menuStyleOptions.indexOf(supportSDK.appearance.menuStyle());
        menuStyle = style==-1 ? MenuStyle.ICON_LIST : menuStyles.get(style);
    }


    public void click() {
        getMenuStyle();
        switch(menuStyle) {
            case ICON_LIST:
            case ICON_LIST_EXIT:
            case ICON_GRID:
            case BUTTON:
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        createSupportMenuView();
                    }
                });
                break;
            case MENU:
            default:
                showSupportDialog();
                break;
        }
    }

    private void clicked() {
        if (!supportSDK.cloudConfigComplete) {
            if (!clickedMethodRequeued) {
                getHandler().post(this::clicked);
                clickedMethodRequeued = true;
                Log.w(TAG, "SDK configuration pending - click re-queued");
            } else {
                Log.w(TAG, "SDK configuration pending - click already re-queued");
            }
            return;
        }
        clickedMethodRequeued = false;
        if ( supportSDK.providerId != null ) {
            showSupportDialog();
        } else {
            if (mListener != null) {
                mListener.supportButtonDidFailWithError(mContext.get().getString(R.string.error_unable_to_get_provider_info), "");
            }
        }
    }


    public void loadConfiguration(String jsonString, String customerInfoJsonString) {
        saveDefaultJSONConfig(jsonString);
        HashMap<String, String> customerInfo = buildCustomerInfo(customerInfoJsonString);
        supportSDK = new SupportSDK(getContext(), jsonString, customerInfo, this);
        appearance = supportSDK.appearance;
        supportSDK.checkAppVersion(appVersion);
    }


    private HashMap<String, String> buildCustomerInfo(String string) {
        HashMap<String, String> info = new HashMap<>();
        if ( string != null ) {
            try {
                JSONObject customerInfoJson = new JSONObject(string);
                Iterator<String> iter = customerInfoJson.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    info.put(key, customerInfoJson.getString(key));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return info;
    }


    public void saveDefaultJSONConfig(String jsonString) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.get());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SupportSDK.KEY_DEV_MODE_JSON_CONFIG_DEFAULT, jsonString);
        editor.apply();
    }


    public void sendRequest(String request) {
        EventManager.notify(request, null);
    }


    private void displayChat(Issue issue) {
        chatFragment = new ChatFragment();
        chatFragment.mContext = getContext();
        chatFragment.supportSDK = supportSDK;
        chatFragment.mIssue = issue;
        chatFragment.chatTitle = "";
        chatFragment.mSupportButton = this;
        if (mListener != null) {
            mListener.supportButtonDisplayFragment(chatFragment, null);
        }
    }


    private void displayKnowledgeBase() {
        kbListFragment = new KBListFragment();
        kbListFragment.mContext = getContext();
        kbListFragment.supportSDK = supportSDK;
        kbListFragment.mSupportButton = this;
        if (mListener != null) {
            String title = (String) supportSDK.appearance.knowledgeMenuText();
            mListener.supportButtonDisplayFragment(kbListFragment, title);
        }
    }


    public void checkScreenCapturePermission(int requestCode, int resultCode, Intent data) {
        Boolean rc = supportSDK.checkScreenCapturePermission(requestCode, resultCode, data);
        if ( rc ) {
            chatFragment.commGetVideo(supportSDK.callId);
        }
    }


    public void setTitle(String title) {
        if (mListener != null) {
            mListener.supportButtonSetTitle(title);
        }
    }

    public void removeChat() {
        if (mListener != null) {
            mListener.supportButtonRemoveFragment(chatFragment);
        }
        if ( supportSDK.rateableIssueId != null ) {
            displayRatingScreen();
        }
    }

    public void removeKnowledgeBase() {
        if (mListener != null) {
            mListener.supportButtonRemoveFragment(kbListFragment);
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if ((mDNSSvc != null) && mDNSSvcEnabled && !mDNSSvc.isSvcRegistered() && View.VISIBLE == getVisibility()) {
            mDNSSvc.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDNSSvcEnabled) {
            mDNSSvc.tearDown();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if ( changedView.getClass().getSimpleName().equals(SupportButton.class.getSimpleName())
                && !(View.GONE == visibility || View.INVISIBLE == visibility)
                && (mDNSSvc != null) && mDNSSvcEnabled
                && !mDNSSvc.isSvcRegistered()) {
            mDNSSvc.start();
        }
    }

    public void supportSDKDidFailWithError(String description, String reason) {
        if (mListener != null) {
            mListener.supportButtonDidFailWithError(description, reason);
        }
    }

    public void supportSDKDidGetSettings() {
        removePreviousMenuIfNecessary();
        createSupportEntries();
         if (this.supportSDK.memberID == null) {
            this.supportSDK.memberID = this.supportSDK.defaultMemberID;
        }

        if (this.supportSDK.memberUserID == null) {
            this.supportSDK.memberUserID = this.supportSDK.defaultMemberUserID;
        }

        if (this.supportSDK.memberLocationID == null) {
            this.supportSDK.memberLocationID = this.supportSDK.defaultMemberLocationID;
        }
        this.supportSDK.saveMemberInfo();
        if ( supportSDK.journeysEnabled ) {
            this.supportSDK.getJourneys(new Callable<Void>() {
                public Void call() {
                    if (mListener != null) {
                        mListener.supportButtonDidGetSettings();
                    }
                    return null;
                }
            });
        }

        if ( this.supportSDK.kbEnabled && !this.supportSDK.isKBRequested ) {
            this.supportSDK.isKBRequested = true;
            this.supportSDK.getKB(null);
        }
        if ( this.supportSDK.formsEnabled && this.supportSDK.hasForms() ) {
            this.supportSDK.getForms(this);
        }
        if ( this.supportSDK.historyEnabled ) {
            this.supportSDK.getHistory();
        }
        if ( !supportSDK.journeysEnabled ) {
            if (mListener != null) {
                mListener.supportButtonDidGetSettings();
            }
        }
    }

    private boolean isCallable(Intent intent) {
        List<ResolveInfo> list = mContext.get().getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    public void supportSDKDidFailToGetSettings() {
        if (mListener != null) {
            mListener.supportButtonDidFailToGetSettings();
        }
    }


    @Override
    public void posConnectorDidRetrieveAccount(BTMerchant merchant) {
        if ( merchant != null ) {
            Log.d(TAG, merchant.name);
            HashMap<String, String> customerInfo = new HashMap<>();
            customerInfo.put(KEY_CUSTOMER_EXTERNAL_ID, merchant.mid);
            customerInfo.put(KEY_CUSTOMER_LOCATION_MID, merchant.mid);
            customerInfo.put(KEY_CUSTOMER_LOCATION_EXTERNAL_ID, merchant.deviceId);
            supportSDK.restGetCustomerInformationWithInfo(customerInfo, null);
            if (mListener != null) {
                mListener.supportButtonDidRetrieveAccount(customerInfo);
            }
        }
    }

    @Override
    public void posConnectorDidToFailRetrieveAccount(String message) {
        if (mListener != null) {
            mListener.supportButtonDidFailToRetrieveAccount(message);
        }
    }


    private void createSupportEntries() {
        mEntries.clear();
        if ( supportSDK.chatEnabled && supportSDK.memberID!=null && supportSDK.memberUserID!=null && supportSDK.memberLocationID!=null ) {
            SupportMenuEntry entry = new SupportMenuEntry();
            entry.label = supportSDK.appearance.chatMenuText();
            entry.drawable = supportSDK.appearance.chatMenuIcon();
            entry.onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventManager.notify(EventManager.kEventMenuEnded, null);
                    if ( supportSDK.supportUnavailable ) {
                        AlertDialog.Builder unavailableBuilder = new AlertDialog.Builder(mContext.get());
                        unavailableBuilder.setTitle(R.string.text_unavailable);
                        unavailableBuilder.setMessage(supportSDK.supportUnavailableSummary);
                        unavailableBuilder.setNeutralButton(mContext.get().getString(R.string.text_ok),
                                (unavailableDialog, which) -> unavailableDialog.dismiss());
                        unavailableBuilder.show();
                    } else {
                        getOrCreateIssue();
                    }
                }
            };
            mEntries.add(entry);
        }
        if ( supportSDK.callmeEnabled ) {
            SupportMenuEntry entry = new SupportMenuEntry();
            entry.label = supportSDK.appearance.callMeMenuText();
            entry.drawable = supportSDK.appearance.callMeMenuIcon();
            entry.onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventManager.notify(EventManager.kEventMenuEnded, null);
                    getCallbackNumber();
                }
            };
            mEntries.add(entry);
        }
        if ( supportSDK.kbEnabled ) {
            SupportMenuEntry entry = new SupportMenuEntry();
            entry.label = (String) supportSDK.appearance.knowledgeMenuText();
            entry.drawable = (Drawable) supportSDK.appearance.knowledgeMenuIcon();
            entry.onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventManager.notify(EventManager.kEventMenuEnded, null);
                    displayKnowledgeBase();
                }
            };
            mEntries.add(entry);
        }
        if ( supportSDK.supportWebsiteURL!=null && supportSDK.websiteEnabled ) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, supportSDK.supportWebsiteURL);
            PackageManager packageManager = mContext.get().getPackageManager();
            List<ResolveInfo> list = packageManager.queryIntentActivities(webIntent, 0);
            if ( list.size() > 0 ) {
                SupportMenuEntry entry = new SupportMenuEntry();
                entry.label = (String) supportSDK.appearance.webMenuText();
                entry.drawable = (Drawable) supportSDK.appearance.webMenuIcon();
                entry.onClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventManager.notify(EventManager.kEventMenuEnded, null);
                        visitWebsite();
                    }
                };
                mEntries.add(entry);
            }
        }
        if ( supportSDK.supportEmailAddress!=null && supportSDK.emailEnabled ) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            PackageManager packageManager = mContext.get().getPackageManager();
            List<ResolveInfo> list = packageManager.queryIntentActivities(emailIntent, 0);
            if ( list.size() > 0 ) {
                SupportMenuEntry entry = new SupportMenuEntry();
                entry.label = (String) supportSDK.appearance.emailMenuText();
                entry.drawable = (Drawable) supportSDK.appearance.emailMenuIcon();
                entry.onClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventManager.notify(EventManager.kEventMenuEnded, null);
                        sendEmail();
                    }
                };
                mEntries.add(entry);
            }
        }
        if ( supportSDK.supportPhoneNumber!=null && supportSDK.phoneEnabled ) {
            PackageManager packageManager = mContext.get().getPackageManager();
            if ( packageManager!=null && packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) ) {
                SupportMenuEntry entry = new SupportMenuEntry();
                entry.label = (String) supportSDK.appearance.phoneMenuText();
                entry.drawable = (Drawable) supportSDK.appearance.phoneMenuIcon();
                entry.onClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventManager.notify(EventManager.kEventMenuEnded, null);
                        phone();
                    }
                };
                mEntries.add(entry);
            }
        }
        if ( supportSDK.formsEnabled ) {
            SupportMenuEntry entry = new SupportMenuEntry();
            entry.label = (String) supportSDK.appearance.formsMenuText();
            entry.drawable = (Drawable) supportSDK.appearance.formsMenuIcon();
            entry.onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventManager.notify(EventManager.kEventMenuEnded, null);
                    form();
                }
            };
            mEntries.add(entry);
        }
        if ( supportSDK.historyEnabled ) {
            SupportMenuEntry entry = new SupportMenuEntry();
            entry.label = (String) supportSDK.appearance.historyMenuText();
            entry.drawable = (Drawable) supportSDK.appearance.historyMenuIcon();
            entry.onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventManager.notify(EventManager.kEventMenuEnded, null);
                    history();
                }
            };
            mEntries.add(entry);
        }
        if ( supportSDK.journeysEnabled && supportSDK.journeys.size()>0 ) {
            SupportMenuEntry entry = new SupportMenuEntry();
            entry.label = (String) supportSDK.appearance.journeysMenuText();
            entry.drawable = (Drawable) supportSDK.appearance.journeysMenuIcon();
            entry.onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventManager.notify(EventManager.kEventMenuEnded, null);
                    journeys();
                }
            };
            mEntries.add(entry);
        }
        if ( menuStyle == MenuStyle.ICON_LIST_EXIT ) {
            SupportMenuEntry entry = new SupportMenuEntry();
            entry.label = (String) supportSDK.appearance.exitMenuText();
            entry.drawable = (Drawable) supportSDK.appearance.exitMenuIcon();
            entry.onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventManager.notify(EventManager.kEventMenuEnded, null);
                    requestExit();
                }
            };
            mEntries.add(entry);
        }
    }


    public void showSupportDialog() {
        createSupportEntries();
        List<String> availableItems = new ArrayList<>();
        for ( SupportMenuEntry entry : mEntries ) {
            availableItems.add(entry.label);
        }
        availableItems.add(getResources().getString(R.string.label_cancel));

        CharSequence[] theItems = new CharSequence[availableItems.size()];
        for ( int n=0; n<availableItems.size(); n++ ) {
            theItems[n] = availableItems.get(n);
        }
        final CharSequence[] items = theItems;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(supportSDK.appearance.menuHeading());
        builder.setItems(items, (dialog, item) -> {
            dialog.dismiss();
            String label = (String) items[item];
            for ( SupportMenuEntry entry : mEntries ) {
                if ( label.equalsIgnoreCase(entry.label) ) {
                    View v = new View(mContext.get());
                    v.setOnClickListener(entry.onClickListener);
                    v.performClick();
                    break;
                }
            }
            if ( label.equalsIgnoreCase(getResources().getString(R.string.label_cancel)) ) {
                EventManager.notify(EventManager.kEventMenuEnded, null);
                requestExit();
            }
        });
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                EventManager.notify(EventManager.kEventMenuStarted, null);
                AlertDialog dialog = builder.show();
            }
        });

    }


    private void createSupportMenuView() {
        createSupportEntries();

        if ( mEntries.size() == 0 ) {
            Toast.makeText(mContext.get(), getResources().getString(R.string.warn_no_components_enabled), Toast.LENGTH_SHORT).show();
        }

        supportMenuView = new SupportMenuView(mContext.get(), mActivity.get(),  mEntries, menuStyle, showLoginPrompt);
        supportMenuView.mActivity = mActivity.get();
        supportMenuView.supportSDK = supportSDK;
        supportMenuView.refresh();
        supportMenuView.supportButton = this;
        supportMenuView.showLoginPrompt = showLoginPrompt;
        supportMenuView.dismissOnClick = dismissMenuOnClick;

        if (mListener != null) {
            supportMenuView.refresh();
            EventManager.notify(EventManager.kEventMenuStarted, null);
            mListener.supportButtonDisplayView(supportMenuView);
        }
    }

    private void removePreviousMenuIfNecessary() {
        if ( supportMenuView != null ) {
            ViewGroup parent = (ViewGroup)supportMenuView.getParent();
            if ( parent != null ) {
                mActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parent.removeView(supportMenuView);
                    }
                });
            }
        }
    }


    private SupportMenuButton supportMenuButton(WeakReference<Context> context, String label, int drawableId) {
        SupportMenuButton menuButton = new SupportMenuButton(context.get());
        menuButton.mLabelView.setText(label);
        Object button = menuButton.mImageButton;
        if ( button instanceof ImageButton ) {
            ImageButton imageButton = (ImageButton) button;
            imageButton.setImageDrawable(getResources().getDrawable(drawableId));
        } else {

        }
        menuButton.mImageButton.setContentDescription(label);
        return menuButton;
    }


    public void getCustomerInformation(HashMap<String, String> customerInfo) {
        supportSDK.restGetCustomerInformationWithInfo(customerInfo, null);
    }

    private void getCallbackNumber() {
        String uri = String.format("%s/customers/getCallbackNumber/%s", SupportSDK.SDK_V1_ENDPOINT, supportSDK.memberUserID);

        this.supportSDK.get(uri, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callMe(null);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                String callbackNumber = null;
                if (response.code() > 199 && response.code() < 300) {
                    try {
                        ResponseBody responseBody = response.body();
                        String responseBodyString = Objects.requireNonNull(responseBody).string();
                        JSONObject jsonObject = new JSONObject(responseBodyString);
                        callbackNumber = jsonObject.optString("callback_number");
                    } catch (JSONException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
                callMe(callbackNumber);
            }
        });
    }


    private void callMe(String callbackNumber) {
        final SupportButton button = this;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                CallMeView callMeView = new CallMeView(mContext.get(), callbackNumber);
                callMeView.supportButton = button;
                callMeView.supportSDK = supportSDK;
                callMeView.mActivity = mActivity.get();
                callMeView.show();
            }
        });
    }


    private void displayRatingScreen() {
        final SupportButton button = this;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                RatingView ratingView = new RatingView(mContext.get());
                ratingView.supportButton = button;
                ratingView.supportSDK = supportSDK;
                ratingView.mActivity = mActivity.get();
                ratingView.show();
            }
        });
    }


    private void visitWebsite() {
        Intent intent = new Intent(Intent.ACTION_VIEW, supportSDK.supportWebsiteURL);
        try {
            getContext().startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(mContext.get(), getResources().getString(R.string.warn_unable_to_visit_website), Toast.LENGTH_SHORT).show();
        }
    }


    private void sendEmail() {
        if (supportSDK.supportEmailAddress.isEmpty())
            return;

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{supportSDK.supportEmailAddress});

        //need this to prompts email client only
        emailIntent.setType("message/rfc822");

        getContext().startActivity(Intent.createChooser(emailIntent, "Select an Email Client:"));
    }


    private void phone() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + supportSDK.supportPhoneNumber));
        getContext().startActivity(intent);
    }


    private void form() {
        if ( supportSDK.forms.size() > 1 ) {
            FormListFragment formListFragment = new FormListFragment();
            formListFragment.mContext = getContext();
            formListFragment.supportSDK = supportSDK;
            formListFragment.supportButton = this;
            if (mListener != null) {
                mListener.supportButtonDisplayFragment(formListFragment, null);
                mListener.supportButtonSetTitle(getResources().getString(R.string.text_forms));
            }
        } else if ( supportSDK.forms.size() == 1 ) {
            formFragment = new SupportFormFragment();
            formFragment.mContext = getContext();
            formFragment.mFormModel = supportSDK.forms.get(0);
            formFragment.supportSDK = supportSDK;
            formFragment.mSupportButton = this;
            if (mListener != null) {
                mListener.supportButtonDisplayFragment(formFragment, null);
                mListener.supportButtonSetTitle(supportSDK.appearance.formsMenuText());
            }
        } else {
            supportSDK.getForms(this);
        }
    }


    public void removeForm() {
        if ( mListener != null ) {
            mListener.supportButtonDidCompleteTask();
        }

        if (mListener != null) {
            mListener.supportButtonRemoveFragment(formFragment);
        }
    }


    private void history() {
        if ( supportSDK.historyEntries.size() == 0 ) {
//            supportSDK.getHistory(this);
        }

        mHistoryListFragment = new HistoryListFragment();
        mHistoryListFragment.mContext = getContext();
        mHistoryListFragment.supportSDK = supportSDK;
        mHistoryListFragment.supportButton = this;
        if (mListener != null) {
            mListener.supportButtonDisplayFragment(mHistoryListFragment, supportSDK.appearance.historyMenuText());
        }
    }


    private void journeys() {
        if ( supportSDK.journeys.size() > 1 ) {
            JourneysFragment journeysFragment = new JourneysFragment();
            journeysFragment.mContext = getContext();
            journeysFragment.supportSDK = supportSDK;
            journeysFragment.supportButton = this;
            if (mListener != null) {
                mListener.supportButtonDisplayFragment(journeysFragment, null);
                mListener.supportButtonSetTitle(supportSDK.appearance.journeysMenuText());
            }
        } else if ( supportSDK.journeys.size() == 1 ) {
            JourneyModel model = supportSDK.journeys.get(0);
            String title = model.title;
            Intent intent = new Intent(getContext(), KBActivity.class);
            if ( !model.journey_url.isEmpty() ) {
                intent.putExtra(KBActivity.ARG_URL, model.journey_url);
                intent.putExtra(KBActivity.ARG_HTML, "");
            }
            intent.putExtra(KBActivity.ARG_TITLE, title);
            getContext().startActivity(intent);
        }
    }


    public void settings() {
        SettingsFragment fragment = new SettingsFragment();
        fragment.supportSDK = supportSDK;
        if (mListener != null) {
            mListener.supportButtonDisplayFragment(fragment, null);
            mListener.supportButtonSetTitle(getResources().getString(R.string.text_settings));
        }
     }


    private void requestExit() {
        if (mListener != null) {
            mListener.supportButtonDidRequestExit();
        }
    }


    public void removeHistory() {
        if (mListener != null) {
            mListener.supportButtonRemoveFragment(mHistoryListFragment);
        }
    }


    private void getOrCreateIssue() {
        supportSDK.showProgressWithMessage(mContext.get().getString(R.string.text_starting_chat));
        Issue currentIssue = Issue.getCurrentIssue(mContext.get());
        xmppdata = null;
        if ( currentIssue != null ) {
            xmppdata = currentIssue.xmpp_data;
            checkForActiveIssue(currentIssue.id);
        } else {
            createIssue();
        }
    }


    private void createIssue() {
        JSONObject params = new JSONObject();
        JSONObject issuesJSON = new JSONObject();
        try {
            issuesJSON.put("members_id", supportSDK.memberID);
            issuesJSON.put("members_users_id", supportSDK.memberUserID);
            issuesJSON.put("members_locations_id", supportSDK.memberLocationID);
            issuesJSON.put("user_agent", supportSDK.clientAppIdentifier()); // redundant - already in headers
            params.put("issues", issuesJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = String.format("%s/issues/create", SupportSDK.SDK_V1_ENDPOINT);

        supportSDK.post(uri, params, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                supportSDK.hideProgress();
                if (mListener != null) {
                    mListener.supportButtonDidFailWithError(mContext.get().getString(R.string.error_unable_to_create_issue), e.getLocalizedMessage());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean success = false;
                Issue issue = null;
                String message = "";

                supportSDK.hideProgress();
                JSONObject jsonObject;
                try {
                    ResponseBody responseBody = response.body();
                    String responseBodyString = Objects.requireNonNull(responseBody).string();
                    jsonObject = new JSONObject(responseBodyString);
                    success = jsonObject.optBoolean("success");
                    message = jsonObject.optString("message");
                    if (success) {
                        JSONArray results = jsonObject.optJSONArray("results");
                        if (results != null && results.length() > 0) {
                            Log.d(TAG, "onResponse");
                            JSONObject issueJSON;
                            try {
                                issueJSON = results.getJSONObject(0);
                                issue = new Issue(issueJSON);
                                success = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (issue != null) {
                                Issue.saveCurrentIssue(mContext.get(), issue);
                                displayChat(issue);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!success) {
                    if (mListener != null) {
                        mListener.supportButtonDidFailWithError(mContext.get().getString(R.string.error_unable_to_create_issue), message);
                    }
                }
            }
        });
    }


    private void checkForActiveIssue(String issueId) {

        String uri = String.format("%s/issues/%s", SupportSDK.SDK_V1_ENDPOINT, issueId);

        supportSDK.get(uri, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                createIssue();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean success = false;
                Issue issue = null;
                String message = "";

                supportSDK.hideProgress();
                JSONObject jsonObject;
                try {
                    ResponseBody responseBody = response.body();
                    String responseBodyString = Objects.requireNonNull(responseBody).string();
                    jsonObject = new JSONObject(responseBodyString);
                    success = response.code()==200 ? jsonObject.optBoolean("success") : false;
                    if (success) {
                        JSONArray results = jsonObject.optJSONArray("results");
                        if (results != null && results.length() > 0) {
                            Log.d(TAG, "onResponse");
                            JSONObject issueJSON;
                            try {
                                issueJSON = results.getJSONObject(0);
                                issue = new Issue(issueJSON);
                                supportSDK.historyEntries.add(issue);
                                success = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if ( issue != null ) {
                                if ( issue.status < Issue.RESOLVED ) {
                                    if ( issue.xmpp_data==null && xmppdata!=null ) {
                                        issue.xmpp_data = xmppdata;
                                    }
                                    displayChat(issue);
                                } else {
                                    createIssue();
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!success) {
                    createIssue();
                }
            }
        });
    }


    public void refreshIssue(String issueId) {

        String uri = String.format("%s/issues/%s", SupportSDK.SDK_V1_ENDPOINT, issueId);

        supportSDK.get(uri, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "refreshIssue onFailure");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean success;
                Issue issue = null;
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
                            Log.d(TAG, "onResponse");
                            JSONObject issueJSON;
                            try {
                                issueJSON = results.getJSONObject(0);
                                issue = new Issue(issueJSON);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if ( issue!=null && issue.status>= Issue.RESOLVED ) {
                                supportSDK.rateableIssueId = issue.id;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     *
     * Advertise the Support SDK service via mDNS
     *
     * @param publicData The contents of publicData will be available in the TXT record in clear text, using the key(s) provided in the map.
     * @param dataToEncrypt The contents of dataToEncrypt will be available as an encrypted string using the key(s) provided in the map.
     */
    public void advertiseServiceWithPublicData(Map<String, String> publicData, Map<String, String> dataToEncrypt) {
        mDNSSvcEnabled = true;
        // create NSD instance
        if (mDNSSvcListener == null) {
            mDNSSvcListener = new BTConnectPresenceService.ServiceAdvertisementListener() {
                @Override
                public void didAdvertiseService() {
                    if (mListener == null) {
                        return;
                    }
                    mListener.supportButtonDidAdvertiseService();
                }

                @Override
                public void didFailToAdvertiseService() {
                    if (mListener == null) {
                        return;
                    }
                    mListener.supportButtonDidFailToAdvertiseService();
                }
            };
        }
        mDNSSvc = BTConnectPresenceService.getInstance(mContext.get());
        mDNSSvc.supportSDK = supportSDK;
        mDNSSvc.addAdvertisementListener(mDNSSvcListener);
        if (publicData != null) {
            for (String k : publicData.keySet()) {
                mDNSSvc.addCustomPayloadData(k, publicData.get(k), false);
            }
        }
        if (dataToEncrypt != null) {
            for (String k : dataToEncrypt.keySet()) {
                mDNSSvc.addCustomPayloadData(k, dataToEncrypt.get(k), true);
            }
        }
        mDNSSvc.start();
    }

    /**
     * Stop advertising mDNS data.
     */
    public void stopAdvertiseServiceWithPublicData() {
        if (!mDNSSvcEnabled) {
            return;
        }
        mDNSSvc.tearDown();
        mDNSSvcEnabled = false;
    }

    private BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String intentAction = null;
            if (intent != null) {
                intentAction = intent.getAction();
            }
            Log.d(TAG, "msg received with intent: " + intentAction);

            if (EventManager.kSupportSDKEvent.equals(intentAction)) {

                String type = intent.getStringExtra(EventManager.kSupportSDKEventType);
                Log.d(TAG, "msg received with type: " + type );
//                event(intentAction, type, null);

            }
        }
    };

}
