
package me.angrybyte.contactsgenerator.test.app;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import me.angrybyte.contactsgenerator.MainActivity;
import me.angrybyte.contactsgenerator.R;
import me.angrybyte.contactsgenerator.RandomApi;

public class RandomApiTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public static final String HTTP_TEST_URL = "http://pastebin.com/raw.php?i=VS10rPNJ";
    public static final String HTTP_TEST_CONTENT = "TESTING... OK!";
    public static final int DEFAULT_KEY_LENGTH = 19;

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

        int length = DEFAULT_KEY_LENGTH;
        assertEquals("API Key '" + apiKey.trim() + "' has wrong length.", length, apiKey.trim().length());
    }

    @SmallTest
    public void testHttpRequest() {
        String pageText = mApi.readUsingHttp(HTTP_TEST_URL);
        assertEquals("Invalid page content: '" + pageText + "'.", HTTP_TEST_CONTENT, pageText);
    }

    @SmallTest
    public void testJsonRequest() {
        String jsonText = mApi.getPersonsJson(1, RandomApi.BOTH);
        assertEquals("Json response is empty.", true, jsonText.length() > 0);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // this runs when tests are finished
        mApi = null;
        mActivity = null;
    }

}
