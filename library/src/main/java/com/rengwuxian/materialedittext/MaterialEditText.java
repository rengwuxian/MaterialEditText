package com.rengwuxian.materialedittext;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.rengwuxian.materialedittext.validation.METValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EditText in Material Design
 * <p/>
 * author:rengwuxian
 * <p/>
 */
public class MaterialEditText extends EditText {

  @IntDef({FLOATING_LABEL_NONE, FLOATING_LABEL_NORMAL, FLOATING_LABEL_HIGHLIGHT})
  public @interface FloatingLabelType {
  }

  public static final int FLOATING_LABEL_NONE = 0;
  public static final int FLOATING_LABEL_NORMAL = 1;
  public static final int FLOATING_LABEL_HIGHLIGHT = 2;

  /**
   * the spacing between the main text and the inner top padding.
   */
  private int extraPaddingTop;

  /**
   * the spacing between the main text and the inner bottom padding.
   */
  private int extraPaddingBottom;

  /**
   * the floating label's text size.
   */
  private final int floatingLabelTextSize;

  /**
   * the spacing between the main text and the floating label.
   */
  private int floatingLabelSpacing;

  /**
   * the spacing between the main text and the bottom components (bottom ellipsis, helper/error text, characters counter).
   */
  private final int bottomSpacing;

  /**
   * whether the floating label should be shown. default is false.
   */
  private boolean floatingLabelEnabled;

  /**
   * whether to highlight the floating label's text color when focused (with the main color). default is true.
   */
  private boolean highlightFloatingLabel;

  /**
   * the base color of the line and the texts. default is black.
   */
  private int baseColor;

  /**
   * inner top padding
   */
  private int innerPaddingTop;

  /**
   * inner bottom padding
   */
  private int innerPaddingBottom;

  /**
   * the underline's highlight color, and the highlight color of the floating label if app:highlightFloatingLabel is set true in the xml. default is black(when app:darkTheme is false) or white(when app:darkTheme is true)
   */
  private int primaryColor;

  /**
   * the color for when something is wrong.(e.g. exceeding max characters)
   */
  private int errorColor;

  /**
   * min characters count limit. 0 means no limit. default is 0. NOTE: the character counter will increase the View's height.
   */
  private int minCharacters;

  /**
   * max characters count limit. 0 means no limit. default is 0. NOTE: the character counter will increase the View's height.
   */
  private int maxCharacters;

  /**
   * whether to show the bottom ellipsis in singleLine mode. default is false. NOTE: the bottom ellipsis will increase the View's height.
   */
  private boolean singleLineEllipsis;

  /**
   * bottom ellipsis's height
   */
  private final int bottomEllipsisSize;

  /**
   * min bottom lines count.
   */
  private int minBottomLines;

  /**
   * reserved bottom text lines count, no matter if there is some helper/error text.
   */
  private int minBottomTextLines;

  /**
   * real-time bottom lines count. used for bottom extending/collapsing animation.
   */
  private float currentBottomLines;

  /**
   * bottom lines count.
   */
  private float bottomLines;

  /**
   * Helper text at the bottom
   */
  private String helperText;

  /**
   * Helper text color
   */
  private int helperTextColor = -1;

  /**
   * error text for manually invoked {@link #setError(CharSequence)}
   */
  private String tempErrorText;

  /**
   * animation fraction of the floating label (0 as totally hidden).
   */
  private float floatingLabelFraction;

  /**
   * whether the floating label is being shown.
   */
  private boolean floatingLabelShown;

  /**
   * the floating label's focusFraction
   */
  private float focusFraction;

  /**
   * The font used for the accent texts (floating label, error/helper text, character counter, etc.)
   */
  private Typeface accentTypeface;

  /**
   * Text for the floatLabel if different from the hint
   */
  private CharSequence floatingLabelText;

  /**
   * Whether or not to show the underline. Shown by default
   */
  private boolean hideUnderline;

