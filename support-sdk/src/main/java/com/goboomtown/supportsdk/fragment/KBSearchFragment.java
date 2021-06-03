package com.goboomtown.supportsdk.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.goboomtown.activity.KBActivity;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.model.KBEntryModel;
import com.goboomtown.supportsdk.model.KBViewModel;
import com.goboomtown.supportsdk.util.JSONHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link KBSearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link KBSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KBSearchFragment extends Fragment
    implements KBExpandableListAdapter.KBExpandableListAdapterListener, SupportSDK.SupportSDKKBListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String RECENT_SEARCHES_KEY = "com.goboomtown.supportsdk.recentsearches";

    public SupportSDK supportSDK = null;
    public  Context         mContext;
    private OnFragmentInteractionListener mListener;

    private ExpandableListView      expandableListView;
    private KBListAdapter           listAdapter;
    private KBExpandableListAdapter expandableListAdapter;
    private SearchView              searchView;

    private ArrayList<String> sectionHeadings   = new ArrayList<>();
    private ArrayList<Object> recentSearches    = new ArrayList<>();
    private ArrayList<Object> topResults        = new ArrayList<>();

    private String      query;
    private Activity    mActivity;

    public KBSearchFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment KBSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KBSearchFragment newInstance(String param1, String param2) {
        KBSearchFragment fragment = new KBSearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();

        sectionHeadings.add(getResources().getString(R.string.search_section_recent_searches));
        sectionHeadings.add(getResources().getString(R.string.search_section_top_results));

        recentSearches = getRecentSearches(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kbsearch, container, false);
        searchView = view.findViewById(R.id.searchView);
        final KBSearchFragment thisFragment = this;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                saveQuery(query);
                supportSDK.searchKB(thisFragment, query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //    adapter.getFilter().filter(newText);
                return false;
            }
        });
        setupAdapter();
        return view;
    }

    private void saveQuery(String query) {
        this.query = query;
    }

    private HashMap<String, List<Object>> createEntries() {
        HashMap<String, List<Object>> entries = new HashMap<>();
        entries.put(sectionHeadings.get(0), recentSearches);
        entries.put(sectionHeadings.get(1), topResults);
        return entries;
    }

    private void setupAdapter() {
        final KBSearchFragment listener = this;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                expandableListAdapter = new KBExpandableListAdapter(mActivity, sectionHeadings, createEntries());
                ArrayList<Drawable> groupIcons = new ArrayList<>();
                groupIcons.add(getResources().getDrawable(R.drawable.ic_search_24px));
                groupIcons.add(null);
                expandableListAdapter.groupIcons = groupIcons;
                if ( expandableListView!=null ) {
                    expandableListAdapter.mListener = listener;
                    expandableListAdapter.expanded = true;
                    expandableListView.setAdapter(expandableListAdapter);
                    for ( int n=0; n<expandableListAdapter.getGroupCount(); n++ ) {
                        expandableListView.expandGroup(n);
                    }
                }
            }
        });
    }


    @Override
    public void adapterDidSelectEntry(int groupPosition, int childPosition, Object object) {
        if ( object instanceof KBEntryModel) {
            KBEntryModel entry = (KBEntryModel) object;
            if ( entry != null ) {
                retrieveKB(entry.id());
            }
        } else if ( object instanceof String) {
            String entry = (String) object;
            if ( groupPosition == 0 ) {
                searchView.setQuery(entry, false);
            }
        }
    }


    public void retrieveKB(String id) {
        String url = SupportSDK.kSDKV1Endpoint + "/kb/get?id=" + id;

        supportSDK.get(url, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                boolean success = false;

                JSONObject jsonObject = SupportSDK.successJSONObject(response);
                if ( jsonObject != null ) {
                    if ( jsonObject.has("results") ) {
                        try {
                            JSONArray resultsJSON = jsonObject.getJSONArray("results");
                            JSONObject result = resultsJSON.getJSONObject(0);
                            KBEntryModel entry = new KBEntryModel(result);
                            if ( entry != null ) {
                                success = true;
                                showArticle(entry);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if ( !success ) {
                    warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
                }
            }
        });
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


    private void addRecentSearch(String search) {
        if ( !recentSearches.contains(search) ) {
            recentSearches.add(search);
            saveRecentSearches(getActivity());
            setupAdapter();
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    expandableListAdapter.updateData(sectionHeadings, createEntries());
                }
            });
        }
    }


    private void saveRecentSearches(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
         try {
            editor.putString(RECENT_SEARCHES_KEY, JSONHelper.toJSON(recentSearches).toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private ArrayList<Object> getRecentSearches(Context context) {
        ArrayList<Object> recents = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String jsonString = prefs.getString(RECENT_SEARCHES_KEY,null);
        if ( jsonString!=null && !jsonString.isEmpty() ) {
            try {
                JSONArray json = new JSONArray(jsonString);
                List<Object> list = JSONHelper.toList(json);
                if ( list instanceof ArrayList ) {
                    recents.addAll(list);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return recents;
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void supportSDKDidRetrieveKB(KBViewModel kbViewModel) {

    }

    @Override
    public void supportSDKDidSearchKB(KBViewModel kbViewModel) {
        if ( kbViewModel!=null && kbViewModel.entries.size()>0 ) {
            topResults.clear();
            topResults.addAll(kbViewModel.entries);
            if ( mActivity != null ) {
                addRecentSearch(query);
                setupAdapter();
            }
        } else {
            warn(getString(R.string.label_search_knowledge), getString(R.string.error_no_results));
        }
    }

    @Override
    public void supportSDKDidFailToSearchKB() {
        warn(getString(R.string.label_search_knowledge), getString(R.string.error_no_results));
    }


    public void warn(final String title, final String message) {
        Activity activity = getActivity();
        if ( activity != null ) {
            activity.runOnUiThread(() -> Toast.makeText(getContext(), title + ": " + message, Toast.LENGTH_LONG).show());
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
