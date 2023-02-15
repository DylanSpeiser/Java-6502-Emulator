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
public class MOVE_USP implements InstructionHandler
{
	protected final Cpu cpu;

	public MOVE_USP(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base;
		Instruction i;

		for(int t = 0; t < 2; t++)
		{
			if(t == 0)
			{
				// move to usp
				base = 0x4e60;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return move_to_usp(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, true);
					}
				};
			}
			else
			{
				//move from usp
				base = 0x4e68;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return move_from_usp(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, false);
					}
				};
			}

			for(int reg = 0; reg < 8; reg++)
			{
				is.addInstruction(base + reg, i);
			}
		}
	}

	protected final int move_to_usp(int opcode)
	{
		if(!cpu.isSupervisorMode())
		{
			cpu.raiseSRException();
			return 34;
		}

		cpu.setUSP(cpu.getAddrRegisterLong(opcode & 0x07));
		return 4;
	}

	protected final int move_from_usp(int opcode)
	{
		if(!cpu.isSupervisorMode())
		{
			cpu.raiseSRException();
			return 34;
		}

		cpu.setAddrRegisterLong(opcode & 0x07, cpu.getUSP());
		return 4;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, boolean move_to_usp)
	{
		DisassembledOperand src;
		DisassembledOperand dst;
		if(move_to_usp)
		{
			//move to usp
			src = new DisassembledOperand("a" + (opcode & 0x07));
			dst = new DisassembledOperand("usp");
		}
		else
		{
			// move from usp
			src = new DisassembledOperand("usp");
			dst = new DisassembledOperand("a" + (opcode & 0x07));
		}

		return new DisassembledInstruction(address, opcode, "move", src, dst);
	}
}
