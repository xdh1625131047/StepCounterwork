package com.tony.stepcounter.provider;

import com.tony.stepcounter.provider.StepDb.StepDaysColumn;
import com.tony.stepcounter.provider.StepDb.StepMonthsColumn;
import com.tony.stepcounter.provider.StepDb.StepWeeksColumn;
import com.tony.stepcounter.utils.DataBaseUtil;
import com.tony.stepcounter.utils.DateUtil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	public class Tables {
		public final static String TABLE_STEP_DAYS = "days";
		public final static String TABLE_STEP_WEEKS = "weeks";
		public final static String TABLE_STEP_MONTHS = "months";
	}
	private static  String databaseName = "steps.db";
	private static int databaseVersion = 1;
	private Context mContext;

    DBHelper(Context paramContext) {
        super(paramContext, databaseName, null, databaseVersion);
        this.mContext = paramContext;
    }

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + Tables.TABLE_STEP_DAYS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.TABLE_STEP_WEEKS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.TABLE_STEP_MONTHS);
		onCreate(db);
	}

	public void onCreate(SQLiteDatabase sqlitedatabase) {
		
		sqlitedatabase.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.TABLE_STEP_WEEKS + " (" +
				StepWeeksColumn._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				StepWeeksColumn.WEEKS_TOTAL_STEP + " INTEGER NOT NULL DEFAULT 0," +
				StepWeeksColumn.DATE_START + " TEXT NOT NULL," +
				StepWeeksColumn.DATE_END + " TEXT NOT NULL" +
				");");
		
		sqlitedatabase.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.TABLE_STEP_MONTHS + " (" +
				StepMonthsColumn._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				StepMonthsColumn.MONTHS_TOTAL_STEP + " INTEGER NOT NULL DEFAULT 0," +
				StepMonthsColumn.DATE + " TEXT NOT NULL" +
				");");
		
		sqlitedatabase.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.TABLE_STEP_DAYS + " (" +
				StepDaysColumn._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				StepDaysColumn.WEEKS_ID + " INTEGER REFERENCES weeks(_id)," +
				StepDaysColumn.MONTHS_ID + " INTEGER REFERENCES months(_id)," +
				StepDaysColumn.PREVIOUS_STEP + " INTEGER NOT NULL DEFAULT 0," +
				StepDaysColumn.STEP + " INTEGER NOT NULL DEFAULT 0," +
				StepDaysColumn.TARGET + " INTEGER NOT NULL DEFAULT 0," +
				StepDaysColumn.DATE+ " TEXT NOT NULL" +
        		");");
		
		sqlitedatabase.execSQL("DROP TRIGGER IF EXISTS " + Tables.TABLE_STEP_DAYS + "_inserted;");
		sqlitedatabase.execSQL("CREATE TRIGGER " + Tables.TABLE_STEP_DAYS + "_inserted "
				+ " AFTER INSERT ON " + Tables.TABLE_STEP_DAYS
				+ " BEGIN "
				+ "   UPDATE " + Tables.TABLE_STEP_WEEKS
				+ "     SET "
				+         StepWeeksColumn.WEEKS_TOTAL_STEP + "=" + StepWeeksColumn.WEEKS_TOTAL_STEP + " + " + "NEW." + StepDaysColumn.STEP
				+ "     WHERE " + StepWeeksColumn._ID + " = " + "NEW." + StepDaysColumn.WEEKS_ID + ";"
				+ "   UPDATE " + Tables.TABLE_STEP_MONTHS
				+ "     SET "
				+         StepMonthsColumn.MONTHS_TOTAL_STEP + "=" + StepMonthsColumn.MONTHS_TOTAL_STEP + " + " + "NEW."+ StepDaysColumn.STEP
				+ "     WHERE " + StepMonthsColumn._ID + " = " + "NEW." + StepDaysColumn.MONTHS_ID + ";"
				+ " END");
		
		sqlitedatabase.execSQL("DROP TRIGGER IF EXISTS " + Tables.TABLE_STEP_DAYS + "_updated;");
		sqlitedatabase.execSQL("CREATE TRIGGER " + Tables.TABLE_STEP_DAYS + "_updated "
              + " AFTER UPDATE ON " + Tables.TABLE_STEP_DAYS
              + " BEGIN "
              + "   UPDATE " + Tables.TABLE_STEP_WEEKS
              + "     SET "
              +         StepWeeksColumn.WEEKS_TOTAL_STEP + "=" + StepWeeksColumn.WEEKS_TOTAL_STEP + " - " + "OLD." + StepDaysColumn.STEP
              + "     WHERE " + StepWeeksColumn._ID + " = " + "OLD." + StepDaysColumn.WEEKS_ID + ";"
              + "   UPDATE " + Tables.TABLE_STEP_WEEKS
              + "     SET "
              +         StepWeeksColumn.WEEKS_TOTAL_STEP + "=" + StepWeeksColumn.WEEKS_TOTAL_STEP + " + " + "NEW." + StepDaysColumn.STEP
              + "     WHERE " + StepWeeksColumn._ID + " = " + "NEW." + StepDaysColumn.WEEKS_ID + ";"
              + "   UPDATE " + Tables.TABLE_STEP_MONTHS
              + "     SET "
              +         StepMonthsColumn.MONTHS_TOTAL_STEP + "=" + StepMonthsColumn.MONTHS_TOTAL_STEP + " - " + "OLD." + StepDaysColumn.STEP
              + "     WHERE " + StepMonthsColumn._ID + " = " + "OLD." + StepDaysColumn.MONTHS_ID + ";"
              + "   UPDATE " + Tables.TABLE_STEP_MONTHS
              + "     SET "
              +         StepMonthsColumn.MONTHS_TOTAL_STEP + "=" + StepMonthsColumn.MONTHS_TOTAL_STEP + " + " + "NEW." + StepDaysColumn.STEP
              + "     WHERE " + StepMonthsColumn._ID + " = " + "NEW." + StepDaysColumn.MONTHS_ID + ";"
              + " END");
		
		sqlitedatabase.execSQL("DROP TRIGGER IF EXISTS " + Tables.TABLE_STEP_DAYS + "_deleted;");
		sqlitedatabase.execSQL("CREATE TRIGGER " + Tables.TABLE_STEP_DAYS + "_deleted "
				+ " BEFORE DELETE ON " + Tables.TABLE_STEP_DAYS
				+ " BEGIN "
				+ "   UPDATE " + Tables.TABLE_STEP_WEEKS
				+ "     SET "
				+         StepWeeksColumn.WEEKS_TOTAL_STEP + "=" + StepWeeksColumn.WEEKS_TOTAL_STEP + " - " + "OLD." + StepDaysColumn.STEP
				+ "     WHERE " + StepWeeksColumn._ID + " = " + "OLD." + StepDaysColumn.WEEKS_ID + ";"
				+ "   UPDATE " + Tables.TABLE_STEP_MONTHS
				+ "     SET "
				+         StepMonthsColumn.MONTHS_TOTAL_STEP + "=" + StepMonthsColumn.MONTHS_TOTAL_STEP + " - " + "OLD."+ StepDaysColumn.STEP
				+ "     WHERE " + StepMonthsColumn._ID + " = " + "OLD." + StepDaysColumn.MONTHS_ID + ";"
				+ " END");
	}
	
