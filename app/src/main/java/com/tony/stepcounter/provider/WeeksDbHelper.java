package com.tony.stepcounter.provider;

import com.tony.stepcounter.provider.DBHelper.Tables;
import com.tony.stepcounter.provider.StepDb.ContentUri;
import com.tony.stepcounter.provider.StepDb.StepDaysColumn;
import com.tony.stepcounter.provider.StepDb.StepWeeksColumn;
import com.tony.stepcounter.utils.DataBaseUtil;
import com.tony.stepcounter.utils.DateUtil;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class WeeksDbHelper {
	private static WeeksDbHelper weeksHelper;
	private DBHelper dbHelper;
	private Context mContext;

	public WeeksDbHelper(DBHelper helper, Context context) {
		this.dbHelper = helper;
		this.mContext = context;
	}

	public static WeeksDbHelper getHelper(DBHelper helper, Context context) {
		if (null == weeksHelper) {
			weeksHelper = new WeeksDbHelper(helper, context);
		}

		return weeksHelper;
	}

	/**
	 * 更新days表单时，需要同时更新weeks表
	 * @param values
	 */
	public Uri insertWeeksStep(ContentValues values) {

		Uri retUri = null;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(Tables.TABLE_STEP_WEEKS);

		String weekStart = DateUtil.getWeekStartByDate(values.getAsString(StepDaysColumn.DATE));
		String weekEnd = DateUtil.getWeekEndByDate(values.getAsString(StepDaysColumn.DATE));

		String selection = DataBaseUtil.SQL_TRUE + DataBaseUtil.SQL_SYMBOL_AND
				+ StepWeeksColumn.DATE_START + DataBaseUtil.SQL_SYMBOL_EQLALS + DataBaseUtil.SQL_SYMBOL_QUOTE + weekStart + DataBaseUtil.SQL_SYMBOL_QUOTE
				+ DataBaseUtil.SQL_SYMBOL_AND
				+ StepWeeksColumn.DATE_END + DataBaseUtil.SQL_SYMBOL_EQLALS + DataBaseUtil.SQL_SYMBOL_QUOTE + weekEnd + DataBaseUtil.SQL_SYMBOL_QUOTE;

		Cursor cursor = null;
		try {
			cursor = qb.query(db, null, selection, null, null, null, StepWeeksColumn._ID + " DESC LIMIT 1");
			ContentValues tmpValues = new ContentValues();
			int step = (null != values.getAsInteger(StepDaysColumn.STEP)) ? values.getAsInteger(StepDaysColumn.STEP) : 0;
			if (null == cursor || !cursor.moveToFirst()) {
				// insert new
				tmpValues.put(StepWeeksColumn.WEEKS_TOTAL_STEP, step);
				tmpValues.put(StepWeeksColumn.DATE_START, weekStart);
				tmpValues.put(StepWeeksColumn.DATE_END, weekEnd);
				long rowId = db.insert(Tables.TABLE_STEP_WEEKS, null, tmpValues);
				retUri = (rowId > 0) ? ContentUris.withAppendedId(ContentUri.STEP_COUNTER_WEEKS, rowId) : null;
			} else {
				int indexId = cursor.getColumnIndex(StepWeeksColumn._ID);
				int indexStep = cursor.getColumnIndex(StepWeeksColumn.WEEKS_TOTAL_STEP);
				int week_id = cursor.getInt(indexId);
				tmpValues.put(StepWeeksColumn.WEEKS_TOTAL_STEP, cursor.getInt(indexStep) + step);
				long count = db.update(Tables.TABLE_STEP_WEEKS, tmpValues, StepWeeksColumn._ID + DataBaseUtil.SQL_SYMBOL_EQLALS + week_id,
						null);
				retUri = (count > 0) ? ContentUris.withAppendedId(ContentUri.STEP_COUNTER_WEEKS, week_id) : null;
			}
		} catch (Exception e) {
		} finally {
			if (null != cursor) {
				cursor.close();
			}
		}

		if (null != retUri) {
			mContext.getContentResolver().notifyChange(retUri, null);
		}
		return retUri;
	}

	/**
	 * 当新一周数据产生时，生成weeks表单数据，只更新date_start、date_end字段
	 * @param values
	 */
	public Uri insertNewRecord(ContentValues values) {

		Uri retUri = null;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(Tables.TABLE_STEP_WEEKS);

		String weekStart = DateUtil.getWeekStartByDate(values.getAsString(StepDaysColumn.DATE));
		String weekEnd = DateUtil.getWeekEndByDate(values.getAsString(StepDaysColumn.DATE));

		String selection = DataBaseUtil.SQL_TRUE + DataBaseUtil.SQL_SYMBOL_AND
				+ StepWeeksColumn.DATE_START + DataBaseUtil.SQL_SYMBOL_EQLALS + DataBaseUtil.SQL_SYMBOL_QUOTE + weekStart + DataBaseUtil.SQL_SYMBOL_QUOTE
				+ DataBaseUtil.SQL_SYMBOL_AND
				+ StepWeeksColumn.DATE_END + DataBaseUtil.SQL_SYMBOL_EQLALS + DataBaseUtil.SQL_SYMBOL_QUOTE + weekEnd + DataBaseUtil.SQL_SYMBOL_QUOTE;

		Cursor cursor = null;
		try {
			cursor = qb.query(db, null, selection, null, null, null, StepWeeksColumn._ID + " DESC LIMIT 1");
			ContentValues tmpValues = new ContentValues();
			if (null == cursor || !cursor.moveToFirst()) {
				tmpValues.put(StepWeeksColumn.WEEKS_TOTAL_STEP, 0);
				tmpValues.put(StepWeeksColumn.DATE_START, weekStart);
				tmpValues.put(StepWeeksColumn.DATE_END, weekEnd);
				long rowId = db.insert(Tables.TABLE_STEP_WEEKS, null, tmpValues);
				retUri = (rowId > 0) ? ContentUris.withAppendedId(ContentUri.STEP_COUNTER_WEEKS, rowId) : null;
			} else {
				int indexId = cursor.getColumnIndex(StepWeeksColumn._ID);
				int week_id = cursor.getInt(indexId);
				retUri = ContentUris.withAppendedId(ContentUri.STEP_COUNTER_WEEKS, week_id);
			}
		} catch (Exception e) {
		} finally {
			if (null != cursor) {
				cursor.close();
			}
		}

		return retUri;
	}
}