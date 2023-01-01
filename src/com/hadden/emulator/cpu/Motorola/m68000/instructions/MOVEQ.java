package com.hadden.emulator.cpu.Motorola.m68000.instructions;

import com.hadden.emulator.cpu.Motorola.m68000.*;

/*
//  M68k - Java Amiga MachineCore
//  Copyright (c) 2008, Tony Headford
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
// $Revision: 21 $
*/
public class MOVEQ implements InstructionHandler
{
	protected final Cpu cpu;

	public MOVEQ(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base = 0x7000;
		Instruction i = new Instruction() {
			public int execute(int opcode)
			{
				return moveq(opcode);
			}
			public DisassembledInstruction disassemble(int address, int opcode)
			{
				return disassembleOp(address, opcode);
			}
		};

		for(int reg = 0; reg < 8; reg++)
		{
			for(int imm = 0; imm < 256; imm++)
			{
				is.addInstruction(base + (reg << 9) + imm, i);
			}
		}
	}

	protected final int moveq(int opcode)
	{
		int reg = (opcode >> 9 & 0x07);
		int data = CpuUtils.signExtendWord(CpuUtils.signExtendByte(opcode & 0xff));
		cpu.setDataRegisterLong(reg, data);
		if(data < 0)
		{
			cpu.setFlags(Cpu.N_FLAG);
		}
		else
		{
			cpu.clrFlags(Cpu.N_FLAG);
		}
		if(data == 0)
		{
			cpu.setFlags(Cpu.Z_FLAG);
		}
		else
		{
			cpu.clrFlags(Cpu.Z_FLAG);
		}
		cpu.clrFlags(Cpu.C_FLAG | Cpu.V_FLAG);
		return 4;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode)
	{
		DisassembledOperand src = new DisassembledOperand(String.format("#$%02x", opcode & 0xff));
		DisassembledOperand dst = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));

		return new DisassembledInstruction(address, opcode, "moveq", src, dst);
	}
}
