package com.zst.xposed.perappfonts;

import java.io.DataOutputStream;

import com.zst.xposed.perappfonts.helpers.FontHelper;
import com.zst.xposed.perappfonts.helpers.FontHelper.FontType;
import com.zst.xposed.perappfonts.helpers.FontLoader;
import com.zst.xposed.perappfonts.lists.FontAdapter;
import com.zst.xposed.perappfonts.lists.FontAdapter.FontItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SettingsActivity extends Activity implements View.OnClickListener,
		RadioGroup.OnCheckedChangeListener {
	
	/* ActionBar IDs */
	static final int MENU_LAUNCH = 0;
	static final int MENU_KILL = 1;
	/* Views */
	Switch mEnableSwitch;
	Switch mForceSwitch;
	TextView mFontPreview;
	RadioGroup mRadioGroup;
	Button mChangeFontButton;
	LinearLayout mFontLayout;
	LinearLayout mWeightLayout;
	LinearLayout mForceLayout;
	/* Font stuff */
	FontLoader mFontLoader;
	
	/* Current App */
	String mAppPkg;
	String mAppName;
	Drawable mIcon;
	
	/* Prefs */
	SharedPreferences mAppPref;
	SharedPreferences mMainPref;
	SharedPreferences mForcePref;
	SharedPreferences mCurrentPref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_settings);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		Bundle b = getIntent().getExtras();
		mAppPkg = b.getString(Common.EXTRAS_KEY_APP_PKG);
		mAppName = b.getCharSequence(Common.EXTRAS_KEY_APP_NAME).toString();
		try {
			mIcon = getPackageManager().getApplicationIcon(mAppPkg);
		} catch (Exception e) {
		}
		initialize();
	}
	
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	private void initialize() {
		mAppPref = getSharedPreferences(Common.PREFERENCE_APPS, MODE_WORLD_READABLE);
		mMainPref = getSharedPreferences(Common.PREFERENCE_MAIN, MODE_WORLD_READABLE);
		mForcePref = getSharedPreferences(Common.PREFERENCE_FORCE, MODE_WORLD_READABLE);
		mFontLoader = new FontLoader(mMainPref);
		
		getActionBar().setTitle(mAppName);
		getActionBar().setSubtitle(mAppPkg);
		getActionBar().setIcon(mIcon);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mFontLayout = (LinearLayout) findViewById(R.id.font_layout);
		mWeightLayout = (LinearLayout) findViewById(R.id.weight_layout);
		mFontPreview = (TextView) findViewById(R.id.font_preview);
		mEnableSwitch = (Switch) findViewById(R.id.enable_switch);
		mForceLayout = (LinearLayout) findViewById(R.id.force_layout);
		mForceSwitch = (Switch) findViewById(R.id.force_switch);
		mRadioGroup = (RadioGroup) findViewById(R.id.weight_radio_group);
		mChangeFontButton = (Button) findViewById(R.id.font_button);
		
		TextView note_text = (TextView) findViewById(R.id.note_text);
		TextView enabled_text = (TextView) findViewById(R.id.enable_text);
		note_text.setText((mAppPkg.equals("android")) ? R.string.please_note_android_system
				: R.string.please_note_app);
		enabled_text.setText((mAppPkg.equals("android")) ? R.string.enabled_android_system
				: R.string.enabled_app);
		
		if (mAppPkg.equals("android") || mAppPkg.equals("com.android.systemui")) {
			mCurrentPref = mMainPref;
		} else {
			mCurrentPref = mAppPref;
		}
		boolean isFound = mCurrentPref.contains(mAppPkg);
		mEnableSwitch.setChecked(isFound);
		changeLayoutVisibility(isFound);
		
		String raw_string = mCurrentPref.getString(mAppPkg, Common.DEFAULT_FONT_ALL_APPS);
		initPreferenceValues(raw_string);
		
		mChangeFontButton.setOnClickListener(this);
		mRadioGroup.setOnCheckedChangeListener(this);
		mEnableSwitch.setOnCheckedChangeListener(mSwitchListener);
		mForceSwitch.setOnCheckedChangeListener(mSwitchListener);
	}
	
	private void initPreferenceValues(String unparsed) {
		String raw_font_name = unparsed.split(Common.SETTINGS_SPLIT_SYMBOL)
				[Common.SETTINGS_INDEX_FONT];		
		FontType type_font = FontHelper.parsedPref(getResources(), unparsed, mFontLoader);
		String proper_font_name = FontHelper.parseFontSyntaxIntoName(getResources(), raw_font_name);
		mFontPreview.setText(proper_font_name + "  "
				+ getResources().getString(R.string.sample_text) + " ");
		mFontPreview.setTypeface(type_font.font, type_font.weight);
		
		switch (type_font.weight) {
		case Typeface.NORMAL:
			mRadioGroup.check(R.id.weight_radio0);
			break;
		case Typeface.BOLD:
			mRadioGroup.check(R.id.weight_radio1);
			break;
		case Typeface.ITALIC:
			mRadioGroup.check(R.id.weight_radio2);
			break;
		case Typeface.BOLD_ITALIC:
			mRadioGroup.check(R.id.weight_radio3);
			break;
		}
		if (mEnableSwitch.isChecked()) {
			mForceSwitch.setChecked(mForcePref.contains(mAppPkg));
		}
	}
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.weight_radio0:
			saveSettingsWeight(Typeface.NORMAL);
			break;
		case R.id.weight_radio1:
			saveSettingsWeight(Typeface.BOLD);
			break;
		case R.id.weight_radio2:
			saveSettingsWeight(Typeface.ITALIC);
			break;
		case R.id.weight_radio3:
			saveSettingsWeight(Typeface.BOLD_ITALIC);
			break;
		}
	}
	
	@Override
	public void onClick(View v) {
		if (mChangeFontButton.getId() == v.getId()) {
			showFontChooser();
		}
	}
	
	final CompoundButton.OnCheckedChangeListener mSwitchListener =
			new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton view, boolean isChecked) {
			if (view.getId() == R.id.enable_switch) {
				changeLayoutVisibility(isChecked);
			}
			if (view.getId() == R.id.force_switch) {
				saveSettingsForced(isChecked);
			}
		}
	};
	
	public void changeLayoutVisibility(boolean isChecked){
		mForceLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
		mWeightLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
		mFontLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
	}
	
	private void saveSettingsForced(boolean forced) {
		if (forced) {
			mForcePref.edit().putBoolean(mAppPkg, forced).commit();
		} else {
			mForcePref.edit().remove(mAppPkg).commit();
		}
		initPreferenceValues(mCurrentPref.getString(mAppPkg, Common.DEFAULT_FONT_ALL_APPS));
	}
	
	private void saveSettingsFont(String fontfile) {
		String raw_string = mCurrentPref.getString(mAppPkg, Common.DEFAULT_FONT_ALL_APPS);
		String weight = raw_string.split(Common.SETTINGS_SPLIT_SYMBOL)[Common.SETTINGS_INDEX_WEIGHT];
		String newValue = fontfile + Common.SETTINGS_SPLIT_SYMBOL + weight;
		mCurrentPref.edit().putString(mAppPkg, newValue).commit();
		initPreferenceValues(mCurrentPref.getString(mAppPkg, Common.DEFAULT_FONT_ALL_APPS));
	}
	
	private void saveSettingsWeight(int weight) {
		String raw_string = mCurrentPref.getString(mAppPkg, Common.DEFAULT_FONT_ALL_APPS);
		String font = raw_string.split(Common.SETTINGS_SPLIT_SYMBOL)[Common.SETTINGS_INDEX_FONT];
		String newValue = font + Common.SETTINGS_SPLIT_SYMBOL + weight;
		mCurrentPref.edit().putString(mAppPkg, newValue).commit();
		initPreferenceValues(mCurrentPref.getString(mAppPkg, Common.DEFAULT_FONT_ALL_APPS));
	}
	
	private void showKillPackageDialog(final String pkgToKill) {
		AlertDialog.Builder build = new AlertDialog.Builder(this);
		build.setMessage(R.string.kill_app_text);
		build.setNegativeButton(android.R.string.no, null);
		build.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				killPackage(pkgToKill);
			}
		});
		build.show();
	}
	
	// code modified from :
	// http://forum.xda-developers.com/showthread.php?t=2235956&page=6
	private void killPackage(String packageToKill) {
		if (packageToKill.equals("android")) return;
		try {
			Process su = Runtime.getRuntime().exec("su");
			if (su == null) return;
			DataOutputStream os = new DataOutputStream(su.getOutputStream());
			os.writeBytes("pkill " + packageToKill + "\n");
			os.writeBytes("exit\n");
			su.waitFor();
			os.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void showFontChooser() {
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View view = getLayoutInflater().inflate(R.layout.layout_app_list, null);
		builder.setView(view);
		
		final AlertDialog dialog = builder.create();
		final ListView listview = (ListView) view.findViewById(R.id.app_list);
		final EditText search_text = (EditText) view.findViewById(R.id.edittext_search);
		final ImageButton search_button = (ImageButton) view.findViewById(R.id.button_search);
		final View progressbar = view.findViewById(R.id.progressbar);
		final FontAdapter fontAdapter = new FontAdapter(this);
		
		fontAdapter.update(progressbar);
		listview.setAdapter(fontAdapter);
		listview.setPersistentDrawingCache(ViewGroup.PERSISTENT_ALL_CACHES);
		listview.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View view, int pos, long id) {
				FontItem info = (FontItem) av.getItemAtPosition(pos);
				saveSettingsFont(info.filename.toString());
				dialog.dismiss();
			}
		});
		search_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				fontAdapter.getFilter().filter(search_text.getText().toString());
			}
		});
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.show();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(!mEnableSwitch.isChecked()){
			mCurrentPref.edit().remove(mAppPkg).commit();
			mForcePref.edit().remove(mAppPkg).commit();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem killItem = menu.add(Menu.NONE, MENU_KILL, 0, R.string.kill_app);
		killItem.setIcon(R.drawable.ic_kill);
		killItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		MenuItem setting_item = menu.add(Menu.NONE, MENU_LAUNCH, 0, R.string.launch_app);
		setting_item.setIcon(R.drawable.ic_launch);
		setting_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		if (mAppPkg.equals("android")) {
			killItem.setVisible(false);
			setting_item.setVisible(false);
		} else if (mAppPkg.equals("com.android.systemui")) {
			setting_item.setVisible(false);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem mi) {
		switch (mi.getItemId()) {
		case MENU_LAUNCH:
			try {
				Intent intent = getPackageManager().getLaunchIntentForPackage(mAppPkg);
				startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(this, R.string.launch_app_error, Toast.LENGTH_LONG).show();
			}
			return true;
		case MENU_KILL:
			showKillPackageDialog(mAppPkg);
			return true;
		}
		return false;
	}
	
}
