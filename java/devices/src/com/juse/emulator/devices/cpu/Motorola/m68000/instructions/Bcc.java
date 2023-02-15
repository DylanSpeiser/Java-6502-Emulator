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
public class Bcc implements InstructionHandler
{
	protected final Cpu cpu;
	protected static final String[] names = { "bra", "bsr", "bhi", "bls", "bcc", "bcs", "bne", "beq",
												"bvc", "bvs", "bpl", "bmi", "bge", "blt", "bgt", "ble"};
	public Bcc(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public void register(InstructionSet is)
	{
		int base = 0x6000;
		Instruction ib = new Instruction() {
			public int execute(int opcode)
			{
				return bxx_byte(opcode);
			}
			public DisassembledInstruction disassemble(int address, int opcode)
			{
				return disassembleOp(address, opcode);
			}
		};

		Instruction iw = new Instruction() {
			public int execute(int opcode)
			{
				return bxx_word(opcode);
			}
			public DisassembledInstruction disassemble(int address, int opcode)
			{
				return disassembleOp(address, opcode);
			}
		};

		for(int cc = 0; cc < 16; cc++)
		{
			is.addInstruction(base + (cc << 8), iw);

			for(int dis = 1; dis < 256; dis++)
			{
				is.addInstruction(base + (cc << 8) + dis, ib);
			}
		}
	}

	protected final int bxx_byte(int opcode)
	{
		int dis = CpuUtils.signExtendByte(opcode & 0xff);
		int cc = (opcode >> 8) & 0x0f;
		int pc = cpu.getPC();
		int time;

		if(cc == 1)
		{
			//bsr
			cpu.pushLong(pc);
			cpu.setPC(pc + dis);
			time = 18;
		}
		else
		{
			if(cpu.testCC(cc))
			{
				cpu.setPC(pc + dis);
				time = 10;
			}
			else
			{
				// condition failed
				time = 8;
			}
		}
		return time;
	}

	protected final int bxx_word(int opcode)
	{
		int pc = cpu.getPC();
		int dis = cpu.readMemoryWordSigned(pc);
		int cc = (opcode >> 8) & 0x0f;
		int time;

		if(cc == 1)
		{
			//bsr
			cpu.pushLong(pc + 2);
			cpu.setPC(pc + dis);
			time = 18;
		}
		else
		{
			if(cpu.testCC(cc))
			{
				cpu.setPC(pc + dis);
				time = 10;
			}
			else
			{
				// condition failed
				time = 12;
				cpu.setPC(pc + 2);
			}
		}
		return time;

	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode)
	{
		DisassembledOperand op;

		int cc = (opcode >> 8) & 0x0f;
		int dis = CpuUtils.signExtendByte(opcode & 0xff);
		String name;

		if(dis != 0)
		{
			op = new DisassembledOperand(String.format("$%08x", dis + address + 2));
			name = names[cc] + ".s";
		}
		else
		{
			//word displacement
			dis = cpu.readMemoryWordSigned(address + 2);
			op = new DisassembledOperand(String.format("$%08x", dis + address + 2), 2, dis);
			name = names[cc] + ".w";
		}

		return new DisassembledInstruction(address, opcode, name, op);
	}

}
