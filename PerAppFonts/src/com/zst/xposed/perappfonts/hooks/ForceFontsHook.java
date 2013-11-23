package com.zst.xposed.perappfonts.hooks;

import com.zst.xposed.perappfonts.Common;
import com.zst.xposed.perappfonts.helpers.FontHelper.FontType;

import android.graphics.Typeface;
import android.widget.TextView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ForceFontsHook {
	
	static FontType mFontType;
	static boolean mDone;
	
	public static boolean handleLoad(final LoadPackageParam lpp, XSharedPreferences force_pref, XSharedPreferences enabled_pref) {
		enabled_pref.reload();
		if (!enabled_pref.contains(lpp.packageName)) return false;
		force_pref.reload();
		if (!force_pref.contains(lpp.packageName)) return false;
		hookTextView(lpp);
		mFontType = AppsHook.mFontType;
		return true;
	}
	
	public static boolean handleLoadAllApps(final LoadPackageParam lpp, XSharedPreferences force_pref, XSharedPreferences enabled_pref) {
		enabled_pref.reload();
		if (!enabled_pref.contains(Common.PACKAGE_ANDROID_SYSTEM)) return false;
		force_pref.reload();
		if (!force_pref.contains(Common.PACKAGE_ANDROID_SYSTEM)) return false;
		hookTextView(lpp);
		mFontType = AppsHook.mFontType;
		return true;
	}
	
	private static void hookTextView(final LoadPackageParam lpp) {
		XposedHelpers.findAndHookMethod(TextView.class, "setTypeface", Typeface.class, int.class,
				new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						if (!AppsHook.isInitialFontSet) return;
						param.args[0] = mFontType.font;
						param.args[1] = mFontType.weight;
						mDone = true;
					}
				});
		
		XposedHelpers.findAndHookMethod(TextView.class, "setTypeface", Typeface.class,
				new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						if (!AppsHook.isInitialFontSet) return;
						// If our own hook hasnt finishes, dont override yet.
						if (mDone == true) {
							mDone = false;
							return;
						}
						param.args[0] = mFontType.font;
					}
				});
	}
}
