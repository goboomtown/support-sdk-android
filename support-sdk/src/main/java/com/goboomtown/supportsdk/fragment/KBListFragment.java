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
import android.widget.ExpandableListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.goboomtown.activity.KBActivity;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.model.KBEntryModel;
import com.goboomtown.supportsdk.model.KBViewModel;
import com.goboomtown.supportsdk.view.SupportButton;


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
    public  SupportButton   mSupportButton = null;
    public  SupportSDK      supportSDK = null;
    public  Context         mContext;
    private OnListFragmentInteractionListener mListener;
    private KBViewModel             kbViewModel = null;
    private ExpandableListView      expandableListView;
    private KBExpandableListAdapter expandableListAdapter;
    private View                    mView;
    private FragmentActivity        mActivity;

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

        setHasOptionsMenu(true);

        mActivity = getActivity();
        if ( kbViewModel == null ) {
            if ( supportSDK.kbViewModel() != null ) {
                kbViewModel = supportSDK.kbViewModel();
            } else {
                if ( supportSDK.isKBRequested )  {
                    supportSDK.mKBListener = this;
                } else {
                    supportSDK.isKBRequested = true;
                    supportSDK.getKB(this);
                }
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_list, container, false);

        expandableListView = view.findViewById(R.id.expandableListView);

        if ( kbViewModel != null ) {
            setupAdapter(kbViewModel);
        }

        mView = view;
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_kblist, menu);
        MenuItem home = menu.findItem(R.id.action_home);
        home.setVisible(supportSDK.kbSubscreensOnStack>0);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            showSearchFragment();
            return true;
        }
        else if (id == R.id.action_home) {
            backToHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setupAdapter(KBViewModel kbViewModel) {
        final KBListFragment listener = this;
        if (mActivity != null) {
            mActivity.runOnUiThread(() -> {
                expandableListAdapter = new KBExpandableListAdapter(mActivity, kbViewModel.folderHeadings(), kbViewModel.allEntriesByFolderName());
                if ( expandableListView!=null && expandableListAdapter!=null ) {
                    expandableListAdapter.mListener = listener;
                    expandableListAdapter.supportSDK = supportSDK;
                    expandableListAdapter.expandableListView = expandableListView;
                    expandableListView.setAdapter(expandableListAdapter);
                    expandableListAdapter.notifyDataSetChanged();
                    for(int i=0; i < expandableListAdapter.getGroupCount(); i++) {
                        expandableListView.expandGroup(i);
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


    private void backToHome() {
        for ( int n=0; n<supportSDK.kbSubscreensOnStack; n++ ) {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            fragmentManager.popBackStackImmediate();
        }
        supportSDK.kbSubscreensOnStack = 0;
//        if ( mActivity instanceof AppCompatActivity ) {
//            AppCompatActivity activity = (AppCompatActivity) mActivity;
//            ActionBar actionBar = activity.getSupportActionBar();
//            if ( actionBar != null ) {
//                actionBar.hide();
//            }
//        }
    }

    private void showFolder(KBEntryModel entry) {
        KBViewModel kbViewModel = new KBViewModel(entry);
        KBListFragment kbListFragment = new KBListFragment(kbViewModel);
        kbListFragment.mContext = mContext;
        kbListFragment.supportSDK = supportSDK;
        kbListFragment.mSupportButton = mSupportButton;
        try {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ViewGroup viewGroup = (ViewGroup) mView.getParent();
            int viewId = viewGroup.getId();
            fragmentTransaction.replace(viewId, kbListFragment, KBLISTFRAGMENT_TAG);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            supportSDK.kbSubscreensOnStack++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showArticle(KBEntryModel article) {
        String title = article.title();
        String body = article.body();
        String html = "<html><body>" + body + "</body></html>";
        Intent intent = new Intent(mContext, KBActivity.class);
        if ( !article.url().isEmpty() ) {
            intent.putExtra(KBActivity.ARG_URL, article.url());
            intent.putExtra(KBActivity.ARG_HTML, "");
        } else {
            intent.putExtra(KBActivity.ARG_URL, "");
            intent.putExtra(KBActivity.ARG_HTML, html);
        }
        intent.putExtra(KBActivity.ARG_TITLE, title);
        startActivity(intent);
    }


    public void showSearchFragment() {
        KBSearchFragment kbSearchFragment = new KBSearchFragment();
        kbSearchFragment.mContext = mContext;
        kbSearchFragment.supportSDK = supportSDK;
        try {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ViewGroup viewGroup = (ViewGroup) mView.getParent();
            int viewId = viewGroup.getId();
            fragmentTransaction.replace(viewId, kbSearchFragment, KBSEARCHFRAGMENT_TAG);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
