package com.ecocitizen.app.test;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;

import com.ecocitizen.service.DeviceManagerService;
import com.ecocitizen.service.IDeviceManagerService;

public class ServiceSanityTest extends ServiceTestCase<DeviceManagerService> {

	public ServiceSanityTest(Class<DeviceManagerService> serviceClass) {
		super(serviceClass);
	}
	
	public ServiceSanityTest() {
		this(DeviceManagerService.class);
	}
	
	public void testSanity() throws Exception {
		this.testServiceTestCaseSetUpProperly();
	}
	
	public void testSentences1() throws Exception {
		IBinder binder = bindService(new Intent(IDeviceManagerService.class.getName()));
		assertNotNull(binder);
		//IDeviceManagerService service = IDeviceManagerService.Stub.asInterface(binder);
		
		// Sadly, this does not work.
		// An NPE is thrown in android.os.RemoteCallbackList.register
		/*
		service.registerCallback(new IDeviceManagerServiceCallback() {
			public void receivedAllDevicesGone() throws RemoteException {
				// TODO Auto-generated method stub
			}

			public void receivedConnectionFailed(String deviceName)
					throws RemoteException {
				// TODO Auto-generated method stub
			}

			public void receivedDeviceAdded(String deviceName)
					throws RemoteException {
				// TODO Auto-generated method stub
			}

			public void receivedDeviceClosed(String deviceName)
					throws RemoteException {
				// TODO Auto-generated method stub
			}

			public void receivedDeviceLost(String deviceName)
					throws RemoteException {
				// TODO Auto-generated method stub
			}

			public void receivedNoteBundle(Bundle bundle)
					throws RemoteException {
				// TODO Auto-generated method stub
			}

			public void receivedSentenceBundle(Bundle bundle)
					throws RemoteException {
				// TODO Auto-generated method stub
			}

			public IBinder asBinder() {
				// TODO Auto-generated method stub
				return null;
			}
		});
		*/
	}

}
