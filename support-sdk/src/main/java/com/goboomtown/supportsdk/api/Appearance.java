package com.goboomtown.supportsdk.api;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Window;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Iterator;

public class Appearance {

    public final static int     defaultTextSize                = 14;

    public final static String chatMenuText                = "chatMenuText";
    public final static String chatMenuIcon                = "chatMenuIcon";
    public final static String callMeMenuText              = "callmeMenuText";
    public final static String callMeMenuIcon              = "callmeMenuIcon";
    public final static String knowledgeMenuText           = "knowledgeMenuText";
    public final static String knowledgeMenuIcon           = "knowledgeMenuIcon";
    public final static String webMenuText                 = "webMenuText";
    public final static String webMenuIcon                 = "webMenuIcon";
    public final static String emailMenuText               = "emailMenuText";
    public final static String emailMenuIcon               = "emailMenuIcon";
    public final static String phoneMenuText               = "phoneMenuText";
    public final static String phoneMenuIcon               = "phoneMenuIcon";
    public final static String formsMenuText               = "formsMenuText";
    public final static String formsMenuIcon               = "formsMenuIcon";
    public final static String historyMenuText             = "historyMenuText";
    public final static String historyMenuIcon             = "historyMenuIcon";
    public final static String exitMenuText                = "exitMenuText";
    public final static String exitMenuIcon                = "exitMenuIcon";

    public final static String navigationBar               = "navigationBarAppearance";
    public final static String menuAppearance              = "menuAppearance";
    public final static String formAppearance              = "formAppearance";
    public final static String menu                        = "menu";
    public final static String icons                       = "icons";
    public final static String colors                      = "colors";
    public final static String chat                        = "chat";
    public final static String callme                      = "callme";
    public final static String knowledge                   = "knowledge";
    public final static String web                         = "web";
    public final static String email                       = "email";
    public final static String phone                       = "phone";
    public final static String forms                       = "forms";
    public final static String history                     = "history";
    public final static String kExit                       = "exit";


    public final static String kbFolderIcon                = "kbFolderIcon";
    public final static String chatAttachmentButtonImage   = "chatAttachmentButtonImage";
    public final static String chatSendButtonImage         = "chatSendButtonImage";

    public final static String navigationBarColor          = "navigationBarColor";
    public final static String navigationBarColorDark      = "navigationBarColorDark";
    public final static String buttonColor                 = "buttonColor";
    public final static String backgroundColor             = "backgroundColor";
    public final static String lineColor                   = "lineColor";
    public final static String lineColorDark               = "lineColorDark";
    public final static String textColor                   = "textColor";
    public final static String textColorDark               = "textColorDark";
    public final static String iconColor                   = "iconColor";
    public final static String iconColorDark               = "iconColorDark";
    public final static String borderColor                 = "borderColor";
    public final static String borderColorDark             = "borderColorDark";
    public final static String style                       = "style";
    public final static String heading                     = "heading";
    public final static String textSize                    = "textSize";
    public final static String textStyle                   = "textStyle";
    public final static String borderWidth                 = "borderWidth";
    public final static String padding                     = "padding";
    public final static String spacing                     = "spacing";
    public final static String requiredTextColor           = "requiredTextColor";
    public final static String requiredTextColorDark       = "requiredTextColorDark";
    public final static String requiredIndicatorColor      = "requiredIndicatorColor";
    public final static String requiredIndicatorColorDark  = "requiredIndicatorColorDark";
    public final static String cancelButtonText            = "cancelbuttontext";
    public final static String saveButtonText              = "savebuttontext";
    public final static String label                       = "label";
    public final static String entry                       = "entry";


