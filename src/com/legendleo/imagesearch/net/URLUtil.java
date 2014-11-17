package com.legendleo.imagesearch.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.legendleo.imagesearch.R;

public class URLUtil {
	/**
	 * ����ͼƬ����
	 */
	public static final int RN = 20;
	/**
	 * �ӷ��ص�һ�ſ�ʼ����һҳ���rn * (RN + 1)��ʼ
	 */
	public static int pn = 0;
	/**
	 * ����URL
	 */
	//public static final String CATEGORY_URL = "http://image.baidu.com/channel/listjson?ie=utf-8&oe=utf-8&pn=0&rn=1&tag1=��ֽ&tag2=�羰&tag3=����ѩ��";
	public static final String CATEGORY_URL = "http://image.baidu.com/channel/listjson?ie=utf-8&oe=utf-8";
	/**
	 * ����URL
	 */
	public static final String SEARCH_URL = "http://image.baidu.com/i?tn=baiduimagejson&ie=utf-8&oe=utf-8";

	public static final Integer[] mThumbIds = { 
		R.drawable.c2, R.drawable.c3, R.drawable.c4, R.drawable.c6,
		R.drawable.c7, R.drawable.c8, R.drawable.c9, R.drawable.c10,
		R.drawable.c11, R.drawable.c12, R.drawable.c13, R.drawable.c14,
		R.drawable.c15, R.drawable.c1, R.drawable.c17, R.drawable.c18,
		R.drawable.c19, R.drawable.c16 
	};
	
	//��ӦmThumbIds
	public static final String[] mThumbTitles = {
		"����","��ֽ","��Ц","����",
		"����","��Ӱ","���","�Ҿ�",
		"���","��ʳ","����","����",
		"ֲ��","����","��ͯ����","DIY",
		"��Ů","��ױ"
	};
	

	public static String generateCategoryUrl(String tag1, int page){
		
		//�����ؼ���ת��
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
		
		//�����ؼ���ת��
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
