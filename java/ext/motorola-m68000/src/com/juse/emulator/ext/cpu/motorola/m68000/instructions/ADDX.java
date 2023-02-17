package com.juse.emulator.ext.cpu.motorola.m68000.instructions;

import com.juse.emulator.ext.cpu.motorola.m68000.*;

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

public class ADDX implements InstructionHandler
{
	protected final Cpu cpu;

	public ADDX(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base;
		Instruction i;

		// register mode
		for(int sz = 0; sz < 3; sz++)
		{
			if(sz == 0)
			{
				// addx byte (reg)
				base = 0xd100;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return addx_byte_reg(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				// addx word (reg)
				base = 0xd140;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return addx_word_reg(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// addx long (reg)
				base = 0xd180;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return addx_long_reg(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}
			for(int regx = 0; regx < 8; regx++)
			{
				for(int regy = 0; regy < 8; regy++)
				{
					is.addInstruction(base + (regx << 9) + regy, i);
				}
			}
		}

		// Memory mode
		for(int sz = 0; sz < 3; sz++)
		{
			if(sz == 0)
			{
				// addx byte (mem)
				base = 0xd108;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return addx_byte_mem(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				// addx word (mem)
				base = 0xd148;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return addx_word_mem(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// addx long (mem)
				base = 0xd188;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return addx_long_mem(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}
			for(int regx = 0; regx < 8; regx++)
			{
				for(int regy = 0; regy < 8; regy++)
				{
					is.addInstruction(base + (regx << 9) + regy, i);
				}
			}
		}
	}

	protected int addx_byte_reg(int opcode)
	{
		int s = cpu.getDataRegisterByteSigned((opcode & 0x07));
		int d = cpu.getDataRegisterByteSigned((opcode >> 9) & 0x07);
		int r = s + d + (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		cpu.setDataRegisterByte((opcode >> 9) & 0x07, r);
		cpu.calcFlags(InstructionType.ADDX, s, d, r, Size.Byte);
		return 4;
	}

	protected int addx_word_reg(int opcode)
	{
		int s = cpu.getDataRegisterWordSigned((opcode & 0x07));
		int d = cpu.getDataRegisterWordSigned((opcode >> 9) & 0x07);
		int r = s + d + (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		cpu.setDataRegisterWord((opcode >> 9) & 0x07, r);
		cpu.calcFlags(InstructionType.ADDX, s, d, r, Size.Word);
		return 4;
	}

	protected int addx_long_reg(int opcode)
	{
		int s = cpu.getDataRegisterLong((opcode & 0x07));
		int d = cpu.getDataRegisterLong((opcode >> 9) & 0x07);
		int r = s + d + (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		cpu.setDataRegisterLong((opcode >> 9) & 0x07, r);
		cpu.calcFlags(InstructionType.ADDX, s, d, r, Size.Long);
		return 8;
	}

	protected int addx_byte_mem(int opcode)
	{
		int rx = (opcode >> 9) & 0x07;
		int ry = (opcode & 0x07);
		cpu.decrementAddrRegister(ry, 1);
		int s = cpu.readMemoryByteSigned(cpu.getAddrRegisterLong(ry));
		cpu.decrementAddrRegister(rx, 1);
		int d = cpu.readMemoryByteSigned(cpu.getAddrRegisterLong(rx));
		int r = s + d + (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		cpu.writeMemoryByte(cpu.getAddrRegisterLong(rx), r);
		cpu.calcFlags(InstructionType.ADDX, s, d, r, Size.Byte);
		return 18;
	}

	protected int addx_word_mem(int opcode) {
		int rx = (opcode >> 9) & 0x07;
		int ry = (opcode & 0x07);
		cpu.decrementAddrRegister(ry, 2);
		int s = cpu.readMemoryWordSigned(cpu.getAddrRegisterLong(ry));
		cpu.decrementAddrRegister(rx, 2);
		int d = cpu.readMemoryWordSigned(cpu.getAddrRegisterLong(rx));
		int r = s + d + (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		cpu.writeMemoryWord(cpu.getAddrRegisterLong(rx), r);
		cpu.calcFlags(InstructionType.ADDX, s, d, r, Size.Word);
		return 18;
	}

	protected int addx_long_mem(int opcode) {
		int rx = (opcode >> 9) & 0x07;
		int ry = (opcode & 0x07);
		cpu.decrementAddrRegister(ry, 4);
		int s = cpu.readMemoryLong(cpu.getAddrRegisterLong(ry));
		cpu.decrementAddrRegister(rx, 4);
		int d = cpu.readMemoryLong(cpu.getAddrRegisterLong(rx));
		int r = s + d + (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		cpu.writeMemoryLong(cpu.getAddrRegisterLong(rx), r);
		cpu.calcFlags(InstructionType.ADDX, s, d, r, Size.Long);
		return 30;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand src;
		DisassembledOperand dst;

		if((opcode & 0x08) == 0)
		{
			// data reg mode
			src = new DisassembledOperand("d" + (opcode & 0x07));
			dst = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));
		}
		else
		{
			//memory mode
			src = new DisassembledOperand("-(a" + (opcode & 0x07) + ")");
			dst = new DisassembledOperand("-(a" + ((opcode >> 9) & 0x07) + ")");
		}

		return new DisassembledInstruction(address, opcode, "addx" + sz.ext(), src, dst);
	}
}
