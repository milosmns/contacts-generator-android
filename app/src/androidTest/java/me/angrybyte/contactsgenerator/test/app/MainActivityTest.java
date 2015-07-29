
package me.angrybyte.contactsgenerator.test.app;

import android.support.v7.widget.Toolbar;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import me.angrybyte.contactsgenerator.MainActivity;
import me.angrybyte.contactsgenerator.R;

/**
 * This class is used for testing the whole MainActivity.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mActivity;

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
        mActivity = getActivity();
    }

    @SmallTest
    public void testLayoutInflation() {
        Toolbar toolbar = (Toolbar) mActivity.findViewById(R.id.main_toolbar);
        assertNotNull("Toolbar failed to inflate", toolbar);

        TextView question = (TextView) mActivity.findViewById(R.id.main_contacts_question);
        assertNotNull("Question view failed to inflate", question);

        View picker = mActivity.findViewById(R.id.main_number_picker);
        assertNotNull("Number picker failed to inflate", picker);

        CheckBox wantAvatars = (CheckBox) mActivity.findViewById(R.id.main_avatars_checkbox);
        assertNotNull("Avatars checkbox failed to inflate", wantAvatars);

        RadioGroup genders = (RadioGroup) mActivity.findViewById(R.id.main_genders);
        assertNotNull("Genders picker failed to inflate", genders);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // this runs when tests are finished
        mActivity = null;
    }

}
