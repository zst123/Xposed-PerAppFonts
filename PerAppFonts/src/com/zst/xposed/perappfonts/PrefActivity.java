package com.zst.xposed.perappfonts;

import com.zst.xposed.perappfonts.helpers.FontHelper;
import com.zst.xposed.perappfonts.lists.FontAdapter;
import com.zst.xposed.perappfonts.lists.FontAdapter.FontItem;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint("CommitPrefEdits")
@SuppressWarnings("deprecation")
public class PrefActivity extends PreferenceActivity {
	static final int REQUEST_DIRECTORY = 1;
	
	Preference prefFontFolder;
	SwitchPreference prefFontEverythingEnable;
	SwitchPreference prefFontEverythingForce;
	Preference prefFontEverythingChoose;

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
				startActivityForResult(new Intent(ctx, DirectoryChooserActivity.class), REQUEST_DIRECTORY);
				return true;
			}
		});
		root.addPreference(prefFontFolder);

		prefFontEverythingEnable = new SwitchPreference(this); 
		prefFontEverythingEnable.setTitle(getResources().getString(R.string.font_everything_enable_title));
		prefFontEverythingEnable.setSummary(getResources().getString(R.string.font_everything_enable_summary));
		prefFontEverythingEnable.setKey(Common.KEY_ENABLE_EVERY_APP);
		root.addPreference(prefFontEverythingEnable);

		prefFontEverythingForce = new SwitchPreference(this); 
		prefFontEverythingForce.setTitle(getResources().getString(R.string.font_everything_force_title));
		prefFontEverythingForce.setKey(Common.KEY_FORCE_EVERY_APP);
		root.addPreference(prefFontEverythingForce);

		prefFontEverythingChoose = new Preference(this); 
		prefFontEverythingChoose.setTitle(getResources().getString(R.string.font_everything_choose_title));
		prefFontEverythingChoose.setKey(Common.KEY_FONT_EVERY_APP);
		prefFontEverythingChoose.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference key) {
				showFontChooser(Common.KEY_FONT_EVERY_APP);
				return true;
			}
		});
		root.addPreference(prefFontEverythingChoose);
		
		setPreferenceScreen(root); 
		prefFontEverythingForce.setDependency(Common.KEY_ENABLE_EVERY_APP);
		prefFontEverythingChoose.setDependency(Common.KEY_ENABLE_EVERY_APP);

		updateSummary();
		return root;
	}
	
	private void updateSummary() {
		String folder_value = getPreferenceManager().getSharedPreferences()
				.getString(prefFontFolder.getKey(), Common.DEFAULT_FOLDER_FONT);
		prefFontFolder.setSummary(getResources().getString(R.string.font_folder_summary) + folder_value);
		String font_value = getPreferenceManager().getSharedPreferences()
				.getString(prefFontEverythingChoose.getKey(), Common.DEFAULT_FONT_EVERY_APP);
		prefFontEverythingChoose.setSummary(FontHelper.parseFontSyntaxIntoName(getResources(), font_value));
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_DIRECTORY) {
			if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
				Editor edit = getPreferenceManager().getSharedPreferences().edit();
				edit.putString(prefFontFolder.getKey(),
						data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
				edit.commit();
				updateSummary();
			} else {
				// Nothing selected
			}
		}
	}
	
	private void showFontChooser(final String key) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View view = getLayoutInflater().inflate(R.layout.layout_app_list, null);
		builder.setView(view);
		
		final AlertDialog dialog = builder.create();
		final ListView listview = (ListView) view.findViewById(R.id.app_list);
		final EditText search_text = (EditText) view.findViewById(R.id.edittext_search);
		final ImageButton search_button = (ImageButton) view.findViewById(R.id.button_search);
		final View progressbar = view.findViewById(R.id.progressbar);
		final FontAdapter fontAdapter = FontAdapter.createAdapter(this);
		
		fontAdapter.update(progressbar);
		listview.setAdapter(fontAdapter);
		listview.setPersistentDrawingCache(ViewGroup.PERSISTENT_ALL_CACHES);
		listview.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View view, int pos, long id) {
				FontItem info = (FontItem) av.getItemAtPosition(pos);
				Editor edit = getPreferenceManager().getSharedPreferences().edit();
				edit.putString(key, info.filename.toString());
				edit.commit();
				dialog.dismiss();
				updateSummary();
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
}
