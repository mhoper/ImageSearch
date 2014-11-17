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
	 * ���μ��ص�Json����
	 */
	private List<JSONObject> mList;
	
	/**
	 * ���дμ��ص����ݣ�����ItemClick
	 */
	private List<JSONObject> mAllList;
	/**
	 * ���ַ��������
	 */
	private int flag;
	private String category = "";
	private String keyword = "";
	
	private boolean hasRequestedMore;
	/**
	 * current page
	 */
	private int page = 0;
	//���ɼ���Ŀ����
	private int lastVisibleIndex;
	//����ͼƬ�ĸ���
	private int totalImagesCount = URLUtil.RN;
	//���ظ���
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
		//�������Ӽ��
		if(!NetUtil.CheckNet(this)){
			networkStateText.setVisibility(View.VISIBLE);
			resultGridViewPBar.setVisibility(View.GONE);
		}
		
		//���÷��ؼ�
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		flag = intent.getIntExtra("flag", 0);
		category = intent.getStringExtra("category");
		keyword = intent.getStringExtra("keyword");
		
		//���ñ���
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
				intent.putExtra("currentPosition", position); //��ǰ���ͼƬ��λ��
				startActivity(intent);
			}
		});
		
		resultGridView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				//ȡ��gridview�п�����
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
					//����ȡ����
					mAdapter.addAll(mList);
					mAllList.addAll(mList);
				}
				
				//����������GridView�����ض���λ��
				if(resultGridViewPBar.getVisibility() == View.VISIBLE){
					resultGridView.setSelection(0); //����smoothScrollTo...����������...why
				}
				//���ؽ�����
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
		//����JsonUTF8Request���ӳ٣���totalItemCount��mAdapter.getCount()��Ҳ�᲻׼������ָ��������ʱlastVisibleIndex==totalItemCount�᲻׼ȷ
		//�᲻ͣ�ĵ���getJsonByVolley���ʴ˴�������ã�������totalImagesCount���ж�
		System.out.println("totalImagesCount:" + totalImagesCount);
		if(scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastVisibleIndex == totalImagesCount){
			//���ü��ظ���View�ɼ������ֻ����loadMoreView����Ϊgone��û������progressBar��text�Ļ���ҳ��ռ仹�ǻᱻռ�ݣ�
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
			lastVisibleIndex = firstVisibleItem + visibleItemCount - 1; //��ȥfooter
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
				System.out.println("SearchResultActivity onQueryTextSubmit:" + query); //ģ������ʹ�ûس��ύΪʲô���ύ����

				flag = 1; //����flag
				page = 0; //����ʱֵ��0
				totalImagesCount = URLUtil.RN;
				keyword = query; //��query��ֵ��keyword�����ظ���ʱ���õ�
				mAdapter.setFlag(flag); //����flagֵΪ����
				setAdapterData(keyword);
				hideSoftInput();
				
				//�����������
				if(NetUtil.CheckNet(SearchResultActivity.this)){
					networkStateText.setVisibility(View.GONE);
					//���ý������ɼ�
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
			//������һ��
			NavUtils.navigateUpFromSameTask(this);
			break;

		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	//ȡ��������̣����������ťʱ������Ч
	private void hideSoftInput(){
		
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm != null){
//			imm.hideSoftInputFromInputMethod(getWindow().getDecorView().getWindowToken(), 0); //��������
			imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0); //��Ч���������������һ��
//			imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
		}
	}

	@Override
	protected void onDestroy() {
		//�Ƴ�������Ϣ
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
