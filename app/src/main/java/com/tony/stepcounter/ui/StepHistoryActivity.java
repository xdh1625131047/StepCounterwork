package com.tony.stepcounter.ui;

import com.tony.stepcounter.R;
import com.tony.stepcounter.base.StepBaseActivity;
import com.tony.stepcounter.base.StepListAdapter;
import com.tony.stepcounter.constants.ListViewType;
import com.tony.stepcounter.provider.StepDb.ContentUri;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class StepHistoryActivity extends StepBaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{
	
	private StepListAdapter mAdapter;
	private ListView mStepListView = null;
	private LinearLayout mLayoutDays;
	private LinearLayout mLayoutWeeks;
	private LinearLayout mLayoutMonths;
	private TextView insistDays;
	private TextView achievedDays;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		if (null != mLayoutDays) {
			mLayoutDays.performClick();
		}
	}
	
	@Override
	protected int getLayoutResourceId() {
		return R.layout.step_history_page;
	}

	@Override
	protected int getTitleResourceId() {
		return R.string.history_page_title;
	}

	@Override
	protected void onLeftButtonPressed(View view) {
		this.finish();
	}

	@Override
	protected void onRightButtonPressed(View view) {
	}

	@Override
	protected boolean isLeftButonVisible() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean isRightButonVisible() {
		return false;
	}

	@Override
	protected void initView() {
		insistDays = (TextView)findViewById(R.id.insist_days_tv);
		achievedDays = (TextView)findViewById(R.id.achieved_days_tv);
		mStepListView = (ListView)findViewById(R.id.step_history_lv);
		
		mLayoutDays = (LinearLayout)findViewById(R.id.layout_days);
		mLayoutWeeks = (LinearLayout)findViewById(R.id.layout_weeks);
		mLayoutMonths = (LinearLayout)findViewById(R.id.layout_months);
		
		mLayoutDays.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				view.setActivated(true);
				mLayoutWeeks.setActivated(false);
				mLayoutMonths.setActivated(false);
				
				refreshList(ListViewType.TYPE_DAYS);
			}
		});
		
		mLayoutWeeks.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				view.setActivated(true);
				mLayoutDays.setActivated(false);
				mLayoutMonths.setActivated(false);
				
				refreshList(ListViewType.TYPE_WEEKS);
			}
		});
		
		mLayoutMonths.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				view.setActivated(true);
				mLayoutDays.setActivated(false);
				mLayoutWeeks.setActivated(false);
				
				refreshList(ListViewType.TYPE_MONTHS);
			}
		});
	}
	
	@Override
	protected void initViewData() {

		if (null != insistDays) {
			int days = logic.getStepLogicPolicy().getInsistDays(getApplicationContext());
			insistDays.setText("" + days);
		}

		if (null != achievedDays) {
			int days = logic.getStepLogicPolicy().getAchievedDays(getApplicationContext());
			achievedDays.setText("" + days);
		}
		
		if (null == mAdapter) {
			mAdapter = new StepListAdapter(this, null);
		}
		mAdapter.setListType(ListViewType.TYPE_DAYS);
		if (null != mStepListView) {
			mStepListView.setAdapter(mAdapter);
		}
		getLoaderManager().initLoader(0, null, this);
	}
	
	private void refreshList(ListViewType type) {
		if (null == mAdapter) {
			mAdapter = new StepListAdapter(this, null);
		}
		mAdapter.setListType(type);
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		Uri uri = ContentUri.STEP_COUNTER_DAYS;
		if (ListViewType.TYPE_DAYS == mAdapter.getListType()) {
			uri = ContentUri.STEP_COUNTER_DAYS;
		} else if (ListViewType.TYPE_WEEKS == mAdapter.getListType()) {
			uri = ContentUri.STEP_COUNTER_WEEKS;
		} else if (ListViewType.TYPE_MONTHS == mAdapter.getListType()) {
			uri = ContentUri.STEP_COUNTER_MONTHS;
		} else {
			//error
		}
		
		Loader<Cursor> loader;
		
		loader = new CursorLoader(this, uri, null, null, null, "_id desc");
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		mAdapter.changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.changeCursor(null);
	}

}
