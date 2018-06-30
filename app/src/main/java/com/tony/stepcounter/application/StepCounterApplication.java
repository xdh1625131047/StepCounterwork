package com.tony.stepcounter.application;

import com.tony.stepcounter.base.BaseApplication;
import com.tony.stepcounter.utils.PropertyUtil;
import com.tony.stepcounter.utils.StepLogUtil;

import android.app.KeyguardManager;
import android.content.Context;

public class StepCounterApplication extends BaseApplication {
	
	private static KeyguardManager.KeyguardLock gKeyguardLock = null;
	public static final String TAG = "StepCounterApplication";
	
	static Context mAppCxt;
	private static StepLogUtil gLog;
	@Override
	public void onCreate() {
		super.onCreate();
		mAppCxt = this;
		gLog = StepLogUtil.getInstance(mAppCxt, isDebugMode());
		
		KeyguardManager keyguardManager = (KeyguardManager) mAppCxt.getSystemService(Context.KEYGUARD_SERVICE);
		gKeyguardLock = keyguardManager.newKeyguardLock("StepLockScreen");
	}
	
	@Override
	public Context getApplicationContext() {
		return mAppCxt;
	}
	
	/**
	 * @return 调试开关是否打开
	 */
	public static boolean isDebugMode() {
		String debug = PropertyUtil.getProperty("tony.stepcounter.debug", "1");
		return (1 == Integer.parseInt(debug));
	}
	
	public static StepLogUtil getLogUtil() {
		if (null == gLog) {
			gLog = StepLogUtil.getInstance(mAppCxt, isDebugMode());
		}
		return gLog;
	}
	
	public static void enableKeyguardLock() {
		if (null != gKeyguardLock) {
			StepCounterApplication.getLogUtil().d(TAG, "enableKeyguardLock");
			gKeyguardLock.reenableKeyguard();
		}
	}
	
	public static void disableKeyguardLock() {
		if (null != gKeyguardLock) {
			StepCounterApplication.getLogUtil().d(TAG, "disableKeyguardLock");
			gKeyguardLock.disableKeyguard();
		}
	}
}
