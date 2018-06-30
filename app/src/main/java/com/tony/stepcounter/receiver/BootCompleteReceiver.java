package com.tony.stepcounter.receiver;

import com.tony.stepcounter.service.StepCounterService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 开机完成广播
 */

public class BootCompleteReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent){
        Intent i=new Intent(context,StepCounterService.class);
        context.startService(i);
    }
}
