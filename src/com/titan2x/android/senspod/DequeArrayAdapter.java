package com.titan2x.android.senspod;

import java.util.Queue;

import android.content.Context;
import android.widget.ArrayAdapter;

public class DequeArrayAdapter<T> extends ArrayAdapter<T> {
	private Queue<T> queue;
	private int max_size = 3;
	
	public DequeArrayAdapter(Queue<T> queue, Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.queue = queue;
	}
	
	@Override
	public void add(T object) {
		while (queue.size() > max_size - 1) {
			super.remove(queue.poll());
		}
		queue.add(object);
		super.add(object);
	}

	@Override
	public void clear() {
		queue.clear();
		super.clear();
	}

	@Override
	/**
	 * Not supported, yet.
	 */
	public void insert(T object, int index) {
		//super.insert(object, index);
	}

	@Override
	/**
	 * Not supported, yet.
	 */
	public void remove(T object) {
		//super.remove(object);
	}
	
	 
}
