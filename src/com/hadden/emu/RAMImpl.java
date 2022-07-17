package com.hadden.emu;

import com.hadden.ROMLoader;

public class RAMImpl implements  RAM
{
	
    static  int CONST_RAM_SIZE = 0xFFFF;
	
	private byte[] array;
	public String RAMString = "";
	
	public RAMImpl() {
		array = new byte[CONST_RAM_SIZE];
		for (int i = 0; i<CONST_RAM_SIZE; i++) {
			array[i] = (byte)0x00;
		}
		RAMString = this.toString(8, true);
	}
	
	public RAMImpl(byte[] theArray) {
		array = theArray;
		RAMString = this.toString(8, true);
	}

	public byte[] getRAMArray() {
		return array;
	}

	public void reset()
	{
		for(int p=0;p<this.array.length;p++)
			this.array[p] = 0x00;		
	}
	
	public void setRAMArray(byte[] array) 
	{
		//this.array = array;
		for(int p=0;p<this.array.length;p++)
			this.array[p] = 0x00;
		
		for(int p=0;p<array.length;p++)
			this.array[p] = array[p];
		
		RAMString = this.toString(8, true);
	}
	
	public byte read(short address) {
		if(address > 0x1000)
			System.out.println("HIMEM:" + Integer.toHexString(address));
		return array[Short.toUnsignedInt(address)];
	}
	
	public void write(short address, byte data) {
		array[Short.toUnsignedInt(address)] = data;
		RAMString = this.toString(8, true);
	}
	
	public String getRAMString()
	{
		return  this.toString(8, true);
	}
	
	public String toString(int bytesPerLine, boolean addresses) {
		StringBuilder sb = new StringBuilder();
		
		if (addresses)
			sb.append("0000: ");
		
		for (int i = 1; i <= array.length; i++) {
			if ((i%bytesPerLine != 0) || (i == 0)) {
				sb.append(ROMLoader.byteToHexString(array[i-1])+" ");
			} else {
				String zeroes = "0000";
				sb.append(ROMLoader.byteToHexString(array[i-1])+"\n");
				if (addresses)
					sb.append(zeroes.substring(0, Math.max(0, 4-Integer.toHexString(i).length()))+Integer.toHexString(i)+": ");
			}
		}
		
		return sb.toString();
	}
}
