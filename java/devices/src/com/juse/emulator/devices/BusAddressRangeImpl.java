package com.juse.emulator.devices;

import com.juse.emulator.interfaces.BusAddressRange;

public class BusAddressRangeImpl implements BusAddressRange
{
	private int highAddress = 0;
	private int lowAddress  = 0;
	
	public BusAddressRangeImpl(int lowAddress, int highAddress)
	{
		this.lowAddress = lowAddress;
		this.highAddress = highAddress;
	}

	public BusAddressRangeImpl(int startAddress, int size, int alignmentBytes)
	{
		this.lowAddress = startAddress;
		
		
		if((size % alignmentBytes)!=0)
		{
			int adjust = (size % alignmentBytes) - 1;
			size+=adjust;			
		}
		if((size % 2)!=0)
		{
			size+=1;					
		}			
		
		this.highAddress = this.lowAddress + size - 1;

	}

	public int getSize()
	{
		return this.highAddress - this.lowAddress + 1;
	}

	
	public int getSizeBytes()
	{
		return getSize();
	}
	
	public int getSizeKB()
	{
		return getSizeBytes()/1024;
	}

	public int getSizeMB()
	{
		return getSizeKB()/1024;
	}	
	
	public int getHighAddress()
	{
		return highAddress; 		
	}
	
	public int getLowAddress()
	{
		return lowAddress; 		
	}
	
	public int getRelativeAddress(int address)
	{
		return address - this.getLowAddress();
	}

	
	public String getHighAddressHex()
	{
		return BusAddressRange.makeHexAddress(this.getHighAddress()); 		
	}
	
	public String getLowAddressHex()
	{
		return BusAddressRange.makeHexAddress(this.getLowAddress()); 		
	}
	
	
	public static void main(String[] args)
	{
		System.out.println("BusAddressRange Test");
		
		BusAddressRange bar1 = new BusAddressRangeImpl(0x0000,17, 1);

		System.out.println(bar1.getHighAddress() + ":" +  Integer.toHexString(bar1.getHighAddress()));

		BusAddressRange bar2 = new BusAddressRangeImpl(0x0000,15, 2);

		System.out.println(bar2.getHighAddress() + ":" +  Integer.toHexString(bar2.getHighAddress()));		

		BusAddressRange bar3 = new BusAddressRangeImpl(0x0000,10, 4);

		System.out.println(bar3.getHighAddress() + ":" +  Integer.toHexString(bar3.getHighAddress()));			
		
		
		int size = 1024 * 8;

		BusAddressRange bar8 = new BusAddressRangeImpl(0x0000,256,1);
		System.out.println("[" + bar8.getLowAddressHex() + ":" + bar8.getHighAddressHex()  + "]" +  
		                   bar8.getSizeBytes() + "/" + bar8.getSizeKB() + "/" + bar8.getSizeMB());			
		
		BusAddressRange bar8k = new BusAddressRangeImpl(0x0000,size,1);
		System.out.println("[" + bar8k.getLowAddressHex() + ":" + bar8k.getHighAddressHex()  + "]" +
		                    bar8k.getSizeBytes() + "/" + bar8k.getSizeKB() + "/" + bar8k.getSizeMB());	


		BusAddressRange bar64k = new BusAddressRangeImpl(0x0000,1024*64,1);
		System.out.println("[" + bar64k.getLowAddressHex() + ":" + bar64k.getHighAddressHex()  + "]" +
				           bar64k.getSizeBytes() + "/" + bar64k.getSizeKB() + "/" + bar64k.getSizeMB());	
		
		
		BusAddressRange bar8m = new BusAddressRangeImpl(0x0000,size * 1024,1);
		System.out.println("[" + bar8m.getLowAddressHex() + ":" + bar8m.getHighAddressHex()  + "]" +
		                   bar8m.getSizeBytes() + "/" + bar8m.getSizeKB() + "/" + bar8m.getSizeMB());	
		
		
	}
	
}

