package com.ecocitizen.common;

import android.os.Bundle;
import android.os.Handler;

import com.ecocitizen.service.IDeviceManagerServiceCallback;

public class DeviceManagerServiceCallback extends
		IDeviceManagerServiceCallback.Stub {
	
	private final Handler mHandler;

	public DeviceManagerServiceCallback(Handler handler) {
		mHandler = handler;
	}
	
	/**
	 * Note that IPC calls are dispatched through a thread
	 * pool running in each process, so the code executing here will
	 * NOT be running in our main thread like most other things -- so,
	 * to update the UI, we need to use a Handler to hop over there.
	 */
	public void receivedSentenceBundle(Bundle bundle) {
		mHandler.obtainMessage(MessageType.SENTENCE, bundle).sendToTarget();
	}

	public void receivedNoteBundle(Bundle bundle) {
		mHandler.obtainMessage(MessageType.NOTE, bundle).sendToTarget();
	}

	public void receivedConnectionFailed(String deviceName) {
		mHandler.obtainMessage(MessageType.SM_CONNECTION_FAILED, deviceName).sendToTarget();
	}

	public void receivedDeviceAdded(String deviceName) {
		mHandler.obtainMessage(MessageType.SM_DEVICE_ADDED, deviceName).sendToTarget();
	}

	public void receivedDeviceClosed(String deviceName) {
		mHandler.obtainMessage(MessageType.SM_DEVICE_CLOSED, deviceName).sendToTarget();
	}

	public void receivedDeviceLost(String deviceName) {
		mHandler.obtainMessage(MessageType.SM_DEVICE_LOST, deviceName).sendToTarget();
	}

	public void receivedAllDevicesGone() {
		mHandler.obtainMessage(MessageType.SM_ALL_DEVICES_GONE).sendToTarget();
	}
}
