
package me.angrybyte.contactsgenerator.service;

import android.graphics.Bitmap;
import android.os.Handler;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private long mTotalStartTime = System.currentTimeMillis();
    private boolean mWithPhotos = true;
    private boolean mLocalInterrupted = false;

    @Gender
    private String mGender;

    public GeneratorThread(@NonNull Handler handler, @NonNull GeneratorService service, @IntRange(from = 0) int howMany,
            boolean withPhotos, @Gender String gender) {
        super();
        mStats = new GeneratorStats();
        mStats.requested = howMany;
        setName(TAG);

        mHandler = handler;
        mService = service;

        mWithPhotos = withPhotos;
        mGender = gender;
    }

    @Override
    public void run() {
        super.run();

        mTotalStartTime = System.currentTimeMillis();

        Operations operations = new Operations(mService);
        ContactOperations contacts = new ContactOperations(mService);

        List<Person> persons = new ArrayList<>();
        try {
            persons = operations.getPersons(mStats.requested, mGender);
        } catch (Exception e) {
            // most likely a thread interrupted exception
            Log.e(TAG, "Cannot download person list", e);
        }

        if (isInterrupted()) {
            return;
        }

        final int listSize = persons.size();

        if (listSize != mStats.requested) {
            Log.e(TAG, "Requested number differs from actual list size");
            notifyFinished(false);
            return;
        }

        for (int i = 0; i < listSize; i++) {
            long contactStartTime = System.currentTimeMillis();

            if (isInterrupted()) {
                break;
            }

            final int finalI = i;
            Person current = new Person(persons.get(i));

            if (mWithPhotos) {
                Bitmap bitmap = operations.fetchImage(current);
                if (bitmap != null) {
                    current.setImage(bitmap);
                }
            }

            if (isInterrupted()) {
                break;
            }

            boolean stored = storeContact(contacts, current);
            if (stored) {
                mStats.generated++;

                if (current.getAppGender().equalsIgnoreCase(Operations.MALE)) {
                    mStats.males++;
                } else {
                    mStats.females++;
                }

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
                            if (mService != null) {
                                float progressFraction = (float) (finalI + 1) / (float) listSize;
                                mService.onGenerateProgress(progressFraction, finalI + 1, mStats.generated);
                            }
                        }
                    });
                }
            }

            // free up the space
            persons.set(i, null);

            long contactTime = System.currentTimeMillis() - contactStartTime;
            if (mStats.shortestContactTime == 0 || mStats.longestContactTime < contactTime) {
                mStats.longestContact = current.getDisplayName();
                mStats.longestContactTime = contactTime;
            }

            if (mStats.shortestContactTime == 0 || mStats.shortestContactTime > contactTime) {
                mStats.shortestContact = current.getDisplayName();
                mStats.shortestContactTime = contactTime;
            }
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
        mStats.totalTime = System.currentTimeMillis() - mTotalStartTime;
        if (mStats.generated != 0) {
            // noinspection RedundantCast
            mStats.averageTimePerContact = ((float) mStats.totalTime) / ((float) mStats.generated);
        }

        synchronized (this) {
            if (mHandler != null) {
                if (forcedStop) {
                    mHandler.removeCallbacksAndMessages(null);
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mService != null) {
                            mService.onGenerateResult(mStats, forcedStop);
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
        // and they check the interrupted flag by using Thread.interrupted() [which resets the flag] instead of
        // Thread.isInterrupted() [which does not]. so.. not 'OK'.
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
        Log.d(TAG, "Cleaning up " + TAG + "...");
        mHandler = null;
        mService = null;
    }

}
