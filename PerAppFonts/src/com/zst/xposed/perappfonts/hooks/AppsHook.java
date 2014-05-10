package com.zst.xposed.perappfonts.hooks;

import android.graphics.Typeface;
import android.util.Log;
import android.widget.TextView;

import com.zst.xposed.perappfonts.Common;
import com.zst.xposed.perappfonts.helpers.FontHelper;
import com.zst.xposed.perappfonts.helpers.FontHelper.FontType;
import com.zst.xposed.perappfonts.ipc.FontServiceManager;
import com.zst.xposed.perappfonts.ipc.IFontService;

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
				final TextView tv1 = ((TextView) param.thisObject);
				
				IFontService mgr = FontServiceManager.retrieveService(tv1.getContext());
				if (mgr != null) {
					Log.d("test", tv1.getContext().getPackageName() + "//"+mgr.getFontFolder());
				}
				if (tv1.getContext().getPackageName().equals("com.android.settings")) {
					//mFontType = FontHelper.parseValues(MainXposed.sModuleRes, mgr.mFontLoader, "Syntax.ttf", Typeface.ITALIC+"");
					tv1.setTypeface(mgr.findFonts("Syntax.ttf").typeface);
					return;
				}
				
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
