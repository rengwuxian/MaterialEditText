package com.rengwuxian.materialedittext;

import android.test.AndroidTestCase;

/**
 * Misc tests for {@link com.rengwuxian.materialedittext.MaterialEditText}.
 * <p/>
 * Created by egor on 25/11/14.
 */
public class MaterialEditTextTest extends AndroidTestCase {

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
        editTextUnderTest.layout(0, 0, 1000, 1000);
        editTextUnderTest.setError("Error!");
        assertEquals("Error!", editTextUnderTest.getError().toString());
    }

    public void testSetErrorWithZeroSizeDoesNotThrow() {
        editTextUnderTest.setError("Error!");
    }
}
