package com.goboomtown.supportsdk.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.goboomtown.supportsdk.R;
import com.goboomtown.video.AVChatSessionFragment;
import com.goboomtown.video.BTParticipant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


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
        if ( error!=null && error.getMessage()!=null ) {
            Log.d(TAG, error.getMessage());
            warn(error.getMessage());
        }
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
    public void didFailToConnect(int code, String explanation) {

    }


    @Override
    public void saveImageToSysGallery(ContentResolver contentResolver, byte[] bytes) {
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            Toast.makeText(this, getString(R.string.msg_error_no_photo_save_permission), Toast.LENGTH_LONG).show();
            return;
        }
        savePhoto(getApplicationContext(), bytes);
    }


    private File savePhoto(Context context, byte[] bytes){
        File pictureFileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!pictureFileDir.exists()) {
            boolean isDirectoryCreated = pictureFileDir.mkdirs();
            if(!isDirectoryCreated)
                Log.i("TAG", "Can't create directory to save the image");
            return null;
        }
        String filename = pictureFileDir.getPath() +File.separator+ System.currentTimeMillis()+".jpg";
        File pictureFile = new File(filename);
//        Bitmap bitmap = getBitmapFromView(drawView);
        try {
            pictureFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            oStream.write(bytes);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
            oStream.flush();
            oStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TAG", "There was an issue saving the image.");
        }
        scanGallery(context, pictureFile.getAbsolutePath());
        return pictureFile;
    }

    private void scanGallery(Context cntx, String path) {
        try {
            MediaScannerConnection.scanFile(cntx, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("TAG", "There was an issue scanning gallery.");
        }
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