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
public class ADD implements InstructionHandler
{
	protected final Cpu cpu;

	public ADD(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base;
		Instruction i;

		// destination dn
		for(int sz = 0; sz < 3; sz++)
		{
			if(sz == 0)
			{
				// add byte (dn dest)
				base = 0xd000;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return add_byte_dn_dest(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				// add word (dn dest)
				base = 0xd040;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return add_word_dn_dest(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// add long (dn dest)
				base = 0xd080;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return add_long_dn_dest(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}
			for(int reg = 0; reg < 8; reg++)
			{
				for(int ea_mode = 0; ea_mode < 8; ea_mode++)
				{
					for(int ea_reg = 0; ea_reg < 8; ea_reg++)
					{
						if(ea_mode == 7 && ea_reg > 4)
							break;
						is.addInstruction(base + (reg << 9) + (ea_mode << 3) + ea_reg, i);
					}
				}
			}
		}

		// destination ea
		for(int sz = 0; sz < 3; sz++)
		{
			if(sz == 0)
			{
				// add byte (ea dest)
				base = 0xd100;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return add_byte_ea_dest(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				// add word (ea dest)
				base = 0xd140;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return add_word_ea_dest(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// add long (ea dest)
				base = 0xd180;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return add_long_ea_dest(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}
			for(int reg = 0; reg < 8; reg++)
			{
				for(int ea_mode = 2; ea_mode < 8; ea_mode++)
				{
					for(int ea_reg = 0; ea_reg < 8; ea_reg++)
					{
						if(ea_mode == 7 && ea_reg > 1)
							break;
						is.addInstruction(base + (reg << 9) + (ea_mode << 3) + ea_reg, i);
					}
				}
			}
		}
	}

	protected final int add_byte_dn_dest(int opcode)
	{
		//data reg destination
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Byte);
		int s = src.getByteSigned();
		int reg = (opcode >> 9) & 0x07;
		int d = cpu.getDataRegisterByteSigned(reg);
		int r = s + d;
		cpu.setDataRegisterByte(reg, r);
		int time = 4 + src.getTiming();
		cpu.calcFlags(InstructionType.ADD, s, d, r, Size.Byte);
		return time;
	}

	protected final int add_byte_ea_dest(int opcode)
	{
		int s = cpu.getDataRegisterByteSigned((opcode >> 9) & 0x07);
		Operand dst = cpu.resolveDstEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Byte);
		int d = dst.getByteSigned();
		int r = s + d;
		dst.setByte(r);
		int time = 8 + dst.getTiming();
		cpu.calcFlags(InstructionType.ADD, s, d, r, Size.Byte);
		return time;
	}

	protected final int add_word_dn_dest(int opcode)
	{
		//data reg destination
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Word);
		int s = src.getWordSigned();
		int reg = (opcode >> 9) & 0x07;
		int d = cpu.getDataRegisterWordSigned(reg);
		int r = s + d;
		cpu.setDataRegisterWord(reg, r);
		int time = 4 + src.getTiming();
		cpu.calcFlags(InstructionType.ADD, s, d, r, Size.Word);
		return time;
	}

	protected final int add_word_ea_dest(int opcode)
	{
		int s = cpu.getDataRegisterWordSigned((opcode >> 9) & 0x07);
		Operand dst = cpu.resolveDstEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Word);
		int d = dst.getWordSigned();
		int r = s + d;
		dst.setWord(r);
		int time = 8 + dst.getTiming();
		cpu.calcFlags(InstructionType.ADD, s, d, r, Size.Word);
		return time;
	}

	protected final int add_long_dn_dest(int opcode)
	{
		//data reg destination
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Long);
		int s = src.getLong();
		int reg = (opcode >> 9) & 0x07;
		int d = cpu.getDataRegisterLong(reg);
		int r = s + d;
		cpu.setDataRegisterLong(reg, r);
		int time = 6 + src.getTiming();
		cpu.calcFlags(InstructionType.ADD, s, d, r, Size.Long);
		return time;
	}

	protected final int add_long_ea_dest(int opcode)
	{
		int s = cpu.getDataRegisterLong((opcode >> 9) & 0x07);
		Operand dst = cpu.resolveDstEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Long);
		int d = dst.getLong();
		int r = s + d;
		dst.setLong(r);
		int time = 12 + dst.getTiming();
		cpu.calcFlags(InstructionType.ADD, s, d, r, Size.Long);
		return time;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand src;
		DisassembledOperand dst;
		if((opcode & 0x0100) == 0)
		{
			//data reg destination
			src = cpu.disassembleSrcEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);
			dst = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));
		}
		else
		{
			//ea destination
			src = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));
			dst = cpu.disassembleDstEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);
		}

		return new DisassembledInstruction(address, opcode, "add" + sz.ext(), src, dst);
	}
}
