package com.rengwuxian.materialedittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * EditText in Material Design
 * <p/>
 * author：rengwuxian date：2014/8/29 0029
 * <p/>
 */
public class MaterialEditText extends EditText {
	/**
	 * padding to the top of the top text (main text or floating label).
	 */
	private final int innerPaddingTop;

	/**
	 * padding to the bottom of the main text.
	 */
	private final int innerPaddingBottom;

	/**
	 * padding to the left of the main text.
	 */
	private final int innerPaddingLeft;

	/**
	 * padding to the right of the main text
	 */
	private final int innerPaddingRight;

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
	private final boolean floatingLabelEnabled;

	/**
	 * whether to highlight the floating label's text color when focused (with the main color). default is true.
	 */
	private final boolean highlightFloatingLabel;

	/**
	 * whether in a dark theme. Used to decide the default textColor, hintTextColor and mainColor. default is false.
	 */
	private final boolean darkTheme;

	/**
	 * the underline's highlight color, and the highlight color of the floating label if app:highlightFloatingLabel is set true in the xml. default is black(when app:darkTheme is false) or white(when app:darkTheme is true)
	 */
	private final int mainColor;

	/**
	 * the color for when something is wrong.(e.g. exceeding max characters)
	 */
	private final int errorColor;

	/**
	 * characters count limit. 0 means no limit. default is 0. NOTE: the character counter will increase the View's height, unless in the full width single line mode(using android:singleLine= true).
	 */
	// todo full with 是否要实现？ 上面的注释最终怎么写？
	private final int maxCharacters;

	/**
	 * whether to show the bottom ellipsis in singleLine mode. default is false. NOTE: the bottom ellipsis will increase the View's height.
	 */
	private final boolean bottomEllipsis;

	/**
	 * bottom ellipsis's height
	 */
	private final int bottomEllipsisHeight;

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

	private ArgbEvaluator focusEvaluator = new ArgbEvaluator();
	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	ObjectAnimator showLabelAnimator;
	ObjectAnimator labelFocusAnimator;
	OnFocusChangeListener interFocusChangeListener;
	OnFocusChangeListener outerFocusChangeListener;

	public MaterialEditText(Context context) {
		this(context, null);
	}

