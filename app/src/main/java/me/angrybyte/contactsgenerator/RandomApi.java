package me.angrybyte.contactsgenerator;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import me.angrybyte.contactsgenerator.parser.data.User;
import me.angrybyte.contactsgenerator.parser.json.RandomUserJsonParser;

/**
 * A clean interface to the Random API (check the README.md for more info). This class helps fetch and parse the persons information from
 * the cloud.
 */
public class RandomApi {

    private static final String TAG = RandomApi.class.getSimpleName();
    private static final String URL_TEMPLATE = "http://api.randomuser.me/?results=%s&gender=%s&key=%s";
    private static final int MAX_RESULTS = 1000;

    // defined as constants in Genders.java
    public static final String MALE = "male";
    public static final String FEMALE = "female";
    public static final String BOTH = "both";

    private Context mContext;
    private String mApiKey;
    private OkHttpClient mClient;

    public RandomApi(Context context) {
        super();
        mContext = context;
        mClient = new OkHttpClient();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Random API object created.");
        }
    }

    /**
     * Retrieves a list of {@link User} objects to populate the Contacts database with.
     *
     * @param amount The amount of contacts you would like
     * @param gender The gender of the contacts. One of {@link #MALE}, {@link #FEMALE}, or {@link #BOTH}
     * @return The list of {@link User} objects.
     */
    public List<User> getUsersForQuery(int amount, @Genders String gender) {
        String response = getPersonsJson(amount, gender);
        RandomUserJsonParser randomUserJsonParser = new RandomUserJsonParser();
        List<User> users = randomUserJsonParser.parseResponse(response);

        for (User user : users) {
            InputStream imageStream = readImageUsingHttp(user.getImageUrl());
            if (imageStream != null) {
                user.setImage(BitmapFactory.decodeStream(imageStream));
            }
        }

        return users;
    }

    public String getPersonsJson(int howMany, @Genders String gender) {
        if (howMany < 0 || howMany > MAX_RESULTS) {
            Log.e(TAG, "Cannot fetch less than 0 or more than " + MAX_RESULTS + " persons.");
            return "";
        }

        String completeUrl = String.format(URL_TEMPLATE, howMany, gender, getApiKey());
        return readUsingHttp(completeUrl);
    }

    /**
     * @return The current API security key in use.
     */
    public String getApiKey() {
        if (mApiKey == null) {
            // we are building with another API key, this one in the repo is public
            mApiKey = readRawTextFile(mContext, R.raw.api_key).trim();
        }
        return mApiKey;
    }

    /**
     * Reads a String from the given Internet location using the HTTP protocol. Connection method is <i>GET</i>.
     *
     * @param location Where to read from (must be a valid URL)
     * @return Parsed text from the given URL, or an empty String if something fails
     */
    public String readUsingHttp(String location) {
        URL url;
        try {
            url = new URL(location);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Cannot parse, invalid URL: " + location, e);
            return "";
        }

        try {
            Request request = new Request.Builder().url(url).build();
            Response response = mClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(TAG, "Cannot fetch " + url.toExternalForm() + ", reason: ", e);
            return "";
        }
    }

    /**
     * Reads an Image file from the Internet address supplied.
     *
     * @param imageUrl The URL pointing to the location of the image
     * @return An InputStream of the image, or null, in case of an error
     */
    @Nullable
    public InputStream readImageUsingHttp(String imageUrl) {
        URL url;
        try {
            url = new URL(imageUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Cannot parse, invalid URL: " + imageUrl, e);
            return null;
        }

        try {
            Request request = new Request.Builder().url(url).build();
            Response response = mClient.newCall(request).execute();
            return response.body().byteStream();
        } catch (IOException e) {
            Log.e(TAG, "Cannot fetch " + url.toExternalForm() + ", reason: ", e);
            return null;
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
