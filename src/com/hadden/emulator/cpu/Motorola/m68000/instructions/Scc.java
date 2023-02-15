package com.hadden.emulator.cpu.Motorola.m68000.instructions;

import com.hadden.emulator.cpu.Motorola.m68000.*;
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

public class Scc implements InstructionHandler
{
	protected final Cpu cpu;
	protected static final String[] names = { "st", "sf", "shi", "sls", "scc", "scs", "sne", "seq",
												"svc", "svs", "spl", "smi", "sge", "slt", "sgt", "sle"};
	public Scc(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public void register(InstructionSet is)
	{
		int base = 0x50c0;
		Instruction i = new Instruction() {
			public int execute(int opcode)
			{
				return sxx(opcode);
			}
			public DisassembledInstruction disassemble(int address, int opcode)
			{
				return disassembleOp(address, opcode);
			}
		};

		for(int ea_mode = 0; ea_mode < 8; ea_mode++)
		{
			if(ea_mode == 1)
				continue;
			for(int ea_reg = 0; ea_reg < 8; ea_reg++)
			{
				if(ea_mode == 7 && ea_reg > 1)
					break;

				for(int cc = 0; cc < 16; cc++)
				{
					is.addInstruction(base + (cc << 8) + (ea_mode << 3) + ea_reg, i);
				}
			}
		}
	}

	protected final int sxx(int opcode)
	{
		Operand op = cpu.resolveDstEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Byte);
		int cc = (opcode >> 8) & 0x0f;
		int time;

		if(cpu.testCC(cc))
		{
			op.setByte(0xff);
			time = (op.isRegisterMode() ? 6 : 8 + op.getTiming());
		}
		else
		{
			// condition failed
			op.setByte(0);
			time = (op.isRegisterMode() ? 4 : 8 + op.getTiming());
		}

		return time;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode)
	{
		DisassembledOperand op = cpu.disassembleDstEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), Size.Byte);
		return new DisassembledInstruction(address, opcode, names[(opcode >> 8) & 0x0f], op);
	}
}
