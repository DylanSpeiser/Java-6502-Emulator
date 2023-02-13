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

public class CMPM implements InstructionHandler
{
	protected final Cpu cpu;

	public CMPM(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public void register(InstructionSet is)
	{
		int base;
		Instruction i;

		for(int sz = 0; sz < 3; sz++)
		{
			if(sz == 0)
			{
				base = 0xb108;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return cmpm_byte(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				base = 0xb148;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return cmpm_word(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				base = 0xb188;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return cmpm_long(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}

			for(int ax = 0; ax < 8; ax++)
			{
				for(int ay = 0; ay < 8; ay++)
				{
					is.addInstruction(base + (ax << 9) + ay, i);
				}
			}
		}
	}

	protected final int cmpm_byte(int opcode)
	{
		int ax = (opcode >> 9) & 0x07;
		int ay = (opcode & 0x07);

		int s = cpu.readMemoryByteSigned(cpu.getAddrRegisterLong(ay));
		cpu.incrementAddrRegister(ay, 1);
		int d = cpu.readMemoryByteSigned(cpu.getAddrRegisterLong(ax));
		cpu.incrementAddrRegister(ax, 1);

		int r = d - s;

		cpu.calcFlags(InstructionType.CMP, s, d, r, Size.Byte);

		return 12;
	}

	protected final int cmpm_word(int opcode)
	{
		int ax = (opcode >> 9) & 0x07;
		int ay = (opcode & 0x07);

		int s = cpu.readMemoryWordSigned(cpu.getAddrRegisterLong(ay));
		cpu.incrementAddrRegister(ay, 2);
		int d = cpu.readMemoryWordSigned(cpu.getAddrRegisterLong(ax));
		cpu.incrementAddrRegister(ax, 2);

		int r = d - s;

		cpu.calcFlags(InstructionType.CMP, s, d, r, Size.Word);

		return 12;
	}

	protected final int cmpm_long(int opcode)
	{
		int ax = (opcode >> 9) & 0x07;
		int ay = (opcode & 0x07);

		int s = cpu.readMemoryLong(cpu.getAddrRegisterLong(ay));
		cpu.incrementAddrRegister(ay, 4);
		int d = cpu.readMemoryLong(cpu.getAddrRegisterLong(ax));
		cpu.incrementAddrRegister(ax, 4);

		int r = d - s;

		cpu.calcFlags(InstructionType.CMP, s, d, r, Size.Long);

		return 20;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand src = new DisassembledOperand("(a" + (opcode & 0x07) + ")+");
		DisassembledOperand dst = new DisassembledOperand("(a" + ((opcode >> 9) & 0x07) + ")+");

		return new DisassembledInstruction(address, opcode, "cmpm" + sz.ext(), src, dst);
	}
}
