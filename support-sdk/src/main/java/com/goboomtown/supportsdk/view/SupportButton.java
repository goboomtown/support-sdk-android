package com.goboomtown.supportsdk.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.Looper;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.goboomtown.forms.model.FormModel;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.activity.ScreenCaptureActivity;
import com.goboomtown.supportsdk.api.Appearance;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.dnssd.BTConnectPresenceService;
import com.goboomtown.supportsdk.fragment.ChatFragment;
import com.goboomtown.supportsdk.fragment.FormListFragment;
import com.goboomtown.supportsdk.fragment.HistoryListFragment;
import com.goboomtown.supportsdk.fragment.SupportFormFragment;
import com.goboomtown.supportsdk.fragment.KBListFragment;
import com.goboomtown.supportsdk.model.BTConnectIssue;
import com.goboomtown.supportsdk.model.HistoryEntryModel;
import com.goboomtown.supportsdk.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * TODO: document your custom view class.
 */
public class SupportButton extends View
        implements SupportSDK.SupportSDKListener, SupportSDK.SupportSDKFormsListener {

    private static final String TAG = SupportButton.class.getSimpleName();

    @Override
    public void supportSDKDidRetrieveForms() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext.get(), mContext.get().getResources().getString(R.string.text_forms_ready), Toast.LENGTH_SHORT).show();
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


    public enum MenuStyle {
        NO_MENU,
        MENU,
        BUTTON,
        ICON_LIST,
        ICON_GRID
    };


    // TODO: remove unused constants and/or rename with all-caps per Java convention for constants
    private static final String SupportSDKErrorDomain = "com.goboomtown.supportsdk";

    /**
     Customer information keys
     */
    public static final String kCustomerId                   = "members_id";
    public static final String kCustomerExternalId           = "members_external_id";
    public static final String kCustomerLocationId           = "members_locations_id";
    public static final String kCustomerLocationExternalId   = "members_locations_external_id";
    public static final String kCustomerLocationMid          = "members_locations_mid";
    public static final String kUserId                       = "members_users_id";
    public static final String kUserExternalId               = "members_users_external_id";
    public static final String kUserEmail                    = "members_users_email";
    public static final String kUserPhone                    = "members_users_phone";

    public  boolean     useSupportView;
    public  MenuStyle   menuStyle;
    public  boolean     showLoginPrompt;

    public  Appearance          appearance;
    private SupportSDK          supportSDK;
    private ChatFragment        chatFragment;
    private SupportFormFragment formFragment;
    private FormListFragment    mFormListFragment;
    private HistoryListFragment mHistoryListFragment;
    private KBListFragment      kbListFragment;
    public  ArrayList<SupportMenuEntry> mEntries = new ArrayList<>();

    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private WeakReference<Context> mContext;

    private BTConnectPresenceService mDNSSvc;
    private boolean mDNSSvcEnabled;
    private BTConnectPresenceService.ServiceAdvertisementListener mDNSSvcListener;
    private WeakReference<Activity> mActivity;
    private boolean clickedMethodRequeued;

    private PopupWindow mPopupWindow;

    private String  xmppdata;

    public interface SupportButtonListener {
        void supportButtonDidFailWithError(String description, String reason);

        void supportButtonDidGetSettings();

        void supportButtonDidFailToGetSettings();

        void supportButtonDisplayView(View view);

        void supportButtonDisplayFragment(Fragment fragment, String title);

        void supportButtonRemoveFragment(Fragment fragment);

        void supportButtonSetTitle(String title);

        void supportButtonDidAdvertiseService();

        void supportButtonDidFailToAdvertiseService();
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

    private void init(AttributeSet attrs, int defStyle) {
        mActivity = new WeakReference<>(Utils.getActivity(getContext()));
        if ( ScreenCaptureActivity.class.isAssignableFrom(mActivity.getClass()) &&
                Utils.doesObjectContainField(mActivity.get(), "supportSDK") ) {
            ((ScreenCaptureActivity) mActivity.get()).supportSDK = supportSDK;
        }

        supportSDK = new SupportSDK(getContext(),this);

        appearance = new Appearance(getContext());
        supportSDK.appearance = appearance;
//        supportSDK.getPermissions(mActivity.get());
        setOnClickListener(v -> clicked());

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SupportButton, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.SupportButton_exampleString);
        if (mExampleString == null)
            mExampleString = "";
        mExampleColor = a.getColor(
                R.styleable.SupportButton_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.SupportButton_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.SupportButton_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.SupportButton_exampleDrawable);
            Objects.requireNonNull(mExampleDrawable).setCallback(this);
        } else {
            mExampleDrawable = getResources().getDrawable(R.drawable.support_sdk_icon);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    /**
     * @param listener {@link SupportButtonListener} to set
     */
    @SuppressWarnings({"unused"})
    public void setListener(SupportButtonListener listener) {
        mListener = listener;
    }

    public void click() {
        switch(menuStyle) {
            case ICON_LIST:
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
//        This is for testing purposes
//        supportSDK.memberID = "WA3QMJ";
//        supportSDK.memberUserID = "WA3QMJ-5XK";
//        supportSDK.memberLocationID = "WA3QMJ-FYH";

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

    public void loadConfiguration(int configResourceId, HashMap<String, String> customerInfo) {
        supportSDK.loadConfiguration(configResourceId, customerInfo);
    }

    public void loadConfiguration(String jsonString, HashMap<String, String> customerInfo) {
        supportSDK.loadConfiguration(jsonString, customerInfo);
    }

    private void displayChat(BTConnectIssue issue) {
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
            String title = (String) appearance.menuConfiguration.get(Appearance.kMenuTextKnowledge);
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


    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the text.
        canvas.drawText(mExampleString,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);

        // Draw the example drawable on top of the text.
        if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
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
        setDefaultMenuConfiguration();
        if (this.supportSDK.memberID == null) {
            this.supportSDK.memberID = this.supportSDK.defaultMemberID;
        }

        if (this.supportSDK.memberUserID == null) {
            this.supportSDK.memberUserID = this.supportSDK.defaultMemberUserID;
        }

        if (this.supportSDK.memberLocationID == null) {
            this.supportSDK.memberLocationID = this.supportSDK.defaultMemberLocationID;
        }
        if (mListener != null) {
            mListener.supportButtonDidGetSettings();
        }
//        createSupportEntries();
    }

    public void supportSDKDidFailToGetSettings() {
        if (mListener != null) {
            mListener.supportButtonDidFailToGetSettings();
        }
    }

    private void setDefaultMenuConfiguration() {
        Hashtable<String, Object> configuration = new Hashtable<>();
        configuration.put(Appearance.kMenuTextChat, getResources().getString(R.string.label_chat_with_us));
        configuration.put(Appearance.kMenuIconChat, getResources().getDrawable(R.drawable.a_chat));

        configuration.put(Appearance.kMenuTextCallMe, supportSDK.callMeButtonText);
        configuration.put(Appearance.kMenuIconCallMe, getResources().getDrawable(R.drawable.phone_call));

        configuration.put(Appearance.kMenuTextKnowledge, getResources().getString(R.string.label_search_knowledge));
        configuration.put(Appearance.kMenuIconKnowledge, getResources().getDrawable(R.drawable.book_bookmark));

        configuration.put(Appearance.kMenuTextWeb, getResources().getString(R.string.label_web_support));
        configuration.put(Appearance.kMenuIconWeb, getResources().getDrawable(R.drawable.globe));

        configuration.put(Appearance.kMenuTextEmail, getResources().getString(R.string.label_email_support));
        configuration.put(Appearance.kMenuIconEmail, getResources().getDrawable(R.drawable.letter));

        configuration.put(Appearance.kMenuTextPhone, getResources().getString(R.string.label_phone_support));
        configuration.put(Appearance.kMenuIconPhone, getResources().getDrawable(R.drawable.phone));

        configuration.put(Appearance.kMenuTextForms, getResources().getString(R.string.label_forms));
        configuration.put(Appearance.kMenuIconForms, getResources().getDrawable(R.drawable.form));

        configuration.put(Appearance.kMenuTextHistory, getResources().getString(R.string.label_history));
        configuration.put(Appearance.kMenuIconHistory, getResources().getDrawable(R.drawable.customer_alt));

        configuration.put(Appearance.kMenuBorderColor, getResources().getColor(android.R.color.black));

        appearance.setMenuConfiguration(configuration);
    }


    private void createSupportEntries() {
        mEntries.clear();
        if ( supportSDK.memberID!=null && supportSDK.memberUserID!=null && supportSDK.memberLocationID!=null ) {
            SupportMenuEntry entry = new SupportMenuEntry();
            entry.label = (String) appearance.menuConfiguration.get(Appearance.kMenuTextChat);
            entry.drawable = (Drawable) appearance.menuConfiguration.get(Appearance.kMenuIconChat);
            entry.onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
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
        if ( supportSDK.showSupportCallMe ) {
            SupportMenuEntry entry = new SupportMenuEntry();
            entry.label = (String) appearance.menuConfiguration.get(Appearance.kMenuTextCallMe);
            entry.drawable = (Drawable) appearance.menuConfiguration.get(Appearance.kMenuIconCallMe);
            entry.onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getCallbackNumber();
                }
            };
            mEntries.add(entry);
        }
        if ( supportSDK.showKnowledgeBase ) {
            SupportMenuEntry entry = new SupportMenuEntry();
            entry.label = (String) appearance.menuConfiguration.get(Appearance.kMenuTextKnowledge);
            entry.drawable = (Drawable) appearance.menuConfiguration.get(Appearance.kMenuIconKnowledge);
            entry.onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayKnowledgeBase();
                }
            };
            mEntries.add(entry);
        }
        if ( supportSDK.supportWebsiteURL!=null && supportSDK.showSupportWebsite ) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, supportSDK.supportWebsiteURL);
            PackageManager packageManager = mContext.get().getPackageManager();
            List<ResolveInfo> list = packageManager.queryIntentActivities(webIntent, 0);
            if ( list.size() > 0 ) {
                SupportMenuEntry entry = new SupportMenuEntry();
                entry.label = (String) appearance.menuConfiguration.get(Appearance.kMenuTextWeb);
                entry.drawable = (Drawable) appearance.menuConfiguration.get(Appearance.kMenuIconWeb);
                entry.onClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        visitWebsite();
                    }
                };
                mEntries.add(entry);
            }
        }
        if ( supportSDK.supportEmailAddress!=null && supportSDK.showSupportEmail ) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            PackageManager packageManager = mContext.get().getPackageManager();
            List<ResolveInfo> list = packageManager.queryIntentActivities(emailIntent, 0);
            if ( list.size() > 0 ) {
                SupportMenuEntry entry = new SupportMenuEntry();
                entry.label = (String) appearance.menuConfiguration.get(Appearance.kMenuTextEmail);
                entry.drawable = (Drawable) appearance.menuConfiguration.get(Appearance.kMenuIconEmail);
                entry.onClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendEmail();
                    }
                };
                mEntries.add(entry);
            }
        }
        if ( supportSDK.supportPhoneNumber!=null && supportSDK.showSupportPhone ) {
            PackageManager packageManager = mContext.get().getPackageManager();
            if ( packageManager!=null && packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) ) {
                SupportMenuEntry entry = new SupportMenuEntry();
                entry.label = (String) appearance.menuConfiguration.get(Appearance.kMenuTextPhone);
                entry.drawable = (Drawable) appearance.menuConfiguration.get(Appearance.kMenuIconPhone);
                entry.onClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        phone();
                    }
                };
                mEntries.add(entry);
            }
        }
        if ( supportSDK.showSupportForms ) {
            SupportMenuEntry entry = new SupportMenuEntry();
            entry.label = (String) appearance.menuConfiguration.get(Appearance.kMenuTextForms);
            entry.drawable = (Drawable) appearance.menuConfiguration.get(Appearance.kMenuIconForms);
            entry.onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    form();
                }
            };
            mEntries.add(entry);
        }
        if ( supportSDK.showSupportHistory ) {
            SupportMenuEntry entry = new SupportMenuEntry();
            entry.label = (String) appearance.menuConfiguration.get(Appearance.kMenuTextHistory);
            entry.drawable = (Drawable) appearance.menuConfiguration.get(Appearance.kMenuIconHistory);
            entry.onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    history();
                }
            };
            mEntries.add(entry);
        }
    }


    private void showSupportDialog() {
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
        builder.setTitle("Select");
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
        });
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                AlertDialog dialog = builder.show();
            }
        });

    }


    private void createSupportMenuView() {
        createSupportEntries();
        SupportMenuView supportMenuView = new SupportMenuView(mContext.get(), mActivity.get(),  mEntries, menuStyle, showLoginPrompt);
        supportMenuView.mActivity = mActivity.get();
        supportMenuView.supportSDK = supportSDK;
        supportMenuView.supportButton = this;
        supportMenuView.showLoginPrompt = showLoginPrompt;

        if (mListener != null) {
            supportMenuView.refresh();
            mListener.supportButtonDisplayView(supportMenuView);
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
        String uri = String.format("%s/customers/getCallbackNumber/%s", SupportSDK.kSDKV1Endpoint, supportSDK.memberUserID);

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
        if ( supportSDK.forms.size() == 0 ) {
            supportSDK.getForms(this);
        }

        if ( supportSDK.forms.size() > 1 ) {
            mFormListFragment = new FormListFragment();
            mFormListFragment.mContext = getContext();
            mFormListFragment.supportSDK = supportSDK;
            mFormListFragment.supportButton = this;
            if (mListener != null) {
                mListener.supportButtonDisplayFragment(mFormListFragment, null);
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
                mListener.supportButtonSetTitle(getResources().getString(R.string.text_form));
            }
        } else {
            Toast.makeText(mContext.get(), getResources().getString(R.string.text_forms_unavailable), Toast.LENGTH_SHORT).show();
        }
    }


    public void removeForm() {
        if (mListener != null) {
            mListener.supportButtonRemoveFragment(formFragment);
        }
    }


    private void history() {
        if ( supportSDK.historyEntries.size() == 0 ) {
//            supportSDK.getHistory(this);
        }

//        if ( supportSDK.historyEntries.size() > 0 ) {
            mHistoryListFragment = new HistoryListFragment();
            mHistoryListFragment.mContext = getContext();
            mHistoryListFragment.supportSDK = supportSDK;
            mHistoryListFragment.supportButton = this;
            if (mListener != null) {
                mListener.supportButtonDisplayFragment(mHistoryListFragment, getResources().getString(R.string.text_site_history));
//                mListener.supportButtonSetTitle(getResources().getString(R.string.text_site_history));
            }
//        } else {
//            Toast.makeText(mContext.get(), getResources().getString(R.string.text_history_unavailable), Toast.LENGTH_SHORT).show();
//        }
    }


    public void removeHistory() {
        if (mListener != null) {
            mListener.supportButtonRemoveFragment(mHistoryListFragment);
        }
    }


    private void getOrCreateIssue() {
        BTConnectIssue currentIssue = BTConnectIssue.getCurrentIssue(mContext.get());
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
//            if ( callbackNumber != null ) {
//                params.put("callbackNumber", callbackNumber);
//            }
//            if ( description != null ) {
//                params.put("description", description);
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = String.format("%s/issues/create", SupportSDK.kSDKV1Endpoint);

        supportSDK.post(uri, params, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (mListener != null) {
                    mListener.supportButtonDidFailWithError(mContext.get().getString(R.string.error_unable_to_create_issue), e.getLocalizedMessage());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean success = false;
                BTConnectIssue issue = null;
                String message = "";

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
                                issue = new BTConnectIssue(issueJSON);
                                success = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (issue != null) {
                                BTConnectIssue.saveCurrentIssue(mContext.get(), issue);
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

        String uri = String.format("%s/issues/%s", SupportSDK.kSDKV1Endpoint, issueId);

        supportSDK.get(uri, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                if (mListener != null) {
//                    mListener.supportButtonDidFailWithError(mContext.get().getString(R.string.error_unable_to_retrieve_issue), null);
//                }
                createIssue();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean success = false;
                BTConnectIssue issue = null;
                String message = "";

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
                                issue = new BTConnectIssue(issueJSON);
                                supportSDK.historyEntries.add(issue);
                                success = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if ( issue != null ) {
                                if ( issue.status < BTConnectIssue.RESOLVED ) {
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
//                    if (mListener != null) {
//                        mListener.supportButtonDidFailWithError(mContext.get().getString(R.string.error_unable_to_retrieve_issue), null);
//                    }
                    createIssue();
                }
            }
        });
    }


    public void refreshIssue(String issueId) {

        String uri = String.format("%s/issues/%s", SupportSDK.kSDKV1Endpoint, issueId);

        supportSDK.get(uri, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                if (mListener != null) {
//                    mListener.supportButtonDidFailWithError(mContext.get().getString(R.string.error_unable_to_retrieve_issue), null);
//                }
                Log.d(TAG, "refreshIssue onFailure");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean success;
                BTConnectIssue issue = null;
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
                                issue = new BTConnectIssue(issueJSON);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if ( issue!=null && issue.status>=BTConnectIssue.RESOLVED ) {
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
}
