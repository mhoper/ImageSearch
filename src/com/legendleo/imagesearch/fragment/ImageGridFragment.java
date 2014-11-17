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
	 * ��ż��ص�Json����
	 */
	private List<String[]> mList;

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
	
	private Handler handler;
	
	public ImageGridFragment(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true); //����Ϊtrue,onCreateOptionsMenu��������
		
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
		//�������Ӽ��
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
				intent.putExtra("currentPosition", position); //��ǰ���ͼƬ��λ��
				intent.putExtra("jsonString", (Serializable)mList);
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
		
		//new handler
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 0){
					if(page == 0){
						mList.clear(); //��������ʱ���
					}
					mList.addAll(mGetJsonByVolley.getmList());
					System.out.println("handleMessage mList.size:" + mList.size());
					
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
				System.out.println("SearchResultActivity onQueryTextSubmit:" + query); //ģ������ʹ�ûس��ύΪʲô���ύ����

				flag = 1; //����flag
				page = 0; //����ʱֵ��0
				totalImagesCount = URLUtil.RN;
				keyword = query; //��query��ֵ��keyword�����ظ���ʱ���õ�
				setAdapterData();
				hideSoftInput();
				
				//�����������
				if(NetUtil.CheckNet(getActivity())){
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

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//������һ��
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

	//ȡ��������̣����������ťʱ������Ч
	private void hideSoftInput(){
		
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm != null){
			imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0); //��Ч
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
		
		//ȡ������json����
		RequestQueue rq = MySingleton.getInstance(getActivity()).getRequestQueue();
		rq.cancelAll(this);
		
		//�Ƴ�������Ϣ
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
