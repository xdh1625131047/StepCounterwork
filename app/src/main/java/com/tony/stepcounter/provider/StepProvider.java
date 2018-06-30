package com.tony.stepcounter.provider;

import java.util.ArrayList;
import java.util.HashMap;

import com.tony.stepcounter.provider.DBHelper.Tables;
import com.tony.stepcounter.provider.StepDb.ContentUri;
import com.tony.stepcounter.provider.StepDb.StepDaysColumn;
import com.tony.stepcounter.provider.StepDb.StepMonthsColumn;
import com.tony.stepcounter.provider.StepDb.StepWeeksColumn;
import com.tony.stepcounter.utils.DataBaseUtil;
import com.tony.stepcounter.utils.StringUtils;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class StepProvider extends ContentProvider{

	private DBHelper dbHelper;
	private static final UriMatcher sUriMather;
	private static HashMap<String, String> daysProjectionMap;
	private static HashMap<String, String> weeksProjectionMap;
	private static HashMap<String, String> monthsProjectionMap;

	// step表macher
	private static final int MATCH_TYPE_STEP_DAY = 1;
	private static final int MATCH_TYPE_STEP_DAY_ID = 2;
	private static final int MATCH_TYPE_STEP_WEEK = 3;
	private static final int MATCH_TYPE_STEP_WEEK_ID = 4;
	private static final int MATCH_TYPE_STEP_MONTH = 5;
	private static final int MATCH_TYPE_STEP_MONTH_ID = 6;

	static {
		sUriMather = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMather.addURI(StepDb.AUTHORITY, StepDb.KeyValue.DAYS, MATCH_TYPE_STEP_DAY);
		sUriMather.addURI(StepDb.AUTHORITY, StepDb.KeyValue.DAYS + "/#", MATCH_TYPE_STEP_DAY_ID);
		sUriMather.addURI(StepDb.AUTHORITY, StepDb.KeyValue.WEEKS, MATCH_TYPE_STEP_WEEK);
		sUriMather.addURI(StepDb.AUTHORITY, StepDb.KeyValue.WEEKS + "/#", MATCH_TYPE_STEP_WEEK_ID);
		sUriMather.addURI(StepDb.AUTHORITY, StepDb.KeyValue.MONTHS, MATCH_TYPE_STEP_MONTH);
		sUriMather.addURI(StepDb.AUTHORITY, StepDb.KeyValue.MONTHS + "/#", MATCH_TYPE_STEP_MONTH_ID);

		daysProjectionMap = new HashMap<String, String>();
		daysProjectionMap.put(StepDaysColumn._ID, StepDaysColumn._ID);
		daysProjectionMap.put(StepDaysColumn._COUNT, StepDaysColumn._COUNT);
		daysProjectionMap.put(StepDaysColumn.WEEKS_ID, StepDaysColumn.WEEKS_ID);
		daysProjectionMap.put(StepDaysColumn.MONTHS_ID, StepDaysColumn.MONTHS_ID);
		daysProjectionMap.put(StepDaysColumn.PREVIOUS_STEP, StepDaysColumn.PREVIOUS_STEP);
		daysProjectionMap.put(StepDaysColumn.STEP, StepDaysColumn.STEP);
		daysProjectionMap.put(StepDaysColumn.TARGET, StepDaysColumn.TARGET);
		daysProjectionMap.put(StepDaysColumn.DATE, StepDaysColumn.DATE);

		weeksProjectionMap = new HashMap<String, String>();
		weeksProjectionMap.put(StepWeeksColumn._ID, StepWeeksColumn._ID);
		weeksProjectionMap.put(StepWeeksColumn._COUNT, StepWeeksColumn._COUNT);
		weeksProjectionMap.put(StepWeeksColumn.WEEKS_TOTAL_STEP, StepWeeksColumn.WEEKS_TOTAL_STEP);
		weeksProjectionMap.put(StepWeeksColumn.DATE_START, StepWeeksColumn.DATE_START);
		weeksProjectionMap.put(StepWeeksColumn.DATE_END, StepWeeksColumn.DATE_END);

		monthsProjectionMap = new HashMap<String, String>();
		monthsProjectionMap.put(StepMonthsColumn._ID, StepMonthsColumn._ID);
		monthsProjectionMap.put(StepMonthsColumn._COUNT, StepMonthsColumn._COUNT);
		monthsProjectionMap.put(StepMonthsColumn.MONTHS_TOTAL_STEP, StepMonthsColumn.MONTHS_TOTAL_STEP);
		monthsProjectionMap.put(StepMonthsColumn.DATE, StepMonthsColumn.DATE);
	}

	private String getKeyValue(Uri uri) {
		if (null == uri) {
			return null;
		}

		String key = null;
		key = uri.getPathSegments().get(0);
		return key;
	}

	private String getId(Uri uri) {
		if (null == uri) {
			return null;
		}

		String id = null;
		id = uri.getPathSegments().get(1);
		return id;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		StringBuilder sqls = new StringBuilder();
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		switch (sUriMather.match(uri)) {
			case MATCH_TYPE_STEP_DAY:
				count = db.delete(Tables.TABLE_STEP_DAYS, selection, selectionArgs);
				break;

			case MATCH_TYPE_STEP_DAY_ID:
				String Id = getId(uri);
				if (StringUtils.isEmpty(Id)) {
					throw new IllegalArgumentException("Unknown URI :" + uri);
				}

				sqls.append(StepDaysColumn._ID).append(DataBaseUtil.SQL_SYMBOL_EQLALS).append(Id);

				if (!(TextUtils.isEmpty(selection))) {
					sqls.append(DataBaseUtil.SQL_SYMBOL_AND)
							.append(DataBaseUtil.SQL_SYMBOL_LEFT_BRACKET)
							.append(selection)
							.append(DataBaseUtil.SQL_SYMBOL_RIGHT_BRACKET);
				} else {
					sqls.append("");
				}
				count = db.delete(Tables.TABLE_STEP_DAYS, sqls.toString(), selectionArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI :" + uri);
		}

		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return count;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		ContentValues values;
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		switch (sUriMather.match(uri)) {
			case MATCH_TYPE_STEP_DAY:
				values = ensureContentValues(initialValues);
				if (!values.containsKey(StepDaysColumn.DATE)
						|| StringUtils.isEmpty(values.getAsString(StepDaysColumn.DATE))) {
					throw new IllegalArgumentException("\"date\" column not exist in values");
				}

				long rowId = -1;
				// 更新weeks、months表单,使用事务实现
				db.beginTransaction();
				try {
					WeeksDbHelper weeksUpdater = WeeksDbHelper.getHelper(dbHelper, getContext());
					Uri weeksUri = weeksUpdater.insertNewRecord(values);
					MonthsDbHelper monthsUpdater = MonthsDbHelper.getHelper(dbHelper, getContext());
					Uri monthsUri = monthsUpdater.insertNewRecord(values);

					if (MATCH_TYPE_STEP_WEEK_ID != sUriMather.match(weeksUri)
							|| MATCH_TYPE_STEP_MONTH_ID != sUriMather.match(monthsUri)) {
						throw new IllegalArgumentException("insert weeks or months table fail");
					} else {
						int weeksId = Integer.parseInt(getId(weeksUri), 10);
						int monthsId = Integer.parseInt(getId(monthsUri), 10);
						values.put(StepDaysColumn.WEEKS_ID, weeksId);
						values.put(StepDaysColumn.MONTHS_ID, monthsId);
					}

					rowId = db.insert(Tables.TABLE_STEP_DAYS, null, values);
					db.setTransactionSuccessful();
				} catch (Exception e) {
				}
				finally {
					db.endTransaction();
				}

				if (rowId > 0L) {

					Uri retUri = ContentUris.withAppendedId(uri, rowId);
					getContext().getContentResolver().notifyChange(retUri, null);
					return retUri;
				}
				break;

			default:
				throw new IllegalArgumentException("Unknown URI :" + uri);
		}

		return null;
	}

	@Override
	public int bulkInsert(Uri arg0, ContentValues[] arg1) {
		// TODO Auto-generated method stub
		return super.bulkInsert(arg0, arg1);
	}

	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arg0)
			throws OperationApplicationException {
		return super.applyBatch(arg0);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DBHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String key;
		String sourceId;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (sUriMather.match(uri)) {
			case MATCH_TYPE_STEP_DAY:
				key = getKeyValue(uri);
				if (StringUtils.isEmpty(key)) {
					throw new IllegalArgumentException("Unknown URI :" + uri);
				}

				qb.setTables(Tables.TABLE_STEP_DAYS);
				qb.setProjectionMap(daysProjectionMap);
				break;

			case MATCH_TYPE_STEP_DAY_ID:
				sourceId = getId(uri);
				if (StringUtils.isEmpty(sourceId)) {
					throw new IllegalArgumentException("Unknown URI :" + uri);
				}

				qb.setTables(Tables.TABLE_STEP_DAYS);
				qb.setProjectionMap(daysProjectionMap);
				break;

			case MATCH_TYPE_STEP_WEEK:
				key = getKeyValue(uri);
				if (StringUtils.isEmpty(key)) {
					throw new IllegalArgumentException("Unknown URI :" + uri);
				}

				qb.setTables(Tables.TABLE_STEP_WEEKS);
				qb.setProjectionMap(weeksProjectionMap);
				break;

			case MATCH_TYPE_STEP_WEEK_ID:
				sourceId = getId(uri);
				if (StringUtils.isEmpty(sourceId)) {
					throw new IllegalArgumentException("Unknown URI :" + uri);
				}

				qb.setTables(Tables.TABLE_STEP_WEEKS);
				qb.setProjectionMap(weeksProjectionMap);
				break;

			case MATCH_TYPE_STEP_MONTH:
				key = getKeyValue(uri);
				if (StringUtils.isEmpty(key)) {
					throw new IllegalArgumentException("Unknown URI :" + uri);
				}

				qb.setTables(Tables.TABLE_STEP_MONTHS);
				qb.setProjectionMap(monthsProjectionMap);
				break;
			case MATCH_TYPE_STEP_MONTH_ID:
				sourceId = getId(uri);
				if (StringUtils.isEmpty(sourceId)) {
					throw new IllegalArgumentException("Unknown URI :" + uri);
				}

				qb.setTables(Tables.TABLE_STEP_MONTHS);
				qb.setProjectionMap(monthsProjectionMap);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI :" + uri);
		}

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues initialValues, String selection, String[] selectionArgs) {
		int count = 0;
		ContentValues values = ensureContentValues(initialValues);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		StringBuilder sqls = new StringBuilder();
		if (!(TextUtils.isEmpty(selection))) {
			sqls.append(DataBaseUtil.SQL_TRUE)
					.append(DataBaseUtil.SQL_SYMBOL_AND)
					.append(DataBaseUtil.SQL_SYMBOL_LEFT_BRACKET)
					.append(selection)
					.append(DataBaseUtil.SQL_SYMBOL_RIGHT_BRACKET);
		} else {
			sqls.append("");
		}

		switch (sUriMather.match(uri)) {
			case MATCH_TYPE_STEP_DAY:
				break;

			case MATCH_TYPE_STEP_DAY_ID:
				String sourceId = getId(uri);
				if (StringUtils.isEmpty(sourceId)) {
					throw new IllegalArgumentException("Unknown URI :" + uri);
				}
				break;

			default:
				throw new IllegalArgumentException("Unknown URI :" + uri);
		}


		db.beginTransaction();
		try {

			// 存在date字段，有可能需要在weeks、months表单中添加新纪录
			if (values.containsKey(StepDaysColumn.DATE)
					&& StringUtils.isNotEmpty(values.getAsString(StepDaysColumn.DATE))) {
				WeeksDbHelper weeksUpdater = WeeksDbHelper.getHelper(dbHelper, getContext());
				Uri weeksUri = weeksUpdater.insertNewRecord(values);
				MonthsDbHelper monthsUpdater = MonthsDbHelper.getHelper(dbHelper, getContext());
				Uri monthsUri = monthsUpdater.insertNewRecord(values);

				if (MATCH_TYPE_STEP_WEEK_ID != sUriMather.match(weeksUri)
						|| MATCH_TYPE_STEP_MONTH_ID != sUriMather.match(monthsUri)) {
					throw new IllegalArgumentException("insert weeks or months table fail");
				} else {
					int weeksId = Integer.parseInt(getId(weeksUri), 10);
					int monthsId = Integer.parseInt(getId(monthsUri), 10);
					values.put(StepDaysColumn.WEEKS_ID, weeksId);
					values.put(StepDaysColumn.MONTHS_ID, monthsId);
				}
			}

			count = db.update(Tables.TABLE_STEP_DAYS, values, sqls.toString(), selectionArgs);
			db.setTransactionSuccessful();
		} catch (Exception e) {
		}
		finally {
			db.endTransaction();
		}

		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	private ContentValues ensureContentValues(ContentValues initialValues) {
		ContentValues result;

		if (initialValues != null) {
			result = new ContentValues(initialValues);
		} else {
			result = new ContentValues();
		}

		return result;
	}
}