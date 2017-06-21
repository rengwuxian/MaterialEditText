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

    ArrayList<TextWatcher> listeners = null;

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
        if (listeners == null)
            listeners = new ArrayList<>();

        listeners.add(watcher);

        super.addTextChangedListener(watcher);

    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        if (listeners != null)
            listeners.remove(watcher);
        
        super.removeTextChangedListener(watcher);
    }

    public void pauseTextChangedListeners() {
        for (TextWatcher listener: listeners) {
            super.removeTextChangedListener(listener);
        }
    }

    public void restartPausedTextChangedListeners() {
        for (TextWatcher watcher: listeners) {
            super.addTextChangedListener(watcher);
        }
    }
}
