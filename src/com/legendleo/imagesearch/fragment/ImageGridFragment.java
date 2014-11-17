package com.legendleo.imagesearch.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import com.android.volley.RequestQueue;
import com.etsy.android.grid.StaggeredGridView;
import com.legendleo.imagesearch.AboutDialog;
import com.legendleo.imagesearch.ImageShowActivity;
import com.legendleo.imagesearch.R;
import com.legendleo.imagesearch.adapter.ImageGridAdapter;
import com.legendleo.imagesearch.net.NetUtil;
import com.legendleo.imagesearch.net.URLUtil;
import com.legendleo.imagesearch.volley.GetJsonByVolley;
import com.legendleo.imagesearch.volley.MySingleton;

public class ImageGridFragment extends Fragment implements OnScrollListener {

	private StaggeredGridView resultGridView;
	private ImageGridAdapter mAdapter;
	
	private GetJsonByVolley mGetJsonByVolley;
	/**
	 * 存放加载的Json数据
	 */
	private List<String[]> mList;

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
	
	private Handler handler;
	
	public ImageGridFragment(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true); //设置为true,onCreateOptionsMenu才起作用
		
		Bundle args = getArguments();
		flag = args.getInt("flag", 0);
		category = args.getString("category");
		keyword = args.getString("keyword");
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		System.out.println("ImageGridFragment: onCreateView");
		final View v = inflater.inflate(R.layout.activity_image_grid, container, false);
		resultGridView = (StaggeredGridView) v.findViewById(R.id.resultGridView);
		
		resultGridViewPBar = (ProgressBar) v.findViewById(R.id.resultGridViewPBar);
		networkStateText = (TextView) v.findViewById(R.id.networkState);
		resultGridViewPBar.setVisibility(View.VISIBLE);
		//网络连接检查
		if(!NetUtil.CheckNet(getActivity())){
			networkStateText.setVisibility(View.VISIBLE);
			resultGridViewPBar.setVisibility(View.GONE);
		}
		
		loadMoreView = getActivity().getLayoutInflater().inflate(R.layout.footer, resultGridView, false);
		loadMoreProgressBar = (ProgressBar) loadMoreView.findViewById(R.id.loadMoreProgressBar);
		loadMoreText = (TextView) loadMoreView.findViewById(R.id.loadMoreText);
		resultGridView.addFooterView(loadMoreView);
		
		mList = new ArrayList<String[]>();
		mAdapter = new ImageGridAdapter(getActivity(), mList);
		resultGridView.setAdapter(mAdapter);
		resultGridView.setOnScrollListener(this);

		resultGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent intent = new Intent(getActivity(), ImageShowActivity.class);
				intent.putExtra("category", category);
				intent.putExtra("keyword", keyword);
				intent.putExtra("page", page);
				intent.putExtra("totalImagesCount", totalImagesCount);
				intent.putExtra("flag", flag);
				intent.putExtra("currentPosition", position); //当前点击图片的位置
				intent.putExtra("jsonString", (Serializable)mList);
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
		
		//new handler
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 0){
					if(page == 0){
						mList.clear(); //重新搜索时清空
					}
					mList.addAll(mGetJsonByVolley.getmList());
					System.out.println("handleMessage mList.size:" + mList.size());
					
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

		mGetJsonByVolley = new GetJsonByVolley(getActivity(), handler);
		setAdapterData();
		
		return v;
	}
	
	private void setAdapterData(){
		if(flag == 0){
			mGetJsonByVolley.getJsonByVolley(category, page, flag, this);
		}else if(flag ==1){
			mGetJsonByVolley.getJsonByVolley(keyword, page, flag, this);
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
		setAdapterData();
		hasRequestedMore = false;
		System.out.println("SearchResultActivity onLoadMoreItems ------------------>>>>>>>>>>>");
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
				setAdapterData();
				hideSoftInput();
				
				//检查网络连接
				if(NetUtil.CheckNet(getActivity())){
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

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//返回上一级
			NavUtils.navigateUpFromSameTask(getActivity());
			break;
			
		case R.id.about:
			AboutDialog dialog = new AboutDialog(getActivity(), R.style.DialogLayout);
			dialog.show();
			break;
			
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	//取消虚拟键盘：点击搜索按钮时调用有效
	private void hideSoftInput(){
		
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm != null){
			imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0); //有效
		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		System.out.println("ImageGridFragment: onDestroyView");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//取消所有json请求
		RequestQueue rq = MySingleton.getInstance(getActivity()).getRequestQueue();
		rq.cancelAll(this);
		
		//移除所有消息
		handler.removeCallbacksAndMessages(null);
		mList = null;
//		mAdapter = null;
//		resultGridView = null;
//		mGetJsonByVolley = null;
//		loadMoreView = null;
//		resultGridViewPBar = null;
//		loadMoreText = null;
//		networkStateText = null;
	}
}
