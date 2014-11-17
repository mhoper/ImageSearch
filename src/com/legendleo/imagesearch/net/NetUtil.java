package com.legendleo.imagesearch.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtil {

	public static boolean CheckNet(Context context){
		boolean wifiConnected = isWIFIConnected(context);
		boolean mobileConnected = isMobileConnected(context);
		
		if(wifiConnected || mobileConnected){
			return true;
		}
		return false;
	}

	private static boolean isMobileConnected(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(networkInfo != null && networkInfo.isConnected()){
			return true;
		}
		return false;
	}

	private static boolean isWIFIConnected(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if(networkInfo != null && networkInfo.isConnected()){
			return true;
		}
		return false;
	}
}
