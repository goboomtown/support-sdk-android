package com.goboomtown.supportsdk.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.Appearance;

import java.lang.ref.WeakReference;

/**
 * TODO: document your custom view class.
 */
public class SupportMenuButton extends LinearLayout {

//    public EditText         mLabelView;
    private Context     mContext;
    public TextView     mLabelView;
    public ImageButton  mImageButton;
    public Appearance   appearance;
//    public com.google.android.material.button.MaterialButton mImageButton;

    public OnClickListener  onClickListener;

    public SupportMenuButton(Context context) {
        super(context);
        setup(context);
    }


    public SupportMenuButton(Context context, String label, Drawable drawable) {
        super(context);
        setup(context);
        mLabelView.setText(label);
        Object button = mImageButton;
        if ( button instanceof ImageButton ) {
            ImageButton imageButton = (ImageButton) button;
            imageButton.setImageDrawable(drawable);
        } else {

        }
        mImageButton.setContentDescription(label);
    }


    public void configureAppearance() {
        mImageButton.setColorFilter(appearance.menuIconColor());
        mLabelView.setTextColor(appearance.menuTextColor());

    }


    private void setup(Context context) {
        View view = inflate(context, R.layout.support_menu_button, this);

        mImageButton = view.findViewById(R.id.imageButton);
        mLabelView = view.findViewById(R.id.labelView);
    }


    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        mImageButton.setOnClickListener(onClickListener);
        mLabelView.setOnClickListener(onClickListener);
    }
}
