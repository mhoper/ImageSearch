package com.legendleo.imagesearch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.etsy.android.grid.util.DynamicHeightImageView;
import com.legendleo.imagesearch.R;
import com.legendleo.imagesearch.net.URLUtil;

public class ImageMainAdapter extends BaseAdapter {
	
	private Context mContext;
	
	public ImageMainAdapter(Context c){
		mContext = c;
	}

	@Override
	public int getCount() {
		return URLUtil.mThumbIds.length;
	}

	@Override
	public Object getItem(int position) {
		return URLUtil.mThumbIds[position];
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.image_main_adapter, parent, false);
			holder.imageView = (DynamicHeightImageView) convertView.findViewById(R.id.cateImageView);
			holder.textView = (TextView) convertView.findViewById(R.id.cateTextView);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		//设置图片高宽比为1
		holder.imageView.setHeightRatio(1.0);
		holder.imageView.setImageResource(URLUtil.mThumbIds[position]);
		holder.textView.setText(URLUtil.mThumbTitles[position]);
		return convertView;
	}
	
	private final class ViewHolder{
		DynamicHeightImageView imageView;
		TextView textView;
	}
	
}
