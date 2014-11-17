package com.legendleo.imagesearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.legendleo.imagesearch.fragment.ImageGridFragment;

public class ImageGridActivity extends FragmentActivity {
	
	private static final String TAG = "ImageGridActivity";

	/**
	 * 区分分类和搜索
	 */
	private int flag;
	private String category = "";
	private String keyword = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//设置返回键
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		flag = intent.getIntExtra("flag", 0);
		category = intent.getStringExtra("category");
		keyword = intent.getStringExtra("keyword");

		//设置标题
		if(flag == 0){
			setTitle(category);
		}else if(flag == 1){
			setTitle(keyword);
		}
		
		if(getSupportFragmentManager().findFragmentByTag(TAG) == null){
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			final ImageGridFragment f = new ImageGridFragment();
			Bundle args = new Bundle();
			args.putInt("flag", flag);
			args.putString("category", category);
			args.putString("keyword", keyword);
			f.setArguments(args);
			
			ft.add(android.R.id.content, f, TAG);
			ft.commit();
		}
	}
	
}
