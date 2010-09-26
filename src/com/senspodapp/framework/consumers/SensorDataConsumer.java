package com.senspodapp.framework.consumers;

import com.senspodapp.framework.SensorDataBundle;

/**
 * Generic interface for a SensorDataConsumer.
 * 
 * @author janos
 *
 */
public interface SensorDataConsumer {
	/**
	 * This method should return very quickly without blocking.
	 * Implementations should use messages to dispatch the received message.
	 * 
	 * @param bundle
	 */
	public void consumeSensorDataBundle(SensorDataBundle bundle);
}
