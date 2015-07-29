
package me.angrybyte.contactsgenerator.test.app;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import me.angrybyte.contactsgenerator.MainActivity;
import me.angrybyte.contactsgenerator.R;
import me.angrybyte.contactsgenerator.RandomApi;

public class RandomApiTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mActivity;
    private RandomApi mApi;

    /**
     * The default constructor.
     */
    public RandomApiTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // this runs before running the tests
        mActivity = getActivity();
        mApi = new RandomApi(mActivity);
    }

    @SmallTest
    public void testReadingApiKey() {
        String apiKey = mApi.readRawTextFile(mActivity, R.raw.api_key);
        assertNotNull("Cannot read the API key.", apiKey);
        apiKey = apiKey.trim();

        // current API key length is always 19
        int length = 19;
        assertEquals("API Key has wrong length", length, apiKey.length());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // this runs when tests are finished
        mApi = null;
        mActivity = null;
    }

}
