package com.hadden.emulator.cpu.MOS;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;

import com.hadden.Instruction;
import com.hadden.emu.AddressMap;
import com.hadden.emu.Bus;
import com.hadden.emu.CPU;
import com.hadden.emu.CPUInfo;
import com.hadden.emu.IOSize;
import com.hadden.emulator.ClockLine;
import com.hadden.emulator.DeviceDebugger;
import com.hadden.emulator.debug.DebugControl;
import com.hadden.emulator.debug.DebugListener;
import com.hadden.emulator.debug.DebugListener.DebugReason;
import com.hadden.emulator.debug.Debugger;
import com.hadden.emulator.util.Convert;

public class MOS65C02A implements CPU, CPUInfo, ClockLine, DeviceDebugger, DebugControl
{
	private static class InterruptLock
	{
		public InterruptLock()
		{
			
		}
	}
	
	public boolean debugger_irq_hold = false;
	
	public byte flags = 0x00;
	// C,Z,I,D,B,U,V,N
	// Carry, Zero, Interrupt Disable, Decimal, Break, Unused, Overflow, Negative

	public byte a = 0x00;
	public byte x = 0x00;
	public byte y = 0x00;
	public byte stackPointer = 0x00;
	public short programCounter = 0x0000;
	public short optPC = 0x0000;
	public Instruction lastInst = null;

	public boolean debug = false;

	public short addressAbsolute = 0x0000;
	
	public short addressAbsoluteBase = 0x0000;
	
	public short addressRelative = 0x0000;
	public byte opcode = 0x00;
	public int cycles = 0;

	public double ClocksPerSecond = 0.0;
	public double kClocksPerSecond = 0.0;
	public double mClocksPerSecond = 0.0;
	public long startTime = 0;

	public int additionalCycles = 0;

	private boolean interruptRequested = false;
	public boolean NMinterruptRequested = false;

	public Instruction[] lookup = new Instruction[0x100];

	public Queue<Integer> pendingIRQ = new LinkedList<Integer>();
	
	public Map<Integer,Integer> cyclesCache = new HashMap<Integer,Integer>();
	public Map<Integer,Method> addressModeCache = new HashMap<Integer,Method>();
	public Map<Integer,Method> instructionCache = new HashMap<Integer,Method>();
	private Telemetry telemetry = new Telemetry();
	
	private Bus cpuBus = null;//new BusImpl();
	private int clocks;
	private int pushCycles;
	private long irqCount = 0;
	private boolean interrupted = false;
	private boolean interruptsEnabled = true;
	private int irqCycles;
	private int starveProtect = 5;

	//
	// debugging variables
	//
	private boolean bDebugControl = false;
	private List<DebugListener> debugSteps = new Vector<DebugListener>();
	private List<DebugListener> debugClocks = new Vector<DebugListener>();
	private List<DebugListener> debugBreaks = new Vector<DebugListener>();
	//
	//
	
	private InterruptLock irqLock = new InterruptLock();
	
	public String getName()
	{
		return "65C02A";
	}
	
	static public class LimitedSizeQueue<K> extends ArrayList<K> {

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
	
	private List<String> history = new LimitedSizeQueue<String>(2000);
	private boolean historyEnabled = true;

	private long irqTime = 0;
	
