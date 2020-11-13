package com.goboomtown.supportsdk.api;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.goboomtown.supportsdk.R;

import java.util.Hashtable;

public class Appearance {

    public final static String kMenuTextChat       = "com.goboomtown.supportsdk.menu.text.chat";
    public final static String kMenuIconChat       = "com.goboomtown.supportsdk.menu.icon.chat";
    public final static String kMenuTextCallMe     = "com.goboomtown.supportsdk.menu.text.callme";
    public final static String kMenuIconCallMe     = "com.goboomtown.supportsdk.menu.icon.callme";
    public final static String kMenuTextKnowledge  = "com.goboomtown.supportsdk.menu.text.knowledge";
    public final static String kMenuIconKnowledge  = "com.goboomtown.supportsdk.menu.icon.knowledge";
    public final static String kMenuTextWeb        = "com.goboomtown.supportsdk.menu.text.web";
    public final static String kMenuIconWeb        = "com.goboomtown.supportsdk.menu.icon.web";
    public final static String kMenuTextEmail      = "com.goboomtown.supportsdk.menu.text.email";
    public final static String kMenuIconEmail      = "com.goboomtown.supportsdk.menu.icon.email";
    public final static String kMenuTextPhone      = "com.goboomtown.supportsdk.menu.text.phone";
    public final static String kMenuIconPhone      = "com.goboomtown.supportsdk.menu.icon.phone";
    public final static String kMenuTextForms      = "com.goboomtown.supportsdk.menu.text.forms";
    public final static String kMenuIconForms      = "com.goboomtown.supportsdk.menu.icon.forms";
    public final static String kMenuTextHistory    = "com.goboomtown.supportsdk.menu.text.history";
    public final static String kMenuIconHistory    = "com.goboomtown.supportsdk.menu.icon.history";
    public final static String kMenuBorderColor    = "com.goboomtown.supportsdk.menu.border.color";

    public  Context     mContext;
    private Resources   mResources;
    private String      mPackageName;

    public Hashtable<String, Object>    menuConfiguration;
    public int    menuBorderColor;

    public Drawable logo;
    public Drawable chatAttachmentButtonImage;
    public Drawable chatSendButtonImage;
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
    public int    chatLocalTextColor;
    public int    chatLocalBackgroundColor;
    public int    chatLocalBorderColor;
    public int    chatRemoteTextColor;
    public int    chatRemoteBackgroundColor;
    public int    chatRemoteBorderColor;

    public int      kbFolderNameTextColor;
    public int      kbFolderL0BackgroundColor;
    public int      kbTextColor;
    public Drawable kbFolderIcon = null;

    public int      borderColor;

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

        borderColor = homeTextColor;

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
        chatLocalTextColor = getColor("chatLocalTextColor");
        chatLocalBackgroundColor = getColor("chatLocalBackgroundColor");
        chatLocalBorderColor = getColor("chatLocalBorderColor");
        chatRemoteTextColor = getColor("chatRemoteTextColor");
        chatRemoteBackgroundColor = getColor("chatRemoteBackgroundColor");
        chatRemoteBorderColor = getColor("chatRemoteBorderColor");

        kbFolderNameTextColor   = getColor("kbFolderNameTextColor");
        kbFolderL0BackgroundColor   = getColor("kbFolderL0BackgroundColor");
        kbTextColor   = getColor("kbTextColor");
        kbFolderIcon = null; // mContext.getResources().getDrawable(R.drawable.book_bookmark);
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
        borderColor = color;
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
        borderColor = color;
    }

    public int menuBorderColor() {
        return menuConfiguration.containsKey(kMenuBorderColor ) ? (int) menuConfiguration.get(kMenuBorderColor) : null;
    }

    public void setMenuConfiguration(Hashtable<String, Object>configuration) {
        menuConfiguration = configuration;
    }
}
