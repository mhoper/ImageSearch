package com.legendleo.imagesearch.volley;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class BitmapCache implements ImageCache {

	private LruCache<String, Bitmap> mCache;
	
	public BitmapCache(){
		//获取应用程序最大可用内存
		int maxSize = (int) Runtime.getRuntime().maxMemory();
		System.out.println("memory maxSize:" + maxSize / 1024 / 1024 + "M");
		int cacheSize = maxSize / 8;
		mCache = new LruCache<String, Bitmap>(cacheSize){
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount();
			}
		};
	}
	
	@Override
	public Bitmap getBitmap(String url) {
		
		return mCache.get(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		mCache.put(url, bitmap);
	}

}
