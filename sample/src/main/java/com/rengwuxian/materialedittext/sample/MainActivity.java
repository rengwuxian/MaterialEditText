package com.rengwuxian.materialedittext.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		initEnableBt();
		initSingleLineEllipsisEt();
	}

	private void initEnableBt() {
		final EditText basicEt = (EditText) findViewById(R.id.basicEt);
		final Button enableBt = (Button) findViewById(R.id.enableBt);
		enableBt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				basicEt.setEnabled(!basicEt.isEnabled());
				enableBt.setText(basicEt.isEnabled() ? "DISABLE" : "ENABLE");
			}
		});
	}

	private void initSingleLineEllipsisEt() {
		EditText singleLineEllipsisEt = (EditText) findViewById(R.id.singleLineEllipsisEt);
		singleLineEllipsisEt.setSelection(singleLineEllipsisEt.getText().length());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
