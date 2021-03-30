package com.goboomtown.supportsdk.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.goboomtown.supportsdk.model.BTMerchant;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class EventManager {

    public interface EventManagerListener {
        void event(String name, String type, HashMap<String, String> userInfo);
    }

    public final static String  kSupportSDKEvent                = "com.goboomtown.supportsdk.event";
    public final static String  kSupportSDKEventName            = "event.name";
    public final static String  kSupportSDKEventType            = "event.type";
    public final static String  kSupportSDKEventData            = "event.data";

    public final static String  kEventSDKStarted                = "com.goboomtown.event.sdk.started";
    public final static String  kEventSDKEnded                  = "com.goboomtown.event.sdk.ended";

    public final static String  kEventMenuStarted               = "com.goboomtown.event.menu.started";
    public final static String  kEventMenuEnded                 = "com.goboomtown.event.menu.ended";

    public final static String  kEventChatStarted               = "com.goboomtown.event.chat.started";
    public final static String  kEventChatIssueCreated          = "com.goboomtown.event.chat.issue_created";
    public final static String  kEventChatIssueContinued        = "com.goboomtown.event.chat.issue_continued";
    public final static String  kEventChatIssueResolved         = "com.goboomtown.event.chat.issue_resolved";
    public final static String  kEventChatEnded                 = "com.goboomtown.event.chat.ended";

    public final static String  kEventHistoryStarted            = "com.goboomtown.event.history.started";
    public final static String  kEventHistoryEnded              = "com.goboomtown.event.history.ended";

    public final static String  kEventKnowledgeStarted          = "com.goboomtown.event.knowledge.started";
    public final static String  kEventKnowledgeEnded            = "com.goboomtown.event.knowledge.ended";

    public final static String  kEventFormStarted               = "com.goboomtown.event.form.started";
    public final static String  kEventFormSubmitted             = "com.goboomtown.event.form.submitted";
    public final static String  kEventFormEnded                 = "com.goboomtown.event.form.ended";

    public final static String kEventAlertPresented            = "com.goboomtown.event.alert.presented";
    public final static String kEventAlertDismissed            = "com.goboomtown.event.alert.dismissed";
    public final static String kEventAlertDefaultClicked       = "com.goboomtown.event.alert.default";
    public final static String kEventAlertPositiveClicked      = "com.goboomtown.event.alert.positive";
    public final static String kEventAlertNegativeClicked      = "com.goboomtown.event.alert.negative";

    public final static String  kRequestSupportSDKExit          = "com.goboomtown.request.supportsdk.exit";

    public final static String  kRequestToast                   = "com.goboomtown.request.toast";
    public final static String  kRequestAlert                   = "com.goboomtown.request.alert";
    public final static String  kRequestAlertTitle              = "alert.title";
    public final static String  kRequestAlertMessage            = "alert.message";
    public final static String  kRequestAlertPositiveButtonTitle = "alert.positive";
    public final static String  kRequestAlertNegativeButtonTitle = "alert.negative";

    public final static String  kRequestChatExit                = "com.goboomtown.request.chat.exit";
    public final static String  kRequestChatExitResolveIssue    = "com.goboomtown.request.chat.exit_resolve_issue";

    private static Context                          mContext    = null;
    private static ArrayList<EventManagerListener>  mListeners  = new ArrayList<>();

    public static void setContext(Context context) {
        mContext = context;
    }


    public static void notify(String eventOrRequest, JSONObject userInfo) {
//        for ( EventManagerListener listener : mListeners ) {
//            listener.event(kSupportSDKEvent, eventOrRequest, userInfo);
//        }
        Intent intent = new Intent();
        intent.setAction(kSupportSDKEvent);
        intent.putExtra(kSupportSDKEventType, eventOrRequest);
        if ( userInfo != null ) {
            intent.putExtra(kSupportSDKEventData, userInfo.toString());
        }
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }


    public static void addObserver(EventManagerListener observer) {
        if ( !mListeners.contains(observer) ) {
            mListeners.add(observer);
        }
    }


    public static void removeObserver(EventManagerListener observer) {
        if ( mListeners.contains(observer) ) {
            mListeners.remove(observer);
        }
    }


    public static void addObserver(BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(mContext).registerReceiver(receiver, new IntentFilter(EventManager.kSupportSDKEvent));
    }


    public static void removeObserver(BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);
     }

}
