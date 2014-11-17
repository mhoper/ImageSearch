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
	    	//*-------------start �˶δ�����4.0.3����Ч��������imageView�߿��requestLayout()����û�������ã���û�е���onMeasure���¼���
	    	//��4.4.2�ϲ�����Ч
	    	if(bm != null){
	    		System.out.println("setImageBitmap StaggeredGridView bitmap width:" + bm.getWidth());
	    		System.out.println("setImageBitmap StaggeredGridView bitmap height:" + bm.getHeight());
	    		//��ȡͼƬ�������ű�
	    		float ratio = bm.getWidth() / (float)mCloumnWidth;
	    		//��ȡͼƬ���ź�ĸ߶�
	    		int ht = (int) (bm.getHeight() / ratio);
	    		System.out.println("ratio:" + ratio + " ht:" + ht);
	    		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mCloumnWidth, ht);
	    		this.setLayoutParams(lp);
	    		//����ˢ��view
	    		requestLayout(); //�������ڣ�
	    		
	    	}
	    	//*-------------end
	    	
	    	super.setImageBitmap(bm);
	    }
	    
}
