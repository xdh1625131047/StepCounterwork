package com.tony.stepcounter.base;

import com.tony.stepcounter.R;
import com.tony.stepcounter.constants.ListViewType;
import com.tony.stepcounter.provider.StepDb.StepDaysColumn;
import com.tony.stepcounter.provider.StepDb.StepMonthsColumn;
import com.tony.stepcounter.provider.StepDb.StepWeeksColumn;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StepListAdapter extends CursorAdapter {
	
	/**
	 * Ĭ����ʾDays��ͼ
	 */
	private ListViewType mType = ListViewType.TYPE_DAYS;
	
	public StepListAdapter(Context context, Cursor cursor) {
		super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
	}
	
	public void setListType(ListViewType type) {
		this.mType = type;
	}
	
	public ListViewType getListType() {
		return this.mType;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		StepListViewItem viewHolder=(StepListViewItem) view.getTag();
		String title = "";
		String date = "";
		String step = "";
		if (ListViewType.TYPE_DAYS == mType) {
			title = cursor.getString(cursor.getColumnIndex(StepDaysColumn.DATE));
			date = cursor.getString(cursor.getColumnIndex(StepDaysColumn.DATE));
			step = cursor.getString(cursor.getColumnIndex(StepDaysColumn.STEP));
		} else if (ListViewType.TYPE_WEEKS == mType) {
			title = cursor.getString(cursor.getColumnIndex(StepWeeksColumn.DATE_START)) + " - "
					+ cursor.getString(cursor.getColumnIndex(StepWeeksColumn.DATE_END));
			date = cursor.getString(cursor.getColumnIndex(StepWeeksColumn.DATE_START)) + " - "
					+ cursor.getString(cursor.getColumnIndex(StepWeeksColumn.DATE_END));
			step = cursor.getString(cursor.getColumnIndex(StepWeeksColumn.WEEKS_TOTAL_STEP));
		} else if (ListViewType.TYPE_MONTHS == mType) {
			title = cursor.getString(cursor.getColumnIndex(StepMonthsColumn.DATE));
			date = cursor.getString(cursor.getColumnIndex(StepMonthsColumn.DATE));
			step = cursor.getString(cursor.getColumnIndex(StepMonthsColumn.MONTHS_TOTAL_STEP));
		} else {
			// error
		}
        viewHolder.list_item_iv.setImageResource(R.drawable.step_history_lv_item_iv);
        viewHolder.list_item_title_tv.setText(title);
        viewHolder.list_item_date_tv.setText(date);
        viewHolder.list_item_step_tv.setText(step);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		StepListViewItem viewHolder= new StepListViewItem();
		LayoutInflater factory = LayoutInflater.from(context);
        View itemView= factory.inflate(R.layout.step_history_lv_item, parent, false);
        viewHolder.list_item_iv = (ImageView) itemView.findViewById(R.id.step_history_lv_item_iv);
        viewHolder.list_item_title_tv = (TextView) itemView.findViewById(R.id.step_history_lv_item_tv_title);
        viewHolder.list_item_date_tv = (TextView) itemView.findViewById(R.id.step_history_lv_item_tv_date);
        viewHolder.list_item_step_tv = (TextView)itemView.findViewById(R.id.step_history_lv_item_tv_step);
        itemView.setTag(viewHolder);
        Log. i("cursor" ,"newView=" +itemView);
        return itemView;
	}

}
