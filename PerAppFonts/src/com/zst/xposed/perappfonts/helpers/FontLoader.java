package com.zst.xposed.perappfonts.helpers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.graphics.Typeface;

import com.zst.xposed.perappfonts.Common;

import de.robv.android.xposed.XSharedPreferences;

public class FontLoader {
	public Map<String, Typeface> map = new HashMap<String, Typeface>();

	public FontLoader(XSharedPreferences pref) {
		getFonts(pref.getString(Common.KEY_FOLDER_FONT, Common.DEFAULT_FOLDER_FONT));
	}

	public FontLoader(SharedPreferences pref) {
		getFonts(pref.getString(Common.KEY_FOLDER_FONT, Common.DEFAULT_FOLDER_FONT));
	}

	private void getFonts(String folder_string) {
		File folder = new File(folder_string);
		if (!folder.exists()) {
			if (!folder.mkdir())
				return;
		}
		if (!folder.isDirectory())
			return;
		File[] file_array = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".ttf"))
					return true;
				return false;
			}
		});
		
		for (File file : file_array) {
			map.put(file.getName(), Typeface.createFromFile(file));
		}
		
	}

	public Typeface findFont(String fontname) {
		if (map == null)
			return null;
		return map.get(fontname);

	}
}
