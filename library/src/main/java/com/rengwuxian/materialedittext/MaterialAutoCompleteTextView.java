package com.rengwuxian.materialedittext;

import android.content.Context;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.content.res.ColorStateList;

import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AutoCompleteTextView in Material Design
 * <p/>
 * author:rengwuxian
 * <p/>
 */
public class MaterialAutoCompleteTextView extends AutoCompleteTextView {

  @IntDef({FLOATING_LABEL_NONE, FLOATING_LABEL_NORMAL, FLOATING_LABEL_HIGHLIGHT})
  public @interface FloatingLabelType {}
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
   * the spacing between the main text and the inner components (floating label, bottom ellipsis, characters counter).
   */
  private final int innerComponentsSpacing;

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
   * characters count limit. 0 means no limit. default is 0. NOTE: the character counter will increase the View's height.
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
   * Helper text at the bottom
   */
  private String helperText;

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

  private boolean attachedToWindow = false;
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

  public MaterialAutoCompleteTextView(Context context) {
    this(context, null);
  }

  public MaterialAutoCompleteTextView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MaterialAutoCompleteTextView(Context context, AttributeSet attrs, int style) {
    super(context, attrs, style);

    setFocusable(true);
    setFocusableInTouchMode(true);
    setClickable(true);

    floatingLabelTextSize = getResources().getDimensionPixelSize(R.dimen.floating_label_text_size);
    innerComponentsSpacing = getResources().getDimensionPixelSize(R.dimen.inner_components_spacing);
    bottomEllipsisSize = getResources().getDimensionPixelSize(R.dimen.bottom_ellipsis_height);

    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialEditText);
    baseColor = typedArray.getColor(R.styleable.MaterialEditText_baseColor, Color.BLACK);
    ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}, EMPTY_STATE_SET}, new int[]{baseColor & 0x00ffffff | 0xdf000000, baseColor & 0x00ffffff | 0x44000000});
    setTextColor(colorStateList);

    primaryColor = typedArray.getColor(R.styleable.MaterialEditText_primaryColor, baseColor);
    setFloatingLabelInternal(typedArray.getInt(R.styleable.MaterialEditText_floatingLabel, 0));
    errorColor = typedArray.getColor(R.styleable.MaterialEditText_errorColor, Color.parseColor("#e7492E"));
    maxCharacters = typedArray.getInt(R.styleable.MaterialEditText_maxCharacters, 0);
    singleLineEllipsis = typedArray.getBoolean(R.styleable.MaterialEditText_singleLineEllipsis, false);
    helperText = typedArray.getString(R.styleable.MaterialEditText_helperText);
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
    extraPaddingTop = floatingLabelEnabled ? floatingLabelTextSize + innerComponentsSpacing : innerComponentsSpacing;
    extraPaddingBottom = (int) ((fontMetrics.descent - fontMetrics.ascent) * currentBottomLines) + (hideUnderline ? innerComponentsSpacing : innerComponentsSpacing * 2);
    setPaddings(getPaddingLeft(), paddingTop, getPaddingRight(), paddingBottom);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    attachedToWindow = true;
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    attachedToWindow = false;
  }

  @Override
  public void onWindowFocusChanged(boolean hasWindowFocus) {
    super.onWindowFocusChanged(hasWindowFocus);
    if (hasWindowFocus) {
      adjustBottomLines();
    }
  }

  /**
   * calculate {@link #minBottomLines}
   */
  private void initMinBottomLines() {
    boolean extendBottom = maxCharacters > 0 || singleLineEllipsis || tempErrorText != null || helperText != null;
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

  public void setSingleLineEllipsis() {
    setSingleLineEllipsis(true);
  }

  public void setSingleLineEllipsis(boolean enabled) {
    singleLineEllipsis = enabled;
    if (enabled) {
      initPadding();
    }
    postInvalidate();
  }

  public int getMaxCharacters() {
    return maxCharacters;
  }

  public void setMaxCharacters(int max) {
    maxCharacters = max;
    if (max > 0) {
      initPadding();
    }
    postInvalidate();
  }

  public void setErrorColor(int color) {
    errorColor = color;
    postInvalidate();
  }

  public void setHelperText(CharSequence helperText) {
    this.helperText = helperText == null ? null : helperText.toString();
    postInvalidate();
    adjustBottomLines();
  }

  public String getHelperText() {
    return helperText;
  }

  @Override
  public void setError(CharSequence errorText) {
    tempErrorText = errorText == null ? null : errorText.toString();
    postInvalidate();
    adjustBottomLines();
  }

  @Override
  public CharSequence getError() {
    return tempErrorText;
  }

  private void adjustBottomLines() {
    int destBottomLines;
    if (tempErrorText != null) {
      textLayout = new StaticLayout(tempErrorText, textPaint, getWidth() - getBottomTextLeftOffset() - getBottomTextRightOffset(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
      destBottomLines = Math.max(textLayout.getLineCount(), minBottomTextLines);
    } else if (helperText != null) {
      textLayout = new StaticLayout(helperText, textPaint, getWidth() - getBottomTextLeftOffset() - getBottomTextRightOffset(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
      destBottomLines = Math.max(textLayout.getLineCount(), minBottomTextLines);
    } else {
      destBottomLines = minBottomLines;
    }
    if (attachedToWindow) {
      getBottomLinesAnimator(destBottomLines).start();
    } else {
      currentBottomLines = minBottomLines;
    }
  }

  /**
   * only used to draw the bottom line
   */
  private boolean isInternalValid() {
    return tempErrorText == null && isMaxCharactersValid();
  }

  /**
   * if the main text matches the regex
   */
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
   */
  public boolean validate(String regex, CharSequence errorText) {
    boolean isValid = isValid(regex);
    if (!isValid) {
      setError(errorText);
    }
    postInvalidate();
    return isValid;
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
      lineStartY += innerComponentsSpacing;
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
        paint.setColor(baseColor);
        canvas.drawRect(getScrollX(), lineStartY, getWidth() + getScrollX(), lineStartY + getPixel(1), paint);
      }
    }

    float relativeHeight = -fontMetrics.ascent - fontMetrics.descent;
    float fontPaddingTop = floatingLabelTextSize + fontMetrics.ascent + fontMetrics.descent;

    // draw the characters counter
    if (maxCharacters > 0) {
      textPaint.setColor(isMaxCharactersValid() ? getCurrentHintTextColor() : errorColor);
      String text = getText().length() + " / " + maxCharacters;
      canvas.drawText(text, getWidth() + getScrollX() - textPaint.measureText(text), lineStartY + innerComponentsSpacing + relativeHeight, textPaint);
    }

    // draw the bottom text
    if (textLayout != null) {
      float bottomTextStartX = getScrollX() + getBottomTextLeftOffset();
      if (tempErrorText != null) { // regex error
        textPaint.setColor(errorColor);
        canvas.save();
        canvas.translate(bottomTextStartX, lineStartY + innerComponentsSpacing - fontPaddingTop);
        textLayout.draw(canvas);
        canvas.restore();
      } else if (!TextUtils.isEmpty(helperText)) {
        textPaint.setColor(getCurrentHintTextColor());
        canvas.save();
        canvas.translate(bottomTextStartX, lineStartY + innerComponentsSpacing - fontPaddingTop);
        textLayout.draw(canvas);
        canvas.restore();
      }
    }

    // draw the floating label
    if (floatingLabelEnabled && !TextUtils.isEmpty(floatingLabelText)) {
      // calculate the text color
      textPaint.setColor((Integer) focusEvaluator.evaluate(focusFraction, getCurrentHintTextColor(), primaryColor));

      // calculate the vertical position
      int start = innerPaddingTop + floatingLabelTextSize + innerComponentsSpacing;
      int distance = innerComponentsSpacing;
      int position = (int) (start - distance * floatingLabelFraction);

      // calculate the alpha
      int alpha = (int) (floatingLabelFraction * 0xff * (0.74f * focusFraction + 0.26f));
      textPaint.setAlpha(alpha);

      // draw the floating label
      canvas.drawText(floatingLabelText.toString(), getPaddingLeft() + getScrollX(), position, textPaint);
    }

    // draw the bottom ellipsis
    if (hasFocus() && singleLineEllipsis && getScrollX() != 0) {
      paint.setColor(primaryColor);
      float startY = lineStartY + innerComponentsSpacing;
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
    return maxCharacters > 0 ? (int) textPaint.measureText("00/000") : 0;
  }

  public boolean isMaxCharactersValid() {
    return maxCharacters <= 0 || getText() == null || getText().length() <= maxCharacters;
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