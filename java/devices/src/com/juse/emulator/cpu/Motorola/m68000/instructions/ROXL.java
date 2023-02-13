package com.juse.emulator.cpu.Motorola.m68000.instructions;

import com.juse.emulator.cpu.Motorola.m68000.*;
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
public class ROXL implements InstructionHandler
{
	protected final Cpu cpu;

	public ROXL(Cpu cpu)
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
				// roxl byte imm
				base = 0xe110;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return roxl_byte_imm(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				// roxl word imm
				base = 0xe150;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return roxl_word_imm(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// roxl long imm
				base = 0xe190;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return roxl_long_imm(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}
			for(int imm = 0; imm < 8; imm++)
			{
				for(int reg = 0; reg < 8; reg++)
				{
					is.addInstruction(base + (imm << 9) + reg, i);
				}
			}
		}

		for(int sz = 0; sz < 3; sz++)
		{
			if(sz == 0)
			{
				// roxl byte reg
				base = 0xe130;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return roxl_byte_reg(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				// roxl word reg
				base = 0xe170;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return roxl_word_reg(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// roxl long reg
				base = 0xe1b0;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return roxl_long_reg(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}
			for(int imm = 0; imm < 8; imm++)
			{
				for(int reg = 0; reg < 8; reg++)
				{
					is.addInstruction(base + (imm << 9) + reg, i);
				}
			}
		}

		// roxl word mem
		base = 0xe5c0;
		i = new Instruction() {
			public int execute(int opcode)
			{
				return roxl_word_mem(opcode);
			}
			public DisassembledInstruction disassemble(int address, int opcode)
			{
				return disassembleOp(address, opcode, Size.Word);
			}
		};

		for(int ea_mode = 2; ea_mode < 8; ea_mode++)
		{
			for(int ea_reg = 0; ea_reg < 8; ea_reg++)
			{
				if(ea_mode == 7 && ea_reg > 1)
					break;
				is.addInstruction(base + (ea_mode << 3) + ea_reg, i);
			}
		}
	}

	protected int roxl_byte_imm(int opcode)
	{
		//  the state of the X cflag BEFORE each shift is moved into the lsb after each shift. Also, check for 0 count rotate if roxr reg (and not imm)
		//  also if roxl reg, the shift count is mod 64 not 32 finally, cond code reg is set directly here
		int shift = (opcode >> 9) & 0x07;
		if(shift == 0)
			shift = 8;

		int reg = (opcode & 0x07);
		int d = cpu.getDataRegisterByte(reg);

		int last_out;
		boolean xflag=cpu.isFlagSet(Cpu.X_FLAG);        // state of the X flag
		int maskFlags;                                  // there is no 0 rotate count here
		for(int s= 0; s < shift; s++)
		{
			last_out = d & 0x80;                    // bit rotated out before ths shift
			d <<= 1;
			if (xflag)
				d |= 1;                             // if xflag was set before the shift, set LSB
			if(last_out != 0)                       // bit goes to xflag
				xflag=true;
			else
				xflag=false;
		}
		d &= 0xff;
		cpu.setDataRegisterByte(reg, d);
		if (xflag)
			maskFlags=Cpu.X_FLAG+Cpu.C_FLAG;        // if last bit was 1, set flags accordingly
		else
			maskFlags=0;                            // these flags aren't set
		if (d==0)
			maskFlags+=Cpu.Z_FLAG;                  // Z flag set if result is 0
		if((d & 0x80)!=0)
			maskFlags+=Cpu.N_FLAG;                  // N flag for result (the V flag is always 0)
		cpu.setCCRegister(maskFlags);
		return 6 + shift + shift;
	}

	protected int roxl_word_imm(int opcode)
	{
		int shift = (opcode >> 9) & 0x07;
		if(shift == 0)
			shift = 8;

		int reg = (opcode & 0x07);
		int d = cpu.getDataRegisterWord(reg);

		int last_out;
		boolean xflag=cpu.isFlagSet(Cpu.X_FLAG);        // state of the X flag
		int maskFlags;                                  // there is no 0 rotate count here
		for(int s= 0; s < shift; s++)
		{
			last_out = d & 0x8000;                 // bit rotated out before ths shift
			d <<= 1;
			if (xflag)
				d |= 1;                             // if xflag was set before the shift, set LSB
			if(last_out != 0)                       // bit goes to xflag
				xflag=true;
			else
				xflag=false;
		}
		d &= 0x0000ffff;
		cpu.setDataRegisterWord(reg, d);
		if (xflag)
			maskFlags=Cpu.X_FLAG+Cpu.C_FLAG;        // if last bit was 1, set flags accordingly
		else
			maskFlags=0;                            // these flags aren't set
		if (d==0)
			maskFlags+=Cpu.Z_FLAG;
		if((d & 0x8000)!=0)
			maskFlags+=Cpu.N_FLAG;                      // N flag (the V flag is always 0)
		cpu.setCCRegister(maskFlags);
		return 6 + shift + shift;
	}

	protected int roxl_long_imm(int opcode)
	{
		int shift = (opcode >> 9) & 0x07;
		if(shift == 0)
			shift = 8;

		int reg = (opcode & 0x07);
		int d = cpu.getDataRegisterLong(reg);
		int last_out;
		boolean xflag=cpu.isFlagSet(Cpu.X_FLAG);        // state of the X flag
		int maskFlags;                                  // there is no 0 rotate count here
		for(int s= 0; s < shift; s++)
		{
			last_out = d & 0x80000000;              // bit rotated out before ths shift
			d <<= 1;
			if (xflag)
				d |= 1;                             // if xflag was set before the shift, set LSB
			if(last_out != 0)                       // bit goes to xflag
				xflag=true;
			else
				xflag=false;
		}
		cpu.setDataRegisterLong(reg, d);
		if (xflag)
			maskFlags=Cpu.X_FLAG+Cpu.C_FLAG;        // if last bit was 1, set flags accordingly
		else
			maskFlags=0;                            // these flags aren't set
		if (d==0)
			maskFlags+=Cpu.Z_FLAG;
		if((d & 0x80000000)!=0)
			maskFlags+=Cpu.N_FLAG;                      // N flag (the V flag is always 0)
		cpu.setCCRegister(maskFlags);
		return 8 + shift + shift;
	}

	protected int roxl_byte_reg(int opcode)
	{
		int shift = cpu.getDataRegisterLong((opcode >> 9) & 0x07) & 63;

		int reg = (opcode & 0x07);
		int d = cpu.getDataRegisterByte(reg);

		int last_out;
		boolean xflag=cpu.isFlagSet(Cpu.X_FLAG);        // state of the X flag
		int maskFlags=xflag?Cpu.X_FLAG:0;               // value of X flag at start of operation
		for(int s= 0; s < shift; s++)
		{
			last_out = d & 0x80;                    // state of last bit befrre rotate
			d <<= 1;                               
			if (xflag)                              // X flag was set before the rotate
				d |= 1;                             // so set it in LSB
			if(last_out != 0)                       // now set new state of X flag according to last bit moved out
				xflag=true;
			else
				xflag=false;
		}
		d &= 0xff;
		cpu.setDataRegisterByte(reg, d);
		if (shift!=0)                                   // X flags in unaffected by a 0 count rotate
		{
			if (xflag)
				maskFlags=Cpu.X_FLAG+Cpu.C_FLAG;        // if last bit was 1, set flags accordingly
			else
				maskFlags=0;                            // these flags aren't set
		}
		else
		{
			if (maskFlags!=0)
				maskFlags +=Cpu.C_FLAG;                 //  rotate count : c flag = state of x flag
		}
		if (d==0)
			maskFlags+=Cpu.Z_FLAG;
		if((d & 0x80)!=0)
			maskFlags+=Cpu.N_FLAG;                      // N flag (the V flag is always 0)
		cpu.setCCRegister(maskFlags);
		return 6 + shift + shift;
	}

	protected int roxl_word_reg(int opcode)
	{
		int shift = cpu.getDataRegisterLong((opcode >> 9) & 0x07) & 63;

		int reg = (opcode & 0x07);
		int d = cpu.getDataRegisterWord(reg);

		int last_out;
		boolean xflag=cpu.isFlagSet(Cpu.X_FLAG);        // state of the X flag
		int maskFlags=xflag?Cpu.X_FLAG:0;               // value of X flag at start of operation
		for(int s= 0; s < shift; s++)
		{
			last_out = d & 0x8000;                    // state of last bit befrre rotate
			d <<= 1;                               
			if (xflag)                              // X flag was set before the rotate
				d |= 1;                             // so set it in LSB
			if(last_out != 0)                       // now set new state of X flag according to last bit moved out
				xflag=true;
			else
				xflag=false;
		}
		d &= 0xffff;
		cpu.setDataRegisterWord(reg, d);
		if (shift!=0)                                   // X flags is unaffected by a 0 count rotate
		{
			if (xflag)
				maskFlags=Cpu.X_FLAG+Cpu.C_FLAG;        // if last bit was 1, set flags accordingly
			else
				maskFlags=0;                            // these flags aren't set
		}
		else
		{
			if (maskFlags!=0)
				maskFlags +=Cpu.C_FLAG;                 //  rotate count : c flag = state of x flag
		}
		if (d == 0)
			maskFlags+=Cpu.Z_FLAG;
		if((d & 0x8000)!=0)
			maskFlags+=Cpu.N_FLAG;                      // N flag (the V flag is always 0)
		cpu.setCCRegister(maskFlags);
		return 6 + shift + shift;
	}

	protected int roxl_long_reg(int opcode)
	{
		int shift = cpu.getDataRegisterLong((opcode >> 9) & 0x07) & 63;

		int reg = (opcode & 0x07);
		int d = cpu.getDataRegisterLong(reg);

		int last_out;
		boolean xflag=cpu.isFlagSet(Cpu.X_FLAG);        // state of the X flag
		int maskFlags=xflag?Cpu.X_FLAG:0;               // value of X flag at start of operation
		for(int s= 0; s < shift; s++)
		{
			last_out = d & 0x80000000;              // state of last bit before rotate
			d <<= 1;                               
			if (xflag)                              // X flag was set before the rotate
				d |= 1;                             // so set it in LSB
			if(last_out != 0)                       // now set new state of X flag according to last bit moved out
				xflag=true;
			else
				xflag=false;
		}
		cpu.setDataRegisterLong(reg, d);
		if (shift!=0)                                   // X flags is unaffected by a 0 count rotate
		{
			if (xflag)
				maskFlags=Cpu.X_FLAG + Cpu.C_FLAG;		// if last bit was 1, set flags accordingly
			else
				maskFlags = 0;  	                          // these flags aren't set
		}
		else
		{
			if (maskFlags!=0)
				maskFlags += Cpu.C_FLAG;                 //  rotate count : c flag = state of x flag
		}
		if (d==0)
			maskFlags += Cpu.Z_FLAG;
		if((d & 0x80000000) != 0)
			maskFlags += Cpu.N_FLAG;                      // N flag (the V flag is always 0)
		cpu.setCCRegister(maskFlags);
		return 8 + shift + shift;
	}

	protected int roxl_word_mem(int opcode)
	{
		Operand op = cpu.resolveDstEA((opcode >> 3) & 0x07, (opcode & 0x07),Size.Word);
		int v = op.getWord();
		int last_out = v & 0x8000;
		v <<= 1;
		if(cpu.isFlagSet(Cpu.X_FLAG))
			v |= 0x01;

		op.setWord(v);
		int maskFlags;
		if (last_out!=0)
			maskFlags=Cpu.X_FLAG+Cpu.C_FLAG;        // if last bit was 1, set flags accordingly
		else
			maskFlags=0;                            // these flags aren't set
		if ((v&0xffff)==0)
			maskFlags+=Cpu.Z_FLAG;
		if((v & 0x8000)!=0)
			maskFlags+=Cpu.N_FLAG;                      // N flag (the V flag is always 0)
		cpu.setCCRegister(maskFlags);
		return 8 + op.getTiming();
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand src;
		DisassembledOperand dst;

		if((opcode & 0x00c0) == 0x00c0)
		{
			//mem mode
			src = cpu.disassembleDstEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);
			return new DisassembledInstruction(address, opcode, "roxl" + sz.ext(), src);
		}
		else if((opcode & 0x0020) == 0x0020)
		{
			//reg mode
			src = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));
			dst = new DisassembledOperand("d" + (opcode & 0x07));
		}
		else
		{
			//immediate mode
			int count = (opcode >> 9) & 0x07;
			if(count == 0)
				count = 8;

			src = new DisassembledOperand("#" + count);
			dst = new DisassembledOperand("d" + (opcode & 0x07));
		}

		return new DisassembledInstruction(address, opcode, "roxl" + sz.ext(), src, dst);
	}

}
