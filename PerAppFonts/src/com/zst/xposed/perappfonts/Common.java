package com.zst.xposed.perappfonts;

import android.os.Environment;

public class Common {
	
	public static final String PACKAGE_PER_APP_FONTS = Common.class.getPackage().getName();
	public static final String PACKAGE_ANDROID_SYSTEM = "android";
	
	//Settings syntax stuff
	public static final String SETTINGS_SPLIT_SYMBOL = ";";
	public static final String SETTINGS_PREFIX_FONT_ASSET = "@asset/";
	public static final int SETTINGS_INDEX_FONT = 0;
	public static final int SETTINGS_INDEX_WEIGHT = 1;
	
	// Preference file
	public static final String PREFERENCE_MAIN = "main";
	public static final String PREFERENCE_APPS = "app_settings";
	public static final String PREFERENCE_FORCE = "force_fonts";

	// Preference Keys
	public static final String KEY_ENABLE_EVERY_APP = "enable_all_app";
	public static final String KEY_FONT_ANDROID_SYSTEM = PACKAGE_ANDROID_SYSTEM;
	public static final String KEY_FONT_SYSTEMUI = "com.android.systemui";
	public static final String KEY_FOLDER_FONT = "folder_font";
	
	// Preference Default Values
	public static final String DEFAULT_FONT_ALL_APPS = SETTINGS_PREFIX_FONT_ASSET+ "0" + SETTINGS_SPLIT_SYMBOL + "0"; /*The format is "FONT;WEIGHT" */
	public static final String DEFAULT_FOLDER_FONT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Fonts/";
	
	// Keys for intent.putExtra
	public static final String EXTRAS_KEY_APP_PKG = "app_pkg";
	public static final String EXTRAS_KEY_APP_NAME = "app_name";	
}
