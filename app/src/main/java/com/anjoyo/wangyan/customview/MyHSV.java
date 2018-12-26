package com.anjoyo.wangyan.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import com.anjoyo.wangyan.activity.MainActivity;

/**
 * 自定义控件
 */
public class MyHSV extends HorizontalScrollView {

	/** 获得点击的坐标 */
	// 手点在屏幕上x轴的坐标
	private int currentOffset = 0;
	// 获得当前window的宽度
	private int sumWidth;

	private int btnWidth;
	private int txtArgWidth;
	private int appWidth;

	public static float deX;

	public MyHSV(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * 设置
	 * 
	 * @param context
	 */
	void init(Context context) {
		// remove the fading as the HSV looks better without it
		setHorizontalFadingEdgeEnabled(false);// 删除滚动阴影部分
		setVerticalFadingEdgeEnabled(false);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		currentOffset = (int) ev.getRawX();// 点击的坐标
		sumWidth = this.getMeasuredWidth();// 获得当前window的宽度
		// 获得Scroll是收缩还是拉伸位移
		MainActivity.offset = computeHorizontalScrollOffset();
		System.out.println("~~~" + currentOffset + "~~" + sumWidth + "--"
				+ btnWidth + "~" + computeHorizontalScrollOffset());
		if (MainActivity.offset == 0) {// 没有滑动
			if (currentOffset <= (sumWidth - btnWidth)) {
				return false;// Do not allow touch events.
			} else {
//				comEvent(ev);
				return super.onTouchEvent(ev);
			}
		} else {
			return super.onTouchEvent(ev);
		}
	}

	private void comEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		float x = 0;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			x = event.getX();
			break;
		case MotionEvent.ACTION_UP:
			break;
		case MotionEvent.ACTION_MOVE:
			float preX = x;
			float nowX = event.getX();
			int deltaX = (int) (preX - nowX);
			if (Math.abs(deltaX) < 150) {
				smoothScrollBy(deltaX, 0);
			}
			break;
		}
	}

	public void setBtnWith(int btnWidth) {
		this.btnWidth = btnWidth;
	}

	public void setTxtArgWidth(int txtArgWidth) {
		this.txtArgWidth = txtArgWidth;
	}

	public void setAppWidth(int appWidth) {
		this.appWidth = appWidth;
	}
}
