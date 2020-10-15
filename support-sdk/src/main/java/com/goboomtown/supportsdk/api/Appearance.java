package com.goboomtown.supportsdk.api;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

public class Appearance {
    public  Context     mContext;
    private Resources   mResources;
    private String      mPackageName;

    public Drawable logo;
    public int    loginBackgroundColor;
    public int    loginFieldBorderColor;
    public int    loginFieldBackgroundColor;
    public int    loginFieldTextColor;
    public int    loginButtonColor;

    public int    homeIconColor;
    public int    homeLineColor;
    public int    homeTextColor;
    public int    homeSelectedColor;

    public int    callMeHeaderTextColor;
    public int    callMeLabelTextColor;
    public int    callMeHintTextColor;
    public int    callMeButtonTextColor;
    public int    callMeButtonBackgroundColor;

    public int    ratingHeaderTextColor;
    public int    ratingLabelTextColor;
    public int    ratingHintTextColor;
    public int    ratingButtonTextColor;
    public int    ratingButtonBackgroundColor;

    public int    chatRefidTextColor;
    public int    chatNavBarColor;
    public int    chatSendButtonEnabledColor;
    public int    chatSendButtonDisabledColor;
    public int    chatTimeStampColor;
    public int    chatActionButtonTextColor;
    public int    chatActionButtonSelectedTextColor;
    public int    chatActionButtonBorderColor;
    public int    chatIconColor;

    public int    kbFolderNameTextColor;
    public int    kbFolderL0BackgroundColor;
    public int    kbTextColor;

    public Appearance(Context context) {
        mContext = context;
        mResources = mContext.getResources();
        mPackageName = mContext.getPackageName();

        loginBackgroundColor        = getColor("loginBackgroundColor");
        loginFieldBorderColor       = getColor("loginFieldBorderColor");
        loginFieldBackgroundColor   = getColor("loginFieldBackgroundColor");
        loginFieldTextColor         = getColor("loginFieldTextColor");
        loginButtonColor            = getColor("loginButtonColor");

        homeIconColor       = getColor("homeIconColor");
        homeLineColor       = getColor("homeLineColor");
        homeTextColor       = getColor("homeTextColor");
        homeSelectedColor   = getColor("homeSelectedColor");

        callMeHeaderTextColor   = getColor("callMeHeaderTextColor");
        callMeLabelTextColor   = getColor("callMeLabelTextColor");
        callMeHintTextColor   = getColor("callMeHintTextColor");
        callMeButtonTextColor   = getColor("callMeButtonTextColor");
        callMeButtonBackgroundColor   = getColor("callMeButtonBackgroundColor");

        ratingHeaderTextColor   = getColor("ratingHeaderTextColor");
        ratingLabelTextColor   = getColor("ratingLabelTextColor");
        ratingHintTextColor   = getColor("ratingHintTextColor");
        ratingButtonTextColor   = getColor("ratingButtonTextColor");
        ratingButtonBackgroundColor   = getColor("ratingButtonBackgroundColor");

        chatRefidTextColor   = getColor("chatRefidTextColor");
        chatNavBarColor   = getColor("chatNavBarColor");
        chatSendButtonEnabledColor   = getColor("chatSendButtonEnabledColor");
        chatSendButtonDisabledColor   = getColor("chatSendButtonDisabledColor");
        chatTimeStampColor   = getColor("chatTimeStampColor");
        chatActionButtonTextColor   = getColor("chatActionButtonTextColor");
        chatActionButtonSelectedTextColor   = getColor("chatActionButtonSelectedTextColor");
        chatActionButtonBorderColor   = getColor("chatActionButtonBorderColor");
        chatIconColor   = getColor("chatIconColor");

        kbFolderNameTextColor   = getColor("kbFolderNameTextColor");
        kbFolderL0BackgroundColor   = getColor("kbFolderL0BackgroundColor");
        kbTextColor   = getColor("kbTextColor");
    }


    private Drawable getDrawable(String identifier) {
        int id = mResources.getIdentifier(identifier, "drawable", mPackageName);
        if ( id != 0 ) {
            return mContext.getResources().getDrawable(id);
        }
        return null;
    }

    public void setLogo(String identifier) {
        int id = mResources.getIdentifier(identifier, "drawable", mPackageName);
        if ( id != 0 ) {
            logo = mContext.getResources().getDrawable(id);
        }
    }


    private int getColor(String identifier) {
        int id = mResources.getIdentifier(identifier, "color", mPackageName);
        if ( id != 0 ) {
            return mContext.getResources().getColor(id);
        }
        return 0;
    }


    public void setIconColor(int color) {
        homeIconColor = color;
        chatIconColor = color;
        chatSendButtonEnabledColor   = color;
        callMeButtonTextColor = color;
        ratingButtonTextColor   = color;
    }

    public void setTextColor(int color) {
        homeTextColor = color;
        kbTextColor = color;
        callMeHeaderTextColor = color;
        callMeLabelTextColor = color;
        callMeHintTextColor = color;
        ratingHeaderTextColor   = color;
        ratingLabelTextColor   = color;

        chatRefidTextColor   = color;
        chatTimeStampColor   = color;
        chatActionButtonTextColor   = color;
        kbTextColor = color;
    }

    public void setDisabledColor(int color) {
        callMeHintTextColor = color;
        ratingHintTextColor = color;
        chatSendButtonDisabledColor   = color;
    }

    public void setBackgroundColor(int color) {
        loginFieldBackgroundColor = color;
        callMeButtonBackgroundColor = color;
        ratingButtonBackgroundColor   = color;
    }

    public void setBorderColor(int color) {
    }


}
