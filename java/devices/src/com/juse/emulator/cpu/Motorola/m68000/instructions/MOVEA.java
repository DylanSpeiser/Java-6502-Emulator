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
public class MOVEA implements InstructionHandler
{
	protected final Cpu cpu;

	public MOVEA(Cpu cpu)
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
				base = 0x3040;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return movea_word(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				base = 0x2040;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return movea_long(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}

			for(int ea_mode = 0; ea_mode < 8; ea_mode++)
			{
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
		}

	}

	protected final int movea_word(int opcode)
	{
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Word);
		cpu.setAddrRegisterLong((opcode >> 9) & 0x07, src.getWordSigned());
		return MOVE.ShortExecutionTime[src.index()][1];
	}

	protected final int movea_long(int opcode)
	{
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Long);
		cpu.setAddrRegisterLong((opcode >> 9) & 0x07, src.getLong());
		return MOVE.LongExecutionTime[src.index()][1];
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand src = cpu.disassembleSrcEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);
		DisassembledOperand dst = new DisassembledOperand("a" + ((opcode >> 9) & 0x07));

		return new DisassembledInstruction(address, opcode, "movea" + sz.ext(), src, dst);
	}
}
