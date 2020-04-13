package com.goboomtown.supportsdk.activity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.util.Log;

import com.goboomtown.supportsdk.R;
import com.goboomtown.video.AVChatSessionFragment;
import com.goboomtown.video.BTParticipant;
import com.twilio.video.TwilioException;


/**
 * Created by larry on 2016-08-04.
 *
 * @author lborsato
 * @author fbeachler
 */
public class VideoActivity extends com.goboomtown.video.VideoActivity {
    private static final String TAG = VideoActivity.class.getSimpleName();


    @Override
    public void didFailToCreateSessionWithError(Error error) {
        Log.d(TAG, error.getMessage());
        warn(error.getMessage());
    }


    private void warn(final String message) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            builder.setTitle(null);
            builder.setMessage(message);
            builder.setCancelable(false);

            builder.setPositiveButton("OK", null);
            builder.setPositiveButton(getString(R.string.text_ok),
                    (dialog, which) -> {
                        dialog.dismiss();
                        didCloseSession();
                    });

            final AlertDialog dlg = builder.create();
            if (dlg != null) {
                dlg.show();
            }
        });
    }

    @Override
    public void didCloseSession() {
        setResult(RESULT_OK, null);
        super.didCloseSession();
    }

    @Override
    public void didJoinRoom(BTParticipant participant) {
        super.didJoinRoom(participant);
        avChatSessionFragment.mVideoClient.switchCamera("BACK");
        avChatSessionFragment.cameraState = AVChatSessionFragment.VideoChatCameraState.BACK_CAMERA;
        avChatSessionFragment.mVideoClient.muteCamera(false);
        avChatSessionFragment.mVideoClient.muteMicrophone(false);
        avChatSessionFragment.mVideoClient.muteSpeaker(false);
    }


    @Override
    public void didFailToJoinRoomWithError(Error error) {

    }

    @Override
    public void didFailToConnect(TwilioException e) {

    }

//    /**
//     * Call {@link NotificationManagerCompat#cancel(String, int)} for given notifications.
//     *
//     * @param notifId notifId to cancel
//     */
//    protected void gcNotifications(Integer notifId) {
////        AsyncTask<Integer, Void, Void> gcNotifsTask = new com.goboomtown.core.activity.BaseActivity.GCNotifAsyncTask(new WeakReference<>(getApplicationContext()));
////        gcNotifsTask.execute(notifId);
//    }
//
//    public void showErrorMessage(final String titleToShow,
//                                 final String msgToShow) {
////        runOnUiThread(() -> DialogHelper.showAlertDialog(VideoActivity.this, titleToShow, msgToShow));
//    }

    @Override
    public void saveImageToSysGallery(ContentResolver contentResolver, byte[] bytes) {
//        Utils.saveImageToSysGallery(contentResolver, bytes, "video chat snapshot", "snapshot from video chat");
    }


    public void commEnter(String callId) {
        super.commEnter(callId);

//        String uri = String.format("%s/%s/chat/enter/%s", hostname, SupportSDK.kSDKV1Endpoint, callId);
//
//        JSONObject params = new JSONObject();
//        RestClient client = new RestClient(new WeakReference<>(getBaseContext()));
//        client.post(getContext(), uri, params, new Callback() {
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                boolean success = false;
//
//                JSONObject jsonObject = SupportSDK.successJSONObject(response);
//                if ( jsonObject != null ) {
//                    success = true;
//                }
//            }
//        });
    }


    public void commExit(String callId) {
        super.commExit(callId);

//        String uri = String.format("%s/%s/chat/exit/%s", hostname, SupportSDK.kSDKV1Endpoint, callId);
//
//        JSONObject params = new JSONObject();
//        try {
//            params.put("duration_start", "0");
//            params.put("duration_end", String.valueOf(elapsedTimeInSeconds));
//        } catch (JSONException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        }
//        RestClient client = new RestClient(new WeakReference<>(getBaseContext()));
//        client.post(uri, params, new Callback() {
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                boolean success = false;
//
//                JSONObject jsonObject = SupportSDK.successJSONObject(response);
//                if ( jsonObject != null ) {
//                    success = true;
//                }
//            }
//        });
    }


}