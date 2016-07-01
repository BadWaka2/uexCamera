package org.zywx.wbpalmstar.plugin.uexcamera.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Bitmap工具类
 * 
 * Created by waka on 2016/6/21.
 */
public class BitmapUtil {

	/**
	 * -------------基本操作---------------
	 */

	/**
	 * 将Bitmap写入文件
	 *
	 * @param bitmap
	 * @param outputPath
	 * @param quality
	 * @return
	 */
	public static boolean writeBitmapToFile(Bitmap bitmap, String outputPath, int quality) {

		BufferedOutputStream bos = null;
		try {

			bos = new BufferedOutputStream(new FileOutputStream(new File(outputPath)));
			if (outputPath.endsWith(".png")) {
				bitmap.compress(Bitmap.CompressFormat.PNG, quality, bos);
			} else if (outputPath.endsWith(".jpg") || outputPath.endsWith(".jpeg")) {
				bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
			} else if (outputPath.endsWith(".webp")) {
				bitmap.compress(Bitmap.CompressFormat.WEBP, quality, bos);
			}
			return true;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MLog.getIns().e(e);
		} finally {
			try {
				bos.flush();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
				MLog.getIns().e(e);
			}
		}
		return false;

	}

	/**
	 * -------------压缩---------------
	 */

	/**
	 * 压缩图片
	 *
	 * @param sourcePath
	 *            源路径
	 * @param outputPath
	 *            输出路径
	 * @param quality
	 *            压缩质量
	 * @param reqWidth
	 *            目标宽
	 * @param reqHeight
	 *            目标高
	 * @return
	 */
	public static final boolean compress(String sourcePath, String outputPath, int quality, int reqWidth, int reqHeight) {

		MLog.getIns().d("");

		if (sourcePath.isEmpty()) {
			MLog.getIns().e("sourcePath.isEmpty()");
			return false;
		}
		if (outputPath.isEmpty()) {
			MLog.getIns().e("outputPath.isEmpty()");
			return false;
		}

		try {

			// 不写入内存，获得图片宽高信息
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;// 不返回实际的bitmap，也不给其分配内存空间,但是允许我们查询图片的信息这其中就包括图片大小信息
			BitmapFactory.decodeStream(new FileInputStream(sourcePath), null, options);

			// 计算压缩比
			int inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			options = null;

			// 调用重载方法统一压缩
			return compress(sourcePath, outputPath, quality, inSampleSize);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MLog.getIns().e(e);
			return false;
		}
	}

	/**
	 * 压缩图片的重载
	 *
	 * @param sourcePath
	 *            源路径
	 * @param outputPath
	 *            输出路径
	 * @param quality
	 *            压缩质量
	 * @param inSampleSize
	 *            压缩比
	 * @return
	 */
	public static final boolean compress(String sourcePath, String outputPath, int quality, int inSampleSize) {

		MLog.getIns().d("");

		if (sourcePath.isEmpty()) {
			MLog.getIns().e("sourcePath.isEmpty()");
			return false;
		}
		if (outputPath.isEmpty()) {
			MLog.getIns().e("outputPath.isEmpty()");
			return false;
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		options.inPurgeable = true;// 为True的话表示使用BitmapFactory创建的Bitmap用于存储Pixel的内存空间在系统内存不足时可以被回收;在Android5.0已过时;http://blog.sina.com.cn/s/blog_7607703f0101fzl7.html
		options.inInputShareable = true;// inInputShareable与inPurgeable一起使用，如果inPurgeable为false那该设置将被忽略，如果为true，那么它可以决定位图是否能够共享一个指向数据源的引用，或者是进行一份拷贝;在Android5.0已过时;http://blog.csdn.net/xu_fu/article/details/7340454
		options.inJustDecodeBounds = false;// decode得到的bitmap将写入内存

		try {

			// 生成bitmap
			Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(sourcePath), null, options);

			// 写入文件
			if (writeBitmapToFile(bitmap, outputPath, quality)) {
				return true;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MLog.getIns().e(e);
		}

		return false;
	}

	/**
	 * 计算inSampleSize
	 *
	 * @param options
	 *            BitmapFactory.Options对象，里面包含图片的宽高信息
	 * @param reqWidth
	 *            目标宽
	 * @param reqHeight
	 *            目标高
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;

		int inSampleSize = 1;
		if (reqWidth <= 0 || reqHeight <= 0) {
			return inSampleSize;
		}

		if (height > reqHeight || width > reqWidth) {

			// 计算出实际宽高和目标宽高的比率,Math.round()四舍五入
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高。
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	/**
	 * -------------翻转（镜像）---------------
	 */

	/**
	 * 不翻转
	 */
	public static final int BITMAP_CONVERT_MODE_NO = 0;

	/**
	 * 水平翻转
	 */
	public static final int BITMAP_CONVERT_MODE_HORIZONTAL = 1;
	/**
	 * 竖直翻转
	 */
	public static final int BITMAP_CONVERT_MODE_VERTICAL = 2;

	/**
	 * 翻转Bitmap
	 *
	 * @param bitmap
	 * @param mode
	 *            翻转方式：0水平翻转，1竖直翻转
	 * @return
	 */
	public static Bitmap convertBitmap(Bitmap bitmap, int mode) {

		MLog.getIns().d("");

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Matrix matrix = new Matrix();
		if (mode == BITMAP_CONVERT_MODE_HORIZONTAL) {
			matrix.postScale(-1, 1); // 镜像水平翻转
		} else if (mode == BITMAP_CONVERT_MODE_VERTICAL) {
			matrix.postScale(1, -1); // 镜像垂直翻转
		}
		Bitmap convertBmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		return convertBmp;

	}

	/**
	 * 翻转Bitmap（直接给出文件路径和输出路径）
	 *
	 * @param sourcePath
	 *            原图路径
	 * @param outputPath
	 *            翻转后图片的保存路径，如果想覆盖，与原图路径设为一样即可
	 * @param mode
	 *            翻转方式：0水平翻转，1竖直翻转
	 * @return
	 */
	public static boolean convertBitmap(String sourcePath, String outputPath, int mode) {

		// 错误检查
		if (sourcePath.isEmpty() || outputPath.isEmpty()) {
			MLog.getIns().e("sourcePath.isEmpty() || outputPath.isEmpty()");
			return false;
		}

		try {

			Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(sourcePath));// 生成bitmap
			bitmap = convertBitmap(bitmap, mode);// 翻转bitmap

			// 写入文件
			if (writeBitmapToFile(bitmap, outputPath, 100)) {
				return true;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MLog.getIns().e(e);
		}
		return false;

	}

}
