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
public class ORI_TO_SR implements InstructionHandler
{
	protected final Cpu cpu;

	public ORI_TO_SR(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public void register(InstructionSet is)
	{
		int base;
		Instruction i;

		   base = 0x007c;
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
                is.addInstruction(base, i);
	}
        
	protected int ori_word(int opcode)
	{
		int s = cpu.fetchPCWordSigned();
		if(cpu.isSupervisorMode())
		{
			cpu.setSR(cpu.getSR()|s);
		}
		else
		{
			cpu.raiseSRException();
			return 34;
		}
		return 8;                                   // i'm not sure this is true
	}

        
	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		int imm_bytes;
		int imm;
		String is;
		imm = cpu.readMemoryWord(address + 2);
		is = String.format("#$%04x", imm);
		imm_bytes = 2;
		DisassembledOperand src = new DisassembledOperand(is, imm_bytes, imm);
		DisassembledOperand dst = cpu.disassembleDstEA(address + 2 + imm_bytes, (opcode >> 3) & 0x07, (opcode & 0x07), sz);
		return new DisassembledInstruction(address, opcode, "ori" + sz.ext(), src, dst);
	}
        
	protected final DisassembledInstruction bdisassembleOp(int address, int opcode, Size sz)
	{
		String is = String.format("#$%04x", cpu.readMemoryWord(address + 2));
		return new DisassembledInstruction(address, opcode, "ori.w  "+is+",SR" );
	}
}
