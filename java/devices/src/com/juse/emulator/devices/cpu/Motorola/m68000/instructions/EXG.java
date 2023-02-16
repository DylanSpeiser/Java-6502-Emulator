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
public class EXG implements InstructionHandler
{
	protected final Cpu cpu;
	enum ExgMode { EXG_DATA, EXG_ADDR, EXG_DATA_ADDR };

	public EXG(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base;
		Instruction i;

		// destination ea
		for(int mode = 0; mode < 3; mode++)
		{
			if(mode == 0)
			{
				// exg data regs
				base = 0xc140;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return exg_dd(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, ExgMode.EXG_DATA);
					}
				};
			}
			else if(mode == 1)
			{
				// exg address regs
				base = 0xc148;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return exg_aa(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, ExgMode.EXG_ADDR);
					}
				};
			}
			else
			{
				// exg data addr regs
				base = 0xc188;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return exg_da(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, ExgMode.EXG_DATA_ADDR);
					}
				};
			}
			for(int rx = 0; rx < 8; rx++)
			{
				for(int ry = 0; ry < 8; ry++)
				{
					is.addInstruction(base + (rx << 9) + ry, i);
				}
			}
		}
	}

	protected final int exg_dd(int opcode)
	{
		int rx = (opcode >> 9) & 0x07;
		int ry = (opcode & 0x07);

		int x = cpu.getDataRegisterLong(rx);
		int y = cpu.getDataRegisterLong(ry);
		cpu.setDataRegisterLong(rx, y);
		cpu.setDataRegisterLong(ry, x);
		return 6;
	}

	protected final int exg_aa(int opcode)
	{
		int rx = (opcode >> 9) & 0x07;
		int ry = (opcode & 0x07);

		int x = cpu.getAddrRegisterLong(rx);
		int y = cpu.getAddrRegisterLong(ry);
		cpu.setAddrRegisterLong(rx, y);
		cpu.setAddrRegisterLong(ry, x);
		return 6;
	}

	protected final int exg_da(int opcode)
	{
		int rx = (opcode >> 9) & 0x07;
		int ry = (opcode & 0x07);

		int x = cpu.getDataRegisterLong(rx);
		int y = cpu.getAddrRegisterLong(ry);
		cpu.setDataRegisterLong(rx, y);
		cpu.setAddrRegisterLong(ry, x);
		return 6;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, ExgMode mode)
	{
		DisassembledOperand src;
		DisassembledOperand dst;
		switch(mode)
		{
			case EXG_DATA:
			{
				src = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));
				dst = new DisassembledOperand("d" + (opcode & 0x07));
				break;
			}
			case EXG_ADDR:
			{
				src = new DisassembledOperand("a" + ((opcode >> 9) & 0x07));
				dst = new DisassembledOperand("a" + (opcode & 0x07));
				break;
			}
			case EXG_DATA_ADDR:
			{
				src = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));
				dst = new DisassembledOperand("a" + (opcode & 0x07));
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Invalid exg type specified");
			}
		}

		return new DisassembledInstruction(address, opcode, "exg", src, dst);
	}
}
