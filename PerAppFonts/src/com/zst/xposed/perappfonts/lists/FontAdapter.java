package com.zst.xposed.perappfonts.lists;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zst.xposed.perappfonts.Common;
import com.zst.xposed.perappfonts.R;
import com.zst.xposed.perappfonts.helpers.FontHelper;
import com.zst.xposed.perappfonts.helpers.FontLoader;

public class FontAdapter extends BaseAdapter implements Filterable {

	private Handler mHandler;
	private Context mContext;
	private Resources mRes;
	private SharedPreferences mPref;
	protected List<FontItem> mFontsList = null;
	protected List<FontItem> mFilteredFontsList = new LinkedList<FontItem>();
	private LayoutInflater mLayoutInflater;

	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	public FontAdapter(Context context) {
		mContext = context;
		mRes = mContext.getResources();
		mPref = mContext.getSharedPreferences(Common.PREFERENCE_MAIN, Activity.MODE_WORLD_READABLE);
		mHandler = new Handler();
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void update(final View progressbar) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mFilteredFontsList) {
					mFilteredFontsList.clear();
					List<FontItem> temporaryList = new LinkedList<FontItem>();
					String[] asset_array = FontHelper.getAssetFontNames(mRes);
					for (int x = 0; x < asset_array.length; x++) {
						FontItem item = new FontItem();
						item.title = asset_array[x];
						item.font = FontHelper.getFontFromAssets(mRes, x);
						item.filename = Common.SETTINGS_PREFIX_FONT_ASSET + x;
						item.asset = true;
						temporaryList.add(item);
					}
					mFilteredFontsList = temporaryList;
					notifyDataSetChangedOnHandler();
					FontLoader loader = new FontLoader(mPref);
					Iterator<Entry<String, Typeface>> entrySetIter = loader.map.entrySet().iterator();
					while (entrySetIter.hasNext()) {
						Entry<String, Typeface> font = entrySetIter.next();
						FontItem item = new FontItem();
						item.title = font.getKey();
						item.filename = font.getKey();
						item.font = font.getValue();
						temporaryList.add(item);
					}
					Collections.sort(temporaryList, new Comparator<FontItem>() {
						@Override
						public int compare(FontItem first, FontItem second) {
							return Collator.getInstance().compare(first.title, second.title);
						}
					});
					mFilteredFontsList = temporaryList;
					notifyDataSetChangedOnHandler();
					toggleProgressBarVisible(progressbar, false);
				}
			}
		}).start();
	}

	private void toggleProgressBarVisible(final View v, final boolean b) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (v != null)
					v.setVisibility(b ? View.VISIBLE : View.GONE);
			}
		});
	}

	private void notifyDataSetChangedOnHandler() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
	}

	@Override
	public int getCount() {
		return mFilteredFontsList.size();
	}

	@Override
	public FontItem getItem(int position) {
		return mFilteredFontsList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mFilteredFontsList.get(position).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FontViewHolder holder;
		if (convertView != null) {
			holder = (FontViewHolder) convertView.getTag();
		} else {
			// http://www.doubleencore.com/2013/05/layout-inflation-as-intended/
			convertView = mLayoutInflater.inflate(R.layout.view_font_list, parent, false);
			holder = new FontViewHolder();
			holder.title = (TextView) convertView.findViewById(android.R.id.title);
			holder.msg = (TextView) convertView.findViewById(android.R.id.message);
			holder.background = (LinearLayout) convertView.findViewById(R.id.bg);
			convertView.setTag(holder);
		}
		FontItem fontItem = getItem(position);

		holder.title.setText(fontItem.title);
		holder.title.setTypeface(fontItem.font);

		holder.msg.setTypeface(fontItem.font);

		if (fontItem.asset) {
			holder.background.setBackgroundColor(Color.LTGRAY);
		} else {
			holder.background.setBackgroundResource(0);
		}

		return convertView;
	}

	@Override
	@SuppressLint("DefaultLocale")
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();// Holds the results
															// of a filtering
															// operation in
															// values
				List<FontItem> temporaryList = new LinkedList<FontItem>();

				if (mFontsList == null) {
					mFontsList = mFilteredFontsList;
					notifyDataSetChangedOnHandler();
				}
				if (TextUtils.isEmpty(constraint)) {
					results.count = mFontsList.size();
					results.values = mFontsList;
					// return original list.
				} else {
					String filterText = constraint.toString().toLowerCase();
					for (int i = 0; i < mFontsList.size(); i++) {
						FontItem item = mFontsList.get(i);
						String data = item.title.toString().toLowerCase();
						if (data.startsWith(filterText)) {
							temporaryList.add(item);
						}
					}
					mFilteredFontsList = temporaryList;
					results.count = temporaryList.size();
					results.values = temporaryList;
				}
				notifyDataSetChangedOnHandler();
				return results;
			}
		};
	}

	public class FontItem {
		public CharSequence title;
		public CharSequence filename;
		public Typeface font;
		public boolean asset;
		public boolean header;
	}

	static class FontViewHolder {
		TextView title;
		TextView msg;
		LinearLayout background;
	}
}