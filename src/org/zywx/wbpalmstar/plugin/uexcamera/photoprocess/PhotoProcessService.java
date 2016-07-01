package org.zywx.wbpalmstar.plugin.uexcamera.photoprocess;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;

import org.zywx.wbpalmstar.plugin.uexcamera.utils.BitmapUtil;
import org.zywx.wbpalmstar.plugin.uexcamera.utils.MLog;

/**
 * 图像处理Service
 * 
 * 因为Bitmap操作会消耗大量内存，容易导致OOM 设想一个新的方案，新建一个进程，在这里专门处理bitmap
 * 
 * Created by waka on 2016/6/21.
 */
public class PhotoProcessService extends Service {

	/**
	 * Action
	 */
	public static final String ACTION_PHOTO_PROCESS_COMPLETED = "com.waka.workspace.wakacamera.ACTION_PHOTO_PROCESS_COMPLETED";// 图像处理完成

	/**
	 * intent传递数据putExtra的key
	 */
	public static final String KEY_PHOTO_BEAN = "photoBean";// 图片Bean
	public static final String KEY_FLAG = "flag";// 结果标志

	@Override
	/**
	 * onBind
	 */
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	/**
	 * onCreate
	 */
	public void onCreate() {
		super.onCreate();
	}

	@Override
	/**
	 * onStartCommand
	 */
	public int onStartCommand(Intent intent, int flags, int startId) {

		PhotoBean photoBean = intent.getParcelableExtra(KEY_PHOTO_BEAN);
		if (photoBean == null) {
			return START_NOT_STICKY;
		}
		processPhoto(photoBean);
		return START_NOT_STICKY;
	}

	/**
	 * 处理图像
	 *
	 * @param photoBean
	 */
	private void processPhoto(PhotoBean photoBean) {

		MLog.getIns().i("SourcePath = " + photoBean.getSourcePath());
		MLog.getIns().i("OutputPath = " + photoBean.getOutputPath());
		MLog.getIns().i("ReqWidth = " + photoBean.getReqWidth());
		MLog.getIns().i("ReqHeight = " + photoBean.getReqHeight());
		MLog.getIns().i("Quality = " + photoBean.getQuality());
		MLog.getIns().i("InSampleSize = " + photoBean.getInSampleSize());
		MLog.getIns().i("Rotation = " + photoBean.getRotation());
		MLog.getIns().i("ConvertMode = " + photoBean.getConvertMode());

		// 初始化成功标志
		boolean flag = false;

		// 临时变量，用来储存最初的源路径，因为中间可能会进行多次图像处理操作，源路径会改变，但在最后发送完成广播的时候，需要重新赋值为最初的源路径
		String sourcePath = photoBean.getSourcePath();

		/**
		 * 是否压缩
		 */
		if (photoBean.getReqHeight() > 0 && photoBean.getReqWidth() > 0) {// 如果有目标宽高，则根据目标宽高计算压缩比
			flag = BitmapUtil.compress(photoBean.getSourcePath(), photoBean.getOutputPath(), photoBean.getQuality(), photoBean.getReqWidth(), photoBean.getReqHeight());
		} else {
			flag = BitmapUtil.compress(photoBean.getSourcePath(), photoBean.getOutputPath(), photoBean.getQuality(), photoBean.getInSampleSize());
		}
		if (flag) {// 如果压缩成功
			photoBean.setSourcePath(photoBean.getOutputPath());// 将生成的路径赋为源路径
		} else {// 如果压缩失败，发送失败广播，return
			sendCompletedBroadcast(flag, photoBean);
			return;
		}

		/**
		 * 是否翻转
		 */
		if (photoBean.getConvertMode() == BitmapUtil.BITMAP_CONVERT_MODE_HORIZONTAL || photoBean.getConvertMode() == BitmapUtil.BITMAP_CONVERT_MODE_VERTICAL) {
			flag = BitmapUtil.convertBitmap(photoBean.getSourcePath(), photoBean.getOutputPath(), photoBean.getConvertMode());
		}
		if (flag) {// 如果翻转成功
			photoBean.setSourcePath(photoBean.getOutputPath());// 将生成的路径赋为源路径
		} else {// 如果翻转失败，发送失败广播，return
			sendCompletedBroadcast(flag, photoBean);
			return;
		}

		/**
		 * 发送成功广播
		 */
		photoBean.setSourcePath(sourcePath);// 还原最初的源路径
		sendCompletedBroadcast(flag, photoBean);
	}

	/**
	 * 发送完成广播
	 */
	private void sendCompletedBroadcast(boolean flag, PhotoBean photoBean) {

		MLog.getIns().i("flag = " + flag);

		// 发送广播，使用广播进行IPC通信
		Intent intent = new Intent(ACTION_PHOTO_PROCESS_COMPLETED);
		intent.putExtra(KEY_FLAG, flag);// 传入标志
		intent.putExtra(KEY_PHOTO_BEAN, photoBean);// 传入bean
		sendBroadcast(intent);
	}

	@Override
	/**
	 * onDestroy
	 */
	public void onDestroy() {
		super.onDestroy();

		Process.killProcess(Process.myPid());// 结束当前进程
	}

}
