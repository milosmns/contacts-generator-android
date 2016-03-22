
package me.angrybyte.contactsgenerator.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import me.angrybyte.contactsgenerator.ProgressActivity;
import me.angrybyte.contactsgenerator.R;
import me.angrybyte.contactsgenerator.api.Gender;
import me.angrybyte.contactsgenerator.api.GeneratorStats;
import me.angrybyte.contactsgenerator.parser.data.Person;

public class GeneratorService extends Service implements ServiceApi {

    public static final String TAG = GeneratorService.class.getSimpleName();
    public static final int NOTIFICATION_ID = 1475369;

    private Person mLastGenerated;
    private Handler mHandler;
    private GeneratorStats mStats;
    private GeneratorThread mGenerator;
    private NotificationManager mNotificationManager;
    private GeneratorServiceBinder mBinder;
    private OnGenerateResultListener mResultListener;
    private OnGenerateProgressListener mProgressListener;
    private boolean mIsForceStopped;
    private boolean mIsGenerating;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating " + TAG + "...");
        mBinder = new GeneratorServiceBinder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
        mStats = null;
        mBinder = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mLastGenerated = null;
        mResultListener = null;
        mProgressListener = null;
        mNotificationManager = null;
    }

    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        builder.setSmallIcon(R.drawable.ic_stat_generator);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.generating));
        builder.setPriority(NotificationCompat.PRIORITY_LOW);

        Intent resultIntent = new Intent(this, ProgressActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ProgressActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void hideNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    /* Local API */

    /**
     * Called by the generator thread just before it dies.
     *
     * @param forced Whether the thread was stopped on purpose (called {@link #stopGenerating()}), or naturally (finished generating)
     */
    public void onGeneratingFinished(boolean forced) {
        Log.d(TAG, "Generating " + (forced ? " force-" : "") + "finished");
        hideNotification();
        mGenerator.clear();
        mGenerator = null;
        mIsGenerating = false;
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

        String format = "Starting generate sequence. Generating %s, gender: %s, with photos: %s";
        Log.i(TAG, String.format(format, howMany, gender, withPhotos));

        mGenerator = new GeneratorThread(mHandler, mProgressListener, mResultListener, this, howMany, withPhotos, gender);
        mStats = mGenerator.getStats();
        mIsGenerating = true;
        showNotification();
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
        mIsForceStopped = true;
        if (mGenerator != null && !mGenerator.isInterrupted()) {
            mGenerator.interrupt();
        }
    }

    @Override
    public boolean isForceStopped() {
        return mIsForceStopped;
    }

    @Nullable
    @Override
    public GeneratorStats getStats() {
        return mStats;
    }

}