	public MaterialEditText(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MaterialEditText(Context context, AttributeSet attrs, int style) {
		super(context, attrs, style);

		setFocusable(true);
		setFocusableInTouchMode(true);
		setClickable(true);

		floatingLabelTextSize = getResources().getDimensionPixelSize(R.dimen.floating_label_size);
		innerComponentsSpacing = getResources().getDimensionPixelSize(R.dimen.inner_components_spacing);
		innerPaddingTop = getResources().getDimensionPixelSize(R.dimen.inner_padding_top);
		innerPaddingBottom = getResources().getDimensionPixelSize(R.dimen.inner_padding_bottom);
		innerPaddingLeft = getResources().getDimensionPixelSize(R.dimen.inner_padding_left);
		innerPaddingRight = getResources().getDimensionPixelSize(R.dimen.inner_padding_right);
		bottomEllipsisHeight = getResources().getDimensionPixelSize(R.dimen.bottom_ellipsis_height);

		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialEditText);
		darkTheme = typedArray.getBoolean(R.styleable.MaterialEditText_darkTheme, false);
		setTextColor(getResources().getColor(darkTheme ? R.color.white_text_primary : R.color.black_text_primary));
		setHintTextColor(getResources().getColor(darkTheme ? R.color.white_text_hint : R.color.black_text_hint));
		mainColor = typedArray.getColor(R.styleable.MaterialEditText_mainColor, darkTheme ? Color.WHITE : Color.BLACK);
		floatingLabelEnabled = typedArray.getBoolean(R.styleable.MaterialEditText_floatLabel, false);
		highlightFloatingLabel = typedArray.getBoolean(R.styleable.MaterialEditText_highlightLabel, true);
		errorColor = typedArray.getColor(R.styleable.MaterialEditText_errorColor, getResources().getColor(R.color.red_error));
		maxCharacters = typedArray.getInt(R.styleable.MaterialEditText_maxCharacters, 0);
		bottomEllipsis = typedArray.getBoolean(R.styleable.MaterialEditText_singleLineEllipsis, false);
		typedArray.recycle();

		initPadding();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			setBackground(null);
		} else {
			setBackgroundDrawable(null);
		}
		initFloatingLabel();
	}

	private float getFloatingLabelFraction() {
		return floatingLabelFraction;
	}

	private void setFloatingLabelFraction(float floatingLabelFraction) {
		this.floatingLabelFraction = floatingLabelFraction;
		invalidate();
	}

	private float getFocusFraction() {
		return focusFraction;
	}

	private void setFocusFraction(float focusFraction) {
		this.focusFraction = focusFraction;
		invalidate();
	}

	private int getPixel(int dp) {
		return Density.dp2px(getContext(), dp);
	}

	private void initPadding() {
		int extraPaddingTop = floatingLabelTextSize + innerComponentsSpacing;
		int extraPaddingBottom = maxCharacters > 0 ? floatingLabelTextSize + innerComponentsSpacing : bottomEllipsis ? innerComponentsSpacing + bottomEllipsisHeight : 0;
		if (floatingLabelEnabled) {
			setPadding(innerPaddingLeft, extraPaddingTop + innerPaddingTop, innerPaddingRight, innerPaddingBottom + extraPaddingBottom);
		} else {
			setPadding(innerPaddingLeft, innerPaddingTop, innerPaddingRight, innerPaddingBottom + extraPaddingBottom);
		}
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
							getShowLabelAnimator().reverse();
						}
					} else if (!floatingLabelShown) {
						floatingLabelShown = true;
						if (getShowLabelAnimator().isStarted()) {
							getShowLabelAnimator().reverse();
						} else {
							getShowLabelAnimator().start();
						}
					}
				}
			});
			if (highlightFloatingLabel) {
				// observe the focus state to animate the floating label's text color appropriately
				interFocusChangeListener = new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							if (getLabelFocusAnimator().isStarted())
								getLabelFocusAnimator().reverse();
							else {
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
				super.setOnFocusChangeListener(interFocusChangeListener);
			}
		}
	}

	@Override
	public void setOnFocusChangeListener(OnFocusChangeListener listener) {
		if (interFocusChangeListener == null) {
			super.setOnFocusChangeListener(listener);
		} else {
			outerFocusChangeListener = listener;
		}
	}

	private ObjectAnimator getShowLabelAnimator() {
		if (showLabelAnimator == null) {
			showLabelAnimator = ObjectAnimator.ofFloat(this, "floatingLabelFraction", 0f, 1f);
		}
		return showLabelAnimator;
	}

	private ObjectAnimator getLabelFocusAnimator() {
		if (labelFocusAnimator == null) {
			labelFocusAnimator = ObjectAnimator.ofFloat(this, "focusFraction", 0f, 1f);
		}
		return labelFocusAnimator;
	}

	@Override
	protected void onDraw(@NonNull Canvas canvas) {
		// set the textSize
		paint.setTextSize(floatingLabelTextSize);

		// draw the background
		float lineStartY = getHeight() - getPaddingBottom() + getPixel(8);
		if (hasFocus()) {
			boolean exceedingMaxCharacters = false;
			if (maxCharacters > 0 && getText() != null && maxCharacters < getText().length()) {
				exceedingMaxCharacters = true;
				paint.setColor(errorColor);
			} else {
				paint.setColor(mainColor);
			}
			canvas.drawRect(getScrollX(), lineStartY, getWidth() + getScrollX(), lineStartY + getPixel(2), paint);

			// draw the characters counter
			if (maxCharacters > 0) {
				if (!exceedingMaxCharacters) {
					paint.setColor(getCurrentHintTextColor());
				}
				String text = getText().length() + " / " + maxCharacters;
				canvas.drawText(text, getWidth() + getScrollX() - paint.measureText(text), lineStartY + innerComponentsSpacing + floatingLabelTextSize, paint);
			}
		} else {
			paint.setColor(darkTheme ? Color.WHITE : Color.BLACK);
			canvas.drawRect(getScrollX(), lineStartY, getWidth() + getScrollX(), lineStartY + getPixel(1), paint);
		}

		// draw the floating label
		if (floatingLabelEnabled && !TextUtils.isEmpty(getHint())) {
			// calculate the text color
			paint.setColor((Integer) focusEvaluator.evaluate(focusFraction, getHintTextColors().getColorForState(EMPTY_STATE_SET, darkTheme ? Color.WHITE : Color.BLACK), mainColor));

			// calculate the vertical position
			int start = innerPaddingTop + floatingLabelTextSize + innerComponentsSpacing;
			int distance = innerComponentsSpacing + getPixel(2);
			int position = (int) (start - distance * floatingLabelFraction);

			// calculate the alpha
			int alpha = (int) (floatingLabelFraction * 0xff * (0.74f * focusFraction + 0.26f));
			paint.setAlpha(alpha);

			// draw the floating label
			canvas.drawText(getHint().toString(), innerPaddingLeft + getScrollX(), position, paint);
		}

		// draw the bottom ellipsis
		if (hasFocus() && bottomEllipsis && getScrollX() != 0) {
			paint.setColor(mainColor);
			float startY = lineStartY + innerComponentsSpacing;
			canvas.drawCircle(getPixel(2) + getScrollX(), startY + getPixel(2), getPixel(2), paint);
			canvas.drawCircle(getPixel(2 + 8) + getScrollX(), startY + getPixel(2), getPixel(2), paint);
			canvas.drawCircle(getPixel(2 + 16) + getScrollX(), startY + getPixel(2), getPixel(2), paint);
		}

		// draw the original things
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (bottomEllipsis && getScrollX() > 0 && event.getAction() == MotionEvent.ACTION_DOWN && event.getX() < getPixel(4 * 5) && event.getY() > getHeight() - innerPaddingBottom * 2 + getPixel(4)) {
			setSelection(0);
			return false;
		}
		return super.onTouchEvent(event);
	}
}