package com.rengwuxian.materialedittext;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.Hashtable;

public class TypefaceCache {
  private static final Hashtable<String, Typeface> FONT_CACHE = new Hashtable<>();

  public static Typeface get(String name, AssetManager assets) {
    synchronized (FONT_CACHE) {
      if (!FONT_CACHE.containsKey(name)) {
        FONT_CACHE.put(name, Typeface.createFromAsset(assets, name));
      }

      return FONT_CACHE.get(name);
    }
  }
}
