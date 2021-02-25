# Boomtown Support SDK for Android (v.1.2.17)

#### Minimum Requirements
- Android 4.4 (API Level 19)
- Android Studio, v2.3.0 or higher recommended
- Gradle 3 or higher recommended

## Overview
**support-sdk-android** contains Android libraries for Boomtown partners. It allows partners to integrate issue creation, notification, and chat in a single `SupportButton` button.

## Getting Started

1. Clone this repository and include as a library project in your Android project.
1. In Android Studio, open File -> New -> Import Module
1. Choose the "Source Directory" by browsing to the file path/location of this clone'd repo.
1. The module name "support-sdk-android" should be auto-filled once you choose this library's file path.
1. Click Finish.

 or -
1. Download the AAR file from [github](https://github.com/goboomtown/support-sdk-android/tree/master/AARs).
1. From within Android Studio, open File -> New -> New Module
1. Choose Import .JAR/.AAR Package, click Next
1. Enter the filename with full path - use "..." to browse to the file location where you downloaded the AARs
1. Include the following dependencies in your application:
```    
    implementation "com.wefika:flowlayout:0.4.1"
    implementation "com.google.code.gson:gson:2.8.5"
    implementation "com.squareup.okhttp3:okhttp:3.12.1"
    implementation "org.igniterealtime.smack:smack-android-extensions:4.3.1"
    implementation "org.igniterealtime.smack:smack-tcp:4.3.1"
    implementation "joda-time:joda-time:2.3"
    implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.8'
    implementation "org.apache.commons:commons-compress:1.18"
    implementation('com.twilio:video-android:5.11.1') {
        exclude group: 'com.android.support', module: 'support-annotations'
    }
```
1. Click Finish

## Support SDK

### Appearance

A `SupportButton` can be added to your app using an XML layout file or programmatically, as shown in this screenshot from one of the included example apps.

Tapping the `SupportButton` will take your user to the Help view.

From the Support view, the user may tap the buttons for chat, web, e-mail, or phone support. If the user taps "Chat With Us," an issue will be created for him/her, and he/she will be taken to a chat room associated with that issue.

### Usage

_Note:_ An example Android application that uses this library may be found in the `Example/BoomtownSample` folder of this repository.

#### Sample XML Layout

```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
       xmlns:tools="http://schemas.android.com/tools"
       android:layout_width="match_parent"
       android:layout_height="match_parent"
       xmlns:app="http://schemas.android.com/apk/res-auto"
       android:paddingBottom="@dimen/activity_vertical_margin"
       android:paddingLeft="@dimen/activity_horizontal_margin"
       android:paddingRight="@dimen/activity_horizontal_margin"
       android:paddingTop="@dimen/activity_vertical_margin"
       tools:context="com.goboomtown.boomtowntest.MainActivity">

       <com.goboomtown.supportsdk.supportsdk.view.SupportButton
           android:id="@+id/supportButton"
           android:layout_width="300dp"
           android:layout_height="300dp"
           android:layout_centerVertical="true"
           android:layout_centerHorizontal="true"
           android:background="@android:color/transparent"
           android:padding="20dp"
           app:exampleColor="#33b5e5"
           app:exampleDimension="24sp"
           app:exampleString="" />

       <FrameLayout
           xmlns:android="http://schemas.android.com/apk/res/android"
           xmlns:app="http://schemas.android.com/apk/res-auto"
           android:id="@+id/fragment_container"
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:visibility="gone" />

   </RelativeLayout>

```


#### Sample Java code

```
SupportButton supportButton = (SupportButton) findViewById(R.id.supportButton);
supportButton.setListener(this);
supportButton.loadConfigurationFile(R.raw.support_sdk, customerId: null);

```


## Server Configuration

The plugin depends on a server JSON configuration file (www/config.json here) that you must obtain from your provider. The file looks like this:

```
{
  "apiHost": "https://api.goboomtown.com",
  "integrationId": "xxxxxx",
  "apiKey": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "buttonURL":"https://api.goboomtown.com/resources/images/sdk_button.png",
  "partnerToken": "xxxxxxxx",
  "privateKey": "xxxxxxxxxxxxxxxxxxxxx"
}
```

This file enables communication with the server and configures the available features.

## Menu Types

The second parameter of loadConfigurationFromJSON() is the desired menu type as per the following list:

```
NoMenu: 0,
Menu: 1,
Button: 2,
IconList: 3,
IconListExit: 4,
```

## Customer Configuration

If desired, the customer may be identified by providing values for any of the following keys"

```
CustomerId : "members_id",
CustomerExternalId: "members_external_id",
CustomerLocationId: "members_locations_id",
CustomerLocationExternalId: "members_locations_external_id",
CustomerLocationMid: "members_locations_mid",
UserId: "members_users_id",
UserExternalId: "members_users_external_id",
UserEmail: "members_users_email",
UserPhone: "members_users_phone",
```

like this:

```
var customerJSON = {
  "members_users_email": "email@example.com"
};
```

## Appearance Configuration

Much of the application (menus, icons, and colors currently) can be configured using a JSON file as follows:

This is the default JSON.

```
{
  "navigationBarAppearance":
  {
    "textColor": "#000000",
    "backgroundColor": "#ffffff",
    "iconColor": "#000000"
  },
  "menuAppearance":
  {
    "heading": "Support",
    "textSize": "20",
    "textStyle": "bold",
    "borderWidth": "1",
    "padding": "10",
    "spacing": "20",
    "textColor": "#000000",
    "iconColor": "#EF5E0D",
    "borderColor": "#E0E0E0",
    "textColorDark": "#ffffff",
    "iconColorDark": "#EF5E0D",
    "borderColorDark": "#E0E0E0"
  },
  "menu":
  {
    "chat": {
      "text": "Chat with Us",
      "icon": "a_chat"
    },
    "callme":
    {
      "text": "Call Me",
      "icon": "phone_call"
    },
    "knowledge":
    {
      "text": "Search Knowledge",
      "icon": "book_bookmark"
    },
    "web":
    {
      "text": "Web Support",
      "icon": "globe"
    },
    "email":
    {
      "text": "Email Support",
      "icon": "letter"
    },
    "phone":
    {
      "text": "Phone Support",
      "icon": "phone"
    },
    "forms":
    {
      "text": "Forms",
      "icon": "form"
    },
    "history":
    {
      "text": "History",
      "icon": "customer_alt"
    },
    "exit":
    {
      "text": "Exit"
    }
  },
  "icons":
  {
    "kbFolderIcon": "book-bookmark",
    "chatAttachmentButtonImage": "paperclip.png",
    "chatSendButtonImage": "send.png"
  },
  "colors":
  {
    "navigationBarColor": "#f2f2f2",
    "iconColor": "#EF5E0D",
    "buttonColor": "#EF5E0D",
    "lineColor": "#E0E0E0",
    "textColor": "#4F4F4F",
    "textColorDark": "#ffffff",
    "homeIconColor": "#EF5E0D",
    "homeLineColor": "#E0E0E0",
    "homeTextColor": "#007AFF",
    "homeSelectedColor": "#EBEBEB",
    "callMeHeaderTextColor": "#4F4F4F",
    "callMeLabelTextColor": "#626363",
    "callMeHintTextColor": "#ACACAC",
    "callMeButtonTextColor": "#ACACAC",
    "callMeButtonBackgroundColor": "#1AA8A8",
    "ratingHeaderTextColor": "#4F4F4F",
    "ratingLabelTextColor": "#626363",
    "ratingHintTextColor": "#ACACAC",
    "ratingButtonTextColor": "#ACACAC",
    "ratingButtonBackgroundColor": "#1AA8A8",
    "chatRefidTextColor": "#4f4f4f",
    "chatNavBarColor": "#f2f2f2",
    "chatSendButtonEnabledColor": "#626363",
    "chatSendButtonDisabledColor": "#ACACAC",
    "chatTimeStampColor": "#ACACAC",
    "chatActionButtonTextColor": "#838383",
    "chatActionButtonSelectedTextColor": "#ffffff",
    "chatActionButtonBorderColor": "#E0E0E0",
    "chatIconColor": "#838383",
    "kbFolderNameTextColor": "#303030",
    "kbFolderL0BackgroundColor": "#F3F8F8",
    "kbTextColor": "#303030",
    "menuBorderColor": "#E0E0E0"
  },
  "formAppearance":
  {
    "cancelButtonText": "Cancel",
    "saveButtonText": "Save",
    "label":
    {
      "textStyle": "bold",
      "textSize": "20",
      "textColor": "#000000",
      "textColorDark": "#ffffff",
      "requiredTextColor": "#000000",
      "requiredTextColorDark": "#ffffff",
      "requiredIndicatorColor": "#cc0000",
      "requiredIndicatorColorDark": "#cc0000"
    },
    "entry":
    {
      "textStyle": "bold",
      "textSize": "20",
      "textColor": "#000000",
      "textColorDark": "#ffffff",
      "borderColor": "#000000",
      "borderColorDark": "#ffffff",
      "borderWidth": "1"
    }
  }
}
```

If you only want to configure a general color scheme you need only set the following colors:

```
iconColor
buttonColor
lineColor
textColor
textColorDark
```


### Connect Intelligent Agent (mDNS) Broadcasts Using SupportButton
The SupportButton provides a convenient way to connect to Boomtown onsite intelligence agents.  This provides a mechanism for broadcasting mDNS data.  This can be done with a call to SupportButton#advertiseServiceWithPublicData(Map, Map) method.

```
Map<String, String> myPubData = new HashMap<String, String>();
myPubData.put("public", "fooData");
Map<String, String> myPrivData = new HashMap<String, String>();
myPrivData.put("private", "someEncryptedData");

supportButton.advertiseServiceWithPublicData(myPubData, myPrivData);
```

The lifecycle of advertiseServiceWithPublicData() will be managed by the SupportButton class.  To
manage the lifecycle yourself you can invoke SupportButton#stopAdvertiseServiceWithPublicData().

Two methods in SupportButtonButtonListener provide for insight into the broadcast of mDNS data:

1. SupportButtonListener#supportButtonDidAdvertiseService()
1. SupporrButtonListener#supportButtonDidFailToAdvertiseService()


## Building Library from Sources

The steps to build this library from source are:

1. Verify a working Gradle v3+ installation.
1. git clone \[this-project]
1. cd /\[this-project]
1. Issue the following command

```
gradle build -x javadoc -x lint
```
## Support for KnowledgeBase Articles

In order to fully support KnowledgeBase articles within chat, you will need to add the KBActivity to your Android manifest:

```
<activity
     android:name="com.goboomtown.activity.KBActivity"
     android:configChanges="orientation|keyboardHidden|screenSize" />
```

Failure to do so will lead to an inevitable crash when attempting to view an article from within chat.

## Acknowledgements

**support-sdk-android** uses Smack (http://www.igniterealtime.org/projects/smack/), and we are grateful for the contributions of the open source community.
