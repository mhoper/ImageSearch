package com.legendleo.imagesearch.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.legendleo.imagesearch.ImageShowActivity;
import com.legendleo.imagesearch.R;
import com.legendleo.imagesearch.util.Utils;
import com.legendleo.imagesearch.volley.ZoomableNetworkImageView.ZoomableNetworkImageView;
import com.legendleo.imagesearch.volley.ZoomableNetworkImageView.ZoomableNetworkImageView.OnImageChangedListener;
import com.legendleo.imagesearch.volley.ZoomableNetworkImageView.ZoomableNetworkImageView.OnImageNotChangedListener;

public class ImageShowFragment extends Fragment {
	
	private static final String IMAGE_DATA_EXTRA = "image_data_extra";
	
	private ZoomableNetworkImageView gestureImageView;
	private ProgressBar imageShowPBar;
	private String mImageUrl;
	
	public ImageShowFragment(){	}
	
	public static ImageShowFragment newInstance(String imageUrl){
		final ImageShowFragment f = new ImageShowFragment();
		
		final Bundle args = new Bundle();
		args.putString(IMAGE_DATA_EXTRA, imageUrl);
		f.setArguments(args);
		
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.image_show_adapter, container, false);
		gestureImageView = (ZoomableNetworkImageView) v.findViewById(R.id.gestureImageView);
		imageShowPBar = (ProgressBar) v.findViewById(R.id.imageShowPBar);
		
		OnImageChangedListener changedListener = new OnImageChangedListener() {
			
			@Override
			public void onImageChanged(ZoomableNetworkImageView zoomableImageView) {
				System.out.println("-------onImageChanged---------");
				imageShowPBar.setVisibility(View.GONE);
				zoomableImageView.removeOnImageChangedListener();
			}
		};
		gestureImageView.setOnImageChangedListener(changedListener);
		
		OnImageNotChangedListener notChangedListener = new OnImageNotChangedListener() {
			
			@Override
			public void onImageNotChanged(ZoomableNetworkImageView zoomableImageView) {
				System.out.println("-------onImageNotChanged---------");
				imageShowPBar.setVisibility(View.GONE);
				zoomableImageView.removeOnImageNotChangedListener();
			}
		};
		gestureImageView.setOnImageNotChangedListener(notChangedListener);
		
		
		return v;
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(ImageShowActivity.class.isInstance(getActivity())){
			//如果URL为空则加载空图片
			if(mImageUrl.length() == 0){
				gestureImageView.setDefaultImageResId(R.drawable.empty_photo);
			}
			gestureImageView.setErrorImageResId(R.drawable.empty_photo);
			gestureImageView.setImageUrl(mImageUrl, ((ImageShowActivity)getActivity()).getImageLoader());
			gestureImageView.setTag(mImageUrl); //给每张图片设置一个tag
		}
		
		// Pass clicks on the ImageView to the parent activity to handle
		if(OnClickListener.class.isInstance(getActivity()) && Utils.hasHoneycomb()){
			gestureImageView.setOnClickListener((OnClickListener) getActivity());
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(gestureImageView != null){
			gestureImageView.setImageDrawable(null);
		}
	}
}
