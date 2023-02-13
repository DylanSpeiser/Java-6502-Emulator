package com.juse.emulator.cpu.Motorola.m68000;

import com.juse.emulator.cpu.Motorola.m68000.memory.AddressSpace;

/*
//  M68k - Java Amiga MachineCore
//  Copyright (c) 2008-2010, Tony Headford
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
//  following conditions are met:
//
//    o  Redistributions of source code must retain the above copyright notice, this list of conditions and the
//       following disclaimer.
//    o  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
//       following disclaimer in the documentation and/or other materials provided with the distribution.
//    o  Neither the name of the M68k Project nor the names of its contributors may be used to endorse or promote
//       products derived from this software without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
//  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
//  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
//  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
//  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
//  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
*/
public interface Cpu {
	public static final int C_FLAG_BITS = 0;
	public static final int V_FLAG_BITS = 1;
	public static final int Z_FLAG_BITS = 2;
	public static final int N_FLAG_BITS = 3;
	public static final int X_FLAG_BITS = 4;
	public static final int C_FLAG = 1 << C_FLAG_BITS;
	public static final int V_FLAG = 1 << V_FLAG_BITS;
	public static final int Z_FLAG = 1 << Z_FLAG_BITS;
	public static final int N_FLAG = 1 << N_FLAG_BITS;
	public static final int X_FLAG = 1 << X_FLAG_BITS;
	public static final int INTERRUPT_FLAGS_MASK = 0x0700;
	public static final int SUPERVISOR_FLAG = 0x2000;
	public static final int TRACE_FLAG = 0x8000;

	int CCR_MASK = 0x1F;
	int SR_MASK = 0xE700 | CCR_MASK; //0xe71f

	public void setAddressSpace(AddressSpace memory);

	public void reset();

	public void resetExternal();

	public void stop();

	public int execute();
	
	// data registers
	public int getDataRegisterByte(int reg);
	public int getDataRegisterByteSigned(int reg);
	public int getDataRegisterWord(int reg);
	public int getDataRegisterWordSigned(int reg);
	public int getDataRegisterLong(int reg);
	public void setDataRegisterByte(int reg, int value);
	public void setDataRegisterWord(int reg, int value);
	public void setDataRegisterLong(int reg, int value);
	// address registers
	public int getAddrRegisterByte(int reg);
	public int getAddrRegisterByteSigned(int reg);
	public int getAddrRegisterWord(int reg);
	public int getAddrRegisterWordSigned(int reg);
	public int getAddrRegisterLong(int reg);
	public void setAddrRegisterByte(int reg, int value);
	public void setAddrRegisterWord(int reg, int value);
	public void setAddrRegisterLong(int reg, int value);
	//memory interface
	public int readMemoryByte(int addr);
	public int readMemoryByteSigned(int addr);
	public int readMemoryWord(int addr);
	public int readMemoryWordSigned(int addr);
	public int readMemoryLong(int addr);
	public void writeMemoryByte(int addr, int value);
	public void writeMemoryWord(int addr, int value);
	public void writeMemoryLong(int addr, int value);
	//addr reg helpers
	public void incrementAddrRegister(int reg, int numBytes);
	public void decrementAddrRegister(int reg, int numBytes);
	
	// PC reg
	public int getPC();
	public void setPC(int address);
	// pc fetches - for reading data following instructions and incrementing the PC afterwards
	public int fetchPCWord();
	public int fetchPCWordSigned();
	public int fetchPCLong();
	// status reg
	public boolean isSupervisorMode();
	public int getCCRegister();
	public int getSR();
	public void setCCRegister(int value);
	public void setSR(int value);
	public void setSR2(int value);
	//flags
	public void setFlags(int flags);
	public void clrFlags(int flags);
	public boolean isFlagSet(int flag);
	public void calcFlags(InstructionType type, int s, int d, int r, Size sz);
	public void calcFlagsParam(InstructionType type, int s, int d, int r, int extraParam, Size sz);
	public boolean testCC(int cc);

	// stacks
	public int getUSP();
	public void setUSP(int address);
	public int getSSP();
	public void setSSP(int address);
	public void pushWord(int value);
	public void pushLong(int value);
	public int popWord();
	public int popLong();
	
	// exceptions & interrupts
	public void raiseException(int vector);
	public void raiseSRException();
	public void raiseInterrupt(int priority);
	public int getInterruptLevel();

	//source EA
	public Operand resolveSrcEA(int mode, int reg, Size sz);
	// destination EA
	public Operand resolveDstEA(int mode, int reg, Size sz);

	// disassembling
	public Instruction getInstructionAt(int address);
	public Instruction getInstructionFor(int opcode);
	public DisassembledOperand disassembleSrcEA(int address, int mode, int reg, Size sz);
	public DisassembledOperand disassembleDstEA(int address, int mode, int reg, Size sz);
}
