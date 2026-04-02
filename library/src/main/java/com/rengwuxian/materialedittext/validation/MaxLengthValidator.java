package com.rengwuxian.materialedittext.validation;

import android.support.annotation.NonNull;

/**
 * Custom validator to check the length of string
 */
public class MaxLengthValidator extends METValidator {
    private final int maxLength;

    public MaxLengthValidator(@NonNull String errorMessage, @NonNull int maxLength) {
        super(errorMessage);
        this.maxLength = maxLength;
    }

    @Override
    public boolean isValid(@NonNull CharSequence text, boolean isEmpty) {
        return text.length() <= maxLength;
    }
}