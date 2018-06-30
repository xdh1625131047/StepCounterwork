package com.tony.stepcounter.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class StepDb {
	public static final String AUTHORITY = "com.tony.stepcounter".intern();

	/**
	 * @author lantian
	 * Provider uri定义
	 */
	public static class ContentUri {
		/** days table uri */
		public static final Uri STEP_COUNTER_DAYS = Uri.parse("content://" + AUTHORITY + "/" + KeyValue.DAYS);

		/** weeks table uri */
		public static final Uri STEP_COUNTER_WEEKS = Uri.parse("content://" + AUTHORITY + "/" + KeyValue.WEEKS);

		/** months table uri */
		public static final Uri STEP_COUNTER_MONTHS = Uri.parse("content://" + AUTHORITY + "/" + KeyValue.MONTHS);
	}

	/**
	 * @author lantian
	 *  数据库days表列名定义
	 */
	public static class StepDaysColumn implements BaseColumns {

		// days表字段
		public static final String WEEKS_ID = "weeks_id";
		public static final String MONTHS_ID = "months_id";
		public static final String PREVIOUS_STEP = "previousStep";
		public static final String STEP = "step";
		public static final String TARGET = "target";
		public static final String DATE = "date";
	}

	/**
	 * @author lantian
	 *  数据库weeks表列名定义
	 */
	public static class StepWeeksColumn implements BaseColumns {

		// weeks表字段
		public static final String WEEKS_TOTAL_STEP = "weeks_total_step";
		public static final String DATE_START = "date_start";
		public static final String DATE_END = "date_end";
	}

	/**
	 * @author lantian
	 *  数据库months表列名定义
	 */
	public static class StepMonthsColumn implements BaseColumns {

		// months表字段
		public static final String MONTHS_TOTAL_STEP = "months_total_step";
		public static final String DATE = "date";
	}

	/**
	 * @author lantian
	 * 数据库子键定义
	 */
	public static class KeyValue {
		/** step子健 */
		public static final String DAYS = "days";

		/** weeks子健 */
		public static final String WEEKS = "weeks";

		/** weeks子健 */
		public static final String MONTHS = "months";
	}
}