
package me.angrybyte.contactsgenerator.service;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.angrybyte.contactsgenerator.api.ContactOperations;
import me.angrybyte.contactsgenerator.api.Gender;
import me.angrybyte.contactsgenerator.api.GeneratorStats;
import me.angrybyte.contactsgenerator.api.Operations;
import me.angrybyte.contactsgenerator.parser.data.Person;

public class GeneratorThread extends Thread {

    private static final String TAG = GeneratorThread.class.getSimpleName();

    private Handler mHandler;
    private GeneratorStats mStats;
    private GeneratorService mService;
    private OnGenerateResultListener mResultListener;
    private OnGenerateProgressListener mProgressListener;
    private int mHowMany = 0;
    private int mTotalGenerated = 0;
    private boolean mWithPhotos = true;
    private boolean mLocalInterrupted = false;

    @Gender
    private String mGender;

    public GeneratorThread(@NonNull Handler handler, @NonNull OnGenerateProgressListener progressListener,
            @NonNull OnGenerateResultListener resultListener, @NonNull GeneratorService service, @IntRange(from = 0) int howMany,
            boolean withPhotos, @Gender String gender) {
        super();
        mStats = new GeneratorStats();
        setName(TAG);

        mHandler = handler;
        mProgressListener = progressListener;
        mResultListener = resultListener;
        mService = service;

        mHowMany = howMany;
        mWithPhotos = withPhotos;
        mGender = gender;
    }

    @Override
    public void run() {
        super.run();

        Operations operations = new Operations(mService);
        ContactOperations contacts = new ContactOperations(mService);

        List<Person> persons = new ArrayList<>();
        try {
            persons = operations.getPersons(mHowMany, mGender);
        } catch (Exception e) {
            // most likely a thread interrupted exception
            Log.e(TAG, "Cannot download person list", e);
        }

        if (isInterrupted()) {
            return;
        }

        final int listSize = persons.size();

        if (listSize != mHowMany) {
            Log.e(TAG, "Requested number differs from actual list size");
            return;
        }

        for (int i = 0; i < listSize; i++) {
            if (isInterrupted()) {
                break;
            }

            final int finalI = i;
            Person current = new Person(persons.get(i));

            Log.i(TAG, "Before loading the photo, thread status is " + isInterrupted());
            if (mWithPhotos) {
                Bitmap bitmap = operations.fetchImage(current);
                if (bitmap != null) {
                    current.setImage(bitmap);
                }
            }
            Log.i(TAG, "After loading the photo, thread status is " + isInterrupted());

            if (isInterrupted()) {
                break;
            }

            boolean stored = storeContact(contacts, current);
            if (stored) {
                mTotalGenerated++;
                synchronized (this) {
                    // need to sync on this because #clear() gets called before the actual interrupt
                    if (mService != null) {
                        mService.setLastGenerated(current);
                    }
                }
            }

            if (isInterrupted()) {
                break;
            }

            synchronized (this) {
                // need to sync on this because #clear() gets called before the actual interrupt
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressListener != null) {
                                float progressFraction = (float) (finalI + 1) / (float) listSize;
                                mProgressListener.onGenerateProgress(progressFraction, finalI + 1, mTotalGenerated);
                            }
                        }
                    });
                }
            }

            // free up the space
            persons.set(i, null);
        }

        if (isInterrupted()) {
            return;
        }

        notifyFinished(false);

        if (isInterrupted()) {
            return;
        }

        persons.clear();
        // noinspection UnusedAssignment: cyclic dependency possible
        operations = null;
        // noinspection UnusedAssignment: cyclic dependency possible
        contacts = null;
    }

    private boolean storeContact(@NonNull ContactOperations operations, @NonNull Person person) {
        try {
            operations.storeContact(person);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to store contact " + String.valueOf(person), e);
            return false;
        }
    }

    public GeneratorStats getStats() {
        return mStats;
    }

    /**
     * Notifies the listener (if available) that the generating sequence is finished. Also notifies the service that thread is dying so that
     * it can clear the resources, most likely by calling {@link #clear()}.
     *
     * @param forcedStop Whether this thread was stopped manually (by calling {@link #interrupt()}), or naturally (generating finished)
     */
    private void notifyFinished(final boolean forcedStop) {
        synchronized (this) {
            if (mHandler != null) {
                if (forcedStop) {
                    mHandler.removeCallbacksAndMessages(null);
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mResultListener != null) {
                            mResultListener.onGenerateResult(mHowMany, mTotalGenerated, forcedStop);
                        }
                        if (mService != null) {
                            mService.onGeneratingFinished(forcedStop);
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean isInterrupted() {
        // if you are shocked by this.. take a look at OkIO/OkHTTP InterruptedIOException handling - it's silent
        // and they check the interrupted flag by using Thread.interrupted() which resets the flag instead of
        // Thread.isInterrupted() which does not. so.. not 'OK'.
        return super.isInterrupted() || mLocalInterrupted;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        mLocalInterrupted = true;
        Log.i(TAG, "Thread interrupt requested, current state is " + isInterrupted());
        notifyFinished(true);
    }

    public synchronized void clear() {
        mStats = null;
        mHandler = null;
        mService = null;
        mResultListener = null;
        mProgressListener = null;
    }

}
