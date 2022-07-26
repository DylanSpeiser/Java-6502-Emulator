package com.hadden.emu;


import java.util.NavigableMap;
import java.util.TreeMap;

import com.hadden.ROMLoader;
import com.hadden.emu.BusDevice.IOSize;
import com.hadden.emu.c64.BASICDevice;
import com.hadden.emu.c64.CIADevice;
import com.hadden.emu.c64.CharacterDevice;
import com.hadden.emu.c64.KernalDevice;
import com.hadden.emu.c64.ScreenDevice;
import com.hadden.emu.impl.DisplayDevice;
import com.hadden.emu.impl.Gfx256Device;
import com.hadden.emu.impl.LCDDevice;
import com.hadden.emu.impl.RAMDevice;
import com.hadden.emu.impl.ROMDevice;
import com.hadden.emu.impl.SerialDevice;
import com.hadden.emu.impl.TimerDevice;

public class AddressMapImpl implements Bus, AddressMap
{
	private NavigableMap<Integer, BusDevice> mappedAddressSpace = new TreeMap<Integer, BusDevice>();

	private BusDevice defaultSpace = null;
	private BusIRQ birq = null;

	private BusListener busListener = null;

	public AddressMapImpl()
	{
	}	
	
	public void setDefaultDevice(BusDevice bd)
	{
		this.defaultSpace  = bd;		
		mappedAddressSpace.put(defaultSpace.getBusAddressRange().getLowAddress(), defaultSpace);
		mappedAddressSpace.put(defaultSpace.getBusAddressRange().getHighAddress() + 1,defaultSpace);
	}

	public void setBusListener(BusListener bl)
	{
		this.busListener  = bl;
	}
	
	public void setIRQHandler(BusIRQ busIRQ)
	{
		this.birq  = busIRQ;		
	}
	
	public AddressMapImpl(BusDevice bd, BusIRQ birq)
	{
		setDefaultDevice(bd);
		this.birq = birq;
	}

	public AddressMapImpl addBusDevice(BusDevice bd)
	{
		mappedAddressSpace.put(bd.getBusAddressRange().getLowAddress(), bd);
		mappedAddressSpace.put(bd.getBusAddressRange().getHighAddress() + 1, defaultSpace);
		
		if(bd instanceof RaisesIRQ)
			((RaisesIRQ)(bd)).attach(this.birq);
		
		if(bd instanceof HasPorts)
		{
			BusDevice[] ports = ((HasPorts)bd).ports(bd.getBusAddressRange().getLowAddress());
			if(ports!=null)
			{
				for(BusDevice port : ports)
				{
					mappedAddressSpace.put(port.getBusAddressRange().getLowAddress(), port);
					mappedAddressSpace.put(port.getBusAddressRange().getHighAddress() + 1, defaultSpace);				
				}
			}
		}
		if(bd instanceof BusAccessor)
		{
			((BusAccessor)bd).setReader(new BusReader() 
			{
				@Override
				public int read(int address)
				{
					return AddressMapImpl.this.read((short)address);
				}
			} );
			((BusAccessor)bd).setWriter(new BusWriter() 
			{
				@Override
				public void write(int address, int value)
				{
					AddressMapImpl.this.write((short)(address & 0xFFFF),(byte)(address & 0xFF));
				}				
			});
		}
		
		return this;
	}
	
	public BusDevice getMemoryMappedDevice(int address)
	{
		BusDevice bd = defaultSpace;
		
		if(mappedAddressSpace.floorEntry(address)!=null)
		{
			bd = mappedAddressSpace.floorEntry(address).getValue();
		}
		
		return bd;
	}

	
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		AddressMapImpl map =  new AddressMapImpl(new RAMDevice(0x00000000,64*1024),
				                         new BusIRQ() 
										{
											@Override
											public void raise(int source)
											{
												System.out.println("CPU IRQ");
												
											}
										});
		/*
		map.addBusDevice(new ROMDevice(0x00008000))
		   .addBusDevice(new DisplayDevice(0x0000A000,40,10))
		   .addBusDevice(new LCDDevice(0x0000B000))
		   .addBusDevice(new TimerDevice(0x0000B003,60000))
		   .addBusDevice(new Gfx256Device(0x0000E000))
		   .addBusDevice(new SerialDevice(0x00000200))
		   ;
		*/
		map.addBusDevice(new CIADevice(0x0000DC00))
		   .addBusDevice(new ScreenDevice(0x00000400,40,25))
		   .addBusDevice(new BASICDevice(0x0000A000))
		   .addBusDevice(new CharacterDevice(0x0000D000))
		   .addBusDevice(new KernalDevice(0x0000E000))
		   ;

		int t = 0;
		for(int i=0;i<1024;i++)
		{
			map.write((short)i, (byte)(t & 0xFF));
			t++;
		}
		map.printAddressMap();
		System.out.println(map.toString(8,true));
		
		
		
		
		BusDevice bd = map.getMemoryMappedDevice(0x00001000);
		if(bd!=null)
			System.out.println(bd.getName());
		
