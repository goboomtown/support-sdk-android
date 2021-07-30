package com.goboomtown.supportsdk.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goboomtown.activity.KBActivity;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.model.JourneyModel;
import com.goboomtown.supportsdk.model.KBEntryModel;
import com.goboomtown.supportsdk.view.SupportButton;
import com.goboomtown.supportsdk.view.TrackSelectionAdapter;

import java.util.ArrayList;
import java.util.List;

public class JourneysFragment extends Fragment {
    private static final String TAG = JourneysFragment.class.getSimpleName();

    private FragmentActivity mActivity;
    private View mView;

    public Context mContext;
    public SupportButton supportButton;
    public SupportSDK supportSDK;

//    public FormFragment.OnFragmentInteractionListener mListener;

    public JourneysFragment() {
    }

    public static JourneysFragment newInstance(int columnCount) {
        JourneysFragment fragment = new JourneysFragment();
        Bundle args = new Bundle();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        mActivity = getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_journeys, container, false);

        RecyclerView recyclerView = mView.findViewById(R.id.recyclerView);
        setupRecyclerView(recyclerView);
        return mView;
    }


    private void backToHome() {
        for ( int n=0; n<supportSDK.kbSubscreensOnStack; n++ ) {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            fragmentManager.popBackStackImmediate();
        }
    }


    private void journey(JourneyModel model) {
        String title = model.title;
        Intent intent = new Intent(mContext, KBActivity.class);
        if ( !model.journey_url.isEmpty() ) {
            intent.putExtra(KBActivity.ARG_URL, model.journey_url);
            intent.putExtra(KBActivity.ARG_HTML, "");
        }
        intent.putExtra(KBActivity.ARG_TITLE, title);
        startActivity(intent);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(supportSDK.journeys));
    }


    public class SimpleItemRecyclerViewAdapter
            extends TrackSelectionAdapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private int mSelectedPosition = RecyclerView.NO_POSITION;
        private List<JourneyModel> mJourneys = new ArrayList<>();
        private final ListItemClickListener mListClickListener;

        public SimpleItemRecyclerViewAdapter(List<JourneyModel> journeys) {
            if (journeys == null) {
                mJourneys = new ArrayList<>();
            } else {
                mJourneys = journeys;
            }
            mListClickListener = new ListItemClickListener() {
                @Override
                public void onListItemClick(JourneyModel model) {
                    View v = new View(mContext);
                }

            };
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.form_list_item, parent, false);
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
            if (mSelectedPosition == position) {
                holder.itemView.setBackgroundColor(Color.parseColor("#993ED4CA"));
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
            JourneyModel journeyModel = mJourneys.get(position);
            holder.bind(journeyModel);
        }

        @Override
        public int getItemCount() {
            return mJourneys.size();
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
                mListClickListener.onListItemClick(mJourneys.get(position));
            }
            return true;
        }

        public class ViewHolder extends TrackSelectionAdapter.ViewHolder {
            public final ImageView mIconView;
            public final TextView mTextView;

            public ViewHolder(View view) {
                super(view);
                mIconView = view.findViewById(R.id.iconView);
                mTextView = view.findViewById(R.id.label);
                if ( mTextView != null ) {
                    mTextView.setTextColor(supportSDK.appearance.textColor());
                }
            }

            public void bind(JourneyModel journey) {
                if (journey == null) {
                    return;
                }
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notifyItemChanged(mSelectedPosition);
                        mSelectedPosition = getLayoutPosition();
                        notifyItemChanged(mSelectedPosition);
                        journey(mJourneys.get(mSelectedPosition));
                    }
                });
                mTextView.setText(journey.title);
            }
        }
    }

    public interface ListItemClickListener {
        ListItemClickListener NULL_LISTENER = item -> {
        };

        void onListItemClick(JourneyModel journeyModel);
    }


    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Uri var1);
    }


}

