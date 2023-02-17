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
public class MOVE implements InstructionHandler
{
	protected final Cpu cpu;
	static final int[][] ShortExecutionTime = {
			{ 4,  4,  8,  8,  8, 12, 14, 12, 16 },
			{ 4,  4,  8,  8,  8, 12, 14, 12, 16 },
			{ 8,  8, 12, 12, 12, 16, 18, 16, 20 },
			{ 8,  8, 12, 12, 12, 16, 18, 16, 20 },
			{ 10, 10, 14, 14, 14, 18, 20, 18, 22 },
			{ 12, 12, 16, 16, 16, 20, 22, 20, 24 },
			{ 14, 14, 18, 18, 18, 22, 24, 22, 26 },
			{ 12, 12, 16, 16, 16, 20, 22, 20, 24 },
			{ 16, 16, 20, 20, 20, 24, 26, 24, 28 },
			{ 12, 12, 16, 16, 16, 20, 22, 20, 24 },
			{ 14, 14, 18, 18, 18, 22, 24, 22, 26 },
			{ 8,  8, 12, 12, 12, 16, 18, 16, 20 }};

	static final int[][] LongExecutionTime = {
			{ 4,  4, 12, 12, 12, 16, 18, 16, 20 },
			{ 4,  4, 12, 12, 12, 16, 18, 16, 20 },
			{ 12, 12, 20, 20, 20, 24, 26, 24, 28 },
			{ 12, 12, 20, 20, 20, 24, 26, 24, 28 },
			{ 14, 14, 22, 22, 22, 26, 28, 26, 30 },
			{ 16, 16, 24, 24, 24, 28, 30, 28, 32 },
			{ 18, 18, 26, 26, 26, 30, 32, 30, 34 },
			{ 16, 16, 24, 24, 24, 28, 30, 28, 32 },
			{ 20, 20, 28, 28, 28, 32, 34, 32, 36 },
			{ 16, 16, 24, 24, 24, 28, 30, 28, 32 },
			{ 18, 18, 26, 26, 26, 30, 32, 30, 34 },
			{ 12, 12, 20, 20, 20, 24, 26, 24, 28 }};


	public MOVE(Cpu cpu)
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
				// move byte
				base = 0x1000;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return move_byte(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				// move word
				base = 0x3000;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return move_word(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// move long
				base = 0x2000;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return move_long(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}
			for(int sea_mode = 0; sea_mode < 8; sea_mode++)
			{
				for(int sea_reg = 0; sea_reg < 8; sea_reg++)
				{
					if(sea_mode == 7 && sea_reg > 4)
						break;

					for(int dea_mode = 0; dea_mode < 8; dea_mode++)
					{
						if(dea_mode == 1)
							continue;

						for(int dea_reg = 0; dea_reg < 8; dea_reg++)
						{
							if(dea_mode == 7 && dea_reg > 1)
								break;

							is.addInstruction(base + (dea_reg << 9) + (dea_mode << 6) + (sea_mode << 3) + sea_reg, i);
						}
					}
				}
			}
		}
	}

	protected final int move_byte(int opcode)
	{
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Byte);
		int s = src.getByte();
		Operand dst = cpu.resolveDstEA((opcode >> 6) & 0x07, (opcode >> 9) & 0x07, Size.Byte);
		dst.setByte(s);
		cpu.calcFlags(InstructionType.MOVE, s, s, s, Size.Byte);
		return ShortExecutionTime[src.index()][dst.index()];
	}

	protected final int move_word(int opcode) {
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Word);
		int s = src.getWord();
		Operand dst = cpu.resolveDstEA((opcode >> 6) & 0x07, (opcode >> 9) & 0x07, Size.Word);
		dst.setWord(s);
		cpu.calcFlags(InstructionType.MOVE, s, s, s, Size.Word);
		return ShortExecutionTime[src.index()][dst.index()];
	}

	protected final int move_long(int opcode) {
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Long);
		int s = src.getLong();
		Operand dst = cpu.resolveDstEA((opcode >> 6) & 0x07, (opcode >> 9) & 0x07, Size.Long);
		setLong(dst, s);
		cpu.calcFlags(InstructionType.MOVE, s, s, s, Size.Long);
		return LongExecutionTime[src.index()][dst.index()];
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz) {
		DisassembledOperand src = cpu.disassembleSrcEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);
		DisassembledOperand dst = cpu.disassembleDstEA(address + 2 + src.bytes, (opcode >> 6) & 0x07, (opcode >> 9) & 0x07, sz);

		return new DisassembledInstruction(address, opcode, "move" + sz.ext(), src, dst);
	}

	private void setLong(Operand dst, int s) {
		if (!(dst instanceof CpuCore.AddressRegisterPreDecOperand)) {
			dst.setLong(s);
		} else {
			//move.l + pre-dec: word writes order is inverted
			int address = dst.getComputedAddress();
			cpu.writeMemoryWord(address + 2, s & 0xFFFF);
			cpu.writeMemoryWord(address, (s >> 16) & 0xFFFF);
		}
	}
}
