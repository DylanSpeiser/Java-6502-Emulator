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
public class SUBA implements InstructionHandler
{
	protected final Cpu cpu;

	public SUBA(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base;
		Instruction i;

		// destination An
		for(int sz = 0; sz < 2; sz++)
		{
			if(sz == 0)
			{
				// suba word
				base = 0x90c0;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return suba_word(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// adda long
				base = 0x91c0;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return suba_long(opcode);
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
	}

	protected final int suba_word(int opcode)
	{
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Word);
		int s = src.getWordSigned();
		int reg = (opcode >> 9) & 0x07;
		int d = cpu.getAddrRegisterLong(reg);
		int r = d - s;
		cpu.setAddrRegisterLong(reg, r);

		//No flags affected

		return 8 + src.getTiming();
	}

	protected final int suba_long(int opcode)
	{
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Long);
		int s = src.getLong();
		int reg = (opcode >> 9) & 0x07;
		int d = cpu.getAddrRegisterLong(reg);
		int r = d - s;
		cpu.setAddrRegisterLong(reg, r);

		//No flags affected

		return 6 + src.getTiming();
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand src = cpu.disassembleSrcEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);
		DisassembledOperand dst = new DisassembledOperand("a" + ((opcode >> 9) & 0x07));

		return new DisassembledInstruction(address, opcode, "suba" + sz.ext(), src, dst);
	}
}
