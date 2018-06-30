package com.tony.stepcounter.service;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import com.tony.stepcounter.R;
import com.tony.stepcounter.application.StepCounterApplication;
import com.tony.stepcounter.base.StepBaseSensor;
import com.tony.stepcounter.base.StepBaseSensor.CounterState;
import com.tony.stepcounter.base.StepBaseSensor.OnStepChangeListener;
import com.tony.stepcounter.bean.AccSensorDataBean;
import com.tony.stepcounter.bean.MessengerBean;
import com.tony.stepcounter.bean.StepInfoBean;
import com.tony.stepcounter.constants.Constant;
import com.tony.stepcounter.logic.StepLogicManager;
import com.tony.stepcounter.sensors.StepAcceleroMeterSensor;
import com.tony.stepcounter.utils.DateUtil;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.widget.RemoteViews;
import android.widget.Toast;

public class StepCounterService extends Service {

	public static final String TAG = "StepCounterService";
	private static StepCounterService gInstance = null;
	// 广播
	private BroadcastReceiver mIntentReceiver;
	private BroadcastReceiver mScreenOffReceiver;
	private PowerManager.WakeLock mWakeLock;
	private SensorManager sensorManager;
	private StepBaseSensor currentSensor;
	private ServiceHandler mServiceHandler;
	private Looper mServiceLooper;
	private static int mSteps = 0;

	/**
	 * 保存ui消息句柄,目前计步器只有StartPageActivity需要保存消息句柄，用于更新ui，但为后续可能存在的其他需求，扩展为ArrayMap形式保存
	 */
	ArrayMap<Integer, MessengerBean> messengerTargets = new ArrayMap<Integer, MessengerBean>();

	/** mCounterDetectTimer上次计步数据，用于判定计步是否在规律自增，以此来判定是否需要开始计步 */
	public static int lastSteps = 0;

	/** 临时计步信息，当StepCounter处于STEP_COUNTER_IN_JUDGE（评估状态）时，计步暂存在此变量中 */
	public static int tempSteps = 0;

	/** 数据存储唤醒计时器，默认30秒做一次db操作，灭屏时为60秒。 */
	private SaveTimer dbUpdateTimer;

	/** 数据存储计时器间隔，默认30秒，灭屏时为60秒 */
	private static int dbUpdateDuration = 30000;

	/** StepCounterService运行状态 */
	private static ServiceState mServiceState = ServiceState.COUNTER_SERVICE_IS_PAUSED;

	enum ServiceState {
		/** 计步服务在运行，此状态持续唤醒，高功耗状态 */
		COUNTER_SERVICE_IS_RUNNING,
		/** 计步服务在睡眠，可待机 */
		COUNTER_SERVICE_IS_PAUSED
	}

	/** 计步评估时间，倒计时3.5秒，3.5秒内不会显示计步，用于屏蔽细微波动 */
	private long COUNTER_JUDGE_TIME = 3500;
	/** 计步评估计时器，在开始步行前3.5秒内，每0.7秒进行一次评估，如两次step值无变动，则代表此时间段内数据是无效振动 */
	private TimeCount mCounterJudgeTimer;
	/** 计步评估计时器，在步行超过3.5秒之后，每2秒进行一次评估，如两次step值无变动，则代表步行停止 */
	private Timer mDetectorTaskTimer;

	private StepCounterState mStepCounterState = StepCounterState.STEP_COUNTER_DISABLED;

	enum StepCounterState {
		/** 没有在计步，睡眠中 */
		STEP_COUNTER_DISABLED,
		/** 正在检测是否需要计步 */
		STEP_COUNTER_IN_JUDGE,
		/** 计步中 */
		STEP_COUNTER_IS_COUNTTING
	}

