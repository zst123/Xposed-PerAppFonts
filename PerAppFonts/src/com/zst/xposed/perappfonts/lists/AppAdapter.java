package com.zst.xposed.perappfonts.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.zst.xposed.perappfonts.Common;
import com.zst.xposed.perappfonts.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class AppAdapter extends BaseAdapter implements Filterable {
	static PackageManager mPackageManager;
	
	Context mContext;
	Handler mHandler;
	SharedPreferences mAppPref;
	SharedPreferences mMainPref;
	
	protected List<PackageInfo> mInstalledAppInfo;
	protected List<AppItem> mInstalledApps = new LinkedList<AppItem>();
	protected List<PackageInfo> temporarylist;
	// temp. list holding the filtered items
	
	public static AppAdapter createAdapter(Context context) {
		mPackageManager = context.getPackageManager();
		List<PackageInfo> packages = mPackageManager
				.getInstalledPackages(PackageManager.GET_PERMISSIONS);
		return new AppAdapter(context, packages);
	}
	
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	public AppAdapter(Context context, List<PackageInfo> installedAppsInfo) {
		mInstalledAppInfo = installedAppsInfo;
		mContext = context;
		temporarylist = mInstalledAppInfo;
		mHandler = new Handler();
		mAppPref = mContext.getSharedPreferences(Common.PREFERENCE_APPS, Activity.MODE_WORLD_READABLE);
		mMainPref = mContext.getSharedPreferences(Common.PREFERENCE_MAIN, Activity.MODE_WORLD_READABLE);
	}
	
	public void update(final View progressbar) {
		toggleProgressBarVisible(progressbar, true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mInstalledApps) {
					mInstalledApps.clear();
					for (PackageInfo info : temporarylist) {
						final AppItem item = new AppItem();
						item.title = info.applicationInfo.loadLabel(mPackageManager);
						item.icon = info.applicationInfo.loadIcon(mPackageManager);
						item.packageName = info.packageName;
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								final int index = Collections.binarySearch(mInstalledApps, item);
								if (index < 0) {
									mInstalledApps.add((-index - 1), item);
									notifyDataSetChanged();
								}
							}
						});
					}
					toggleProgressBarVisible(progressbar, false);
				}
			}
		}).start();
	}
	
	private void toggleProgressBarVisible(final View v, final boolean b) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (v != null) v.setVisibility(b ? View.VISIBLE : View.GONE);
			}
		});
	}
	
	@Override
	public int getCount() {
		return mInstalledApps.size();
	}
	
	@Override
	public AppItem getItem(int position) {
		return mInstalledApps.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return mInstalledApps.get(position).packageName.hashCode();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView != null) {
			holder = (ViewHolder) convertView.getTag();
		} else {
			final LayoutInflater layoutInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.view_app_list, null, false);
			holder = new ViewHolder();
			convertView.setTag(holder);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.icon = (ImageView) convertView.findViewById(android.R.id.icon);
			holder.pkg = (TextView) convertView.findViewById(R.id.pkg);
		}
		AppItem appInfo = getItem(position);
		
		if (holder.name != null) {
			holder.name.setText(appInfo.title);
			boolean pref_enabled;
			if (appInfo.packageName.equals("android")) {
				pref_enabled = mMainPref.contains(appInfo.packageName);
			} else if (appInfo.packageName.equals("com.android.systemui")) {
				pref_enabled = mMainPref.contains(appInfo.packageName);
			} else {
				pref_enabled = mAppPref.contains(appInfo.packageName);
			}
			holder.name.setTextColor(pref_enabled ? Color.BLUE : Color.BLACK);

		}
		if (holder.pkg != null) {
			holder.pkg.setText(appInfo.packageName);
		}
		if (holder.icon != null) {
			Drawable loadIcon = appInfo.icon;
			holder.icon.setImageDrawable(loadIcon);
		}
		return convertView;
	}
	
	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {
			@Override
			@SuppressWarnings("unchecked")
			protected void publishResults(CharSequence constraint, FilterResults results) {
				temporarylist = (List<PackageInfo>) results.values;
				update(null);
			}
			
			@Override
			@SuppressLint("DefaultLocale")
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				ArrayList<PackageInfo> FilteredList = new ArrayList<PackageInfo>();
				
				if (TextUtils.isEmpty(constraint)) {
					// No filter implemented we return all the list
					results.values = mInstalledAppInfo;
					results.count = mInstalledAppInfo.size();
					return results;
				}
				
				for (int i = 0; i < mInstalledAppInfo.size(); i++) {
					String filterText = constraint.toString().toLowerCase();
					try {
						PackageInfo data = mInstalledAppInfo.get(i);
						if (data.applicationInfo.loadLabel(mPackageManager).toString()
								.toLowerCase().contains(filterText)) {
							FilteredList.add(data);
						} else if (data.packageName.contains(filterText)) {
							FilteredList.add(data);
						}
					} catch (Exception e) {
					}
				}
				results.values = FilteredList;
				results.count = FilteredList.size();
				return results;
			}
		};
		return filter;
	}
	
	public class AppItem implements Comparable<AppItem> {
		public CharSequence title;
		public String packageName;
		public Drawable icon;
		
		@Override
		public int compareTo(AppItem another) {
			return this.title.toString().compareTo(another.title.toString());
		}
	}
	
	static class ViewHolder {
		TextView name;
		ImageView icon;
		TextView pkg;
	}
}