	public MOS65C02A(Bus bus)
	{
		cpuBus = bus;
		
		reset();

		Arrays.fill(lookup, new Instruction("XXX", "IMP", 2));

		// ADC
		lookup[0x69] = new Instruction("ADC", "IMM", 2);
		lookup[0x65] = new Instruction("ADC", "ZPP", 3);
		lookup[0x75] = new Instruction("ADC", "ZPX", 4);
		lookup[0x6D] = new Instruction("ADC", "ABS", 4);
		lookup[0x7D] = new Instruction("ADC", "ABX", 4);
		lookup[0x79] = new Instruction("ADC", "ABY", 4);
		lookup[0x61] = new Instruction("ADC", "IZX", 6);
		lookup[0x71] = new Instruction("ADC", "IZY", 5);

		lookup[0x29] = new Instruction("AND", "IMM", 2);
		lookup[0x25] = new Instruction("AND", "ZPP", 3);
		lookup[0x35] = new Instruction("AND", "ZPX", 4);
		lookup[0x2D] = new Instruction("AND", "ABS", 4);
		lookup[0x3D] = new Instruction("AND", "ABX", 4);
		lookup[0x39] = new Instruction("AND", "ABY", 4);
		lookup[0x21] = new Instruction("AND", "IZX", 6);
		lookup[0x31] = new Instruction("AND", "IZY", 5);

		lookup[0x0A] = new Instruction("ASL", "IMP", 2);
		lookup[0x06] = new Instruction("ASL", "ZPP", 5);
		lookup[0x16] = new Instruction("ASL", "ZPX", 6);
		lookup[0x0E] = new Instruction("ASL", "ABS", 6);
		lookup[0x1E] = new Instruction("ASL", "ABX", 7);

		lookup[0x90] = new Instruction("BCC", "REL", 2);

		lookup[0xB0] = new Instruction("BCS", "REL", 2);

		lookup[0xF0] = new Instruction("BEQ", "REL", 2);

		lookup[0x24] = new Instruction("BIT", "ZPP", 3);
		lookup[0x2C] = new Instruction("BIT", "ABS", 4);

		lookup[0x30] = new Instruction("BMI", "REL", 2);

		lookup[0xD0] = new Instruction("BNE", "REL", 2);

		lookup[0x10] = new Instruction("BPL", "REL", 2);

		lookup[0x00] = new Instruction("BRK", "IMP", 2);

		lookup[0x50] = new Instruction("BVC", "REL", 2);

		lookup[0x70] = new Instruction("BVS", "REL", 2);

		lookup[0x18] = new Instruction("CLC", "IMP", 2);

		lookup[0xD8] = new Instruction("CLD", "IMP", 2);

		lookup[0x58] = new Instruction("CLI", "IMP", 2);

		lookup[0xB8] = new Instruction("CLV", "IMP", 2);

		lookup[0xC9] = new Instruction("CMP", "IMM", 2);
		lookup[0xC5] = new Instruction("CMP", "ZPP", 3);
		lookup[0xD5] = new Instruction("CMP", "ZPX", 4);
		lookup[0xCD] = new Instruction("CMP", "ABS", 4);
		lookup[0xDD] = new Instruction("CMP", "ABX", 4);
		lookup[0xD9] = new Instruction("CMP", "ABY", 4);
		lookup[0xC1] = new Instruction("CMP", "IZX", 6);
		lookup[0xD1] = new Instruction("CMP", "IZY", 5);

		lookup[0xE0] = new Instruction("CPX", "IMM", 2);
		lookup[0xE4] = new Instruction("CPX", "ZPP", 3);
		lookup[0xEC] = new Instruction("CPX", "ABS", 4);

		lookup[0xC0] = new Instruction("CPY", "IMM", 2);
		lookup[0xC4] = new Instruction("CPY", "ZPP", 3);
		lookup[0xCC] = new Instruction("CPY", "ABS", 4);

		lookup[0xC6] = new Instruction("DEC", "ZPP", 5);
		lookup[0xD6] = new Instruction("DEC", "ZPX", 6);
		lookup[0xCE] = new Instruction("DEC", "ABS", 6);
		lookup[0xDE] = new Instruction("DEC", "ABX", 7);

		lookup[0xCA] = new Instruction("DEX", "IMP", 2);

		lookup[0x88] = new Instruction("DEY", "IMP", 2);

		lookup[0x49] = new Instruction("EOR", "IMM", 2);
		lookup[0x45] = new Instruction("EOR", "ZPP", 3);
		lookup[0x55] = new Instruction("EOR", "ZPX", 4);
		lookup[0x4D] = new Instruction("EOR", "ABS", 4);
		lookup[0x5D] = new Instruction("EOR", "ABX", 4);
		lookup[0x59] = new Instruction("EOR", "ABY", 4);
		lookup[0x41] = new Instruction("EOR", "IZX", 6);
		lookup[0x51] = new Instruction("EOR", "IZY", 5);

		lookup[0xE6] = new Instruction("INC", "ZPP", 5);
		lookup[0xF6] = new Instruction("INC", "ZPX", 6);
		lookup[0xEE] = new Instruction("INC", "ABS", 6);
		lookup[0xFE] = new Instruction("INC", "ABX", 7);

		lookup[0xE8] = new Instruction("INX", "IMP", 2);

		lookup[0xC8] = new Instruction("INY", "IMP", 2);

		lookup[0x4C] = new Instruction("JMP", "ABS", 3);
		lookup[0x6C] = new Instruction("JMP", "IND", 5);

		lookup[0x20] = new Instruction("JSR", "ABS", 6);

		lookup[0xA9] = new Instruction("LDA", "IMM", 2);
		lookup[0xA5] = new Instruction("LDA", "ZPP", 3);
		lookup[0xB5] = new Instruction("LDA", "ZPX", 4);
		lookup[0xAD] = new Instruction("LDA", "ABS", 4);
		lookup[0xBD] = new Instruction("LDA", "ABX", 4);
		lookup[0xB9] = new Instruction("LDA", "ABY", 4);
		lookup[0xA1] = new Instruction("LDA", "IZX", 6);
		lookup[0xB1] = new Instruction("LDA", "IZY", 5);

		lookup[0xA2] = new Instruction("LDX", "IMM", 2);
		lookup[0xA6] = new Instruction("LDX", "ZPP", 3);
		lookup[0xB6] = new Instruction("LDX", "ZPY", 4);
		lookup[0xAE] = new Instruction("LDX", "ABS", 4);
		lookup[0xBE] = new Instruction("LDX", "ABY", 4);

		lookup[0xA0] = new Instruction("LDY", "IMM", 2);
		lookup[0xA4] = new Instruction("LDY", "ZPP", 3);
		lookup[0xB4] = new Instruction("LDY", "ZPX", 4);
		lookup[0xAC] = new Instruction("LDY", "ABS", 4);
		lookup[0xBC] = new Instruction("LDY", "ABX", 4);

		lookup[0x4A] = new Instruction("LSR", "IMP", 2);
		lookup[0x46] = new Instruction("LSR", "ZPP", 5);
		lookup[0x56] = new Instruction("LSR", "ZPX", 6);
		lookup[0x4E] = new Instruction("LSR", "ABS", 6);
		lookup[0x5E] = new Instruction("LSR", "ABX", 7);

		lookup[0xEA] = new Instruction("NOP", "IMP", 2);

		lookup[0x09] = new Instruction("ORA", "IMM", 2);
		lookup[0x05] = new Instruction("ORA", "ZPP", 3);
		lookup[0x15] = new Instruction("ORA", "ZPX", 4);
		lookup[0x0D] = new Instruction("ORA", "ABS", 4);
		lookup[0x1D] = new Instruction("ORA", "ABX", 4);
		lookup[0x19] = new Instruction("ORA", "ABY", 4);
		lookup[0x01] = new Instruction("ORA", "IZX", 6);
		lookup[0x11] = new Instruction("ORA", "IZY", 5);

		lookup[0x48] = new Instruction("PHA", "IMP", 3);
		lookup[0xDA] = new Instruction("PHX", "IMP", 3);
		lookup[0x5A] = new Instruction("PHY", "IMP", 3);

		lookup[0x08] = new Instruction("PHP", "IMP", 3);

		lookup[0x68] = new Instruction("PLA", "IMP", 4);
		lookup[0xFA] = new Instruction("PLX", "IMP", 4);
		lookup[0x7A] = new Instruction("PLY", "IMP", 4);

		lookup[0x28] = new Instruction("PLP", "IMP", 4);

		lookup[0x2A] = new Instruction("ROL", "IMP", 2);
		lookup[0x26] = new Instruction("ROL", "ZPP", 5);
		lookup[0x36] = new Instruction("ROL", "ZPX", 6);
		lookup[0x2E] = new Instruction("ROL", "ABS", 6);
		lookup[0x3E] = new Instruction("ROL", "ABX", 7);

		lookup[0x6A] = new Instruction("ROR", "IMP", 2);
		lookup[0x66] = new Instruction("ROR", "ZPP", 5);
		lookup[0x76] = new Instruction("ROR", "ZPX", 6);
		lookup[0x6E] = new Instruction("ROR", "ABS", 6);
		lookup[0x7E] = new Instruction("ROR", "ABX", 7);

		lookup[0x40] = new Instruction("RTI", "IMP", 6);

		lookup[0x60] = new Instruction("RTS", "IMP", 6);

		lookup[0xE9] = new Instruction("SBC", "IMM", 2);
		lookup[0xE5] = new Instruction("SBC", "ZPP", 3);
		lookup[0xF5] = new Instruction("SBC", "ZPX", 4);
		lookup[0xED] = new Instruction("SBC", "ABS", 4);
		lookup[0xFD] = new Instruction("SBC", "ABX", 4);
		lookup[0xF9] = new Instruction("SBC", "ABY", 4);
		lookup[0xE1] = new Instruction("SBC", "IZX", 6);
		lookup[0xF1] = new Instruction("SBC", "IZY", 5);

		lookup[0x38] = new Instruction("SEC", "IMP", 2);

		lookup[0xF8] = new Instruction("SED", "IMP", 2);

		lookup[0x78] = new Instruction("SEI", "IMP", 2);

		lookup[0x85] = new Instruction("STA", "ZPP", 3);
		lookup[0x95] = new Instruction("STA", "ZPX", 4);
		lookup[0x8D] = new Instruction("STA", "ABS", 4);
		lookup[0x9D] = new Instruction("STA", "ABX", 5);
		lookup[0x99] = new Instruction("STA", "ABY", 5);
		lookup[0x81] = new Instruction("STA", "IZX", 6);
		lookup[0x91] = new Instruction("STA", "IZY", 6);

		lookup[0x86] = new Instruction("STX", "ZPP", 3);
		lookup[0x96] = new Instruction("STX", "ZPY", 4);
		lookup[0x8E] = new Instruction("STX", "ABS", 4);

		lookup[0x84] = new Instruction("STY", "ZPP", 3);
		lookup[0x94] = new Instruction("STY", "ZPX", 4);
		lookup[0x8C] = new Instruction("STY", "ABS", 4);

		lookup[0xAA] = new Instruction("TAX", "IMP", 2);

		lookup[0xA8] = new Instruction("TAY", "IMP", 2);

		lookup[0xBA] = new Instruction("TSX", "IMP", 2);

		lookup[0x8A] = new Instruction("TXA", "IMP", 2);

		lookup[0x9A] = new Instruction("TXS", "IMP", 2);

		lookup[0x98] = new Instruction("TYA", "IMP", 2);
	}

	
	public int register(String name)
	{
		if(name.equalsIgnoreCase("a"))
			return a;

		if(name.equalsIgnoreCase("x"))
			return x;

		if(name.equalsIgnoreCase("y"))
			return y;
		
		if(name.equalsIgnoreCase("s"))
			return (int)stackPointer;

		if(name.equalsIgnoreCase("p"))
			return (int)programCounter;
		
		return 0;
	}
	
