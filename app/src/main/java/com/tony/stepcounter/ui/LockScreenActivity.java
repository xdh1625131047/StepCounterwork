package com.tony.stepcounter.ui;

import java.util.Locale;

import com.tony.stepcounter.R;
import com.tony.stepcounter.application.StepCounterApplication;
import com.tony.stepcounter.bean.MessengerBean;
import com.tony.stepcounter.constants.Constant;
import com.tony.stepcounter.service.StepCounterService;
import com.tony.stepcounter.service.StepCounterService.ServiceBinder;
import com.tony.stepcounter.ui.view.SwipeListener;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

public class LockScreenActivity extends Activity implements Handler.Callback {
	RelativeLayout mLayout;
	TextView mSteps;
	TextClock mDate;
	Context mContext;
	private Handler uiHandler;
	private MessengerBean mUiMessenger = null;
	private StepCounterService mService = null;

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case Constant.MSG_ADD_TARGET_TO_SERVER:
			if (null != mService) {
				mService.appendMessenger(mUiMessenger);
			}
			break;

		case Constant.MSG_REMOVE_TARGET_TO_SERVER:
			if (null != mService) {
				mService.removeMessenger(mUiMessenger);
			}
			break;

		case Constant.MSG_SEND_DATA_TO_CLIENT:
			// ʵʱ���²���
			Bundle data = msg.getData();
			if (null != data && null != mSteps) {
				mSteps.setText(String.valueOf(data.getInt("step")));
			}
			break;

		case Constant.MSG_STEP_COUNTER_STATE_CHANGE:
			if (null == mService) {
				return true;
			}

			// ���ǲ�����ֹͣʱ��ɱ���Լ�
			if (!mService.isCounterServiceRunning()) {
				finish();
			}
			break;
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		mContext = this;
		uiHandler = new Handler(this);
		mUiMessenger = new MessengerBean(this.getComponentName(), uiHandler);
		setContentView(R.layout.step_lockscreen);
		initView();

		// Ӧ��������enable service,ͬʱ����Ϣ���
		this.bindService(new Intent(this, StepCounterService.class), mConn, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		if (null != mService) {
			mService.removeMessenger(mUiMessenger);
		}
		unbindService(mConn);
		super.onDestroy();
	}

	// ��bind��ʽ����service������ServiceConnection���ջص�
	ServiceConnection mConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			if (null == binder) {
				return;
			}

			mService = ((ServiceBinder) binder).getStepCounterService();

			// ���������Ϣ�������Ϣ
			Message message = uiHandler.obtainMessage(Constant.MSG_ADD_TARGET_TO_SERVER);
			uiHandler.sendMessageDelayed(message, 100);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = (null != mService) ? null : mService;
		}
	};

	private void initView() {
		mLayout = (RelativeLayout) findViewById(R.id.step_lockscreen_layout);
		if (null != mLayout) {
			mLayout.setOnTouchListener(new MySwipeListener(mContext));
		}

		mSteps = (TextView) findViewById(R.id.step_lockscreen_current_step);
		mDate = (TextClock) findViewById(R.id.step_lockscreen_date);
		
		Locale locale = Locale.getDefault();
        String skeleton = "MMMMdEEEE";
		CharSequence dateFormat = DateFormat.getBestDateTimePattern(locale, skeleton);
        mDate.setFormat12Hour(dateFormat);
        mDate.setFormat24Hour(dateFormat);
	}

	private class MySwipeListener extends SwipeListener {

		public MySwipeListener(Context context) {
			super(context);
		}

		@Override
		public boolean swipeUp() {
			StepCounterApplication.disableKeyguardLock();
			finish();
			return true;
		}

		@Override
		public boolean swipeDown() {
			return true;
		}

		@Override
		public boolean swipeLeft() {
			return true;
		}

		@Override
		public boolean swipeRight() {
			return true;
		}
	}
}
