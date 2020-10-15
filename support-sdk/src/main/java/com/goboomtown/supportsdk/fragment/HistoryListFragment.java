package com.goboomtown.supportsdk.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.goboomtown.supportsdk.R;
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class HistoryListFragment extends Fragment
    implements SupportSDK.SupportSDKHistoryListener {

    private static final String TAG = HistoryListFragment.class.getSimpleName();

    private FragmentActivity    mActivity;
    private SupportFormFragment mFormFragment;
    private RecyclerView        mRecyclerView;
    private View                mView;

    public  Context             mContext;
    public  SupportButton       supportButton;
    public  SupportSDK          supportSDK;

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
            if ( issue.xmpp_data == null ) {
                Activity activity = getActivity();
                if ( activity != null ) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.warn_unable_to_obtain_chat_server_information), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                displayChat(issue);
            }
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
//        if (context instanceof OnListFragmentInteractionListener) {
//            mListener = (OnListFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnListFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(supportSDK.historyEntries));
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

//        @Override
//        public void onBindViewHolder(@NonNull SimpleItemRecyclerViewAdapter.ViewHolder holder, int position) {
//            if (mSelectedPosition == position) {
//                holder.itemView.setBackgroundColor(getResources().getColor(R.color.homeSelectedColor));
//            } else {
//                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
//            }
//            holder.itemView.setSelected(mSelectedPosition == position);
//            FormModel form = mForms.get(position);
//            holder.bind(form);
//        }

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
                    refIdLabel.setTextColor(supportSDK.appearance.homeTextColor);
                }
                if ( dateLabel != null ) {
                    dateLabel.setTextColor(supportSDK.appearance.homeTextColor);
                }
                if ( lastMessageLabel != null ) {
                    lastMessageLabel.setTextColor(supportSDK.appearance.homeTextColor);
                }
                if ( statusLabel != null ) {
                    statusLabel.setTextColor(supportSDK.appearance.homeTextColor);
                }
                if ( ratingLabel != null ) {
                    ratingLabel.setTextColor(supportSDK.appearance.homeTextColor);
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
                    if ( transcripts != null ) {
                        keys = transcripts.keys();
                        key = keys.next();
                        JSONObject transcript = transcripts.optJSONObject(key);
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

    public interface ListItemClickListener {
        ListItemClickListener NULL_LISTENER = item -> {
        };

        void onListItemClick(BTConnectIssue entry);
    }


    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Uri var1);
    }


}
