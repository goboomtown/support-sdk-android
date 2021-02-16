package com.goboomtown.supportsdk.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.goboomtown.chat.BoomtownChat;
import com.goboomtown.chat.BoomtownChatMessage;
import com.goboomtown.chat.ChatRecord;
import com.goboomtown.fragment.BaseChatFragment;
import com.goboomtown.activity.KBActivity;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.model.BTConnectIssue;
import com.goboomtown.supportsdk.activity.VideoActivity;
import com.goboomtown.supportsdk.view.SupportButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Larry Borsato on 2016-07-12.
 */
public class ChatFragment extends BaseChatFragment {

    private static final String TAG = ChatFragment.class.getSimpleName();

    public  SupportButton   mSupportButton = null;
    public  SupportSDK      supportSDK = null;
    public  BTConnectIssue  mIssue = null;
    public  Context         mContext;
    private MenuItem        mMenuItemEndCall;
    private boolean         isScreenShare = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }


    public void configureAppearance()
    {
        if ( supportSDK.appearance.chatAttachmentButtonImage() != null ) {
            BoomtownChat.sharedInstance().chatAttachmentButtonImage = supportSDK.appearance.chatAttachmentButtonImage();
        }
        if ( supportSDK.appearance.chatSendButtonImage() != null ) {
            BoomtownChat.sharedInstance().chatSendButtonImage = supportSDK.appearance.chatSendButtonImage();
        }
        BoomtownChat.sharedInstance().chatLocalTextColor = supportSDK.appearance.chatLocalTextColor;
        BoomtownChat.sharedInstance().chatLocalBackgroundColor = supportSDK.appearance.chatLocalBackgroundColor;
        BoomtownChat.sharedInstance().chatLocalBorderColor = supportSDK.appearance.chatLocalBorderColor;
        BoomtownChat.sharedInstance().chatRemoteTextColor = supportSDK.appearance.chatRemoteTextColor;
        BoomtownChat.sharedInstance().chatRemoteBackgroundColor = supportSDK.appearance.chatRemoteBackgroundColor;
        BoomtownChat.sharedInstance().chatRemoteBorderColor = supportSDK.appearance.chatRemoteBorderColor;
        BoomtownChat.sharedInstance().chatSendButtonDisabledColor   = supportSDK.appearance.chatSendButtonDisabledColor();
        BoomtownChat.sharedInstance().chatSendButtonColor           = supportSDK.appearance.chatSendButtonEnabledColor();
        BoomtownChat.sharedInstance().chatActionButtonTextColor     = supportSDK.appearance.chatActionButtonTextColor();
        BoomtownChat.sharedInstance().chatActionButtonTextColor     = supportSDK.appearance.chatActionButtonTextColor();
        BoomtownChat.sharedInstance().chatActionButtonBorderColor   = supportSDK.appearance.chatActionButtonBorderColor();
        chatUploadButton.setColorFilter(supportSDK.appearance.chatSendButtonEnabledColor());
    }

    private int colorFromValue(int value) {
        return getResources().getColor(value);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
        mMenuItemActionDone = menu.findItem(R.id.action_done);
        mMenuItemActionDone.setVisible(false);
        mMenuItemActionResolve = menu.findItem(R.id.action_resolve);
        mMenuItemEndCall = menu.findItem(R.id.action_end_call);

        styleMenuItem(mMenuItemActionDone);
        styleMenuItem(mMenuItemActionResolve);
        styleMenuItem(mMenuItemEndCall);

        if (supportSDK != null) {
            mMenuItemEndCall.setVisible(supportSDK.isConnected());
        }
        if ( isReadOnly ) {
            mMenuItemActionResolve.setVisible(false);
        }
    }


    private void styleMenuItem(MenuItem menuItem) {
        SpannableString spanString = new SpannableString(menuItem.getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(supportSDK.appearance.navigationBarIconColor()), 0,     spanString.length(), 0); //fix the color to white
        menuItem.setTitle(spanString);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            if (webViewShowing) {
                hideWebView();
                return true;
            }
        }
        if (id == R.id.action_resolve) {
            if (webViewShowing) {
                hideWebView();
            }
            resolveIssueIfPossible();
            return true;
        }
        if (id == R.id.action_end_call) {
            if (webViewShowing) {
                hideWebView();
            }
            endScreenSharing();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnectError(String message) {
        super.onConnectError(message);
        warnJoinFailureAndReconnect(message);
    }


    @Override
    public void onNotAuthenticate() {
        super.onNotAuthenticate();
        warnJoinFailureAndReconnect(getString(R.string.warn_unable_to_connect_to_chat_server));
    }


    @Override
    public void onJoinRoom() {
        super.onJoinRoom();
        FragmentActivity activity = getActivity();
        if ( activity != null ) {
            supportSDK.getPermissions(activity);
        }
    }


    @Override
    public void onJoinRoomNoResponse() {
        super.onJoinRoomNoResponse();
//        warn("", "No response when joining room");
        warnJoinFailureAndReconnect(getString(R.string.warn_no_response_when_joining_chat_room));
    }


    @Override
    public void onJoinRoomFailed(String reason) {
        super.onJoinRoomFailed(reason);
//        warn("", "Failed to join room");
        warnJoinFailureAndReconnect(getString(R.string.warn_unable_to_join_chat_room));
    }

    @Override
    public void onReceiveMessage(BoomtownChatMessage message) {
        super.onReceiveMessage(message);
        if ( mIssue != null ) {
            mSupportButton.refreshIssue(mIssue.id);
        }
    }

    private void warnJoinFailureAndReconnect(String title) {
        final FragmentActivity activity = getActivity();
        if ( activity != null ) {
            activity.runOnUiThread(() -> {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

                alertDialog.setTitle(title);
                alertDialog.setMessage(getString(R.string.text_would_you_like_to_try_to_join_again));
                alertDialog.setPositiveButton(getString(R.string.text_yes), (dialog, whichButton) -> {
                    dialog.dismiss();
                    connect();
                });
                alertDialog.setNegativeButton(getString(R.string.text_no), (dialog, whichButton) -> dialog.dismiss());
                AlertDialog b = alertDialog.create();
                b.show();
            });
        }
    }

    @Override
    public void configureChat() {
        if ( mIssue != null ) {
            if ( mIssue.reference_num!=null && chatTitle==null ) {
                String title = String.format("%s", mIssue.reference_num);
                mSupportButton.setTitle(title);
            }

            JSONObject mXmppInfo = SupportSDK.extractXmppInformation(mIssue.xmpp_data, supportSDK.getKey());
            if ( mXmppInfo != null ) {
                setXmppInfo(mXmppInfo);
                if (mCommId != null) {
                    if ( isReadOnly ) {
                        setup();
                        if ( chatTitle != null ) {
                            mSupportButton.setTitle(chatTitle);
                        }
                        getTranscripts(mCommId);
                    } else {
                        commGet(mCommId);
                    }
                }
                return;
            }
        }
        warn(getString(R.string.app_name), getString(R.string.warn_unable_to_obtain_chat_server_information));
    }


    private void getTranscripts(String comm_id) {
        String me = "members_users:" + supportSDK.memberUserID;
        senderId = me;
        BoomtownChat.sharedInstance().me = me;
        BoomtownChat.sharedInstance().ignoreParticipantStatus = true;
        JSONObject transcripts = mIssue.transcripts.optJSONObject(comm_id);
        if ( transcripts != null ) {
            Iterator<String> keys = transcripts.keys();
            while ( keys.hasNext() ) {
                String key = keys.next();
                BoomtownChatMessage chatMessage = new BoomtownChatMessage();
                JSONObject transcript = transcripts.optJSONObject(key);
                chatMessage.populateFromTranscript(transcript);
                String fromKey = transcript.optString("from_key");
                onReceiveMessage(chatMessage);
            }
        }
    }


    public void joinVideoChat(String callId) {
        if ( supportSDK.supportScreenShareEnabled ) {
            displayVideoChoices(callId);
        } else {
            AlertDialog.Builder unavailableBuilder = new AlertDialog.Builder(mContext);
//            unavailableBuilder.setTitle(R.string.text_unavailable);
            unavailableBuilder.setMessage(R.string.warn_screen_share_unavailable);
            unavailableBuilder.setNeutralButton(mContext.getString(R.string.text_ok),
                    (unavailableDialog, which) -> unavailableDialog.dismiss());
            unavailableBuilder.show();
        }
    }


    private static final int ANDROID_VERSION_10_SDK = 29;

    private void warnNoScreenSharingAvailableIfNecessary() {
        String message = "";
        if ( !supportSDK.supportScreenShareEnabled ) {
            return;
        }
        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ) {
            message = getString(R.string.warn_screen_sharing_device_too_old);
        }
        else if ( Build.VERSION.SDK_INT >= ANDROID_VERSION_10_SDK ) {
            message = getString(R.string.warn_screen_sharing_not_yet_supported);
        }
        final String text = message;
        this.mParent.runOnUiThread(() -> Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show());

    }


    public void displayVideoChoices(final String callId) {
        List<String> availableItems = new ArrayList<>();
        if ( supportSDK.supportScreenShareEnabled &&
             Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP &&
                Build.VERSION.SDK_INT<ANDROID_VERSION_10_SDK ) {
            availableItems.add(getResources().getString(R.string.label_share_screen));
        } else {
            warnNoScreenSharingAvailableIfNecessary();
        }
        availableItems.add(getResources().getString(R.string.label_video_chat));
        availableItems.add(getResources().getString(R.string.label_cancel));

        CharSequence[] theItems = new CharSequence[availableItems.size()];
        for ( int n=0; n<availableItems.size(); n++ ) {
            theItems[n] = availableItems.get(n);
        }
        final CharSequence[] items = theItems;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select");
        builder.setItems(items, (dialog, item) -> {
            dialog.dismiss();
            String label = (String) items[item];
            if ( label.equalsIgnoreCase(getResources().getString(R.string.label_share_screen))) {
                isScreenShare = true;
                startScreenSharing(callId);
            } else if ( label.equalsIgnoreCase(getResources().getString(R.string.label_video_chat))) {
                isScreenShare = false;
                commGetVideo(callId);
            }

        });
        builder.show();
    }


    public void startScreenSharing(String callId) {
        supportSDK.callId = callId;
        isScreenShare = true;
        FragmentActivity activity = getActivity();
        if ( activity != null ) {
             supportSDK.requestScreenCapturePermission(activity);
        }
    }


    public void endScreenSharing() {
//        Intent intent = new Intent();
//        intent.setAction(BTSessionManager.SESSION_END_SCREEN_SHARE);
//        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        if ( supportSDK!=null && supportSDK.sessionManager!=null ) {
            supportSDK.sessionManager.endSession();
        }
        mMenuItemEndCall.setVisible(false);
    }


    public void startVideoChat(String callId, String accessToken) {
        if ( callId==null || callId.isEmpty() || accessToken==null || accessToken.isEmpty()) {
//            String sb = getString(com.goboomtown.core.R.string.no_active_call) + " " +
//                    getString(com.goboomtown.core.R.string.msg_unable_to_join_conference);
//            showErrorMessage(getString(R.string.unable_to_start_boomtown_video_support), sb);
            return;
        }
        Intent intent = new Intent(mContext, VideoActivity.class);
        intent.putExtra(VideoActivity.kCallId, callId);
        intent.putExtra(VideoActivity.kAccessToken, accessToken);
        intent.putExtra(VideoActivity.kUsername, "Support SDK");
        intent.putExtra(VideoActivity.kHostname, supportSDK.hostname());
        if ( isCallable(intent) ) {
            startActivity(intent);
        }
    }


    private boolean isCallable(Intent intent) {
        if ( mContext == null ) {
            return false;
        }
        List<ResolveInfo> list = mContext.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    private void resolveIssueIfPossible() {

        String uri = String.format("%s/issues/%s", SupportSDK.kSDKV1Endpoint, mIssue.id);

        supportSDK.get(uri, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                exitIssue();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean success = false;
                BTConnectIssue issue = null;
                JSONObject jsonObject;
                try {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String responseBodyString = responseBody.string();
                        jsonObject = new JSONObject(responseBodyString);
                        success = jsonObject.optBoolean("success");
                        if (success) {
                            JSONArray results = jsonObject.optJSONArray("results");
                            if (results != null && results.length() > 0) {
                                Log.d(TAG, "onResponse");
                                JSONObject issueJSON;
                                try {
                                    issueJSON = results.getJSONObject(0);
                                    issue = new BTConnectIssue(issueJSON);
                                    success = true;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (issue != null) {
                                    if ( issue.status < BTConnectIssue.RESOLVED ) {
                                        resolveIssue();
                                    } else {
                                        exitIssue();
                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!success) {
                    exitIssue();
                }
            }
        });
    }


    private void cancelIssue() {
        if ( mIssue == null ) {
            return;
        }

        endScreenSharing();

        showProgressWithMessage(getString(R.string.cancelling_issue));

        String uri = String.format("%s/issues/cancel/%s", SupportSDK.kSDKV1Endpoint, mIssue.id);

        JSONObject params = new JSONObject();
        try {
            params.put("members_users_id", supportSDK.memberUserID);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        supportSDK.post(uri, params, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                hideProgress();
                warn(getString(R.string.app_name), getString(R.string.cancel_failed));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                hideProgress();
                JSONObject jsonObject = SupportSDK.successJSONObject(response);
                if ( jsonObject != null ) {
                    supportSDK.rateableIssueId = mIssue.id;
                    exitIssue();
                }
             }
        });
    }


    private void resolveIssue() {
        if ( mIssue == null ) {
            return;
        }

        endScreenSharing();

        showProgressWithMessage(getString(R.string.resolving_issue));

        String uri = String.format("%s/issues/resolve/%s", SupportSDK.kSDKV1Endpoint, mIssue.id);

        JSONObject params = new JSONObject();
        try {
            params.put("resolution", BTConnectIssue.kIssueResolutionCompleted);
            params.put("members_users_id", supportSDK.memberUserID);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        supportSDK.post(uri, params, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                hideProgress();
//                warn(getString(R.string.app_name), getString(R.string.resolve_failed));
                cancelIssue();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                boolean success;

                hideProgress();
                JSONObject jsonObject = SupportSDK.jsonObject(response);
                if ( jsonObject != null ) {
                    success = jsonObject.optBoolean("success", false);
                    if ( success ) {
                        supportSDK.rateableIssueId = mIssue.id;
                        exitIssue();
                    } else {
//                        String message = getString(R.string.resolve_failed);
                        String reason = jsonObject.optString("message");
                        if ( reason != null ) {
//                            message += ": " + reason;
//                            warn(getString(R.string.app_name), message);
                            cancelIssue();
                        }
                    }
                }
            }
        });
    }


    private void exitIssue() {
//        mSupportButton.refreshIssue(mIssue.id);
        BTConnectIssue.clearCurrentIssue(mContext);
        mIssue = null;
        FragmentActivity activity = getActivity();
        if ( activity != null ) {
            activity.runOnUiThread(() -> mSupportButton.removeChat());
        }
    }


    /**
     * Retrieve chat information.
     */
    public void commGet(final String comm_id) {
        String uri = String.format("%s/chat/get/%s", SupportSDK.kSDKV1Endpoint, comm_id);

        JSONObject params = new JSONObject();
        try {
             params.put("members_users_id", supportSDK.memberUserID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        supportSDK.post(uri, params, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                warn(getString(R.string.error_unable_to_retrieve_chat_information), e.getLocalizedMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody responseBody = response.body();
                if ( responseBody != null ) {
                    String responseBodyString = responseBody.string();
                    JSONObject chatJSON = SupportSDK.successJSONObject(responseBodyString);
                    if (chatJSON != null) {
                        mChatRecord = new ChatRecord(chatJSON);
                        FragmentActivity activity = getActivity();
                        if ( activity != null ) {
                            activity.runOnUiThread(() -> setup());
                        }
                    } else {
                        String message = SupportSDK.failureMessageFromJSONData(responseBodyString);
                        warn(getString(R.string.error_unable_to_retrieve_chat_information), message);
                    }
                 }
            }
        });
    }


    /**
     * Retrieve chat information.
     */
    public void commGetVideo(final String comm_id) {
        String uri = String.format("%s/chat/get/%s", SupportSDK.kSDKV1Endpoint, comm_id);

        JSONObject params = new JSONObject();
        try {
            params.put("members_users_id", supportSDK.memberUserID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        supportSDK.post(uri, params, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                warn(getString(R.string.error_unable_to_retrieve_chat_information), e.getLocalizedMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                JSONObject chatJSON = SupportSDK.successJSONObject(response);
                if (chatJSON != null) {
                     ChatRecord videoChatInfo = new ChatRecord(chatJSON);
                    if ( isScreenShare ) {
                        FragmentActivity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(() -> {
                                if (getView() != null && getView().getParent() != null) {
                                    mMenuItemEndCall.setVisible(true);
                                    supportSDK.startScreenSharingSession(getActivity(), (ViewGroup) getView().getParent(), videoChatInfo.external_id, videoChatInfo.access_token);
                                }
                            });
                        }
                    } else {
                        startVideoChat(videoChatInfo.external_id, videoChatInfo.access_token);
                    }
                }
            }
        });
    }


    /**
     * Enter a chat.
     */
    public void commEnter(final String comm_id) {
        String uri = String.format("%s/chat/enter/%s", SupportSDK.kSDKV1Endpoint, comm_id);

        JSONObject params = new JSONObject();
        try {
            params.put("members_users_id", supportSDK.memberUserID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        supportSDK.post(uri, params, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {

                JSONObject jsonObject = SupportSDK.successJSONObject(response);
                if (jsonObject != null) {
                    JSONArray resultsArray = jsonObject.optJSONArray("results");
                    if (resultsArray != null) {
                        JSONObject chatJSON = resultsArray.optJSONObject(0);
                        if (chatJSON != null) {
                            mCommEntered = true;
                        }
                    }
                }
            }
        });
    }


    /**
     * Exit a chat.
     */
    public void commExit(final String comm_id) {
        String uri = String.format("%s/chat/exit/%s", SupportSDK.kSDKV1Endpoint, comm_id);

        JSONObject params = new JSONObject();
        try {
            params.put("members_users_id", supportSDK.memberUserID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        supportSDK.post(uri, params, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {

                JSONObject jsonObject = SupportSDK.successJSONObject(response);
                if (jsonObject != null) {
                    JSONArray resultsArray = jsonObject.optJSONArray("results");
                    if (resultsArray != null) {
                        JSONObject chatJSON = resultsArray.optJSONObject(0);
                        if (chatJSON != null) {
                            mCommEntered = false;
                        }
                    }
                }
            }
        });
    }


    public void upload() {
        switch (mUploadType) {
            case UPLOAD_TYPE_NONE:
                commPutFile(mCommId, mImage);
                break;
            case UPLOAD_TYPE_AVATAR:
                showProgressWithMessage("Uploading photo");
//                BoomtownAPI.sharedInstance().apiImageUpload(getApplicationContext(), mImage, mImageType);
                break;

            case UPLOAD_TYPE_CHAT:
            default:
                break;
        }
    }


    public void commPutFile(String commId, Bitmap image) {
        String url = String.format("%s/chat/filePut/%s", SupportSDK.kSDKV1Endpoint, commId);

        JSONObject params = new JSONObject();
        try {
            params.put("members_users_id", supportSDK.memberUserID);
            params.put("file_tag", "attachment");
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        supportSDK.post(url, params, image, "image", new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                hideProgress();
                warn(getString(R.string.app_name), getString(R.string.warn_unable_to_add_attachment));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {

                hideProgress();
                JSONObject jsonObject = SupportSDK.successJSONObject(response);
                if ( jsonObject != null ) {
                    if ( jsonObject.has("results") ) {
                        try {
                            JSONArray resultsArray = jsonObject.getJSONArray("results");
                            if ( resultsArray.length() > 0 ) {
                                JSONObject resultJSON = resultsArray.getJSONObject(0);

                                String payload = resultJSON.toString();
                                if ( !payload.isEmpty() ) {
                                    Log.d(TAG, payload);
                                    if (mInRoom) {
                                        BoomtownChat.sharedInstance().sendGroupchatMessage(payload, true);
                                    }

                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        warn(getString(R.string.app_name), getString(R.string.warn_unable_to_add_attachment));
                    }
                }
            }
        });
    }

    public String kbUrl(final Context applicationContext, String kb_id) {
        return kb_id; //String.format("%s?id=%s&authtoken=%s",BoomtownArticlePage, kb_id, authToken);
    }

    public void retrieveKB(String id) {
        String url = SupportSDK.kSDKV1Endpoint + "/kb/get?id=" + id;

        supportSDK.get(url, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                boolean success = false;

                JSONObject jsonObject = SupportSDK.successJSONObject(response);
                if ( jsonObject != null ) {
                    if ( jsonObject.has("results") ) {
                        try {
                            JSONArray resultsJSON = jsonObject.getJSONArray("results");
                            JSONObject result = resultsJSON.getJSONObject(0);
                            success = true;
                            String title = result.getString("title");
                            String body = result.getString("body");
                            String html = "<html><body>" + body + "</body></html>";
                            Intent intent = new Intent(mContext, KBActivity.class);
                            intent.putExtra(KBActivity.ARG_URL, "");
                            intent.putExtra(KBActivity.ARG_HTML, html);
                            intent.putExtra(KBActivity.ARG_TITLE, title);
                            startActivity(intent);
                        } catch (JSONException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                }
                if ( !success ) {
                    warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
                }
            }
        });
    }


    public void warn(final String title, final String message) {
        this.mParent.runOnUiThread(() -> Toast.makeText(getContext(), title + ": " + message, Toast.LENGTH_LONG).show());
    }

}
