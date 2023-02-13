package com.juse.emulator.cpu.Motorola.m68000.instructions;

import com.juse.emulator.cpu.Motorola.m68000.*;
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

public class BTST implements InstructionHandler
{
	protected final Cpu cpu;

	public BTST(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public void register(InstructionSet is)
	{
		int base = 0x0100;
		Instruction i;

		// dynamic mode
		for(int ea_mode = 0; ea_mode < 8; ea_mode++)
		{
			if(ea_mode == 1)
				continue;

			if(ea_mode == 0)
			{
				// data register destination
				i = new Instruction() {
					public int execute(int opcode)
					{
						return btst_dyn_long(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}
			else
			{
				i = new Instruction() {
					public int execute(int opcode)
					{
						return btst_dyn_byte(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			for(int ea_reg = 0; ea_reg < 8; ea_reg++)
			{
				if(ea_mode == 7 && ea_reg > 4)
					break;

				for(int r = 0; r < 8; r++)
				{
					is.addInstruction(base + (r << 9) + (ea_mode << 3) + ea_reg, i);
				}

			}
		}

		// static mode
		base = 0x0800;
		for(int ea_mode = 0; ea_mode < 8; ea_mode++)
		{
			if(ea_mode == 1)
				continue;

			if(ea_mode == 0)
			{
				// data register destination
				i = new Instruction() {
					public int execute(int opcode)
					{
						return btst_static_long(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}
			else
			{
				i = new Instruction() {
					public int execute(int opcode)
					{
						return btst_static_byte(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			for(int ea_reg = 0; ea_reg < 8; ea_reg++)
			{
				if(ea_mode == 7 && ea_reg > 3)
					break;

				is.addInstruction(base + (ea_mode << 3) + ea_reg, i);
			}
		}

	}

	protected final int btst_dyn_byte(int opcode)
	{
		// for memory destination, the bitnbr is MOD 8 - for data reg destination, the bitnbr is mod 32
		int bit = cpu.getDataRegisterLong((opcode >> 9) & 0x07) & 7;
		bit = 1 << bit;
		// memory destination
		Operand op = cpu.resolveSrcEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Byte);
		int val = op.getByte();

		// Z_FLAG set according to original value
		if ((val & bit) != 0) {
			cpu.clrFlags(Cpu.Z_FLAG);
		} else {
			cpu.setFlags(Cpu.Z_FLAG);
		}

		return 4 + op.getTiming();
	}

	protected final int btst_dyn_long(int opcode)
	{
		// for memory destination, the bitnbr is MOD 8 - for data reg destination, the bitnbr is mod 32
		int bit =cpu.getDataRegisterLong((opcode >> 9) & 0x07) & 31;
		bit=1 << bit;
		// data register destination
		Operand op = cpu.resolveDstEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Long);
		int val = op.getLong();

		// Z_FLAG set according to original value
		if((val & bit) != 0)
		{
			cpu.clrFlags(Cpu.Z_FLAG);
		}
		else
		{
			cpu.setFlags(Cpu.Z_FLAG);
		}

		return 6;
	}


	protected final int btst_static_byte(int opcode)
	{
		// for memory destination, the bitnbr is MOD 8 - for data reg destination, the bitnbr is mod 32
		int bit = cpu.fetchPCWord() & 0x07;
		bit = 1 << bit;

		// memory destination
		Operand op = cpu.resolveDstEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Byte);
		int val = op.getByte();

		// Z_FLAG set according to original value
		if((val & bit) != 0)
		{
			cpu.clrFlags(Cpu.Z_FLAG);
		}
		else
		{
			cpu.setFlags(Cpu.Z_FLAG);
		}
		return 8 + op.getTiming();
	}

	protected final int btst_static_long(int opcode)
	{
		// for memory destination, the bitnbr is MOD 8 - for data reg destination, the bitnbr is mod 32
		int bit = cpu.fetchPCWord() & 31;
		bit = 1 << bit;
		
		// data register destination
		Operand op = cpu.resolveDstEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Long);
		int val = op.getLong();

		// Z_FLAG set according to original value
		if((val & bit) != 0)
		{
			cpu.clrFlags(Cpu.Z_FLAG);
		}
		else
		{
			cpu.setFlags(Cpu.Z_FLAG);
		}
		return 10;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand src;
		int bytes = 2;

		if((opcode & 0x0100) != 0)
		{
			//dynamic mode
			src = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));
		}
		else
		{
			//static mode
			int ext = cpu.readMemoryWord(address + 2);
			int val;
			if(((opcode >> 3) & 0x07) == 0)
			{
				val = ext & 0x1f;
			}
			else
			{
				val = ext & 0x07;
			}
			src = new DisassembledOperand(String.format("#$%x", val), 2, ext);
			bytes += 2;
		}

		DisassembledOperand dst = cpu.disassembleDstEA(address + bytes, (opcode >> 3) & 0x07, (opcode & 0x07), sz);

		return new DisassembledInstruction(address, opcode, "btst", src, dst);
	}
}
