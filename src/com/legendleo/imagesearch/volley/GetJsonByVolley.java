package com.legendleo.imagesearch.volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Handler;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.legendleo.imagesearch.net.NetUtil;
import com.legendleo.imagesearch.net.URLUtil;

public class GetJsonByVolley {
	private List<String[]> mList;
	private Context mContext;
	private Handler mHandler;
	public GetJsonByVolley(Context context, Handler handler){
		mContext = context;
		mHandler = handler;
		mList = new ArrayList<String[]>();
	}
	
	public void getJsonByVolley(String keyword, int page, final int flag){
		//网络连接正常
		if(NetUtil.CheckNet(mContext)){
			String searchUrl = "";
			if(flag == 0){
				searchUrl = URLUtil.generateCategoryUrl(keyword, page);
			}else if(flag ==1){
				searchUrl = URLUtil.generateSearchUrl(keyword, page);
			}
			
			System.out.println("GetJsonByVolley getJsonByVolley:" + searchUrl);
			RequestQueue requestQueue = Volley.newRequestQueue(mContext);
	
			JsonUTF8Request jsonUTF8Request = new JsonUTF8Request(
					Request.Method.GET, searchUrl, null, 
					new Listener<JSONObject>() {
	
						@Override
						public void onResponse(JSONObject response) {
							try {
	//							String queryEnc = response.getString("queryEnc");
	//							int	listNum = response.getInt("listNum");
	//							int displayNum = response.getInt("displayNum");
								JSONArray data = response.getJSONArray("data");
								int len = data.length() - 1; //data最后有一个空值
								System.out.println("data.length:" + len);
	
								mList.clear(); //先清空
								for (int i = 0; i < len; i++) {
									String imageJson = data.getString(i);
									JSONTokener jsonParser = new JSONTokener(imageJson);
									JSONObject imageObject = (JSONObject) jsonParser.nextValue();
									String[] strUrls = new String[2];
									if(flag == 0){
										strUrls[0] = imageObject.getString("thumbnail_url");
										strUrls[1] = imageObject.getString("download_url");
									}else if(flag == 1){
										strUrls[0] = imageObject.getString("thumbURL");
										strUrls[1] = imageObject.getString("objURL");
									}
									mList.add(strUrls);
								}
								
								//数据获取完成后,发送消息通知
								mHandler.sendEmptyMessage(0);
								System.out.println("getJsonByVolley sendEmptyMessage mList size:" + mList.size());
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}, 
					new ErrorListener() {
	
						@Override
						public void onErrorResponse(VolleyError error) {
	
						}
					});
	
			requestQueue.add(jsonUTF8Request);
		}
	}
	
	public List<String[]> getmList() {
		return mList;
	}
}
