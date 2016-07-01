package org.zywx.wbpalmstar.plugin.uexcamera.photoprocess;

/**
 * 图像处理完成回调接口
 * 
 * @author waka
 * @version createTime:2016年7月1日 下午2:08:02
 */
public interface PhotoProcessCompleteCallback {

	/**
	 * 另一个进程中图像处理完成回调
	 *
	 * @param flag
	 * @param photoBean
	 */
	void onPhotoProcessComplete(boolean flag, PhotoBean photoBean);

}