	void setFlag(char flag, boolean condition)
	{
		flag = Character.toUpperCase(flag);
		switch (flag)
		{
		case 'C':
			flags = setBit(flags, 0, condition);
			break;
		case 'Z':
			flags = setBit(flags, 1, condition);
			break;
		case 'I':
			flags = setBit(flags, 2, condition);
			break;
		case 'D':
			flags = setBit(flags, 3, condition);
			break;
		case 'B':
			flags = setBit(flags, 4, condition);
			break;
		case 'U':
			flags = setBit(flags, 5, condition);
			break;
		case 'V':
			flags = setBit(flags, 6, condition);
			break;
		case 'N':
			flags = setBit(flags, 7, condition);
			break;
		}
	}

	boolean getFlag(char flag)
	{
		flag = Character.toUpperCase(flag);
		switch (flag)
		{
		case 'N':
			return ((flags & 0b10000000) == 0b10000000);
		case 'V':
			return ((flags & 0b01000000) == 0b01000000);
		case 'U':
			return ((flags & 0b00100000) == 0b00100000);
		case 'B':
			return ((flags & 0b00010000) == 0b00010000);
		case 'D':
			return ((flags & 0b00001000) == 0b00001000);
		case 'I':
			return ((flags & 0b00000100) == 0b00000100);
		case 'Z':
			return ((flags & 0b00000010) == 0b00000010);
		case 'C':
			return ((flags & 0b00000001) == 0b00000001);
		}
		System.out.println("Something has gone wrong in getFlag!");
		return false;
	}

	public static byte setBit(byte b, int bit, boolean value)
	{
		if (value)
		{
			b |= (byte) (0x00 + Math.pow(2, bit));
		}
		else
		{
			b &= (byte) (0xFF - Math.pow(2, bit));
		}
		return b;
	}

	public void interrupt()
	{
		//debug = false;
		/*
		if(true)			
		{
			interruptRequested = true;
		}
		*/
		synchronized(irqLock)
		{
			long iirq = System.currentTimeMillis() - irqTime ;			
			if(iirq < 5000 || clocks < 10)
			{
				//System.out.println("Skip IRQ");
				return;
			}
			
			if(!interruptRequested)
			{
				irqTime = System.currentTimeMillis();
				interruptRequested = true;
			}
		
		};
	}		
	
