package com.juse.emulator.devices.cpu.Motorola.m68000.instructions;

import com.juse.emulator.devices.cpu.Motorola.m68000.*;
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

public class CHK implements InstructionHandler
{
	protected final Cpu cpu;

	public CHK(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public void register(InstructionSet is)
	{
		int base = 0x4180;
		Instruction i = new Instruction() {
			public int execute(int opcode)
			{
				return chk(opcode);
			}
			public DisassembledInstruction disassemble(int address, int opcode)
			{
				return disassembleOp(address, opcode, Size.Word);
			}
		};

		for(int ea_mode = 0; ea_mode < 8; ea_mode++)
		{
			if(ea_mode == 1)
				continue;

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

	protected final int chk(int opcode)
	{
		int reg = (opcode >> 9) & 0x07;
		int dval = cpu.getDataRegisterWordSigned(reg);

		Operand op = cpu.resolveSrcEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Word);
		int sval = op.getWord();
		boolean raiseException = false;

		if(dval < 0)
		{
			cpu.setFlags(Cpu.N_FLAG);
			raiseException = true;
		}
		else if(dval > sval)
		{
			cpu.clrFlags(Cpu.N_FLAG);
			raiseException = true;
		}

		if(raiseException)
		{
			// CHK exception is vector 6
			cpu.raiseException(6);
			return 40 + op.getTiming();
		}

		return 10 + op.getTiming();
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand src = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));
		DisassembledOperand dst = cpu.disassembleDstEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);

		return new DisassembledInstruction(address, opcode, "chk", src, dst);
	}
}
