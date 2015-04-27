package com.rengwuxian.materialedittext.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by Charles Anderson on 4/27/15.
 */
public class TestActivity extends ActionBarActivity {
    private LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LinearLayout testView = (LinearLayout)findViewById(R.id.test_view);
        MaterialEditText nextEditor = (MaterialEditText) this.inflateView(R.layout.basic_edit, testView, false);
        nextEditor.setFloatingLabelText("Test Floating Label");
        nextEditor.setHint("Test Hint");
        nextEditor.setText("...");
        nextEditor.setText(null);
        nextEditor.setText("...");
        testView.addView(nextEditor);
    }

    private View inflateView(int resourceId, ViewGroup container, boolean attach) {
        if (mInflater == null)
            mInflater = LayoutInflater.from(this.getApplicationContext());
        return mInflater.inflate(resourceId, container, attach);
    }

}