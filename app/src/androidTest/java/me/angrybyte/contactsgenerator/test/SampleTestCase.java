
package me.angrybyte.contactsgenerator.test;

import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestCase;

import me.angrybyte.contactsgenerator.samples.SampleTestClass;

/**
 * A sample class used to test the {@link me.angrybyte.contactsgenerator.samples.SampleTestClass}. Note that all methods are returning <b>
 * {@code void}</b> and asserting result values.
 */
public class SampleTestCase extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // this runs before running the tests
    }

    @SmallTest
    public void testAddingNumbers() {
        int a = 3;
        int b = 4;
        int r = SampleTestClass.add(a, b);
        assertEquals(7, r);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // this runs when tests are finished
    }

}
