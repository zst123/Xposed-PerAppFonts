package com.zst.xposed.perappfonts;

import java.util.List;

import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.zst.xposed.perappfonts.lists.AppAdapter;

public class MainActivity extends ListActivity implements OnQueryTextListener, LoaderCallbacks<List<AppEntry>> {
	/**
	 * This is the main activity with the app list
	 */
	private static final String TAG = MainActivity.class.getSimpleName();
	/* Action Bar IDs */
	private static final int MENU_PREFERENCE = 1;
	private static final int MENU_SEARCH = 2;

	private AppAdapter mAdapter;
	private String mCurFilter;
	private ProgressBar mIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_app);

		mAdapter = new AppAdapter(this);
		setListAdapter(mAdapter);
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View view, int pos, long id) {
				AppEntry item = (AppEntry) mAdapter.getItem(pos);
				String packageName = item.getPackageName();
				String label = item.getLabel();
				openSettings(packageName, label);
			}
		});
		mIndicator = (ProgressBar) findViewById(R.id.indicator);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem search_item = menu.add(Menu.NONE, MENU_SEARCH, 0, R.string.search);
		SearchView sv = new SearchView(this);
		sv.setOnQueryTextListener(this);
		search_item.setActionView(sv);
		search_item.setIcon(R.drawable.ic_search);
		search_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_ALWAYS);

		MenuItem setting_item = menu.add(Menu.NONE, MENU_PREFERENCE, 0, R.string.preference);
		setting_item.setIcon(R.drawable.ic_settings);
		setting_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem mi) {
		int menu_id = mi.getItemId();
		switch (menu_id) {
		case MENU_PREFERENCE:
			openPreference();
		}
		return false;
	}

	private void openPreference() {
		startActivity(new Intent(this, PrefActivity.class));
	}

	private void openSettings(String packageName, String label) {
		Intent i = new Intent(this, SettingsActivity.class);
		i.putExtra(Common.EXTRAS_KEY_APP_PKG, packageName);
		i.putExtra(Common.EXTRAS_KEY_APP_NAME, label);
		startActivity(i);
	}

	@Override
	public boolean onQueryTextChange(String query) {
		mCurFilter = !TextUtils.isEmpty(query) ? query : null;
		mAdapter.getFilter().filter(mCurFilter);
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return true;
	}

	@Override
	public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
		mIndicator.setVisibility(View.VISIBLE);
		return new AppLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<List<AppEntry>> loader, List<AppEntry> data) {
		Log.i(TAG, "onLoadFinished");
		mAdapter.setData(data);
		mIndicator.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<List<AppEntry>> loader) {
		Log.i(TAG, "onLoaderReset");
		mAdapter.setData(null);
	}
}
