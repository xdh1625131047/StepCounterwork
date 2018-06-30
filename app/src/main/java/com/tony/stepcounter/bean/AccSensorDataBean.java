package com.tony.stepcounter.bean;

import java.util.concurrent.TimeUnit;

import android.hardware.SensorEvent;

/**
 * @author lantian
 * 加速传感器数据对象,用于计算波峰、波谷等信息
 */
public class AccSensorDataBean implements Cloneable{

	/** 当前时间 */
	private long time=0;

	/** 传感器的平均数值，x、y、z值2次幂开方，降低噪声差异 */
	private float averageGravity=0;

	public float x;
	public float y;
	public float z;

	public boolean isPeak = false;
	public boolean isTrough = false;
	public boolean isStepCount = false;

	/** 持续上升的次数，>0:处于持续上升过程，0:代表下降 */
	private int continueUpCount = 0;

	public AccSensorDataBean(SensorEvent event, long time) {
		this.x = event.values[0];
		this.y = event.values[1];
		this.z = event.values[2];
		//x、y、z值2次幂开方，降低噪声差异
		this.setAverageGravity(averageXYZ(x, y, z));
		this.setTime(time);
	}

	/**
	 * @param x x轴
	 * @param y y轴
	 * @param z z轴
	 * @return 取平方和开根
	 */
	private float averageXYZ(float x, float y, float z) {
		return (float)Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
	}

	/**
	 * @return 获取当前sensor数据产生的时间
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time 系统时间
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return 获取当前sensor数据，x、y、z平均值
	 */
	public float getAverageGravity() {
		return averageGravity;
	}

	/**
	 * @param averageGravity sensor数据，x、y、z平均值
	 */
	public void setAverageGravity(float averageGravity) {
		this.averageGravity = averageGravity;
	}

	public int getContinueUpCount() {
		return continueUpCount;
	}

	public void setContinueUpCount(int continueUpCount) {
		this.continueUpCount = continueUpCount;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}