package com.sahaab.hijri.caldroid;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class SquareTextView extends androidx.appcompat.widget.AppCompatTextView {

	public SquareTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SquareTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public SquareTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//	    super.onSizeChanged(w, w, oldw, oldh);
	    super.onSizeChanged(w, h, oldw, oldh);
	}
}
