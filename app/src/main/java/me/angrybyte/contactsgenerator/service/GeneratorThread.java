
package me.angrybyte.contactsgenerator.service;

import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Log;

import me.angrybyte.contactsgenerator.api.ContactOperations;
import me.angrybyte.contactsgenerator.api.Gender;
import me.angrybyte.contactsgenerator.api.Operations;
import me.angrybyte.contactsgenerator.parser.data.Person;

import java.util.List;

public class GeneratorThread extends Thread {

    private Handler mHandler;
    private GeneratorService mService;
    private OnGenerateResultListener mResultListener;
    private OnGenerateProgressListener mProgressListener;
    private int mHowMany;
    private boolean mWithPhotos;

    @Gender
    private String mGender;

    public GeneratorThread(@NonNull Handler handler, @NonNull OnGenerateProgressListener progressListener,
            @NonNull OnGenerateResultListener resultListener, @NonNull GeneratorService service, @IntRange(from = 0) int howMany,
            boolean withPhotos, @Gender String gender) {
        super();

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
        int totalGenerated = 0;

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
                totalGenerated++;
                mService.setLastGenerated(current);
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mProgressListener != null) {
                        mProgressListener.onGenerateProgress((float) finalI / (float) listSize);
                    }
                }
            });
        }

        final int finalTotal = totalGenerated;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mResultListener != null) {
                    mResultListener.onGenerateResult(listSize, finalTotal);
                }
                mService.onGeneratingFinished();
            }
        });

        persons.clear();
        // noinspection UnusedAssignment
        operations = null;
        // noinspection UnusedAssignment
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

    public void clear() {
        mHandler = null;
        mProgressListener = null;
        mResultListener = null;
        mService = null;
    }

}
