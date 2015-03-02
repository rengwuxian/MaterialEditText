package com.rengwuxian.materialedittext;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by Zhukai on 2014/5/29 0029.
 */
class Density {
  public static int dp2px(Context context, float dp) {
    Resources r = context.getResources();
    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    return Math.round(px);
  }
}
