package com.rengwuxian.materialedittext;

import android.test.AndroidTestCase;

/**
 * Tests setting and getting the error message from a {@link com.rengwuxian.materialedittext.MaterialEditText}.
 * <p/>
 * Created by egor on 25/11/14.
 */
public class MaterialEditTextSetErrorTest extends AndroidTestCase {

    private MaterialEditText editTextUnderTest;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        editTextUnderTest = new MaterialEditText(getContext());
    }

    public void testGetErrorReturnsNullIfNoErrorMessageWasSet() {
        assertNull(editTextUnderTest.getError());
    }

    public void testGetErrorReturnsMessageSetEarlierViaSetError() {
        editTextUnderTest.setError("Error!");
        assertEquals("Error!", editTextUnderTest.getError().toString());
    }
}
