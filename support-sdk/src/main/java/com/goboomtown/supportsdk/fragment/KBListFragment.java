package com.goboomtown.supportsdk.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goboomtown.activity.KBActivity;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.EventManager;
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
    implements SupportSDK.SupportSDKKBListener,
        KBListAdapter.KBListAdapterListener {

    public static final String KBLISTFRAGMENT_TAG   = "com.goboomtown.supportsdk.kblistfragment2";
    public static final String KBSEARCHFRAGMENT_TAG = "com.goboomtown.supportsdk.kbsearchfragment";

    private static final String ARG_COLUMN_COUNT = "column-count";
    public  SupportButton   mSupportButton = null;
    public  SupportSDK      supportSDK = null;
    public  Context         mContext;
    private KBViewModel             kbViewModel = null;
    private RecyclerView            recyclerView;
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
    }


    private void refreshModel() {
        if ( kbViewModel == null ) {
            if ( supportSDK == null ) {
                return;
            }
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

        recyclerView = view.findViewById(R.id.recyclerView);

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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                KBListAdapter adapter = new KBListAdapter(mContext, kbViewModel);
                adapter.supportSDK = supportSDK;
                adapter.mListener = listener;
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
     }

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

    @Override
    public void adapterDidSelectEntry(KBEntryModel entry) {
        showArticle(entry);
    }


    private void backToHome() {
        for ( int n=0; n<supportSDK.kbSubscreensOnStack; n++ ) {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            fragmentManager.popBackStackImmediate();
        }
        supportSDK.kbSubscreensOnStack = 0;
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
        EventManager.notify(EventManager.kEventKnowledgeStarted, null);
        refreshModel();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventManager.notify(EventManager.kEventKnowledgeEnded, null);
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
