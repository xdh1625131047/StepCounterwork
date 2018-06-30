package com.tony.stepcounter.bean;

public class StepInfoBean {
	private int id;
	private int previousStep;
	private int step;
	private String date;
	private int target;
	
	public StepInfoBean() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPreviousStep() {
		return previousStep;
	}

	public void setPreviousStep(int previousStep) {
		this.previousStep = previousStep;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}
}
