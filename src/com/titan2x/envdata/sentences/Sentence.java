package com.titan2x.envdata.sentences;

import java.io.Serializable;

public class Sentence implements Serializable {
	private static final long serialVersionUID = 7750977717812396992L;
	
	public String str;

	public Sentence(String str) {
		this.str = str;
	}
}