    public final static String homeIconColor               = "homeIconColor";
    public final static String homeLineColor               = "homeLineColor";
    public final static String homeTextColor               = "homeTextColor";
    public final static String homeSelectedColor           = "homeSelectedColor";
    public final static String callMeHeaderTextColor       = "callMeHeaderTextColor";
    public final static String callMeLabelTextColor        = "callMeLabelTextColor";
    public final static String callMeHintTextColor         = "callMeHintTextColor";
    public final static String callMeButtonTextColor       = "callMeButtonTextColor";
    public final static String callMeButtonBackgroundColor = "callMeButtonBackgroundColor";
    public final static String ratingHeaderTextColor       = "ratingHeaderTextColor";
    public final static String ratingLabelTextColor        = "ratingLabelTextColor";
    public final static String ratingHintTextColor         = "ratingHintTextColor";
    public final static String ratingButtonTextColor       = "ratingButtonTextColor";
    public final static String ratingButtonBackgroundColor = "ratingButtonBackgroundColor";
    public final static String chatRefidTextColor          = "chatRefidTextColor";
    public final static String chatNavBarColor             = "chatNavBarColor";
    public final static String chatSendButtonEnabledColor  = "chatSendButtonEnabledColor";
    public final static String chatSendButtonDisabledColor = "chatSendButtonDisabledColor";
    public final static String chatTimeStampColor          = "chatTimeStampColor";
    public final static String chatActionButtonTextColor   = "chatActionButtonTextColor";
    public final static String chatActionButtonSelectedTextColor = "chatActionButtonSelectedTextColor";
    public final static String chatActionButtonBorderColor = "chatActionButtonBorderColor";
    public final static String chatIconColor               = "chatIconColor";
    public final static String kbFolderNameTextColor       = "kbFolderNameTextColor";
    public final static String kbFolderNameTextColorDark   = "kbFolderNameTextColorDark";
    public final static String kbFolderL0BackgroundColor   = "kbFolderL0BackgroundColor";
    public final static String kbTextColor                 = "kbTextColor";
    public final static String kbTextColorDark             = "kbTextColorDark";
//    public final static String menuBorderColor             = "menuBorderColor";

    public final static int defaultIconColor    = 0xEF5E0D;
    public final static int defaultButtonColor  = 0xEF5E0D;
    public final static int defaultLineColor    = 0xE0E0E0;
    public final static int defaultTextColor    = 0x4F4F4F;


    public  Context     mContext;
    private Resources   mResources;
    private String      mPackageName;

    private Hashtable<String, Object>    configuration;
    private Hashtable<String, Object>    menuConfiguration;
    private Hashtable<String, Object>    formConfiguration;
    private Hashtable<String, Object>    navBarConfiguration;

    public Drawable logo;
//    public Drawable chatAttachmentButtonImage;
//    public Drawable chatSendButtonImage;
    public int    loginBackgroundColor;
    public int    loginFieldBorderColor;
    public int    loginFieldBackgroundColor;
    public int    loginFieldTextColor;
    public int    loginButtonColor;

    public int    chatLocalTextColor;
    public int    chatLocalBackgroundColor;
    public int    chatLocalBorderColor;
    public int    chatRemoteTextColor;
    public int    chatRemoteBackgroundColor;
    public int    chatRemoteBorderColor;

    private String  menuStyle;

//    public int      borderColor;

    public Appearance(Context context) {
        mContext = context;
        mResources = mContext.getResources();
        mPackageName = mContext.getPackageName();

        configuration = new Hashtable<String, Object>();
        navBarConfiguration = new Hashtable<String, Object>();
        menuConfiguration = new Hashtable<String, Object>();
        formConfiguration = new Hashtable<String, Object>();

        String jsonString = getJSONFromFile(R.raw.default_appearance);
        if ( jsonString != null ) {
            configureFromJSON(jsonString);
        }

        chatLocalTextColor = getColor("chatLocalTextColor");
        chatLocalBackgroundColor = getColor("chatLocalBackgroundColor");
        chatLocalBorderColor = getColor("chatLocalBorderColor");
        chatRemoteTextColor = getColor("chatRemoteTextColor");
        chatRemoteBackgroundColor = getColor("chatRemoteBackgroundColor");
        chatRemoteBorderColor = getColor("chatRemoteBorderColor");
    }

    public void configureActionBarForActivity(AppCompatActivity activity) {
        if ( activity==null || !(activity instanceof AppCompatActivity) ) {
            return;
        }
        ActionBar actionBar = activity.getSupportActionBar();
        if ( actionBar == null ) {
            return;
        }
        configureActionBar(actionBar);
    }

