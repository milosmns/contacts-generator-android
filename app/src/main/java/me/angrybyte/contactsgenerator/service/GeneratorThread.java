
package me.angrybyte.contactsgenerator.service;

import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import me.angrybyte.contactsgenerator.api.ContactOperations;
import me.angrybyte.contactsgenerator.api.Gender;
import me.angrybyte.contactsgenerator.api.Operations;
import me.angrybyte.contactsgenerator.parser.data.Person;

public class GeneratorThread extends Thread {

    private static final String TAG = GeneratorThread.class.getSimpleName();

    private Handler mHandler;
    private GeneratorService mService;
    private OnGenerateResultListener mResultListener;
    private OnGenerateProgressListener mProgressListener;
    private int mHowMany = 0;
    private int mTotalGenerated = 0;
    private boolean mWithPhotos = true;

    @Gender
    private String mGender;

    public GeneratorThread(@NonNull Handler handler, @NonNull OnGenerateProgressListener progressListener,
            @NonNull OnGenerateResultListener resultListener, @NonNull GeneratorService service, @IntRange(from = 0) int howMany,
            boolean withPhotos, @Gender String gender) {
        super();
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

        final List<Person> persons = operations.getPersons(mHowMany, mGender);
        final int listSize = persons.size();

        if (listSize != mHowMany) {
            throw new RuntimeException("Requested number differs from actual list size");
        }

        for (int i = 0; i < listSize; i++) {
            final int finalI = i;
            Person current = persons.get(i);

            if (mWithPhotos) {
                Bitmap bitmap = operations.fetchImage(current);
                if (bitmap != null) {
                    current.setImage(bitmap);
                }
            }

            boolean stored = storeContact(contacts, current);
            if (stored) {
                mTotalGenerated++;
                mService.setLastGenerated(current);
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mProgressListener != null) {
                        mProgressListener.onGenerateProgress((float) finalI / (float) listSize, finalI, mTotalGenerated);
                    }
                }
            });
        }

        notifyFinished(false);

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
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(GeneratorThread.class.getSimpleName(), "Failed to store contact", e);
            return false;
        }
    }

    /**
     * Notifies the listener (if available) that the generating sequence is finished. Also notifies the service that thread is dying so that
     * it can clear the resources, most likely by calling {@link #clear()}.
     * 
     * @param forcedStop Whether this thread was stopped manually (by calling {@link #interrupt()}), or naturally (generating finished)
     */
    private void notifyFinished(final boolean forcedStop) {
        if (forcedStop) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mResultListener != null) {
                    mResultListener.onGenerateResult(mHowMany, mTotalGenerated, forcedStop);
                }
                mService.onGeneratingFinished(forcedStop);
            }
        });
    }

    @Override
    public void interrupt() {
        Log.d(TAG, "Requested interrupt");
        notifyFinished(true);
        super.interrupt();
    }

    public void clear() {
        mHandler = null;
        mProgressListener = null;
        mResultListener = null;
        mService = null;
    }

}
