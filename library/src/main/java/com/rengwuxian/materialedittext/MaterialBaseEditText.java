package com.rengwuxian.materialedittext;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextWatcher;
import android.util.AttributeSet;

import java.util.ArrayList;

/**
 * Created by admin.ilegra on 21/06/17.
 */

public class MaterialBaseEditText extends AppCompatEditText {

    ArrayList<TextWatcher> activeListeners = null;
    ArrayList<TextWatcher> pausedListeners = null;

    public MaterialBaseEditText(Context context) {
        super(context);
    }

    public MaterialBaseEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaterialBaseEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        if (activeListeners == null)
            activeListeners = new ArrayList<>();

        activeListeners.add(watcher);

        super.addTextChangedListener(watcher);

    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        if (activeListeners != null)
            activeListeners.remove(watcher);

        super.removeTextChangedListener(watcher);
    }

    public void pauseTextChangedListeners() {
        pausedListeners = activeListeners;

        for (TextWatcher listener: activeListeners) {
            super.removeTextChangedListener(listener);
        }

        activeListeners = null;
    }

    public void restartPausedTextChangedListeners() {
        if (pausedListeners == null)
            return;
        for (TextWatcher watcher: pausedListeners) {
            super.addTextChangedListener(watcher);
        }
        pausedListeners = null;
    }
}
