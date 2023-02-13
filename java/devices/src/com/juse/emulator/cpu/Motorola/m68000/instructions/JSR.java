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
public class JSR implements InstructionHandler
{
	protected final Cpu cpu;
	protected static final int[] TIMING = { 0, 0, 16, 0, 0, 18, 22, 18, 20, 18, 22 };

	public JSR(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base = 0x4e80;
		Instruction i = new Instruction() {
			public int execute(int opcode)
			{
				return jsr(opcode);
			}
			public DisassembledInstruction disassemble(int address, int opcode)
			{
				return disassembleOp(address, opcode, Size.Long);
			}
		};

		for(int ea_mode = 2; ea_mode < 8; ea_mode++)
		{
			if(ea_mode == 3 || ea_mode == 4)
				continue;

			for(int ea_reg = 0; ea_reg < 8; ea_reg++)
			{
				if(ea_mode == 7 && ea_reg > 3)
					break;
				is.addInstruction(base + (ea_mode << 3) + ea_reg, i);
			}
		}
	}

	protected final int jsr(int opcode)
	{
		int mode = (opcode >> 3) & 0x07;
		int reg = (opcode & 0x07);
		Operand op = cpu.resolveSrcEA(mode, reg, Size.Long);
		cpu.pushLong(cpu.getPC());
		cpu.setPC(op.getComputedAddress());

		int idx;
		if(mode == 7)
			idx = mode + reg;
		else
			idx = mode;

		return TIMING[idx];
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand op = cpu.disassembleSrcEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);
		return new DisassembledInstruction(address, opcode, "jsr", op);
	}
}
