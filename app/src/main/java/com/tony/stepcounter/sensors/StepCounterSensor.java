package com.tony.stepcounter.sensors;

import com.tony.stepcounter.base.StepBaseSensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

public class StepCounterSensor extends StepBaseSensor {

	public StepCounterSensor() {
		
		SensorManager manager = (SensorManager)getSensorContext().getSystemService(Context.SENSOR_SERVICE);
		if (null == manager) {
			return;
		}
		
		mSensorType = Sensor.TYPE_STEP_COUNTER;
		mSensor = manager.getDefaultSensor(mSensorType);
	}

}
