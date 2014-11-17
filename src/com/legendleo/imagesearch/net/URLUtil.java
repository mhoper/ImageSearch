package com.legendleo.imagesearch.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.legendleo.imagesearch.R;

public class URLUtil {
	/**
	 * 返回图片数量
	 */
	public static final int RN = 20;
	/**
	 * 从返回第一张开始，下一页则从rn * (RN + 1)开始
	 */
	public static int pn = 0;
	/**
	 * 分类URL
	 */
	//public static final String CATEGORY_URL = "http://image.baidu.com/channel/listjson?ie=utf-8&oe=utf-8&pn=0&rn=1&tag1=壁纸&tag2=风景&tag3=冰天雪地";
	public static final String CATEGORY_URL = "http://image.baidu.com/channel/listjson?ie=utf-8&oe=utf-8";
	/**
	 * 搜索URL
	 */
	public static final String SEARCH_URL = "http://image.baidu.com/i?tn=baiduimagejson&ie=utf-8&oe=utf-8";

	public static final Integer[] mThumbIds = { 
		R.drawable.c2, R.drawable.c3, R.drawable.c4, R.drawable.c6,
		R.drawable.c7, R.drawable.c8, R.drawable.c9, R.drawable.c10,
		R.drawable.c11, R.drawable.c12, R.drawable.c13, R.drawable.c14,
		R.drawable.c15, R.drawable.c1, R.drawable.c17, R.drawable.c18,
		R.drawable.c19, R.drawable.c16 
	};
	
	//对应mThumbIds
	public static final String[] mThumbTitles = {
		"明星","壁纸","搞笑","动漫",
		"旅游","摄影","婚嫁","家居",
		"设计","美食","汽车","动物",
		"植物","军事","儿童文艺","DIY",
		"美女","美妆"
	};
	

	public static String generateCategoryUrl(String tag1, int page){
		
		//搜索关键字转码
		if(tag1 == null){
			tag1 = "";
		}else{
			try {
				tag1 = URLEncoder.encode(tag1, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		pn = RN * page;
		
		return CATEGORY_URL + "&rn=" + RN + "&pn=" + pn + "&tag1=" + tag1 + "&tag2=&tag3=";
	}
	
	public static String generateSearchUrl(String keyword, int page){
		
		//搜索关键字转码
		if(keyword == null){
			keyword = "";
		}else{
			try {
				keyword = URLEncoder.encode(keyword, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		pn = RN * page;
		
		return SEARCH_URL + "&rn=" + RN + "&pn=" + pn + "&word=" + keyword;
	}
}
