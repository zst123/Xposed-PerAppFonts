package com.zst.xposed.perappfonts.lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.zst.xposed.perappfonts.AppEntry;
import com.zst.xposed.perappfonts.Common;
import com.zst.xposed.perappfonts.R;

public class AppAdapter extends ArrayAdapter<AppEntry> implements Filterable {

	private LayoutInflater mLayoutInflater;
	private AppEntryFilter mFilter;
	
	// Store not-filtered data
	private List<AppEntry> mData;
	
	private SharedPreferences mAppPref;
	
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	public AppAdapter(Context context) {
		super(context, R.layout.view_app_list);
		// TODO Move loading sharedPref to Loader
		mAppPref = context.getSharedPreferences(Common.PREFERENCE_TYPEFACE, Activity.MODE_WORLD_READABLE);
		mLayoutInflater = LayoutInflater.from(context);
	}

	public void setData(List<AppEntry> data) {
		clear();
		if (data != null) {
			addAll(data);
		}
		mData = data;
	}

	public List<AppEntry> getData() {
		return mData;
	}

	static class ViewHolder {
		TextView name;
		ImageView icon;
		TextView pkg;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView != null) {
			holder = (ViewHolder) convertView.getTag();
		} else {
			convertView = mLayoutInflater.inflate(R.layout.view_app_list, parent, false);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(android.R.id.icon);
			holder.name = (TextView) convertView.findViewById(android.R.id.text1);
			holder.pkg = (TextView) convertView.findViewById(android.R.id.text2);
			convertView.setTag(holder);
		}
		AppEntry appInfo = getItem(position);

		holder.name.setText(appInfo.getLabel());

		String packageName = appInfo.getPackageName();
		boolean pref_enabled = mAppPref.contains(packageName);
		
		holder.name.setTextColor(pref_enabled ? Color.BLUE : Color.BLACK);
		holder.pkg.setText(packageName);
		holder.icon.setImageDrawable(appInfo.getIcon());

		return convertView;
	}

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new AppEntryFilter(this);
		}
		return mFilter;
	}

	@Override
	public long getItemId(int position) {
		// For cursor/database etc. ignore it.
		return 0;
	}

	public static class AppEntryFilter extends Filter {
		private AppAdapter mAdapter;

		public AppEntryFilter(AppAdapter adapter) {
			mAdapter = adapter;
		}

		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();
			List<AppEntry> originalValues = mAdapter.getData();
			if (TextUtils.isEmpty(prefix)) {
				ArrayList<AppEntry> entries = new ArrayList<AppEntry>(originalValues);
				results.values = entries;
				results.count = entries.size();
			} else {
				Locale locale = Locale.getDefault();
				String prefixString = prefix.toString().toLowerCase(locale);

				final int count = originalValues.size();
				final ArrayList<AppEntry> newValues = new ArrayList<AppEntry>();

				for (int i = 0; i < count; i++) {
					AppEntry value = originalValues.get(i);

					if (value.getPackageName().startsWith(prefixString)) {
						newValues.add(value);
					} else {
						String label = value.getLabel().toLowerCase(locale);
						if (label.startsWith(prefixString)) {
							newValues.add(value);
						}
					}
				}
				results.values = newValues;
				results.count = newValues.size();
			}
			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mAdapter.clear();

			if (results.count > 0) {
				mAdapter.addAll((List<AppEntry>) results.values);
				mAdapter.notifyDataSetChanged();
			} else {
				mAdapter.notifyDataSetInvalidated();
			}
		}
	}

}