    public void configureActionBar(ActionBar actionBar) {
        actionBar.setBackgroundDrawable(new ColorDrawable(navigationBarBackgroundColor()));
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            window.setNavigationBarColor(navigationBarBackgroundColor());
//        }
        Spannable text = new SpannableString(actionBar.getTitle());
        text.setSpan(new ForegroundColorSpan(navigationBarTextColor()), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        actionBar.setTitle(text);
    }


    public void configureActionBarTitle(ActionBar actionBar, String title) {
        actionBar.setBackgroundDrawable(new ColorDrawable(navigationBarBackgroundColor()));
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            window.setNavigationBarColor(navigationBarBackgroundColor());
//        }
        Spannable text = new SpannableString(title);
        text.setSpan(new ForegroundColorSpan(navigationBarTextColor()), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        actionBar.setTitle(text);
    }


    public boolean isDarkMode() {
        int currentNightMode = mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public String getJSONFromFile(int fileResource) {
        String jsonString = null;
        try {
            jsonString = Utils.readRawTextFile(mContext, fileResource);
        } catch (Exception e) {
        }
        return jsonString;
    }

    public void configureFromJSON(String jsonString) {
        Hashtable<String, Object> newConfiguration = new Hashtable<>();
        try {
            Iterator iter;
            JSONObject json = new JSONObject(jsonString);

            JSONObject navigationBarAppearanceJSON = json.optJSONObject(navigationBar);
            if ( navigationBarAppearanceJSON != null ) {
                iter = navigationBarAppearanceJSON.keys();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    if (key.toLowerCase().contains("color")) {
                        String colorString = navigationBarAppearanceJSON.getString(key);
                        int color = Color.parseColor(colorString);
                        navBarConfiguration.put(key.toLowerCase(), color);
                    }
                }
            }

            processFormAppearanceWithDictionary(json);

            processMenuAppearance(json);

            JSONObject menu = json.optJSONObject("menu");
            if ( menu != null ) {
                iter = menu.keys();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    JSONObject menuItem = menu.getJSONObject(key);
                    String text = menuItem.optString("text");
                    if (text != null) {
                        String newKey = key + "MenuText";
                        menuConfiguration.put(newKey, text);
                    }
                    String icon = menuItem.optString("icon");
                    if (icon != null) {
                        String newKey = key + "MenuIcon";
                        int id = mContext.getResources().getIdentifier(icon, "drawable", mContext.getPackageName());
                        if (id != 0) {
                            Drawable image = mContext.getResources().getDrawable(id);
                            if (image != null) {
                                menuConfiguration.put(newKey, image);
                            }
                        }
                    }
                }
            }

            JSONObject icons = json.optJSONObject("icons");

            JSONObject colors = json.optJSONObject("colors");
            if ( colors != null ) {
                iter = colors.keys();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    String colorString = colors.getString(key);
                    int color = Color.parseColor(colorString);
                    newConfiguration.put(key, color);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if ( newConfiguration.size() > 0 ) {
            configuration.clear();
            configuration = newConfiguration;
        }
    }

    private void processMenuAppearance(JSONObject json) {
        try {
            JSONObject menuAppearanceJSON = json.optJSONObject(menuAppearance);
            if (menuAppearanceJSON == null) {
                return;
            }
            Iterator iter = menuAppearanceJSON.keys();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                if (key.toLowerCase().contains("color")) {
                    String colorString = menuAppearanceJSON.getString(key);
                    int color = Color.parseColor(colorString);
                    menuConfiguration.put(key, color);
                } else if ( key.toLowerCase().equals("style") ) {
                    menuStyle = menuAppearanceJSON.getString(key).toLowerCase();
                } else if ( key.toLowerCase().contains("heading") || key.toLowerCase().contains("style")) {
                    menuConfiguration.put(key, menuAppearanceJSON.getString(key));
                } else {
                    int value = menuAppearanceJSON.getInt(key);
                    menuConfiguration.put(key, value);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processFormAppearanceWithDictionary(JSONObject json) {
        try {
            JSONObject formAppearanceJSON = json.optJSONObject(formAppearance);
            if ( formAppearanceJSON == null ) {
                return;
            }
            Iterator iter = formAppearanceJSON.keys();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                if ( key.equalsIgnoreCase(label) || key.equalsIgnoreCase(entry) ) {
                    JSONObject itemJSON = formAppearanceJSON.getJSONObject(key);
                    Iterator itemIter = itemJSON.keys();
                    while ( itemIter.hasNext() ) {
                        String itemKey = (String) itemIter.next();
                        String storekey = (key + itemKey).toLowerCase();
                        if ( itemKey.toLowerCase().contains("color") ) {
                            String colorString = itemJSON.getString(itemKey);
                            int color = Color.parseColor(colorString);
                            formConfiguration.put(storekey, color);
                        } else if (itemKey.toLowerCase().contains("heading") || itemKey.toLowerCase().contains("style")) {
                            formConfiguration.put(storekey, itemJSON.getString(itemKey));
                        } else {
                            int value = itemJSON.getInt(itemKey);
                            formConfiguration.put(storekey, value);
                        }
                    }
                } else {
                    if ( key.toLowerCase().contains("text") ) {
                         formConfiguration.put(key.toLowerCase(), formAppearanceJSON.getString(key));
                    } else {
                        int value = formAppearanceJSON.getInt(key);
                        formConfiguration.put(key, value);
                    }
                }
            }
        } catch ( JSONException e ) {

        }
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

    public boolean hasMenuBorder() {
        return (int)menuConfiguration.get(borderWidth) > 0;
    }

//    public int menuBorderColor() {
//        return (int) menuConfiguration.get(menuBorderColor);
//    }

    public void setMenuConfiguration(Hashtable<String, Object>configuration) {
        menuConfiguration = configuration;
    }


    public int navigationBarColor() {
        int color;
        if ( isDarkMode() ) {
            color = configuration.containsKey(navigationBarColorDark) ? (int)configuration.get(navigationBarColorDark) : (int)configuration.get(navigationBarColor);
        } else {
            color = (int)configuration.get(navigationBarColor);
        }
        return color;
    }

    public int iconColor() {
        int color;
        if ( isDarkMode() ) {
            color = configuration.containsKey(iconColorDark) ? (int)configuration.get(iconColorDark) : (int)configuration.get(iconColor);
        } else {
            color = (int)configuration.get(iconColor);
        }
        return color;
    }

    public int buttonColor() {
        return configuration.containsKey(buttonColor) ? (int) configuration.get(buttonColor) : defaultButtonColor;
    }

    public int lineColor() {
        int color;
        if ( isDarkMode() ) {
            color = configuration.containsKey(lineColorDark) ? (int)configuration.get(lineColorDark) : (int)configuration.get(lineColor);
        } else {
            color = (int)configuration.get(lineColor);
        }
        return color;
    }

    public int textColor() {
        int color;
        if ( isDarkMode() ) {
//            int baseTextColorReversed = 0xffffff - (int)configuration.get(textColor);
            color = configuration.containsKey(textColorDark) ? (int)configuration.get(textColorDark) : (int)configuration.get(textColor);
//            color = configuration.containsKey(textColorDark) ? (int)configuration.get(textColorDark) : baseTextColorReversed;
        } else {
            color = (int)configuration.get(textColor);
        }
        return color;
    }


    public int homeIconColor() {
        return configuration.containsKey(homeIconColor) ? (int) configuration.get(homeIconColor) : iconColor();
    }

    public int homeLineColor() {
        return configuration.containsKey(homeLineColor) ? (int) configuration.get(homeLineColor) : lineColor();
    }

    public int homeTextColor() {
        return configuration.containsKey(homeTextColor) ? (int) configuration.get(homeTextColor) : textColor();
    }

    public int homeSelectedColor() {
        return configuration.containsKey(homeSelectedColor) ? (int) configuration.get(homeSelectedColor) : textColor();
    }

    public int callMeHeaderTextColor() {
        return configuration.containsKey(callMeHeaderTextColor) ? (int) configuration.get(callMeHeaderTextColor) : textColor();
    }

    public int callMeLabelTextColor() {
        return configuration.containsKey(callMeLabelTextColor) ? (int) configuration.get(callMeLabelTextColor) : textColor();
    }

    public int callMeHintTextColor() {
        return configuration.containsKey(callMeHintTextColor) ? (int) configuration.get(callMeHintTextColor) : textColor();
    }

    public int callMeButtonTextColor() {
        return configuration.containsKey(callMeButtonTextColor) ? (int) configuration.get(callMeButtonTextColor) : buttonColor();
    }

    public int callMeButtonBackgroundColor() {
        return (int) configuration.get(callMeButtonBackgroundColor);
    }


    public int chatRefidTextColor() {
        return configuration.containsKey(chatRefidTextColor) ? (int) configuration.get(chatRefidTextColor) : textColor();
    }

    public int chatNavBarColor() {
        return configuration.containsKey(chatNavBarColor) ? (int) configuration.get(chatNavBarColor) : navigationBarColor();
    }


    public int chatSendButtonEnabledColor() {
        return configuration.containsKey(chatSendButtonEnabledColor) ? (int) configuration.get(chatSendButtonEnabledColor) : buttonColor();
    }

    public int chatSendButtonDisabledColor() {
        return configuration.containsKey(chatSendButtonDisabledColor) ? (int) configuration.get(chatSendButtonDisabledColor) : buttonColor();
    }

    public int chatTimeStampColor() {
        return configuration.containsKey(chatTimeStampColor) ? (int) configuration.get(chatTimeStampColor) : textColor();
    }

    public int chatActionButtonTextColor() {
        return configuration.containsKey(chatActionButtonTextColor) ? (int) configuration.get(chatActionButtonTextColor) : buttonColor();
    }

    public int chatActionButtonSelectedTextColor() {
        return configuration.containsKey(chatActionButtonSelectedTextColor) ? (int) configuration.get(chatActionButtonSelectedTextColor) : buttonColor();
    }

    public int chatActionButtonBorderColor() {
        return configuration.containsKey(chatActionButtonBorderColor) ? (int) configuration.get(chatActionButtonBorderColor) : lineColor();
    }

    public int chatIconColor() {
        return configuration.containsKey(chatIconColor) ? (int) configuration.get(chatIconColor) : buttonColor();
    }

    public int ratingHeaderTextColor() {
        return configuration.containsKey(ratingHeaderTextColor) ? (int) configuration.get(ratingHeaderTextColor) : textColor();
    }


    public int ratingLabelTextColor() {
        return configuration.containsKey(ratingLabelTextColor) ? (int) configuration.get(ratingLabelTextColor) : textColor();
    }


    public int ratingHintTextColor() {
        return configuration.containsKey(ratingHintTextColor) ? (int) configuration.get(ratingHintTextColor) : textColor();
    }


    public int ratingButtonTextColor() {
        return configuration.containsKey(ratingButtonTextColor) ? (int) configuration.get(ratingButtonTextColor) : buttonColor();
    }


    public int ratingButtonBackgroundColor() {
        return (int) configuration.get(ratingButtonBackgroundColor);
    }


    public int kbFolderNameTextColor() {
        int color;
        if ( isDarkMode() ) {
            color = configuration.containsKey(kbFolderNameTextColorDark) ? (int)configuration.get(kbFolderNameTextColorDark) : (int)textColor();
        } else {
            color = configuration.containsKey(kbFolderNameTextColor) ? (int)configuration.get(kbFolderNameTextColor) : (int)textColor();
        }
        return color;
    }

    public int kbFolderL0BackgroundColor() {
        return (int) configuration.get(kbFolderL0BackgroundColor);
    }

    public int kbTextColor() {
        int color;
        if ( isDarkMode() ) {
            color = configuration.containsKey(kbTextColorDark) ? (int)configuration.get(kbTextColorDark) : (int)textColor();
        } else {
            color = configuration.containsKey(kbTextColor) ? (int)configuration.get(kbTextColor) : (int)textColor();
        }
        return color;
    }

    public Drawable kbFolderIcon() {
        return (Drawable) configuration.get(kbFolderIcon);
    }


    public Drawable chatAttachmentButtonImage() {
        return (Drawable) configuration.get(chatAttachmentButtonImage);
    }

    public Drawable chatSendButtonImage()
    {
        return (Drawable) configuration.get(chatSendButtonImage);
    }


    /* Navigation Bar Configuration */

    public int navigationBarTextColor() {
        int color;
        String colorKey = textColor.toLowerCase();
        String colorDarkKey = colorKey; //(colorKey + "dark").toLowerCase();
        if ( isDarkMode() ) {
            color = navBarConfiguration.get(colorDarkKey)!=null ? (int)navBarConfiguration.get(colorDarkKey) : (int)navBarConfiguration.get(colorKey);
        } else {
            color = (int)navBarConfiguration.get(colorKey);
        }
        return color;
    }

    public int navigationBarIconColor() {
        int color;
        String colorKey = iconColor.toLowerCase();
        String colorDarkKey = colorKey; //(colorKey + "dark").toLowerCase();
        if ( isDarkMode() ) {
            color = navBarConfiguration.get(colorDarkKey)!=null ? (int)navBarConfiguration.get(colorDarkKey) : (int)navBarConfiguration.get(colorKey);
        } else {
            color = (int)navBarConfiguration.get(colorKey);
        }
        return color;
    }

    public int navigationBarBackgroundColor() {
        int color;
        String colorKey = backgroundColor.toLowerCase();
        String colorDarkKey = colorKey; //(colorKey + "dark").toLowerCase();
        if ( isDarkMode() ) {
            color = navBarConfiguration.get(colorDarkKey)!=null ? (int)navBarConfiguration.get(colorDarkKey) : (int)navBarConfiguration.get(colorKey);
        } else {
            color = (int)navBarConfiguration.get(colorKey);
        }
        return color;
    }


    public int backgroundColor() {
        int color;
        if ( isDarkMode() ) {
            color = android.R.color.black;
        } else {
            color = android.R.color.white;
        }
        return color;
    }


    /*  Form Configuration */

    public String formCancelButtonText() {
        return formConfiguration.get(cancelButtonText)!=null ? (String)formConfiguration.get(cancelButtonText) : "Cancel";
    }

    public String formSaveButtonText() {
        return formConfiguration.get(saveButtonText)!=null ? (String)formConfiguration.get(saveButtonText) : "Save";
    }

    public int formSpacing() {
        String key = spacing.toLowerCase();
        return formConfiguration.get(key) != null ? (int)formConfiguration.get(key) : 10;
    }


    public int formEntryBorderColor() {
        int color;
        String colorKey = (entry + borderColor).toLowerCase();
        String colorDarkKey = (colorKey + "dark").toLowerCase();
        if ( isDarkMode() ) {
            color = formConfiguration.get(colorDarkKey)!=null ? (int)formConfiguration.get(colorDarkKey) : (int)formConfiguration.get(colorKey);
        } else {
            color = (int)formConfiguration.get(colorKey);
        }
        return color;
    }


    public int formEntryTextColor() {
        int color;
        String colorKey = (entry + textColor).toLowerCase();
        String colorDarkKey = (colorKey + "dark").toLowerCase();
        if ( isDarkMode() ) {
            color = formConfiguration.get(colorDarkKey)!=null ? (int)formConfiguration.get(colorDarkKey) : (int)formConfiguration.get(colorKey);
        } else {
            color = (int)formConfiguration.get(colorKey);
        }
        return color;
    }


    public int formLabelTextColor() {
        int color;
        String colorKey = (label + textColor).toLowerCase();
        String colorDarkKey = (colorKey + "dark").toLowerCase();
        if ( isDarkMode() ) {
            color = formConfiguration.get(colorDarkKey)!=null ? (int)formConfiguration.get(colorDarkKey) : (int)formConfiguration.get(colorKey);
        } else {
            color = (int)formConfiguration.get(colorKey);
        }
        return color;
    }


    public int formLabelRequiredTextColor() {
        int color;
        String colorKey = (label + requiredTextColor).toLowerCase();
        String colorDarkKey = (colorKey + "dark").toLowerCase();
        if ( isDarkMode() ) {
            color = formConfiguration.get(colorDarkKey)!=null ? (int)formConfiguration.get(colorDarkKey) : (int)formConfiguration.get(colorKey);
        } else {
            color = (int)formConfiguration.get(colorKey);
        }
        return color;
    }


    public int formLabelRequiredIndicatorColor() {
        int color;
        String colorKey = (label + requiredIndicatorColor).toLowerCase();
        String colorDarkKey = (colorKey + "dark").toLowerCase();
        if ( isDarkMode() ) {
            color = formConfiguration.get(colorDarkKey)!=null ? (int)formConfiguration.get(colorDarkKey) : (int)formConfiguration.get(colorKey);
        } else {
            color = (int)formConfiguration.get(colorKey);
        }
        return color;
    }


    public int formLabelTextSize() {
        String key = (label + textSize).toLowerCase();
        return formConfiguration.get(key) != null ? (int)formConfiguration.get(key) : 14;
    }


    public int formEntryTextSize() {
        String key = (entry + textSize).toLowerCase();
        return formConfiguration.get(key) != null ? (int)formConfiguration.get(key) : 14;
    }


    public int formEntryBorderWidth() {
        String key = (entry + borderWidth).toLowerCase();
        return formConfiguration.get(key) != null ? (int)formConfiguration.get(key) : 0;
    }



    public String formLabelTextStyle() {
        String key = (label + textStyle).toLowerCase();
        return formConfiguration.get(key)!=null ? (String)formConfiguration.get(key) : null;
    }

    public String formEntryTextStyle() {
        String key = (entry + textStyle).toLowerCase();
        return formConfiguration.get(key)!=null ? (String)formConfiguration.get(key) : null;
    }


    /* Menu Configuration */

    public String menuStyle() {
        return menuStyle;
    }


    public String menuHeading() {
        return menuConfiguration.get(heading)!=null ? (String)menuConfiguration.get(heading) : "Support";
    }


    public int menuTextSize() {
        return menuConfiguration.get(textSize)!=null ? (int)menuConfiguration.get(textSize) : defaultTextSize;
    }

    public String menuTextStyle() {
        return menuConfiguration.get(textStyle)!=null ? (String)menuConfiguration.get(textStyle) : null;
    }

    public int menuTextColor() {
        int color;
        if ( isDarkMode() ) {
            color = menuConfiguration.get(textColorDark)!=null ? (int)menuConfiguration.get(textColorDark) : (int)menuConfiguration.get(textColor);
        } else {
            color = (int)menuConfiguration.get(textColor);
        }
        return color;
    }

    public int menuIconColor() {
        int color;
        if ( isDarkMode() ) {
            color = menuConfiguration.get(iconColorDark)!=null ? (int)menuConfiguration.get(iconColorDark) : (int)menuConfiguration.get(iconColor);
        } else {
            color = (int)menuConfiguration.get(iconColor);
        }
        return color;
    }

    public int menuBorderColor() {
        int color;
        if ( isDarkMode() ) {
            color = menuConfiguration.get(borderColorDark)!=null ? (int)menuConfiguration.get(borderColorDark) : (int)menuConfiguration.get(borderColor);
        } else {
            color = (int)menuConfiguration.get(borderColor);
        }
        return color;
    }

    public int menuBorderWidth() {
        return menuConfiguration.get(borderWidth)!=null ? (int)menuConfiguration.get(borderWidth) : 1;
    }

    public int menuPadding() {
        return menuConfiguration.get(padding)!=null ? (int)menuConfiguration.get(padding) : 0;
    }

    public int menuSpacing() {
        return menuConfiguration.get(spacing)!=null ? (int)menuConfiguration.get(spacing) : 0;
    }

    public String chatMenuText() {
        return (String) menuConfiguration.get(chatMenuText);
    }

    public Drawable chatMenuIcon() {
        return (Drawable) menuConfiguration.get(chatMenuIcon);
    }

    public String callMeMenuText() {
        return (String) menuConfiguration.get(callMeMenuText);
    }

    public Drawable callMeMenuIcon() {
        return (Drawable) menuConfiguration.get(callMeMenuIcon);
    }

    public String knowledgeMenuText() {
        return (String) menuConfiguration.get(knowledgeMenuText);
    }

    public Drawable knowledgeMenuIcon() {
        return (Drawable) menuConfiguration.get(knowledgeMenuIcon);
    }

    public String webMenuText() {
        return (String) menuConfiguration.get(webMenuText);
    }

    public Drawable webMenuIcon() {
        return (Drawable) menuConfiguration.get(webMenuIcon);
    }

    public String emailMenuText() {
        return (String) menuConfiguration.get(emailMenuText);
    }

    public Drawable emailMenuIcon() {
        return (Drawable) menuConfiguration.get(emailMenuIcon);
    }

    public String phoneMenuText() {
        return (String) menuConfiguration.get(phoneMenuText);
    }

    public Drawable phoneMenuIcon() {
        return (Drawable) menuConfiguration.get(phoneMenuIcon);
    }

    public String formsMenuText() {
        return (String) menuConfiguration.get(formsMenuText);
    }

    public Drawable formsMenuIcon() {
        return (Drawable) menuConfiguration.get(formsMenuIcon);
    }

    public String historyMenuText() {
        return (String) menuConfiguration.get(historyMenuText);
    }

    public Drawable historyMenuIcon() {
        return (Drawable) menuConfiguration.get(historyMenuIcon);
    }

    public String exitMenuText() {
        return (String) menuConfiguration.get(exitMenuText);
    }

    public Drawable exitMenuIcon() {
        return (Drawable) menuConfiguration.get(exitMenuIcon);
    }



}
