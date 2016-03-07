
package me.angrybyte.contactsgenerator.test.app;

import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;

import me.angrybyte.contactsgenerator.MainActivity;
import me.angrybyte.contactsgenerator.R;
import me.angrybyte.contactsgenerator.api.ContactOperations;
import me.angrybyte.contactsgenerator.api.Operations;
import me.angrybyte.contactsgenerator.parser.data.Person;

import java.util.List;

/**
 * A test case used for testing app's core functionality. Note that all methods are returning {@code void} and asserting result values.
 */
public class OperationTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public static final String HTTP_TEST_URL = "http://pastebin.com/raw.php?i=VS10rPNJ";
    public static final String HTTP_TEST_CONTENT = "TESTING... OK!";
    public static final int DEFAULT_KEY_LENGTH = 19;

    private MainActivity mActivity;
    private Operations mApi;

    /**
     * The default constructor.
     */
    public OperationTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // this runs before running the tests
        mActivity = getActivity();
        mApi = new Operations(mActivity);
    }

    @SmallTest
    public void testAReadingApiKey() {
        String apiKey = mApi.readRawTextFile(mActivity, R.raw.api_key);
        assertNotNull("Cannot read the API key.", apiKey);

        int length = DEFAULT_KEY_LENGTH;
        assertEquals("API Key '" + apiKey.trim() + "' has wrong length.", length, apiKey.trim().length());
    }

    @SmallTest
    public void testBHttpRequest() {
        String pageText = mApi.readUsingHttp(HTTP_TEST_URL);
        assertEquals("Invalid page content: '" + pageText + "'.", HTTP_TEST_CONTENT, pageText);
    }

    @SmallTest
    public void testCJsonRequest() {
        String jsonText = mApi.getPersonsJson(1, Operations.BOTH);
        assertEquals("Json response is empty.", true, jsonText.length() > 0);
    }

    @MediumTest
    public void testDQueryPersons() {
        List<Person> persons = mApi.getPersons(1, Operations.BOTH);
        assertNotNull("Persons list is null", persons);
        assertEquals("Persons list is empty", 1, persons.size());

        Person first = persons.get(0);
        assertNotNull("Person is null", first);

        assertEquals("Person has no first name", false, TextUtils.isEmpty(first.getFirstName()));
        assertEquals("Person has no last name", false, TextUtils.isEmpty(first.getLastName()));
        assertEquals("Person has no email", false, TextUtils.isEmpty(first.getEmail()));
        assertEquals("Person has no number", false, TextUtils.isEmpty(first.getPhone()));
    }

    @SmallTest
    public void testEImageDownload() {
        List<Person> persons = mApi.getPersons(1, Operations.BOTH);
        Person person = persons.get(0);

        Bitmap bitmap = mApi.fetchImage(person);
        assertNotNull("Person's image is null!", bitmap);
        assertNotSame("Bitmap width is 0", bitmap.getWidth(), 0);
        assertNotSame("Bitmap height is 0", bitmap.getHeight(), 0);
    }

    @MediumTest
    public void testFContactStorage() throws RemoteException, OperationApplicationException {
        ContactOperations contacts = new ContactOperations(mActivity);
        List<Person> persons = mApi.getPersons(5, Operations.BOTH);
        for (Person person : persons) {
            person.setImage(mApi.fetchImage(person));
            contacts.storeContact(person);
        }
    }

    @MediumTest
    public void testGDeleteAllContacts() {
        ContactOperations contacts = new ContactOperations(mActivity);
        boolean deleted = contacts.deleteContacts(null);
        assertTrue("Contacts not deleted", deleted);
    }

    @MediumTest
    public void testHDeleteExampleContacts() {
        ContactOperations contacts = new ContactOperations(mActivity);
        boolean deleted = contacts.deleteContacts("example.com");
        assertTrue("Contacts not deleted", deleted);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // this runs when tests are finished
        mApi = null;
        mActivity = null;
    }

}
