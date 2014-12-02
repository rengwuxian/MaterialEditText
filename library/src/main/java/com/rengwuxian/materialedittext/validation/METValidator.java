package com.rengwuxian.materialedittext.validation;

import android.support.annotation.NonNull;

/**
 * Base Validator class to either implement or inherit from for custom validation
 */
public abstract class METValidator {

  protected String errorMessage;

  public METValidator(@NonNull String errorMessage) {
      this.errorMessage = errorMessage;
  }

  @NonNull
  public String getErrorMessage() {
    return this.errorMessage;
  }

  /**
   * Abstract method to implement your own validation checking.
   *
   * @param text The CharSequence representation of the text in the EditText field. Cannot be null, but may be empty.
   * @param isEmpty Boolean indicating whether or not the text param is empty
   * @return True if valid, false if not
   */
  public abstract boolean isValid(@NonNull CharSequence text, boolean isEmpty);

}
