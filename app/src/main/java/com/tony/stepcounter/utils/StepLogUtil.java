package com.tony.stepcounter.utils;

import android.content.Context;
import android.util.Log;

public class StepLogUtil {
	
	static StepLogUtil instance;
	private static String TAG = "StepCounter";
	private static boolean isSwitchOn = false;
	
	public static StepLogUtil getInstance(Context context, boolean isDebug) {
		if (null == instance) {
			instance = new StepLogUtil();
		}
		isSwitchOn = isDebug;
		return instance;
	}
	
    
    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public void v(String tag, String msg) {
    	if (isSwitchOn)
    		Log.v(TAG, tag + ", " + msg);
    }

    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     * @param t an exception to log.
     */
    public void v(String tag, String msg, Throwable t) {
    	if (isSwitchOn)
    		Log.v(TAG, tag + ", " + msg, t);
    }
    
    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public void i(String tag, String msg) {
    	if (isSwitchOn)
    		Log.i(TAG, tag + ", " + msg);
    }

    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     * @param t an exception to log.
     */
    public void i(String tag, String msg, Throwable t) {
    	if (isSwitchOn)
    		Log.i(TAG, tag + ", " + msg, t);
    }
    
    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public void d(String tag, String msg) {
    	if (isSwitchOn)
    		Log.d(TAG, tag + ", " + msg);
    }

    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     * @param t an exception to log.
     */
    public void d(String tag, String msg, Throwable t) {
    	if (isSwitchOn)
    		Log.d(TAG, tag + ", " + msg, t);
    }
    
    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public void e(String tag, String msg) {
    	if (isSwitchOn)
    		Log.e(TAG, tag + ", " + msg);
    }

    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     * @param t an exception to log.
     */
    public void e(String tag, String msg, Throwable t) {
    	if (isSwitchOn)
    		Log.e(TAG, tag + ", " + msg, t);
    }
    
    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public void w(String tag, String msg) {
    	if (isSwitchOn)
    		Log.w(TAG, tag + ", " + msg);
    }

    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     * @param t an exception to log.
     */
    public void w(String tag, String msg, Throwable t) {
    	if (isSwitchOn)
    		Log.w(TAG, tag + ", " + msg, t);
    }
}
