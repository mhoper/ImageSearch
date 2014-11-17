package com.legendleo.imagesearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.legendleo.imagesearch.adapter.ImageAdapter;
import com.legendleo.imagesearch.net.NetUtil;
import com.legendleo.imagesearch.net.URLUtil;

public class MainActivity extends Activity {
	
	private GridView categoryGridView;
	private ImageAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		categoryGridView = (GridView) findViewById(R.id.categoryGridView);
		mAdapter = new ImageAdapter(this);
		categoryGridView.setAdapter(mAdapter);
		
		categoryGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int flag = 0; //���ַ��������
				Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
				intent.putExtra("category", URLUtil.mThumbTitles[position]);
				intent.putExtra("flag", flag);
				startActivity(intent);
			}
		});
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
				System.out.println("MainActivity onQueryTextSubmit:" + query); //��������ȷ�ģ�ֻ�ύһ��
				int flag = 1; //���ַ��������
				Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
				intent.putExtra("keyword", query);
				intent.putExtra("flag", flag);
				startActivity(intent);

				hideSoftInput();
				
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

	//ȡ��������̣����������ťʱ������Ч
	private void hideSoftInput(){
		
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm != null){
//			imm.hideSoftInputFromInputMethod(getWindow().getDecorView().getWindowToken(), 0); //��������
			imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0); //��Ч���������������һ��
//			imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
		}
	}
	
}