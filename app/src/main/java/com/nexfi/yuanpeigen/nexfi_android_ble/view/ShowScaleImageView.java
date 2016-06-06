package com.nexfi.yuanpeigen.nexfi_android_ble.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

public class ShowScaleImageView extends ImageView implements OnGlobalLayoutListener{
	
	private boolean mOnce;
	
	private float mInitScale;
	
	private float mMidScale;

	private float mMaxScale;

	private Matrix mScaleMatrix;
	



	



	public ShowScaleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mScaleMatrix=new Matrix();
		setScaleType(ScaleType.MATRIX);
	}


	public ShowScaleImageView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
		// TODO Auto-generated constructor stub
	}

	public ShowScaleImageView(Context context) {
		this(context,null);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);//ע�����
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		getViewTreeObserver().removeOnGlobalLayoutListener(this);
	}

	@Override
	public void onGlobalLayout() {
		// TODO Auto-generated method stub
		if(!mOnce){
			//�õ��ؼ��Ŀ��
			int width=getWidth();
			int height=getHeight();
			//�õ�ͼƬ���Լ����
			Drawable d=getDrawable();
			if(d==null){
				return;
			}
			int dw=d.getIntrinsicWidth();
			int dh=d.getIntrinsicHeight();
			
			float scale=1.0f;
			
			if(dw>width && dh<height){
				scale=width*1.0f/dw;
			}
			
			if(dh>height && dw<width){
				scale=height*1.0f/dh;
			}
			
			if(dw>width && dh>height){
				scale=Math.min(width*1.0f/dw,height*1.0f/dw);
			}
			
			if(dw<width && dh<height){
				scale=Math.min(width*1.0f/dh,height*1.0f/dh);
			}
			
			mInitScale=scale;
			mMaxScale=mInitScale*4;
			mMidScale=mInitScale*2;
			
			int dx=getWidth()/2-dw/2;
			int dy=getHeight()/2-dh/2;
			
			mScaleMatrix.postTranslate(dx, dy);
			mScaleMatrix.postScale(mInitScale, mInitScale,width/2,height/2);
			setImageMatrix(mScaleMatrix);
			
			mOnce=true;
		}
	}


	private RectF getMatrixRectF(){
		Matrix matrix=mScaleMatrix;
		RectF rectF=new RectF();
		Drawable drawable=getDrawable();
		if(drawable!=null){
			rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			matrix.mapRect(rectF);
		}
		return rectF;
	}

}
