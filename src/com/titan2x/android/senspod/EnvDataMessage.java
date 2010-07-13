package com.titan2x.android.senspod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import com.titan2x.envdata.sentences.CO2Sentence;
import com.titan2x.envdata.sentences.GPRMCSentence;
import com.titan2x.envdata.sentences.Sentence;

public class EnvDataMessage {
	final static int version = 1;
	
	public Sentence sentence = null;
	public GPRMCSentence gprmc = null;
	public CO2Sentence co2 = null;
	
	public EnvDataMessage() {
	}
	
	public EnvDataMessage(byte[] bytes) {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		try {
			ObjectInputStream ois = new ObjectInputStream(bais);
			ois.readInt(); // version, for future use
			sentence = (Sentence)ois.readObject();
			gprmc = (GPRMCSentence)ois.readObject();
			co2 = (CO2Sentence)ois.readObject();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] toByteArray() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeInt(version);
			oos.writeObject(sentence);
			oos.writeObject(gprmc);
			oos.writeObject(co2);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return baos.toByteArray();
	}

}
