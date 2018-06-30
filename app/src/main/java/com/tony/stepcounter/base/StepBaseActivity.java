package com.tony.stepcounter.base;

import com.tony.stepcounter.R;
import com.tony.stepcounter.application.StepCounterApplication;
import com.tony.stepcounter.logic.StepLogicManager;
import com.tony.stepcounter.ui.PermissionHelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class StepBaseActivity extends Activity{
	private TextView title_tv = null;
	private ImageView title_left_btn = null;
	private TextView title_right_btn = null;
	protected StepLogicManager logic;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		PermissionHelper.init(this);
		setContentView(getLayoutResourceId());
		logic = StepLogicManager.getInstance(getApplicationContext());
		
		initTitleBar();
		
		initView();
		
		initViewData();
	}
	
	private void initTitleBar() {
		title_tv = (TextView)findViewById(R.id.title_centre_tv);
		title_tv.setText(getTitleResourceId());
		
		
		title_left_btn = (ImageView)findViewById(R.id.title_left_iv);
		title_left_btn.setVisibility(isLeftButonVisible()? View.VISIBLE:View.INVISIBLE);
		title_left_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				onLeftButtonPressed(view);
			}
		});
		
		title_right_btn = (TextView)findViewById(R.id.title_right_tv);
		title_right_btn.setVisibility(isRightButonVisible()? View.VISIBLE:View.INVISIBLE);
		title_right_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				onRightButtonPressed(view);
			}
		});
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.getInstance().onPermissionsResult(requestCode, permissions, grantResults);
	}

	protected abstract int getLayoutResourceId();
	protected abstract int getTitleResourceId();
	protected abstract void onLeftButtonPressed(View view);
	protected abstract void onRightButtonPressed(View view);
	protected abstract boolean isLeftButonVisible();
	protected abstract boolean isRightButonVisible();
	protected abstract void initView();
	protected abstract void initViewData();
}