	public void clock()
	{
		short lastPC = 0;
		
 		if (cycles == 0)
		{
			if(interruptRequested && !debugger_irq_hold)
				irq();
			else if (NMinterruptRequested)
				nmi();		
			//else
			if(true)
			{
				additionalCycles = 0;
				optPC = programCounter;
				
				
				//System.out.println("PC" + AddressMap.toHexAddress(programCounter,IOSize.IO16Bit));

				opcode = cpuBus.read(programCounter);
				programCounter++;

				//cycles = lookup[Byte.toUnsignedInt(opcode)].cycles;
				if(!cyclesCache.containsKey((int)opcode))
				{
					cyclesCache.put((int)opcode,lookup[Byte.toUnsignedInt(opcode)].cycles);
				}				

				try
				{
					
					//this.getClass().getMethod(lookup[Byte.toUnsignedInt(opcode)].addressMode).invoke(this);
					//this.getClass().getMethod(lookup[Byte.toUnsignedInt(opcode)].opcode).invoke(this);
					Instruction instruction = lookup[Byte.toUnsignedInt(opcode)];
					lastInst = instruction;
					
					//
					// added mode and op caching for simple increase in speed
					//
					if(!addressModeCache.containsKey((int)opcode))
					{
						addressModeCache.put((int)opcode,this.getClass().getMethod(instruction.addressMode));
					}
					
					if(!instructionCache.containsKey((int)opcode))
					{
						instructionCache.put((int)opcode,this.getClass().getMethod(instruction.opcode));
					}
					
					//debugInstruction(instruction);
					
					cycles = cyclesCache.get((int)opcode);
					addressModeCache.get((int)opcode).invoke(this);
					instructionCache.get((int)opcode).invoke(this);
					
					debugInstruction(instruction);
					
					//if("RTI".equals(instruction.opcode))
					//	historyEnabled = false;
					//interruptsEnabled = true;
					
				} 
				catch (Exception e)
				{
					System.out.println(Integer.toHexString(opcode & 0xFF) + ": " + Integer.toHexString(this.addressAbsolute & 0xFFFF));
					e.printStackTrace();
				}
			}
		}

		if (((System.currentTimeMillis() - startTime) / 1000) > 0)
		{
			ClocksPerSecond = clocks / ((System.currentTimeMillis() - startTime) / 1000);
			kClocksPerSecond = ClocksPerSecond/1000;
			mClocksPerSecond = kClocksPerSecond/1000; 
		}
		clocks++;
		
		if(clocks < 0)
		{
			clocks = 1;
			startTime = System.currentTimeMillis();
		}
			
		cycles--;
		
		
		if(cycles <= 0)
		{
			cycles = 0;
			if(bDebugControl && debugSteps.size() > 0)
				for(DebugListener dl : this.debugSteps)
					dl.debugEvent(DebugReason.Step, programCounter,IOSize.IO16Bit);
		}
		
		
		
		
		telemetry.a = a;
		telemetry.x = x;
		telemetry.y = y;
		telemetry.addressAbsolute = addressAbsolute;
		telemetry.addressRelative = addressRelative;
		telemetry.stackPointer = stackPointer;
		telemetry.programCounter = programCounter;
		telemetry.opcode = opcode;
		telemetry.opcodeName = lookup[Byte.toUnsignedInt(opcode)].opcode;
		telemetry.clocksPerSecond = ClocksPerSecond;
		telemetry.cycles = cycles;
		telemetry.clocks = clocks;
		telemetry.flags  = flags;	
		telemetry.irqs = irqCount;
		telemetry.history = history;
	}


	private void debugInstruction(Instruction dbgOp)
	{
		if (true)
		{
			String debugLine = "";
			
			debugLine = (Convert.padStringWithZeroes(Integer.toHexString(Short.toUnsignedInt((short)(optPC))), 4) + ":" + 
				     dbgOp.opcode + " " + 
				     Convert.byteToHexString(opcode) + " ");
			
			/*
			debugLine = (Convert.padStringWithZeroes(Integer.toHexString(Short.toUnsignedInt(programCounter)), 4) + ":" + 
					     dbgOp.opcode + " " + 
					     Convert.byteToHexString(opcode) + " ");
			*/
			//System.out.println("addressAbsolute[" + dbgOp.opcode + ":" + dbgOp.addressMode  + "]:" +  addressAbsolute);
			//System.out.println("addressAbsolute[" + dbgOp.opcode + ":" + dbgOp.addressMode  + "]:" +  Convert.toHex16String(addressAbsolute));
			
			
			if (!(dbgOp.addressMode.equals("IMP") || 
				  dbgOp.addressMode.equals("REL")))
			{
				if (dbgOp.addressMode.equals("IMM"))
				{
					//System.out.println("addressAbsolute IMM:" + Convert.toHex8String(addressAbsolute));
					//debugLine+=("#$" + Integer.toHexString(Byte.toUnsignedInt(fetched))); 
					debugLine+=("#$" + Convert.toHex8String(fetched));
				}
				else if (dbgOp.addressMode.equals("REL"))
				{
					//System.out.println("addressAbsolute REL:" +  Convert.toHex16String(addressAbsolute));
					//debugLine+=("$" + Integer.toHexString(Short.toUnsignedInt(addressAbsolute) & 0x0000FFFF));
					debugLine+=("$" + Convert.toHex16String(addressAbsolute));
				}

				else if (dbgOp.addressMode.equals("ABS"))
				{
					//System.out.println("addressAbsolute ELSE:" +  Convert.toHex16String(addressAbsolute));
					//debugLine+=("$" + Integer.toHexString(Short.toUnsignedInt(addressAbsolute)  & 0x0000FFFF));
					debugLine+=("$" + Convert.toHex16String(addressAbsoluteBase));
				}
				else if (dbgOp.addressMode.equals("ABX"))
				{
					//System.out.println("addressAbsolute ELSE:" +  Convert.toHex16String(addressAbsolute));
					//debugLine+=("$" + Integer.toHexString(Short.toUnsignedInt(addressAbsolute)  & 0x0000FFFF));
					debugLine+=("$" + Convert.toHex16String(addressAbsoluteBase));
				}
				else if (dbgOp.addressMode.equals("ABY"))
				{
					//System.out.println("addressAbsolute ELSE:" +  Convert.toHex16String(addressAbsolute));
					//debugLine+=("$" + Integer.toHexString(Short.toUnsignedInt(addressAbsolute)  & 0x0000FFFF));
					debugLine+=("$" + Convert.toHex16String(addressAbsoluteBase));
				}
				
				else
				{
					//System.out.println("addressAbsolute ELSE:" +  Convert.toHex16String(addressAbsolute));
					//debugLine+=("$" + Integer.toHexString(Short.toUnsignedInt(addressAbsolute)  & 0x0000FFFF));
					debugLine+=("$" + Convert.toHex16String(addressAbsolute));
				}
			}
			else if (!dbgOp.addressMode.equals("IMP"))
			{
				//debugLine+=("$" + Integer.toHexString(Short.toUnsignedInt(addressRelative)  & 0x00000FF));
				debugLine+=("$" + Convert.toHex8String(addressRelative));
			}
			
			if (dbgOp.addressMode.equals("ABX")	|| 
			    dbgOp.addressMode.equals("INX") || 
			    dbgOp.addressMode.equals("ZPX"))
			{
				//debugLine+=(",X ");
				debugLine+=("," + Convert.toHex8String(x) + " ");
			}
			else if (dbgOp.addressMode.equals("ABY") || 
					 dbgOp.addressMode.equals("INY") || 
					 dbgOp.addressMode.equals("ZPY"))
			{
				debugLine+=(",Y ");
			}
			else
			{
				debugLine+=(" ");
			}
			
//			debugLine+=("A:"  + Convert.toHex8String(a) +
//					    " X:" + Convert.toHex8String(x) + 
//					    " Y:" + Convert.toHex8String(y));

			int pad = debugLine.length();
			debugLine+=("                      ".substring(pad));
	
			debugLine+=("A:"  + Convert.toHex8String(a) +
				    " X:" + Convert.toHex8String(x) + 
				    " Y:" + Convert.toHex8String(y));
			
			debugLine+=(" F:"
			            + Convert.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(flags)), 8));

