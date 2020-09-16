
package me.angrybyte.contactsgenerator.service;

import android.os.Binder;
import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public class GeneratorServiceBinder extends Binder {

    private WeakReference<ServiceApi> mService;

    public GeneratorServiceBinder(@NonNull ServiceApi service) {
        super();
        mService = new WeakReference<>(service);
    }

    @NonNull
    public ServiceApi getService() {
        return mService.get();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        clear();
    }

    public void clear() {
        if (mService != null) {
            mService.clear();
            mService = null;
        }
    }

}
