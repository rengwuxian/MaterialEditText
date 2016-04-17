package com.rengwuxian.materialedittext;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class TypefaceCache {
  private static final Map<String, WeakReference<Typeface>> FONT_CACHE = new HashMap<>();

  public static Typeface get(String name, AssetManager assets) {
    if (!FONT_CACHE.containsKey(name)) {
      return createAndAddTypeFace(name, assets);
    }

    WeakReference<Typeface> reference = FONT_CACHE.get(name);
    Typeface typeFace = reference.get();

    if (typeFace != null) {
      return typeFace;
    }

    return createAndAddTypeFace(name, assets);
  }

  private static Typeface createAndAddTypeFace(String name, AssetManager assets) {
    Typeface typeface = Typeface.createFromAsset(assets, name);

    FONT_CACHE.put(name, new WeakReference<>(typeface));

    return typeface;
  }
}
