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
		//设置图片
		helper.setImageByUrl(R.id.id_item_image, mDirPath + "/" + item);
		
		final ImageView mImageView = helper.getView(R.id.id_item_image);

		mImageView.setColorFilter(null);
	}
}
