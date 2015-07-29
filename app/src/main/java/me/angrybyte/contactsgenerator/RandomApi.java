
package me.angrybyte.contactsgenerator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RandomApi {

    private static final String TAG = RandomApi.class.getSimpleName();
    private static final String URL_TEMPLATE = "http://api.randomuser.me/?results=%s&gender=%s&key=%s";

    private Context mContext;
    private String mApiKey;

    public RandomApi(Context context) {
        super();
        mContext = context;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Random API object created.");
        }
    }

    /**
     * Reads a String from the given raw resource.
     * 
     * @param context Which context to use (can be app context)
     * @param resId Which raw resource to use
     * @return Contents of the raw resource, or an empty String if something fails
     */
    public String readRawTextFile(@NonNull Context context, @RawRes int resId) {
        InputStream inputStream = context.getResources().openRawResource(resId);

        InputStreamReader inputReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputReader);
        StringBuilder text = new StringBuilder();
        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            close(bufferedReader);
            close(inputReader);
        } catch (IOException e) {
            close(bufferedReader);
            close(inputReader);
            return null;
        }
        return text.toString();
    }

    /**
     * @return The current API security key in use.
     */
    public String getApiKey() {
        if (mApiKey == null) {
            // we are building with another API key, this one in the repo is public
            mApiKey = readRawTextFile(mContext, R.raw.api_key);
        }
        return mApiKey;
    }

    /**
     * Stream closer - closes the given {@link Closeable} instance.
     * 
     * @param closeable Instance of a closeable object
     */
    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

}
