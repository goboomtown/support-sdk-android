package com.goboomtown.supportsdk.fragment;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.model.KBEntryModel;
import com.goboomtown.supportsdk.view.SupportButton;

public class KBExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<Object>> listDataChild;
    public ExpandableListView expandableListView;
    public boolean  expanded;
    public SupportButton mSupportButton = null;
    public SupportSDK supportSDK = null;
    public List<Drawable> groupIcons = null;

    public interface KBExpandableListAdapterListener {
        void adapterDidSelectEntry(int groupPosition, int childPosition, Object object);
    }

    public KBExpandableListAdapterListener  mListener;


    public KBExpandableListAdapter(Context context,
                                   List<String> listDataHeader,
                                   HashMap<String, List<Object>> listChildData) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
    }


    public void updateData(List<String> listDataHeader,
                           HashMap<String, List<Object>> listChildData) {
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
        notifyDataSetChanged();
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        Object object = null;
        try {
            object = this.listDataChild.get(this.listDataHeader.get(groupPosition))
                    .get(childPosititon);
        } catch ( Exception e ) {

        }
        return object;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.fragment_article, parent, false);
        }
        ImageView iconView = convertView.findViewById(R.id.iconView);
        TextView articleLabel = convertView.findViewById(R.id.articleLabel);

        if ( expanded && expandableListView!=null ) {
            expandableListView.expandGroup(groupPosition);
        }

        iconView.setVisibility(View.GONE);
        if ( groupIcons != null ) {
            Drawable icon = groupIcons.get(groupPosition);
            if (icon != null) {
                iconView.setImageDrawable(icon);
                iconView.setVisibility(View.VISIBLE);
            }
        }

        Object child = getChild(groupPosition, childPosition);
        if ( child instanceof KBEntryModel ) {
            final KBEntryModel entry = (KBEntryModel) child;
            String childText = entry.title();
            articleLabel.setText(childText);
        }  else if ( child instanceof String ) {
            final String text = (String) child;
            articleLabel.setText(text);
        }
        convertView.setOnClickListener(v -> {
            if ( mListener != null ) {
                mListener.adapterDidSelectEntry(groupPosition, childPosition, child);
            }
        });

        return convertView;
    }

//    private void showFolder(KBEntryModel entry) {
//        KBListFragment kbListFragment = new KBListFragment();
//        kbListFragment.mContext = context;
//        kbListFragment.supportSDK = supportSDK;
//        kbListFragment.mSupportButton = mSupportButton;
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        ViewGroup viewGroup = (ViewGroup) mView.getParent();
//        int viewId = viewGroup.getId();
//        fragmentTransaction.replace(viewId,kbListFragment, KBLISTFRAGMENT_TAG);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
//    }

//    private void showArticle(KBEntryModel article) {
//        String title = article.title();
//        String body = article.body();
//        String html = "<html><body>" + body + "</body></html>";
//        Intent intent = new Intent(context, KBActivity.class);
//        intent.putExtra(KBActivity.ARG_URL, "");
//        intent.putExtra(KBActivity.ARG_HTML, html);
//        intent.putExtra(KBActivity.ARG_TITLE, title);
//        context.startActivity(intent);
//    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if ( convertView == null ) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.fragment_article_section_header, parent, false);
        }

        TextView sectionHeaderLabel = convertView.findViewById(R.id.sectionHeaderLabel);

        String title = (String) getGroup(groupPosition);
        sectionHeaderLabel.setText(title);

        if ( expanded && expandableListView!=null ) {
            expandableListView.expandGroup(groupPosition);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}