package com.tony.stepcounter.constants;

/**
 * @author lantian
 *
 */
public class Constant {
    
	public static final String PREFERENCE_FILE = "preference-steps";
	public static final String IS_FIRST_RUNNING = "is_first_running";
    public static final int MSG_BASE = 0x100;

    /** UI发送target messenger句柄到service, 当数据更新时，service再通过target发送数据到UI更新显示*/
    public static final int MSG_ADD_TARGET_TO_SERVER = MSG_BASE + 1;
    /** UI销毁target messenger句柄，一般在不需要数据或者退出时调用 */
    public static final int MSG_REMOVE_TARGET_TO_SERVER = MSG_BASE + 2;
    /** service有数据更新时，发送此消息到UI */
    public static final int MSG_SEND_DATA_TO_CLIENT = MSG_BASE + 3;
    /** step counter service 状态变化*/
    public static final int MSG_STEP_COUNTER_STATE_CHANGE = MSG_BASE + 4;
    
    /** 打开计步器消息，StepCounterService内部使用*/
    public static final int MSG_ENABLE_STEP_COUNTER = MSG_BASE + 5;
    /** 关闭计步器消息，StepCounterService内部使用*/
    public static final int MSG_DISABLE_STEP_COUNTER = MSG_BASE + 6;
    /** 显示锁屏UI消息*/
    public static final int MSG_DISPLAY_LOCKSCREEN_UI = MSG_BASE + 7;
    
    /** 设定当前锻炼目标对话框ID*/
    public static final int DIALOG_ID_SET_TARGET = 0x1001;
    
    /** 停止计步对话框ID*/
    public static final int DIALOG_ID_STOP_STEP_COUNTER = 0x1002;
    
    public static final int NOTIFICATION_STEP_COUNTER_ENABLED = 0x10001;
    
    /** 主界面action*/
    public static final String ACTION_SHOW_STEP_COUNTER = "android.intent.action.SHOW_STEP_COUNTER";
    
    public static final String ACTION_ENABLE_STEP_COUNTER = "com.tony.intent.action.ENABLE_STEP_COUNTER";
    public static final String ACTION_DISABLE_STEP_COUNTER = "com.tony.intent.action.DISABLE_STEP_COUNTER";
    public static final String ACTION_STEP_SHOW_IN_LOCKSCREEN = "com.tony.intent.action.STEP_SHOW_IN_LOCKSCREEN";
}
