package com.zst.xposed.perappfonts.helpers;

import java.io.File;

import com.zst.xposed.perappfonts.Common;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.Log;
import de.robv.android.xposed.XSharedPreferences;

public class FontLoader {
	
	public FontHolder[] array;
	
	public FontLoader(XSharedPreferences pref) {
		getFonts(pref.getString(Common.KEY_FOLDER_FONT, Common.DEFAULT_FOLDER_FONT));
	}
	
	public FontLoader(SharedPreferences pref) {
		getFonts(pref.getString(Common.KEY_FOLDER_FONT, Common.DEFAULT_FOLDER_FONT));
	}
	
	private void getFonts(String folder_string) {
		File folder = new File(folder_string);
		if (!folder.isDirectory()) return;
		
		File[] file_array = folder.listFiles();
		array = new FontHolder[file_array.length];
		for (int x = 0; x < file_array.length; x++) {
			File file = file_array[x];
			if (!file.getAbsolutePath().endsWith(".ttf")) {
				array[x] = null;
				continue;
			}
			FontHolder fh = new FontHolder();
			fh.font = Typeface.createFromFile(file);
			fh.name = file.getName();
			Log.d("test", "File: " + fh.name);
			
			array[x] = fh;
		}
	}
	
	public Typeface findFont(String fontname) {
		for (int x = 0; x < array.length; x++) {
			if (array[x] == null) continue;
			Log.d("test", "GOT = " + array[x].name + "-" + fontname);
			if (array[x].name.equals(fontname)) {
				Log.d("test", "FOUND = " + array[x].name + "=" + fontname);
				return array[x].font;
			}
		}
		return null;
		
	}
	
	public static class FontHolder {
		public Typeface font;
		public String name;
	}
}
