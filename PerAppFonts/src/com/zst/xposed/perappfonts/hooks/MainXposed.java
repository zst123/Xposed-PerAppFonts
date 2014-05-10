package com.zst.xposed.perappfonts.hooks;

import com.zst.xposed.perappfonts.Common;
import com.zst.xposed.perappfonts.helpers.FontLoader;
import android.content.res.XModuleResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainXposed implements IXposedHookZygoteInit, IXposedHookLoadPackage {
	
	public XSharedPreferences sMainPref;
	public XSharedPreferences sAppPref;
	public XSharedPreferences sWeightPref;
	public XSharedPreferences sForcePref;
	
	public static XModuleResources sModuleRes;
	public static String MODULE_PATH = null;
	public static FontLoader sFontLoader;
	public static boolean sEveryAppFontEnabled;
	public static boolean sEveryAppFontForced;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		sMainPref = new XSharedPreferences(Common.PACKAGE_PER_APP_FONTS, Common.PREFERENCE_MAIN);
		sAppPref = new XSharedPreferences(Common.PACKAGE_PER_APP_FONTS, Common.PREFERENCE_TYPEFACE);
		sWeightPref = new XSharedPreferences(Common.PACKAGE_PER_APP_FONTS, Common.PREFERENCE_WEIGHT);
		sForcePref = new XSharedPreferences(Common.PACKAGE_PER_APP_FONTS, Common.PREFERENCE_FORCE);
		MODULE_PATH = startupParam.modulePath;
		sModuleRes = XModuleResources.createInstance(MODULE_PATH, null);
		
		sFontLoader = new FontLoader(sMainPref);
		/*
		 * Why did I preload all fonts instead of on demand? It's because Google introduced
		 * READ_EXTERNAL_STORAGE permission.
		 * 
		 * Everything in handleLoadPackage is actually in the app context itself. So we hold
		 * their permissions. Some apps don't have READ/WRITE_EXTERNAL_STORAGE so we have to
		 * use a work-around. (preloading it on boot)
		 * 
		 * Also, everything "static" that's in handleLoadPackage cannot be passed to another
		 * handleLoadPackage process. Retrieved Context (with the permission) from the System
		 * won't be passed to another app.
		 * 
		 * I did this as my last resort. If anyone finds a more efficient method, please
		 * inform me(zst123).
		 */
		sEveryAppFontEnabled = sMainPref.getBoolean(Common.KEY_ENABLE_EVERY_APP, false);
	}
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpp) throws Throwable {
		if (sEveryAppFontEnabled){
			AppsHook.handleAllApps(lpp, sAppPref, sWeightPref);
		}
		
		boolean hook = AppsHook.handleLoadApps(lpp, sAppPref, sWeightPref);
		ForceFontsHook.handleLoad(lpp, sForcePref, sAppPref);

		if (sEveryAppFontEnabled && !hook) {
			ForceFontsHook.handleLoadAllApps(lpp, sForcePref, sAppPref);
		}
	}
	
}
