package com.code4ever.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Hashtable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapCache {
	static private BitmapCache mCache;

	private Hashtable<String, Entry> mBitmapEnties;
	private ReferenceQueue<Bitmap> mGCedBitmapQueue;

	private static class Entry extends SoftReference<Bitmap> {
		private String key;

		public Entry(Bitmap bmp, ReferenceQueue<Bitmap> queue, String key) {
			super(bmp, queue);
			this.key = key;
		}
	}

	private BitmapCache() {
		mBitmapEnties = new Hashtable<String, Entry>();
		mGCedBitmapQueue = new ReferenceQueue<Bitmap>();

	}

	public static BitmapCache getInstance() {
		if (mCache == null) {
			mCache = new BitmapCache();
		}
		return mCache;

	}

	private void addBitmap(String key, Bitmap bmp) {
		cleanCache();  // remove bad entries in the ReferenceQueue
		Entry ref = new Entry(bmp, mGCedBitmapQueue, key);
		mBitmapEnties.put(key, ref);
	}


	public Bitmap getBitmap(String filename) {

		Bitmap bitmapImage = null;

		if (mBitmapEnties.containsKey(filename)) {
			Entry ref = (Entry) mBitmapEnties.get(filename);
			bitmapImage = (Bitmap) ref.get();
		}
		if (bitmapImage == null) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inTempStorage = new byte[16 * 1024];

			bitmapImage = BitmapFactory.decodeFile(filename, options);
			if (bitmapImage != null) {
				this.addBitmap(filename, bitmapImage);	
			}
		}

		return bitmapImage;
	}

	private void cleanCache() {
		Entry ref = null;
		while ((ref = (Entry) mGCedBitmapQueue.poll()) != null) {
			mBitmapEnties.remove(ref.key);
		}
	}

	public void clearCache() {
		cleanCache();
		mBitmapEnties.clear();
		System.gc();
		System.runFinalization();
	}
}
