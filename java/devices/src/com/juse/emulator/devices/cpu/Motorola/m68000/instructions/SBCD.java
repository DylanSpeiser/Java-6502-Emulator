package com.juse.emulator.devices.cpu.Motorola.m68000.instructions;

import com.juse.emulator.devices.cpu.Motorola.m68000.*;

import static com.juse.emulator.devices.cpu.Motorola.m68000.Cpu.*;

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
public class SBCD implements InstructionHandler
{
	protected final Cpu cpu;

	public SBCD(Cpu cpu)
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
				base = 0x8100;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return sbcd_dr(opcode);
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
				base = 0x8108;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return sbcd_ar(opcode);
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

	protected final int sbcd_dr(int opcode) {
		int sreg = (opcode & 0x07);
		int dreg = (opcode >> 9) & 0x07;
		int s = cpu.getDataRegisterByte(sreg);
		int d = cpu.getDataRegisterByte(dreg);

		int result = calc(cpu, s, d);
		cpu.setDataRegisterByte(dreg, result);
		return 6;
	}

	protected final int sbcd_ar(int opcode) {
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
	public static final int calc(Cpu cpu, int yy, int xx) {
		int x = (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		int z = (cpu.isFlagSet(Cpu.Z_FLAG) ? 1 : 0);
		int c = (cpu.isFlagSet(Cpu.C_FLAG) ? 1 : 0);
		int dd = (xx - yy - x) & 0xFF;
		// Normal carry computation for subtraction:
		// (sm & ~dm) | (rm & ~dm) | (sm & rm)
		int bc = ((~xx & yy) | (dd & ~xx) | (dd & yy)) & 0x88;
		int corf = (bc - (bc >> 2)) & 0xFF;
		int rr = (dd - corf) & 0xFF;
		// Compute flags.
		// Carry has two parts: normal carry for subtraction
		// (computed above) OR'ed with normal carry for
		// subtraction with corf:
		// (sm & ~dm) | (rm & ~dm) | (sm & rm)
		// but simplified because sm = 0 and ~sm = 1 for corf:
		x = c = ((bc | (~dd & rr)) >> 7) & 0xFF;
		// Normal overflow computation for subtraction with corf:
		// (~sm & dm & ~rm) | (sm & ~dm & rm)
		// but simplified because sm = 0 and ~sm = 1 for corf:
		int v = ((dd & ~rr) >> 7) & 0xFF;
		;
		// Accumulate zero flag:
		z = z & ((rr == 0) ? 1 : 0);
		int n = (rr >> 7) & 0xFF;

		int ccr = ((x & 1) << X_FLAG_BITS) | ((n & 1) << N_FLAG_BITS) | ((z & 1) << Z_FLAG_BITS)
				| ((v & 1) << V_FLAG_BITS) | ((c & 1) << C_FLAG_BITS);
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
		return new DisassembledInstruction(address, opcode, "sbcd", src, dst);
	}
}