			//System.out.println(debugLine);				
			if(historyEnabled)
				history.add(debugLine);
		}
	}

	
	public int rate(ClockRateUnit cru)
	{
		switch(cru)
		{
		case HZ:
			return (int)ClocksPerSecond;
		case KHZ:	
			return (int)kClocksPerSecond;
		case MHZ:
			return (int)mClocksPerSecond;
		}
		
		return (int)0;
	}
	
	// Input Signal Handlers
	public void reset()
	{
		history.clear();
		
		a = 0;
		x = 0;
		y = 0;
		
		stackPointer = (byte) 0xFD;
		flags = (byte) (0x00 | (getFlag('U') ? 0b00000100 : 0));

		addressAbsolute = (short) (0xFFFC);

		byte lo = cpuBus.read(addressAbsolute);
		byte hi = cpuBus.read((short) (addressAbsolute + 1));
		programCounter = (short) (Byte.toUnsignedInt(lo) + 256 * Byte.toUnsignedInt(hi));

		clocks = 0;
		ClocksPerSecond = 0;
		kClocksPerSecond = 0;

		addressRelative = 0;
		addressAbsolute = 0;
		fetched = 0;

		cycles = 8;

		startTime = System.currentTimeMillis();
		
		opcode = cpuBus.read(programCounter);
	}

	private byte pushStack(byte value)
	{
		cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), value);
		stackPointer--;
		
		return stackPointer;
	}

	private byte popStack()
	{
		stackPointer++;
		byte value = cpuBus.read((short) (0x0100 + Byte.toUnsignedInt(stackPointer)));
		
		return value;
	}

	
	void irq()
	{

		//if(debug)
		//	System.out.println("pre-irq(" + getFlag('I') + ")");
		
		boolean fi = getFlag('I');
		if(!fi)
		{
			//pendingIRQ.poll();
			irqCount++;
			this.interrupted = true;
			if(debug)
				System.out.println("irq()");
			
			pushCycles = cycles;
			
			//System.out.println("\nIRQ PC:" + Integer.toHexString(programCounter & 0xFFFF) );
			if(historyEnabled)
				history.add("IRQ PC:" + Integer.toHexString(programCounter & 0xFFFF));
			
			//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), (byte) (programCounter >> 8));
			//stackPointer--;
			pushStack((byte) (programCounter >> 8));
			//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), (byte) (programCounter));
			//stackPointer--;
			pushStack((byte)programCounter);
			
			setFlag('B', false);
			setFlag('U', false);
			//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), flags);
			//stackPointer--;
			pushStack(flags);
			setFlag('I', true);

			addressAbsolute = (short) (0xFFFE);
			byte lo = cpuBus.read(addressAbsolute);
			byte hi = cpuBus.read((short) (addressAbsolute + 1));
			
			//if(debug)
			//	System.out.println("IR:0x" + Integer.toHexString(hi & 0xFF) + ":" + Integer.toHexString(lo));
			
			programCounter = (short) (Byte.toUnsignedInt(lo) + 256 * (Byte.toUnsignedInt(hi) & 0xFF));

			//System.out.println("IRQ PTR:" + Integer.toHexString(programCounter & 0xFFFF) );
			//System.out.println("*** IRQ ***");
			
			cycles = 7;
			irqCycles = 7;
			//interruptRequested = false;
			//interruptsEnabled = false;
		}
		else
		{
			//System.out.println("IRQ Cycles:" + cycles);
			cycles = 1;
		}

		synchronized(irqLock)
		{
			//pendingIRQ.offer(this.clocks);
			// put this back 
			//interruptRequested = false;
		};

	}

	void nmi()
	{
		//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), (byte) (programCounter >> 8));
		//stackPointer--;
		pushStack((byte) (programCounter >> 8));
		//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), (byte) (programCounter));
		//stackPointer--;
		pushStack((byte) (programCounter));
		
		setFlag('B', false);
		setFlag('U', false);
		//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), flags);
		//stackPointer--;
		pushStack((byte) (flags));
		setFlag('I', true);

		addressAbsolute = (short) (0xFFFA);
		byte lo = cpuBus.read(addressAbsolute);
		byte hi = cpuBus.read((short) ((addressAbsolute & 0xFF) + 1));
		programCounter = (short) (Byte.toUnsignedInt(lo) + 256 * Byte.toUnsignedInt(hi));

		cycles = 7;
		NMinterruptRequested = false;
	}

	// Data Getter
	byte fetched = 0x00;
	
	byte fetch()
	{
		if (!(lookup[Byte.toUnsignedInt(opcode)].addressMode.equals("IMP")))
			fetched = cpuBus.read(addressAbsolute);
		return fetched;
	}

	// Addressing Modes
	public void IMP()
	{
		fetched = a;
	}

	public void IMM()
	{
		addressAbsolute = programCounter++;
	}

	public void ZPP()
	{
		addressAbsolute = cpuBus.read(programCounter);
		programCounter++;
		addressAbsolute &= 0x00FF;
	}

	public void ZPX()
	{
		addressAbsolute = (short) (Byte.toUnsignedInt(cpuBus.read(programCounter)) + Byte.toUnsignedInt(x));
		programCounter++;
		addressAbsolute &= 0x00FF;
	}

	public void ZPY()
	{
		addressAbsolute = (short) (Byte.toUnsignedInt(cpuBus.read(programCounter)) + Byte.toUnsignedInt(y));
		programCounter++;
		addressAbsolute &= 0x00FF;
	}

	public void REL()
	{
		addressRelative = cpuBus.read(programCounter);
		programCounter++;
		if ((addressRelative & 0x80) == 0x80)
			addressRelative |= 0xFF00;
	}

	public void ABS()
	{
		byte lo = cpuBus.read(programCounter);
		programCounter++;
		byte hi = cpuBus.read(programCounter);
		programCounter++;

		addressAbsoluteBase = (short) (Byte.toUnsignedInt(lo) + 256 * Byte.toUnsignedInt(hi)); 
		addressAbsolute = (short) (Byte.toUnsignedInt(lo) + 256 * Byte.toUnsignedInt(hi));
	}

	public void ABX()
	{
		byte lo = cpuBus.read(programCounter);
		programCounter++;
		byte hi = cpuBus.read(programCounter);
		programCounter++;

		addressAbsoluteBase = (short) (Byte.toUnsignedInt(lo) + 256 * Byte.toUnsignedInt(hi));
		addressAbsolute = (short) (Byte.toUnsignedInt(lo) + 256 * Byte.toUnsignedInt(hi) + Byte.toUnsignedInt(x));

		if ((addressAbsolute & 0xFF00) != (hi << 8))
			additionalCycles++;
	}

	public void ABY()
	{
		byte lo = cpuBus.read(programCounter);
		programCounter++;
		byte hi = cpuBus.read(programCounter);
		programCounter++;

		addressAbsoluteBase = (short) (Byte.toUnsignedInt(lo) + 256 * Byte.toUnsignedInt(hi));
		addressAbsolute = (short) (Byte.toUnsignedInt(lo) + 256 * Byte.toUnsignedInt(hi) + Byte.toUnsignedInt(y));

		if ((addressAbsolute & 0xFF00) != (hi << 8))
			additionalCycles++;
	}

	public void IND()
	{
		byte lowPointer = cpuBus.read(programCounter);
		programCounter++;
		byte highPointer = cpuBus.read(programCounter);
		programCounter++;

		short pointer = (short) ((highPointer << 8) | lowPointer);

		addressAbsolute = (short) (Byte.toUnsignedInt(cpuBus.read((short) (pointer + 1))) * 256
				+ Byte.toUnsignedInt(cpuBus.read(pointer)));
	}

	public void IZX()
	{
		byte t = cpuBus.read(programCounter);
		programCounter++;

		byte lo = cpuBus.read((short) ((t + x) & 0x00FF));
		byte hi = cpuBus.read((short) ((t + x + 1) & 0x00FF));

		addressAbsolute = (short) (Byte.toUnsignedInt(lo) + 256 * Byte.toUnsignedInt(hi));
	}

	public void IZY()
	{
		byte t = cpuBus.read(programCounter);
		programCounter++;

		byte lo = cpuBus.read((short) (t & 0x00FF));
		byte hi = cpuBus.read((short) ((t + 1) & 0x00FF));

		addressAbsolute = (short) (Byte.toUnsignedInt(lo) + 256 * Byte.toUnsignedInt(hi) + Byte.toUnsignedInt(y));

		if ((addressAbsolute & 0xFF00) != (hi << 8))
			additionalCycles++;
	}

	// INSTRUCTIONS
	public void ADC()
	{
		fetch();
		short temp = (short) ((short) Byte.toUnsignedInt(a) + (short) Byte.toUnsignedInt(fetched)
				+ (short) (getFlag('C') ? 1 : 0));
		setFlag('C', temp > 255);
		setFlag('Z', (temp & 0x00FF) == 0);
		setFlag('N', (temp & 0x80) == 0x80);
		setFlag('V', (~((short) a ^ (short) fetched) & ((short) a ^ (short) temp) & 0x0080) == 0x0080);
		a = (byte) temp;
		additionalCycles++;
	}

	public void AND()
	{
		fetch();
		a &= fetched;
		setFlag('Z', a == 0x00);
		setFlag('N', (a & 0x80) == 0x80);

		additionalCycles++;
	}

	public void ASL()
	{
		fetch();
		short temp = (short) (fetched << 1);
		setFlag('Z', (temp & 0x00FF) == 0x00);
		setFlag('C', Short.toUnsignedInt((short) (temp & 0xFF00)) > 0);
		setFlag('N', (temp & 0x80) == 0x80);

		if (lookup[Byte.toUnsignedInt(opcode)].addressMode.equals("IMP"))
		{
			a = (byte) (temp & 0x00FF);
		}
		else
		{
			cpuBus.write(addressAbsolute, (byte) (temp & 0x00FF));
		}
	}

	public void BCC()
	{
		if (!getFlag('C'))
		{
			cycles++;
			addressAbsolute = (short) (programCounter + addressRelative);

			if ((addressAbsolute & 0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BCS()
	{
		if (getFlag('C'))
		{
			cycles++;
			addressAbsolute = (short) (programCounter + addressRelative);

			if ((addressAbsolute & 0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BEQ()
	{
		if (getFlag('Z'))
		{
			cycles++;
			addressAbsolute = (short) (programCounter + addressRelative);

			if ((addressAbsolute & 0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BIT()
	{
		fetch();
		short temp = (short) (a & fetched);
		setFlag('Z', (temp & 0x00FF) == 0x00);
		setFlag('N', (fetched & 0x80) == 0x80);
		setFlag('V', (fetched & 0x40) == 0x40);
	}

	public void BMI()
	{
		if (getFlag('N'))
		{
			cycles++;
			addressAbsolute = (short) (programCounter + addressRelative);

			if ((addressAbsolute & 0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BNE()
	{
		if (!getFlag('Z'))
		{
			cycles++;
			addressAbsolute = (short) (programCounter + addressRelative);

			if ((addressAbsolute & 0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BPL()
	{
		if (!getFlag('N'))
		{
			cycles++;
			addressAbsolute = (short) (programCounter + addressRelative);

			if ((addressAbsolute & 0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BRK()
	{
		programCounter++;

		//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), (byte) (programCounter >> 8));
		//stackPointer--;
		pushStack((byte) (programCounter >> 8));
		//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), (byte) (programCounter));
		//stackPointer--;
		pushStack((byte) (programCounter));
		
		setFlag('B', true);
		setFlag('U', true);
		//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), flags);
		//stackPointer--;
		pushStack((byte) flags);
		setFlag('I', true);

		addressAbsolute = (short) 0xFFFE;
		byte lo = cpuBus.read(addressAbsolute);
		byte hi = cpuBus.read((short) (addressAbsolute + 1));
		programCounter = (short) (Byte.toUnsignedInt(lo) + 256 * Byte.toUnsignedInt(hi));
	}

	public void BVC()
	{
		if (!getFlag('V'))
		{
			cycles++;
			addressAbsolute = (short) (programCounter + addressRelative);

			if ((addressAbsolute & 0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BVS()
	{
		if (getFlag('V'))
		{
			cycles++;
			addressAbsolute = (short) (programCounter + addressRelative);

			if ((addressAbsolute & 0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void CLC()
	{
		setFlag('C', false);
	}

	public void CLD()
	{
		setFlag('D', false);
	}

	public void CLI()
	{
		//System.out.println("**CLI**");
		setFlag('I', false);

	}

	public void CLV()
	{
		setFlag('V', false);
	}

	public void CMP()
	{
		fetch();
		short temp = (short) (Byte.toUnsignedInt(a) - Byte.toUnsignedInt(fetched));
		setFlag('C', Byte.toUnsignedInt(a) >= Byte.toUnsignedInt(fetched));
		setFlag('Z', (temp & 0x00FF) == 0x0000);
		setFlag('N', (temp & 0x0080) == 0x0080);

		additionalCycles++;
	}

	public void CPX()
	{
		fetch();
		short temp = (short) (Byte.toUnsignedInt(x) - Byte.toUnsignedInt(fetched));
		setFlag('C', Byte.toUnsignedInt(x) >= Byte.toUnsignedInt(fetched));
		setFlag('Z', (temp & 0x00FF) == 0x0000);
		setFlag('N', (temp & 0x0080) == 0x0080);
	}

	public void CPY()
	{
		fetch();
		short temp = (short) (Byte.toUnsignedInt(y) - Byte.toUnsignedInt(fetched));
		setFlag('C', Byte.toUnsignedInt(y) >= Byte.toUnsignedInt(fetched));
		setFlag('Z', (temp & 0x00FF) == 0x0000);
		setFlag('N', (temp & 0x0080) == 0x0080);
	}

	public void DEC()
	{
		fetch();
		int temp = (Byte.toUnsignedInt(fetched) - 1);
		cpuBus.write(addressAbsolute, (byte) (temp & 0x00FF));
		setFlag('Z', (temp & 0x00FF) == 0x0000);
		setFlag('N', (temp & 0x0080) == 0x0080);
	}

	public void DEX()
	{
		x--;
		setFlag('Z', x == 0x00);
		setFlag('N', (x & 0x80) == 0x80);
	}

	public void DEY()
	{
		y--;
		setFlag('Z', y == 0x00);
		setFlag('N', (y & 0x80) == 0x80);
	}

	public void EOR()
	{
		fetch();
		a ^= fetched;
		setFlag('Z', a == 0x00);
		setFlag('N', (a & 0x80) == 0x80);

		additionalCycles++;
	}

	public void INC()
	{
		fetch();
		short temp = (short) (fetched + 1);
		cpuBus.write(addressAbsolute, (byte) (temp & 0x00FF));
		setFlag('Z', (temp & 0x00FF) == 0x0000);
		setFlag('N', (temp & 0x0080) == 0x0080);
	}

	public void INX()
	{
		x++;
		setFlag('Z', x == 0x00);
		setFlag('N', (x & 0x80) == 0x80);
	}

	public void INY()
	{
		y++;
		setFlag('Z', y == 0x00);
		setFlag('N', (y & 0x80) == 0x80);
	}

	public void JMP()
	{
		programCounter = addressAbsolute;
	}

	public void JSR()
	{
		programCounter--;

		//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), (byte) ((programCounter >> 8) & 0x00FF));
		//stackPointer--;
		pushStack((byte)  ((programCounter >> 8) & 0x00FF));
		//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), (byte) (programCounter & 0x00FF));
		//stackPointer--;
		pushStack((byte)  (programCounter & 0x00FF));
		
		programCounter = addressAbsolute;
	}

	public void LDA()
	{
		fetch();
		a = fetched;
		setFlag('Z', a == 0x00);
		setFlag('N', (a & 0x80) == 0x80);
		additionalCycles++;
	}

	public void LDX()
	{
		fetch();
		x = fetched;
		setFlag('Z', x == 0x00);
		setFlag('N', (x & 0x80) == 0x80);
		additionalCycles++;
	}

	public void LDY()
	{
		fetch();
		y = fetched;
		setFlag('Z', y == 0x00);
		setFlag('N', (y & 0x80) == 0x80);
		additionalCycles++;
	}

	public void LSR()
	{
		fetch();
		setFlag('C', (fetched & 0x0001) == 0x0001);
		short temp = (short) ((0x00FF & fetched) >> 1);
		setFlag('Z', (temp & 0x00FF) == 0x0000);
		setFlag('N', (temp & 0x0080) == 0x0080);
		if (lookup[Byte.toUnsignedInt(opcode)].addressMode.equals("IMP"))
		{
			a = (byte) ((byte) (temp) & 0x00FF);
		}
		else
		{
			cpuBus.write(addressAbsolute, (byte) (temp & 0x00FF));
		}
	}

	public void NOP()
	{
		additionalCycles++;
	}

	public void ORA()
	{
		fetch();
		a |= fetched;
		setFlag('Z', a == 0x00);
		setFlag('N', (a & 0x80) == 0x80);

		additionalCycles++;
	}

	public void PHA()
	{
		//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), a);
		//stackPointer--;
		pushStack(a);
	}

	public void PHX()
	{
		//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), x);
		//stackPointer--;
		pushStack(x);
	}

	public void PHY()
	{
		//cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), y);
		//stackPointer--;
		pushStack(y);
	}

	
	public void PHP()
	{
		cpuBus.write((short) (0x0100 + Byte.toUnsignedInt(stackPointer)), (byte) (flags | 0b00110000));
		stackPointer--;
		pushStack((byte) (flags | 0b00110000));
		setFlag('B', false);
		setFlag('U', false);
	}

	public void PLA()
	{
		//stackPointer++;
		//a = cpuBus.read((short) (0x0100 + Byte.toUnsignedInt(stackPointer)));
		a = popStack();
		setFlag('Z', a == 0);
		setFlag('N', (a & 0x80) == 0x80);
	}

	public void PLX()
	{
		//stackPointer++;
		//x = cpuBus.read((short) (0x0100 + Byte.toUnsignedInt(stackPointer)));
		x = popStack();
		setFlag('Z', a == 0);
		setFlag('N', (a & 0x80) == 0x80);
	}	
	
	public void PLY()
	{
		//stackPointer++;
		//y = cpuBus.read((short) (0x0100 + Byte.toUnsignedInt(stackPointer)));
		y = popStack();
		setFlag('Z', a == 0);
		setFlag('N', (a & 0x80) == 0x80);
	}
	
	public void PLP()
	{
		//stackPointer++;
		//flags = cpuBus.read((short) (0x0100 + Byte.toUnsignedInt(stackPointer)));
		flags = popStack();
		setFlag('U', true);
	}

	public void ROL()
	{
		fetch();
		short temp = (short) ((fetched << 1) | (getFlag('C') ? 1 : 0));
		setFlag('C', (temp & 0xFF00) == 0xFF00);
		setFlag('Z', (temp & 0x00FF) == 0x0000);
		setFlag('N', (temp & 0x0080) == 0x0080);
		if (lookup[Byte.toUnsignedInt(opcode)].addressMode.equals("IMP"))
		{
			a = (byte) (temp & 0x00FF);
		}
		else
		{
			cpuBus.write(addressAbsolute, (byte) (temp & 0x00FF));
		}
	}

	public void ROR()
	{
		fetch();
		short temp = (short) (((0x00FF & fetched) >> 1) | (short) (getFlag('C') ? 0x0080 : 0));
		setFlag('C', (fetched & 0x01) == 0x01);
		setFlag('Z', (temp & 0x00FF) == 0x0000);
		setFlag('N', (temp & 0x0080) == 0x0080);
		if (lookup[Byte.toUnsignedInt(opcode)].addressMode.equals("IMP"))
		{
			a = (byte) (temp & 0x00FF);
		}
		else
		{
			cpuBus.write(addressAbsolute, (byte) (temp & 0x00FF));
		}
	}
	
	public void RTI()
	{
		//System.out.println("**RTI**");		
		//stackPointer++;
		//flags = cpuBus.read((short) (0x100 + Byte.toUnsignedInt(stackPointer)));
		flags = popStack();
		flags = (byte) (flags & (getFlag('B') ? 0b11101111 : 0));
		flags = (byte) (flags & (getFlag('U') ? 0b11011111 : 0));

		//stackPointer++;
		//byte lo = cpuBus.read((short) (0x100 + Byte.toUnsignedInt(stackPointer)));
		byte lo = popStack();
		//stackPointer++;
		//byte hi = cpuBus.read((short) (0x100 + Byte.toUnsignedInt(stackPointer)));
		byte hi = popStack();
		programCounter = (short) (Byte.toUnsignedInt(lo) + 256 * Byte.toUnsignedInt(hi));
		
		//System.out.println("RTI PC:" + Integer.toHexString(programCounter & 0xFFFF) );
		history.add("RTI PC:" + Convert.toHex16String(programCounter & 0xFFFF));

		cycles = pushCycles;
	}

	public void RTS()
	{
		//stackPointer++;
		//byte lo = cpuBus.read((short) (0x100 + Byte.toUnsignedInt(stackPointer)));
		byte lo = popStack();
		//stackPointer++;
		//byte hi = cpuBus.read((short) (0x100 + Byte.toUnsignedInt(stackPointer)));
		byte hi = popStack();
		programCounter = (short) (Byte.toUnsignedInt(lo) + 256 * Byte.toUnsignedInt(hi));
		
		
		programCounter++;
	}

	public void SBC()
	{
		fetch();
		short value = (short) ((short) fetched ^ 0x00FF);
		short temp = (short) ((short) a + value + (short) (getFlag('C') ? 1 : 0));
		setFlag('C', temp > 255);
		setFlag('Z', (temp & 0x00FF) == 0);
		setFlag('N', (temp & 0x80) == 0x80);
		setFlag('V', (~((short) a ^ (short) fetched) & ((short) a ^ (short) temp) & 0x0080) == 0x0080);
		a = (byte) temp;
		additionalCycles++;
	}

	public void SEC()
	{
		setFlag('C', true);
	}

	public void SED()
	{
		setFlag('D', true);
	}

	public void SEI()
	{
		//System.out.println("**SEI**");
		//this.interruptsEnabled = false;
		setFlag('I', true);
	}

	public void STA()
	{
		//System.out.println("STA:" +  addressAbsolute);
		//System.out.println("STA:" +  Integer.toHexString((int)addressAbsolute & 0x0000FFFF));
		cpuBus.write(addressAbsolute, a);
	}

	public void STX()
	{
		cpuBus.write(addressAbsolute, x);
	}

	public void STY()
	{
		cpuBus.write(addressAbsolute, y);
	}

	public void TAX()
	{
		x = a;
		setFlag('Z', x == 0x00);
		setFlag('N', (x & 0x80) == 0x80);
	}

	public void TAY()
	{
		y = a;
		setFlag('Z', y == 0x00);
		setFlag('N', (y & 0x80) == 0x80);
	}

	public void TSX()
	{
		x = stackPointer;
		setFlag('Z', x == 0x00);
		setFlag('N', (x & 0x80) == 0x80);
	}

	public void TXA()
	{
		a = x;
		setFlag('Z', a == 0x00);
		setFlag('N', (a & 0x80) == 0x80);
	}

	public void TXS()
	{
		stackPointer = x;
	}

	public void TYA()
	{
		a = y;
		setFlag('Z', a == 0x00);
		setFlag('N', (a & 0x80) == 0x80);
	}

	public void XXX()
	{
		System.out.println(
				"Illegal Opcode at $" + Integer.toHexString(Short.toUnsignedInt(programCounter)).toUpperCase() + " ("
						+ Convert.byteToHexString(opcode) + ") - " + lookup[Byte.toUnsignedInt(opcode)].opcode);
	}

	@Override
	public Telemetry getTelemetry()
	{
		return telemetry;
	}

	@Override
	public void pulse()
	{
		this.clock();		
	}

	@Override
	public String[] getFeatures()
	{
		return new String[]{"interrupt-hold","cycles"};
	}

	@Override
	public boolean isEnabled(String feature)
	{
		if("interrupt-hold".equals(feature))
			return debugger_irq_hold;
		
		return false;
	}

	@Override
	public void setEnabled(String feature, boolean state)
	{
		if("interrupt-hold".equals(feature))
			debugger_irq_hold = state;		
	}


	public void setBus(Bus bus) 
	{
		cpuBus = bus;	
		this.history.clear();
		this.reset();
	}

	@Override
	public IOSize getAddressableSize()
	{
		return IOSize.IO16Bit;
	}

	@Override
	public IOSize getDataSize()
	{
		return IOSize.IO8Bit;
	}

	@Override
	public boolean isOddAlignmentValid(IOSize size)
	{
		return true;
	}


	@Override
	public void enable()
	{
		bDebugControl = true;
	}


	@Override
	public void disable()
	{
		bDebugControl = false;		
	}

	@Override
	public void addStepListener(DebugListener dsl)
	{
		this.debugSteps.add(dsl);		
	}

	@Override
	public void addClockListener(DebugListener dsl)
	{
		this.debugClocks.add(dsl);
	}

	@Override
	public void addExecutionListener(DebugListener dsl)
	{
		this.debugBreaks.add(dsl);
	}	
}