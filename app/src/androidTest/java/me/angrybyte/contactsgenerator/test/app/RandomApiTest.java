
package me.angrybyte.contactsgenerator.test.app;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;

import me.angrybyte.contactsgenerator.ContactPersister;
import me.angrybyte.contactsgenerator.MainActivity;
import me.angrybyte.contactsgenerator.R;
import me.angrybyte.contactsgenerator.RandomApi;
import me.angrybyte.contactsgenerator.parser.data.User;

import java.util.List;

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

    @MediumTest
    public void testQueryUsers() {
        List<User> users = mApi.getUsersForQuery(1, RandomApi.BOTH);
        assertNotNull("Users list is null", users);
        assertEquals("Users list is empty", 0, users.size());

        User first = users.get(0);
        assertNotNull("User is null", first);

        assertEquals("User has no first name", false, TextUtils.isEmpty(first.getFirstName()));
        assertEquals("User has no last name", false, TextUtils.isEmpty(first.getLastName()));
        assertEquals("User has no email", false, TextUtils.isEmpty(first.getEmail()));
        assertEquals("User has no number", false, TextUtils.isEmpty(first.getPhone()));
    }

    @MediumTest
    public void testContactStorage() {
        ContactPersister contactPersister = new ContactPersister(mActivity);
        List<User> users = mApi.getUsersForQuery(1, RandomApi.BOTH);
        User user = users.get(0);
        contactPersister.storeContact(user);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // this runs when tests are finished
        mApi = null;
        mActivity = null;
    }

}