//    public String getSettingAccountNameOrNull(AccountWithDataSet accountWithDataSet) {
//        if (accountWithDataSet == null) {
//            accountWithDataSet = AccountWithDataSet.LOCAL;
//        }
//        final SQLiteStatement select = getWritableDatabase().compileStatement(
//                "SELECT " + Settings.ACCOUNT_NAME +
//                " FROM " + Tables.SETTINGS +
//                " WHERE " + Settings.ACCOUNT_NAME + "= ?");
//        try {
//            DatabaseUtils.bindObjectToProgram(select, 1, accountWithDataSet.getAccountName());
//            try {
//                return select.simpleQueryForString();
//            } catch (SQLiteDoneException notFound) {
//                return null;
//            }
//        } finally {
//            select.close();
//        }
//    }
    
	@Override
	public void onOpen(SQLiteDatabase db) {
//		String weekStart = DateUtil.getWeekStartByDate(values.getAsString(StepDaysColumn.DATE));
//		String weekEnd = DateUtil.getWeekEndByDate(values.getAsString(StepDaysColumn.DATE));
		
//		String selection = DataBaseUtil.SQL_TRUE + DataBaseUtil.SQL_SYMBOL_AND 
//				+ StepWeeksColumn.DATE_START + DataBaseUtil.SQL_SYMBOL_EQLALS + DataBaseUtil.SQL_SYMBOL_QUOTE + weekStart + DataBaseUtil.SQL_SYMBOL_QUOTE
//				+ DataBaseUtil.SQL_SYMBOL_AND 
//				+ StepWeeksColumn.DATE_END + DataBaseUtil.SQL_SYMBOL_EQLALS + DataBaseUtil.SQL_SYMBOL_QUOTE + weekEnd + DataBaseUtil.SQL_SYMBOL_QUOTE;
//		insert or ignore into weeks(step, date_start, date_end) select step,date_start,date_end from weeks where _id = 1 and exists (select * from weeks where _id = 60)
//		final String updateWeeksWhileInsertDays =
//                "INSERT OR IGNORE INTO " + Tables.TABLE_STEP_WEEKS + "(" 
//                		+ StepWeeksColumn.STEP + ", "
//                		+ StepWeeksColumn.DATE_START + ", "
//                		+StepWeeksColumn.DATE_END + ") VALUES (?,?,?)" 
//                +
//                
//        db.execSQL("DROP TRIGGER IF EXISTS " + Tables.TABLE_STEP_DAYS + "_inserted;");
//        db.execSQL("CREATE TRIGGER " + Tables.TABLE_STEP_DAYS + "_inserted "
//                + " AFTER INSERT ON " + Tables.TABLE_STEP_DAYS
//                + " BEGIN "
//                + "   UPDATE " + Tables.TABLE_STEP_MONTHS
//                + "     SET "
//                +         StepMonthsColumn.MONTHS_TOTAL_STEP + "=" + StepMonthsColumn.MONTHS_TOTAL_STEP + " + " + "NEW."+ StepDaysColumn.STEP
//                + "     WHERE " + StepMonthsColumn._ID + " = " + "NEW." + StepDaysColumn.MONTHS_ID + ";"
//                + "   UPDATE " + Tables.TABLE_STEP_WEEKS
//                + "     SET "
//                +         StepWeeksColumn.WEEKS_TOTAL_STEP + "=" + StepWeeksColumn.WEEKS_TOTAL_STEP + " + " + "NEW." + StepDaysColumn.STEP
//                + "     WHERE " + StepWeeksColumn._ID + " = " + "NEW." + StepDaysColumn.WEEKS_ID + ";"
//                + " END");
        
//        db.execSQL("DROP TRIGGER IF EXISTS " + Tables.TABLE_STEP_DAYS + "_updated;");
//        db.execSQL("CREATE TRIGGER " + Tables.TABLE_STEP_DAYS + "_updated "
//                + " AFTER UPDATE ON " + Tables.TABLE_STEP_DAYS
//                + " BEGIN "
//                + "   UPDATE " + Tables.TABLE_STEP_WEEKS
//                + "     SET "
//                +         StepWeeksColumn.STEP + "=" + StepWeeksColumn.STEP + " - " + "OLD." + StepDaysColumn.STEP + " + " + "NEW." + StepDaysColumn.STEP
//                + "     WHERE " + StepWeeksColumn._ID + " = " + "NEW." + StepDaysColumn.WEEKS_ID + ";"
//                + "   UPDATE " + Tables.TABLE_STEP_MONTHS
//                + "     SET "
//                +         StepMonthsColumn.STEP + "=" + StepMonthsColumn.STEP + " - " + "OLD." + StepMonthsColumn.STEP + " + " + "NEW." + StepMonthsColumn.STEP
//                + "     WHERE " + StepMonthsColumn._ID + " = " + "NEW." + StepDaysColumn.MONTHS_ID + ";"
//                + " END");
//        
//        final String replaceAggregatePresenceSql =
//                "INSERT OR REPLACE INTO " + Tables.AGGREGATED_PRESENCE + "("
//                        + AggregatedPresenceColumns.CONTACT_ID + ", "
//                        + StatusUpdates.PRESENCE + ", "
//                        + StatusUpdates.CHAT_CAPABILITY + ")"
//                + " SELECT "
//                        + PresenceColumns.CONTACT_ID + ","
//                        + StatusUpdates.PRESENCE + ","
//                        + StatusUpdates.CHAT_CAPABILITY
//                + " FROM " + Tables.PRESENCE
//                + " WHERE "
//                    + " (ifnull(" + StatusUpdates.PRESENCE + ",0)  * 10 "
//                            + "+ ifnull(" + StatusUpdates.CHAT_CAPABILITY + ", 0))"
//                    + " = (SELECT "
//                        + "MAX (ifnull(" + StatusUpdates.PRESENCE + ",0)  * 10 "
//                                + "+ ifnull(" + StatusUpdates.CHAT_CAPABILITY + ", 0))"
//                        + " FROM " + Tables.PRESENCE
//                        + " WHERE " + PresenceColumns.CONTACT_ID
//                            + "=NEW." + PresenceColumns.CONTACT_ID
//                    + ")"
//                + " AND " + PresenceColumns.CONTACT_ID + "=NEW." + PresenceColumns.CONTACT_ID + ";";
//        
//        db.execSQL("CREATE TRIGGER " + Tables.TABLE_STEP_DAYS + "_inserted"
//                + " AFTER INSERT ON " + Tables.TABLE_STEP_DAYS + "." + Tables.PRESENCE
//                + " BEGIN "
//                + replaceAggregatePresenceSql
//                + " END");
//		
//        db.execSQL("CREATE TRIGGER " + DATABASE_PRESENCE + "." + Tables.PRESENCE + "_deleted"
//                + " AFTER DELETE ON " + DATABASE_PRESENCE + "." + Tables.PRESENCE
//                + " BEGIN "
//                + "   DELETE FROM " + Tables.AGGREGATED_PRESENCE
//                + "     WHERE " + AggregatedPresenceColumns.CONTACT_ID + " = " +
//                        "(SELECT " + PresenceColumns.CONTACT_ID +
//                        " FROM " + Tables.PRESENCE +
//                        " WHERE " + PresenceColumns.RAW_CONTACT_ID
//                                + "=OLD." + PresenceColumns.RAW_CONTACT_ID +
//                        " AND NOT EXISTS" +
//                                "(SELECT " + PresenceColumns.RAW_CONTACT_ID +
//                                " FROM " + Tables.PRESENCE +
//                                " WHERE " + PresenceColumns.CONTACT_ID
//                                        + "=OLD." + PresenceColumns.CONTACT_ID +
//                                " AND " + PresenceColumns.RAW_CONTACT_ID
//                                        + "!=OLD." + PresenceColumns.RAW_CONTACT_ID + "));"
//                + " END");
//        
//        String UPDATE_SQL =
//                "UPDATE " + Tables.CONTACTS +
//                " SET "
//                        + Contacts.NAME_RAW_CONTACT_ID + "=?, "
//                        + Contacts.PHOTO_ID + "=?, "
//                        + Contacts.PHOTO_FILE_ID + "=?, "
//                        + Contacts.SEND_TO_VOICEMAIL + "=?, "
//                        + Contacts.CUSTOM_RINGTONE + "=?, "
//                        + Contacts.LAST_TIME_CONTACTED + "=?, "
//                        + Contacts.TIMES_CONTACTED + "=?, "
//                        + Contacts.STARRED + "=?, "
//                        + Contacts.PINNED + "=?, "
//                        + Contacts.HAS_PHONE_NUMBER + "=?, "
//                        + Contacts.LOOKUP_KEY + "=?, "
//                        + Contacts.CONTACT_LAST_UPDATED_TIMESTAMP + "=? " +
//                " WHERE " + Contacts._ID + "=?";
//
//        String INSERT_SQL =
//                "INSERT INTO " + Tables.CONTACTS + " ("
//                        + Contacts.NAME_RAW_CONTACT_ID + ", "
//                        + Contacts.PHOTO_ID + ", "
//                        + Contacts.PHOTO_FILE_ID + ", "
//                        + Contacts.SEND_TO_VOICEMAIL + ", "
//                        + Contacts.CUSTOM_RINGTONE + ", "
//                        + Contacts.LAST_TIME_CONTACTED + ", "
//                        + Contacts.TIMES_CONTACTED + ", "
//                        + Contacts.STARRED + ", "
//                        + Contacts.PINNED + ", "
//                        + Contacts.HAS_PHONE_NUMBER + ", "
//                        + Contacts.LOOKUP_KEY + ", "
//                        + Contacts.CONTACT_LAST_UPDATED_TIMESTAMP
//                        + ") " +
//                " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
		super.onOpen(db);
	}
}
