/*
 * Copyright (C) 2014 Naver Corp.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.legendleo.imagesearch.volley.ZoomableNetworkImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.GestureDetector.OnGestureListener;
/**
 * <pre>
 * A ZoomableNIV which can zoom in/out by double-tapping.
 *  
 * The maximum zoom level is 2 on this view. Also it supports pinch zooms.
 * 
 * </pre>
 * @author Wonjun Kim
 * @author Kangsoo Kim
 *
 */
public class TwoLevelDoubleTapZoomNetworkImageView extends LimitedLevelZoomNetworkImageView {

	private static final float MAXIMUM_LEVEL = 2.0f;
	private static final boolean EVENT_DONE = true;
	/**
	 * The gesture detector for double taps
	 */
	private GestureDetector gestureDetector;
	/**
	 * The gesture detector for pinch zooms
	 */
	private ScaleGestureDetector scaleGestureDetector;
	private boolean mIsScrollOver;

	public TwoLevelDoubleTapZoomNetworkImageView(Context context) {
		this(context, null);
	}

	public TwoLevelDoubleTapZoomNetworkImageView(Context context, AttributeSet attrs) {
		this(context, attrs, ZoomableNetworkImageView.NONE_DEF_STYLE);
	}

	public TwoLevelDoubleTapZoomNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// Assign a scale gesture detector for pinch zooms 
		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
			
			@Override
			public void onScaleEnd(ScaleGestureDetector detector) {
				// do nothing
			}
			
			@Override
			public boolean onScaleBegin(ScaleGestureDetector detector) {
				// Do nothing
				return EVENT_DONE;
			}

			/**
			 * Scale(zoom by distance of pinch) when a pinch occurs 
			 */
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				float zoomX = detector.getFocusX(), zoomY = detector.getFocusY();
				float dScale = detector.getScaleFactor();
				TwoLevelDoubleTapZoomNetworkImageView.this.scaleTo(dScale, zoomX, zoomY);
				return EVENT_DONE;
			}
		});
		// Assign a gesture detector for double taps
		gestureDetector = new GestureDetector(context, new OnGestureListener() {

			@Override
			public boolean onDown(MotionEvent e) {
				// Do nothing
				return EVENT_DONE;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				// Do nothing
				return EVENT_DONE;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				// Do nothing
			}

			/**
			 * Pan when scrolling
			 */
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				panTo(-distanceX, -distanceY);
				
				mIsScrollOver = isScrollOver(getmBitmapDisplayed(), distanceX);
				System.out.println("------------TwoLevelDoubleTapZoom...onScroll------------ isScrollOver:" + mIsScrollOver);
				
				return EVENT_DONE;
			}

			@Override
			public void onShowPress(MotionEvent e) {
				// Do nothing
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return EVENT_DONE;
			}
		});
		// Create a setting for double taps
		gestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent event) {
				//single tapʱ����ImageView�ĵ���¼�������ImageView��onClick�¼���onTouchEvent��ͻ��ֱ�ӵ��ò������ã����ʲ���performClick
				TwoLevelDoubleTapZoomNetworkImageView.this.performClick();
				
				return EVENT_DONE;
			}
			
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				// Do nothing
				return EVENT_DONE;
			}

			/**
			 * Zoom when double-tapping
			 */
			@Override
			public boolean onDoubleTap(MotionEvent event) {
				float zoomX = event.getX();
				float zoomY = event.getY();

				float targetLevel = computeTargetZoomLevel();

				TwoLevelDoubleTapZoomNetworkImageView.this.zoomTo(targetLevel, zoomX, zoomY);
				return EVENT_DONE;
			}
			

		});
	}
	/**
	 * Switch a zoom level between 1 and 2.
	 * @return Target zoom level
	 */
	private float computeTargetZoomLevel() {
		// Toggle the zoom level between 1 and 2.
		float targetLevel = Math.round(getZoomLevel()) % 2 + 1;
		return targetLevel;
	}

	@Override
	protected void onInitialized() {
	}
	// Edit by legendleo 2014-11-3 start
	/**
	 * Binds gesture detectors and route touch events to it
	 */
//	@Override 
//	public boolean dispatchTouchEvent(MotionEvent event) { 
//		boolean isEventDone = scaleGestureDetector.onTouchEvent(event);
//	    isEventDone = gestureDetector.onTouchEvent(event) || isEventDone;
//	    return isEventDone || super.onTouchEvent(event);
//	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean isEventDone = scaleGestureDetector.onTouchEvent(event);
	    isEventDone = gestureDetector.onTouchEvent(event) || isEventDone;
	    System.out.println("TwoLevelDoubleTab onTouchEvent getZoomLevel:" + getZoomLevel() + " mIsScrollOver:" + mIsScrollOver);
	    //����Ŵ��жϣ��򻬶�ʱ������viewpager�����һ���
	    if(getZoomLevel() > 1.0){

			//�Ŵ����ͼƬ�������߽�
			if(mIsScrollOver){
				getParent().requestDisallowInterceptTouchEvent(false);
			}else{
				getParent().requestDisallowInterceptTouchEvent(true);
			}
	    }
	    
	    return isEventDone || super.onTouchEvent(event);
	}
	// Edit by legendleo 2014-11-3 end
	
	@Override
	protected float determineMaximumZoomLevel() {
		return MAXIMUM_LEVEL;
	}
}
