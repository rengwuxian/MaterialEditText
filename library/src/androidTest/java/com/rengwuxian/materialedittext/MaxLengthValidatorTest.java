package com.rengwuxian.materialedittext;

import android.test.AndroidTestCase;

import com.rengwuxian.materialedittext.validation.MaxLengthValidator;

/**
 * Test the functioning of the max length validator
 */
public class MaxLengthValidatorTest extends AndroidTestCase {

    private MaterialEditText editTextUnderTest;
    private String errorMessage;
    private final int maxLength = 5;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        editTextUnderTest = new MaterialEditText(getContext());
        errorMessage = "Text length is not valid";
        editTextUnderTest.addValidator(new MaxLengthValidator(errorMessage, maxLength));
    }

    public void testAValidLength() {
        editTextUnderTest.setText("foo");
        assertTrue(editTextUnderTest.validate());
    }

    public void testATextReachingTheLimit() {
        editTextUnderTest.setText("fooba");
        assertTrue(editTextUnderTest.validate());
    }

    public void testAInvalidLength() {
        editTextUnderTest.setText("foobar");
        assertFalse(editTextUnderTest.validate());
        assertEquals(errorMessage, editTextUnderTest.getError());
    }

}
