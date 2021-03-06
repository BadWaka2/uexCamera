package org.zywx.wbpalmstar.plugin.uexcamera.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;

/**
 * 文件工具类
 * 
 * @author waka
 *
 */
public class FileUtil {

	/**
	 * 得到根目录
	 *
	 * @return
	 */
	public static String getRootPath() {

		String rootPath = null;

		// 如果外部储存可用
		if (Environment.MEDIA_MOUNTED.equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()) {
			rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
		}
		return rootPath;
	}

	/**
	 * 得到以日期命名的文件名
	 * 
	 * @param prefix前缀
	 * @param postfix后缀
	 * @return 文件名
	 */
	public static String getSimpleDateFormatFileName(String prefix, String postfix) {

		Date date = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
		String fileName = prefix + simpleDateFormat.format(date) + postfix;

		return fileName;
	}

	/**
	 * 检查文件夹路径是否存在，不存在则创建
	 * 
	 * @param folderPath
	 * @return 返回创建结果
	 */
	public static boolean checkFolderPath(String folderPath) {

		File file = new File(folderPath);
		if (!file.exists()) {
			return file.mkdirs();
		}
		return true;
	}
}
