
package me.angrybyte.contactsgenerator.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class GeneratorService extends Service implements ServiceApi {

    public static final String TAG = GeneratorService.class.getSimpleName();

    private GeneratorServiceBinder mBinder;
    private boolean mInitialized;

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new GeneratorServiceBinder(this);
        Log.d(TAG, "Creating " + TAG + "...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting " + TAG + " with intent " + String.valueOf(intent));
        setInitialized();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Binding " + TAG + " with intent " + String.valueOf(intent));
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinder = null;
        Log.d(TAG, "Destroying " + TAG + "...");
    }

    /* Service API */

    @Override
    public boolean isInitialized() {
        return mInitialized;
    }

    @Override
    public void setInitialized() {
        mInitialized = true;
    }

}
