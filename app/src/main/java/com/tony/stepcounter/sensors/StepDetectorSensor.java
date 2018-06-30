package com.tony.stepcounter.sensors;

import com.tony.stepcounter.base.StepBaseSensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

public class StepDetectorSensor extends StepBaseSensor {
	public StepDetectorSensor() {
		SensorManager manager = (SensorManager)getSensorContext().getSystemService(Context.SENSOR_SERVICE);
		if (null == manager) {
			return;
		}
		
		mSensorType = Sensor.TYPE_STEP_DETECTOR;
		mSensor = manager.getDefaultSensor(mSensorType);
	}
}
