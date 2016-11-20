package me.angrybyte.contactsgenerator.test.app;

import android.test.ActivityInstrumentationTestCase2;

import me.angrybyte.contactsgenerator.MainActivity;
import me.angrybyte.contactsgenerator.api.ContactOperations;

public class ContactOperationsTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mActivity;
    private ContactOperations mApi;

    /**
     * The default constructor.
     */
    public ContactOperationsTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // this runs before running the tests
        mActivity = getActivity();
        mApi = new ContactOperations(mActivity);
    }
}
