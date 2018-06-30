package com.tony.stepcounter.logic;

import com.tony.stepcounter.application.StepCounterApplication;
import com.tony.stepcounter.bean.StepInfoBean;
import com.tony.stepcounter.dao.StepDbDao;
import com.tony.stepcounter.utils.StringUtils;

import android.content.Context;
import android.net.Uri;

public class StepLogicPolicy implements StepLogicInterface {
	public static final String TAG = "StepLogicPolicy";
	public StepLogicPolicy() {
	}
	
	@Override
	public StepInfoBean getStepInfoById() {
		return null;
	}

	@Override
	public int getLatestTarget(Context context) {
		StepDbDao daoObject = new StepDbDao(context);
		StepInfoBean bean = daoObject.getLatestStepInfoBean(context);
		return (null != bean) ? bean.getTarget() : 0;
	}

	@Override
	public int getCurrentSteps(Context context) {
		StepDbDao dao = new StepDbDao(context);
		return dao.getTodaySteps();
	}

	@Override
	public StepInfoBean getLatestStepInfoBean(Context context) {
		StepDbDao daoObject = new StepDbDao(context);
		return daoObject.getLatestStepInfoBean(context);
	}

	@Override
	public boolean addStepRecord(Context context, StepInfoBean bean) {
		
		//�������ݲ���Ϊ��
		if (null == bean || StringUtils.isEmpty(bean.getDate())) {
			return false;
		}
		
		StepDbDao daoObject = new StepDbDao(context);

		StepInfoBean latestBean = daoObject.getLatestStepInfoBean(context);
		
		if (null != latestBean && latestBean.getDate().equals(bean.getDate())) {
			int count = daoObject.updateRecord(context, latestBean.getId(), bean);
			StepCounterApplication.getLogUtil().d(TAG, "addStepRecord:same day exist,update " + latestBean.getId() + " return " + count);
			return 0 < count;
		} else {
			Uri uri = daoObject.insertRecord(context, bean);
			StepCounterApplication.getLogUtil().d(TAG, "addStepRecord:same day not exist,insert return uri:" + uri);
			return null != uri;
		}
	}

	@Override
	public int getInsistDays(Context context) {
		StepDbDao daoObject = new StepDbDao(context);
		return daoObject.getInsistDays(context);
	}

	@Override
	public int getAchievedDays(Context context) {
		StepDbDao daoObject = new StepDbDao(context);
		return daoObject.getAchievedDays(context);
	}
}