		bd = map.getMemoryMappedDevice(0x0000A000);
		if(bd!=null)
			System.out.println(bd.getName());
		bd = map.getMemoryMappedDevice(0x0000a18f);
		if(bd!=null)
			System.out.println(bd.getName());

		bd = map.getMemoryMappedDevice(0x0000a190);
		if(bd!=null)
			System.out.println(bd.getName());
		
		bd = map.getMemoryMappedDevice(0x0000B000);
		if(bd!=null)
			System.out.println(bd.getName());		

		bd = map.getMemoryMappedDevice(0x0000B001);
		if(bd!=null)
			System.out.println(bd.getName());	
		
		bd = map.getMemoryMappedDevice(0x0000B003);
		if(bd!=null)
			System.out.println(bd.getName());	
		
		bd = map.getMemoryMappedDevice(0x0000B00A);
		if(bd!=null)
			System.out.println(bd.getName());		
		
		int base = 0x0000A000;
		String msg = "Hello World!";
		for(int c=0;c<msg.length();c++)
			map.getMemoryMappedDevice(base).writeAddress(base++, (byte)(int)msg.charAt(c), IOSize.IO8Bit);
		
		map.getMemoryMappedDevice(0x0000B000).writeAddress(0x0000B000, 0x0, IOSize.IO8Bit);
		map.getMemoryMappedDevice(0x0000B001).writeAddress(0x0000B001, 0xF, IOSize.IO8Bit);
		map.getMemoryMappedDevice(0x0000B000).writeAddress(0x0000B000, 0x1, IOSize.IO8Bit);
		map.getMemoryMappedDevice(0x0000B001).writeAddress(0x0000B001, 'A', IOSize.IO8Bit);
		
	}


	@Override
	public byte read(short address)
	{
		int laddr = address;
		
		if(address < 0)
		{
			laddr = 0xFFFF + address + 1;
			//System.out.println(Integer.toHexString(laddr));
		}
		byte v =  (byte) getMemoryMappedDevice(laddr).readAddressUnsigned(laddr, IOSize.IO8Bit);
		
		if(busListener!=null)
			busListener.readListener(address);
		
		return v;
	}


	@Override
	public void write(short address, byte data)
	{
		int laddr = address;
		
		
		if(address < 0)
		{
			laddr = 0xFFFF + address + 1;
			//System.out.println(Integer.toHexString(laddr));
		}		
		
		getMemoryMappedDevice(laddr).writeAddress(laddr, data,IOSize.IO8Bit);		

		if(busListener!=null)
			busListener.writeListener(address, data);

	}


	public void printAddressMap()
	{
		for(Integer adr : this.mappedAddressSpace.keySet())
		{
			String hex = Integer.toHexString(adr).toUpperCase();
			
			int hlen = hex.length();
			int nlen = (8 - hlen);
			
			if(nlen > 0)
			{
				for(int i=0;i<nlen;i++)
				{
					hex = "0" + hex;
				}
			}	
			
			hex = hex.substring(0,4) + ":" + hex.substring(4); 
			
			System.out.println("[" + hex + "] " + mappedAddressSpace.get(adr).getName());
			
		}
	}

	public String dumpBytesAsString()
	{
		return toString(8, true);
	}
	
	public String toString(int bytesPerLine, boolean addresses)
	{
		StringBuilder sb = new StringBuilder();

		int lineSize = bytesPerLine;
		//System.out.println();
		if(this.defaultSpace!=null)
		{
			BusAddressRange bar = defaultSpace.getBusAddressRange();
			
			for(int bk = bar.getLowAddress(); bk <= bar.getHighAddress(); bk++)
			{
				String hex = Integer.toHexString(this.read((short)bk) & 0xFF).toUpperCase();
				
				int hlen = hex.length();
				int nlen = (2 - hlen);
				
				if(nlen > 0)
				{
					for(int i=0;i<nlen;i++)
					{
						hex = "0" + hex;
					}
				}	
				
				if(addresses && (lineSize == bytesPerLine))
				{
					String hexAddress = Integer.toHexString(bk).toUpperCase(); 
							
					int halen = hexAddress.length();
					int nalen = (4 - halen);
					
					if(nalen > 0)
					{
						for(int i=0;i<nalen;i++)
						{
							hexAddress = "0" + hexAddress;
						}
					}
					
					//System.out.print(hexAddress + ": ");
					sb.append(hexAddress + ": ");
				}				
				
				//System.out.print(hex + " ");
				sb.append(hex);
				
				lineSize--;
				if(lineSize < 1)
				{
					lineSize = bytesPerLine;
					//System.out.println();
					sb.append("\n");
				}
				else
				{
					sb.append(" ");
				}
					
			}
			

			
			//System.out.println("[" + hex + "] " + mappedAddressSpace.get(adr).getName());
			
		}
		
		return sb.toString();
	}	
	
}
