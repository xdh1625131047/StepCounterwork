package com.tony.stepcounter.logic;

import com.tony.stepcounter.bean.StepInfoBean;

import android.content.Context;

public interface StepLogicInterface {
	/**
	 * @return 返回指定id的计步数据
	 */
	public StepInfoBean getStepInfoById();

	/**
	 * @return 返回最近一次计步数据
	 */
	public StepInfoBean getLatestStepInfoBean(Context context);

	/**
	 * @return 返回最近的计步目标
	 */
	public int getLatestTarget(Context context);

	/**
	 * @return 返回最新一天步数
	 */
	public int getCurrentSteps(Context context);

	/**
	 * @return 更新最近step数据
	 */
	public boolean addStepRecord(Context context, StepInfoBean bean);

	/**
	 * @return 获取坚持锻炼天数
	 */
	public int getInsistDays(Context context);

	/**
	 * @return 获取锻炼达标天数
	 */
	public int getAchievedDays(Context context);
}