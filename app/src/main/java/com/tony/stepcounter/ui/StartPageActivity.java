package com.tony.stepcounter.ui;

import com.littlejie.circleprogress.CircleProgress;
import com.tony.guideview.Component;
import com.tony.guideview.Guide;
import com.tony.guideview.GuideBuilder;
import com.tony.stepcounter.R;
import com.tony.stepcounter.application.StepCounterApplication;
import com.tony.stepcounter.base.StepBaseActivity;
import com.tony.stepcounter.bean.MessengerBean;
import com.tony.stepcounter.bean.StepInfoBean;
import com.tony.stepcounter.constants.Constant;
import com.tony.stepcounter.service.StepCounterService;
import com.tony.stepcounter.service.StepCounterService.ServiceBinder;
import com.tony.stepcounter.utils.DateUtil;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StartPageActivity extends StepBaseActivity implements Handler.Callback {

	public static final String TAG = "StartPageActivity";
	private TextView tvCurrentTarget = null;
	private TextView tvCurrentDistance = null;
	private CircleProgress circleProgress = null;
	private Button btnOpStepCounter = null;
	private StepCounterService mService = null;

	private Handler uiHandler;
	private MessengerBean mUiMessenger = null;
	private BroadcastReceiver mIntentReceiver;
	Guide guide;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		uiHandler = new Handler(this);
		mUiMessenger = new MessengerBean(this.getComponentName(), uiHandler);

		// 应用启动先enable service,同时绑定消息句柄
		this.bindService(new Intent(this, StepCounterService.class), mConn, Context.BIND_AUTO_CREATE);

		registerUiRefreshReceiver();

		final SharedPreferences preferences = getApplicationContext().getSharedPreferences(Constant.PREFERENCE_FILE, Context.MODE_PRIVATE);
		if (preferences.getBoolean(Constant.IS_FIRST_RUNNING, true)) {
			new Handler().post(new Runnable() {

				@Override
				public void run() {
					showGuideView();
				}
			});
		}
	}

	/**
	 * 注册UI刷新的广播监听器
	 */
	private void registerUiRefreshReceiver() {
		// 定义意图过滤器
		final IntentFilter filter = new IntentFilter();
		// 日期修改
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		// 修改时区
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		// 屏幕高亮广播
		filter.addAction(Intent.ACTION_SCREEN_ON);
		// 屏幕解锁广播
		filter.addAction(Intent.ACTION_USER_PRESENT);

		if (null == mIntentReceiver) {
			mIntentReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					StepCounterApplication.getLogUtil().v(TAG, "onReceive:action" + action);
					if (Intent.ACTION_SCREEN_ON.equals(action) || Intent.ACTION_USER_PRESENT.equals(action)
							|| Intent.ACTION_TIME_CHANGED.equals(action)
							|| Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
						// 刷新数据，当CounterService Running状态时，数据一般能正常刷新，
						// 但是当CounterService
						// Disable状态时，UI数据不会自动更新，因此用如上几个广播监听主动刷新UI
						if (null != mService && !mService.isCounterServiceRunning()) {
							updateDisplay(getCurrentSteps());
						}
					}
				}
			};
			registerReceiver(mIntentReceiver, filter);
		}
	}

	/**
	 * 注销UI刷新的广播监听器
	 */
	private void unregisterUiRefreshReceiver() {
		if (null != mIntentReceiver) {
			unregisterReceiver(mIntentReceiver);
			mIntentReceiver = null;
		}
	}

	private void enableStepCounter() {
		StepCounterApplication.getLogUtil().d(TAG, "enableStepCounter");
		Intent intent = new Intent(this, StepCounterService.class);
		intent.setAction(Constant.ACTION_ENABLE_STEP_COUNTER);
		startService(intent);
	}

	private void disableStepCounter() {
		StepCounterApplication.getLogUtil().d(TAG, "disableStepCounter");
		Intent intent = new Intent(this, StepCounterService.class);
		intent.setAction(Constant.ACTION_DISABLE_STEP_COUNTER);
		startService(intent);
	}

	// 以bind形式开启service，故有ServiceConnection接收回调
	ServiceConnection mConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			if (null == binder) {
				return;
			}
			mService = ((ServiceBinder) binder).getStepCounterService();

			// 发送初始化控件按钮的消息，因为按钮是在确定了service运行状态之后才能确定显示状态的，因此需要在这里发送
			Message msg1 = uiHandler.obtainMessage(Constant.MSG_STEP_COUNTER_STATE_CHANGE);
			uiHandler.sendMessageDelayed(msg1, 100);

			// 发送添加消息句柄的消息
			Message msg2 = uiHandler.obtainMessage(Constant.MSG_ADD_TARGET_TO_SERVER);
			uiHandler.sendMessageDelayed(msg2, 100);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = (null != mService) ? null : mService;
		}
	};

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
				// 实时更新步数
				Bundle data = msg.getData();
				if (null != data && 0 != data.getInt("step")) {
					updateDisplay(data.getInt("step"));
				} else {
					updateDisplay(getCurrentSteps());
				}
				break;

			case Constant.MSG_STEP_COUNTER_STATE_CHANGE:
				if (null == mService) {
					return true;
				}

				// 刷新按钮显示
				Resources res = getApplicationContext().getResources();
				if (mService.isCounterServiceRunning()) {
					btnOpStepCounter.setTextColor(res.getColor(R.color.color_stop_button_tv, null));
					btnOpStepCounter.setText(R.string.str_stop_step_counter);
				} else {
					btnOpStepCounter.setTextColor(res.getColor(R.color.color_start_button_tv, null));
					btnOpStepCounter.setText(R.string.str_start_step_counter);
				}
				break;
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		// 取消服务绑定
		if (null != mService) {

			// 解决应用退出后再次进入界面时出现步数倒退的情况，同时用Service状态做条件，避免关闭计步器状态下出现日期变化后，将旧的步数插入到新日期的记录中
			if (mService.isCounterServiceRunning()) {
				mService.saveToStepDb(true);
			}
			mService.removeMessenger(mUiMessenger);
		}
		unregisterUiRefreshReceiver();
		unbindService(mConn);
		super.onDestroy();
	}

	@Override
	protected int getLayoutResourceId() {
		return R.layout.step_start_page;
	}

	@Override
	protected int getTitleResourceId() {
		return R.string.app_name;
	}

	@Override
	protected void onLeftButtonPressed(View view) {
		this.finish();
	}

	@Override
	protected void onRightButtonPressed(View view) {

		Intent intent = new Intent("com.tony.intent.action.STEP_COUNT_HISTORY");
		startActivity(intent);
		//
		// final Dialog popupMenu = new Dialog(this,
		// R.style.StylePopupWindowMenu);
		// LinearLayout root = (LinearLayout) LayoutInflater.from(this).inflate(
		// R.layout.window_menu, null);
		// root.findViewById(R.id.popup_menu_item_history).setOnClickListener(new
		// OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// Intent intent = new
		// Intent("com.tony.intent.action.STEP_COUNT_HISTORY");
		// startActivity(intent);
		//
		// if (null != popupMenu) {
		// popupMenu.dismiss();
		// }
		// }
		// });
		//
		// root.findViewById(R.id.popup_menu_item_target).setOnClickListener(new
		// OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// showTargetDialog();
		// if (null != popupMenu) {
		// popupMenu.dismiss();
		// }
		// }
		// });
		//
		// root.findViewById(R.id.popup_menu_item_stop_step_counter).setOnClickListener(new
		// OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// showStopCounterDialog();
		// if (null != popupMenu) {
		// popupMenu.dismiss();
		// }
		// }
		// });
		// popupMenu.setContentView(root);
		// Window dialogWindow = popupMenu.getWindow();
		// dialogWindow.setGravity(Gravity.TOP | Gravity.RIGHT);
		// root.measure(0, 0);
		// popupMenu.show();
	}

	@Override
	protected boolean isLeftButonVisible() {
		return false;
	}

	@Override
	protected boolean isRightButonVisible() {
		return true;
	}

	@Override
	protected void initView() {
		tvCurrentTarget = (TextView) findViewById(R.id.current_target_tv);
		tvCurrentDistance = (TextView) findViewById(R.id.current_distance_tv);
		circleProgress = (CircleProgress) findViewById(R.id.circle_progress_bar);
		btnOpStepCounter = (Button) findViewById(R.id.operate_step_counter_btn);
	}

	@Override
	protected void initViewData() {
		Resources res = getApplicationContext().getResources();
		int currentTarget = getCurrentTarget();
		String target = res.getString(R.string.str_current_target, currentTarget);
		if (null != tvCurrentTarget) {
			tvCurrentTarget.setText(target);
		}
		if (null != circleProgress) {
			circleProgress.setMaxValue(currentTarget);
		}

		if (null != tvCurrentTarget) {
			tvCurrentTarget.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					showTargetDialog();
				}
			});
		}

		//updateDisplay(getCurrentSteps());

		if (null != btnOpStepCounter) {
			btnOpStepCounter.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					if (null == mService) {
						// bind service有延时效应，这里避免mService空指针
						return;
					}

					if (mService.isCounterServiceRunning()) {
						// 显示停止对话框
						showStopCounterDialog();
					} else {
						enableStepCounter();
					}
				}
			});
		}
	}

	/**
	 * 更新几个需要动态update的view视图
	 *
	 * @param steps
	 *            步数
	 */
	private void updateDisplay(int steps) {
		Resources res = getApplicationContext().getResources();
		if (null != circleProgress) {
			circleProgress.setValue(steps);
		}

		if (null != tvCurrentDistance) {
			tvCurrentDistance
					.setText(String.format(res.getString(R.string.str_current_distance), (float) (steps * 0.5) / 1000));
		}
	}

	private int getCurrentTarget() {
		Context context = getApplicationContext();
		int ret = logic.getStepLogicPolicy().getLatestTarget(context);
		return (0 < ret) ? ret : context.getResources().getInteger(R.integer.integer_default_tareget);
	}

	private int getCurrentSteps() {
		Context context = getApplicationContext();
		return logic.getStepLogicPolicy().getCurrentSteps(context);
	}

	private void showTargetDialog() {
		final CustomDialog dialog = new CustomDialog(this, Constant.DIALOG_ID_SET_TARGET, R.layout.dialog_set_target);
		dialog.setListener(new DialogButtonClickListener() {

			@Override
			public void onSecondButtonClick() {
				if (null != dialog) {
					dialog.dismiss();
				}
			}

			@Override
			public void onFirstButtonClick() {
				if (null != dialog) {
					EditText edit = (EditText) dialog.findViewById(R.id.edit_target);

					if (null != edit) {
						String text = edit.getEditableText().toString();
						int target = 0;
						try {
							target = Integer.parseInt(text);
						} catch (Exception e) {
						}

						if (0 >= target || 1000000 < target) {
							Toast.makeText(getApplicationContext(), R.string.str_toast_input_target, Toast.LENGTH_SHORT)
									.show();
							return;
						} else {
							// 插入数据库
							StepInfoBean bean = new StepInfoBean();
							bean.setDate(DateUtil.getTodayDate());
							bean.setTarget(Integer.parseInt(text, 10));
							logic.getStepLogicPolicy().addStepRecord(getApplicationContext(), bean);

							// 更新显示
							Resources res = getApplicationContext().getResources();
							tvCurrentTarget.setText(res.getString(R.string.str_current_target, target));
							circleProgress.setMaxValue(target);

							// 控件bug，更新target之后，circleProgress不会自动更新，需要主动再重新setValue，出发progressBar运算
							// updateDisplay(getCurrentSteps());
						}
					}

					dialog.dismiss();
				}
			}
		});
		dialog.show();
		dialog.setEditHint(R.id.edit_target, "" + getCurrentTarget());
	}

	private void showStopCounterDialog() {
		final CustomDialog dialog = new CustomDialog(this, Constant.DIALOG_ID_STOP_STEP_COUNTER,
				R.layout.dialog_stop_stepcounter);
		dialog.setListener(new DialogButtonClickListener() {

			@Override
			public void onSecondButtonClick() {
				if (null != dialog) {
					dialog.dismiss();
				}
			}

			@Override
			public void onFirstButtonClick() {
				disableStepCounter();
				if (null != dialog) {
					dialog.dismiss();
				}
			}
		});
		dialog.show();
	}

	public void showGuideView() {
		GuideBuilder builder = new GuideBuilder();
		builder.setTargetView(tvCurrentTarget).setAlpha(170).setHighTargetCorner(20).setHighTargetPadding(10)
				.setOverlayTarget(false).setOutsideTouchable(false).setAutoDismiss(false);
		builder.setOnVisibilityChangedListener(new GuideBuilder.OnVisibilityChangedListener() {
			@Override
			public void onShown() {
			}

			@Override
			public void onDismiss() {
			}
		});

		builder.addComponent(new SimpleComponent());
		guide = builder.createGuide();
		guide.setShouldCheckLocInWindow(false);
		guide.show(StartPageActivity.this);
	}

	public class SimpleComponent implements Component {

		@Override
		public View getView(LayoutInflater inflater) {

			LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.layer_frends, null);
			Button btnKnows = (Button)ll.findViewById(R.id.btn_knows);
			btnKnows.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (null != guide) {
						guide.dismiss();
						SharedPreferences preferences = getApplicationContext().getSharedPreferences(Constant.PREFERENCE_FILE, Context.MODE_PRIVATE);
						preferences.edit().putBoolean(Constant.IS_FIRST_RUNNING, false).commit();
					}
				}
			});
			return ll;
		}

		@Override
		public int getAnchor() {
			return Component.ANCHOR_BOTTOM;
		}

		@Override
		public int getFitPosition() {
			return Component.FIT_END;
		}

		@Override
		public int getXOffset() {
			return 0;
		}

		@Override
		public int getYOffset() {
			return 10;
		}
	}
}