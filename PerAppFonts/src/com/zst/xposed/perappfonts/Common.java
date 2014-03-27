package com.zst.xposed.perappfonts;

import android.os.Environment;

public class Common {
	
	public static final String PACKAGE_PER_APP_FONTS = Common.class.getPackage().getName();
	public static final String PACKAGE_ANDROID_SYSTEM = "android";
	
	//Settings syntax stuff
	public static final String SETTINGS_PREFIX_FONT_ASSET = "@asset/";
	public static final String SETTINGS_SUFFIX_INCOMPATIBLE = "@incompatible_font";
	
	// Preference file
	public static final String PREFERENCE_MAIN = "main_pref";
	public static final String PREFERENCE_WEIGHT = "font_weight";
	public static final String PREFERENCE_TYPEFACE = "font_typeface";
	public static final String PREFERENCE_FORCE = "force_fonts";

	// Preference Keys
	public static final String KEY_ENABLE_EVERY_APP = "enable_all_app";
	public static final String KEY_FOLDER_FONT = "folder_font";
	
	// Preference Default Values
	public static final String DEFAULT_FONT_TYPEFACE = SETTINGS_PREFIX_FONT_ASSET+ "0";
	public static final String DEFAULT_FONT_WEIGHT = "0";
	public static final String DEFAULT_FOLDER_FONT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Fonts/";
	
	// Keys for intent.putExtra
	public static final String EXTRAS_KEY_APP_PKG = "app_pkg";
	public static final String EXTRAS_KEY_APP_NAME = "app_name";	
}
