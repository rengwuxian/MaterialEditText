package com.rengwuxian.materialedittext.validation;

import java.util.regex.Pattern;

import androidx.annotation.NonNull;

/**
 * Custom validator for Regexes
 */
public class RegexpValidator extends METValidator {

  private Pattern pattern;

  public RegexpValidator(@NonNull String errorMessage, @NonNull String regex) {
    super(errorMessage);
    pattern = Pattern.compile(regex);
  }

  public RegexpValidator(@NonNull String errorMessage, @NonNull Pattern pattern) {
    super(errorMessage);
    this.pattern = pattern;
  }

  @Override
  public boolean isValid(@NonNull CharSequence text, boolean isEmpty) {
    return pattern.matcher(text).matches();
  }
}
