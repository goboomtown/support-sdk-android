package com.goboomtown.supportsdk.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.goboomtown.activity.KBActivity;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.model.KBEntryModel;
import com.goboomtown.supportsdk.model.KBViewModel;
import com.goboomtown.supportsdk.view.SupportButton;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class KBListFragment extends Fragment
    implements SupportSDK.SupportSDKKBListener, KBExpandableListAdapter.KBExpandableListAdapterListener {

    public static final String KBLISTFRAGMENT_TAG   = "com.goboomtown.supportsdk.kblistfragment2";
    public static final String KBSEARCHFRAGMENT_TAG = "com.goboomtown.supportsdk.kbsearchfragment";

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int             mColumnCount = 1;
    public  SupportButton   mSupportButton = null;
    public  SupportSDK      supportSDK = null;
    public  Context         mContext;
    private OnListFragmentInteractionListener mListener;
    private KBViewModel kbViewModel = null;
    private ExpandableListView  expandableListView;
    private KBExpandableListAdapter expandableListAdapter;
    private View            mView;

    public KBListFragment() {
    }


    public KBListFragment(KBViewModel kbViewModel) {
        this.kbViewModel = kbViewModel;
    }

    public static KBListFragment newInstance(int columnCount) {
        KBListFragment fragment = new KBListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_list, container, false);

        expandableListView = (ExpandableListView) view.findViewById(R.id.expandableListView);

        if ( kbViewModel == null ) {
            if ( supportSDK.kbViewModel() != null ) {
                kbViewModel = supportSDK.kbViewModel();
            } else {
                supportSDK.getKB(this);
            }
        }
        if ( kbViewModel != null ) {
            setupAdapter(kbViewModel);
        }

        mView = view;
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_kblist, menu);
     }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            showSearchFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setupAdapter(KBViewModel kbViewModel) {
        final KBListFragment listener = this;
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    expandableListAdapter = new KBExpandableListAdapter(activity, kbViewModel.folderHeadings(), kbViewModel.allEntriesByFolderName());
                    if ( expandableListView!=null && expandableListAdapter!=null ) {
                        expandableListAdapter.mListener = listener;
                        expandableListAdapter.expandableListView = expandableListView;
                        expandableListView.setAdapter(expandableListAdapter);
                        expandableListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void adapterDidSelectEntry(int groupPosition, int childPosition, Object object) {
        if ( object instanceof KBEntryModel ) {
            KBEntryModel entry = (KBEntryModel) object;
            if (entry.isFolder()) {
                showFolder(entry);
            } else {
                showArticle(entry);
            }
        }
    }

    private void showFolder(KBEntryModel entry) {
        KBViewModel kbViewModel = new KBViewModel(entry);
        KBListFragment kbListFragment = new KBListFragment(kbViewModel);
        kbListFragment.mContext = mContext;
        kbListFragment.supportSDK = supportSDK;
        kbListFragment.mSupportButton = mSupportButton;
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ViewGroup viewGroup = (ViewGroup) mView.getParent();
        int viewId = viewGroup.getId();
        fragmentTransaction.replace(viewId, kbListFragment, KBLISTFRAGMENT_TAG);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    private void showArticle(KBEntryModel article) {
        String title = article.title();
        String body = article.body();
        String html = "<html><body>" + body + "</body></html>";
        Intent intent = new Intent(mContext, KBActivity.class);
        intent.putExtra(KBActivity.ARG_URL, "");
        intent.putExtra(KBActivity.ARG_HTML, html);
        intent.putExtra(KBActivity.ARG_TITLE, title);
        mContext.startActivity(intent);
    }


    public void showSearchFragment() {
        KBSearchFragment kbSearchFragment = new KBSearchFragment();
        kbSearchFragment.mContext = mContext;
        kbSearchFragment.supportSDK = supportSDK;
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ViewGroup viewGroup = (ViewGroup) mView.getParent();
        int viewId = viewGroup.getId();
        fragmentTransaction.replace(viewId, kbSearchFragment, KBSEARCHFRAGMENT_TAG);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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


    @Override
    public void supportSDKDidRetrieveKB(KBViewModel kbViewModel) {
        this.kbViewModel = kbViewModel;
        setupAdapter(this.kbViewModel);
    }


    @Override
    public void supportSDKDidSearchKB(KBViewModel kbViewModel) {

    }

    @Override
    public void supportSDKDidFailToSearchKB() {

    }



    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Uri var1);
    }


}
