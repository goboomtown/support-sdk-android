package com.goboomtown.supportsdk.fragment;

import android.content.Context;
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

import com.goboomtown.forms.fragment.FormFragment;
import com.goboomtown.forms.model.FormModel;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.view.SupportButton;
import com.goboomtown.supportsdk.view.TrackSelectionAdapter;

import java.util.ArrayList;
import java.util.List;

public class FormListFragment extends Fragment {

    private static final String TAG = FormListFragment.class.getSimpleName();

    private FragmentActivity    mActivity;
    private View                mView;

    public  Context             mContext;
    public  SupportButton       supportButton;
    public  SupportSDK          supportSDK;

    public FormFragment.OnFragmentInteractionListener mListener;

    public FormListFragment() {
    }

    public static FormListFragment newInstance(int columnCount) {
        FormListFragment fragment = new FormListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
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
        mView = inflater.inflate(R.layout.fragment_form_list, container, false);

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


    private void form(FormModel formModel) {
        SupportFormFragment formFragment = new SupportFormFragment();
        formFragment.mContext = getContext();
        formFragment.mFormModel = formModel;
        formFragment.supportSDK = supportSDK;
        formFragment.mSupportButton = supportButton;
        formFragment.isFromList = true;
        try {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ViewGroup viewGroup = (ViewGroup) mView.getParent();
            int viewId = viewGroup.getId();
            fragmentTransaction.replace(viewId, formFragment, TAG);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            supportSDK.kbSubscreensOnStack++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void removeForm() {
//        if (mListener != null) {
//            mListener.supportButtonRemoveFragment(formFragment);
//        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(supportSDK.forms));
    }


    public class SimpleItemRecyclerViewAdapter
            extends TrackSelectionAdapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private int mSelectedPosition = RecyclerView.NO_POSITION;
        private List<FormModel> mForms = new ArrayList<>();
        private final ListItemClickListener mListClickListener;

        public SimpleItemRecyclerViewAdapter(List<FormModel> forms) {
            if (forms == null) {
                mForms = new ArrayList<>();
            } else {
                mForms = forms;
            }
            mListClickListener = new ListItemClickListener() {
                @Override
                public void onListItemClick(FormModel formModel) {
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
            FormModel formModel = mForms.get(position);
            holder.bind(formModel);
        }

        @Override
        public int getItemCount() {
            return mForms.size();
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
                mListClickListener.onListItemClick(mForms.get(position));
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

            public void bind(FormModel form) {
                if (form == null) {
                    return;
                }
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notifyItemChanged(mSelectedPosition);
                        mSelectedPosition = getLayoutPosition();
                        notifyItemChanged(mSelectedPosition);
                        form(mForms.get(mSelectedPosition));
                    }
                });
                mTextView.setText(form.name);
            }
        }
    }

    public interface ListItemClickListener {
        ListItemClickListener NULL_LISTENER = item -> {
        };

        void onListItemClick(FormModel entry);
    }


    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Uri var1);
    }


}
