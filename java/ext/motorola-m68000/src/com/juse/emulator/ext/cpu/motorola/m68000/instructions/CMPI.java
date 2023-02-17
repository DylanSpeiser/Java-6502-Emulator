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

public class CMPI implements InstructionHandler
{
	protected final Cpu cpu;

	public CMPI(Cpu cpu)
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
				base = 0x0c00;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return cmpi_byte(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				base = 0x0c40;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return cmpi_word(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				base = 0x0c80;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return cmpi_long(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}

			for(int ea_mode = 0; ea_mode < 8; ea_mode++)
			{
				if(ea_mode == 1)
					continue;

				for(int ea_reg = 0; ea_reg < 8; ea_reg++)
				{
					if(ea_mode == 7 && ea_reg > 1)
						break;

					is.addInstruction(base + (ea_mode << 3) + ea_reg, i);
				}
			}
		}
	}

	protected final int cmpi_byte(int opcode)
	{
		int s = CpuUtils.signExtendByte(cpu.fetchPCWord());
		Operand op = cpu.resolveDstEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Byte);

		int d = op.getByteSigned();

		int r = d - s;

		cpu.calcFlags(InstructionType.CMP, s, d, r, Size.Byte);

		return (op.isRegisterMode() ? 8 : 8 + op.getTiming());
	}

	protected final int cmpi_word(int opcode)
	{
		int s = cpu.fetchPCWordSigned();
		Operand op = cpu.resolveDstEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Word);

		int d = op.getWordSigned();

		int r = d - s;

		cpu.calcFlags(InstructionType.CMP, s, d, r, Size.Word);
		return (op.isRegisterMode() ? 8 : 8 + op.getTiming());
	}

	protected final int cmpi_long(int opcode)
	{
		int s = cpu.fetchPCLong();
		Operand op = cpu.resolveDstEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Long);

		int d = op.getLong();

		int r = d - s;

		cpu.calcFlags(InstructionType.CMP, s, d, r, Size.Long);

		return (op.isRegisterMode() ? 14 : 12 + op.getTiming());
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		int val;
		int bytes_read;
		String op;

		switch(sz)
		{
			case Byte:
			{
				val = cpu.readMemoryWord(address + 2);
				bytes_read = 2;
				op = String.format("#$%02x", (val & 0xff));
				break;
			}
			case Word:
			{
				val = cpu.readMemoryWord(address + 2);
				bytes_read = 2;
				op = String.format("#$%04x", (val & 0x0000ffff));
				break;
			}
			case Long:
			{
				val = cpu.readMemoryLong(address + 2);
				bytes_read = 4;
				op = String.format("#$%08x", val);
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Invalid size for CMPI");
			}
		}

		DisassembledOperand src = new DisassembledOperand(op, bytes_read, val);
		DisassembledOperand dst = cpu.disassembleDstEA(address + 2 + bytes_read, (opcode >> 3) & 0x07, (opcode & 0x07), sz);

		return new DisassembledInstruction(address, opcode, "cmpi" + sz.ext(), src, dst);
	}
}
