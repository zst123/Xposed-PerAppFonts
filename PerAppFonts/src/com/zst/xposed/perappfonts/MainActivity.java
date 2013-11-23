package com.zst.xposed.perappfonts;

import com.zst.xposed.perappfonts.lists.AppAdapter;
import com.zst.xposed.perappfonts.lists.AppAdapter.AppItem;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	/**
	 * This is the main activity with the app list
	 */
	
	/* Action Bar IDs */
	static final int MENU_REFRESH = 1;
	static final int MENU_PREFERENCE = 2;
	
	static AppAdapter mAppAdapter;
	static Toast mToast;
	static View mProgressBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_app_list);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		final ListView list = (ListView) findViewById(R.id.app_list);
		final EditText inputSearch = (EditText) findViewById(R.id.edittext_search);
		final ImageButton searchButton = (ImageButton) findViewById(R.id.button_search);
		mProgressBar = findViewById(R.id.progressbar);
		
		mAppAdapter = AppAdapter.createAdapter(this);
		mAppAdapter.update(mProgressBar);
		list.setAdapter(mAppAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View view, int pos, long id) {
				if (mProgressBar.getVisibility() == View.VISIBLE) {
					showToast(R.string.toast_warning);
					return;
				}
				AppItem info = (AppItem) av.getItemAtPosition(pos);
				openSettings(info);
			}
		});
		
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mAppAdapter.getFilter().filter(inputSearch.getText().toString());
			}
		});
	}
	
	public void showToast(int resId) {
		if (mToast == null) {
			mToast = Toast.makeText(this, resId, Toast.LENGTH_LONG);
			mToast.setGravity(Gravity.CENTER, 0, 0);
		}
		if (!mToast.getView().isShown()) {
			mToast.show();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem refresh_item = menu.add(Menu.NONE, MENU_REFRESH, 0, R.string.refresh);
		refresh_item.setIcon(R.drawable.ic_refresh);
		refresh_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		MenuItem setting_item = menu.add(Menu.NONE, MENU_PREFERENCE, 0, R.string.preference);
		setting_item.setIcon(R.drawable.ic_settings);
		setting_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem mi) {
		switch (mi.getItemId()) {
		case MENU_REFRESH:
			if (mProgressBar.getVisibility() != View.VISIBLE){
				mAppAdapter.update(findViewById(R.id.progressbar));
			}
			return true;
		case MENU_PREFERENCE:
			openPreference();
		}
		return false;
	}
	
	private void openPreference() {
        startActivity(new Intent(this, PrefActivity.class));
	}
	
	private void openSettings(AppItem info) {
		Intent i = new Intent(this, SettingsActivity.class);
		i.putExtra(Common.EXTRAS_KEY_APP_PKG, info.packageName);
		i.putExtra(Common.EXTRAS_KEY_APP_NAME, info.title);
		startActivity(i);
	}
}
