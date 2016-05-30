
package me.angrybyte.contactsgenerator.test.app;

import android.support.v7.widget.Toolbar;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import me.angrybyte.contactsgenerator.MainActivity;
import me.angrybyte.contactsgenerator.R;

/**
 * This class is used for sample testing of the MainActivity.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mActivity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // this runs before running the tests
        mActivity = getActivity();
    }

    @SmallTest
    public void testLayoutInflation() {
        Toolbar toolbar = (Toolbar) mActivity.findViewById(R.id.main_toolbar);
        assertNotNull("Toolbar failed to inflate.", toolbar);

        // ... and so on ...
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // this runs when tests are finished
        mActivity = null;
    }

}
