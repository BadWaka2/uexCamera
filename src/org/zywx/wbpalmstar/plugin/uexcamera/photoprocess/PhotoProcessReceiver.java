package org.zywx.wbpalmstar.plugin.uexcamera.photoprocess;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 图像处理广播接收器
 * 
 * 主线程注册此广播用于跟图像处理进程进行通信
 * 
 * Created by waka on 2016/6/24.
 */
public class PhotoProcessReceiver extends BroadcastReceiver {

	private PhotoProcessCompleteCallback mCallback;

	/**
	 * Constructor
	 *
	 * @param presenter
	 */
	public PhotoProcessReceiver(PhotoProcessCompleteCallback callback) {
		mCallback = callback;
	}

	@Override
	/**
	 * onReceive
	 */
	public void onReceive(Context context, Intent intent) {

		if (PhotoProcessService.ACTION_PHOTO_PROCESS_COMPLETED.equals(intent.getAction())) {

			boolean flag = intent.getBooleanExtra(PhotoProcessService.KEY_FLAG, false);
			PhotoBean photoBean = intent.getParcelableExtra(PhotoProcessService.KEY_PHOTO_BEAN);
			mCallback.onPhotoProcessComplete(flag, photoBean);

		}
	}
}
