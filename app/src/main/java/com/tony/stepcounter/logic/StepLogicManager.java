package com.tony.stepcounter.logic;

import android.content.Context;

public class StepLogicManager {

	Context context;
	private StepLogicPolicy stepLogicPolicy;
	static StepLogicManager instance = null;
	public static StepLogicManager getInstance(Context context) {
		if (null == instance) {
			instance = new StepLogicManager(context.getApplicationContext());
		}
		return instance;
	}
	
	private StepLogicManager(Context context) {
		this.context = context;
	}
	
	public StepLogicPolicy getStepLogicPolicy() {
		if (null == stepLogicPolicy) {
			stepLogicPolicy = new StepLogicPolicy();
		}

		return stepLogicPolicy;
	}
}
