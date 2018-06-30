package com.tony.stepcounter.sensors;

import java.nio.charset.Charset;
import java.util.ArrayList;

import com.tony.stepcounter.application.StepCounterApplication;
import com.tony.stepcounter.base.StepBaseSensor;
import com.tony.stepcounter.bean.AccSensorDataBean;
import com.tony.stepcounter.service.StepCounterService;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class StepAcceleroMeterSensor extends StepBaseSensor{
	private static final String TAG="StepAcceleroMeterSensor";

	/** 前一个Acc sensor信息 */
	private AccSensorDataBean previousBean;

	/** 当前Acc sensor信息 */
//	private AccSensorDataBean currentBean;

	/** 前一个波峰数据 */
	private AccSensorDataBean previousPeakBean;

	/** 前一个波谷数据 */
	private AccSensorDataBean previousTroughBean;

	/** 波峰、波谷差值的动态阈值，当前波长必须大于此值才能算是波峰，否则是噪声，每次波峰时根据实时数据动态生成，初始值为2.0 */
	private float dynamicThreshold=(float)2.0;

	/** 每5个波峰计算一次动态阈值 */
	private final int valueNum=5;
	/** 波峰波谷差值数组，用于计算dynamicThreshold */
	private float[] subPeakTroughValues =new float[valueNum];
	/** subPeakTroughValues的有效数据长度 */
	private int valuesCount=0;

	/** 经验数据：波峰最小值，1.2g */
	private float minWaveValue=11f;
	/** 经验数据：波峰最大值 ，2g*/
	private float maxWaveValue=19.6f;

	/** dynamicThreshold的生成条件，当相邻两次波峰、波谷差大于此值，则重新计算dynamicThreshold */
	private float waveThreshold=(float)1.7;

	public class XYZ {
		public float x;
		public float y;
		public float z;
		public float average;
		public XYZ(float x, float y, float z, float average) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.average = average;
		}
	}

	/** debug模式时，保存最近200个数据，用于分析、优化计步器*/
	public static ArrayList<AccSensorDataBean> arrayList = new ArrayList<AccSensorDataBean>();

	public StepAcceleroMeterSensor() {

		SensorManager manager = (SensorManager)getSensorContext().getSystemService(Context.SENSOR_SERVICE);
		if (null == manager) {
			return;
		}

		mSensorType = Sensor.TYPE_ACCELEROMETER;
		mSensor = manager.getDefaultSensor(mSensorType);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		super.onSensorChanged(event);
		Sensor sensor=event.sensor;
//		Log.d(TAG, "onSensorChanged:sensorType=" + sensor.getType());
		if(sensor.getType()==sensor.TYPE_ACCELEROMETER){
//			Log.d(TAG, "      [x,y,z]=[" + event.values[0] + "," + event.values[1] + "," +event.values[0] + "]");
			AccSensorDataBean currentBean = new AccSensorDataBean(event, System.currentTimeMillis());
			caculateStep(currentBean);
		}
	}

	private float averageXYZ(float x, float y, float z) {
		return (float)Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
	}

	/**
	 * @param previousBean 前一次sensor info
	 * @param currentBean 当前sensor info
	 * @return 前一次sensor数据是否属于波峰
	 */
	private boolean isPeakApears(AccSensorDataBean previousBean, AccSensorDataBean currentBean) {

		int previousCount = previousBean.getContinueUpCount();
		int currentCount = currentBean.getContinueUpCount();

		/** 1：前一次sensor数据处于上升趋势，并且当前sensor数据处于下降趋势
		 *  2：前一次gravity值必须在波峰经验值区间范围
		 *  3：计数器连续上升超过2次，或者是与上次波谷差值达到阈值，注：当曲线交替上升时，计数器没有增加，所以需要加入阈值判定
		 *  4：前一次波峰时间一定小于前一次波谷时间，避免连续出现波峰时进行计数。注：如果不存在波峰、波谷数据，即最初的几次数据，还是以条件1的计数器计算为主
		 *  5：如果出现连续波峰数据，则取较大者，方便统计波峰值*/
////    	if (2 <= previousBean.getContinueUpCount() && 0 == currentBean.getContinueUpCount()
//    	if ((2 <= previousCount || (null != previousTroughBean && previousBean.getAverageGravity() - previousTroughBean.getAverageGravity() >= dynamicThreshold))
//    			&& 0 > (currentCount - previousCount)
//    			&& (minWaveValue <= previousBean.getAverageGravity() && maxWaveValue >= previousBean.getAverageGravity())) {
//			return true;
//		}

		if (0 > (currentCount - previousCount)
				&& (minWaveValue <= previousBean.getAverageGravity() && maxWaveValue >= previousBean.getAverageGravity())) {

			// 最初几次数据主要还是以计数器为主
			if (null == previousPeakBean || null == previousTroughBean) {
				if (2 <= previousCount) {
					return true;
				}
			} else {
				if ((2 <= previousCount || previousBean.getAverageGravity() - previousTroughBean.getAverageGravity() >= dynamicThreshold)) {

					if (previousPeakBean.getTime() < previousTroughBean.getTime()) {
						return true;
					} else {
						// TODO 视测试结果待定是否需要统计这一次波峰
						if (previousBean.getAverageGravity() > previousPeakBean.getAverageGravity()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * @param previousBean 前一次sensor info
	 * @param currentBean 当前sensor info
	 * * @return 前一次sensor数据是否属于波谷
	 */
	private boolean isTroughApears(AccSensorDataBean previousBean, AccSensorDataBean currentBean) {

		int previousCount = previousBean.getContinueUpCount();
		int currentCount = currentBean.getContinueUpCount();

//    	/** 1:前一次sensor数据处于下降趋势，并且当前sensor数据处于上升趋势
//    	 *  2:前一次波峰时间一定大于前一次波谷时间，避免连续出现波谷时进行计数。注：如果不存在波峰、波谷数据，则按条件1计算*/
////    	if (0 == previousBean.getContinueUpCount() && 0 < currentBean.getContinueUpCount()) {
//    	if ( 0 < (currentCount - previousCount)
//    			&& (null == previousPeakBean || null == previousTroughBean || (previousPeakBean.getTime() > previousTroughBean.getTime() ))) {
//			return true;
//		}

		/** 1:前一次sensor数据处于下降趋势，并且当前sensor数据处于上升趋势
		 *  2:前一次波峰gravity - previousBean gravity必须大于动态阈值，避免高频振动导致的连续波峰、波谷
		 *  3:前一次波峰时间一定大于前一次波谷时间，避免连续出现波谷时进行计数。注：如果不存在波峰、波谷数据，则按条件1计算
		 *  4:如果出现连续波谷数据，则取较小者,用于下次波峰计算，避免瞬时波谷数据不正确导致下次波峰计算出现偏差*/
		if ( 0 < currentCount - previousCount) {
			if (null == previousPeakBean || null == previousTroughBean) {

				// 只有计数器为0的点才是波谷，避免刚开始测试时，从第一个波谷开始连续上升的点都算作波谷，这样每个波谷计数器都清零会导致永远无法找到波峰
				if (0 == previousBean.getContinueUpCount()) {
					return true;
				}

			} else {
				if(previousPeakBean.getAverageGravity() - previousBean.getAverageGravity() >= dynamicThreshold ) {
					if (previousPeakBean.getTime() > previousTroughBean.getTime()) {
						return true;
					} else {

						// TODO 视测试结果待定是否需要统计这一次波谷
						if (previousBean.getAverageGravity() < previousTroughBean.getAverageGravity()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * 监测新的步数
	 *
	 * 1.传入sersor中的数据
	 * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定位1步
	 * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
	 * @param bean 当前sensor数据对象
	 */
	private void caculateStep(AccSensorDataBean currentBean) {
		if(null != previousBean){
			StepCounterApplication.getLogUtil().d(TAG, "      current[x,y,z]=[" + currentBean.x + "," + currentBean.y + "," + currentBean.z + "],average=" + currentBean.getAverageGravity());

			/**1：sensor gravity递增，说明处于上升趋势, +1;
			 * 2：sensor gravity降低，说明处于下降趋势, -1;
			 * 3：sensor gravity前后一致，保持上一次值，避免连续出现相同值时出现两个波峰或者两个波谷的情况*/
			int count = previousBean.getContinueUpCount();
			count = (currentBean.getAverageGravity() > previousBean.getAverageGravity()) ? count + 1 : count - 1;
			currentBean.setContinueUpCount(count);

			if (isPeakApears(previousBean, currentBean)) {
				// 说明previousBean是波峰，两次波峰之间时间在200ms到2秒之间， 同时波峰、波谷差值大于动态阈值
				if (null != previousPeakBean && null != previousTroughBean
						&& previousBean.getTime() - previousPeakBean.getTime() >= 200
						&& previousBean.getTime() - previousPeakBean.getTime() <= 2000
						&& previousBean.getAverageGravity() - previousTroughBean.getAverageGravity() >= dynamicThreshold) {
					// TODO count++
					previousBean.isStepCount = true;
					mStepChangeListener.onChange();
				}

				// 当前波峰时间-前一次波峰时间大于200，保证当前波峰数据不是噪声，噪声数据不作为阈值计算凭据
				if (null != previousPeakBean && null != previousTroughBean
						&& previousBean.getTime() - previousPeakBean.getTime() >= 200
						&& previousBean.getAverageGravity() - previousTroughBean.getAverageGravity() >= waveThreshold) {
					dynamicThreshold = dynamicCalculateThreshold(currentBean.getAverageGravity() - previousTroughBean.getAverageGravity());
				}

				// 保存当前前一次波峰数据
				previousBean.isPeak = true;
				previousPeakBean = previousBean;
				StepCounterApplication.getLogUtil().d(TAG, "apears Peak---------------");
				StepCounterApplication.getLogUtil().d(TAG, "      previousPeakBean[x,y,z]=[" + previousPeakBean.x + "," + previousPeakBean.y + "," + previousPeakBean.z + "], av=" + averageXYZ(previousPeakBean.x, previousPeakBean.y, previousPeakBean.z) + ",gravity=" + previousPeakBean.getAverageGravity());
			} else if (isTroughApears(previousBean, currentBean)) {

				// 波谷，保存当前前一次数据为波谷数据
				previousBean.isTrough = true;
				// 如果确定当前是波谷，则计数器清零，避免当多次自减操作之后值过小，导致无法满足isPeakApears()接口中大于等2的条件限制
				previousBean.setContinueUpCount(0);
				// 同时当前数据计数器变成0+1，因为紧接着previousBean会被currentBean覆写
				currentBean.setContinueUpCount(previousBean.getContinueUpCount() + 1);

				previousTroughBean = previousBean;
				StepCounterApplication.getLogUtil().d(TAG, "apears Trough---------------");
				StepCounterApplication.getLogUtil().d(TAG, "      previousTroughBean[x,y,z]=[" + previousTroughBean.x + "," + previousTroughBean.y + "," + previousTroughBean.z + "], av=" + averageXYZ(previousTroughBean.x, previousTroughBean.y, previousTroughBean.z) + ",gravity=" + previousTroughBean.getAverageGravity());

			} else {
				//其它状态
			}

			if (StepCounterApplication.isDebugMode() && StepCounterService.getTotalSteps() > 10 && isSensorEnabled() && arrayList.size() < 200) {
				arrayList.add(previousBean);
			}
		}

		// 保存当前sensor info
		previousBean = currentBean;
	}

	/**
	 * 功能：获取最近5次的波峰、波谷差值平均值，然后根据经验数据返回阈值
	 * 1.前5次不做计算，直接返回旧阈值
	 * 2.averageFloat计算新阈值，计算完成后丢弃第一个数据，等待下一次计算。
	 * @param waveSubValue 波峰、波谷差值
	 * @return 返回新计算的动态阈值
	 */
	public float dynamicCalculateThreshold(float waveSubValue){
		float tempThreshold = dynamicThreshold;
		if(valuesCount < valueNum){
			subPeakTroughValues[valuesCount] = waveSubValue;
			valuesCount++;
		}else{
			//此时tempCount=valueNum=5
			tempThreshold = averageFloat(subPeakTroughValues, valueNum);

			//先入先出原则，舍弃数组中第一个值，并将新值保留在最后
			for(int i = 1; i < valueNum; i++){
				subPeakTroughValues[i-1] = subPeakTroughValues[i];
			}
			subPeakTroughValues[valueNum-1] = waveSubValue;
		}
		return tempThreshold;
	}

	/**
	 * 梯度化阈值
	 * 1.计算数组的均值
	 * 2.通过均值将阈值梯度化在一个范围里
	 *
	 * 这些数据是通过大量的统计得到的
	 * @param value 波峰、波谷差值数组
	 * @param n 数据量
	 * @return 返回当前最优的波峰、波谷差值
	 */
	public float averageFloat(float value[], int n){
		float ave = 0;
		for(int i = 0; i < n; i++){
			ave += value[i];
		}
		ave = ave/n;  //计算数组均值
		if(ave >= 8){
			StepCounterApplication.getLogUtil().v(TAG, new String((Float.toString(ave) + ":超过8").getBytes(), Charset.forName("utf-8")));
			ave = (float)4.3;
		} else if (ave >= 7 && ave < 8){
			StepCounterApplication.getLogUtil().v(TAG, "7-8");
			ave = (float)3.3;
		} else if (ave >= 4 && ave < 7){
			StepCounterApplication.getLogUtil().v(TAG, "4-7");
			ave = (float)2.3;
		} else if (ave >= 3 && ave < 4){
			StepCounterApplication.getLogUtil().v(TAG, "3-4");
			ave = (float)2.0;
		} else {
			StepCounterApplication.getLogUtil().v(TAG, "else (ave<3)");
			ave = (float)1.7;
		}
		return ave;
	}
}