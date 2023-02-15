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

public class CLR implements InstructionHandler
{
	protected final Cpu cpu;

	public CLR(Cpu cpu)
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
				base = 0x4200;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return clr_byte(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				base = 0x4240;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return clr_word(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				base = 0x4280;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return clr_long(opcode);
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

	protected final int clr_byte(int opcode)
	{
		Operand op = cpu.resolveDstEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Byte);

		//anomaly as the memory is always read (might affect mapped memory) before being cleared
		op.getByte();
		op.setByte(0);

		int flags = cpu.getCCRegister();
		flags &= ~(Cpu.N_FLAG | Cpu.C_FLAG | Cpu.V_FLAG);
		flags |= Cpu.Z_FLAG;
		cpu.setCCRegister(flags);

		return (op.isRegisterMode() ? 4 : 8 + op.getTiming());
	}

	protected final int clr_word(int opcode)
	{
		Operand op = cpu.resolveDstEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Word);

		//anomaly as the memory is always read (might affect mapped memory) before being cleared
		op.getWord();
		op.setWord(0);

		int flags = cpu.getCCRegister();
		flags &= ~(Cpu.N_FLAG | Cpu.C_FLAG | Cpu.V_FLAG);
		flags |= Cpu.Z_FLAG;
		cpu.setCCRegister(flags);

		return (op.isRegisterMode() ? 4 : 8 + op.getTiming());
	}

	protected final int clr_long(int opcode)
	{
		Operand op = cpu.resolveDstEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Long);

		//anomaly as the memory is always read (might affect mapped memory) before being cleared
		op.getLong();
		op.setLong(0);

		int flags = cpu.getCCRegister();
		flags &= ~(Cpu.N_FLAG | Cpu.C_FLAG | Cpu.V_FLAG);
		flags |= Cpu.Z_FLAG;
		cpu.setCCRegister(flags);

		return (op.isRegisterMode() ? 6 : 12 + op.getTiming());
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand dst = cpu.disassembleDstEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);

		return new DisassembledInstruction(address, opcode, "clr" + sz.ext(), dst);
	}
}
