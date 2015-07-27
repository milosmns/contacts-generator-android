
package me.angrybyte.contactsgenerator;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RandomApi {

    private static final String TAG = RandomApi.class.getSimpleName();
    private static final String URL_TEMPLATE = "http://api.randomuser.me/?results=%s&gender=%s&key=%s";

    public RandomApi(Context context) {
        super();
        // we are actually building with another API key, this one is public
        String apiKey = readRawTextFile(context, R.raw.api_key);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "API Key = " + apiKey);
        }
    }

    private String readRawTextFile(Context context, int resId) {
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

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

}