	@Override
	public void onCreate() {
		super.onCreate();
		gInstance = this;
		HandlerThread thread = new HandlerThread(StepCounterService.class.getName(),
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		currentSensor = StepBaseSensor.getInstance(this.getApplicationContext());
		currentSensor.setOnStepChangeListener(new OnStepChangeListener() {

			@Override
			public void onChange() {
				StepCounterApplication.getLogUtil().d(TAG, "apears Step+++++++++++++" + getTotalSteps());
				StepCounterApplication.getLogUtil().d(TAG, "mStepCounterState:" + mStepCounterState);
				if (StepCounterState.STEP_COUNTER_DISABLED == mStepCounterState) {
					// 开启计时器(倒计时3.5秒,倒计时时间间隔为0.7秒) 是在3.5秒内每0.7面去监测一次。
					mCounterJudgeTimer = new TimeCount(COUNTER_JUDGE_TIME, 700);
					mCounterJudgeTimer.start();
					mStepCounterState = StepCounterState.STEP_COUNTER_IN_JUDGE; // 开始评估是否需要计步
					StepCounterApplication.getLogUtil().v(TAG, "JudgeTimer started:CounterService begin judging!");
				} else if (StepCounterState.STEP_COUNTER_IN_JUDGE == mStepCounterState) {
					tempSteps++; // 如果传感器测得的数据满足走一步的条件则步数加1
					StepCounterApplication.getLogUtil().v(TAG, "计步中 TEMP_STEP:" + tempSteps);
				} else if (StepCounterState.STEP_COUNTER_IS_COUNTTING == mStepCounterState) {
					increaseTotalSteps(1);
				}
			}
		});
		
		// 注册灭屏广播，处理锁屏、解锁业务
		mScreenOffReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				StepCounterApplication.enableKeyguardLock();
				if (isCounterServiceRunning()) {
					Message message = mServiceHandler.obtainMessage(Constant.MSG_DISPLAY_LOCKSCREEN_UI);
					mServiceHandler.sendMessageDelayed(message, 2000);										
				}
			}
		};
		registerReceiver(mScreenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}
	
	/**
	 * 初始化service数据
	 */
	private void initServiceData() {

		StepLogicManager logic = StepLogicManager.getInstance(getApplicationContext());
		StepInfoBean latestBean = logic.getStepLogicPolicy().getLatestStepInfoBean(getApplicationContext());
		if (null != latestBean && null != latestBean.getDate()
				&& latestBean.getDate().equals(DateUtil.getTodayDate())) {
			mSteps = latestBean.getStep();
		} else {
			mSteps = 0;
		}

		Bundle bundle = new Bundle();
		bundle.putInt("step", getTotalSteps());

		// TODO 消息通知,需要测试性能问题
		sendUIMessage(Constant.MSG_SEND_DATA_TO_CLIENT, bundle);
	}

	private void startStepCounter() {

		if (null != sensorManager && null != currentSensor) {
			sensorManager.registerListener(currentSensor, currentSensor.getCurrentSensor(),
					SensorManager.SENSOR_DELAY_UI);
			currentSensor.setCounterState(CounterState.ENABLED);

			// 打开计步器，获取唤醒锁
			acquireWakeLock(getApplicationContext());

			// 开始计时
			updateServiceState(ServiceState.COUNTER_SERVICE_IS_RUNNING);
			startDbUpdateTimeCount();

			initServiceData();
		}
	}
    
	/**
	 * 操作消息通知
	 * @param enable true:显示通知，false:取消通知显示 
	 */
	private void opStepNotification(boolean enable) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (!enable) {
			// 取消通知消息
			notificationManager.cancel(Constant.NOTIFICATION_STEP_COUNTER_ENABLED);
			return;
		} else {
			// 生成通知消息
			RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
			Intent btnIntent = new Intent(Constant.ACTION_DISABLE_STEP_COUNTER);
			PendingIntent btnPendingIntent = PendingIntent.getService(getApplicationContext(), 0, btnIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.btn_notification_stop, btnPendingIntent);

			// 通知消息Intent定义
			Intent intent = new Intent(Constant.ACTION_SHOW_STEP_COUNTER);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			Notification.Builder builder = new Builder(getApplicationContext())
					.setShowWhen(false)
					.setSmallIcon(R.mipmap.ic_launcher)
					.setAutoCancel(true)
					.setOngoing(true)
					.setPriority(Notification.PRIORITY_DEFAULT)
					.setCategory(Notification.CATEGORY_MESSAGE)
					.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
					.setVisibility(Notification.VISIBILITY_PUBLIC)
					.setContentIntent(pendingIntent);

			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
				builder.setContent(remoteViews);
			} else {
				builder.setCustomContentView(remoteViews);
			}

