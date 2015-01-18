package com.rengwuxian.materialedittext;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.DrawableRes;
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
import android.widget.AutoCompleteTextView;
import android.content.res.ColorStateList;

import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.rengwuxian.materialedittext.validation.METValidator;

import java.util.ArrayList;
import java.util.List;
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
   * the extra spacing between the main text and the left, actually for the left icon.
   */
  private int extraPaddingLeft;

  /**
   * the extra spacing between the main text and the right, actually for the right icon.
   */
  private int extraPaddingRight;

  /**
   * the floating label's text size.
   */
  private int floatingLabelTextSize;

  /**
   * the bottom texts' size.
   */
  private int bottomTextSize;

  /**
   * the spacing between the main text and the floating label.
   */
  private int floatingLabelSpacing;

  /**
   * the spacing between the main text and the bottom components (bottom ellipsis, helper/error text, characters counter).
   */
  private int bottomSpacing;

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
   * inner left padding
   */
  private int innerPaddingLeft;

  /**
   * inner right padding
   */
  private int innerPaddingRight;

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
   * Always show the floating label, instead of animating it in/out. False by default.
   */
  private boolean floatingLabelAlwaysShown;

  /**
   * Always show the helper text, no matter if the edit text is focused. False by default.
   */
  private boolean helperTextAlwaysShown;

  /**
   * bottom ellipsis's height
   */
  private int bottomEllipsisSize;

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
   * The font used on the view (AutoCompleteTextView content)
   */
  private Typeface typeface;

  /**
   * Text for the floatLabel if different from the hint
   */
  private CharSequence floatingLabelText;

  /**
   * Whether or not to show the underline. Shown by default
   */
  private boolean hideUnderline;

  /**
   * Whether to validate as soon as the text has changed. False by default
   */
  private boolean autoValidate;

  /**
   * Whether the characters count is valid
   */
  private boolean charactersCountValid;

  /**
   * Left Icon
   */
  private Bitmap[] iconLeftBitmaps;

  /**
   * Right Icon
   */
  private Bitmap[] iconRightBitmaps;

  private int iconSize;
  private int iconOuterWidth;
  private int iconOuterHeight;
  private int iconPadding;
  private ArgbEvaluator focusEvaluator = new ArgbEvaluator();
  Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
  StaticLayout textLayout;
  ObjectAnimator labelAnimator;
  ObjectAnimator labelFocusAnimator;
  ObjectAnimator bottomLinesAnimator;
  OnFocusChangeListener innerFocusChangeListener;
  OnFocusChangeListener outerFocusChangeListener;
  private ArrayList<METValidator> validators = new ArrayList<>();

  public MaterialAutoCompleteTextView(Context context) {
    super(context);
    init(context, null);
  }

  public MaterialAutoCompleteTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public MaterialAutoCompleteTextView(Context context, AttributeSet attrs, int style) {
    super(context, attrs, style);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    iconSize = getPixel(32);
    iconOuterWidth = getPixel(48);
    iconOuterHeight = getPixel(32);

    bottomSpacing = getResources().getDimensionPixelSize(R.dimen.inner_components_spacing);
    bottomEllipsisSize = getResources().getDimensionPixelSize(R.dimen.bottom_ellipsis_height);

    // retrieve the default baseColor
    int defaultBaseColor;
    TypedValue baseColorTypedValue = new TypedValue();
    context.getTheme().resolveAttribute(android.R.attr.windowBackground, baseColorTypedValue, true);
    defaultBaseColor = Colors.getBaseColor(baseColorTypedValue.data);

    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialEditText);
    baseColor = typedArray.getColor(R.styleable.MaterialEditText_baseColor, defaultBaseColor);
    setBaseColor(baseColor);

    // retrieve the default primaryColor
    int defaultPrimaryColor;
    TypedValue primaryColorTypedValue = new TypedValue();
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        context.getTheme().resolveAttribute(android.R.attr.colorPrimary, primaryColorTypedValue, true);
        defaultPrimaryColor = primaryColorTypedValue.data;
      } else {
        throw new RuntimeException("SDK_INT less than LOLLIPOP");
      }
    } catch (Exception e) {
      try {
        int colorPrimaryId = getResources().getIdentifier("colorPrimary", "attr", getContext().getPackageName());
        if (colorPrimaryId != 0) {
          context.getTheme().resolveAttribute(colorPrimaryId, primaryColorTypedValue, true);
          defaultPrimaryColor = primaryColorTypedValue.data;
        } else {
          throw new RuntimeException("colorPrimary not found");
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
    String fontPathForAccent = typedArray.getString(R.styleable.MaterialEditText_accentTypeface);
    if (fontPathForAccent != null && !isInEditMode()) {
      accentTypeface = getCustomTypeface(fontPathForAccent);
      textPaint.setTypeface(accentTypeface);
    }
    String fontPathForView = typedArray.getString(R.styleable.MaterialEditText_typeface);
    if (fontPathForView != null && !isInEditMode()) {
      typeface = getCustomTypeface(fontPathForView);
      setTypeface(typeface);
    }
    floatingLabelText = typedArray.getString(R.styleable.MaterialEditText_floatingLabelText);
    if (floatingLabelText == null) {
      floatingLabelText = getHint();
    }
    floatingLabelSpacing = typedArray.getDimensionPixelSize(R.styleable.MaterialEditText_floatingLabelSpacing, bottomSpacing);
    floatingLabelTextSize = typedArray.getDimensionPixelSize(R.styleable.MaterialEditText_floatingLabelTextSize, getResources().getDimensionPixelSize(R.dimen.floating_label_text_size));
    bottomTextSize = typedArray.getDimensionPixelSize(R.styleable.MaterialEditText_bottomTextSize, getResources().getDimensionPixelSize(R.dimen.bottom_text_size));
    hideUnderline = typedArray.getBoolean(R.styleable.MaterialEditText_hideUnderline, false);
    autoValidate = typedArray.getBoolean(R.styleable.MaterialEditText_autoValidate, false);
    iconLeftBitmaps = generateIconBitmaps(typedArray.getResourceId(R.styleable.MaterialEditText_iconLeft, -1));
    iconRightBitmaps = generateIconBitmaps(typedArray.getResourceId(R.styleable.MaterialEditText_iconRight, -1));
    iconPadding = typedArray.getDimensionPixelSize(R.styleable.MaterialEditText_iconPadding, getPixel(8));
    floatingLabelAlwaysShown = typedArray.getBoolean(R.styleable.MaterialEditText_floatingLabelAlwaysShown, false);
    helperTextAlwaysShown = typedArray.getBoolean(R.styleable.MaterialEditText_helperTextAlwaysShown, false);
    typedArray.recycle();

    int[] paddings = new int[]{
        android.R.attr.padding, // 0
        android.R.attr.paddingLeft, // 1
        android.R.attr.paddingTop, // 2
        android.R.attr.paddingRight, // 3
        android.R.attr.paddingBottom // 4
    };
    TypedArray paddingsTypedArray = context.obtainStyledAttributes(attrs, paddings);
    int padding = paddingsTypedArray.getDimensionPixelSize(0, 0);
    innerPaddingLeft = paddingsTypedArray.getDimensionPixelSize(1, padding);
    innerPaddingTop = paddingsTypedArray.getDimensionPixelSize(2, padding);
    innerPaddingRight = paddingsTypedArray.getDimensionPixelSize(3, padding);
    innerPaddingBottom = paddingsTypedArray.getDimensionPixelSize(4, padding);
    paddingsTypedArray.recycle();

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
    initMinBottomLines();
    initPadding();
    initText();
    initFloatingLabel();
    initTextWatcher();
    checkCharactersCount();
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

  private void initTextWatcher() {
    addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        checkCharactersCount();
        if (autoValidate) {
          validate();
        } else {
          setError(null);
        }
        postInvalidate();
      }
    });
  }

  private Typeface getCustomTypeface(@NonNull String fontPath) {
    return Typeface.createFromAsset(getContext().getAssets(), fontPath);
  }

  public void setIconLeft(@DrawableRes int res) {
    iconLeftBitmaps = generateIconBitmaps(res);
    initPadding();
  }

  public void setIconLeft(Bitmap bitmap) {
    iconLeftBitmaps = generateIconBitmaps(bitmap);
    initPadding();
  }

  public void setIconRight(@DrawableRes int res) {
    iconRightBitmaps = generateIconBitmaps(res);
    initPadding();
  }

  public void setIconRight(Bitmap bitmap) {
    iconRightBitmaps = generateIconBitmaps(bitmap);
    initPadding();
  }

  private Bitmap[] generateIconBitmaps(@DrawableRes int origin) {
    if (origin == -1) {
      return null;
    }
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(getResources(), origin, options);
    int size = Math.max(options.outWidth, options.outHeight);
    options.inSampleSize = size > iconSize ? size / iconSize : 1;
    options.inJustDecodeBounds = false;
    return generateIconBitmaps(BitmapFactory.decodeResource(getResources(), origin, options));
  }

  private Bitmap[] generateIconBitmaps(Bitmap origin) {
    if (origin == null) {
      return null;
    }
    Bitmap[] iconBitmaps = new Bitmap[4];
    origin = scaleIcon(origin);
    iconBitmaps[0] = origin.copy(Bitmap.Config.ARGB_8888, true);
    Canvas canvas = new Canvas(iconBitmaps[0]);
    canvas.drawColor(baseColor & 0x00ffffff | (Colors.isLight(baseColor) ? 0xff000000 : 0x8a000000), PorterDuff.Mode.SRC_IN);
    iconBitmaps[1] = origin.copy(Bitmap.Config.ARGB_8888, true);
    canvas = new Canvas(iconBitmaps[1]);
    canvas.drawColor(primaryColor, PorterDuff.Mode.SRC_IN);
    iconBitmaps[2] = origin.copy(Bitmap.Config.ARGB_8888, true);
    canvas = new Canvas(iconBitmaps[2]);
    canvas.drawColor(baseColor & 0x00ffffff | (Colors.isLight(baseColor) ? 0x4c000000 : 0x42000000), PorterDuff.Mode.SRC_IN);
    iconBitmaps[3] = origin.copy(Bitmap.Config.ARGB_8888, true);
    canvas = new Canvas(iconBitmaps[3]);
    canvas.drawColor(errorColor, PorterDuff.Mode.SRC_IN);
    return iconBitmaps;
  }

  private Bitmap scaleIcon(Bitmap origin) {
    int width = origin.getWidth();
    int height = origin.getHeight();
    int size = Math.max(width, height);
    if (size == iconSize) {
      return origin;
    } else if (size > iconSize) {
      int scaledWidth;
      int scaledHeight;
      if (width > iconSize) {
        scaledWidth = iconSize;
        scaledHeight = (int) (iconSize * ((float) height / width));
      } else {
        scaledHeight = iconSize;
        scaledWidth = (int) (iconSize * ((float) width / height));
      }
      return Bitmap.createScaledBitmap(origin, scaledWidth, scaledHeight, false);
    } else {
      return origin;
    }
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

  public boolean getFloatingLabelAlwaysShown() {
    return floatingLabelAlwaysShown;
  }

  public void setFloatingLabelAlwaysShown(boolean floatingLabelAlwaysShown) {
    this.floatingLabelAlwaysShown = floatingLabelAlwaysShown;
    invalidate();
  }

  public boolean getHelperTextAlwaysShown() {
    return helperTextAlwaysShown;
  }

  public void setHelperText(boolean helperTextAlwaysShown) {
    this.helperTextAlwaysShown = helperTextAlwaysShown;
    invalidate();
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

  public int getFloatingLabelTextSize() {
    return floatingLabelTextSize;
  }

  public void setFloatingLabelTextSize(int size) {
    floatingLabelTextSize = size;
    initPadding();
  }

  public int getBottomTextSize() {
    return bottomTextSize;
  }

  public void setBottomTextSize(int size) {
    bottomTextSize = size;
    initPadding();
  }

  private int getPixel(int dp) {
    return Density.dp2px(getContext(), dp);
  }

  private void initPadding() {
    extraPaddingTop = floatingLabelEnabled ? floatingLabelTextSize + floatingLabelSpacing : floatingLabelSpacing;
    textPaint.setTextSize(bottomTextSize);
    Paint.FontMetrics textMetrics = textPaint.getFontMetrics();
    extraPaddingBottom = (int) ((textMetrics.descent - textMetrics.ascent) * currentBottomLines) + (hideUnderline ? bottomSpacing : bottomSpacing * 2);
    extraPaddingLeft = iconLeftBitmaps == null ? 0 : (iconOuterWidth + iconPadding);
    extraPaddingRight = iconRightBitmaps == null ? 0 : (iconOuterWidth + iconPadding);
    setPaddings(innerPaddingLeft, innerPaddingTop, innerPaddingRight, innerPaddingBottom);
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
    innerPaddingLeft = left;
    innerPaddingRight = right;
    super.setPadding(left + extraPaddingLeft, top + extraPaddingTop, right + extraPaddingRight, bottom + extraPaddingBottom);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (changed) {
      adjustBottomLines();
    }
  }

  /**
   * @return True, if adjustments were made that require the view to be invalidated.
   */
  private boolean adjustBottomLines() {
    // Bail out if we have a zero width; lines will be adjusted during next layout.
    if (getWidth() == 0) {
      return false;
    }
    int destBottomLines;
    textPaint.setTextSize(bottomTextSize);
    if (tempErrorText != null || helperText != null) {
      Layout.Alignment alignment = (getGravity() & Gravity.RIGHT) == Gravity.RIGHT || isRTL() ?
          Layout.Alignment.ALIGN_OPPOSITE : (getGravity() & Gravity.LEFT) == Gravity.LEFT ?
          Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_CENTER;
      textLayout = new StaticLayout(tempErrorText != null ? tempErrorText : helperText, textPaint, getWidth() - getBottomTextLeftOffset() - getBottomTextRightOffset() - getPaddingLeft() - getPaddingRight(), alignment, 1.0f, 0.0f, true);
      destBottomLines = Math.max(textLayout.getLineCount(), minBottomTextLines);
    } else {
      destBottomLines = minBottomLines;
    }
    if (bottomLines != destBottomLines) {
      getBottomLinesAnimator(destBottomLines).start();
    }
    bottomLines = destBottomLines;
    return true;
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

  /**
   * get inner left padding, not the real paddingLeft
   */
  public int getInnerPaddingLeft() {
    return innerPaddingLeft;
  }

  /**
   * get inner right padding, not the real paddingRight
   */
  public int getInnerPaddingRight() {
    return innerPaddingRight;
  }

  private void initFloatingLabel() {
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
        if (floatingLabelEnabled) {
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
      }
    });
    // observe the focus state to animate the floating label's text color appropriately
    innerFocusChangeListener = new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (floatingLabelEnabled && highlightFloatingLabel) {
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
      }
    };
    super.setOnFocusChangeListener(innerFocusChangeListener);
  }

  public void setBaseColor(int color) {
    if (baseColor != color) {
      baseColor = color;
    }
    ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}, EMPTY_STATE_SET}, new int[]{baseColor & 0x00ffffff | 0xdf000000, baseColor & 0x00ffffff | 0x44000000});
    setTextColor(colorStateList);
    setHintTextColor(baseColor & 0x00ffffff | 0x44000000);
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
    initPadding();
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

  public boolean isAutoValidate() {
    return autoValidate;
  }

  public void setAutoValidate(boolean autoValidate) {
    this.autoValidate = autoValidate;
    if (autoValidate) {
      validate();
    }
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
    if (adjustBottomLines()) {
      postInvalidate();
    }
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
    if (adjustBottomLines()) {
      postInvalidate();
    }
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
    if (isValid) {
      setError(null);
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
  public MaterialAutoCompleteTextView addValidator(METValidator validator) {
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
      bottomLinesAnimator.cancel();
      bottomLinesAnimator.setFloatValues(destBottomLines);
    }
    return bottomLinesAnimator;
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    int startX = getScrollX() + (iconLeftBitmaps == null ? 0 : (iconOuterWidth + iconPadding));
    int endX = getScrollX() + (iconRightBitmaps == null ? getWidth() : getWidth() - iconOuterWidth - iconPadding);
    int lineStartY = getScrollY() + getHeight() - getPaddingBottom();

    // draw the icon(s)
    paint.setAlpha(255);
    if (iconLeftBitmaps != null) {
      Bitmap icon = iconLeftBitmaps[!isInternalValid() ? 3 : !isEnabled() ? 2 : hasFocus() ? 1 : 0];
      int iconLeft = startX - iconPadding - iconOuterWidth + (iconOuterWidth - icon.getWidth()) / 2;
      int iconTop = lineStartY + bottomSpacing - iconOuterHeight + (iconOuterHeight - icon.getHeight()) / 2;
      canvas.drawBitmap(icon, iconLeft, iconTop, paint);
    }
    if (iconRightBitmaps != null) {
      Bitmap icon = iconRightBitmaps[!isInternalValid() ? 3 : !isEnabled() ? 2 : hasFocus() ? 1 : 0];
      int iconRight = endX + iconPadding + (iconOuterWidth - icon.getWidth()) / 2;
      int iconTop = lineStartY + bottomSpacing - iconOuterHeight + (iconOuterHeight - icon.getHeight()) / 2;
      canvas.drawBitmap(icon, iconRight, iconTop, paint);
    }

    // draw the underline
    if (!hideUnderline) {
      lineStartY += bottomSpacing;
      if (!isInternalValid()) { // not valid
        paint.setColor(errorColor);
        canvas.drawRect(startX, lineStartY, endX, lineStartY + getPixel(2), paint);
      } else if (!isEnabled()) { // disabled
        paint.setColor(baseColor & 0x00ffffff | 0x44000000);
        float interval = getPixel(1);
        for (float xOffset = 0; xOffset < getWidth(); xOffset += interval * 3) {
          canvas.drawRect(startX + xOffset, lineStartY, startX + xOffset + interval, lineStartY + getPixel(1), paint);
        }
      } else if (hasFocus()) { // focused
        paint.setColor(primaryColor);
        canvas.drawRect(startX, lineStartY, endX, lineStartY + getPixel(2), paint);
      } else { // normal
        paint.setColor(baseColor & 0x00ffffff | 0x1E000000);
        canvas.drawRect(startX, lineStartY, endX, lineStartY + getPixel(1), paint);
      }
    }

    textPaint.setTextSize(bottomTextSize);
    Paint.FontMetrics textMetrics = textPaint.getFontMetrics();
    float relativeHeight = -textMetrics.ascent - textMetrics.descent;
    float bottomTextPadding = bottomTextSize + textMetrics.ascent + textMetrics.descent;

    // draw the characters counter
    if ((hasFocus() && hasCharatersCounter()) || !isCharactersCountValid()) {
      textPaint.setColor(isCharactersCountValid() ? getCurrentHintTextColor() : errorColor);
      String text;
      if (minCharacters <= 0) {
        text = isRTL() ? maxCharacters + " / " + getText().length() : getText().length() + " / " + maxCharacters;
      } else if (maxCharacters <= 0) {
        text = isRTL() ? "+" + minCharacters + " / " + getText().length() : getText().length() + " / " + minCharacters + "+";
      } else {
        text = isRTL() ? maxCharacters + "-" + minCharacters + " / " + getText().length() : getText().length() + " / " + minCharacters + "-" + maxCharacters;
      }

      canvas.drawText(text, isRTL() ? startX : endX - textPaint.measureText(text), lineStartY + bottomSpacing + relativeHeight, textPaint);
    }

    // draw the bottom text
    if (textLayout != null) {
      if (tempErrorText != null || ((helperTextAlwaysShown || hasFocus()) && !TextUtils.isEmpty(helperText))) { // error text or helper text
        textPaint.setColor(tempErrorText != null ? errorColor : helperTextColor != -1 ? helperTextColor : getCurrentHintTextColor());
        canvas.save();
        canvas.translate(startX + getBottomTextLeftOffset(), lineStartY + bottomSpacing - bottomTextPadding);
        textLayout.draw(canvas);
        canvas.restore();
      }
    }

    // draw the floating label
    if (floatingLabelEnabled && !TextUtils.isEmpty(floatingLabelText)) {
      textPaint.setTextSize(floatingLabelTextSize);
      // calculate the text color
      textPaint.setColor((Integer) focusEvaluator.evaluate(focusFraction, getCurrentHintTextColor(), primaryColor));

      // calculate the horizontal position
      float floatingLabelWidth = textPaint.measureText(floatingLabelText.toString());
      int floatingLabelStartX;
      if ((getGravity() & Gravity.RIGHT) == Gravity.RIGHT || isRTL()) {
        floatingLabelStartX = (int) (endX - floatingLabelWidth);
      } else if ((getGravity() & Gravity.LEFT) == Gravity.LEFT) {
        floatingLabelStartX = startX;
      } else {
        floatingLabelStartX = startX + (int) (getInnerPaddingLeft() + (getWidth() - getInnerPaddingLeft() - getInnerPaddingRight() - floatingLabelWidth) / 2);
      }

      // calculate the vertical position
      int floatingLabelStartY = innerPaddingTop + floatingLabelTextSize + floatingLabelSpacing;
      int distance = floatingLabelSpacing;
      int position = (int) (floatingLabelStartY - distance * (floatingLabelAlwaysShown ? 1 : floatingLabelFraction));

      // calculate the alpha
      int alpha = (int) ((floatingLabelAlwaysShown ? 1 : floatingLabelFraction) * 0xff * (0.74f * focusFraction + 0.26f));
      textPaint.setAlpha(alpha);

      // draw the floating label
      canvas.drawText(floatingLabelText.toString(), floatingLabelStartX, position, textPaint);
    }

    // draw the bottom ellipsis
    if (hasFocus() && singleLineEllipsis && getScrollX() != 0) {
      paint.setColor(primaryColor);
      float startY = lineStartY + bottomSpacing;
      int ellipsisStartX;
      if (isRTL()) {
        ellipsisStartX = endX;
      } else {
        ellipsisStartX = startX;
      }
      int signum = isRTL() ? -1 : 1;
      canvas.drawCircle(ellipsisStartX + signum * bottomEllipsisSize / 2, startY + bottomEllipsisSize / 2, bottomEllipsisSize / 2, paint);
      canvas.drawCircle(ellipsisStartX + signum * bottomEllipsisSize * 5 / 2, startY + bottomEllipsisSize / 2, bottomEllipsisSize / 2, paint);
      canvas.drawCircle(ellipsisStartX + signum * bottomEllipsisSize * 9 / 2, startY + bottomEllipsisSize / 2, bottomEllipsisSize / 2, paint);
    }

    // draw the original things
    super.onDraw(canvas);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  private boolean isRTL() {
    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) && getTextDirection() == TEXT_DIRECTION_RTL;
  }

  private int getBottomTextLeftOffset() {
    return isRTL() ? getCharactersCounterWidth() : getBottomEllipsisWidth();
  }

  private int getBottomTextRightOffset() {
    return isRTL() ? getBottomEllipsisWidth() : getCharactersCounterWidth();
  }

  private int getCharactersCounterWidth() {
    return hasCharatersCounter() ? (int) textPaint.measureText("00/000") : 0;
  }

  private int getBottomEllipsisWidth() {
    return singleLineEllipsis ? (bottomEllipsisSize * 5 + getPixel(4)) : 0;
  }

  public void checkCharactersCount() {
    charactersCountValid = !hasCharatersCounter() || getText() == null || getText().length() == 0 || (getText().length() >= minCharacters && (maxCharacters <= 0 || getText().length() <= maxCharacters));
  }

  public boolean isCharactersCountValid() {
    return charactersCountValid;
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