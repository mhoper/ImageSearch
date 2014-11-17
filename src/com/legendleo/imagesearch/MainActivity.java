package com.legendleo.imagesearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;

import com.legendleo.imagesearch.adapter.ImageMainAdapter;
import com.legendleo.imagesearch.net.URLUtil;

public class MainActivity extends Activity {
	
	private GridView categoryGridView;
	private ImageMainAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		categoryGridView = (GridView) findViewById(R.id.categoryGridView);
		//根据屏幕方向动态调整GridView的列数
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			categoryGridView.setNumColumns(3); //竖向为3列
		}else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			categoryGridView.setNumColumns(5); //横向为5列
		}
		
		mAdapter = new ImageMainAdapter(this);
		categoryGridView.setAdapter(mAdapter);
		
		categoryGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int flag = 0; //区分分类和搜索
				Intent intent = new Intent(MainActivity.this, ImageGridActivity.class);
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
				System.out.println("MainActivity onQueryTextSubmit:" + query); //这里是正确的，只提交一次
				int flag = 1; //区分分类和搜索
				Intent intent = new Intent(MainActivity.this, ImageGridActivity.class);
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			AboutDialog dialog = new AboutDialog(this, R.style.DialogLayout);
			dialog.show();
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
	
}
