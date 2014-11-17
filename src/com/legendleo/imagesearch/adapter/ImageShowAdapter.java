package com.legendleo.imagesearch.adapter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.legendleo.imagesearch.R;
import com.legendleo.imagesearch.volley.BitmapCache;
import com.legendleo.imagesearch.volley.MySingleton;
import com.legendleo.imagesearch.volley.ZoomableNetworkImageView.ZoomableNetworkImageView;
import com.legendleo.imagesearch.volley.ZoomableNetworkImageView.ZoomableNetworkImageView.OnImageChangedListener;
import com.legendleo.imagesearch.volley.ZoomableNetworkImageView.ZoomableNetworkImageView.OnImageNotChangedListener;

public class ImageShowAdapter extends PagerAdapter {
	private Context mContext;
	private List<String[]> mList;
	private ImageLoader imageLoader;
	
	private ZoomableNetworkImageView gestureImageView;
	private String downloadUrl;
	private int flag;
	
	public ImageShowAdapter(Context context, int flg, List<String[]> mData){
		mContext = context;
		mList = mData; //ֱ������mList��ֵ
		flag = flg;
		imageLoader = MySingleton.getInstance(context).getImageLoader();
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View imageLayout = inflater.inflate(R.layout.image_show_adapter, container, false);
		assert imageLayout != null;

		if(flag == 0){
			downloadUrl = mList.get(position)[1];
		}else if(flag ==1 ){
			downloadUrl = mList.get(position)[1];
		}
		
		System.out.println("downloadUrl:" + downloadUrl);
		
		gestureImageView = (ZoomableNetworkImageView) imageLayout.findViewById(R.id.gestureImageView);
		final ProgressBar imageShowPBar = (ProgressBar) imageLayout.findViewById(R.id.imageShowPBar);
		OnImageChangedListener listener = new OnImageChangedListener() {
			
			@Override
			public void onImageChanged(ZoomableNetworkImageView zoomableImageView) {
				System.out.println("-------onImageChanged---------");
				imageShowPBar.setVisibility(View.GONE);
				zoomableImageView.removeOnImageChangedListener();
			}

		};
		
		gestureImageView.setOnImageChangedListener(listener);
		
		//���URLΪ������ؿ�ͼƬ
		if(downloadUrl.length() == 0){
			gestureImageView.setDefaultImageResId(R.drawable.empty_photo);
		}
		gestureImageView.setImageUrl(downloadUrl, imageLoader);
		
		OnImageNotChangedListener listener2 = new OnImageNotChangedListener() {
			
			@Override
			public void onImageNotChanged(ZoomableNetworkImageView zoomableImageView) {
				System.out.println("-------onImageNotChanged---------");
				imageShowPBar.setVisibility(View.GONE);
				zoomableImageView.removeOnImageNotChangedListener();
			}
		};
		gestureImageView.setOnImageNotChangedListener(listener2);
		gestureImageView.setErrorImageResId(R.drawable.empty_photo);
		gestureImageView.setTag(position); //��ÿ��ͼƬ����һ��tag
		
		container.addView(imageLayout);
		
		return imageLayout;
	}
	
	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {}
	
	@Override
	public Parcelable saveState() {
		return null;
	}
	
}
