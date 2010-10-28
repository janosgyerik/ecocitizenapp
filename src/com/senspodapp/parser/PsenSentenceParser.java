package com.senspodapp.parser;

public class PsenSentenceParser {
	String metric;
	float floatValue;
	String strValue;
	String name;
	int[] levelBoundaries = new int[]{};
    int level;
    
    String pattern = "$PSEN,";
    
    public PsenSentenceParser(String pattern) {
    	this.pattern = pattern;
    }
    
    void setLevel() {
        for (level = 0; level < levelBoundaries.length; ++level) {
            if (floatValue < levelBoundaries[level]) break;
        }
    }
	
	void reset() {
		metric = null;
		floatValue = Float.NaN;
		strValue = null;
		level = 0;
		name = null;
	}
	
	public PsenSentenceParser() {
		reset();
	}
	
	public boolean match(String line) {
		reset();
		int dataStartIndex = line.indexOf(pattern);
		if (dataStartIndex > -1) {
			String[] cols = line.substring(dataStartIndex).split(",");
			if (cols.length < 4)
				return false;
			name = cols[1];
			metric = cols[2];
			try {
				floatValue = Float.parseFloat(cols[3]);
				strValue = String.valueOf(floatValue);
			} catch (NumberFormatException  e) {
				strValue = String.valueOf(cols[3]);
			}
			setLevel();
			return true;
		} else {
			return false;
		}
	}
	
	public String getMetric() {
		return metric;
	}
	
	public float getFloatValue() {
		return floatValue;
	}
	
	public String getStrValue() {
		return strValue;
	}
	
	public int getLevel() {
		return level;
	}
	
	public String getName() {
		return name;
	}
}
