package com.zst.xposed.perappfonts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class AppLoader extends AsyncTaskLoader<List<AppEntry>> {

	private static final String TAG = AppLoader.class.getSimpleName();
	public final PackageManager mPm;

	public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
		private final Collator sCollator = Collator.getInstance();

		@Override
		public int compare(AppEntry object1, AppEntry object2) {

			String text1 = object1.getLabel();
			String text2 = object2.getLabel();

			if (text1 == null)
				text1 = object1.getPackageName();
			if (text2 == null)
				text2 = object1.getPackageName();

			return sCollator.compare(text1, text2);
		}
	};

	List<AppEntry> mApps;
	PackageIntentReceiver mPackageObserver;

	public AppLoader(Context context) {
		super(context);
		mPm = context.getPackageManager();
	}

	@Override
	public List<AppEntry> loadInBackground() {
		List<ApplicationInfo> apps = mPm.getInstalledApplications(0);

		if (apps == null) {
			apps = new ArrayList<ApplicationInfo>();
		}

		final Context context = getContext();

		List<AppEntry> entries = new ArrayList<AppEntry>();
		for (int i = 0; i < apps.size(); i++) {
			AppEntry entry = new AppEntry(this, apps.get(i));
			entry.loadLabel(context);
			entry.loadIcon(context);
			entries.add(entry);
			Log.i(TAG, "loaded :" + entry.getPackageName());
		}

		Collections.sort(entries, ALPHA_COMPARATOR);
		Log.i(TAG, "loaded apps count:" + entries.size());
		return entries;
	}

	@Override
	protected void onStartLoading() {
		if (mApps != null) {
			deliverResult(mApps);
		}
		if (mPackageObserver == null) {
			mPackageObserver = new PackageIntentReceiver(this);
		}
		if (mApps == null) {
			forceLoad();
		}
	}

	@Override
	protected void onReset() {
		super.onReset();
		onStopLoading();
		mApps = null;

		if (mPackageObserver != null) {
			getContext().unregisterReceiver(mPackageObserver);
			mPackageObserver = null;
		}
	}
	
	public static class PackageIntentReceiver extends BroadcastReceiver {
		final AppLoader mLoader;

		public PackageIntentReceiver(AppLoader loader) {
			mLoader = loader;
			IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
			filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
			filter.addDataScheme("package");
			mLoader.getContext().registerReceiver(this, filter);
			// Register for events related to sdcard installation.
			IntentFilter sdFilter = new IntentFilter();
			sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
			sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
			mLoader.getContext().registerReceiver(this, sdFilter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// Tell the loader about the change.
			mLoader.onContentChanged();
		}
	}
}
