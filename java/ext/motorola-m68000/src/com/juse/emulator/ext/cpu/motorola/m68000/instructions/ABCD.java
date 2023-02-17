package com.juse.emulator.ext.cpu.motorola.m68000.instructions;

import com.juse.emulator.ext.cpu.motorola.m68000.*;

import static com.juse.emulator.ext.cpu.motorola.m68000.Cpu.*;



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
public class ABCD implements InstructionHandler
{
	protected final Cpu cpu;

	public ABCD(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base;
		Instruction i;

		for(int f = 0; f < 2; f++)
		{
			if(f == 0)
			{
				// data reg mode
				base = 0xc100;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return abcd_dr(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, true);
					}
				};
			}
			else
			{
				// addr reg mode
				base = 0xc108;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return abcd_ar(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, false);
					}
				};
			}

			for(int d = 0; d < 8; d++)
			{
				for(int s = 0; s < 8; s++)
				{
					is.addInstruction(base + (d << 9) + s, i);
				}
			}
		}
	}

	protected final int abcd_dr(int opcode) {
		int sreg = (opcode & 0x07);
		int dreg = (opcode >> 9) & 0x07;
		int s = cpu.getDataRegisterByte(sreg);
		int d = cpu.getDataRegisterByte(dreg);

		int result = calc(cpu, s, d);
		cpu.setDataRegisterByte(dreg, result);
		return 6;
	}

	protected final int abcd_ar(int opcode) {
		int sreg = (opcode & 0x07);
		int dreg = (opcode >> 9) & 0x07;
		cpu.decrementAddrRegister(sreg, 1);
		int s = cpu.readMemoryByte(cpu.getAddrRegisterLong(sreg));
		cpu.decrementAddrRegister(dreg, 1);
		int d = cpu.readMemoryByte(cpu.getAddrRegisterLong(dreg));

		int result = calc(cpu, s, d);
		cpu.writeMemoryByte(cpu.getAddrRegisterLong(dreg), result);
		return 18;
	}

	/**
	 * Code and research courtesy of flamewing
	 * http://gendev.spritesmind.net/forum/viewtopic.php?f=2&t=1964
	 * https://github.com/flamewing/68k-bcd-verifier
	 * <p>
	 * computes the undefined N,V flags
	 */
	protected final static int calc(Cpu cpu, int s, int d) {
		int x = (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		int z = (cpu.isFlagSet(Cpu.Z_FLAG) ? 1 : 0);
		int c;

		int ss = (s + d + x) & 0xFF;
		// Normal carry computation for addition:
		// (sm & dm) | (~rm & dm) | (sm & ~rm)
		int bc = ((s & d) | (~ss & s) | (~ss & d)) & 0x88;
		// Compute if we have a decimal carry in both nibbles:
		int dc = (((ss + 0x66) ^ ss) & 0x110) >> 1;
		int corf = (bc | dc) - ((bc | dc) >> 2);
		int rr = (ss + corf) & 0xFF;

		// Compute flags.
		// Carry has two parts: normal carry for addition
		// (computed above) OR'ed with normal carry for
		// addition with corf:
		// (sm & dm) | (~rm & dm) | (sm & ~rm)
		// but simplified because sm = 0 and ~sm = 1 for corf:
		c = x = (bc | (ss & ~rr)) >> 7;
		// Normal overflow computation for addition with corf:
		// (sm & dm & ~rm) | (~sm & ~dm & rm)
		// but simplified because sm = 0 and ~sm = 1 for corf:
		int v = (~ss & rr) >> 7;
		// Accumulate zero flag:
		z = z & ((rr == 0) ? 1 : 0);
		int n = rr >> 7;

		int ccr = ((x & 1) << X_FLAG_BITS) | ((n & 1) << N_FLAG_BITS) | ((z & 1) << Z_FLAG_BITS) |
				((v & 1) << V_FLAG_BITS) | ((c & 1) << C_FLAG_BITS);
		cpu.setCCRegister(ccr);
		return rr;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, boolean data_reg_mode)
	{
		DisassembledOperand src;
		DisassembledOperand dst;

		if(data_reg_mode)
		{
			src = new DisassembledOperand("d" + (opcode & 0x07));
			dst = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));
		}
		else
		{
			src = new DisassembledOperand("-(a" + (opcode & 0x07) + ")");
			dst = new DisassembledOperand("-(a" + ((opcode >> 9) & 0x07) + ")");
		}
		return new DisassembledInstruction(address, opcode, "abcd", src, dst);
	}
}
