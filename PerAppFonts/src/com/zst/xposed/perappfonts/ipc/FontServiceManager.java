package com.zst.xposed.perappfonts.ipc;

import com.zst.xposed.perappfonts.helpers.FontLoader;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class FontServiceManager extends IFontService.Stub {
	/* Font Manager IPC Service
	 * 
	 * Thanks to help from these websites:
	 * http://stackoverflow.com/questions/19325010/aosp-stubs-vs-getsystemservice
	 * http://processors.wiki.ti.com/index.php/Android-Adding_SystemService
	 * https://github.com/SpazeDog/xposed-additions/commit/d22bb72a6b08ea409b7b5f6472f2fff22381ac62
	 */
	
	// Constants
	public static final String SERVICE_NAME = "PerAppFonts-Xposed-Server";
	
	static final String CLASS_SERVICE_MANAGER = "android.os.ServiceManager";
	static final String CLASS_CONTEXT_IMPL = "android.app.ContextImpl";
	
	// Local
	public FontLoader mFontLoader;

	private XSharedPreferences mMainPref;
	

	public static void initZygote(final XSharedPreferences mainPref) throws Throwable {
		final Class<?> classAMS = XposedHelpers.findClass(
				"com.android.server.am.ActivityManagerService", null);
		final Class<?> classSvcMgr = XposedHelpers.findClass(CLASS_SERVICE_MANAGER, null);
		final Class<?> classContextImpl = XposedHelpers.findClass(CLASS_CONTEXT_IMPL, null);
		
		XposedBridge.hookAllMethods(classAMS, "main", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				/*
				 * I had problems hooking ServerThread where the services are
				 * added directly so I decided to hook this instead.
				 */
				try {
					Log.d("zst123", "in hook run ActivityManagerService 1"); // XXX
					
					final Class<?>[] paramType = { String.class, IBinder.class };
					final FontServiceManager server = new FontServiceManager();
					server.mMainPref = mainPref;
					server.reloadFonts();
					
					XposedHelpers.callStaticMethod(classSvcMgr, "addService", paramType,
							SERVICE_NAME, server);
				} catch (Throwable e) {
					logXposed("Error hooking ActivityManagerService ==> See Logcat");
					e.printStackTrace();
					// We are hooking a system method and might cause a
					// bootloop if it throws any exception, log this throwable.
				}
			}
		});
		
		XposedBridge.hookAllMethods(classContextImpl, "getSystemService", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				/* Hook the getSystemService so any app get get our service */
				try {
					if (!(param.args[0] instanceof String))
						return;
					Log.d("zst123", "is inctance string 7ea70e9a78");
					
					if (((String) param.args[0]).equals(SERVICE_NAME)) {
						final Class<?>[] paramType = { String.class };
						Log.d("zst123", "inside system servec hook");
						Object server = XposedHelpers.callStaticMethod(classSvcMgr, "getService", paramType,
								SERVICE_NAME);
						param.setResult(server);
						Log.d("zst123", "Done System Service hook!!! :)");
					}
				} catch (Throwable e) {
					logXposed("Error hooking getSystemService ==> See Logcat");
					e.printStackTrace();
				}
			}
		});
	}
	
	public static IFontService retrieveService(Context context) {
		try {
			/*Method m = Class.forName(CLASS_SERVICE_MANAGER).getMethod("getService", String.class);
			IBinder binder = (IBinder) m.invoke(null, SERVICE_NAME);
			FontServiceManager server = (FontServiceManager) IFontService.Stub.asInterface(binder);*/
			IFontService server = IFontService.Stub.asInterface(
					(IBinder) context.getSystemService(SERVICE_NAME));
			Log.d("zst123", context.getPackageName()+" is trying to get font service");
			if (!server.asBinder().pingBinder()){
				Log.d("zst123", "FontServiceManager is not running");
			}
			return server;
		} catch (Exception e) {
			Log.d("zst123", "Error Retrieving FontServiceManager");
			e.printStackTrace();
			return null;
		}
	}
	
	static void logXposed(String t) {
		XposedBridge.log("PerAppFonts (FontServiceManager): " + t);
		Log.d("zst123", "PerAppFonts (FontServiceManager): " + t);
	}
	
	/*************************************************************************/
	
	public String getFontFolder() throws RemoteException {
		// TODO Auto-generated method stub
		return "isRunnin";
	}

	/** Re-create the fonts loader. <br> Use after changing font folder
	 * @return true if successful
	 */
	@Override
	public boolean reloadFonts() throws RemoteException {
		if (mMainPref != null) {
			mFontLoader = null;
			mFontLoader = new FontLoader(mMainPref);
			return true;
		}		
		return false;
	}

	public void getFonts(String font) throws RemoteException {
		
	}
	
	@Override
	public ParcelableTypeface findFonts(String font) throws RemoteException {
		if (mFontLoader != null) {
			Log.d("zst123", "inside findFont");
			return new ParcelableTypeface(mFontLoader.findFont(font));
		}
		Log.d("zst123", "outside findFont");
		return null;
	}
	
	
}
