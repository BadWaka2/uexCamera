package org.zywx.wbpalmstar.plugin.uexcamera.photoprocess;

import org.zywx.wbpalmstar.plugin.uexcamera.utils.BitmapUtil;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 图片Bean类 包含图片基础信息
 * <p/>
 * Parcelable序列化之后可以用Intent传递，进行IPC通信
 * <p/>
 * Created by waka on 2016/6/21.
 */
public class PhotoBean implements Parcelable {

	/**
	 * 图片源路径
	 */
	private String sourcePath;

	/**
	 * 图片输出路径
	 */
	private String outputPath;

	/**
	 * 压缩质量
	 */
	private int quality;

	/**
	 * 期望宽
	 */
	private int reqWidth;

	/**
	 * 期望高
	 */
	private int reqHeight;

	/**
	 * 压缩比
	 */
	private int inSampleSize;

	/**
	 * 旋转角度
	 */
	private int rotation;

	/**
	 * 翻转模式
	 */
	private int convertMode;

	/**
	 * 构造方法 初始化为默认值，进行容错处理
	 */
	public PhotoBean() {
		sourcePath = "";
		outputPath = "";
		quality = 100;// 默认质量100
		reqWidth = -1;// 默认没有期望宽度
		reqHeight = -1;// 默认没有期望高度
		inSampleSize = 1;// 默认压缩比为1
		rotation = 0;// 默认旋转角为0
		convertMode = BitmapUtil.BITMAP_CONVERT_MODE_NO;// 默认翻转模式为不翻转
	}

	/**
	 * 继承Parcelable后自动生成的构造方法
	 *
	 * @param in
	 */
	protected PhotoBean(Parcel in) {
		sourcePath = in.readString();
		outputPath = in.readString();
		quality = in.readInt();
		reqWidth = in.readInt();
		reqHeight = in.readInt();
		inSampleSize = in.readInt();
		rotation = in.readInt();
		convertMode = in.readInt();
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getReqWidth() {
		return reqWidth;
	}

	public void setReqWidth(int reqWidth) {
		this.reqWidth = reqWidth;
	}

	public int getReqHeight() {
		return reqHeight;
	}

	public void setReqHeight(int reqHeight) {
		this.reqHeight = reqHeight;
	}

	public int getInSampleSize() {
		return inSampleSize;
	}

	public void setInSampleSize(int inSampleSize) {
		this.inSampleSize = inSampleSize;
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public int getConvertMode() {
		return convertMode;
	}

	public void setConvertMode(int convertMode) {
		this.convertMode = convertMode;
	}

	/**
	 * 内容描述接口
	 *
	 * @return
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * 将你的对象序列化为一个Parcel对象， 即：将类的数据写入外部提供的Parcel中， 打包需要传递的数据到Parcel容器保存，
	 * 以便从Parcel容器获取数据
	 *
	 * @param parcel
	 * @param i
	 */
	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(sourcePath);
		parcel.writeString(outputPath);
		parcel.writeInt(quality);
		parcel.writeInt(reqWidth);
		parcel.writeInt(reqHeight);
		parcel.writeInt(inSampleSize);
		parcel.writeInt(rotation);
		parcel.writeInt(convertMode);
	}

	/**
	 * 实例化静态内部对象CREATOR实现接口Parcelable.Creator
	 */
	public static final Creator<PhotoBean> CREATOR = new Creator<PhotoBean>() {

		@Override
		public PhotoBean createFromParcel(Parcel in) {
			return new PhotoBean(in);
		}

		/**
		 * 供反序列化本类数组时调用的
		 *
		 * @param size
		 * @return
		 */
		@Override
		public PhotoBean[] newArray(int size) {
			return new PhotoBean[size];
		}
	};

}
