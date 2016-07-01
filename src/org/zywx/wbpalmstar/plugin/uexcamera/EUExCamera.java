package org.zywx.wbpalmstar.plugin.uexcamera;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexcamera.ViewCamera.CallbackCameraViewClose;
import org.zywx.wbpalmstar.plugin.uexcamera.ViewCamera.CameraView;
import org.zywx.wbpalmstar.plugin.uexcamera.photoprocess.PhotoBean;
import org.zywx.wbpalmstar.plugin.uexcamera.photoprocess.PhotoProcessCompleteCallback;
import org.zywx.wbpalmstar.plugin.uexcamera.photoprocess.PhotoProcessReceiver;
import org.zywx.wbpalmstar.plugin.uexcamera.photoprocess.PhotoProcessService;
import org.zywx.wbpalmstar.plugin.uexcamera.utils.FileUtil;
import org.zywx.wbpalmstar.plugin.uexcamera.utils.MLog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;

public class EUExCamera extends EUExBase implements CallbackCameraViewClose, PhotoProcessCompleteCallback {

	// 回调
	private static final String FUNC_OPEN_CALLBACK = "uexCamera.cbOpen";// 打开系统相机回调
	private static final String FUNC_OPEN_INTERNAL_CALLBACK = "uexCamera.cbOpenInternal";// 打开自定义相机回调
	private static final String FUNC_OPEN_VIEW_CAMERA_CALLBACK = "uexCamera.cbOpenViewCamera";// 打开自定义相机View回调
	private static final String FUNC_CHANGE_FLASHMODE_CALLBACK = "uexCamera.cbChangeFlashMode";// 改变闪关灯模式的回调
	private static final String FUNC_CHANGE_CAMERA_POSITION_CALLBACK = "uexCamera.cbChangeCameraPosition";// 改变摄像头位置的回调

	private String label = "";// 拍照时显示在界面中的提示语或标签
	private View view;// 自定义相机View
	private CameraView mCameraView;// 自定义相机View实例

	/**
	 * 是否压缩
	 */
	private boolean mIsCompress = false;

	/**
	 * 压缩质量
	 */
	private int mQuality = 100;

	/**
	 * 临时图片文件
	 */
	private File mTempPhotoFile;

	/**
	 * 当前调用图像处理Service时需要回调的方法
	 */
	private String mCbMethod = "";

	/**
	 * 广播接收器,用来与图像处理Service进行IPC通信
	 */
	private PhotoProcessReceiver mReceiver;

	/**
	 * 构造方法
	 * 
	 * @param context
	 * @param inParent
	 */
	public EUExCamera(Context context, EBrowserView inParent) {
		super(context, inParent);
	}

	@Override
	protected boolean clean() {
		return false;
	}