  private ArgbEvaluator focusEvaluator = new ArgbEvaluator();
  Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
  Paint.FontMetrics fontMetrics;
  StaticLayout textLayout;
  ObjectAnimator labelAnimator;
  ObjectAnimator labelFocusAnimator;
  ObjectAnimator bottomLinesAnimator;
  OnFocusChangeListener innerFocusChangeListener;
  OnFocusChangeListener outerFocusChangeListener;
  private ArrayList<METValidator> validators = new ArrayList<>();

  public MaterialEditText(Context context) {
    this(context, null);
  }

  public MaterialEditText(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public MaterialEditText(Context context, AttributeSet attrs, int style) {
    super(context, attrs, style);

    setFocusable(true);
    setFocusableInTouchMode(true);
    setClickable(true);

    floatingLabelTextSize = getResources().getDimensionPixelSize(R.dimen.floating_label_text_size);
    bottomSpacing = getResources().getDimensionPixelSize(R.dimen.inner_components_spacing);
    bottomEllipsisSize = getResources().getDimensionPixelSize(R.dimen.bottom_ellipsis_height);


    // retrieve the default baseColor
    int defaultBaseColor;
    TypedValue baseColorTypedValue = new TypedValue();
    context.getTheme().resolveAttribute(android.R.attr.windowBackground, baseColorTypedValue, true);
    defaultBaseColor = Colors.getBaseColor(baseColorTypedValue.data);

    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialEditText);
    baseColor = typedArray.getColor(R.styleable.MaterialEditText_baseColor, defaultBaseColor);
    ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}, EMPTY_STATE_SET}, new int[]{baseColor & 0x00ffffff | 0xdf000000, baseColor & 0x00ffffff | 0x44000000});
    setTextColor(colorStateList);

    // retrieve the default primaryColor
    int defaultPrimaryColor;
    TypedValue primaryColorTypedValue = new TypedValue();
    try {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        throw new RuntimeException("SDK_INT less than LOLLIPOP");
      }
      context.getTheme().resolveAttribute(android.R.attr.colorPrimary, primaryColorTypedValue, true);
      defaultPrimaryColor = primaryColorTypedValue.data;
    } catch (Exception e) {
      try {
        int colorAccentId = getResources().getIdentifier("colorPrimary", "attr", getContext().getPackageName());
        if (colorAccentId != 0) {
          context.getTheme().resolveAttribute(colorAccentId, primaryColorTypedValue, true);
          defaultPrimaryColor = primaryColorTypedValue.data;
        } else {
          throw new RuntimeException("colorAccent not found");
        }
      } catch (Exception e1) {
        defaultPrimaryColor = baseColor;
      }
    }

    primaryColor = typedArray.getColor(R.styleable.MaterialEditText_primaryColor, defaultPrimaryColor);
    setFloatingLabelInternal(typedArray.getInt(R.styleable.MaterialEditText_floatingLabel, 0));
    errorColor = typedArray.getColor(R.styleable.MaterialEditText_errorColor, Color.parseColor("#e7492E"));
    minCharacters = typedArray.getInt(R.styleable.MaterialEditText_minCharacters, 0);
    maxCharacters = typedArray.getInt(R.styleable.MaterialEditText_maxCharacters, 0);
    singleLineEllipsis = typedArray.getBoolean(R.styleable.MaterialEditText_singleLineEllipsis, false);
    helperText = typedArray.getString(R.styleable.MaterialEditText_helperText);
    helperTextColor = typedArray.getColor(R.styleable.MaterialEditText_helperTextColor, -1);
    minBottomTextLines = typedArray.getInt(R.styleable.MaterialEditText_minBottomTextLines, 0);
    String fontPath = typedArray.getString(R.styleable.MaterialEditText_accentTypeface);
    if (fontPath != null) {
      accentTypeface = getCustomTypeface(fontPath);
      textPaint.setTypeface(accentTypeface);
    }
    floatingLabelText = typedArray.getString(R.styleable.MaterialEditText_floatingLabelText);
    if (floatingLabelText == null) {
      floatingLabelText = getHint();
    }
    floatingLabelSpacing = typedArray.getDimensionPixelSize(R.styleable.MaterialEditText_floatingLabelSpacing, bottomSpacing);
    hideUnderline = typedArray.getBoolean(R.styleable.MaterialEditText_hideUnderline, false);
    typedArray.recycle();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      setBackground(null);
    } else {
      setBackgroundDrawable(null);
    }
    if (singleLineEllipsis) {
      TransformationMethod transformationMethod = getTransformationMethod();
      setSingleLine();
      setTransformationMethod(transformationMethod);
    }
    textPaint.setTextSize(floatingLabelTextSize);
    fontMetrics = textPaint.getFontMetrics();
    initMinBottomLines();
    initPadding();
    initText();
    initFloatingLabel();
    initErrorTextListener();
  }

  private void initText() {
    if (!TextUtils.isEmpty(getText())) {
      CharSequence text = getText();
      setText(null);
      setHintTextColor(baseColor & 0x00ffffff | 0x44000000);
      setText(text);
      floatingLabelFraction = 1;
      floatingLabelShown = true;
    } else {
      setHintTextColor(baseColor & 0x00ffffff | 0x44000000);
    }
  }

  private void initErrorTextListener() {
    addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        setError(null);
      }
    });
  }

  private Typeface getCustomTypeface(@NonNull String fontPath) {
    return Typeface.createFromAsset(getContext().getAssets(), fontPath);
  }

  public float getFloatingLabelFraction() {
    return floatingLabelFraction;
  }

  public void setFloatingLabelFraction(float floatingLabelFraction) {
    this.floatingLabelFraction = floatingLabelFraction;
    invalidate();
  }

  public float getFocusFraction() {
    return focusFraction;
  }

  public void setFocusFraction(float focusFraction) {
    this.focusFraction = focusFraction;
    invalidate();
  }

  public float getCurrentBottomLines() {
    return currentBottomLines;
  }

  public void setCurrentBottomLines(float currentBottomLines) {
    this.currentBottomLines = currentBottomLines;
    initPadding();
  }

  @Nullable
  public Typeface getAccentTypeface() {
    return accentTypeface;
  }

  /**
   * Set typeface used for the accent texts (floating label, error/helper text, character counter, etc.)
   */
  public void setAccentTypeface(Typeface accentTypeface) {
    this.accentTypeface = accentTypeface;
    this.textPaint.setTypeface(accentTypeface);
    postInvalidate();
  }

  public boolean getHideUnderline() {
    return hideUnderline;
  }

  /**
   * Set whether or not to hide the underline (shown by default).
   * <p/>
   * The positions of text below will be adjusted accordingly (error/helper text, character counter, ellipses, etc.)
   * <p/>
   * NOTE: You probably don't want to hide this if you have any subtext features of this enabled, as it can look weird to not have a dividing line between them.
   */
  public void setHideUnderline(boolean hideUnderline) {
    this.hideUnderline = hideUnderline;
    initPadding();
    postInvalidate();
  }

  public CharSequence getFloatingLabelText() {
    return floatingLabelText;
  }

  /**
   * Set the floating label text.
   * <p/>
   * Pass null to force fallback to use hint's value.
   *
   * @param floatingLabelText
   */
  public void setFloatingLabelText(@Nullable CharSequence floatingLabelText) {
    this.floatingLabelText = floatingLabelText == null ? getHint() : floatingLabelText;
    postInvalidate();
  }

  private int getPixel(int dp) {
    return Density.dp2px(getContext(), dp);
  }

  private void initPadding() {
    int paddingTop = getPaddingTop() - extraPaddingTop;
    int paddingBottom = getPaddingBottom() - extraPaddingBottom;
    extraPaddingTop = floatingLabelEnabled ? floatingLabelTextSize + floatingLabelSpacing : floatingLabelSpacing;
    extraPaddingBottom = (int) ((fontMetrics.descent - fontMetrics.ascent) * currentBottomLines) + (hideUnderline ? bottomSpacing : bottomSpacing * 2);
    setPaddings(getPaddingLeft(), paddingTop, getPaddingRight(), paddingBottom);
  }

  /**
   * calculate {@link #minBottomLines}
   */
  private void initMinBottomLines() {
    boolean extendBottom = minCharacters > 0 || maxCharacters > 0 || singleLineEllipsis || tempErrorText != null || helperText != null;
    currentBottomLines = minBottomLines = minBottomTextLines > 0 ? minBottomTextLines : extendBottom ? 1 : 0;
  }

  /**
   * use {@link #setPaddings(int, int, int, int)} instead, or the paddingTop and the paddingBottom may be set incorrectly.
   */
  @Deprecated
  @Override
  public final void setPadding(int left, int top, int right, int bottom) {
    super.setPadding(left, top, right, bottom);
  }

  /**
   * Use this method instead of {@link #setPadding(int, int, int, int)} to automatically set the paddingTop and the paddingBottom correctly.
   */
  public void setPaddings(int left, int top, int right, int bottom) {
    innerPaddingTop = top;
    innerPaddingBottom = bottom;
    super.setPadding(left, top + extraPaddingTop, right, bottom + extraPaddingBottom);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int oldWidth = getMeasuredWidth();
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int newWidth = getMeasuredWidth();

    if (newWidth > 0 && oldWidth != newWidth) {
      adjustBottomLines();
    }
  }

  private void adjustBottomLines() {
    // adjust bottom lines
    int destBottomLines;
    if (tempErrorText != null) {
      textLayout = new StaticLayout(tempErrorText, textPaint, getMeasuredWidth() - getBottomTextLeftOffset() - getBottomTextRightOffset(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
      destBottomLines = Math.max(textLayout.getLineCount(), minBottomTextLines);
    } else if (helperText != null) {
      textLayout = new StaticLayout(helperText, textPaint, getMeasuredWidth() - getBottomTextLeftOffset() - getBottomTextRightOffset(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
      destBottomLines = Math.max(textLayout.getLineCount(), minBottomTextLines);
    } else {
      destBottomLines = minBottomLines;
    }
    if (bottomLines != destBottomLines) {
      getBottomLinesAnimator(destBottomLines).start();
    }
    bottomLines = destBottomLines;
  }

  /**
   * get inner top padding, not the real paddingTop
   */
  public int getInnerPaddingTop() {
    return innerPaddingTop;
  }

  /**
   * get inner bottom padding, not the real paddingBottom
   */
  public int getInnerPaddingBottom() {
    return innerPaddingBottom;
  }

  private void initFloatingLabel() {
    if (floatingLabelEnabled) {
      // observe the text changing
      addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
          if (s.length() == 0) {
            if (floatingLabelShown) {
              floatingLabelShown = false;
              getLabelAnimator().reverse();
            }
          } else if (!floatingLabelShown) {
            floatingLabelShown = true;
            if (getLabelAnimator().isStarted()) {
              getLabelAnimator().reverse();
            } else {
              getLabelAnimator().start();
            }
          }
        }
      });
      if (highlightFloatingLabel) {
        // observe the focus state to animate the floating label's text color appropriately
        innerFocusChangeListener = new OnFocusChangeListener() {
          @Override
          public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
              if (getLabelFocusAnimator().isStarted()) {
                getLabelFocusAnimator().reverse();
              } else {
                getLabelFocusAnimator().start();
              }
            } else {
              getLabelFocusAnimator().reverse();
            }
            if (outerFocusChangeListener != null) {
              outerFocusChangeListener.onFocusChange(v, hasFocus);
            }
          }
        };
        super.setOnFocusChangeListener(innerFocusChangeListener);
      }
    }

  }

  public void setBaseColor(int color) {
    baseColor = color;
    postInvalidate();
  }

  public void setPrimaryColor(int color) {
    primaryColor = color;
    postInvalidate();
  }

  private void setFloatingLabelInternal(int mode) {
    switch (mode) {
      case FLOATING_LABEL_NORMAL:
        floatingLabelEnabled = true;
        highlightFloatingLabel = false;
        break;
      case FLOATING_LABEL_HIGHLIGHT:
        floatingLabelEnabled = true;
        highlightFloatingLabel = true;
        break;
      default:
        floatingLabelEnabled = false;
        highlightFloatingLabel = false;
        break;
    }
  }

  public void setFloatingLabel(@FloatingLabelType int mode) {
    setFloatingLabelInternal(mode);
    postInvalidate();
  }

  public int getFloatingLabelSpacing() {
    return floatingLabelSpacing;
  }

  public void setFloatingLabelSpacing(int spacing) {
    floatingLabelSpacing = spacing;
    postInvalidate();
  }

  public void setSingleLineEllipsis() {
    setSingleLineEllipsis(true);
  }

  public void setSingleLineEllipsis(boolean enabled) {
    singleLineEllipsis = enabled;
    initMinBottomLines();
    initPadding();
    postInvalidate();
  }

  public int getMaxCharacters() {
    return maxCharacters;
  }

  public void setMaxCharacters(int max) {
    maxCharacters = max;
    initMinBottomLines();
    initPadding();
    postInvalidate();
  }

  public int getMinCharacters() {
    return minCharacters;
  }

  public void setMinCharacters(int min) {
    minCharacters = min;
    initMinBottomLines();
    initPadding();
    postInvalidate();
  }

  public int getErrorColor() {
    return errorColor;
  }

  public void setErrorColor(int color) {
    errorColor = color;
    postInvalidate();
  }

  public void setHelperText(CharSequence helperText) {
    this.helperText = helperText == null ? null : helperText.toString();
    adjustBottomLines();
    postInvalidate();
  }

  public String getHelperText() {
    return helperText;
  }

  public int getHelperTextColor() {
    return helperTextColor;
  }

  public void setHelperTextColor(int color) {
    helperTextColor = color;
    postInvalidate();
  }

  @Override
  public void setError(CharSequence errorText) {
    tempErrorText = errorText == null ? null : errorText.toString();
    adjustBottomLines();
    postInvalidate();
  }

  @Override
  public CharSequence getError() {
    return tempErrorText;
  }

  /**
   * only used to draw the bottom line
   */
  private boolean isInternalValid() {
    return tempErrorText == null && isCharactersCountValid();
  }

  /**
   * if the main text matches the regex
   *
   * @deprecated use the new validator interface to add your own custom validator
   */
  @Deprecated
  public boolean isValid(String regex) {
    if (regex == null) {
      return false;
    }
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(getText());
    return matcher.matches();
  }

  /**
   * check if the main text matches the regex, and set the error text if not.
   *
   * @return true if it matches the regex, false if not.
   * @deprecated use the new validator interface to add your own custom validator
   */
  @Deprecated
  public boolean validate(String regex, CharSequence errorText) {
    boolean isValid = isValid(regex);
    if (!isValid) {
      setError(errorText);
    }
    postInvalidate();
    return isValid;
  }

  /**
   * Run validation on a single validator instance
   *
   * @param validator Validator to check
   * @return True if valid, false if not
   */
  public boolean validateWith(@NonNull METValidator validator) {
    CharSequence text = getText();
    boolean isValid = validator.isValid(text, text.length() == 0);
    if (!isValid) {
      setError(validator.getErrorMessage());
    }
    postInvalidate();
    return isValid;
  }

  /**
   * Check all validators, sets the error text if not
   * <p/>
   * NOTE: this stops at the first validator to report invalid.
   *
   * @return True if all validators pass, false if not
   */
  public boolean validate() {

    if (validators == null || validators.size() == 0) {
      return true;
    }

    CharSequence text = getText();
    boolean isEmpty = text.length() == 0;

    boolean isValid = true;
    for (METValidator validator : validators) {
      //noinspection ConstantConditions
      isValid = isValid && validator.isValid(text, isEmpty);
      if (!isValid) {
        setError(validator.getErrorMessage());
        break;
      }
    }

    postInvalidate();
    return isValid;
  }

  public boolean hasValidator() {
    return this.validators.size() != 0;
  }

  /**
   * Adds a new validator to the View's list of validators
   * <p/>
   * This will be checked with the others in {@link #validate()}
   *
   * @param validator Validator to add
   * @return This instance, for easy chaining
   */
  public MaterialEditText addValidator(METValidator validator) {
    this.validators.add(validator);
    return this;
  }

  public List<METValidator> getValidators() {
    return this.validators;
  }

  @Override
  public void setOnFocusChangeListener(OnFocusChangeListener listener) {
    if (innerFocusChangeListener == null) {
      super.setOnFocusChangeListener(listener);
    } else {
      outerFocusChangeListener = listener;
    }
  }

  private ObjectAnimator getLabelAnimator() {
    if (labelAnimator == null) {
      labelAnimator = ObjectAnimator.ofFloat(this, "floatingLabelFraction", 0f, 1f);
    }
    return labelAnimator;
  }

  private ObjectAnimator getLabelFocusAnimator() {
    if (labelFocusAnimator == null) {
      labelFocusAnimator = ObjectAnimator.ofFloat(this, "focusFraction", 0f, 1f);
    }
    return labelFocusAnimator;
  }

  private ObjectAnimator getBottomLinesAnimator(float destBottomLines) {
    if (bottomLinesAnimator == null) {
      bottomLinesAnimator = ObjectAnimator.ofFloat(this, "currentBottomLines", destBottomLines);
    } else {
      bottomLinesAnimator.end();
      bottomLinesAnimator.setFloatValues(destBottomLines);
    }
    return bottomLinesAnimator;
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    float lineStartY = getScrollY() + getHeight() - getPaddingBottom();

    if (!hideUnderline) {
      lineStartY += bottomSpacing;

      // draw the background
      if (!isInternalValid()) { // not valid
        paint.setColor(errorColor);
        canvas.drawRect(getScrollX(), lineStartY, getWidth() + getScrollX(), lineStartY + getPixel(2), paint);
      } else if (!isEnabled()) { // disabled
        paint.setColor(baseColor & 0x00ffffff | 0x44000000);
        float interval = getPixel(1);
        for (float startX = 0; startX < getWidth(); startX += interval * 3) {
          canvas.drawRect(getScrollX() + startX, lineStartY, getScrollX() + startX + interval, lineStartY + getPixel(1), paint);
        }
      } else if (hasFocus()) { // focused
        paint.setColor(primaryColor);
        canvas.drawRect(getScrollX(), lineStartY, getWidth() + getScrollX(), lineStartY + getPixel(2), paint);
      } else { // normal
        paint.setColor(baseColor & 0x00ffffff | 0x44000000);
        canvas.drawRect(getScrollX(), lineStartY, getWidth() + getScrollX(), lineStartY + getPixel(1), paint);
      }
    }

    float relativeHeight = -fontMetrics.ascent - fontMetrics.descent;
    float fontPaddingTop = floatingLabelTextSize + fontMetrics.ascent + fontMetrics.descent;

    // draw the characters counter
    if ((hasFocus() && hasCharatersCounter()) || !isCharactersCountValid()) {
      textPaint.setColor(isCharactersCountValid() ? getCurrentHintTextColor() : errorColor);
      String text;
      if (minCharacters <= 0) {
        text = getText().length() + " / " + maxCharacters;
      } else if (maxCharacters <= 0) {
        text = getText().length() + " / " + minCharacters + "+";
      } else {
        text = getText().length() + " / " + minCharacters + "-" + maxCharacters;
      }

      canvas.drawText(text, getWidth() + getScrollX() - textPaint.measureText(text), lineStartY + bottomSpacing + relativeHeight, textPaint);
    }

    // draw the bottom text
    if (textLayout != null) {
      float bottomTextStartX = getScrollX() + getBottomTextLeftOffset();
      if (tempErrorText != null) { // validation failed
        textPaint.setColor(errorColor);
        canvas.save();
        canvas.translate(bottomTextStartX, lineStartY + bottomSpacing - fontPaddingTop);
        textLayout.draw(canvas);
        canvas.restore();
      } else if (hasFocus() && !TextUtils.isEmpty(helperText)) {
        textPaint.setColor(helperTextColor != -1 ? helperTextColor : getCurrentHintTextColor());
        canvas.save();
        canvas.translate(bottomTextStartX, lineStartY + bottomSpacing - fontPaddingTop);
        textLayout.draw(canvas);
        canvas.restore();
      }
    }

    // draw the floating label
    if (floatingLabelEnabled && !TextUtils.isEmpty(floatingLabelText)) {
      // calculate the text color
      textPaint.setColor((Integer) focusEvaluator.evaluate(focusFraction, getCurrentHintTextColor(), primaryColor));

      // calculate the horizontal position
      float floatingLabelWidth = textPaint.measureText(floatingLabelText.toString());
      int floatingLabelStartX = getScrollX();
      if ((getGravity() & Gravity.LEFT) == Gravity.LEFT) {
        floatingLabelStartX += getPaddingLeft();
      } else if ((getGravity() & Gravity.RIGHT) == Gravity.RIGHT) {
        floatingLabelStartX += getWidth() - getPaddingRight() - floatingLabelWidth;
      } else {
        floatingLabelStartX += (int) (getPaddingLeft() + (getWidth() - getPaddingLeft() - getPaddingRight() - floatingLabelWidth) / 2);
      }

      // calculate the vertical position
      int floatingLabelStartY = innerPaddingTop + floatingLabelTextSize + floatingLabelSpacing;
      int distance = floatingLabelSpacing;
      int position = (int) (floatingLabelStartY - distance * floatingLabelFraction);

      // calculate the alpha
      int alpha = (int) (floatingLabelFraction * 0xff * (0.74f * focusFraction + 0.26f));
      textPaint.setAlpha(alpha);

      // draw the floating label
      canvas.drawText(floatingLabelText.toString(), floatingLabelStartX, position, textPaint);
    }

    // draw the bottom ellipsis
    if (hasFocus() && singleLineEllipsis && getScrollX() != 0) {
      paint.setColor(primaryColor);
      float startY = lineStartY + bottomSpacing;
      canvas.drawCircle(bottomEllipsisSize / 2 + getScrollX(), startY + bottomEllipsisSize / 2, bottomEllipsisSize / 2, paint);
      canvas.drawCircle(bottomEllipsisSize * 5 / 2 + getScrollX(), startY + bottomEllipsisSize / 2, bottomEllipsisSize / 2, paint);
      canvas.drawCircle(bottomEllipsisSize * 9 / 2 + getScrollX(), startY + bottomEllipsisSize / 2, bottomEllipsisSize / 2, paint);
    }

    // draw the original things
    super.onDraw(canvas);
  }

  private int getBottomTextLeftOffset() {
    return (singleLineEllipsis ? (bottomEllipsisSize * 5 + getPixel(4)) : 0);
  }

  private int getBottomTextRightOffset() {
    return hasCharatersCounter() ? (int) textPaint.measureText("00/000") : 0;
  }

  public boolean isCharactersCountValid() {
    return !hasCharatersCounter() || getText() == null || getText().length() == 0 || (getText().length() >= minCharacters && (maxCharacters <= 0 || getText().length() <= maxCharacters));
  }

  private boolean hasCharatersCounter() {
    return minCharacters > 0 || maxCharacters > 0;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (singleLineEllipsis && getScrollX() > 0 && event.getAction() == MotionEvent.ACTION_DOWN && event.getX() < getPixel(4 * 5) && event.getY() > getHeight() - extraPaddingBottom - innerPaddingBottom && event.getY() < getHeight() - innerPaddingBottom) {
      setSelection(0);
      return false;
    }
    return super.onTouchEvent(event);
  }
}