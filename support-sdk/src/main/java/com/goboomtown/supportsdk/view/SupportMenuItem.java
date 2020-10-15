package com.goboomtown.supportsdk.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.Appearance;

public class SupportMenuItem extends LinearLayout {

    //    public EditText         mLabelView;
    private Context mContext;
    public Appearance appearance;
    public TextView mLabelView;
    public ImageView mImageView;
//    public com.google.android.material.button.MaterialButton mImageButton;

    public View.OnClickListener onClickListener;

    public SupportMenuItem(Context context) {
        super(context);
        setup(context);
    }


    public SupportMenuItem(Context context, String label, int drawableId) {
        super(context);
        setup(context);
        mLabelView.setText(label);
        mImageView.setImageDrawable(getResources().getDrawable(drawableId));

        mImageView.setColorFilter(appearance.homeIconColor);
        mLabelView.setTextColor(appearance.homeTextColor);
    }


    private void setup(Context context) {
        View view = inflate(context, R.layout.support_menu_item, this);

        mImageView = view.findViewById(R.id.iconView);
        mLabelView = view.findViewById(R.id.label);
    }


    @Override
    public void setOnClickListener(View.OnClickListener onClickListener) {
        mImageView.setOnClickListener(onClickListener);
    }

}
