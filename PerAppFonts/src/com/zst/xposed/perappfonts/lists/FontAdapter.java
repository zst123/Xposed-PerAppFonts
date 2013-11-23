package com.zst.xposed.perappfonts.lists;

import java.util.LinkedList;
import java.util.List;

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

public class FontAdapter extends BaseAdapter implements Filterable{
	
	private Handler mHandler;
	private Context mContext;
	private Resources mRes;
	private SharedPreferences mPref;
	protected List<FontItem> mFontsList = null;
	protected List<FontItem> mFilteredFontsList = new LinkedList<FontItem>();

	
	public static FontAdapter createAdapter(Context context) {
		return new FontAdapter(context);
	}
	
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	public FontAdapter(Context context) {
		mContext = context;
		mRes = mContext.getResources();
		mPref = mContext.getSharedPreferences(Common.PREFERENCE_MAIN, Activity.MODE_WORLD_READABLE);
		mHandler = new Handler();
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
					if (loader.array != null) {
						for (int x = 0; x < loader.array.length; x++) {
							if (loader.array[x] == null) continue;
							FontItem item = new FontItem();
							item.title = loader.array[x].name;
							item.filename = loader.array[x].name;
							item.font = loader.array[x].font;
							temporaryList.add(item);
						}
					}
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
				if (v != null) v.setVisibility(b ? View.VISIBLE : View.GONE);
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
			final LayoutInflater layoutInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.view_font_list, null, false);
			holder = new FontViewHolder();
			convertView.setTag(holder);
			holder.title = (TextView) convertView.findViewById(android.R.id.title);
			holder.msg = (TextView) convertView.findViewById(android.R.id.message);
			holder.background = (LinearLayout) convertView.findViewById(R.id.bg);
		}
		FontItem fontItem = getItem(position);
		if (holder.title != null) {
			holder.title.setText(fontItem.title);
			holder.title.setTypeface(fontItem.font);
		}
		if (holder.msg != null) {
			holder.msg.setTypeface(fontItem.font);
		}
		if (holder.background != null) {
			if (fontItem.asset) {
				holder.background.setBackgroundColor(Color.LTGRAY);
			} else {
				holder.background.setBackground(null);
			}
		}
		return convertView;
	}
	
	@Override
	@SuppressLint("DefaultLocale")
	public Filter getFilter() {
		return new Filter() {
	        @Override
	        protected void publishResults(CharSequence constraint,FilterResults results) {
	        }
	        
	        @Override
	        protected FilterResults performFiltering(CharSequence constraint) {
	            FilterResults results = new FilterResults();// Holds the results of a filtering operation in values
	            List<FontItem> temporaryList = new LinkedList<FontItem>();

	            if (mFontsList == null){
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