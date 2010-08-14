package com.titan2x.android.senspod;

import java.io.InputStream;
import java.util.Formatter;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.titan2x.envdata.sentences.CO2Sentence;
import com.titan2x.envdata.sentences.GPRMCSentence;

/**
 * This class is responsible for uploading measurements to the *sensormap*.
 * When the *sensormap* is unreachable, measurements should be stored
 * in a queue, to be sent when the *sensormap* becomes reachable again.
 */
public class SensormapUploaderService {
	// Debugging
	public static final String TAG = "SensormapUploaderService";
	public static final boolean D = true;
	
	// Constants
	public static final int HTTP_STATUS_OK = 200;

	// Todo: it would be good to get this from a properties file
	public static final String SENSORMAP_BASE_URL = "http://10.0.2.2:8000/api/"; 
	public static final String SENSORMAP_STATUS_URL = SENSORMAP_BASE_URL + "status/";
	public static final String SENSORMAP_LOGIN_URL = SENSORMAP_BASE_URL + "login/";
	public static final String SENSORMAP_STORE_URL = SENSORMAP_BASE_URL + "store/";
	
	// Member variables
	// Todo: make these configurable in the App
	public String username = "janos";
	public String sensorId = "dummy1";
	// Todo: make this dynamic, depending on the data available from the device
	public String formatstr = "AndroidGPS,GPRMC,co2";
	
	private int sessionId = 0;
	
	private int maxQueueSize = (int)1e+5; // 0.1kb * 1e+5 ~~ 10mb 
	private Vector<String> queue = new Vector<String>(maxQueueSize / 10);

	// The number of messages to send at once
	// Todo: make this configurable in the App
	private int bufferSize = 5;
		
	private QueueProcessorThread mQueueProcessorThread;
	
	public SensormapUploaderService() {
		start();
	}
	
	private boolean hasCapacity() {
		return queue.size() < maxQueueSize;
	}

	public void received_GPRMC_CO2(GPRMCSentence gprmc, CO2Sentence co2) {
		if (! hasCapacity()) return;
		
		Formatter formatter = new Formatter();
		String item = formatter.format(
				"%s,%f,%f,%f,%f,%f", 
				gprmc.datetimeSTR, 
				0.,
				0.,
				gprmc.latitude,
				gprmc.longitude,
				co2.ppm
				).toString();
		queue.add(item);
	}
	
	public synchronized void start() {
		if (D) Log.d(TAG, "start");
		
		if (mQueueProcessorThread == null) {
			mQueueProcessorThread = new QueueProcessorThread();
			mQueueProcessorThread.start();
		}
	}
	
    /**
     * Stop all threads
     */
    public void stopAllThreads() {
    	mQueueProcessorThread.shutdown();
    	if (mQueueProcessorThread != null) {
    		mQueueProcessorThread.cancel();
    		mQueueProcessorThread = null;
    	}
    }

    
    /**
     * Stop all threads
     */
    public void stop() {
        if (D) Log.d(TAG, "stop");
        stopAllThreads();
    }
    
	
	public static boolean isSensormapReachable() {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(SENSORMAP_STATUS_URL);
        
        try {
        	HttpResponse response = client.execute(request);
        	StatusLine status = response.getStatusLine();
        	return status.getStatusCode() == HTTP_STATUS_OK;
        }
        catch (Exception e) {
        	e.printStackTrace();
        	return false;
        }
	}
	
	public int getIntResponse(String url) {
		if (D) Log.d(TAG, url);
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        
        try {
         	HttpResponse response = client.execute(request);
        	StatusLine status = response.getStatusLine();
        	if (status.getStatusCode() != HTTP_STATUS_OK) return -1;

        	HttpEntity entity = response.getEntity();
			InputStream istream = entity.getContent();
			byte[] buf = new byte[100];
			int bytes_read = istream.read(buf);
			char[] charbuf = new char[bytes_read];
			for (int i = 0; i < bytes_read; ++i) charbuf[i] = (char)buf[i];
			String intstr = String.valueOf(charbuf);
			return Integer.valueOf(intstr);
        }
        catch (Exception e) {
        	e.printStackTrace();
        	return -1;
        }
	}
	
	public boolean login(String username, String sensor_id, String formatstr) {        
		sessionId = getIntResponse(SENSORMAP_LOGIN_URL + username + "/" + sensor_id + "/" + formatstr);
		return sessionId > 0;
	}
	
	public boolean store(String data) {
		return getIntResponse(SENSORMAP_STORE_URL + sessionId + "/" + data) == 0;
	}

	private class QueueProcessorThread extends Thread {
		private boolean stop = false;

		public QueueProcessorThread() {
			
		}
		
		public void shutdown() {
			stop = true;
		}
		
		public void cancel() {
			
		}
		
		public void run() {
			Log.i(TAG, "BEGIN mQueueProcessorThread");
			setName("QueueProcessorThread");
			
			sessionId = 0;
						
			while (! stop) {
				if (! queue.isEmpty()) {
					if (isSensormapReachable()) {
						if (! (sessionId > 0)) {
							if (! login(username, sensorId, formatstr)) {
								Log.e(TAG, "login ERR");
								break;
							}
						}
					}
					else {
						try {
							Thread.sleep(10000);
						}
						catch (Exception e) {
							break;
						}
						continue;
					}
					
					if (D) Log.d(TAG, "queue size = " + queue.size());

					while (queue.size() > 0) {
						synchronized (queue) {
							String item = queue.elementAt(0);
							if (! store(item)) return;
							queue.remove(0);
						}
					}
				}
				
				try {
					Thread.sleep(10000);
				}
				catch (Exception e) {
					break;
				}
			}
		}		
	}
}
