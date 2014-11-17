package com.legendleo.imagesearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.legendleo.imagesearch.adapter.ImageShowAdapter;
import com.legendleo.imagesearch.adapter.ImageShowAdapter;
import com.legendleo.imagesearch.net.URLUtil;
import com.legendleo.imagesearch.util.FileUtil;
import com.legendleo.imagesearch.volley.GetJsonByVolley;
import com.legendleo.imagesearch.volley.MySingleton;
import com.legendleo.imagesearch.volley.ZoomableNetworkImageView.ZoomableNetworkImageView;

import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class ImageShowActivity extends FragmentActivity implements OnClickListener {
	
	private ViewPager imageViewPager;
	private ImageShowAdapter mAdapter;
	
	private ImageLoader imageLoader;
	/**
	 * 存放加载的Json数据
	 */
	private List<String[]> mList;
	/**
	 * 判断是否为最后一页
	 */
	private boolean isFirstOrLast = true;
	
	private GetJsonByVolley mGetJsonByVolley;
	private String keyword = "";
	private String category = "";
	private int page;

	//所有图片的个数
	private int totalImagesCount;
	private int flag;
	//首次进入时当前图片的位置
	private int currentPosition;
	
	private Handler handler;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		
		setContentView(R.layout.activity_image_show);
		
		Intent intent = getIntent();
		flag = intent.getIntExtra("flag", 0);
		keyword = intent.getStringExtra("keyword");
		category = intent.getStringExtra("category");
		page = intent.getIntExtra("page", 0);
		totalImagesCount = intent.getIntExtra("totalImagesCount", URLUtil.RN);
		currentPosition = intent.getIntExtra("currentPosition", 0);
		
		imageLoader = MySingleton.getInstance(this).getImageLoader();
		
		mList = new ArrayList<String[]>();
		mList.addAll((List<String[]>) intent.getSerializableExtra("jsonString"));
		
		System.out.println("ImageShowActivity position:" + currentPosition);
		mAdapter = new ImageShowAdapter(getSupportFragmentManager(), mList);
		imageViewPager = (ViewPager) findViewById(R.id.imageViewPager);
		imageViewPager.setAdapter(mAdapter);
		
		//mAdapter.notifyDataSetChanged();
		//加载数据后,再设置当前页
		imageViewPager.setCurrentItem(currentPosition);
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 0){
					System.out.println("收到来自handler的更新消息");
					mList.addAll(mGetJsonByVolley.getmList());
					
					mAdapter.notifyDataSetChanged();
				}
			};
		};
		
		imageViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				System.out.println("ImageShowActivity onPageSelected position------------------>>>>>>>>>>>:" + position);
				//首次进入并不会执行此方法
				// 为了体验效果更好一些，设置为提前加载，当滑动到倒数第二页时就开始加载下一次Json数据
				if(position == mList.size() - 2 && position == totalImagesCount - 2){
					System.out.println("到达倒数第2页，将加载更多");
					//最后一页向左滑后，则获取下一次Json数据
					loadMoreItems();
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				//有三个值：0（END）,1(PRESS) , 2(UP)
				//当用手指滑动翻页时，手指按下去的时候会触发这个方法，state值为1
				//手指抬起时，如果发生了滑动（即使很小），这个值会变为2，然后最后变为0 。总共执行这个方法三次。
				//一种特殊情况是手指按下去以后一点滑动也没有发生，这个时候只会调用这个方法两次，state值分别是1,0 
				//第一页向右滑或最后一页向左滑便是这种特殊情况
				if(state == 2){
					isFirstOrLast = false;
				}else if(state == 0 && isFirstOrLast){
					//到达最后一页
					if(imageViewPager.getCurrentItem() == mList.size() - 1){
						System.out.println("ImageShowActivity onPageScrollStateChanged totalImagesCount------------------>>>>>>>>>>>:" + totalImagesCount);
						//首次进入时直接到最后一页
						if(currentPosition == mList.size() - 1 && imageViewPager.getCurrentItem() == totalImagesCount - 1){
							System.out.println("到达最后一页，将加载更多");
							loadMoreItems();
						}
						
						Toast.makeText(ImageShowActivity.this, "is loading...", Toast.LENGTH_SHORT).show(); //给予加载中提示
					}
//					//判断是否为最后一页并且向左滑动，是的话就加载下一次Json数据，这种方式体验不太好，应提前加载
//					if(imageViewPager.getCurrentItem() == mAllList.size() - 1 && imageViewPager.getCurrentItem() == totalImagesCount - 1){
//						System.out.println("到达最后一页，将加载更多");
//						//最后一页向左滑后，则获取下一次Json数据
//						mGetJsonByVolley = new GetJsonByVolley(ImageShowActivity.this, handler);
//						page++; //下一页rn
//						totalImagesCount += URLUtil.RN; //防止由于网络延迟mAllList的size还未增加时而多次调用GetJsonByVolley，故此处先加上
//						mGetJsonByVolley.getJsonByVolley(keyword, page);
//					}
				}else{
					isFirstOrLast = true;
				}
				
			}
		});
		
	}
	
	private void loadMoreItems(){
		mGetJsonByVolley = new GetJsonByVolley(ImageShowActivity.this, handler);
		page++; //下一页rn
		totalImagesCount += URLUtil.RN; //防止由于网络延迟mAllList的size还未增加时而多次调用GetJsonByVolley，故此处先加上

		if(flag == 0){
			mGetJsonByVolley.getJsonByVolley(category, page, flag, this);
		}else if(flag == 1){
			mGetJsonByVolley.getJsonByVolley(keyword, page, flag, this);
		}
	}
	

    /**
     * Called by the ViewPager child fragments to load images via the one ImageLoader
     */
	public ImageLoader getImageLoader(){
		return imageLoader;
	}
	
	//返回按钮
	public void onBack(View view) {
		finish();
	}
	
	//下载按钮
	public void onDownloadIamge(View view){
		String downloadUrl = mList.get(imageViewPager.getCurrentItem())[1];
		
		//通过tag获取当前页图片
		ZoomableNetworkImageView gestureImageView = (ZoomableNetworkImageView) imageViewPager.findViewWithTag(downloadUrl);
		gestureImageView.setDrawingCacheEnabled(true);
		

		if(FileUtil.writeSDcard(downloadUrl, gestureImageView.getDrawingCache())){
			Toast.makeText(this, "download successful", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this, "download failed", Toast.LENGTH_SHORT).show();
		}
			
		gestureImageView.setDrawingCacheEnabled(false);
	}
	
	public void onSetWallpaper(View view){
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
		String downloadUrl = mList.get(imageViewPager.getCurrentItem())[1];
		
		//通过tag获取当前页图片
		ZoomableNetworkImageView gestureImageView = (ZoomableNetworkImageView) imageViewPager.findViewWithTag(downloadUrl);
		gestureImageView.setDrawingCacheEnabled(true);
		try {
			wallpaperManager.setBitmap(gestureImageView.getDrawingCache());
		} catch (IOException e) {
			e.printStackTrace();
		}
		gestureImageView.setDrawingCacheEnabled(false);
		Toast.makeText(this, "set wallpaper successfully", Toast.LENGTH_SHORT).show();
	}
	
	public void onShare(View view){
		String downloadUrl = mList.get(imageViewPager.getCurrentItem())[1];

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, "share");
		intent.putExtra(Intent.EXTRA_TEXT, downloadUrl);
		
		PackageManager pkManager = getPackageManager();
		List<ResolveInfo> activities = pkManager.queryIntentActivities(intent, 0);
		//如果获取到的activities个数大于1个就创建createChooser
		if(activities.size() > 1){
			Intent chooserIntent = Intent.createChooser(intent, getTitle());
			startActivity(chooserIntent);
			
		}else{
			System.out.println("Can't find share component to share");
			startActivity(intent);
		}
		
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		//取消所有json请求
		RequestQueue rq = MySingleton.getInstance(this).getRequestQueue();
		rq.cancelAll(this);
		
		//移除所有消息
		handler.removeCallbacksAndMessages(null);
		
		mList = null;
//		mAdapter = null;
//		imageViewPager = null;
//		mGetJsonByVolley = null;
		System.out.println("ImageShowActivity onDestroy");
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	@Override
	public void onClick(View v) {
		System.out.println("*************************ImageShowActivity onClick");
		//single tap时显示或隐藏功能按钮区
		View imageShowButtons = findViewById(R.id.imageShowButtons);
		int viewState = imageShowButtons.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
		imageShowButtons.setVisibility(viewState);
		
		//single tap时全屏或非全屏显示
		if(viewState == View.VISIBLE){
			showStatusBar();
		}else if(viewState == View.GONE){
			hideStatusBar();
		}
//		WindowManager.LayoutParams attrs = getWindow().getAttributes();
//		int flagState = attrs.flags == WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN ? WindowManager.LayoutParams.FLAG_FULLSCREEN : WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
//		getWindow().setFlags(flagState, flagState);
	}
	
	//隐藏状态栏
	private void hideStatusBar() {
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setAttributes(attrs);
	}

	//显示状态栏
	private void showStatusBar() {
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setAttributes(attrs);
	}
}
