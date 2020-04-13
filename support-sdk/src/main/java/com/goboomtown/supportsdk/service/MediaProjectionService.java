package com.goboomtown.supportsdk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MediaProjectionService extends Service {
    public MediaProjectionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
