package com.tony.stepcounter.bean;

import android.content.ComponentName;
import android.os.Handler;
import android.os.Messenger;

/**
 * 消息句柄对象
 * @author lantian
 */
public class MessengerBean {

	/**
	 * UI界面Component
	 */
	private ComponentName componentName;

	/**
	 * component name 相应的hash值，可针对每个activity
	 */
	private int targetHash;

	private Messenger messenger;
	/**
	 * 消息句柄
	 */
	private Handler targetHandler;

	public MessengerBean(ComponentName cn, Handler handler) {
		this.componentName = cn;
		this.targetHash = cn.hashCode();
		this.messenger = new Messenger(handler);
		this.targetHandler = handler;
	}

	public Handler getTargetHandler() {
		return targetHandler;
	}

	public int getTargetHash() {
		return targetHash;
	}

	public Messenger getMessenger() {
		return messenger;
	}

	public ComponentName getComponentName() {
		return componentName;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof MessengerBean)) {
			return false;
		}
		final MessengerBean bean = (MessengerBean)obj;
		if(this.targetHash != bean.targetHash) {
			return false;
		}
		if(!this.targetHandler.equals(bean.targetHandler)) {
			return false;
		}
		if(!this.messenger.equals(bean.messenger)) {
			return false;
		}
		if(!this.componentName.equals(bean.componentName)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + targetHash;
		result = prime * result + ((null == targetHandler) ? 0 : targetHandler.hashCode());
		result = prime * result + ((null == messenger) ? 0 : messenger.hashCode());
		result = prime * result + ((null == componentName) ? 0 : componentName.hashCode());
		return result;
	}
}