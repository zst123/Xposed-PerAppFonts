package com.zst.xposed.perappfonts.hooks;

import android.graphics.Typeface;
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
	
	public static boolean handleLoadApps(final LoadPackageParam lpp, XSharedPreferences font_pref, XSharedPreferences weight_pref) {
		font_pref.reload();
		if (!font_pref.contains(lpp.packageName)) return false;
		weight_pref.reload();
		String font = font_pref.getString(lpp.packageName, Common.DEFAULT_FONT_TYPEFACE);
		String weight = weight_pref.getString(lpp.packageName, Common.DEFAULT_FONT_WEIGHT);
		
		mFontType = FontHelper.parseValues(MainXposed.sModuleRes, MainXposed.sFontLoader, font, weight);
		hookTextView(lpp, XCallback.PRIORITY_HIGHEST);
		return true;
	}
	
	public static void handleAllApps(final LoadPackageParam lpp, XSharedPreferences font_pref, XSharedPreferences weight_pref) {
		font_pref.reload();
		if (!font_pref.contains(Common.PACKAGE_ANDROID_SYSTEM)) return;
		weight_pref.reload();
		String font = font_pref.getString(Common.PACKAGE_ANDROID_SYSTEM, Common.DEFAULT_FONT_TYPEFACE);
		String weight = weight_pref.getString(Common.PACKAGE_ANDROID_SYSTEM, Common.DEFAULT_FONT_WEIGHT);
		
		mFontType = FontHelper.parseValues(MainXposed.sModuleRes, MainXposed.sFontLoader, font, weight);
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
