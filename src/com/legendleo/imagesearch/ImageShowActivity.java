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
	 * ���μ��ص�Json����
	 */
	private static List<JSONObject> mList;
	/**
	 * ���дμ��ص�Json����
	 */
	private List<JSONObject> mAllList;
	/**
	 * �ж��Ƿ�Ϊ���һҳ
	 */
	private boolean isFirstOrLast = true;
	
	private GetJsonByVolley mGetJsonByVolley;
	private String keyword = "";
	private String category = "";
	private int page;

	//����ͼƬ�ĸ���
	private int totalImagesCount;
	private int flag;
	//�״ν���ʱ��ǰͼƬ��λ��
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
		mAllList.addAll(mList); //�״�����add
		
		System.out.println("ImageShowActivity position:" + currentPosition);
		mAdapter = new ImageShowAdapter(this, flag);
		imageViewPager = (ViewPager) findViewById(R.id.imageViewPager);
		imageViewPager.setAdapter(mAdapter);
		
		mAdapter.addAll(mList);
		mAdapter.notifyDataSetChanged();
		//�������ݺ�,�����õ�ǰҳ
		imageViewPager.setCurrentItem(currentPosition);
		
		imageViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				System.out.println("ImageShowActivity onPageSelected position------------------>>>>>>>>>>>:" + position);
				//�״ν��벢����ִ�д˷���
				// Ϊ������Ч������һЩ������Ϊ��ǰ���أ��������������ڶ�ҳʱ�Ϳ�ʼ������һ��Json����
				if(position == mAllList.size() - 2 && position == totalImagesCount - 2){
					System.out.println("���ﵹ����2ҳ�������ظ���");
					//���һҳ���󻬺����ȡ��һ��Json����
					mGetJsonByVolley = new GetJsonByVolley(ImageShowActivity.this, handler);
					page++; //��һҳrn
					totalImagesCount += URLUtil.RN; //��ֹ���������ӳ�mAllList��size��δ����ʱ����ε���GetJsonByVolley���ʴ˴��ȼ���
					
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
				//������ֵ��0��END��,1(PRESS) , 2(UP)
				//������ָ������ҳʱ����ָ����ȥ��ʱ��ᴥ�����������stateֵΪ1
				//��ָ̧��ʱ����������˻�������ʹ��С�������ֵ���Ϊ2��Ȼ������Ϊ0 ���ܹ�ִ������������Ρ�
				//һ�������������ָ����ȥ�Ժ�һ�㻬��Ҳû�з��������ʱ��ֻ���������������Σ�stateֵ�ֱ���1,0 
				//��һҳ���һ������һҳ���󻬱��������������
				if(state == 2){
					isFirstOrLast = false;
				}else if(state == 0 && isFirstOrLast){
					//�������һҳ
					if(imageViewPager.getCurrentItem() == mAllList.size() - 1){
						System.out.println("ImageShowActivity onPageScrollStateChanged totalImagesCount------------------>>>>>>>>>>>:" + totalImagesCount);
						//�״ν���ʱֱ�ӵ����һҳ
						if(currentPosition == mAllList.size() - 1 && imageViewPager.getCurrentItem() == totalImagesCount - 1){
							System.out.println("�������һҳ�������ظ���");
							mGetJsonByVolley = new GetJsonByVolley(ImageShowActivity.this, handler);
							page++; //��һҳrn
							totalImagesCount += URLUtil.RN; //��ֹ���������ӳ�mAllList��size��δ����ʱ����ε���GetJsonByVolley���ʴ˴��ȼ���

							if(flag == 0){
								mGetJsonByVolley.getJsonByVolley(category, page, flag);
							}else if(flag == 1){
								mGetJsonByVolley.getJsonByVolley(keyword, page, flag);
							}
						}
						
						Toast.makeText(ImageShowActivity.this, "is loading...", Toast.LENGTH_SHORT).show(); //�����������ʾ
					}
//					//�ж��Ƿ�Ϊ���һҳ�������󻬶����ǵĻ��ͼ�����һ��Json���ݣ����ַ�ʽ���鲻̫�ã�Ӧ��ǰ����
//					if(imageViewPager.getCurrentItem() == mAllList.size() - 1 && imageViewPager.getCurrentItem() == totalImagesCount - 1){
//						System.out.println("�������һҳ�������ظ���");
//						//���һҳ���󻬺����ȡ��һ��Json����
//						mGetJsonByVolley = new GetJsonByVolley(ImageShowActivity.this, handler);
//						page++; //��һҳrn
//						totalImagesCount += URLUtil.RN; //��ֹ���������ӳ�mAllList��size��δ����ʱ����ε���GetJsonByVolley���ʴ˴��ȼ���
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
//					System.out.println("ͼƬ�Ѿ����Ŵ�");
//					//���ͼƬ���Ŵ����ֹ���������
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
				System.out.println("�յ�����handler�ĸ�����Ϣ");
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
	
	
	//���ذ�ť
	public void onBack(View view) {
		finish();
		//�Ƴ�������Ϣ
		handler.removeCallbacksAndMessages(null);
	}
	
	//���ذ�ť
	public void onDownloadIamge(View view){
		//ͨ��tag��ȡ��ǰҳͼƬ
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
		
		//ͨ��tag��ȡ��ǰҳͼƬ
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
		//�����ȡ����activities��������1���ʹ���createChooser
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
		//�Ƴ�������Ϣ
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