			notificationManager.notify(Constant.NOTIFICATION_STEP_COUNTER_ENABLED, builder.build());
		}
	}
	
//	/**
//	 * 操作消息通知
//	 * @param enable true:显示通知，false:取消通知显示 
//	 */
//	private void opStepNotification(boolean enable) {
//		Intent intent = new Intent(Constant.ACTION_SHOW_STEP_COUNTER);
//		intent.addCategory(Intent.CATEGORY_DEFAULT);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
//		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
//				.setCustomContentView(remoteViews)
//				.setContentTitle("已开启计步器：")
//				.setContentText("手机耗电中，请注意电池电量。")
//				.setShowWhen(false)
//				.setUsesChronometer(false)
//				.setAutoCancel(false)
//				.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_VIBRATE)
//				.setSmallIcon(R.drawable.ic_launcher)
//				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
//				.setCategory(NotificationCompat.CATEGORY_REMINDER)
//				.setUsesChronometer(false)
//				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//        
//		builder.setContentIntent(
//				PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
//
//		NotificationManagerCompat nm = NotificationManagerCompat.from(getApplicationContext());
//		if (enable) {
//			nm.notify(Constant.NOTIFICATION_STEP_COUNTER_ENABLED, builder.build());			
//		} else {
//			nm.cancel(Constant.NOTIFICATION_STEP_COUNTER_ENABLED);
//		}
//	}

	private void stopStepCounter() {

		if (null != sensorManager && null != currentSensor) {
			currentSensor.setCounterState(CounterState.DISABLED);
			sensorManager.unregisterListener(currentSensor);

			// 停止计步器，释放唤醒锁
			releaseWakeLock();

			// 停止计时
			updateServiceState(ServiceState.COUNTER_SERVICE_IS_PAUSED);

			// 打印统计数据，用于分析、优化计步器性能
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {

					StepCounterApplication.getLogUtil().d("excel", "begin print time");
					for (AccSensorDataBean bean : StepAcceleroMeterSensor.arrayList) {
						StepCounterApplication.getLogUtil().d("excel-time", "" + bean.getTime());
					}
					StepCounterApplication.getLogUtil().d("excel", "begin print sensor x");
					for (AccSensorDataBean bean : StepAcceleroMeterSensor.arrayList) {
						StepCounterApplication.getLogUtil().d("excel-x", "" + bean.x);
					}
					StepCounterApplication.getLogUtil().d("excel", "begin print sensor y");
					for (AccSensorDataBean bean : StepAcceleroMeterSensor.arrayList) {
						StepCounterApplication.getLogUtil().d("excel-y", "" + bean.y);
					}
					StepCounterApplication.getLogUtil().d("excel", "begin print sensor z");
					for (AccSensorDataBean bean : StepAcceleroMeterSensor.arrayList) {
						StepCounterApplication.getLogUtil().d("excel-z", "" + bean.z);
					}
					StepCounterApplication.getLogUtil().d("excel", "begin print sensor average");
					for (AccSensorDataBean bean : StepAcceleroMeterSensor.arrayList) {
						StepCounterApplication.getLogUtil().d("excel-a", "" + bean.getAverageGravity());
					}
					StepCounterApplication.getLogUtil().d("excel", "begin print peak");
					for (AccSensorDataBean bean : StepAcceleroMeterSensor.arrayList) {
						if (bean.isPeak) {
							StepCounterApplication.getLogUtil().d("excel-peak", "" + bean.getAverageGravity());
						} else {
							StepCounterApplication.getLogUtil().d("excel-peak", "");
						}
					}
					StepCounterApplication.getLogUtil().d("excel", "begin print trough");
					for (AccSensorDataBean bean : StepAcceleroMeterSensor.arrayList) {
						if (bean.isTrough) {
							StepCounterApplication.getLogUtil().d("excel-trough", "" + bean.getAverageGravity());
						} else {
							StepCounterApplication.getLogUtil().d("excel-trough", "");
						}
					}
					StepCounterApplication.getLogUtil().d("excel", "begin print step count");
					for (AccSensorDataBean bean : StepAcceleroMeterSensor.arrayList) {
						if (bean.isStepCount) {
							StepCounterApplication.getLogUtil().d("excel-step", "" + bean.getAverageGravity());
						} else {
							StepCounterApplication.getLogUtil().d("excel-step", "");
						}
					}
					StepCounterApplication.getLogUtil().d("excel", "print finish");

					StepAcceleroMeterSensor.arrayList.clear();
				}
			});
			thread.start();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (null == intent) {
			return Service.START_NOT_STICKY;
		}
		StepCounterApplication.getLogUtil().v(TAG,
				"onStartCommand: intent.getaction()=" + intent.getAction() + ",startId=" + startId);

		if (Constant.ACTION_ENABLE_STEP_COUNTER.equals(intent.getAction())) {
			Message msg = mServiceHandler.obtainMessage(Constant.MSG_ENABLE_STEP_COUNTER);
			mServiceHandler.sendMessage(msg);
		} else if (Constant.ACTION_DISABLE_STEP_COUNTER.equals(intent.getAction())) {
			Message msg = mServiceHandler.obtainMessage(Constant.MSG_DISABLE_STEP_COUNTER);
			mServiceHandler.sendMessage(msg);
		}
		return Service.START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopStepCounter();
		if (null != mScreenOffReceiver) {
			unregisterReceiver(mScreenOffReceiver);
			mScreenOffReceiver = null;
		}

		// 清除托盘通知消息
		opStepNotification(false);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ServiceBinder();
	}

	/**
	 * @return 返回StepCounterService单独实例
	 */
	public StepCounterService getInstance() {
		if (null == gInstance) {
			gInstance = StepCounterService.this;
		}

		return gInstance;
	}

	public synchronized void appendMessenger(MessengerBean bean) {
		if (null != bean && !messengerTargets.containsKey(bean.getTargetHash())) {
			messengerTargets.put(bean.getTargetHash(), bean);
			
			// 添加消息句柄后立马发送一个当前step数据过去，方便更新UI
			Bundle bundle = new Bundle();
			bundle.putInt("step", getTotalSteps());
			sendUIMessage(Constant.MSG_SEND_DATA_TO_CLIENT, bundle);
		}
	}

	public synchronized void removeMessenger(MessengerBean bean) {
		if (null != bean && messengerTargets.containsKey(bean.getTargetHash())) {
			messengerTargets.remove(bean.getTargetHash());
		}
	}

	/**
	 * 计步服务是否在计步
	 * 
	 * @return true:在计步, false:在睡眠
	 */
	public boolean isCounterServiceRunning() {
		return ServiceState.COUNTER_SERVICE_IS_RUNNING == mServiceState;
	}

	private boolean updateServiceState(ServiceState state) {
		if (mServiceState != state) {
			mServiceState = state;
			
			//显示/移除通知消息
			opStepNotification(ServiceState.COUNTER_SERVICE_IS_RUNNING == mServiceState);
			
			int resId = (ServiceState.COUNTER_SERVICE_IS_RUNNING == mServiceState) ? R.string.str_toast_enable_step_counter : R.string.str_toast_disable_step_counter;
			Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_SHORT).show();

			// TODO 消息通知,更新ui消息
			sendUIMessage(Constant.MSG_STEP_COUNTER_STATE_CHANGE, null);
			return true;
		}
		return false;
	}

	/**
	 * mStep自增, 因为mStep的更新同UI、托盘消息、锁屏UI的更新息息相关，这里统一做set接口，方便消息通知
	 * 
	 * @param increaseStep
	 *            增加的步数，一般是1,特殊情况下为多步。
	 */
	private void increaseTotalSteps(int increaseStep) {
		if (increaseStep > 0) {
			mSteps += increaseStep;

			Bundle bundle = new Bundle();
			bundle.putInt("step", getTotalSteps());

			// TODO 消息通知,需要测试性能问题
			sendUIMessage(Constant.MSG_SEND_DATA_TO_CLIENT, bundle);
		}
	}
	
	/**
	 * @param msgId 发送的msg id
	 * @param data 附加包，一般就是steps值
	 */
	private void sendUIMessage(int msgId, Bundle data) {
		for (Entry<Integer, MessengerBean> entryItem : messengerTargets.entrySet()) {
			StepCounterApplication.getLogUtil().d(TAG, "send " + msgId +" to " + entryItem.getValue().getComponentName().toString());
			Messenger messenger = entryItem.getValue().getMessenger();
			
			Message msg = Message.obtain(entryItem.getValue().getTargetHandler(), msgId);
			if (null != data) {
				msg.setData(data);				
			}
			try {
				messenger.send(msg); // 发送要返回的消息
			} catch (RemoteException e) {
				StepCounterApplication.getLogUtil().d(TAG,
						"Failed for sending " + msgId + " to " + entryItem.getValue().getComponentName().toString());
			}
		}
	}

	/**
	 * mSteps get接口,为避免mSteps状态混乱，统一设置set\get接口
	 * @return 返回当天总步数
	 */
	public static int getTotalSteps() {
		return mSteps;
	}

	public class ServiceBinder extends Binder {

		public StepCounterService getStepCounterService() {
			return getInstance();
		}
	}

	/**
	 * 初始化广播
	 */
	private void enableReceiverCatcher() {
		// 定义意图过滤器
		final IntentFilter filter = new IntentFilter();
		// 屏幕灭屏广播
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		// 日期修改
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		// 修改时区
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		// 关闭广播
		filter.addAction(Intent.ACTION_SHUTDOWN);
		// 屏幕高亮广播
		filter.addAction(Intent.ACTION_SCREEN_ON);
		// 屏幕解锁广播
		filter.addAction(Intent.ACTION_USER_PRESENT);
		// 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
		// example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
		// 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
		filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

		if (null == mIntentReceiver) {
			mIntentReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					StepCounterApplication.getLogUtil().v(TAG, "onReceive:action" + action);
					if (Intent.ACTION_SCREEN_ON.equals(action)) {
					} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
						saveToStepDb(true);

						// 改为60秒一存储
						dbUpdateDuration = 60000;
					} else if (Intent.ACTION_USER_PRESENT.equals(action)) {
						saveToStepDb(true);
						// 改为30秒一存储
						dbUpdateDuration = 30000;
					} else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
						// 保存一次
						saveToStepDb(true);
					} else if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
						saveToStepDb(true);
					} else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())
							|| Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {

						// 参数isNewDay设为false，将旧数据保存到旧的一天,
						// 而且必须在initServiceData之前，否则会造成步数丢失
						saveToStepDb(false);
						initServiceData();
					}
				}
			};
			registerReceiver(mIntentReceiver, filter);
		}
	}

	private void disableReceiverCatcher() {
		if (null != mIntentReceiver) {
			unregisterReceiver(mIntentReceiver);
			mIntentReceiver = null;
		}
	}
	
	// 同步方法 得到休眠锁
	synchronized private void acquireWakeLock(Context context) {
		releaseWakeLock();
		StepCounterApplication.getLogUtil().v(TAG, "releaseWakeLock first before acquire");

		if (mWakeLock == null) {
			PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, StepCounterService.class.getName());
			mWakeLock.setReferenceCounted(false);
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis((System.currentTimeMillis()));
			int hour = c.get(Calendar.HOUR_OF_DAY);
			mWakeLock.acquire();
//			 if(hour>=23||hour<=6){
//			 mWakeLock.acquire(5000);
//			 }else{
//			 mWakeLock.acquire(300000);
//				 mWakeLock.acquire(5000);
//			 }
			StepCounterApplication.getLogUtil().v(TAG, "acquireWakeLock");
		}
	}

	// 同步方法 得到休眠锁
	synchronized private void releaseWakeLock() {
		if (mWakeLock != null) {
			if (mWakeLock.isHeld()) {
				mWakeLock.release();
				mWakeLock = null;
				StepCounterApplication.getLogUtil().v(TAG, "releaseWakeLock");
			}

		}
	}

	public class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super();
		}

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case Constant.MSG_ENABLE_STEP_COUNTER:
				enableReceiverCatcher();
				startStepCounter();
				break;
			case Constant.MSG_DISABLE_STEP_COUNTER:
				disableReceiverCatcher();
				stopStepCounter();

				// 停止计步器时，保存一次最新数据，避免下次UI刷新时出现步数倒退的情况，因为UI初始化是从数据库读取数据
				saveToStepDb(true);
				
				// opStepNotification接口只有在状态变化时才调用，这里强制调用一次，避免服务运行状态混乱后无法删除托盘消息
				opStepNotification(false);
				break;
				
			case Constant.MSG_DISPLAY_LOCKSCREEN_UI:
				StepCounterApplication.getLogUtil().d(TAG, "---------message " + msg.what + " received---------");
				PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
				boolean isScreenOn = powerManager.isInteractive();
				if (isScreenLocked()) {
					if (isCounterServiceRunning() && !isScreenOn) {
						Intent intent = new Intent(Constant.ACTION_STEP_SHOW_IN_LOCKSCREEN);
						intent.addFlags(Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
						
						PackageManager pm = getApplicationContext().getPackageManager();
						List<ResolveInfo> resolves = pm.queryIntentActivities(intent, 0);
						if (null != resolves && !resolves.isEmpty()) {
							String targetActvity = resolves.get(0).activityInfo.name;
							if (!isTopActivity(targetActvity, getApplicationContext())) {
								StepCounterApplication.getLogUtil().d(TAG, "enable lock screen display");
								startActivity(intent);
							}
						}
					}
				} else {
					StepCounterApplication.getLogUtil().d(TAG, "Screen is not Locked! send " + msg.what + " delay");
					Message message = mServiceHandler.obtainMessage(Constant.MSG_DISPLAY_LOCKSCREEN_UI);
					mServiceHandler.sendMessageDelayed(message, 500);
				}
				break;
				
			default:
				break;
			}
		}
	}
	
	/**
	 * @return 手机是否锁屏，当设置图案、数字密码时，优先返回isDeviceLocked()
	 */
	private boolean isScreenLocked() {
		boolean ret = false;
		KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		StringBuilder builder = new StringBuilder();
		if (keyguardManager.isDeviceSecure()) {
			ret = keyguardManager.isDeviceLocked();
			builder.append("Device is Secured, DeviceLocked:");
		} else {
			ret = keyguardManager.isKeyguardLocked();
			builder.append("Device is not Secured, KeyguardLocked:");
		}
		builder.append(ret);
		StepCounterApplication.getLogUtil().d(TAG, builder.toString());
		return ret;
	}

	/**
	 * 判断当前activity是否是Top Activity
	 * @param className
	 * @param context
	 * @return
	 */
	public boolean isTopActivity(String className,Context context){
	    // Get the Activity Manager
	    ActivityManager manager = (ActivityManager)context.getSystemService(context.ACTIVITY_SERVICE);

	    // Get a list of running tasks, we are only interested in the last one,
	    // the top most so we give a 1 as parameter so we only get the topmost.
	    List< ActivityManager.RunningTaskInfo > task = manager.getRunningTasks(1);

	    // Get the info we need for comparison.
	    ComponentName componentInfo = task.get(0).topActivity;
	    StepCounterApplication.getLogUtil().d(TAG, "topActivity:" + componentInfo.getClassName());
	    // Check if it matches our package name.

	    if(componentInfo.getClassName().equals(className))
	        return true;

	    // If not then our app is not on the foreground.
	    return false;
	}
	
	/**
	 * 启动数据库更新计时器
	 */
	private void startDbUpdateTimeCount() {
		// 当计步器处于睡眠时，不执行数据库更新
		if (isCounterServiceRunning()) {
			dbUpdateTimer = new SaveTimer(dbUpdateDuration, 1000);
			dbUpdateTimer.start();
		}
	}

	class SaveTimer extends CountDownTimer {
		public SaveTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {

		}

		@Override
		public void onFinish() {
			// 如果计时器正常结束，则开始计步
			dbUpdateTimer.cancel();
			saveToStepDb(true);
			startDbUpdateTimeCount();
		}
	}

	/**
	 * 保存数据
	 * 
	 * @param isNewDay
	 *            是否保存到新的一天，一般为true, 但是某些场景需要将数据保存到旧一天，比如手动设置时间、时区等
	 */
	public void saveToStepDb(boolean isNewDay) {
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		StepCounterApplication.getLogUtil().d(TAG, "saveToStepDb():" + dateFormat.format(new Date()));

		Context context = getApplicationContext();
		StepLogicManager logic = StepLogicManager.getInstance(getApplicationContext());

		// 生成target,取最后一个target记录，否则取资源default值
		StepInfoBean latestStepInfoBean = logic.getStepLogicPolicy().getLatestStepInfoBean(context);
		int target = (null == latestStepInfoBean) ? 0 : latestStepInfoBean.getTarget();

		// 只有当isNewDay为false,且可以取到上一次date时才能使用上一条旧记录的日期
		String date = (null == latestStepInfoBean || isNewDay) ? DateUtil.getTodayDate() : latestStepInfoBean.getDate();
		StepCounterApplication.getLogUtil().d(TAG, "target:" + target + "date:" + date);

		StepInfoBean bean = new StepInfoBean();
		bean.setDate(date);
		bean.setStep(getTotalSteps());
		bean.setTarget((0 < target) ? target : context.getResources().getInteger(R.integer.integer_default_tareget));

		boolean ret = logic.getStepLogicPolicy().addStepRecord(getApplicationContext(), bean);
		if (!ret) {
			StepCounterApplication.getLogUtil().e(TAG, "updateDb failed");
		}
	}

	class TimeCount extends CountDownTimer {
		/**
		 * 构造函数
		 * 
		 * @param millisInFuture
		 *            倒计时时间
		 * @param countDownInterval
		 *            倒计时时间间隔
		 */
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			if (lastSteps == tempSteps) {
				// 一段时间内，TEMP_STEP没有步数增长，则计时停止，同时计步也停止
				StepCounterApplication.getLogUtil().v(TAG, "JudgeTimer canceled:CounterService return to bed!");
				mCounterJudgeTimer.cancel();
				mStepCounterState = StepCounterState.STEP_COUNTER_DISABLED;
				lastSteps = -1;
				tempSteps = 0;
			} else {
				lastSteps = tempSteps;
			}
		}

		@Override
		public void onFinish() {

			// 如果计时器正常结束，则开始计步
			mCounterJudgeTimer.cancel();
			StepCounterApplication.getLogUtil().v(TAG, "JudgeTimer finished:CounterService begin countting!");
			increaseTotalSteps(tempSteps);
			lastSteps = -1;
			mStepCounterState = StepCounterState.STEP_COUNTER_IS_COUNTTING;

			// 正常计步开始后，需要启动正常计步的监控器
			mDetectorTaskTimer = new Timer(true);
			TimerTask task = new TimerTask() {
				public void run() {
					// 当步数不在增长的时候停止计步
					if (lastSteps == getTotalSteps()) {
						mDetectorTaskTimer.cancel();
						mStepCounterState = StepCounterState.STEP_COUNTER_DISABLED;
						lastSteps = -1;
						tempSteps = 0;
						StepCounterApplication.getLogUtil().v(TAG,
								"DetectorTaskTimer canceled:Detector stopped, CounterService return to bed!");
					} else {
						lastSteps = getTotalSteps();
					}
				}
			};
			StepCounterApplication.getLogUtil().v(TAG, "DetectorTaskTimer started:Detector begin!");
			mDetectorTaskTimer.schedule(task, 0, 2000); // 每隔两秒执行一次，不断监测是否已经停止运动了。
		}
	}
}
