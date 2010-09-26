package com.senspodapp.framework.consumers;

import com.senspodapp.framework.SensorDataBundle;

import android.os.Handler;
import android.os.Message;

public abstract class SensorDataConsumerBase implements SensorDataConsumer {
	abstract void handleSensorDataBundle(SensorDataBundle bundle);
	
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] buf = (byte[]) msg.obj;
            handleSensorDataBundle(new SensorDataBundle(buf));
        }
    };

    public void consumeSensorDataBundle(SensorDataBundle bundle) {
    	byte[] buf = bundle.toByteArray();
		Message msg = mHandler.obtainMessage(1, buf.length, -1, buf);
		msg.sendToTarget();
	}

}
