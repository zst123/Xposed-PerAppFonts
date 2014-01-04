package com.zst.xposed.perappfonts;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

@SuppressLint("CommitPrefEdits")
@SuppressWarnings("deprecation")
public class PrefActivity extends PreferenceActivity {
	static final int REQUEST_DIRECTORY = 1;
	
	Preference prefFontFolder;
	SwitchPreference prefFontEverythingEnable;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getPreferenceManager().setSharedPreferencesMode(PreferenceActivity.MODE_WORLD_READABLE);
		getPreferenceManager().setSharedPreferencesName(Common.PREFERENCE_MAIN);
		setPreferenceScreen(createPreferences(this));
	}
	
	private PreferenceScreen createPreferences(final Context ctx) {
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
		prefFontFolder = new Preference(this); 
		prefFontFolder.setTitle(getResources().getString(R.string.font_folder_title));
		prefFontFolder.setKey(Common.KEY_FOLDER_FONT);
		prefFontFolder.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent i = new Intent(ctx, DirectoryChooserActivity.class);
				i.putExtra(DirectoryChooserActivity.EXTRA_INITIAL_DIRECTORY, "/sdcard/");
				startActivityForResult(i, REQUEST_DIRECTORY);
				return true;
			}
		});
		root.addPreference(prefFontFolder);

		prefFontEverythingEnable = new SwitchPreference(this); 
		prefFontEverythingEnable.setTitle(getResources().getString(R.string.font_everything_enable_title));
		prefFontEverythingEnable.setSummary(getResources().getString(R.string.font_everything_enable_summary));
		prefFontEverythingEnable.setKey(Common.KEY_ENABLE_EVERY_APP);
		root.addPreference(prefFontEverythingEnable);

		updateSummary();
		return root;
	}
	
	private void updateSummary() {
		String folder_value = getPreferenceManager().getSharedPreferences()
				.getString(prefFontFolder.getKey(), Common.DEFAULT_FOLDER_FONT);
		prefFontFolder.setSummary(getResources().getString(R.string.font_folder_summary) + folder_value);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_DIRECTORY) {
			if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
				Editor edit = getPreferenceManager().getSharedPreferences().edit();
				edit.putString(prefFontFolder.getKey(),
						data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
				edit.commit();
				showDirectoryWarning(data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
				updateSummary();
			} else {
				// Nothing selected
			}
		}
	}
	
	private void showDirectoryWarning(String dir) {
		if (!dir.startsWith("/mnt/emulated"))
			if (!dir.startsWith("/storage/emulated"))
					if (!dir.startsWith("/mnt/shell/emulated"))
						return;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.font_folder_warning);
		builder.setPositiveButton(android.R.string.yes, null);
		builder.create().show();
	}
}
