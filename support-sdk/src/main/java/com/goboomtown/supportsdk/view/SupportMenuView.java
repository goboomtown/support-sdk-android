package com.goboomtown.supportsdk.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.DynamicDrawableSpan;
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

import com.goboomtown.chat.BoomtownChat;
import com.goboomtown.chat.BoomtownChatMessage;
import com.goboomtown.fragment.ChatAdapter;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.wefika.flowlayout.FlowLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.ref.WeakReference;
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

    public SupportButton    supportButton;
    public SupportSDK       supportSDK;
    public Activity         mActivity;

    private Context         mContext;

    private LinearLayout        mEmailEntryView;
    private LinearLayout        mMenuView;
    private EditText            mEmailEditText;
    private GridView            mGridView;
    private FlowLayout          mFlowView;
    private RecyclerView        mRecyclerView;
    private SupportButton.MenuStyle     mMenuStyle;
    public  boolean             showLoginPrompt;
    private ArrayList<View> mButtons = new ArrayList<>();
    private ArrayList<SupportMenuEntry> mEntries = new ArrayList<>();


    public SupportMenuView(Context context, Activity activity, ArrayList<SupportMenuEntry> entries, SupportButton.MenuStyle menuStyle, boolean showLoginPrompt) {
        super(context);
        mContext = context;
        mActivity = activity;
        mEntries = entries;
        mMenuStyle = menuStyle;
        this.showLoginPrompt = showLoginPrompt;

        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        View view = inflate(mContext, R.layout.support_menu_view, this);

        mEmailEntryView = view.findViewById(R.id.emailEntryView);
        mMenuView       = view.findViewById(R.id.menuView);

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
//        mGridView.setOnItemClickListener((parent, view1, position, id) -> {
////            SupportMenuButton button = mButtons.get(position);
//        });

        mFlowView = view.findViewById(R.id.flowView);

        mRecyclerView = view.findViewById(R.id.recyclerView);

        mMenuView.setVisibility(View.GONE);

        refresh();
    }


    public void refresh() {
        if ( showLoginPrompt ) {
            mEmailEntryView.setVisibility(View.VISIBLE);
            mMenuView.setVisibility(View.GONE);
        } else {
            mEmailEntryView.setVisibility(View.GONE);
            mMenuView.setVisibility(View.VISIBLE);
        }

        switch ( mMenuStyle ) {
            case ICON_LIST:
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
                                entry.label, entry.resourceId);
                        menuButton.appearance = supportSDK.appearance;
                        menuButton.setOnClickListener(entry.onClickListener);
                        mButtons.add(menuButton);
                    } else {
                        SupportMenuItem menuButton = new SupportMenuItem(mContext,
                                entry.label, entry.resourceId);
                        menuButton.appearance = supportSDK.appearance;
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


//    protected void createFlowMenu() {
//        // set container visibility
//        if ( mFlowView == null ) {
//            return;
//        }
//        mFlowView.setVisibility(View.VISIBLE);
//        // add new buttons
//        for (int i = 0; i < mEntries.size(); i++) {
//            try {
//                JSONObject action = actions.getJSONObject(i);
//                Spannable btnLabel = new SpannableString(action.optString(BoomtownChatMessage.JSON_KEY_LBL));
//                final String btnUri = action.optString(BoomtownChatMessage.JSON_KEY_URI);
//                if (btnLabel != null && btnUri != null) {
//                    final Button btn = new Button(context.get());
//                    FlowLayout.LayoutParams llp = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    llp.setMargins(5, 5, 5, 5);
//                    btn.setLayoutParams(llp);
//                    btn.setAllCaps(false);  // override default styling else lose spannable data via #AllCapsTransformationMethod
//                    btnLabel = BoomtownChatMessage.processTxtWithEmoticons(btnLabel, emoticonMap, new BoomtownChatMessage.EmoticonSpannableBuilderStrategy() {
//                        @Override
//                        public DynamicDrawableSpan buildDynamicDrawableSpan(BoomtownChatMessage.Emoticon emot) {
//                            return BoomtownChatMessage.buildDynamicDrawableSpan(context.get(), btn, emot, btn.getLineHeight() * 2, btn.getLineHeight() * 2);
//                        }
//                    });
//                    btn.setPadding(30, 0, 30, 0);
//                    btn.setText(btnLabel);
//                    btn.setBackground(this.context.get().getResources().getDrawable(com.goboomtown.boomtownchat.R.drawable.rounded_button_selector));
//                    mFlowView.addView(btn);
//                }
//            } catch (JSONException e) {
//                Log.e(TAG, Log.getStackTraceString(e));
//            }
//        }
//    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
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
            view.setOnClickListener(clickListener);
            return result;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (mSelectedPosition == position) {
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.homeSelectedColor));
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
            holder.itemView.setSelected(mSelectedPosition == position);
            SupportMenuEntry entry = mEntries.get(position);
            holder.bind(entry);
        }

//        @Override
//        public void onBindViewHolder(final ViewHolder holder, int position) {
//            // super.onBindViewHolder(holder, position);
//            if (mSelectedPosition == position) {
//                holder.itemView.setBackgroundColor(Color.parseColor("#993ED4CA"));
//            } else {
//                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
//            }
//            SupportMenuEntry entry = mEntries.get(position);
//            holder.bind(entry);
//        }

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
            public final ImageView      mIconView;
            public final TextView       mTextView;

            public ViewHolder(View view) {
                super(view);
                mIconView = view.findViewById(R.id.iconView);
                mTextView = view.findViewById(R.id.label);
            }

            public void bind(SupportMenuEntry entry) {
                if (entry == null) {
                    return;
                }
                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notifyItemChanged(mSelectedPosition);
                        mSelectedPosition = getLayoutPosition();
                        notifyItemChanged(mSelectedPosition);
                        v.setOnClickListener(entry.onClickListener);
                        v.performClick();
                    }
                });
//                mItemView.setOnClickListener(entry.onClickListener);
                mIconView.setImageDrawable(getResources().getDrawable(entry.resourceId));
                mIconView.setColorFilter(supportSDK.appearance.homeIconColor);
                mTextView.setText(entry.label);
                mTextView.setTextColor(supportSDK.appearance.homeTextColor);
            }
        }
    }

    public interface ListItemClickListener {
        ListItemClickListener NULL_LISTENER = item -> {
        };

        void onListItemClick(SupportMenuEntry entry);
    }


}
