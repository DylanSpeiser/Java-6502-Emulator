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
public class TAS implements InstructionHandler {
	//EMULATE_BROKEN_TAS -> hardware where write-back to *memory* doesn't work
	public static boolean EMULATE_BROKEN_TAS;

	protected final Cpu cpu;

	public TAS(Cpu cpu) {
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base = 0x4ac0;
		Instruction i = new Instruction() {
			public int execute(int opcode)
			{
				return tas(opcode);
			}
			public DisassembledInstruction disassemble(int address, int opcode)
			{
				return disassembleOp(address, opcode, Size.Byte);
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
				is.addInstruction(base + (ea_mode << 3) + ea_reg, i);
			}
		}
	}

	protected synchronized final int tas(int opcode)
	{
		//TODO: this is for multi-processor systems and provides an atomic read-modify-write - this isn't handled at the moment
                
		int mode = (opcode >> 3) & 0x07;
		int reg = (opcode & 0x07);
		Operand op = cpu.resolveSrcEA(mode, reg, Size.Byte);
		int v = op.getByte();

		if(v == 0)
		{
			cpu.setFlags(Cpu.Z_FLAG);
		}
		else
		{
			cpu.clrFlags(Cpu.Z_FLAG);
		}
		if ((v & 0x080) != 0) {
			cpu.setFlags(Cpu.N_FLAG);
		} else {
			cpu.clrFlags(Cpu.N_FLAG);
		}
		cpu.clrFlags(Cpu.C_FLAG | Cpu.V_FLAG);

		boolean writeBack = !EMULATE_BROKEN_TAS || (EMULATE_BROKEN_TAS && op.isRegisterMode());
		if (writeBack) {
			op.setByte(v | 0x80);
		}

		return (op.isRegisterMode() ? 4 : 14 + op.getTiming());
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand op = cpu.disassembleSrcEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);
		return new DisassembledInstruction(address, opcode, "tas", op);
	}
}
