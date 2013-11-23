package com.zst.xposed.perappfonts.hooks;

import android.graphics.Typeface;
import android.util.Log;
import android.widget.TextView;

import com.zst.xposed.perappfonts.Common;
import com.zst.xposed.perappfonts.helpers.FontHelper;
import com.zst.xposed.perappfonts.helpers.FontHelper.FontType;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.callbacks.XCallback;

public class AppsHook {
	
	public static boolean isInitialFontSet = false;
	public static FontType mFontType = new FontType();
	
	/*
	 * This method holds the hooks for SystemUI and Android System Server.
	 * It's separated from the rest of the apps due to the different
	 * XSharedPreference file. They are separated since there could be 100's
	 * of apps and we don't want to slow boot time by reading from a large file.
	 */
	public static boolean handleLoadSystem(final LoadPackageParam lpp, XSharedPreferences pref) {
		if (lpp.packageName.equals("android") || lpp.packageName.equals("com.android.systemui")) {
			pref.reload();
			if (!pref.contains(lpp.packageName)) return false;
			String unparsed = pref.getString(lpp.packageName, Common.DEFAULT_FONT_ALL_APPS);
			mFontType = FontHelper.parsedPref(MainXposed.sModuleRes, unparsed,
					MainXposed.sFontLoader);
			hookTextView(lpp, XCallback.PRIORITY_HIGHEST);
			return true;
		}
		return false;
	}
	
	public static boolean handleLoadApps(final LoadPackageParam lpp, XSharedPreferences pref) {
		pref.reload();
		if (!pref.contains(lpp.packageName)) return false;
		String unparsed = pref.getString(lpp.packageName, Common.DEFAULT_FONT_ALL_APPS);
		mFontType = FontHelper.parsedPref(MainXposed.sModuleRes, unparsed, MainXposed.sFontLoader);
		Log.d("test", "GGG=" + unparsed + lpp.packageName);
		hookTextView(lpp, XCallback.PRIORITY_HIGHEST);
		return true;
	}
	
	public static void handleAllApps(final LoadPackageParam lpp, XSharedPreferences pref) {
		pref.reload();
		String fontString = pref.getString(Common.KEY_FONT_EVERY_APP, Common.DEFAULT_FONT_EVERY_APP);
		mFontType = FontHelper.parsedPref(MainXposed.sModuleRes, fontString+Common.SETTINGS_SPLIT_SYMBOL+"0", MainXposed.sFontLoader);
		hookTextView(lpp, XCallback.PRIORITY_LOWEST);
	}
	
	private static void hookTextView(final LoadPackageParam lpp, final int priority) {
		XposedBridge.hookAllConstructors(TextView.class, new XC_MethodHook(priority) {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if (mFontType == null) return;
				isInitialFontSet = false;
				
				final TextView tv = ((TextView) param.thisObject);
				if (mFontType.weight == Typeface.NORMAL) {
					tv.setTypeface(mFontType.font);
				} else {
					tv.setTypeface(mFontType.font, mFontType.weight);
				}
				isInitialFontSet = true;
			}
		});
	}
}
