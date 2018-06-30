package com.tony.stepcounter.ui;

import com.tony.stepcounter.R;
import com.tony.stepcounter.constants.Constant;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class CustomDialog extends Dialog implements OnClickListener{

	private Context mContext;
	private DialogButtonClickListener listener;
	private int mId;
	private Button btnFirst;
	private Button btnSecond;
	private EditText edit;
	private int layoutId;
	
	public CustomDialog(Context context, int id, int layout) {
		super(context);
		this.mContext = context;
		this.mId = id;
		this.layoutId = layout;
	}
	
	public void setListener(DialogButtonClickListener listener) {
		this.listener = listener;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		setContentView(this.layoutId);
		if (Constant.DIALOG_ID_SET_TARGET == mId) {
			btnFirst = (Button)findViewById(R.id.dialog_set_target_confirm_btn);
			btnSecond = (Button)findViewById(R.id.dialog_set_target_cancel_btn);			
		} else if (Constant.DIALOG_ID_STOP_STEP_COUNTER == mId){
			btnFirst = (Button)findViewById(R.id.dialog_stop_stepcounter_confirm_btn);			
			btnSecond = (Button)findViewById(R.id.dialog_stop_stepcounter_cancel_btn);
		} else {
			return;
		}
		
		edit = (EditText)findViewById(R.id.edit_target);
		if (null != edit) {
			edit.setInputType(InputType.TYPE_CLASS_NUMBER);
		}
		
		if (null != btnFirst) {
			btnFirst.setOnClickListener(this);
		}
		
		if (null != btnSecond) {
			btnSecond.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View view) {
		if (null == listener) {
			return;
		}
		
		if (R.id.dialog_set_target_confirm_btn == view.getId() 
				|| R.id.dialog_stop_stepcounter_confirm_btn == view.getId()) {
			listener.onFirstButtonClick();
		} else if  (R.id.dialog_set_target_cancel_btn == view.getId() 
				|| R.id.dialog_stop_stepcounter_cancel_btn == view.getId()) {
			listener.onSecondButtonClick();
		} else {
			// nothing happened
		}
	}

	public void setEditHint(int resId, String message) {
		EditText edit = (EditText)findViewById(resId);
		if (null != edit) {
			edit.setHint(message);
		}
		
	}
}
