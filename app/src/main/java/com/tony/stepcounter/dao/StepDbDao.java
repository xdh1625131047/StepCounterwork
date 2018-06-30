package com.tony.stepcounter.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.tony.stepcounter.provider.StepDb.ContentUri;
import com.tony.stepcounter.provider.StepDb.StepDaysColumn;
import com.tony.stepcounter.bean.StepInfoBean;
import com.tony.stepcounter.utils.DataBaseUtil;
import com.tony.stepcounter.utils.DateUtil;
import com.tony.stepcounter.utils.StringUtils;

import java.util.Locale;

public class StepDbDao {

	public static final String TAG = "StepDbDao";
	Context context;
	StepDbDao instance = null;

	public StepDbDao(Context context) {
		this.context = context;
	}

	/**
	 * @return 返回最近一次计步信息
	 */
	public StepInfoBean getLatestStepInfoBean(Context context) {
		StepInfoBean bean = null;

		ContentResolver resolver = context.getContentResolver();
		Cursor c = null;

		try {
			c = resolver.query(ContentUri.STEP_COUNTER_DAYS, null, null, null, StepDaysColumn._ID + " DESC LIMIT 1");

			if (null == c || !c.moveToFirst()) {
				return bean;
			} else {
				int indexId = c.getColumnIndex(StepDaysColumn._ID);
				int indexPreviousStep = c.getColumnIndex(StepDaysColumn.PREVIOUS_STEP);
				int indexStep = c.getColumnIndex(StepDaysColumn.STEP);
				int indexDate = c.getColumnIndex(StepDaysColumn.DATE);
				int indexTarget = c.getColumnIndex(StepDaysColumn.TARGET);

				bean = new StepInfoBean();
				bean.setId(c.getInt(indexId));
				bean.setPreviousStep(c.getInt(indexPreviousStep));
				bean.setStep(c.getInt(indexStep));
				bean.setDate(c.getString(indexDate));
				bean.setTarget(c.getInt(indexTarget));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != c) {
				c.close();
			}
		}

		return bean;
	}

	/**
	 * @return 获取今日Step数
	 */
	public int getTodaySteps() {
		int ret = 0;
		ContentResolver resolver = context.getContentResolver();
		Cursor c = null;
		String today = DateUtil.getTodayDate();
		String selection = DataBaseUtil.SQL_TRUE + DataBaseUtil.SQL_SYMBOL_AND
				+ StepDaysColumn.DATE + DataBaseUtil.SQL_SYMBOL_EQLALS + DataBaseUtil.SQL_SYMBOL_QUOTE + today + DataBaseUtil.SQL_SYMBOL_QUOTE;

		try {
			//只取最后一天的记录，如果用户频繁调整时间，则会重新生成记录，所有业务流程都遵循此条件，若后续需要修改需要统一修改
			c = resolver.query(ContentUri.STEP_COUNTER_DAYS, null, selection, null, StepDaysColumn._ID + " DESC LIMIT 1");

			if (null == c || !c.moveToFirst()) {
				return 0;
			} else {
				int index = c.getColumnIndex(StepDaysColumn.STEP);
				ret = c.getInt(index);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != c) {
				c.close();
			}
		}

		return ret;
	}

	public Uri insertRecord(Context context, StepInfoBean bean) {
		ContentValues values = new ContentValues();
		if (0 != bean.getPreviousStep())
			values.put(StepDaysColumn.PREVIOUS_STEP, bean.getPreviousStep());
		if (0 != bean.getStep())
			values.put(StepDaysColumn.STEP, bean.getStep());
		if (0 != bean.getTarget())
			values.put(StepDaysColumn.TARGET, bean.getTarget());
		if (!StringUtils.isEmpty(bean.getDate()))
			values.put(StepDaysColumn.DATE, bean.getDate());

		ContentResolver resolver = context.getContentResolver();
		return resolver.insert(ContentUri.STEP_COUNTER_DAYS, values);
	}

	public int updateRecord(Context context, int id, StepInfoBean bean) {
		ContentValues values = new ContentValues();
		if (0 != bean.getPreviousStep())
			values.put(StepDaysColumn.PREVIOUS_STEP, bean.getPreviousStep());
		if (0 != bean.getStep())
			values.put(StepDaysColumn.STEP, bean.getStep());
		if (0 != bean.getTarget())
			values.put(StepDaysColumn.TARGET, bean.getTarget());
		if (!StringUtils.isEmpty(bean.getDate()))
			values.put(StepDaysColumn.DATE, bean.getDate());

		ContentResolver resolver = context.getContentResolver();
		String selection = String.format(Locale.ENGLISH, "%s IN (%s)", StepDaysColumn._ID, "" + id);
		return resolver.update(ContentUri.STEP_COUNTER_DAYS, values, selection, null);
	}

	public int getInsistDays(Context context) {
		int ret = 0;
		ContentResolver resolver = context.getContentResolver();
		Cursor c = null;

		// 大于3000算作坚持锻炼
		String selection = DataBaseUtil.SQL_TRUE + DataBaseUtil.SQL_SYMBOL_AND
				+ StepDaysColumn.STEP + DataBaseUtil.SQL_SYMBOL_GREATER_OR_EQLALS + "3000";
		try {
			c = resolver.query(ContentUri.STEP_COUNTER_DAYS, new String[] {"COUNT(*) AS COUNT_TABLE"}, selection, null, StepDaysColumn._ID + " DESC");

			if (null != c && c.moveToFirst()) {
				ret = c.getInt(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != c) {
				c.close();
			}
		}

		return ret;
	}

	public int getAchievedDays(Context context) {
		int ret = 0;
		ContentResolver resolver = context.getContentResolver();
		Cursor c = null;

		// step大于等于target，并且target不等于0。注：当没有设定target值时，db中target默认值为0
		String selection = DataBaseUtil.SQL_TRUE + DataBaseUtil.SQL_SYMBOL_AND
				+ StepDaysColumn.STEP + DataBaseUtil.SQL_SYMBOL_GREATER_OR_EQLALS + StepDaysColumn.TARGET
				+ DataBaseUtil.SQL_SYMBOL_AND
				+ StepDaysColumn.TARGET + DataBaseUtil.SQL_SYMBOL_NOT_EQLALS + "0";
		try {
			c = resolver.query(ContentUri.STEP_COUNTER_DAYS, new String[] {"COUNT(*) AS COUNT_TABLE"}, selection, null, StepDaysColumn._ID + " DESC");

			if (null != c && c.moveToFirst()) {
				ret = c.getInt(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != c) {
				c.close();
			}
		}

		return ret;
	}
}