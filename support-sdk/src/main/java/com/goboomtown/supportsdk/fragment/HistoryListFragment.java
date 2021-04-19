package com.goboomtown.supportsdk.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goboomtown.forms.model.FormModel;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.EventManager;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.model.BTConnectIssue;
import com.goboomtown.supportsdk.view.RatingView;
import com.goboomtown.supportsdk.view.SupportButton;
import com.goboomtown.supportsdk.view.TrackSelectionAdapter;
import com.goboomtown.widget.WebImageView;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class HistoryListFragment extends Fragment
    implements SupportSDK.SupportSDKHistoryListener {

    private static final String TAG = HistoryListFragment.class.getSimpleName();

    private static final String kActionSettingSortDescending    = "com.goboomtownn.history.sort_descending";
    private static final String kActionSettingShowOpen          = "com.goboomtownn.history.show_open";
    private static final String kActionSettingShowResolved      = "com.goboomtownn.history.show_resolved";
    private static final String kActionSettingShowClosed        = "com.goboomtownn.history.show_closed";

    private FragmentActivity    mActivity;
    private SupportFormFragment mFormFragment;
    private RecyclerView        mRecyclerView;
    private View                mView;

    public  Context             mContext;
    public  SupportButton       supportButton;
    public  SupportSDK          supportSDK;
    private MenuItem            mMenuSelect;

    private boolean             sortDescending;
    private boolean             showOpen;
    private boolean             showResolved;
    private boolean             showClosed;

    public ArrayList<BTConnectIssue>    filteredEntries = new ArrayList<>();
    public ArrayList<BTConnectIssue>    sortedEntries = new ArrayList<>();

    public OnFragmentInteractionListener mListener;


    public HistoryListFragment() {
    }

    public static HistoryListFragment newInstance(int columnCount) {
        HistoryListFragment fragment = new HistoryListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportSDK.mHistoryListener = this;

        setHasOptionsMenu(true);
        mActivity = getActivity();
    }


    @Override
    public void supportSDKDidRetrieveHistory() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_history_list, container, false);

        loadActionSettings();
        sort();
        mRecyclerView = mView.findViewById(R.id.recyclerView);
        setupRecyclerView(mRecyclerView);
        return mView;
    }


    private void backToHome() {
        for ( int n=0; n<supportSDK.kbSubscreensOnStack; n++ ) {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            fragmentManager.popBackStackImmediate();
        }
    }


    private void showEntry(BTConnectIssue issue) {
        if ( issue.isResolved() && !issue.isRated ) {
            displayRatingScreen(issue);
        } else {
            displayChat(issue);
        }
    }


    private void displayChat(BTConnectIssue issue) {
        if ( (issue.xmpp_data==null && issue.comm_id==null) || issue.transcripts==null ) {
            Activity activity = getActivity();
            if ( activity != null ) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.warn_no_chat_history), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return;
        }

        ChatFragment chatFragment = new ChatFragment();
        chatFragment.mContext = getContext();
        chatFragment.supportSDK = supportSDK;
        chatFragment.mIssue = issue;
        chatFragment.chatTitle = getString(R.string.text_site_history);
        chatFragment.mSupportButton = supportButton;
        chatFragment.isReadOnly = !issue.isOpen();
        try {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ViewGroup viewGroup = (ViewGroup) mView.getParent();
            int viewId = viewGroup.getId();
            fragmentTransaction.replace(viewId, chatFragment, TAG);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayRatingScreen(BTConnectIssue issue) {
        supportSDK.rateableIssueId = issue.id;
        final SupportButton button = supportButton;
        if ( mActivity != null ) {
            mActivity.runOnUiThread(() -> {
                RatingView ratingView = new RatingView(mContext);
                ratingView.supportButton = button;
                ratingView.supportSDK = supportSDK;
                ratingView.mActivity = mActivity;
                ratingView.show();
            });
        }
    }


    public void removeEntry() {
//        if (mListener != null) {
//            mListener.supportButtonRemoveFragment(formFragment);
//        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        EventManager.notify(EventManager.kEventHistoryStarted, null);
        if ( supportSDK.isRetrievingHistory ) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    supportSDK.showProgressWithMessage(getString(R.string.text_retrieving));
                }
            });
        }
    }

    @Override
    public void onDetach() {
        EventManager.notify(EventManager.kEventHistoryEnded, null);
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_history, menu);
        mMenuSelect = menu.findItem(R.id.action_select);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_select) {
            View menuItemView = mActivity.findViewById(R.id.action_select);
            showMenu(menuItemView);
        }
        return super.onOptionsItemSelected(item);
    }


    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(mActivity, v);
        popup.inflate(R.menu.menu_history_popup);
        popup.getMenu().getItem(0).setChecked(sortDescending);
        popup.getMenu().getItem(1).setChecked(showOpen);
        popup.getMenu().getItem(2).setChecked(showResolved);
        popup.getMenu().getItem(3).setChecked(showClosed);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
