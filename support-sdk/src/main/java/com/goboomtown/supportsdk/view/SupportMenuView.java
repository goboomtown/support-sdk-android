package com.goboomtown.supportsdk.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.wefika.flowlayout.FlowLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public SupportButton            supportButton;
    public SupportSDK               supportSDK;
    public Activity                 mActivity;
    private Context                 mContext;
    private LinearLayout            mEmailEntryView;
    private LinearLayout            mMenuView;
    private EditText                mEmailEditText;
    private TextView                infoLabel;
    private GridView                mGridView;
    private RecyclerView            mRecyclerView;
    private SupportButton.MenuStyle mMenuStyle;
    public  boolean                 showLoginPrompt;
    private SupportMenuView         mSupportMenuView = null;
    public  boolean                 dismissOnClick = false;
    private ArrayList<View> mButtons = new ArrayList<>();
    private ArrayList<SupportMenuEntry> mEntries = new ArrayList<>();


    public SupportMenuView(Context context, Activity activity, ArrayList<SupportMenuEntry> entries, SupportButton.MenuStyle menuStyle, boolean showLoginPrompt) {
        super(context);
        mContext = context;
        mActivity = activity;
        mEntries = entries;
        mMenuStyle = menuStyle;
        this.showLoginPrompt = showLoginPrompt;

        mSupportMenuView = this;

        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        View view = inflate(mContext, R.layout.support_menu_view, this);

        mEmailEntryView = view.findViewById(R.id.emailEntryView);
        mMenuView       = view.findViewById(R.id.menuView);
        infoLabel       = view.findViewById(R.id.infoLabel);

        mEmailEditText = view.findViewById(R.id.emailEditText);
        Button submitButton = view.findViewById(R.id.submitButton);
        submitButton.setBackgroundColor(getResources().getColor(R.color.loginButtonColor));
        submitButton.setOnClickListener(v -> {
            String emailAddress = mEmailEditText.getText().toString();
            if ( !emailAddress.isEmpty() ) {
                getCustomerInfo(emailAddress);
            } else {
                showMenu();
            }
        });

        mGridView = view.findViewById(R.id.gridView);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mMenuView.setVisibility(View.GONE);
    }


    public void refresh() {
        if ( showLoginPrompt ) {
            mEmailEntryView.setVisibility(View.VISIBLE);
            mMenuView.setVisibility(View.GONE);
        } else {
            mEmailEntryView.setVisibility(View.GONE);
            mMenuView.setVisibility(View.VISIBLE);
        }

        showInfoLabelIfNecessary();

        switch ( mMenuStyle ) {
            case ICON_LIST:
            case ICON_LIST_EXIT:
                mGridView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                setupRecyclerView(mRecyclerView);
                break;
            case BUTTON:
            case ICON_GRID:
            default:
                mButtons.clear();
                for ( SupportMenuEntry entry : mEntries ) {
                    if ( mMenuStyle == SupportButton.MenuStyle.BUTTON ) {
                        SupportMenuButton menuButton = new SupportMenuButton(mContext,
                                entry.label, entry.drawable);
                        menuButton.appearance = supportSDK.appearance;
                        menuButton.configureAppearance();
                        menuButton.setOnClickListener(entry.onClickListener);
                        mButtons.add(menuButton);
                    } else {
                        SupportMenuItem menuButton = new SupportMenuItem(mContext,
                                entry.label, entry.drawable);
                        menuButton.appearance = supportSDK.appearance;
                        menuButton.configureAppearance();
                        menuButton.setOnClickListener(entry.onClickListener);
                        mButtons.add(menuButton);
                    }
                }
                mGridView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                CustomGridAdapter gridAdapter = new CustomGridAdapter(mContext, mButtons);
                mGridView.setAdapter(gridAdapter);
                gridAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void showInfoLabelIfNecessary() {
        if ( !supportSDK.isProduction() ) {
            infoLabel.setText(supportSDK.getHost());
            infoLabel.setTextColor(supportSDK.appearance.textColor());
            infoLabel.setVisibility(View.VISIBLE);
        } else {
            infoLabel.setVisibility(View.GONE);
        }
    }

    private void showMenu() {
        showInfoLabelIfNecessary();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mEmailEntryView.setVisibility(View.GONE);
                mMenuView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void dismissSupportMenuView() {
        if ( !dismissOnClick ) {
            return;
        }
        if ( mSupportMenuView != null ) {
            ViewGroup parent = (ViewGroup) mSupportMenuView.getParent();
            if ( parent != null ) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        parent.removeView(mSupportMenuView);
                    }
                });

            }
        }
    }

    private void getCustomerInfo(String emailAddress) {
        HashMap<String, String> customerInfo = new HashMap<>();
        customerInfo.put(SupportButton.KEY_CUSTOMER_USER_EMAIL, emailAddress);
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
                        supportSDK.historyEntries.clear();
                        supportSDK.saveMemberInfo();
                        if ( supportSDK.historyEnabled ) {
                            supportSDK.getHistory();
                        }
                    } catch (JSONException e) {
                        Log.w(TAG, Log.getStackTraceString(e));
                    } catch (Exception e) {
                        Log.w(TAG, Log.getStackTraceString(e));
                    }
                }
            }
        });
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
//        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
//        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, 0));
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(mEntries));
    }


    public class SimpleItemRecyclerViewAdapter
            extends TrackSelectionAdapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private int mSelectedPosition = RecyclerView.NO_POSITION;
        private List<SupportMenuEntry> mEntries = new ArrayList<>();
        private final ListItemClickListener mListClickListener;

        public SimpleItemRecyclerViewAdapter(List<SupportMenuEntry> entries) {
            if (entries == null) {
                mEntries = new ArrayList<>();
            } else {
                mEntries = entries;
            }
            mListClickListener = new ListItemClickListener() {
                @Override
                public void onListItemClick(SupportMenuEntry entry) {
                    dismissSupportMenuView();
                    View v = new View(mContext);
                    v.setOnClickListener(entry.onClickListener);
                    v.performClick();
                }

            };
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.support_menu_item, parent, false);
            final ViewHolder result = new ViewHolder(view);
            View.OnClickListener clickListener = v -> {
                v.setBackgroundColor(getResources().getColor(R.color.homeSelectedColor));
                trySelectFocusedItem(result.getAdapterPosition());
            };
            view.setBackgroundColor(supportSDK.appearance.menuBackgroundColor());
            view.setOnClickListener(clickListener);
            return result;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int backgroundColor = supportSDK.appearance.menuBackgroundColor();
            holder.mView.setBackgroundColor(backgroundColor);
            holder.itemView.setSelected(mSelectedPosition == position);
            if ( holder.layout != null ) {
                holder.layout.setBackgroundColor(backgroundColor);
            }

            SupportMenuEntry entry = mEntries.get(position);
            holder.bind(entry);
        }

        @Override
        public int getItemCount() {
            return mEntries.size();
        }

        @Override
        protected boolean trySelectFocusedItem(int position) {
            Log.v(TAG, "select item via KB at pos=" + position);
            if (position < 0) {
                Log.w(TAG, "invalid recyclerview item position: " + position);
                return true;
            }
            if (mSelectedPosition != position) {
                // don't reload chatroom on every invocation
                int oldPos = mSelectedPosition;
                mSelectedPosition = position;
                notifyItemChanged(position);
                notifyItemChanged(oldPos);
                mListClickListener.onListItemClick(mEntries.get(position));
            }
            return true;
        }

        public class ViewHolder extends TrackSelectionAdapter.ViewHolder {
            public final View           mView;
            public final LinearLayout   layout;
            public final ImageView      mIconView;
            public final TextView       mTextView;

            public ViewHolder(View view) {
                super(view);
                mView     = view;
                int backgroundColor = supportSDK.appearance.menuBackgroundColor();

                layout    = view.findViewById(R.id.layout);
                if ( layout != null ) {
                    layout.setBackgroundColor(backgroundColor);
                }

                mIconView = view.findViewById(R.id.iconView);
                mTextView = view.findViewById(R.id.label);
            }

            public void bind(SupportMenuEntry entry) {
                if (entry == null) {
                    return;
                }

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT
                );
                params.setMargins(supportSDK.appearance.menuPadding(),
                        supportSDK.appearance.menuPadding(),
                        supportSDK.appearance.menuPadding(),
                        supportSDK.appearance.menuPadding());
                itemView.setLayoutParams(params);

                int backgroundColor = supportSDK.appearance.menuBackgroundColor();
                layout.setBackgroundColor(backgroundColor);
                if ( supportSDK.appearance.hasMenuBorder() ) {
                    addBorder(itemView, (int)supportSDK.appearance.menuBorderColor());
                }

                mTextView.setPadding(supportSDK.appearance.menuSpacing()+10, 10, 10, 10);

                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addBorder(itemView, supportSDK.appearance.menuBorderColor());
                        notifyItemChanged(mSelectedPosition);
                        mSelectedPosition = getLayoutPosition();
                        notifyItemChanged(mSelectedPosition);
                        dismissSupportMenuView();
                        v.setOnClickListener(entry.onClickListener);
                        v.performClick();
                    }
                });
//                mItemView.setOnClickListener(entry.onClickListener);
                mIconView.setImageDrawable(entry.drawable);
                mIconView.setColorFilter(supportSDK.appearance.menuIconColor());
//                mIconView.setBackgroundColor(supportSDK.appearance.menuBackgroundColor());
                mTextView.setText(entry.label);
                mTextView.setTextColor(supportSDK.appearance.menuTextColor());
//                mTextView.setBackgroundColor(supportSDK.appearance.menuBackgroundColor());
                mTextView.setTextSize(supportSDK.appearance.menuTextSize());
            }

            private void addBorder(View view, int borderColor) {
                GradientDrawable border = new GradientDrawable();
                border.setCornerRadius(8);
                border.setColor(supportSDK.appearance.menuBackgroundColor());
                border.setStroke(supportSDK.appearance.menuBorderWidth(), borderColor);
                view.setBackground(border);
            }
        }
    }

    public interface ListItemClickListener {
        ListItemClickListener NULL_LISTENER = item -> {
        };

        void onListItemClick(SupportMenuEntry entry);
    }


}
