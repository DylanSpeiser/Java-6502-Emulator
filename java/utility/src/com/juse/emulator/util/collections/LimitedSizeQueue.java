package com.juse.emulator.util.collections;

import java.util.ArrayList;

public class LimitedSizeQueue<K> extends ArrayList<K> {

    @Override
	public synchronized int size()
	{
		// TODO Auto-generated method stub
		return super.size();
	}

	private int maxSize;

    public LimitedSizeQueue(int size){
        this.maxSize = size;
    }

    public synchronized boolean add(K k){
        boolean r = super.add(k);
        if (size() > maxSize){
            removeRange(0, size() - maxSize);
        }
        return r;
    }

    public K getYoungest() {
        return get(size() - 1);
    }

    public K getOldest() {
        return get(0);
    }
}