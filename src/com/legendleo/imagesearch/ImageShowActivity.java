package com.legendleo.imagesearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Toast;

import com.legendleo.imagesearch.adapter.ImageShowAdapter;
import com.legendleo.imagesearch.net.URLUtil;
import com.legendleo.imagesearch.util.FileUtil;
import com.legendleo.imagesearch.volley.GetJsonByVolley;
import com.legendleo.imagesearch.volley.ZoomableNetworkImageView.ZoomableNetworkImageView;

public class ImageShowActivity extends Activity {
	private ViewPager imageViewPager;
	private ImageShowAdapter mAdapter;
	/**
	 * 本次加载的Json数据
	 */
	private static List<JSONObject> mList;
	/**
	 * 所有次加载的Json数据
	 */
	private List<JSONObject> mAllList;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_image_show);
		
		Intent intent = getIntent();
		flag = intent.getIntExtra("flag", 0);
		keyword = intent.getStringExtra("keyword");
		category = intent.getStringExtra("category");
		page = intent.getIntExtra("page", 0);
		totalImagesCount = intent.getIntExtra("totalImagesCount", URLUtil.RN);
		currentPosition = intent.getIntExtra("currentPosition", 0);
		
		mAllList = new ArrayList<JSONObject>();
		mAllList.addAll(mList); //首次数据add
		
		System.out.println("ImageShowActivity position:" + currentPosition);
		mAdapter = new ImageShowAdapter(this, flag);
		imageViewPager = (ViewPager) findViewById(R.id.imageViewPager);
		imageViewPager.setAdapter(mAdapter);
		
		mAdapter.addAll(mList);
		mAdapter.notifyDataSetChanged();
		//加载数据后,再设置当前页
		imageViewPager.setCurrentItem(currentPosition);
		
		imageViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				System.out.println("ImageShowActivity onPageSelected position------------------>>>>>>>>>>>:" + position);
				//首次进入并不会执行此方法
				// 为了体验效果更好一些，设置为提前加载，当滑动到倒数第二页时就开始加载下一次Json数据
				if(position == mAllList.size() - 2 && position == totalImagesCount - 2){
					System.out.println("到达倒数第2页，将加载更多");
					//最后一页向左滑后，则获取下一次Json数据
					mGetJsonByVolley = new GetJsonByVolley(ImageShowActivity.this, handler);
					page++; //下一页rn
					totalImagesCount += URLUtil.RN; //防止由于网络延迟mAllList的size还未增加时而多次调用GetJsonByVolley，故此处先加上
					
					if(flag == 0){
						mGetJsonByVolley.getJsonByVolley(category, page, flag);
					}else if(flag == 1){
						mGetJsonByVolley.getJsonByVolley(keyword, page, flag);
					}
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
					if(imageViewPager.getCurrentItem() == mAllList.size() - 1){
						System.out.println("ImageShowActivity onPageScrollStateChanged totalImagesCount------------------>>>>>>>>>>>:" + totalImagesCount);
						//首次进入时直接到最后一页
						if(currentPosition == mAllList.size() - 1 && imageViewPager.getCurrentItem() == totalImagesCount - 1){
							System.out.println("到达最后一页，将加载更多");
							mGetJsonByVolley = new GetJsonByVolley(ImageShowActivity.this, handler);
							page++; //下一页rn
							totalImagesCount += URLUtil.RN; //防止由于网络延迟mAllList的size还未增加时而多次调用GetJsonByVolley，故此处先加上

							if(flag == 0){
								mGetJsonByVolley.getJsonByVolley(category, page, flag);
							}else if(flag == 1){
								mGetJsonByVolley.getJsonByVolley(keyword, page, flag);
							}
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
		
//		final ZoomableNetworkImageView gestureImageView1 = (ZoomableNetworkImageView) imageViewPager.findViewWithTag(imageViewPager.getCurrentItem());
//		gestureImageView1.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				System.out.println("gestureImageView onTouch:" + gestureImageView1.getZoomLevel());
//				if(gestureImageView1.getZoomLevel() > 1.0){
//					System.out.println("图片已经被放大");
//					//如果图片被放大，则禁止父组件消费
//					gestureImageView1.getParent().requestDisallowInterceptTouchEvent(true);
//				}
//				return false;
//			}
//		});
	}
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0){
				System.out.println("收到来自handler的更新消息");
				setmList(mGetJsonByVolley.getmList());
				mAllList.addAll(mList);
				
				mAdapter.addAll(mList);
				mAdapter.notifyDataSetChanged();
			}
		};
	};
	
	public static void setmList(List<JSONObject> mData) {
		mList = mData;
	}
	
	
	//返回按钮
	public void onBack(View view) {
		finish();
		//移除所有消息
		handler.removeCallbacksAndMessages(null);
	}
	
	//下载按钮
	public void onDownloadIamge(View view){
		//通过tag获取当前页图片
		ZoomableNetworkImageView gestureImageView = (ZoomableNetworkImageView) imageViewPager.findViewWithTag(imageViewPager.getCurrentItem());
		gestureImageView.setDrawingCacheEnabled(true);
		try {
			String downloadUrl = "";
			if(flag == 0){
				downloadUrl = mList.get(imageViewPager.getCurrentItem()).getString("obj_url");
			}else if(flag == 1){
				downloadUrl = mList.get(imageViewPager.getCurrentItem()).getString("objURL");
			}

			if(FileUtil.writeSDcard(downloadUrl, gestureImageView.getDrawingCache())){
				Toast.makeText(this, "download successful", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, "download failed", Toast.LENGTH_SHORT).show();
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		gestureImageView.setDrawingCacheEnabled(false);
	}
	
	public void onSetWallpaper(View view){
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
		
		//通过tag获取当前页图片
		ZoomableNetworkImageView gestureImageView = (ZoomableNetworkImageView) imageViewPager.findViewWithTag(imageViewPager.getCurrentItem());
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
		String downloadUrl = "";
		try {
			if(flag == 0){
				downloadUrl = mList.get(imageViewPager.getCurrentItem()).getString("obj_url");
			}else if(flag == 1){
				downloadUrl = mList.get(imageViewPager.getCurrentItem()).getString("objURL");
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		//移除所有消息
		handler.removeCallbacksAndMessages(null);
		
//		mList = null;
//		mAllList = null;
//		mAdapter = null;
//		imageViewPager = null;
//		mGetJsonByVolley = null;
		
		super.onDestroy();
		System.out.println("ImageShowActivity onDestroy");
	}
}
