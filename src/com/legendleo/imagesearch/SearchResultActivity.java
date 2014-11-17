package com.legendleo.imagesearch;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.etsy.android.grid.StaggeredGridView;
import com.legendleo.imagesearch.adapter.SearchResultAdapter;
import com.legendleo.imagesearch.net.NetUtil;
import com.legendleo.imagesearch.net.URLUtil;
import com.legendleo.imagesearch.volley.GetJsonByVolley;

public class SearchResultActivity extends Activity implements OnScrollListener {

	private StaggeredGridView resultGridView;
	private SearchResultAdapter mAdapter;
	
	private GetJsonByVolley mGetJsonByVolley;
	/**
	 * 本次加载的Json数据
	 */
	private List<JSONObject> mList;
	
	/**
	 * 所有次加载的数据，用于ItemClick
	 */
	private List<JSONObject> mAllList;
	/**
	 * 区分分类和搜索
	 */
	private int flag;
	private String category = "";
	private String keyword = "";
	
	private boolean hasRequestedMore;
	/**
	 * current page
	 */
	private int page = 0;
	//最后可见条目索引
	private int lastVisibleIndex;
	//所有图片的个数
	private int totalImagesCount = URLUtil.RN;
	//加载更多
	private View loadMoreView;
	private ProgressBar loadMoreProgressBar, resultGridViewPBar;
	private TextView loadMoreText;
	private TextView networkStateText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_result);

		resultGridViewPBar = (ProgressBar) findViewById(R.id.resultGridViewPBar);
		networkStateText = (TextView) findViewById(R.id.networkState);
		resultGridViewPBar.setVisibility(View.VISIBLE);
		//网络连接检查
		if(!NetUtil.CheckNet(this)){
			networkStateText.setVisibility(View.VISIBLE);
			resultGridViewPBar.setVisibility(View.GONE);
		}
		
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
		
		resultGridView = (StaggeredGridView) findViewById(R.id.resultGridView);

		loadMoreView = getLayoutInflater().inflate(R.layout.footer, resultGridView, false);
		loadMoreProgressBar = (ProgressBar) loadMoreView.findViewById(R.id.loadMoreProgressBar);
		loadMoreText = (TextView) loadMoreView.findViewById(R.id.loadMoreText);
		resultGridView.addFooterView(loadMoreView);
		
		mAdapter = new SearchResultAdapter(this, flag);
		resultGridView.setAdapter(mAdapter);
		resultGridView.setOnScrollListener(this);

		mAllList = new ArrayList<JSONObject>();
		
		resultGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ImageShowActivity.setmList(mAllList);
				
				Intent intent = new Intent(SearchResultActivity.this, ImageShowActivity.class);
				intent.putExtra("category", category);
				intent.putExtra("keyword", keyword);
				intent.putExtra("page", page);
				intent.putExtra("totalImagesCount", totalImagesCount);
				intent.putExtra("flag", flag);
				intent.putExtra("currentPosition", position); //当前点击图片的位置
				startActivity(intent);
			}
		});
		
		resultGridView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				//取得gridview列宽并设置
				mAdapter.setColumnWidth(resultGridView.getColumnWidth());
				resultGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
		
		mGetJsonByVolley = new GetJsonByVolley(this, handler);
		setAdapterData(keyword);
		
	}

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0){
				mList = mGetJsonByVolley.getmList();
				System.out.println("handleMessage mList.size:" + mList.size());
				
				if(page == 0){
					mAdapter.setData(mList);
					System.out.println("handleMessage mAdapter.setData");
					mAllList.clear();
					mAllList.addAll(mList);
				}else{
					//滑动取更多
					mAdapter.addAll(mList);
					mAllList.addAll(mList);
				}
				
				//重新搜索后，GridView滚动回顶部位置
				if(resultGridViewPBar.getVisibility() == View.VISIBLE){
					resultGridView.setSelection(0); //其它smoothScrollTo...都不起作用...why
				}
				//隐藏进度条
				resultGridViewPBar.setVisibility(View.GONE);
				loadMoreProgressBar.setVisibility(View.GONE);
				loadMoreText.setVisibility(View.GONE);
				
				mAdapter.notifyDataSetChanged();
			}
		};
	};
	
	private void setAdapterData(String keyword){
		if(flag == 0){
			mGetJsonByVolley.getJsonByVolley(category, page, flag);
		}else if(flag ==1){
			mGetJsonByVolley.getJsonByVolley(keyword, page, flag);
		}
	}
	
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//由于JsonUTF8Request有延迟，故totalItemCount和mAdapter.getCount()数也会不准，当手指滑动过快时lastVisibleIndex==totalItemCount会不准确
		//会不停的调用getJsonByVolley；故此处不予采用，而用用totalImagesCount作判断
		System.out.println("totalImagesCount:" + totalImagesCount);
		if(scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastVisibleIndex == totalImagesCount){
			//设置加载更多View可见（如果只设置loadMoreView属性为gone而没有设置progressBar和text的话，页面空间还是会被占据）
			loadMoreProgressBar.setVisibility(View.VISIBLE);
			loadMoreText.setVisibility(View.VISIBLE);
			
			hasRequestedMore = true;
			onLoadMoreItems();
		}
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		System.out.println("onScroll firstVisibleItem:" + firstVisibleItem +
                " visibleItemCount:" + visibleItemCount +
                " totalItemCount:" + totalItemCount);
		if(!hasRequestedMore){
			lastVisibleIndex = firstVisibleItem + visibleItemCount - 1; //减去footer
		}
		
	}
	
	private void onLoadMoreItems(){
		page++;
		totalImagesCount += URLUtil.RN;
		setAdapterData(keyword);
		hasRequestedMore = false;
		System.out.println("SearchResultActivity onLoadMoreItems ------------------>>>>>>>>>>>");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		MenuItem searchItem = menu.findItem(R.id.headerSearchView);
		SearchView searchView = (SearchView) searchItem.getActionView();
		
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				System.out.println("SearchResultActivity onQueryTextSubmit:" + query); //模拟器中使用回车提交为什么会提交两次

				flag = 1; //搜索flag
				page = 0; //搜索时值清0
				totalImagesCount = URLUtil.RN;
				keyword = query; //把query赋值给keyword，加载更多时会用到
				mAdapter.setFlag(flag); //更改flag值为搜索
				setAdapterData(keyword);
				hideSoftInput();
				
				//检查网络连接
				if(NetUtil.CheckNet(SearchResultActivity.this)){
					networkStateText.setVisibility(View.GONE);
					//设置进度条可见
					resultGridViewPBar.setVisibility(View.VISIBLE);
				}else{
					networkStateText.setVisibility(View.VISIBLE);
					resultGridViewPBar.setVisibility(View.GONE);
				}
				
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//返回上一级
			NavUtils.navigateUpFromSameTask(this);
			break;

		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	//取消虚拟键盘：点击搜索按钮时调用有效
	private void hideSoftInput(){
		
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm != null){
//			imm.hideSoftInputFromInputMethod(getWindow().getDecorView().getWindowToken(), 0); //不起作用
			imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0); //有效，与下文语句作用一样
//			imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
		}
	}

	@Override
	protected void onDestroy() {
		//移除所有消息
		handler.removeCallbacksAndMessages(null);
//		mList = null;
//		mAllList = null;
//		mAdapter = null;
//		resultGridView = null;
//		mGetJsonByVolley = null;
//		loadMoreView = null;
//		resultGridViewPBar = null;
//		loadMoreText = null;
//		networkStateText = null;
		
		super.onDestroy();
	}

}
