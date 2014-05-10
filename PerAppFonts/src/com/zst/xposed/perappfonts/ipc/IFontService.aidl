package com.zst.xposed.perappfonts.ipc;

import com.zst.xposed.perappfonts.ipc.ParcelableTypeface;

interface IFontService { 
    String getFontFolder();
    boolean reloadFonts(); 
    ParcelableTypeface findFonts(String font); 
}