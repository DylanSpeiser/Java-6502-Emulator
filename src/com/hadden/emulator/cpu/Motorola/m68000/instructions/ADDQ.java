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
public class ADDQ implements InstructionHandler
{
	protected final Cpu cpu;

	public ADDQ(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base;
		Instruction i;

		// destination ea
		for(int sz = 0; sz < 3; sz++)
		{
			if(sz == 0)
			{
				// addq byte
				base = 0x5000;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return addq_byte(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				// addq word
				base = 0x5040;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return addq_word(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// addq long
				base = 0x5080;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return addq_long(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}
			for(int ea_mode = 0; ea_mode < 8; ea_mode++)
			{
				if(sz == 0 && ea_mode == 1)
					continue;

				for(int ea_reg = 0; ea_reg < 8; ea_reg++)
				{
					if(ea_mode == 7 && ea_reg > 1)
						break;

					for(int imm = 0; imm < 8; imm++)
					{
						is.addInstruction(base + (imm << 9) + (ea_mode << 3) + ea_reg, i);
					}
				}
			}
		}
	}

	protected final int addq_byte(int opcode)
	{
		int s = (opcode >> 9 & 0x07);
		if(s == 0)
			s = 8;

		Operand dst = cpu.resolveDstEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Byte);
		int d = dst.getByteSigned();
		int r = s + d;
		dst.setByte(r);
		cpu.calcFlags(InstructionType.ADD, s, d, r, Size.Byte);
		return (dst.isRegisterMode() ? 4 : 8 + dst.getTiming());
	}

	protected final int addq_word(int opcode)
	{
		// ADDQ where the destination is an address register does not affect the flags and the ENTIRE address
		// reg is affected by the addition (same not true for byte sized, as there is no byte sized addq with address reg).
		int s = (opcode >> 9 & 0x07);
		if(s == 0)
			s = 8;

		int mode = (opcode >> 3) & 0x07;
		if (mode!=1)
		{
			Operand dst = cpu.resolveDstEA(mode, (opcode & 0x07), Size.Word);
			int d = dst.getWordSigned();
			int r = s + d;
			dst.setWord(r);
			cpu.calcFlags(InstructionType.ADD, s, d, r, Size.Word);
			return (dst.isRegisterMode() ? 4 : 8 + dst.getTiming());
		}
		else
		{
			int reg=opcode & 0x07;
			cpu.setAddrRegisterLong(reg,cpu.getAddrRegisterLong(reg)+s);
			return 4;
		}
	}

	protected final int addq_long(int opcode)
	{
		int s = (opcode >> 9 & 0x07);
		if(s == 0)
			s = 8;

		int mode = (opcode >> 3) & 0x07;
		Operand dst = cpu.resolveDstEA(mode, (opcode & 0x07), Size.Long);
		int d = dst.getLong();
		int r = s + d;
		dst.setLong(r);

		// if destination is An then no CC affected
		if(mode != 1)
			cpu.calcFlags(InstructionType.ADD, s, d, r, Size.Long);

		return (dst.isRegisterMode() ? 8 : 12 + dst.getTiming());
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		int s = (opcode >> 9 & 0x07);
		if(s == 0)
			s = 8;
		DisassembledOperand src = new DisassembledOperand("#" + s);
		DisassembledOperand dst = cpu.disassembleDstEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);

		return new DisassembledInstruction(address, opcode, "addq" + sz.ext(), src, dst);
	}
}
