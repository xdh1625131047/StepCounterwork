package com.tony.stepcounter.base;

import com.tony.stepcounter.application.StepCounterApplication;
import com.tony.stepcounter.sensors.StepAcceleroMeterSensor;
import com.tony.stepcounter.sensors.StepCounterSensor;
import com.tony.stepcounter.sensors.StepDetectorSensor;
import com.tony.stepcounter.service.ServiceCallback;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

public class StepBaseSensor implements SensorEventListener {

	private static final String TAG = "StepBaseSensor";
	protected static Sensor mSensor;
	protected static int mSensorType;
	static StepBaseSensor instance = null;
	private static Context mContext;
	private ServiceCallback mCallback;
	private CounterState isStepCounterEnabled = CounterState.DISABLED;
	protected OnStepChangeListener mStepChangeListener;
	
    //������set����
    public void setOnStepChangeListener(OnStepChangeListener onSensorChangeListener){
        this.mStepChangeListener = onSensorChangeListener;
    }
	
    // ����ص�����
    public interface OnStepChangeListener{
        void onChange();
    }
	
	public enum CounterState{
		ENABLED,
		DISABLED
	}

	public static StepBaseSensor getInstance(Context context) {
		
		if (null != instance) {
			return instance;
		}
		
		mContext = context;
		
		SensorManager manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		if (null == manager) {
			return null;
		}
		
		//android4.4�Ժ����ʹ�üƲ�������
		int VERSION_CODES = Build.VERSION.SDK_INT;
        if(VERSION_CODES < 19){
        	instance = new StepAcceleroMeterSensor();
        } else if (null != manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)) {
			instance = new StepCounterSensor();
		} else if (null != manager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)) {
			instance = new StepDetectorSensor();
		} else {
			instance = new StepAcceleroMeterSensor();
		}
		
		return instance;
	}
	
	protected Context getSensorContext() {
		return mContext;
	}
	
	public Sensor getCurrentSensor() {
		return mSensor;
	}
	
	public void setServiceCallback(ServiceCallback cb) {
		this.mCallback = cb;
	}
	
	public boolean isSensorEnabled() {
		return (isStepCounterEnabled == CounterState.ENABLED);
	}
	
	public void setCounterState(CounterState state) {
		isStepCounterEnabled = state;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor=event.sensor;
		StepCounterApplication.getLogUtil().d(TAG, "onSensorChanged:sensorType=" + sensor.getType());
		if(sensor.getType()==sensor.TYPE_ACCELEROMETER){
			StepCounterApplication.getLogUtil().d(TAG, "      [x,y,z]=[" + event.values[0] + "," + event.values[1] + "," +event.values[0] + "]");
//            calc_step(event);
        }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	private void registerListener() {
		// TODO Auto-generated method stub

	}

}
