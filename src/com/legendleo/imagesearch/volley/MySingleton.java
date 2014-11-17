package com.legendleo.imagesearch.volley;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class MySingleton {
	private static MySingleton mInstance;
	private static RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private static Context mContext;
	
	private MySingleton(Context context){
		mContext = context;
		mRequestQueue = getRequestQueue();
		mImageLoader = new ImageLoader(mRequestQueue, new BitmapCache());
	}
	
	public static synchronized MySingleton getInstance(Context context){
		if(mInstance == null){
			mInstance = new MySingleton(context);
		}
		return mInstance;
	}
	
	public RequestQueue getRequestQueue(){
		if(mRequestQueue == null){
			//getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
			mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
		}
		return mRequestQueue;
	}
	
	public ImageLoader getImageLoader(){
		return mImageLoader;
	}
}
