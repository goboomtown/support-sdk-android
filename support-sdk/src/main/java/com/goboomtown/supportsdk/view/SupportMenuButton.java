package com.goboomtown.supportsdk.view;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goboomtown.supportsdk.R;

import java.lang.ref.WeakReference;

/**
 * TODO: document your custom view class.
 */
public class SupportMenuButton extends LinearLayout {

//    public EditText         mLabelView;
    private Context mContext;
    public TextView mLabelView;
    public ImageButton      mImageButton;
//    public com.google.android.material.button.MaterialButton mImageButton;

    public OnClickListener  onClickListener;

    public SupportMenuButton(Context context) {
        super(context);
        setup(context);
    }


    public SupportMenuButton(Context context, String label, int drawableId) {
        super(context);
        setup(context);
        mLabelView.setText(label);
        Object button = mImageButton;
        if ( button instanceof ImageButton ) {
            ImageButton imageButton = (ImageButton) button;
            imageButton.setImageDrawable(getResources().getDrawable(drawableId));
        } else {

        }
        mImageButton.setContentDescription(label);
    }


    private void setup(Context context) {
        View view = inflate(context, R.layout.support_menu_button, this);

        mImageButton = view.findViewById(R.id.imageButton);
        mLabelView = view.findViewById(R.id.labelView);

    }


    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        mImageButton.setOnClickListener(onClickListener);
    }
}
