package com.legendleo.imagesearch.volley;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.android.volley.toolbox.NetworkImageView;

public class DynamicHeightNetworkImageView extends NetworkImageView {

	  private double mHeightRatio;
	  private int mCloumnWidth;

	    public DynamicHeightNetworkImageView(Context context, AttributeSet attrs) {
	        super(context, attrs);
	    }

	    public DynamicHeightNetworkImageView(Context context) {
	        super(context);
	    }

	    public void setHeightRatio(double ratio) {
	        if (ratio != mHeightRatio) {
	            mHeightRatio = ratio;
	            //requestLayout();
	        }
	    }

	    public void setmCloumnWidth(int columnWidth) {
			this.mCloumnWidth = columnWidth;
		}

	    @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	        if (mHeightRatio > 0.0) {
	        	System.out.println("onMeasure StaggeredGridView mHeightRatio:" + mHeightRatio);
	            // set the image views size
	            int width = MeasureSpec.getSize(widthMeasureSpec);
	            int height = MeasureSpec.getSize(heightMeasureSpec);
	            if(height == 0){
	            	height = (int) (width * mHeightRatio);
	            }
		    	System.out.println("onMeasure StaggeredGridView width:" + width);
		    	System.out.println("onMeasure StaggeredGridView height:" + height);
	            setMeasuredDimension(width, height);
	        }
	        else {
	            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	        }
	    }

	    @Override
	    public void setImageBitmap(Bitmap bm) {
	    	//*-------------start 此段代码在4.0.3上无效，在设置imageView高宽后，requestLayout()请求没有起作用，并没有调用onMeasure重新计算
	    	//在4.4.2上测试有效
	    	if(bm != null){
	    		System.out.println("setImageBitmap StaggeredGridView bitmap width:" + bm.getWidth());
	    		System.out.println("setImageBitmap StaggeredGridView bitmap height:" + bm.getHeight());
	    		//获取图片所需缩放比
	    		float ratio = bm.getWidth() / (float)mCloumnWidth;
	    		//获取图片缩放后的高度
	    		int ht = (int) (bm.getHeight() / ratio);
	    		System.out.println("ratio:" + ratio + " ht:" + ht);
	    		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mCloumnWidth, ht);
	    		this.setLayoutParams(lp);
	    		//请求刷新view
	    		requestLayout(); //问题所在？
	    		
	    	}
	    	//*-------------end
	    	
	    	super.setImageBitmap(bm);
	    }
	    
}
