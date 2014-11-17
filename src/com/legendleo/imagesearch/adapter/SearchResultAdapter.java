package com.legendleo.imagesearch.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.legendleo.imagesearch.R;
import com.legendleo.imagesearch.volley.BitmapCache;
import com.legendleo.imagesearch.volley.DynamicHeightNetworkImageView;
import com.legendleo.imagesearch.volley.MySingleton;

public class SearchResultAdapter extends BaseAdapter {

	private Context mContext;
	private List<String[]> list;
	private ImageLoader imageLoader;
	private final Random mRandom;
	private int columnWidth;
	/**
	 * 区分分类和搜索
	 */
	private int flag;
	
	public void setFlag(int flg) {
		this.flag = flg;
	}
	
	private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();
	
	public SearchResultAdapter(Context context, int flg, List<String[]> mData){
		mContext = context;
		flag = flg;
		list = mData; //直接引用mList的值
		imageLoader = MySingleton.getInstance(context).getImageLoader();
		mRandom = new Random();
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.search_result_adapter, parent, false);
			holder.mImageView = (DynamicHeightNetworkImageView) convertView.findViewById(R.id.resultNetworkImageView);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		double positionHeight = getPositionRatio(position);
		holder.mImageView.setHeightRatio(positionHeight);
		holder.mImageView.setmCloumnWidth(columnWidth);
		
		holder.mImageView.setDefaultImageResId(R.drawable.empty_photo);
		holder.mImageView.setErrorImageResId(R.drawable.empty_photo);
		
		String url = "";
		if(flag == 0){
			url = list.get(position)[0];
		}else if(flag == 1){
			url = list.get(position)[1];
		}
		
		holder.mImageView.setImageUrl(url, imageLoader);

		return convertView;
	}
	
	private static class ViewHolder{
		DynamicHeightNetworkImageView mImageView;
	}
	
	public void setColumnWidth(int columnWidth) {
		this.columnWidth = columnWidth;
	}
	
	private double getPositionRatio(final int position){
		double ratio = sPositionHeightRatios.get(position, 0.0);
		if(ratio == 0){
			ratio = getRandomHeightRatio();
			sPositionHeightRatios.append(position, ratio);
		}
		return ratio;
	}

	//设置一个随机高度比(0.5-1.5之间)，用于加载default图片前onMeasure使用
	private double getRandomHeightRatio() {
		return (mRandom.nextDouble() /2) + 1.0;
	}
}
