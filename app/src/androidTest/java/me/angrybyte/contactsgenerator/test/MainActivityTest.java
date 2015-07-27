
package me.angrybyte.contactsgenerator.test;

import android.support.v7.widget.Toolbar;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import me.angrybyte.contactsgenerator.MainActivity;
import me.angrybyte.contactsgenerator.R;

/**
 * This class is used for testing the whole MainActivity.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mMainActivity;

    /**
     * The default constructor.
     */
    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // this runs before running the tests
        mMainActivity = getActivity();
    }

    @SmallTest
    public void testInflationToolbar() {
        Toolbar toolbar = (Toolbar) mMainActivity.findViewById(R.id.main_toolbar);
        assertNotNull("Toolbar failed to inflate", toolbar);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // this runs when tests are finished
        mMainActivity = null;
    }

}