	/**
	 * 注册进程间通信广播接收器
	 */
	private void registerPhotoProcessReceiver() {
		if (mReceiver == null) {
			mReceiver = new PhotoProcessReceiver(this);
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(PhotoProcessService.ACTION_PHOTO_PROCESS_COMPLETED);
		mContext.registerReceiver(mReceiver, filter);
	}

	/**
	 * 注销进程间通信广播接收器
	 */
	private void unregisterPhotoProcessReceiver() {
		mContext.unregisterReceiver(mReceiver);
		mReceiver = null;
	}

	/**
	 * 初始化压缩相关成员变量
	 */
	private void initCompressFields() {

		mIsCompress = false;// 初始化压缩标志
		mQuality = 100;// 初始化压缩质量，默认为100，即不压缩

	}

	/**
	 * 解析从前端传来的数据(打开相机)
	 * 
	 * @param params
	 */
	private void analysisDataFromFrontByOpen(String[] params) {

		if (params.length < 2) {
			MLog.getIns().e("params.length < 2");
			return;
		}

		try {

			int compressInt = Integer.valueOf(params[0]);
			int quality = Integer.valueOf(params[1]);

			if (compressInt == 0) {
				mIsCompress = true;
			}
			if (quality > 0 && quality <= 100) {
				mQuality = quality;
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
			MLog.getIns().e(e);
		}

	}

	/**
	 * 得到输出路径
	 * 
	 * @return
	 */
	private String getOutputPath() {

		String folderPath = FileUtil.getRootPath() + "/widgetone/apps/" + mBrwView.getRootWidget().m_appId + "/photo";// 获得文件夹路径
		FileUtil.checkFolderPath(folderPath);// 如果不存在，则创建所有的父文件夹
		String newPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/widgetone/apps/" + mBrwView.getRootWidget().m_appId + "/"
				+ FileUtil.getSimpleDateFormatFileName("photo/scan", ".jpg");// 获得新的存放目录
		return newPath;

	}

	/**
	 * 打开系统相机
	 * 
	 * @param params
	 */
	public void open(String[] params) {

		// 初始化压缩相关成员变量
		initCompressFields();

		// 注册广播接收器
		registerPhotoProcessReceiver();

		// 解析前端数据
		analysisDataFromFrontByOpen(params);

		// 图片保存路径
		mTempPhotoFile = new File(getOutputPath());

		// 调用系统相机
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		Uri uri = Uri.fromFile(mTempPhotoFile);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(intent, Constant.REQUEST_CODE_SYSTEM_CAMERA);

	}

	/**
	 * 打开自定义相机
	 */
	public void openInternal(String[] params) {

		// 初始化压缩相关成员变量
		initCompressFields();

		// 注册广播接收器
		registerPhotoProcessReceiver();

		// 解析前端数据
		analysisDataFromFrontByOpen(params);

		// 发Intent调用自定义相机
		Intent intent = new Intent(mContext, CustomCameraActivity.class);
		intent.putExtra(Constant.INTENT_EXTRA_NAME_PHOTO_PATH, mTempPhotoFile.getAbsolutePath());
		startActivityForResult(intent, Constant.REQUEST_CODE_CUSTOM_CAMERA);

	}

	/**
	 * 打开自定义View相机
	 * 
	 * @param parm
	 */
	public void openViewCamera(String[] parm) {
		// Toast.makeText(mContext, "openViewCamera",
		// Toast.LENGTH_SHORT).show();
		if (parm.length < 6) {
			return;
		}
		String inX = parm[0];
		String inY = parm[1];
		String inW = parm[2];
		String inH = parm[3];
		label = parm[4];
		// 新字段 图片质量
		int quality = -1;// 初始化为-1
		try {
			quality = Integer.valueOf(parm[5]);
			// 对quality进行容错处理
			if (quality < 0) {
				quality = 0;
			} else if (quality > 100) {
				quality = 100;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		MLog.getIns().i("label = " + label);
		MLog.getIns().i("quality = " + quality);

		int x = 0;
		int y = 0;
		int w = 0;
		int h = 0;
		try {
			x = Integer.parseInt(inX);
			y = Integer.parseInt(inY);
			w = Integer.parseInt(inW);
			h = Integer.parseInt(inH);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		// 2016/1/19 增加超出分辨率判断
		/** 获得屏幕分辨率 **/
		Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int screenWidth = size.x;
		int screenHeight = size.y;

		if ((x + w) > screenWidth) {// 如果宽度超出屏幕宽度
			w = screenWidth - x;// 限制最大为屏幕宽度
		}
		if ((y + h) > screenHeight) {// 如果高度超出屏幕高度
			h = screenHeight - y;// 限制最大为屏幕高度
		}

		if (null == view) {
			// // Dynamic get resources ID, does not allow use R
			// int myViewID =
			// EUExUtil.getResLayoutID("plugin_uex_demo_test_view");
			// if (myViewID <= 0) {
			// Toast.makeText(mContext, "找不到名为:my_uex_test_view的layout文件!",
			// Toast.LENGTH_LONG).show();
			// return;
			// }
			view = View.inflate(mContext, EUExUtil.getResLayoutID("plugin_camera_view_camera"), null);// 用view引入布局文件
			mCameraView = (CameraView) view;// 将View强转为CameraView，获得CameraView的实例
			mCameraView.setmEuExCamera(this);// 设置EUExCamera的实例
			String filePath = mBrwView.getWidgetPath() + "uexViewCameraPhotos";
			MLog.getIns().i("filePath = " + filePath);
			mCameraView.setFilePath(filePath);
			mCameraView.setCallbackCameraViewClose(this);// 注册callback，将当前类传入
			mCameraView.setLabelText(label);// 调用方法写入地址
			if (quality != -1) {// 如果quality格式正确
				mCameraView.setQuality(quality);
			}
			RelativeLayout.LayoutParams lparm = new RelativeLayout.LayoutParams(w, h);
			lparm.leftMargin = x;
			lparm.topMargin = y;
			addViewToCurrentWindow(mCameraView, lparm);
		}
	}

	/**
	 * 移除自定义相机
	 * 
	 * @param parm
	 */
	public void removeViewCameraFromWindow(String[] parm) {
		if (null != view) {
			removeViewFromCurrentWindow(view);
			view = null;
		}
		// Toast.makeText(mContext, "removeViewCameraFromWindow",
		// Toast.LENGTH_SHORT).show();
	}

	/**
	 * 更改闪光灯模式,只允许输入0、1、2三个数字,0代表自动，1代表开启，2代表关闭,默认为关闭
	 * 
	 * @param parm
	 */
	public void changeFlashMode(String[] parm) {
		String flashMode = parm[0];
		if (flashMode.equals("0") || flashMode.equals("1") || flashMode.equals("2")) {
			mCameraView.setFlashMode(Integer.valueOf(flashMode));
			jsCallback(FUNC_CHANGE_FLASHMODE_CALLBACK, 0, EUExCallback.F_C_TEXT, flashMode);
		} else {
			jsCallback(FUNC_CHANGE_FLASHMODE_CALLBACK, 0, EUExCallback.F_C_TEXT, "-1");
		}
	}

	/**
	 * 设置前后摄像头,只允许输入0、1两个数字,0代表前置，1代表后置,默认为后置
	 * 
	 * @param parm
	 */
	public void changeCameraPosition(String[] parm) {
		String cameraPosition = parm[0];
		if (view == null) {
			return;
		}
		if (cameraPosition.equals("0")) {
			CameraView.cameraPosition = 1;
			((Activity) mContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mCameraView.overturnCamera();
				}
			});
			jsCallback(FUNC_CHANGE_CAMERA_POSITION_CALLBACK, 0, EUExCallback.F_C_TEXT, cameraPosition);
		} else if (cameraPosition.equals("1")) {
			CameraView.cameraPosition = 0;
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mCameraView.overturnCamera();
				}

			});
			jsCallback(FUNC_CHANGE_CAMERA_POSITION_CALLBACK, 0, EUExCallback.F_C_TEXT, cameraPosition);
		} else {
			jsCallback(FUNC_CHANGE_CAMERA_POSITION_CALLBACK, 0, EUExCallback.F_C_TEXT, "-1");
		}

	}

