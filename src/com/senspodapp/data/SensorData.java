package com.senspodapp.data;

/**
 * Generic structure to encapsulate data from a sensor device/chip.
 * Base class for all specialized sensor data objects.
 * 
 * @author janos
 *
 */
public abstract class SensorData {
	/**
	 * The original sentence the object was created from.
	 */
	public String sentence = null;
	
	/**
	 * Initialize SensorData object from sentence string.
	 * @param sentence
	 */
	public abstract void initFromSentence(String sentence);
	
	/**
	 * Return the simple name of the class, for example "Co2Data".
	 * @return
	 */
	public final String getName() {
		return getClass().getSimpleName();
	}

	/**
	 * Level indicator, when applicable.
	 * Calculated from the main measurement value.
	 */
	public int level = 0;
	
	/**
	 * Defines the boundaries of levels. 
	 * Set to null when not applicable.
	 */
	protected static float[] levelboundaries = null;

	/**
	 * Re-calculate level based on levelboundaries and the main measurement value.
	 */
	protected void setLevel(float val) {
		for (level = 0; level < levelboundaries.length; ++level) {
			if (val < levelboundaries[level]) break;
		}
	}
}