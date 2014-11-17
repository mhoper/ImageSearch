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
	private List<JSONObject> list = new ArrayList<JSONObject>();
	private ImageLoader imageLoader;
	private final Random mRandom;
	private int columnWidth;
	/**
	 * ���ַ��������
	 */
	private int flag;
	
	public void setFlag(int flg) {
		this.flag = flg;
	}
	
	private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();
	
	public SearchResultAdapter(Context context, int flg){
		mContext = context;
		flag = flg;
		imageLoader = MySingleton.getInstance(context).getImageLoader();
		mRandom = new Random();
	}
	
	public void setData(List<JSONObject> mData){
		list.clear();
		list.addAll(mData);
	}
	
	public void addAll(List<JSONObject> mData){
		list.addAll(mData);
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
		
		try {
			double positionHeight = getPositionRatio(position);
			holder.mImageView.setHeightRatio(positionHeight);
			holder.mImageView.setmCloumnWidth(columnWidth);
			
			holder.mImageView.setDefaultImageResId(R.drawable.empty_photo);
			holder.mImageView.setErrorImageResId(R.drawable.empty_photo);
			
			String url = "";
			if(flag == 0){
				url = list.get(position).getString("thumbnail_url");
			}else if(flag == 1){
				url = list.get(position).getString("thumbURL");
			}
			
			holder.mImageView.setImageUrl(url, imageLoader);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	//����һ������߶ȱ�(0.5-1.5֮��)�����ڼ���defaultͼƬǰonMeasureʹ��
	private double getRandomHeightRatio() {
		return (mRandom.nextDouble() /2) + 1.0;
	}
}
