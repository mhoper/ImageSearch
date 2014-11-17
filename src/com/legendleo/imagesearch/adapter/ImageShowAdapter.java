package com.legendleo.imagesearch.adapter;

import java.util.List;

import com.legendleo.imagesearch.fragment.ImageShowFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ImageShowAdapter extends FragmentStatePagerAdapter {
	
	private List<String[]> mList;
	
	public ImageShowAdapter(FragmentManager fm, List<String[]> mData) {
		super(fm);
		mList = mData;
	}

	@Override
	public Fragment getItem(int position) {
		return ImageShowFragment.newInstance(mList.get(position)[1]);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

}
