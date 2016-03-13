
package me.angrybyte.contactsgenerator.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.Log;

import me.angrybyte.contactsgenerator.api.Gender;
import me.angrybyte.contactsgenerator.parser.data.Person;

public class GeneratorService extends Service implements ServiceApi {

    public static final String TAG = GeneratorService.class.getSimpleName();

    private Person mLastGenerated;
    private Handler mHandler;
    private GeneratorThread mGenerator;
    private GeneratorServiceBinder mBinder;
    private OnGenerateResultListener mResultListener;
    private OnGenerateProgressListener mProgressListener;
    private boolean mIsGenerating;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating " + TAG + "...");
        mBinder = new GeneratorServiceBinder(this);
        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting " + TAG + " with intent " + String.valueOf(intent));
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
        Log.d(TAG, "Destroying " + TAG + "...");
        mBinder = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mLastGenerated = null;
        mProgressListener = null;
        mResultListener = null;
    }

    /* Local API */

    /**
     * Called by the generator thread just before it dies.
     *
     * @param forced Whether the thread was stopped on purpose (called {@link #stopGenerating()}), or naturally (finished generating)
     */
    public void onGeneratingFinished(boolean forced) {
        Log.d(TAG, "Generating " + (forced ? " force-" : "") + "finished");
        mGenerator.clear();
        mGenerator = null;
    }

    public void setLastGenerated(@Nullable Person person) {
        mLastGenerated = person;
    }

    /* Service API */

    @Override
    public boolean isGenerating() {
        return mIsGenerating;
    }

    @Override
    public boolean generate(@IntRange(from = 0) int howMany, boolean withPhotos, @Gender String gender) {
        if (mProgressListener == null || mResultListener == null) {
            Log.e(TAG, "Cannot generate, no listeners attached");
            return false;
        }

        if (mGenerator != null && !mGenerator.isInterrupted() && mGenerator.isAlive()) {
            Log.e(TAG, "Cannot generate, already generating");
            return false;
        } else if (mGenerator != null) {
            // probably not needed
            stopGenerating();
        }

        mGenerator = new GeneratorThread(mHandler, mProgressListener, mResultListener, this, howMany, withPhotos, gender);
        mGenerator.start();

        return true;
    }

    @Nullable
    @Override
    public Person getLastGeneratedPerson() {
        return mLastGenerated;
    }

    @Override
    public void setOnGenerateProgressListener(@Nullable OnGenerateProgressListener listener) {
        mProgressListener = listener;
    }

    @Override
    public void setOnGenerateResultListener(@Nullable OnGenerateResultListener listener) {
        mResultListener = listener;
    }

    @Override
    public void stopGenerating() {
        if (mGenerator != null && !mGenerator.isInterrupted()) {
            mGenerator.interrupt();
        }
    }

}
