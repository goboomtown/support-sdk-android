package com.goboomtown.supportsdk.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.goboomtown.fragment.ChatAdapter;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.model.KBEntryModel;
import com.goboomtown.supportsdk.model.KBViewModel;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KBListAdapter
        extends RecyclerView.Adapter<KBListAdapter.ViewHolder>
{
    public static final String TAG = KBListAdapter.class.getSimpleName();

    private final Context       mContext;
    private final KBViewModel   mViewModel;
    public SupportSDK supportSDK = null;
    public List<Drawable> groupIcons = null;
    public KBListAdapterListener mListener;


    public interface KBListAdapterListener {
        void adapterDidSelectEntry(KBEntryModel entryModel);
    }


    public KBListAdapter(Context context, KBViewModel viewModel) {
        mContext = context;
        mViewModel = viewModel;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_kb_entry, parent, false);
        return new ViewHolder(view);
    }


    private void setIndicator(KBEntryModel entryModel, ViewHolder holder) {
        holder.mIndicator.setVisibility(View.GONE);
        holder.mViewindicator.setVisibility(View.GONE);
        if ( entryModel.isArticle() ) {
            holder.mViewindicator.setVisibility(View.VISIBLE);
        } else {
            if ( entryModel.children().size()>0 ) {
                int indicator = entryModel.isCollapsed() ? R.drawable.ic_expand_more_24px : R.drawable.ic_expand_less_24px;
                Drawable iconImage = mContext.getResources().getDrawable(indicator);
                holder.mIndicator.setImageDrawable(iconImage);
                holder.mIndicator.setVisibility(View.VISIBLE);
            }
        }
     }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.v(TAG, "binding viewholder at position=" + position + " with initial obj value=" + holder);
        holder.mPosition = position;
        KBEntryModel entryModel = mViewModel.visibleEntries.get(position);
        holder.mLabel.setText(entryModel.title());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(mViewModel.indentForEntry(entryModel)+20, 10, 10, 10);
        holder.mLabel.setLayoutParams(params);

        if ( entryModel.isArticle() ) {
            holder.mLabel.setTextColor(supportSDK.appearance.kbTextColor());
            holder.mViewindicator.setTextColor(supportSDK.appearance.kbTextColor());
         } else {
            holder.mLabel.setTextColor(supportSDK.appearance.kbFolderNameTextColor());
            holder.mLayout.setBackgroundColor(mContext.getResources().getColor(R.color.kbFolderL0BackgroundColor));
        }

        if ( supportSDK!=null && supportSDK.appearance!=null && supportSDK.appearance.kbFolderIcon != null ) {
            holder.mIcon.setImageDrawable(supportSDK.appearance.kbFolderIcon());
            holder.mIcon.setVisibility(View.VISIBLE);
        } else {
            holder.mIcon.setVisibility(View.GONE);
        }
        setIndicator(entryModel, holder);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( entryModel.isArticle() ) {
                    if (mListener != null) {
                        mListener.adapterDidSelectEntry(entryModel);
                    }
                } else {
                    entryModel.setCollapsed(entryModel.isCollapsed() ? false : true);
                    setIndicator(entryModel, holder);
                    mViewModel.updateVisibleEntries();
                    notifyDataSetChanged();
                }
            }
        });
        holder.setIsRecyclable(false);
    }


    @Override
    public int getItemCount() {
        return mViewModel.visibleEntries.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public  int                     mPosition;
        public  final View              mView;
        public  final RelativeLayout    mLayout;
        public  final ImageView         mIcon;
        public  final TextView          mLabel;
        public  final ImageView         mIndicator;
        public  final TextView          mViewindicator;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mLayout = view.findViewById(R.id.kbEntryLayout);
            mIcon = view.findViewById(R.id.kbEntryIcon);
            mLabel = view.findViewById(R.id.kbEntryLabel);
            mIndicator = view.findViewById(R.id.kbEntryIndicator);
            mViewindicator = view.findViewById(R.id.kbEntryViewIndicator);
        }

        @Override
        public String toString() {
            return super.toString();
        }

    }

}