	/**
	 * onActivityResult
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		// 系统相机
		if (requestCode == Constant.REQUEST_CODE_SYSTEM_CAMERA) {

			// OK
			if (resultCode == Activity.RESULT_OK) {

				MLog.getIns().d("系统相机拍照完成");

				// 不压缩
				if (!mIsCompress) {
					jsCallback(FUNC_OPEN_CALLBACK, 0, EUExCallback.F_C_TEXT, mTempPhotoFile.getAbsolutePath());// 直接返回临时图片地址
				}
				// 压缩
				else {
					PhotoBean photoBean = new PhotoBean();
					photoBean.setSourcePath(mTempPhotoFile.getAbsolutePath());
					photoBean.setOutputPath(getOutputPath());
					photoBean.setQuality(mQuality);
					compressPhotoInPhotoProcessService(FUNC_OPEN_CALLBACK, photoBean);
				}

			}

			// Cancel
			else if (resultCode == Activity.RESULT_CANCELED) {

				MLog.getIns().d("系统相机拍照取消");

			}

		}

		// 自定义相机
		else if (requestCode == Constant.REQUEST_CODE_CUSTOM_CAMERA) {

			// OK
			if (resultCode == Activity.RESULT_OK) {

				MLog.getIns().d("Custom相机拍照完成");

				// 不压缩
				if (!mIsCompress) {
					jsCallback(FUNC_OPEN_INTERNAL_CALLBACK, 0, EUExCallback.F_C_TEXT, mTempPhotoFile.getAbsolutePath());// 直接返回临时图片地址
				}
				// 压缩
				else {
					PhotoBean photoBean = new PhotoBean();
					photoBean.setSourcePath(mTempPhotoFile.getAbsolutePath());
					photoBean.setOutputPath(getOutputPath());
					photoBean.setQuality(mQuality);
					compressPhotoInPhotoProcessService(FUNC_OPEN_INTERNAL_CALLBACK, photoBean);
				}

			}

			// Cancel
			else if (resultCode == Activity.RESULT_CANCELED) {
				MLog.getIns().d("Custom相机拍照取消");
			}
		}

		// ViewCamera
		else if (requestCode == Constant.REQUEST_CODE_VIEW_CAMERA) {

			// OK
			if (resultCode == Activity.RESULT_OK) {

				MLog.getIns().i("requestCode = " + requestCode);
				removeViewCameraFromWindow(null);// 移除自定义View相机
				String photoPath = data.getStringExtra("photoPath");
				String jsonResult = "";
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("photoPath", photoPath);
					jsonObject.put("location", label);
					jsonObject.put("label", label);
					jsonResult = jsonObject.toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				jsCallback(FUNC_OPEN_VIEW_CAMERA_CALLBACK, 0, EUExCallback.F_C_TEXT, jsonResult);

			}

			// Cancel
			else if (resultCode == Activity.RESULT_CANCELED) {
				mCameraView.setCameraTakingPhoto(false);// 设置正在照相标记为false
			}

		}
	}

	/**
	 * CameraView的关闭回调，在这里移除View
	 */
	@Override
	public void callbackClose() {
		if (null != view) {
			removeViewFromCurrentWindow(view);
			view = null;
		}
	}

	/**
	 * 在另一个进程中的图片压缩Service中处理图片
	 * 
	 * @param cbMethod
	 *            图片处理完成后回调的方法
	 * @param photoBean
	 */
	private void compressPhotoInPhotoProcessService(String cbMethod, PhotoBean photoBean) {

		mCbMethod = cbMethod;

		Intent intent = new Intent(mContext, PhotoProcessService.class);
		intent.putExtra(PhotoProcessService.KEY_PHOTO_BEAN, photoBean);// 传入bean
		mContext.startService(intent);

	}

	@Override
	/**
	 * 另一个进程中图像处理完成回调
	 */
	public void onPhotoProcessComplete(boolean flag, PhotoBean photoBean) {

		// 注销广播接收器
		unregisterPhotoProcessReceiver();

		if (mCbMethod.isEmpty()) {
			MLog.getIns().e("mCbMethod.isEmpty()");
			return;
		}

		if (flag) {
			jsCallback(mCbMethod, 0, EUExCallback.F_C_TEXT, photoBean.getOutputPath());
		}

		// 关闭图像处理进程
		Intent intent = new Intent(mContext, PhotoProcessService.class);
		mContext.stopService(intent);
	}
}
