package com.tony.stepcounter.provider;

import com.tony.stepcounter.provider.DBHelper.Tables;
import com.tony.stepcounter.provider.StepDb.ContentUri;
import com.tony.stepcounter.provider.StepDb.StepDaysColumn;
import com.tony.stepcounter.provider.StepDb.StepMonthsColumn;
import com.tony.stepcounter.provider.StepDb.StepWeeksColumn;
import com.tony.stepcounter.utils.DataBaseUtil;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MonthsDbHelper {
	private static MonthsDbHelper monthsHelper;
	private DBHelper dbHelper;
	private Context mContext;

	public MonthsDbHelper(DBHelper helper, Context context) {
		this.dbHelper = helper;
		this.mContext = context;
	}

	public static MonthsDbHelper getHelper(DBHelper helper, Context context) {
		if (null == monthsHelper) {
			monthsHelper = new MonthsDbHelper(helper, context);
		}

		return monthsHelper;
	}

	/**
	 * 更新days表单时，需要同时更新weeks表
	 * @param values
	 */
	public Uri insertMonthsStep(ContentValues values) {

		Uri retUri = null;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(Tables.TABLE_STEP_MONTHS);

		String month = values.getAsString(StepDaysColumn.DATE).substring(0, 7);

		String selection = DataBaseUtil.SQL_TRUE + DataBaseUtil.SQL_SYMBOL_AND
				+ StepMonthsColumn.DATE + DataBaseUtil.SQL_SYMBOL_EQLALS + DataBaseUtil.SQL_SYMBOL_QUOTE + month + DataBaseUtil.SQL_SYMBOL_QUOTE;

		Cursor cursor = null;
		try {
			cursor = qb.query(db, null, selection, null, null, null, StepWeeksColumn._ID + " DESC LIMIT 1");
			ContentValues tmpValues = new ContentValues();
			int step = (null != values.getAsInteger(StepDaysColumn.STEP)) ? values.getAsInteger(StepDaysColumn.STEP) : 0;
			if (null == cursor || !cursor.moveToFirst()) {
				// insert new
				tmpValues.put(StepMonthsColumn.MONTHS_TOTAL_STEP, step);
				tmpValues.put(StepMonthsColumn.DATE, month);
				long rowId = db.insert(Tables.TABLE_STEP_MONTHS, null, tmpValues);
				retUri = (rowId > 0) ? ContentUris.withAppendedId(ContentUri.STEP_COUNTER_WEEKS, rowId) : null;
			} else {
				int indexId = cursor.getColumnIndex(StepMonthsColumn._ID);
				int indexStep = cursor.getColumnIndex(StepMonthsColumn.MONTHS_TOTAL_STEP);
				int month_id = cursor.getInt(indexId);
				tmpValues.put(StepMonthsColumn.MONTHS_TOTAL_STEP, cursor.getInt(indexStep) + step);
				long count = db.update(Tables.TABLE_STEP_MONTHS, tmpValues, StepMonthsColumn._ID + DataBaseUtil.SQL_SYMBOL_EQLALS + month_id,
						null);
				retUri = (count > 0) ? ContentUris.withAppendedId(ContentUri.STEP_COUNTER_MONTHS, month_id) : null;
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
	 * 当新一月数据产生时，生成months表单数据，只更新date字段
	 * @param values
	 */
	public Uri insertNewRecord(ContentValues values) {

		Uri retUri = null;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(Tables.TABLE_STEP_MONTHS);

		String month = values.getAsString(StepDaysColumn.DATE).substring(0, 7);

		String selection = DataBaseUtil.SQL_TRUE + DataBaseUtil.SQL_SYMBOL_AND
				+ StepMonthsColumn.DATE + DataBaseUtil.SQL_SYMBOL_EQLALS + DataBaseUtil.SQL_SYMBOL_QUOTE + month + DataBaseUtil.SQL_SYMBOL_QUOTE;

		Cursor cursor = null;
		try {
			cursor = qb.query(db, null, selection, null, null, null, StepWeeksColumn._ID + " DESC LIMIT 1");
			ContentValues tmpValues = new ContentValues();
			if (null == cursor || !cursor.moveToFirst()) {
				// insert new
				tmpValues.put(StepMonthsColumn.MONTHS_TOTAL_STEP, 0);
				tmpValues.put(StepMonthsColumn.DATE, month);
				long rowId = db.insert(Tables.TABLE_STEP_MONTHS, null, tmpValues);
				retUri = (rowId > 0) ? ContentUris.withAppendedId(ContentUri.STEP_COUNTER_MONTHS, rowId) : null;
			} else {
				int indexId = cursor.getColumnIndex(StepMonthsColumn._ID);
				int month_id = cursor.getInt(indexId);
				retUri = ContentUris.withAppendedId(ContentUri.STEP_COUNTER_MONTHS, month_id);
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