package com.zst.xposed.perappfonts.ipc;

import de.robv.android.xposed.XposedHelpers;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableTypeface implements Parcelable {
	//http://www.app-solut.com/blog/2011/05/using-self-defined-parcelable-objects-during-an-android-aidl-rpc-ipc-call/
	public final Typeface typeface;
	
	public ParcelableTypeface(Typeface typeface) {
		this.typeface = typeface;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel outParcel, int flags) {
		int native_instance = XposedHelpers.getIntField(typeface, "native_instance");
		outParcel.writeInt(native_instance);
	}
	
	public static final Parcelable.Creator<ParcelableTypeface> CREATOR = new Parcelable.Creator<ParcelableTypeface>() {
		
		@Override
		public ParcelableTypeface createFromParcel(Parcel in) {
			final int native_instance = in.readInt();
			try {
				Typeface typeface = (Typeface) XposedHelpers.findConstructorBestMatch(Typeface.class,
						Integer.class).newInstance(native_instance);
				return new ParcelableTypeface(typeface);
				
			} catch (Exception e) {
				return null;
			}
		}
		
		@Override
		public ParcelableTypeface[] newArray(int size) {
			return new ParcelableTypeface[size];
		}
	};
}