//                int id = item.getItemId();
//                if        ( id == R.id.action_sort_descending ) {
//                    popup.show();
//                    return true;
//                } else if ( id == R.id.action_show_open ) {
//                    return true;
//                } else if ( id == R.id.action_show_resolved ) {
//                    return true;
//                } else if ( id == R.id.action_show_closed ) {
//                    return true;
//                }
//                return false;

                item.setChecked(!item.isChecked());

                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(mContext));
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return false;
                    }
                });
                saveActionSettings();
                int id = item.getItemId();
                if ( id == R.id.action_sort_descending ) {
                    sortDescending = item.isChecked();
                } else if ( id == R.id.action_show_open ) {
                    showOpen = item.isChecked();
                } else if ( id == R.id.action_show_resolved ) {
                    showResolved = item.isChecked();
                } else if ( id == R.id.action_show_closed ) {
                    showClosed = item.isChecked();
                }
                sort();
                mRecyclerView.getAdapter().notifyDataSetChanged();
                return false;

            }
        });
        popup.show();
    }

    private void sort() {
        filteredEntries.clear();
        for ( BTConnectIssue entry : supportSDK.historyEntries ) {
            if ( entry .status<BTConnectIssue.RESOLVED && showOpen ) {
                filteredEntries.add(entry);
            }
            else if ( entry.status==BTConnectIssue.RESOLVED && showResolved ) {
                filteredEntries.add(entry);
            }
            else if ( entry.status>BTConnectIssue.RESOLVED && showClosed ) {
                filteredEntries.add(entry);
            }
        }
        Collections.sort(filteredEntries, new OptionComparator());
    }


    public class OptionComparator implements Comparator<BTConnectIssue> {
        public int compare(BTConnectIssue left, BTConnectIssue right) {
            Date leftDate = dateFromString(left.created, "yyyy-MM-dd HH:mm:ss");
            Date rightDate = dateFromString(right.created, "yyyy-MM-dd HH:mm:ss");
            return sortDescending ? rightDate.compareTo(leftDate) : leftDate.compareTo(rightDate);
        }
    }


    private void saveActionSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(kActionSettingSortDescending, sortDescending);
        editor.putBoolean(kActionSettingShowOpen, showOpen);
        editor.putBoolean(kActionSettingShowResolved, showResolved);
        editor.putBoolean(kActionSettingShowClosed, showClosed);
        editor.apply();     // This line is IMPORTANT !!!
    }


    private void loadActionSettings() {
        BTConnectIssue issue = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        sortDescending = prefs.getBoolean(kActionSettingSortDescending, true);
        showOpen = prefs.getBoolean(kActionSettingShowOpen, true);
        showResolved = prefs.getBoolean(kActionSettingShowResolved, true);
        showClosed = prefs.getBoolean(kActionSettingShowClosed, true);
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
//        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(supportSDK.historyEntries));
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(filteredEntries));
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // At this point the layout is complete and the
                    // dimensions of recyclerView and any child views
                    // are known.
                    supportSDK.hideProgress();
                    recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        );
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Object item);
    }

    public class SimpleItemRecyclerViewAdapter
            extends TrackSelectionAdapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private int mSelectedPosition = RecyclerView.NO_POSITION;
        public  List<BTConnectIssue> mEntries = new ArrayList<>();
        private final ListItemClickListener mListClickListener;

        public SimpleItemRecyclerViewAdapter(List<BTConnectIssue> entries) {
            if (entries == null) {
                mEntries = new ArrayList<>();
            } else {
                mEntries = entries;
            }
            mListClickListener = new ListItemClickListener() {
                @Override
                public void onListItemClick(BTConnectIssue model) {
                    View v = new View(mContext);
//                    v.setOnClickListener(formModel.onClickListener);
//                    v.performClick();

                }

            };
        }



        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_list_item, parent, false);
            final ViewHolder result = new ViewHolder(view);
            View.OnClickListener clickListener = v -> {
                v.setBackgroundColor(getResources().getColor(R.color.homeSelectedColor));
                trySelectFocusedItem(result.getAdapterPosition());
            };
            view.setOnClickListener(clickListener);
            return result;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            // super.onBindViewHolder(holder, position);
            if (mSelectedPosition == position) {
                holder.itemView.setBackgroundColor(Color.parseColor("#993ED4CA"));
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
            BTConnectIssue entryModel = mEntries.get(position);
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    form(mForms.get(position));
//                }
//            });
            holder.bind(entryModel);
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

        public String displayStatus(BTConnectIssue issue) {
            if ( issue.isClosed() ) {
                return getString(R.string.text_closed);
            } else if ( issue.isResolved() ) {
                return getString(R.string.text_resolved);
            }
            return getString(R.string.text_open);
        }


        public class ViewHolder extends TrackSelectionAdapter.ViewHolder {
            public WebImageView avatarView = null;
            public TextView     refIdLabel = null;
            public TextView     dateLabel = null;
            public TextView     lastMessageLabel = null;
            public TextView     statusLabel = null;
            public TextView     ratingLabel = null;

            public ViewHolder(View view) {
                super(view);
                avatarView = view.findViewById(R.id.avatarView);
                refIdLabel = view.findViewById(R.id.refIdLabel);
                dateLabel = view.findViewById(R.id.dateLabel);
                lastMessageLabel = view.findViewById(R.id.lastMessageLabel);
                statusLabel = view.findViewById(R.id.statusLabel);
                ratingLabel = view.findViewById(R.id.ratingLabel);
                if ( refIdLabel != null ) {
                    refIdLabel.setTextColor(supportSDK.appearance.textColor());
                }
                if ( dateLabel != null ) {
                    dateLabel.setTextColor(supportSDK.appearance.textColor());
                }
                if ( lastMessageLabel != null ) {
                    lastMessageLabel.setTextColor(supportSDK.appearance.textColor());
                }
                if ( statusLabel != null ) {
                    statusLabel.setTextColor(supportSDK.appearance.textColor());
                }
                if ( ratingLabel != null ) {
                    ratingLabel.setTextColor(supportSDK.appearance.textColor());
                }
            }


            public void bind(BTConnectIssue entry) {
                if (entry == null) {
                     return;
                }
                String displayDate = entry.isOpen() ? entry.created : entry.resolved;
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date date = format.parse(displayDate);
                    format = new SimpleDateFormat("MMMM dd/yyyy");
                    displayDate = format.format(date);
                    System.out.println("Date ->" + date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                refIdLabel.setText(getString(R.string.text_chat_id) + ": " + entry.reference_num);
                dateLabel.setText(displayDate);
                statusLabel.setText(getString(R.string.text_status) + ": " + displayStatus(entry));
                if ( !entry.isOpen() && entry.isRated ) {
                    ratingLabel.setText(getString(R.string.text_rating) + ": " + entry.rating);
                } else {
                    ratingLabel.setText("");
                }
                if ( avatarView != null ) {
                    avatarView.setPlaceholderImage(R.drawable.user_placeholder_round);
                    if (entry.owner_user_avatar != null) {
                        avatarView.setImageUrl(entry.owner_user_avatar);
                    }
                }
                if ( entry.isOpen() && entry.transcripts != null ) {
                    Iterator<String> keys = entry.transcripts.keys();
                    String key = keys.next();
                    JSONObject transcripts = entry.transcripts.optJSONObject(key);
                    String highKey = null;
                    Date highDate = null;
                    if ( transcripts != null ) {
                        keys = transcripts.keys();
                        while ( keys.hasNext() ) {
                            key = keys.next();
                            if ( highKey == null ) {
                                highKey = key;
                                highDate = dateFromString(highKey);
                            }
                            Date thisDate = dateFromString(key);
                            if (thisDate.after(highDate)) {
                                highKey = key;
                                highDate = thisDate;
                            }
                        }
                        JSONObject transcript = transcripts.optJSONObject(highKey);
                        if ( transcript != null ) {
                            String from = transcript.optString("from");
                            String message = transcript.optString("message");
                            if ( from!=null && message!=null ) {
                                lastMessageLabel.setText(from + ": " + message);
                            }
                        }
                    }
                } else {
                    lastMessageLabel.setText("");
                }

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notifyItemChanged(mSelectedPosition);
                        mSelectedPosition = getLayoutPosition();
                        notifyItemChanged(mSelectedPosition);
//                        v.setOnClickListener(form.onClickListener);
//                        v.performClick();
                        BTConnectIssue issue = mEntries.get(mSelectedPosition);
                        showEntry(issue);
                    }
                });
//                mItemView.setOnClickListener(entry.onClickListener);
//                mIconView.setImageDrawable(getResources().getDrawable(entry.resourceId));
//                mTextView.setText(entry.name);
            }
        }
    }


    private Date dateFromString(String dateString, String format) {
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            date = sdf.parse(dateString);
        } catch (ParseException ex) {
        }
        return date;
    }


    private Date dateFromString(String dateString) {
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            date = sdf.parse(dateString);
        } catch (ParseException ex) {
        }
        return date;
    }


    public interface ListItemClickListener {
        ListItemClickListener NULL_LISTENER = item -> {
        };

        void onListItemClick(BTConnectIssue entry);
    }


    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Uri var1);
    }


}
