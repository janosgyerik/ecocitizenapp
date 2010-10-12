package com.titan2x.android.senspod;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.location.Location;
import android.util.Log;

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
    public static final SimpleDateFormat dateformatter = new SimpleDateFormat("yyyyMMddHHmmss.S");
    public static final int HTTP_STATUS_OK = 200;
	public static final int QUEUE_CYCLE_SLEEP = 3000; // 3 seconds
	public static final int QUEUE_NOSENSORMAP_SLEEP = 30000;
	public static final int QUEUE_LOGINERROR_SLEEP = 10000;
	public static final int QUEUE_STOREERROR_SLEEP = 10000;
	
	final String SENSORMAP_STATUS_URL;
	final String SENSORMAP_STARTSESSION_URL;
	final String SENSORMAP_STORE_URL;
	final String SENSORMAP_ENDSESSION_URL;
	
	// Member variables
	public String username;
	public String sensorId;
	
	private int sessionId = 0;
	
	private int maxQueueSize = (int)1e+5; // 0.1kb * 1e+5 ~~ 10mb 
	private Vector<String> queue = new Vector<String>(maxQueueSize / 10);

	private QueueProcessorThread mQueueProcessorThread;
	
	public SensormapUploaderService(String username, String map_server_url, String sensorId) {
		this.username = username;
		this.sensorId = sensorId;
		SENSORMAP_STATUS_URL = map_server_url + "status/";
		SENSORMAP_STARTSESSION_URL = map_server_url + "startsession/";
		SENSORMAP_STORE_URL = map_server_url + "store/";
		SENSORMAP_ENDSESSION_URL = map_server_url + "endsession/";
		start();
	}
	
	private boolean hasCapacity() {
		return queue.size() < maxQueueSize;
	}
	
	private int loc_id = 0;

	public void receivedSentence(String sentence, Location lastLocation, Date lastLocationDate) {
		if (! hasCapacity()) return;
		
		++loc_id;
		
		Formatter formatter = new Formatter();
		String format = "%s,AndroidGps,%d,%f,%f,%f,%f,%f,ENDGPS,%s,%s,ENDSENTENCE";
		String item;
		if (lastLocation == null) {
			item = formatter.format(
					format,
					dateformatter.format(lastLocationDate),
					0, 0f, 0f, 0f, 0f, 0f,
					dateformatter.format(new Date()),
					sentence
			).toString();
		}
		else {
			item = formatter.format(
					format,
					dateformatter.format(lastLocationDate),
					loc_id,
					lastLocation.getLatitude(),
					lastLocation.getLongitude(),
					lastLocation.getAccuracy(),
					lastLocation.getAltitude(),
					lastLocation.getSpeed(),
					dateformatter.format(new Date()),
					sentence
			).toString();
		}
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
    
	
	public boolean isSensormapReachable() {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(SENSORMAP_STATUS_URL);
        if (D) Log.d(TAG, SENSORMAP_STATUS_URL);
        
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
	
	public boolean startsession(String username, String sensor_id) {        
		sessionId = getIntResponse(SENSORMAP_STARTSESSION_URL + username + "/" + sensor_id);
		if (sessionId > 0) return true;
		Log.d(TAG, "startsession returned " + sessionId);
		return false;
	}
	
	public boolean store(String data) {
		int ret = getIntResponse(SENSORMAP_STORE_URL + sessionId + "/" + data.replace(" ", "")); //URLEncoder.encode(data));
		if (ret == 0) return true;
		Log.d(TAG, "store returned " + ret);
		return false;
	}

	public void endsession() {
		getIntResponse(SENSORMAP_ENDSESSION_URL + sessionId);
		Log.d(TAG, "endsession DONE");
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
			if (D) Log.i(TAG, "BEGIN mQueueProcessorThread");
			setName("QueueProcessorThread");
			
			sessionId = 0;
						
			while (! stop) {
				try {
					if (! queue.isEmpty()) {
						if (isSensormapReachable()) {
							if (! (sessionId > 0)) {
								if (! startsession(username, sensorId)) {
									if (D) Log.e(TAG, "login ERR");
									Thread.sleep(QUEUE_LOGINERROR_SLEEP);
									continue;
								}
							}
						}
						else {
							if (D) Log.e(TAG, "sensormap UNREACHABLE");
							Thread.sleep(QUEUE_NOSENSORMAP_SLEEP);
							continue;
						}

						if (D) Log.d(TAG, "queue size = " + queue.size());

						boolean was_store_error = false;
						
						while (queue.size() > 0) {
							synchronized (queue) {
								String item = queue.elementAt(0);
								if (! store(item)) {
									was_store_error = true;
									break;
								}
								queue.remove(0);
							}
						}
						
						if (was_store_error) {
							if (D) Log.e(TAG, "store ERR");
							Thread.sleep(QUEUE_STOREERROR_SLEEP);
							continue;
						}
					}

					Thread.sleep(QUEUE_CYCLE_SLEEP);
				}
				catch (InterruptedException e) {
					// ignore it 
				}
			}
		}		
	}
}
