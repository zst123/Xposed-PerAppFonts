package com.zst.xposed.perappfonts.helpers;

import com.zst.xposed.perappfonts.Common;
import com.zst.xposed.perappfonts.R;

import android.content.res.Resources;
import android.graphics.Typeface;

public class FontHelper {
	
	/* Index of fonts path names */
	private static final int INDEX_FONT_DEFAULT = 0;
	private static final int INDEX_FONT_MONOSPACE = 1;
	private static final int INDEX_FONT_SERIF = 2;
	private static final int INDEX_FONT_SANS = 3;
	private static final int INDEX_FONT_ROBOTO_KITKAT = 4;
	private static final int INDEX_FONT_STOROPIA = 5;
	private static final int INDEX_FONT_ROSEMARY = 6;
	private static final int INDEX_FONT_ROBOTO_SLAB = 7;
	private static final int INDEX_MAX = INDEX_FONT_ROBOTO_SLAB;
	
	/* Assets path names */
	private static final String ASSET_FONT_FOLDER = "fonts/";
	private static final String ASSET_FONT_ROBOTO_KITKAT = ASSET_FONT_FOLDER + "KitKat-RobotoCondensed-Regular.ttf";
	private static final String ASSET_FONT_STOROPIA = ASSET_FONT_FOLDER + "Storopia.ttf";
	private static final String ASSET_FONT_ROSEMARY = ASSET_FONT_FOLDER + "Rosemary.ttf";
	private static final String ASSET_FONT_ROBOTO_SLAB = ASSET_FONT_FOLDER + "RobotoSlab-Regular.ttf";
	
	public static String[] getAssetFontNames(Resources res) {
		String[] str = new String[INDEX_MAX + 1];
		str[INDEX_FONT_DEFAULT] = res.getString(R.string.font_default);
		str[INDEX_FONT_MONOSPACE] = res.getString(R.string.font_monospace);
		str[INDEX_FONT_SERIF] = res.getString(R.string.font_serif);
		str[INDEX_FONT_SANS] = res.getString(R.string.font_sans);
		str[INDEX_FONT_ROBOTO_KITKAT] = res.getString(R.string.font_roboto_kitkat);
		str[INDEX_FONT_STOROPIA] = res.getString(R.string.font_storopia);
		str[INDEX_FONT_ROSEMARY] = res.getString(R.string.font_rosemary);
		str[INDEX_FONT_ROBOTO_SLAB] = res.getString(R.string.font_roboto_slab);
		return str;
		
	}
	
	public static String parseFontSyntaxIntoName(Resources res, String unparsed) {
		if (unparsed.startsWith(Common.SETTINGS_PREFIX_FONT_ASSET)) {
			unparsed = unparsed.substring(Common.SETTINGS_PREFIX_FONT_ASSET.length());
			return getAssetFontNames(res)[Integer.parseInt(unparsed)];
		} else {
			// Fonts not from assets are already their name.
			return unparsed;
		}
	}
	
	public static Typeface getFontFromAssets(Resources res, int index) {
		switch (index) {
		case INDEX_FONT_MONOSPACE:
			return Typeface.MONOSPACE;
		case INDEX_FONT_SERIF:
			return Typeface.SERIF;
		case INDEX_FONT_SANS:
			return Typeface.SANS_SERIF;
		case INDEX_FONT_ROBOTO_KITKAT:
			return Typeface.createFromAsset(res.getAssets(), ASSET_FONT_ROBOTO_KITKAT);
		case INDEX_FONT_STOROPIA:
			return Typeface.createFromAsset(res.getAssets(), ASSET_FONT_STOROPIA);
		case INDEX_FONT_ROSEMARY:
			return Typeface.createFromAsset(res.getAssets(), ASSET_FONT_ROSEMARY);
		case INDEX_FONT_ROBOTO_SLAB:
			return Typeface.createFromAsset(res.getAssets(), ASSET_FONT_ROBOTO_SLAB);
		}
		return null;
	}
	
	public static Typeface getFontFromAssets(Resources res, String index) {
		return getFontFromAssets(res, Integer.parseInt(index));
	}
	
	public static Typeface parseSettingsFontSyntax(Resources res, String unparsed,
			FontLoader loader) {
		if (unparsed.startsWith(Common.SETTINGS_PREFIX_FONT_ASSET)) {
			unparsed = unparsed.substring(Common.SETTINGS_PREFIX_FONT_ASSET.length());
			return getFontFromAssets(res, unparsed);
		} else {
			return loader.findFont(unparsed);
		}
	}
	
	public static FontType parsedPref(Resources res, String unparsed, FontLoader loader) {
		String[] string = unparsed.split(Common.SETTINGS_SPLIT_SYMBOL);
		FontType fType = new FontType();
		fType.font = parseSettingsFontSyntax(res, string[Common.SETTINGS_INDEX_FONT], loader);
		fType.weight = Integer.parseInt(string[Common.SETTINGS_INDEX_WEIGHT]);
		return fType;
	}
	
	public static class FontType {
		public Typeface font;
		public int weight;
	}
}
