package com.nexfi.yuanpeigen.nexfi_android_ble.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;

import java.util.LinkedList;
import java.util.List;

public class MyAdapter extends CommonAdapter<String>
{
	private Context mContext;

	/**
	 * 用户选择的图片，存储为图片的完整路径
	 */
	public static List<String> mSelectedImage = new LinkedList<String>();

	/**
	 * 文件夹路径
	 */
	private String mDirPath;

	public MyAdapter(Context context, List<String> mDatas, int itemLayoutId,
					 String dirPath)
	{
		super(context, mDatas, itemLayoutId);
		this.mDirPath = dirPath;
		this.mContext=context;
	}

	@Override
	public void convert(final ViewHolder helper, final String item)
	{
		//设置no_pic
		helper.setImageResource(R.id.id_item_image, R.drawable.pictures_no);
		//设置no_selected
//				helper.setImageResource(R.id.id_item_select,
//						R.drawable.picture_unselected);
		//设置图片
		helper.setImageByUrl(R.id.id_item_image, mDirPath + "/" + item);
		
		final ImageView mImageView = helper.getView(R.id.id_item_image);
//		final ImageView mSelect = helper.getView(R.id.id_item_select);
		
		mImageView.setColorFilter(null);
		//设置ImageView的点击事件
//		mImageView.setOnClickListener(new View.OnClickListener()
//		{
//			//选择，则将图片变暗，反之则反之
//			@Override
//			public void onClick(View v)
//			{//TODO 这里的逻辑修改为：选中一个图片就直接发出去
//				Log.e("TAG", "选择的路径是：     " + mDirPath + "/" + item);
//				String picturePath=mDirPath + "/" + item;
//				Intent intent=new Intent(mContext, SelectBigImageActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent.putExtra("picturePath",picturePath);
//				mContext.startActivity(intent);
				// 已经选择过该图片
//				if (mSelectedImage.contains(mDirPath + "/" + item))
//				{
//					mSelectedImage.remove(mDirPath + "/" + item);
//					mSelect.setImageResource(R.drawable.picture_unselected);
//					mImageView.setColorFilter(null);
//				} else
//				// 未选择该图片
//				{
//					mSelectedImage.add(mDirPath + "/" + item);
//
//					Log.e("TAG", "选择的路径是：     " + mDirPath + "/" + item);
//					mSelect.setImageResource(R.drawable.pictures_selected);
//					mImageView.setColorFilter(Color.parseColor("#77000000"));
//				}
//			}
//		});

//		/**
//		 * 已经选择过的图片，显示出选择过的效果
//		 */
//		if (mSelectedImage.contains(mDirPath + "/" + item))
//		{
//			mSelect.setImageResource(R.drawable.pictures_selected);
//			mImageView.setColorFilter(Color.parseColor("#77000000"));
//		}

	}
}
