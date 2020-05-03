package com.goboomtown.supportsdk.view;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goboomtown.supportsdk.R;

/**
 * TODO: document your custom view class.
 */
public class SupportMenuButton extends LinearLayout {

//    public EditText         mLabelView;
    public TextView mLabelView;
    public ImageButton      mImageButton;
//    public com.google.android.material.button.MaterialButton mImageButton;

    public OnClickListener  onClickListener;

    public SupportMenuButton(Context context) {
        super(context);

        View view = inflate(context, R.layout.support_menu_button, this);

        mImageButton = view.findViewById(R.id.imageButton);
        mLabelView = view.findViewById(R.id.labelView);
    }


    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        mImageButton.setOnClickListener(onClickListener);
    }
}
