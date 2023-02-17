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
public class MOVEP implements InstructionHandler
{
	protected final Cpu cpu;

	public MOVEP(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base;
		Instruction i;

		// r2m
		for(int sz = 0; sz < 2; sz++)
		{
			if(sz == 0)
			{
				//word
				base = 0x0188;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return r2m_word(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word, true);
					}
				};
			}
			else
			{
				// long
				base = 0x01c8;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return r2m_long(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long, true);
					}
				};
			}

			for(int dreg = 0; dreg < 8; dreg++)
			{
				for(int areg = 0; areg < 8; areg++)
				{
					is.addInstruction(base + (dreg << 9) + areg, i);
				}
			}
		}

		// m2r
		for(int sz = 0; sz < 2; sz++)
		{
			if(sz == 0)
			{
				//word
				base = 0x0108;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return m2r_word(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word, false);
					}
				};
			}
			else
			{
				// long
				base = 0x0148;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return m2r_long(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long, false);
					}
				};
			}

			for(int dreg = 0; dreg < 8; dreg++)
			{
				for(int areg = 0; areg < 8; areg++)
				{
					is.addInstruction(base + (dreg << 9) + areg, i);
				}
			}
		}
	}

	protected final int r2m_word(int opcode)
	{
		int dis = cpu.fetchPCWordSigned();
		int address = cpu.getAddrRegisterLong(opcode & 0x07) + dis;
		int val = cpu.getDataRegisterWord((opcode >> 9) & 0x07);
		cpu.writeMemoryByte(address, (val >>> 8) & 0xff);
		cpu.writeMemoryByte(address + 2, val & 0xff);
		return 16;
	}

	protected final int r2m_long(int opcode)
	{
		int dis = cpu.fetchPCWordSigned();
		int address = cpu.getAddrRegisterLong(opcode & 0x07) + dis;
		int val = cpu.getDataRegisterLong((opcode >> 9) & 0x07);
		cpu.writeMemoryByte(address, (val >>> 24) & 0xff);
		cpu.writeMemoryByte(address + 2, (val >>> 16) & 0xff);
		cpu.writeMemoryByte(address + 4, (val >>> 8) & 0xff);
		cpu.writeMemoryByte(address + 6, val & 0xff);
		return 24;
	}

	protected final int m2r_word(int opcode)
	{
		int dis = cpu.fetchPCWordSigned();
		int address = cpu.getAddrRegisterLong(opcode & 0x07) + dis;
		int val = 0;

		val = cpu.readMemoryByte(address) << 8;
		val |= cpu.readMemoryByte(address + 2);

		cpu.setDataRegisterWord((opcode >> 9) & 0x07, val);
		return 16;
	}

	protected final int m2r_long(int opcode)
	{
		int dis = cpu.fetchPCWordSigned();
		int address = cpu.getAddrRegisterLong(opcode & 0x07) + dis;
		int val = 0;

		val = cpu.readMemoryByte(address) << 24;
		val |= cpu.readMemoryByte(address + 2) << 16;
		val |= cpu.readMemoryByte(address + 4) << 8;
		val |= cpu.readMemoryByte(address + 6);

		cpu.setDataRegisterLong((opcode >> 9) & 0x07, val);
		return 24;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz, boolean r2m)
	{
		DisassembledOperand src;
		DisassembledOperand dst;
		int dis = cpu.readMemoryWordSigned(address + 2);

		if(r2m)
		{
			src = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));
			dst = new DisassembledOperand(String.format("#$%04x(a%d)", dis, (opcode & 0x07)), 2, dis);
		}
		else
		{
			src = new DisassembledOperand(String.format("#$%04x(a%d)", dis, (opcode & 0x07)), 2, dis);
			dst = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));
		}
		return new DisassembledInstruction(address, opcode, "movep" + sz.ext(), src, dst);
	}
}
