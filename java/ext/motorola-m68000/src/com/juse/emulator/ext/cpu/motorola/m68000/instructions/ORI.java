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
public class ORI implements InstructionHandler
{
	protected final Cpu cpu;

	public ORI(Cpu cpu)
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
				// ori byte
				base = 0x0000;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return ori_byte(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				// ori word
				base = 0x0040;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return ori_word(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// ori long
				base = 0x0080;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return ori_long(opcode);
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
					if(ea_mode == 7 && (ea_reg == 2 || ea_reg == 3 || ea_reg == 4))
						continue;

					is.addInstruction(base + (ea_mode << 3) + ea_reg, i);
				}
			}
		}
	}

	protected int ori_byte(int opcode)
	{
		int s = CpuUtils.signExtendByte(cpu.fetchPCWord());
		Operand dst = cpu.resolveDstEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Byte);
		int d = dst.getByteSigned();
		int r = s | d;
		dst.setByte(r);
		if(!dst.isSR())
			cpu.calcFlags(InstructionType.OR, s, d, r, Size.Byte);

		return (dst.isRegisterMode() ? 8 : 12 + dst.getTiming());
	}

	protected int ori_word(int opcode)
	{
		int s = cpu.fetchPCWordSigned();
		Operand dst = cpu.resolveDstEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Word);
		int d = dst.getWordSigned();
		int r = s | d;
		if(dst.isSR())
		{
			if(cpu.isSupervisorMode())
			{
				cpu.setSR(r);
			}
			else
			{
				cpu.raiseSRException();
				return 34;
			}
		}
		else
		{
			dst.setWord(r);
			cpu.calcFlags(InstructionType.OR, s, d, r, Size.Word);
		}
		return (dst.isRegisterMode() ? 8 : 12 + dst.getTiming());
	}

	protected int ori_long(int opcode)
	{
		int s = cpu.fetchPCLong();
		Operand dst = cpu.resolveDstEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Long);
		int d = dst.getLong();
		int r = s | d;
		dst.setLong(r);
		cpu.calcFlags(InstructionType.OR, s, d, r, Size.Long);
		return (dst.isRegisterMode() ? 16 : 20 + dst.getTiming());
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		int imm_bytes;
		int imm;
		String is;

		switch(sz)
		{
			case Byte:
			{
				imm = cpu.readMemoryWord(address + 2);
				is = String.format("#$%02x", imm & 0x00ff);
				imm_bytes = 2;
				break;
			}
			case Word:
			{
				imm = cpu.readMemoryWord(address + 2);
				is = String.format("#$%04x", imm);
				imm_bytes = 2;
				break;
			}
			case Long:
			{
				imm = cpu.readMemoryLong(address + 2);
				is = String.format("#$%08x", imm);
				imm_bytes = 4;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Size unsized for ORI");
			}
		}

		DisassembledOperand src = new DisassembledOperand(is, imm_bytes, imm);
		DisassembledOperand dst = cpu.disassembleDstEA(address + 2 + imm_bytes, (opcode >> 3) & 0x07, (opcode & 0x07), sz);

		return new DisassembledInstruction(address, opcode, "ori" + sz.ext(), src, dst);
	}
}
