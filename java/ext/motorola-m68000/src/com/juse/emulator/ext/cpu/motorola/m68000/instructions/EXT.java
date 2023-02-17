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
public class EXT implements InstructionHandler
{
	protected final Cpu cpu;

	public EXT(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base;
		Instruction i;

		for(int sz = 0; sz < 2; sz++)
		{
			if(sz == 0)
			{
				// ext byte to word
				base = 0x4880;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return ext_byte_to_word(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// ext word to long
				base = 0x48c0;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return ext_word_to_long(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}

			for(int reg = 0; reg < 8; reg++)
			{
				is.addInstruction(base + reg, i);
			}
		}
	}

	protected final int ext_byte_to_word(int opcode)
	{
		int s = cpu.getDataRegisterByte(opcode & 0x07);
		if((s & 0x80) == 0x80)
		{
			s |= 0xff00;
			cpu.setFlags(Cpu.N_FLAG);
		}
		else
		{
			cpu.clrFlags(Cpu.N_FLAG);
		}
		cpu.setDataRegisterWord((opcode & 0x07), s);
		if(s == 0)
		{
			cpu.setFlags(Cpu.Z_FLAG);
		}
		else
		{
			cpu.clrFlags(Cpu.Z_FLAG);
		}
		cpu.clrFlags(Cpu.C_FLAG | Cpu.V_FLAG);
		return 4;
	}

	protected final int ext_word_to_long(int opcode)
	{
		int s = cpu.getDataRegisterWord(opcode & 0x07);
		if((s & 0x8000) == 0x8000)
		{
			s |= 0xffff0000;
			cpu.setFlags(Cpu.N_FLAG);
		}
		else
		{
			cpu.clrFlags(Cpu.N_FLAG);
		}
		cpu.setDataRegisterLong((opcode & 0x07), s);
		if(s == 0)
		{
			cpu.setFlags(Cpu.Z_FLAG);
		}
		else
		{
			cpu.clrFlags(Cpu.Z_FLAG);
		}
		cpu.clrFlags(Cpu.C_FLAG | Cpu.V_FLAG);
		return 4;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand src = new DisassembledOperand("d" + (opcode & 0x07));

		return new DisassembledInstruction(address, opcode, "ext" + sz.ext(), src);
	}
}
