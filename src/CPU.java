import java.util.Arrays;

public class CPU {
	public byte flags = 0x00;
	//C,Z,I,D,B,U,V,N
	//Carry, Zero, Interrupt Disable, Decimal, Break, Unused, Overflow, Negative

	public byte a = 0x00;
	public byte x = 0x00;
	public byte y = 0x00;
	public byte stackPointer = 0x00;
	public short programCounter = 0x0000;

	public boolean debug = false;

	public short addressAbsolute = 0x0000;
	public short addressRelative = 0x0000;
	public byte opcode = 0x00;
	public int cycles = 0;

	public double ClocksPerSecond = 0;
	public int clockDelta = 1;
	public int lastClocks = 0;

	public long startTime = 0;
	public long timeDelta = 1;
	public long lastTime = System.nanoTime();

	public int additionalCycles = 0;

	public boolean interruptRequested = false;
	public boolean NMinterruptRequested = false;

	public boolean stopped = false, waiting = false;

	public Instruction[] lookup = new Instruction[0x100];

	public CPU() {
		reset();

		Arrays.fill(lookup, new Instruction(OpCode.XXX, AddressMode.IMP, 2, false));

		// useful reference for instructions: https://www.masswerk.at/6502/6502_instruction_set.html
		
		//ADC
		lookup[0x69] = new Instruction(OpCode.ADC, AddressMode.IMM, 2, false);
		lookup[0x65] = new Instruction(OpCode.ADC, AddressMode.ZPP, 3, false);
		lookup[0x75] = new Instruction(OpCode.ADC, AddressMode.ZPX, 4, false);
		lookup[0x6D] = new Instruction(OpCode.ADC, AddressMode.ABS, 4, false);
		lookup[0x7D] = new Instruction(OpCode.ADC, AddressMode.ABX, 4, false);
		lookup[0x79] = new Instruction(OpCode.ADC, AddressMode.ABY, 4, false);
		lookup[0x61] = new Instruction(OpCode.ADC, AddressMode.IZX, 6, false);
		lookup[0x71] = new Instruction(OpCode.ADC, AddressMode.IZY, 5, false);
		lookup[0x72] = new Instruction(OpCode.ADC, AddressMode.ZPI, 5, true);

		lookup[0x29] = new Instruction(OpCode.AND, AddressMode.IMM, 2, false);
		lookup[0x25] = new Instruction(OpCode.AND, AddressMode.ZPP, 3, false);
		lookup[0x35] = new Instruction(OpCode.AND, AddressMode.ZPX, 4, false);
		lookup[0x2D] = new Instruction(OpCode.AND, AddressMode.ABS, 4, false);
		lookup[0x3D] = new Instruction(OpCode.AND, AddressMode.ABX, 4, false);
		lookup[0x39] = new Instruction(OpCode.AND, AddressMode.ABY, 4, false);
		lookup[0x21] = new Instruction(OpCode.AND, AddressMode.IZX, 6, false);
		lookup[0x31] = new Instruction(OpCode.AND, AddressMode.IZY, 5, false);
		lookup[0x32] = new Instruction(OpCode.AND, AddressMode.ZPI, 5, true);

		lookup[0x0A] = new Instruction(OpCode.ASL, AddressMode.ACC, 2, false);
		lookup[0x06] = new Instruction(OpCode.ASL, AddressMode.ZPP, 5, false);
		lookup[0x16] = new Instruction(OpCode.ASL, AddressMode.ZPX, 6, false);
		lookup[0x0E] = new Instruction(OpCode.ASL, AddressMode.ABS, 6, false);
		lookup[0x1E] = new Instruction(OpCode.ASL, AddressMode.ABX, 7, false);

		lookup[0x0F] = new Instruction(OpCode.BBR0, AddressMode.ZPP, 5, true);
		lookup[0x1F] = new Instruction(OpCode.BBR1, AddressMode.ZPP, 5, true);
		lookup[0x2F] = new Instruction(OpCode.BBR2, AddressMode.ZPP, 5, true);
		lookup[0x3F] = new Instruction(OpCode.BBR3, AddressMode.ZPP, 5, true);
		lookup[0x4F] = new Instruction(OpCode.BBR4, AddressMode.ZPP, 5, true);
		lookup[0x5F] = new Instruction(OpCode.BBR5, AddressMode.ZPP, 5, true);
		lookup[0x6F] = new Instruction(OpCode.BBR6, AddressMode.ZPP, 5, true);
		lookup[0x7F] = new Instruction(OpCode.BBR7, AddressMode.ZPP, 5, true);

		lookup[0x8F] = new Instruction(OpCode.BBS0, AddressMode.ZPP, 5, true);
		lookup[0x9F] = new Instruction(OpCode.BBS1, AddressMode.ZPP, 5, true);
		lookup[0xAF] = new Instruction(OpCode.BBS2, AddressMode.ZPP, 5, true);
		lookup[0xBF] = new Instruction(OpCode.BBS3, AddressMode.ZPP, 5, true);
		lookup[0xCF] = new Instruction(OpCode.BBS4, AddressMode.ZPP, 5, true);
		lookup[0xDF] = new Instruction(OpCode.BBS5, AddressMode.ZPP, 5, true);
		lookup[0xEF] = new Instruction(OpCode.BBS6, AddressMode.ZPP, 5, true);
		lookup[0xFF] = new Instruction(OpCode.BBS7, AddressMode.ZPP, 5, true);

		lookup[0x90] = new Instruction(OpCode.BCC, AddressMode.REL, 2, false);

		lookup[0xB0] = new Instruction(OpCode.BCS, AddressMode.REL, 2, false);

		lookup[0xF0] = new Instruction(OpCode.BEQ, AddressMode.REL, 2, false);

		lookup[0x89] = new Instruction(OpCode.BIT, AddressMode.IMM, 2, true);
		lookup[0x24] = new Instruction(OpCode.BIT, AddressMode.ZPP, 3, false);
		lookup[0x2C] = new Instruction(OpCode.BIT, AddressMode.ABS, 4, false);
		lookup[0x34] = new Instruction(OpCode.BIT, AddressMode.ZPX, 4, true);
		lookup[0x3C] = new Instruction(OpCode.BIT, AddressMode.ABX, 4, true);

		lookup[0x30] = new Instruction(OpCode.BMI, AddressMode.REL, 2, false);

		lookup[0xD0] = new Instruction(OpCode.BNE, AddressMode.REL, 2, false);

		lookup[0x10] = new Instruction(OpCode.BPL, AddressMode.REL, 2, false);

		lookup[0x80] = new Instruction(OpCode.BRA, AddressMode.REL, 2, true);

		lookup[0x00] = new Instruction(OpCode.BRK, AddressMode.IMP, 2, false);

		lookup[0x50] = new Instruction(OpCode.BVC, AddressMode.REL, 2, false);

		lookup[0x70] = new Instruction(OpCode.BVS, AddressMode.REL, 2, false);

		lookup[0x18] = new Instruction(OpCode.CLC, AddressMode.IMP, 2, false);

		lookup[0xD8] = new Instruction(OpCode.CLD, AddressMode.IMP, 2, false);

		lookup[0x58] = new Instruction(OpCode.CLI, AddressMode.IMP, 2, false);

		lookup[0xB8] = new Instruction(OpCode.CLV, AddressMode.IMP, 2, false);

		lookup[0xC9] = new Instruction(OpCode.CMP, AddressMode.IMM, 2, false);
		lookup[0xC5] = new Instruction(OpCode.CMP, AddressMode.ZPP, 3, false);
		lookup[0xD5] = new Instruction(OpCode.CMP, AddressMode.ZPX, 4, false);
		lookup[0xCD] = new Instruction(OpCode.CMP, AddressMode.ABS, 4, false);
		lookup[0xDD] = new Instruction(OpCode.CMP, AddressMode.ABX, 4, false);
		lookup[0xD9] = new Instruction(OpCode.CMP, AddressMode.ABY, 4, false);
		lookup[0xC1] = new Instruction(OpCode.CMP, AddressMode.IZX, 6, false);
		lookup[0xD1] = new Instruction(OpCode.CMP, AddressMode.IZY, 5, false);
		lookup[0xD2] = new Instruction(OpCode.CMP, AddressMode.ZPI, 5, true);

		lookup[0xE0] = new Instruction(OpCode.CPX, AddressMode.IMM, 2, false);
		lookup[0xE4] = new Instruction(OpCode.CPX, AddressMode.ZPP, 3, false);
		lookup[0xEC] = new Instruction(OpCode.CPX, AddressMode.ABS, 4, false);

		lookup[0xC0] = new Instruction(OpCode.CPY, AddressMode.IMM, 2, false);
		lookup[0xC4] = new Instruction(OpCode.CPY, AddressMode.ZPP, 3, false);
		lookup[0xCC] = new Instruction(OpCode.CPY, AddressMode.ABS, 4, false);

		lookup[0x3A] = new Instruction(OpCode.DEC, AddressMode.ACC,  2, true);
		lookup[0xC6] = new Instruction(OpCode.DEC, AddressMode.ZPP, 5, false);
		lookup[0xD6] = new Instruction(OpCode.DEC, AddressMode.ZPX, 6, false);
		lookup[0xCE] = new Instruction(OpCode.DEC, AddressMode.ABS, 6, false);
		lookup[0xDE] = new Instruction(OpCode.DEC, AddressMode.ABX, 7, false);

		lookup[0xCA] = new Instruction(OpCode.DEX, AddressMode.IMP, 2, false);

		lookup[0x88] = new Instruction(OpCode.DEY, AddressMode.IMP, 2, false);

		lookup[0x49] = new Instruction(OpCode.EOR, AddressMode.IMM, 2, false);
		lookup[0x45] = new Instruction(OpCode.EOR, AddressMode.ZPP, 3, false);
		lookup[0x55] = new Instruction(OpCode.EOR, AddressMode.ZPX, 4, false);
		lookup[0x4D] = new Instruction(OpCode.EOR, AddressMode.ABS, 4, false);
		lookup[0x5D] = new Instruction(OpCode.EOR, AddressMode.ABX, 4, false);
		lookup[0x59] = new Instruction(OpCode.EOR, AddressMode.ABY, 4, false);
		lookup[0x41] = new Instruction(OpCode.EOR, AddressMode.IZX, 6, false);
		lookup[0x51] = new Instruction(OpCode.EOR, AddressMode.IZY, 5, false);
		lookup[0x52] = new Instruction(OpCode.EOR, AddressMode.ZPI, 5, true);

		lookup[0x1A] = new Instruction(OpCode.INC, AddressMode.ACC,  2, true);
		lookup[0xE6] = new Instruction(OpCode.INC, AddressMode.ZPP, 5, false);
		lookup[0xF6] = new Instruction(OpCode.INC, AddressMode.ZPX, 6, false);
		lookup[0xEE] = new Instruction(OpCode.INC, AddressMode.ABS, 6, false);
		lookup[0xFE] = new Instruction(OpCode.INC, AddressMode.ABX, 7, false);

		lookup[0xE8] = new Instruction(OpCode.INX, AddressMode.IMP, 2, false);

		lookup[0xC8] = new Instruction(OpCode.INY, AddressMode.IMP, 2, false);

		lookup[0x4C] = new Instruction(OpCode.JMP, AddressMode.ABS, 3, false);
		lookup[0x6C] = new Instruction(OpCode.JMP, AddressMode.IND, 5, false);

		lookup[0x20] = new Instruction(OpCode.JSR, AddressMode.ABS, 6, false);

		lookup[0xA9] = new Instruction(OpCode.LDA, AddressMode.IMM, 2, false);
		lookup[0xA5] = new Instruction(OpCode.LDA, AddressMode.ZPP, 3, false);
		lookup[0xB5] = new Instruction(OpCode.LDA, AddressMode.ZPX, 4, false);
		lookup[0xAD] = new Instruction(OpCode.LDA, AddressMode.ABS, 4, false);
		lookup[0xBD] = new Instruction(OpCode.LDA, AddressMode.ABX, 4, false);
		lookup[0xB9] = new Instruction(OpCode.LDA, AddressMode.ABY, 4, false);
		lookup[0xA1] = new Instruction(OpCode.LDA, AddressMode.IZX, 6, false);
		lookup[0xB1] = new Instruction(OpCode.LDA, AddressMode.IZY, 5, false);
		lookup[0xB2] = new Instruction(OpCode.LDA, AddressMode.ZPI, 5, true);

		lookup[0xA2] = new Instruction(OpCode.LDX, AddressMode.IMM, 2, false);
		lookup[0xA6] = new Instruction(OpCode.LDX, AddressMode.ZPP, 3, false);
		lookup[0xB6] = new Instruction(OpCode.LDX, AddressMode.ZPY, 4, false);
		lookup[0xAE] = new Instruction(OpCode.LDX, AddressMode.ABS, 4, false);
		lookup[0xBE] = new Instruction(OpCode.LDX, AddressMode.ABY, 4, false);

		lookup[0xA0] = new Instruction(OpCode.LDY, AddressMode.IMM, 2, false);
		lookup[0xA4] = new Instruction(OpCode.LDY, AddressMode.ZPP, 3, false);
		lookup[0xB4] = new Instruction(OpCode.LDY, AddressMode.ZPX, 4, false);
		lookup[0xAC] = new Instruction(OpCode.LDY, AddressMode.ABS, 4, false);
		lookup[0xBC] = new Instruction(OpCode.LDY, AddressMode.ABX, 4, false);

		lookup[0x4A] = new Instruction(OpCode.LSR, AddressMode.ACC, 2, false);
		lookup[0x46] = new Instruction(OpCode.LSR, AddressMode.ZPP, 5, false);
		lookup[0x56] = new Instruction(OpCode.LSR, AddressMode.ZPX, 6, false);
		lookup[0x4E] = new Instruction(OpCode.LSR, AddressMode.ABS, 6, false);
		lookup[0x5E] = new Instruction(OpCode.LSR, AddressMode.ABX, 7, false);

		lookup[0xEA] = new Instruction(OpCode.NOP, AddressMode.IMP, 2, false);

		lookup[0x09] = new Instruction(OpCode.ORA, AddressMode.IMM, 2, false);
		lookup[0x05] = new Instruction(OpCode.ORA, AddressMode.ZPP, 3, false);
		lookup[0x15] = new Instruction(OpCode.ORA, AddressMode.ZPX, 4, false);
		lookup[0x0D] = new Instruction(OpCode.ORA, AddressMode.ABS, 4, false);
		lookup[0x1D] = new Instruction(OpCode.ORA, AddressMode.ABX, 4, false);
		lookup[0x19] = new Instruction(OpCode.ORA, AddressMode.ABY, 4, false);
		lookup[0x01] = new Instruction(OpCode.ORA, AddressMode.IZX, 6, false);
		lookup[0x11] = new Instruction(OpCode.ORA, AddressMode.IZY, 5, false);
		lookup[0x12] = new Instruction(OpCode.ORA, AddressMode.ZPI, 5, true);

		lookup[0x48] = new Instruction(OpCode.PHA, AddressMode.IMP, 3, false);

		lookup[0x08] = new Instruction(OpCode.PHP, AddressMode.IMP, 3, false);

		lookup[0xDA] = new Instruction(OpCode.PHX, AddressMode.IMP, 3, true);

		lookup[0x5A] = new Instruction(OpCode.PHY, AddressMode.IMP, 3, true);

		lookup[0x68] = new Instruction(OpCode.PLA, AddressMode.IMP, 4, false);

		lookup[0x28] = new Instruction(OpCode.PLP, AddressMode.IMP, 4, false);

		lookup[0xFA] = new Instruction(OpCode.PLX, AddressMode.IMP, 4, true);

		lookup[0x7A] = new Instruction(OpCode.PLY, AddressMode.IMP, 4, true);

		lookup[0x07] = new Instruction(OpCode.RMB0, AddressMode.ZPP, 5, true);
		lookup[0x17] = new Instruction(OpCode.RMB1, AddressMode.ZPP, 5, true);
		lookup[0x27] = new Instruction(OpCode.RMB2, AddressMode.ZPP, 5, true);
		lookup[0x37] = new Instruction(OpCode.RMB3, AddressMode.ZPP, 5, true);
		lookup[0x47] = new Instruction(OpCode.RMB4, AddressMode.ZPP, 5, true);
		lookup[0x57] = new Instruction(OpCode.RMB5, AddressMode.ZPP, 5, true);
		lookup[0x67] = new Instruction(OpCode.RMB6, AddressMode.ZPP, 5, true);
		lookup[0x77] = new Instruction(OpCode.RMB7, AddressMode.ZPP, 5, true);

		lookup[0x2A] = new Instruction(OpCode.ROL, AddressMode.ACC, 2, false);
		lookup[0x26] = new Instruction(OpCode.ROL, AddressMode.ZPP, 5, false);
		lookup[0x36] = new Instruction(OpCode.ROL, AddressMode.ZPX, 6, false);
		lookup[0x2E] = new Instruction(OpCode.ROL, AddressMode.ABS, 6, false);
		lookup[0x3E] = new Instruction(OpCode.ROL, AddressMode.ABX, 7, false);

		lookup[0x6A] = new Instruction(OpCode.ROR, AddressMode.ACC, 2, false);
		lookup[0x66] = new Instruction(OpCode.ROR, AddressMode.ZPP, 5, false);
		lookup[0x76] = new Instruction(OpCode.ROR, AddressMode.ZPX, 6, false);
		lookup[0x6E] = new Instruction(OpCode.ROR, AddressMode.ABS, 6, false);
		lookup[0x7E] = new Instruction(OpCode.ROR, AddressMode.ABX, 7, false);

		lookup[0x40] = new Instruction(OpCode.RTI, AddressMode.IMP, 6, false);

		lookup[0x60] = new Instruction(OpCode.RTS, AddressMode.IMP, 6, false);

		lookup[0xE9] = new Instruction(OpCode.SBC, AddressMode.IMM, 2, false);
		lookup[0xE5] = new Instruction(OpCode.SBC, AddressMode.ZPP, 3, false);
		lookup[0xF5] = new Instruction(OpCode.SBC, AddressMode.ZPX, 4, false);
		lookup[0xED] = new Instruction(OpCode.SBC, AddressMode.ABS, 4, false);
		lookup[0xFD] = new Instruction(OpCode.SBC, AddressMode.ABX, 4, false);
		lookup[0xF9] = new Instruction(OpCode.SBC, AddressMode.ABY, 4, false);
		lookup[0xE1] = new Instruction(OpCode.SBC, AddressMode.IZX, 6, false);
		lookup[0xF1] = new Instruction(OpCode.SBC, AddressMode.IZY, 5, false);
		lookup[0xF2] = new Instruction(OpCode.SBC, AddressMode.ZPI, 5, true);

		lookup[0x38] = new Instruction(OpCode.SEC, AddressMode.IMP, 2, false);

		lookup[0xF8] = new Instruction(OpCode.SED, AddressMode.IMP, 2, false);

		lookup[0x78] = new Instruction(OpCode.SEI, AddressMode.IMP, 2, false);

		lookup[0x87] = new Instruction(OpCode.SMB0, AddressMode.ZPP, 5, true);
		lookup[0x97] = new Instruction(OpCode.SMB1, AddressMode.ZPP, 5, true);
		lookup[0xA7] = new Instruction(OpCode.SMB2, AddressMode.ZPP, 5, true);
		lookup[0xB7] = new Instruction(OpCode.SMB3, AddressMode.ZPP, 5, true);
		lookup[0xC7] = new Instruction(OpCode.SMB4, AddressMode.ZPP, 5, true);
		lookup[0xD7] = new Instruction(OpCode.SMB5, AddressMode.ZPP, 5, true);
		lookup[0xE7] = new Instruction(OpCode.SMB6, AddressMode.ZPP, 5, true);
		lookup[0xF7] = new Instruction(OpCode.SMB7, AddressMode.ZPP, 5, true);

		lookup[0x85] = new Instruction(OpCode.STA, AddressMode.ZPP, 3, false);
		lookup[0x95] = new Instruction(OpCode.STA, AddressMode.ZPX, 4, false);
		lookup[0x8D] = new Instruction(OpCode.STA, AddressMode.ABS, 4, false);
		lookup[0x9D] = new Instruction(OpCode.STA, AddressMode.ABX, 5, false);
		lookup[0x99] = new Instruction(OpCode.STA, AddressMode.ABY, 5, false);
		lookup[0x81] = new Instruction(OpCode.STA, AddressMode.IZX, 6, false);
		lookup[0x91] = new Instruction(OpCode.STA, AddressMode.IZY, 6, false);
		lookup[0x92] = new Instruction(OpCode.STA, AddressMode.ZPI, 5, true);

		lookup[0xDB] = new Instruction(OpCode.STP, AddressMode.IMP, 3, true);

		lookup[0x86] = new Instruction(OpCode.STX, AddressMode.ZPP, 3, false);
		lookup[0x96] = new Instruction(OpCode.STX, AddressMode.ZPY, 4, false);
		lookup[0x8E] = new Instruction(OpCode.STX, AddressMode.ABS, 4, false);

		lookup[0x84] = new Instruction(OpCode.STY, AddressMode.ZPP, 3, false);
		lookup[0x94] = new Instruction(OpCode.STY, AddressMode.ZPX, 4, false);
		lookup[0x8C] = new Instruction(OpCode.STY, AddressMode.ABS, 4, false);

		lookup[0x64] = new Instruction(OpCode.STZ, AddressMode.ZPP, 3, true);
		lookup[0x74] = new Instruction(OpCode.STZ, AddressMode.ZPX, 4, true);
		lookup[0x9C] = new Instruction(OpCode.STZ, AddressMode.ABS, 4, true);
		lookup[0x9E] = new Instruction(OpCode.STZ, AddressMode.ABX, 4, true);

		lookup[0xAA] = new Instruction(OpCode.TAX, AddressMode.IMP, 2, false);

		lookup[0xA8] = new Instruction(OpCode.TAY, AddressMode.IMP, 2, false);

		lookup[0x14] = new Instruction(OpCode.TRB, AddressMode.ZPP, 5, false);
		lookup[0x1C] = new Instruction(OpCode.TRB, AddressMode.ABS, 6, false);

		lookup[0x04] = new Instruction(OpCode.TSB, AddressMode.ZPP, 5, false);
		lookup[0x0C] = new Instruction(OpCode.TSB, AddressMode.ABS, 6, false);

		lookup[0xBA] = new Instruction(OpCode.TSX, AddressMode.IMP, 2, false);

		lookup[0x8A] = new Instruction(OpCode.TXA, AddressMode.IMP, 2, false);

		lookup[0x9A] = new Instruction(OpCode.TXS, AddressMode.IMP, 2, false);

		lookup[0x98] = new Instruction(OpCode.TYA, AddressMode.IMP, 2, false);

		lookup[0xCB] = new Instruction(OpCode.WAI, AddressMode.IMP, 3, true);
	}

	void setFlag(char flag, boolean condition) {
		flag = Character.toUpperCase(flag);
		switch (flag) {
		case 'C':
			flags = setBit(flags,0,condition);
				break;
		case 'Z':
			flags = setBit(flags,1,condition);
				break;
		case 'I':
			flags = setBit(flags,2,condition);
				break;
		case 'D':
			flags = setBit(flags,3,condition);
				break;
		case 'B':
			flags = setBit(flags,4,condition);
				break;
		case 'U':
			flags = setBit(flags,5,condition);
				break;
		case 'V':
			flags = setBit(flags,6,condition);
				break;
		case 'N':
			flags = setBit(flags,7,condition);
				break;
		}
	}

	boolean getFlag(char flag) {
		flag = Character.toUpperCase(flag);
		switch (flag) {
		case 'N':
			return ((flags&0b10000000) == 0b10000000);
		case 'V':
			return ((flags&0b01000000) == 0b01000000);
		case 'U':
			return ((flags&0b00100000) == 0b00100000);
		case 'B':
			return ((flags&0b00010000) == 0b00010000);
		case 'D':
			return ((flags&0b00001000) == 0b00001000);
		case 'I':
			return ((flags&0b00000100) == 0b00000100);
		case 'Z':
			return ((flags&0b00000010) == 0b00000010);
		case 'C':
			return ((flags&0b00000001) == 0b00000001);
		}
		if (EaterEmulator.verbose) System.out.println("Something has gone wrong in getFlag!");
		return false;
	}

	public static byte setBit(byte b, int bit, boolean value) {
		if (value) {
			b |= (byte)(0x00+Math.pow(2, bit));
		} else {
			b &= (byte)(0xFF-Math.pow(2, bit));
		}
		return b;
	}

	void clock() {
		if (interruptRequested || NMinterruptRequested) waiting = false;
		if (waiting || stopped) return;

		if (cycles == 0) {
			if (interruptRequested)
				irq();
			else if (NMinterruptRequested)
				nmi();
			else {
				additionalCycles = 0;
				opcode = Bus.read(programCounter);
				programCounter++;

				cycles = lookup[Byte.toUnsignedInt(opcode)].cycles;

				//Execute the functions corresponding to the addressing mode and opcode

					//this.getClass().getMethod(lookup[Byte.toUnsignedInt(opcode)].addressMode).invoke(this);
					//this.getClass().getMethod(lookup[Byte.toUnsignedInt(opcode)].opcode).invoke(this);

				Instruction currentInstruction = lookup[Byte.toUnsignedInt(opcode)];

				executeAddressModeFunction(currentInstruction.addressMode);
				executeOpcodeFunction(currentInstruction.opcode);

			}

			if (debug) {
				System.out.print(Integer.toHexString(Short.toUnsignedInt(programCounter))+"   "+lookup[Byte.toUnsignedInt(opcode)].opcode+" "+ROMLoader.byteToHexString(opcode)+" ");
				if (!(lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.IMP || lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.ACC || lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.REL)) {
					if (lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.IMM) {
						System.out.print("#$"+Integer.toHexString(Byte.toUnsignedInt(fetched)));
					} else if (lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.REL) {
						System.out.print("$"+Integer.toHexString(Byte.toUnsignedInt((byte)addressAbsolute)));
					} else {
						System.out.print("$"+Integer.toHexString(Short.toUnsignedInt(addressAbsolute)));
					}
				} else if (!(lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.IMP || lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.ACC)) {
					System.out.print("$"+Integer.toHexString(Short.toUnsignedInt(addressRelative)));
				}
				if (lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.ABX || lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.IZX || lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.ZPX) {
					System.out.print(",X");
				} else if (lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.ABY || lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.IZY || lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.ZPY) {
					System.out.print(",Y");
				}
				System.out.print("  A:"+Integer.toHexString(Byte.toUnsignedInt(a))+" X:"+Integer.toHexString(Byte.toUnsignedInt(x))+" Y:"+Integer.toHexString(Byte.toUnsignedInt(y))+" Flags:"+ROMLoader.padStringWithZeroes(Integer.toBinaryString(Byte.toUnsignedInt(flags)), 8));
				if (EaterEmulator.verbose) System.out.println();
			}

			if (DisplayPanel.breakpoints.contains(programCounter)) EaterEmulator.clockState = false;
		}

		EaterEmulator.clocks++;

		cycles--;

		if (cycles < 0) {
			cycles = 0;
		}
	}

	void executeAddressModeFunction(AddressMode addressMode) {
		switch (addressMode) {
			case ACC, IMP -> IMP();
			case ABS -> ABS();
			case ABX -> ABX();
			case ABY -> ABY();
			case IMM -> IMM();
			case IND -> IND();
			case IZX -> IZX();
			case IZY -> IZY();
			case REL -> REL();
			case ZPP -> ZPP();
			case ZPX -> ZPX();
			case ZPY -> ZPY();
			case ZPI -> ZPI();
			case IAX -> IAX();
			default -> {
				if (EaterEmulator.verbose)
					System.out.println("Something has gone seriously wrong! AddressMode: " + addressMode);
			}
		}
	}

	void executeOpcodeFunction(OpCode opcode) {
		switch (opcode) {
			case ADC -> ADC();
			case AND -> AND();
			case ASL -> ASL();
			case BBR0 -> BBR0();
			case BBR1 -> BBR1();
			case BBR2 -> BBR2();
			case BBR3 -> BBR3();
			case BBR4 -> BBR4();
			case BBR5 -> BBR5();
			case BBR6 -> BBR6();
			case BBR7 -> BBR7();
			case BBS0 -> BBS0();
			case BBS1 -> BBS1();
			case BBS2 -> BBS2();
			case BBS3 -> BBS3();
			case BBS4 -> BBS4();
			case BBS5 -> BBS5();
			case BBS6 -> BBS6();
			case BBS7 -> BBS7();
			case BCC -> BCC();
			case BCS -> BCS();
			case BEQ -> BEQ();
			case BIT -> BIT();
			case BMI -> BMI();
			case BNE -> BNE();
			case BPL -> BPL();
			case BRA -> BRA();
			case BRK -> BRK();
			case BVC -> BVC();
			case BVS -> BVS();
			case CLC -> CLC();
			case CLD -> CLD();
			case CLI -> CLI();
			case CLV -> CLV();
			case CMP -> CMP();
			case CPX -> CPX();
			case CPY -> CPY();
			case DEC -> DEC();
			case DEX -> DEX();
			case DEY -> DEY();
			case EOR -> EOR();
			case INC -> INC();
			case INX -> INX();
			case INY -> INY();
			case JMP -> JMP();
			case JSR -> JSR();
			case LDA -> LDA();
			case LDX -> LDX();
			case LDY -> LDY();
			case LSR -> LSR();
			case NOP -> NOP();
			case ORA -> ORA();
			case PHA -> PHA();
			case PHP -> PHP();
			case PHX -> PHX();
			case PHY -> PHY();
			case PLA -> PLA();
			case PLP -> PLP();
			case PLX -> PLX();
			case PLY -> PLY();
			case RMB0 -> RMB0();
			case RMB1 -> RMB1();
			case RMB2 -> RMB2();
			case RMB3 -> RMB3();
			case RMB4 -> RMB4();
			case RMB5 -> RMB5();
			case RMB6 -> RMB6();
			case RMB7 -> RMB7();
			case ROL -> ROL();
			case ROR -> ROR();
			case RTI -> RTI();
			case RTS -> RTS();
			case SBC -> SBC();
			case SEC -> SEC();
			case SED -> SED();
			case SEI -> SEI();
			case SMB0 -> SMB0();
			case SMB1 -> SMB1();
			case SMB2 -> SMB2();
			case SMB3 -> SMB3();
			case SMB4 -> SMB4();
			case SMB5 -> SMB5();
			case SMB6 -> SMB6();
			case SMB7 -> SMB7();
			case STA -> STA();
			case STP -> STP();
			case STX -> STX();
			case STY -> STY();
			case STZ -> STZ();
			case TAX -> TAX();
			case TAY -> TAY();
			case TRB -> TRB();
			case TSB -> TSB();
			case TSX -> TSX();
			case TXA -> TXA();
			case TXS -> TXS();
			case TYA -> TYA();
			case WAI -> WAI();
			case XXX -> XXX();
			default -> {
				if (EaterEmulator.verbose)
					System.out.println("Something has gone seriously wrong! OpCode: " + opcode);
			}
		}
	}

	//Input Signal Handlers
	void reset() {
		stopped = false;
		waiting = false;

		EaterEmulator.clockState = false;
		if (EaterEmulator.serial != null) EaterEmulator.serial.reset();

		a = 0;
		x = 0;
		y = 0;
		stackPointer = (byte)0xFD;
		flags = (byte)(getFlag('U') ? 0b00000100 : 0);

		addressAbsolute = (short)(0xFFFC);

		byte lo = Bus.read(addressAbsolute);
		byte hi = Bus.read((short)(addressAbsolute+1));
		programCounter = (short)(Byte.toUnsignedInt(lo)+256*Byte.toUnsignedInt(hi));

		EaterEmulator.clocks = 0;
		ClocksPerSecond = 0;

		addressRelative = 0;
		addressAbsolute = 0;
		fetched = 0;

		cycles = 8;

		startTime = System.currentTimeMillis();

		opcode = Bus.read(programCounter);
	}

	void irq() {
		if (!getFlag('I')) {
			if (debug)
				if (EaterEmulator.verbose) System.out.println("Interrupted!");

			Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), (byte)(programCounter>>8));
			stackPointer--;
			Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), (byte)(programCounter));
			stackPointer--;

			setFlag('B',false);
			setFlag('U',false);
			Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), flags);
			stackPointer--;
			setFlag('I',true);

			addressAbsolute = (short)(0xFFFE);
			byte lo = Bus.read(addressAbsolute);
			byte hi = Bus.read((short)(addressAbsolute+1));
			programCounter = (short)(Byte.toUnsignedInt(lo)+256*Byte.toUnsignedInt(hi));

			cycles = 7;
		}
		interruptRequested = false;
	}

	void nmi() {
		Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), (byte)(programCounter>>8));
		stackPointer--;
		Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), (byte)(programCounter));
		stackPointer--;

		setFlag('B',false);
		setFlag('U',false);
		Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), flags);
		stackPointer--;
		setFlag('I',true);

		addressAbsolute = (short)(0xFFFA);
		byte lo = Bus.read(addressAbsolute);
		byte hi = Bus.read((short)(addressAbsolute+1));
		programCounter = (short)(Byte.toUnsignedInt(lo)+256*Byte.toUnsignedInt(hi));

		cycles = 7;
		NMinterruptRequested = false;
	}

	//Data Getter
	byte fetched = 0x00;
	byte fetch() {
		if (!(lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.IMP || lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.ACC))
			fetched = Bus.read(addressAbsolute);
		return fetched;
	}

	//Addressing Modes
	public void IMP() {
		fetched = a;
	}

	public void IMM() {
		addressAbsolute = programCounter++;
	}

	public void ZPP() {
		addressAbsolute = Bus.read(programCounter++);
		addressAbsolute &= 0x00FF;
	}

	public void ZPX() {
		addressAbsolute = (short)(Byte.toUnsignedInt(Bus.read(programCounter++))+Byte.toUnsignedInt(x));
		addressAbsolute &= 0x00FF;
	}

	public void ZPY() {
		addressAbsolute = (short)(Byte.toUnsignedInt(Bus.read(programCounter++))+Byte.toUnsignedInt(y));
		addressAbsolute &= 0x00FF;
	}

	public void REL() {
		addressRelative = Bus.read(programCounter++);
		if ((addressRelative & 0x80)==0x80)
			addressRelative |= (short) 0xFF00;
	}

	public void ABS() {
		byte lo = Bus.read(programCounter++);
		byte hi = Bus.read(programCounter++);

		addressAbsolute = (short)(Byte.toUnsignedInt(lo)+256*Byte.toUnsignedInt(hi));
	}

	public void ABX() {
		byte lo = Bus.read(programCounter++);
		byte hi = Bus.read(programCounter++);

		addressAbsolute = (short)(Byte.toUnsignedInt(lo)+256*Byte.toUnsignedInt(hi)+Byte.toUnsignedInt(x));

		if ((addressAbsolute & 0xFF00) != (hi<<8))
			additionalCycles++;
	}

	public void ABY() {
		byte lo = Bus.read(programCounter++);
		byte hi = Bus.read(programCounter++);

		addressAbsolute = (short)(Byte.toUnsignedInt(lo)+256*Byte.toUnsignedInt(hi)+Byte.toUnsignedInt(y));

		if ((addressAbsolute & 0xFF00) != (hi<<8))
			additionalCycles++;
	}

	public void IND() {
 		short lowPointer = (short)(Bus.read(programCounter++)&0xff);
		short highPointer = (short)(Bus.read(programCounter++)&0xff);

		short pointer = (short)((highPointer << 8) | lowPointer);

		addressAbsolute = (short)(Byte.toUnsignedInt(Bus.read((short)(pointer+1)))*256+Byte.toUnsignedInt(Bus.read(pointer)));
	}

	public void IZX() {
		byte t = Bus.read(programCounter++);

		byte lo = Bus.read((short)((t+x)&0x00FF));
		byte hi = Bus.read((short)((t+x+1)&0x00FF));

		addressAbsolute = (short)(Byte.toUnsignedInt(lo)+256*Byte.toUnsignedInt(hi));
	}

	public void IZY() {
		byte t = Bus.read(programCounter++);

		byte lo = Bus.read((short)(t&0x00FF));
		byte hi = Bus.read((short)((t+1)&0x00FF));

		addressAbsolute = (short)(Byte.toUnsignedInt(lo)+256*Byte.toUnsignedInt(hi)+Byte.toUnsignedInt(y));

		if ((addressAbsolute & 0xFF00) != (hi<<8))
			additionalCycles++;
	}

	public void ZPI() {
		short lowPointer = (short) (Bus.read(programCounter++) & 0x00ff);

		addressAbsolute = (short)(Byte.toUnsignedInt(Bus.read((short)(lowPointer+1)))*256+Byte.toUnsignedInt(Bus.read(lowPointer)));
	}

	public void IAX() {
		byte lowPointer = Bus.read(programCounter++);
		byte highPointer = Bus.read(programCounter++);

		short pointer = (short)((highPointer << 8) | lowPointer);

		byte lo = Bus.read((short) (pointer+x));
		byte hi = Bus.read((short) (pointer+x+1));

		addressAbsolute = (short)(Byte.toUnsignedInt(lo)+256*Byte.toUnsignedInt(hi));
	}

	//INSTRUCTIONS
	public void ADC() {
		fetch();
		short temp = (short)((short)Byte.toUnsignedInt(a) + (short)Byte.toUnsignedInt(fetched) + (short)(getFlag('C') ? 1 : 0));
		setFlag('C', temp > 255);
		setFlag('Z', (temp & 0x00FF) == 0);
		setFlag('N', (temp & 0x80) == 0x80);
		setFlag('V', (~((short)a^(short)fetched) & ((short)a^(short)temp) & 0x0080)==0x0080);
		a = (byte)temp;
		additionalCycles++;
	}

	public void AND() {
		fetch();
		a &= fetched;
		setFlag('Z', a==0x00);
		setFlag('N', (a & 0x80)==0x80);

		additionalCycles++;
	}

	public void ASL() {
		fetch();
		short temp = (short)(fetched << 1);
		setFlag('Z', (temp & 0x00FF)==0x00);
		setFlag('C', Short.toUnsignedInt((short)(temp & 0xFF00)) > 0);
		setFlag('N', (temp & 0x80)==0x80);

		if (lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.ACC) {
			a = (byte)(temp & 0x00FF);
		} else {
			Bus.write(addressAbsolute, (byte)(temp & 0x00FF));
		}
	}

	public void BBR0() { BBRn(0); }
	public void BBR1() { BBRn(1); }
	public void BBR2() { BBRn(2); }
	public void BBR3() { BBRn(3); }
	public void BBR4() { BBRn(4); }
	public void BBR5() { BBRn(5); }
	public void BBR6() { BBRn(6); }
	public void BBR7() { BBRn(7); }

	private void BBRn(int n) {
		if ((a & (0b1<<n)) == 0) {
			cycles++;
			addressAbsolute = (short)(programCounter+addressRelative);

			if ((addressAbsolute&0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BBS0() { BBSn(0); }
	public void BBS1() { BBSn(1); }
	public void BBS2() { BBSn(2); }
	public void BBS3() { BBSn(3); }
	public void BBS4() { BBSn(4); }
	public void BBS5() { BBSn(5); }
	public void BBS6() { BBSn(6); }
	public void BBS7() { BBSn(7); }

	private void BBSn(int n) {
		if ((a & (0b1<<n)) != 0) {
			cycles++;
			addressAbsolute = (short)(programCounter+addressRelative);

			if ((addressAbsolute&0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BCC() {
		if (!getFlag('C')) {
			cycles++;
			addressAbsolute = (short)(programCounter+addressRelative);

			if ((addressAbsolute&0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BCS() {
		if (getFlag('C')) {
			cycles++;
			addressAbsolute = (short)(programCounter+addressRelative);

			if ((addressAbsolute&0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BEQ() {
		if (getFlag('Z')) {
			cycles++;
			addressAbsolute = (short)(programCounter+addressRelative);

			if ((addressAbsolute&0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BIT() {
		fetch();
		short temp = (short)(a&fetched);
		setFlag('Z',(temp&0x00FF)==0x00);
		setFlag('N',(fetched&0x80)==0x80);
		setFlag('V',(fetched&0x40)==0x40);
	}

	public void BMI() {
		if (getFlag('N')) {
			cycles++;
			addressAbsolute = (short)(programCounter+addressRelative);

			if ((addressAbsolute&0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BNE() {
		if (!getFlag('Z')) {
			cycles++;
			addressAbsolute = (short)(programCounter+addressRelative);

			if ((addressAbsolute&0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BPL() {
		if (!getFlag('N')) {
			cycles++;
			addressAbsolute = (short)(programCounter+addressRelative);

			if ((addressAbsolute&0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BRA() {
		cycles++;
		addressAbsolute = (short)(programCounter+addressRelative);

		if ((addressAbsolute&0xFF00) != (programCounter & 0xFF00))
			cycles++;

		programCounter = addressAbsolute;
	}

	public void BRK() {
		programCounter++;

		Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), (byte)(programCounter>>8));
		stackPointer--;
		Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), (byte)(programCounter));
		stackPointer--;

		setFlag('B',true);
		setFlag('U',true);
		Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), flags);
		stackPointer--;
		setFlag('I',true);

		addressAbsolute = (short)0xFFFE;
		byte lo = Bus.read(addressAbsolute);
		byte hi = Bus.read((short)(addressAbsolute+1));
		programCounter = (short)(Byte.toUnsignedInt(lo)+256*Byte.toUnsignedInt(hi));
	}

	public void BVC() {
		if (!getFlag('V')) {
			cycles++;
			addressAbsolute = (short)(programCounter+addressRelative);

			if ((addressAbsolute&0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void BVS() {
		if (getFlag('V')) {
			cycles++;
			addressAbsolute = (short)(programCounter+addressRelative);

			if ((addressAbsolute&0xFF00) != (programCounter & 0xFF00))
				cycles++;

			programCounter = addressAbsolute;
		}
	}

	public void CLC() {
		setFlag('C',false);
	}

	public void CLD() {
		setFlag('D',false);
	}

	public void CLI() {
		setFlag('I',false);
	}

	public void CLV() {
		setFlag('V',false);
	}

	public void CMP() {
		fetch();
		short temp = (short)(Byte.toUnsignedInt(a) - Byte.toUnsignedInt(fetched));
		setFlag('C',Byte.toUnsignedInt(a) >= Byte.toUnsignedInt(fetched));
		setFlag('Z',(temp&0x00FF)==0x0000);
		setFlag('N',(temp&0x0080)==0x0080);

		additionalCycles++;
	}

	public void CPX() {
		fetch();
		short temp = (short)(Byte.toUnsignedInt(x) - Byte.toUnsignedInt(fetched));
		setFlag('C',Byte.toUnsignedInt(x) >= Byte.toUnsignedInt(fetched));
		setFlag('Z',(temp&0x00FF)==0x0000);
		setFlag('N',(temp&0x0080)==0x0080);
	}

	public void CPY() {
		fetch();
		short temp = (short)(Byte.toUnsignedInt(y) - Byte.toUnsignedInt(fetched));
		setFlag('C',Byte.toUnsignedInt(y) >= Byte.toUnsignedInt(fetched));
		setFlag('Z',(temp&0x00FF)==0x0000);
		setFlag('N',(temp&0x0080)==0x0080);
	}

	public void DEC() {
		fetch();
		int temp = (Byte.toUnsignedInt(fetched)-1);
		if (lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.ACC) {
			a = (byte) temp;
		} else {
			Bus.write(addressAbsolute, (byte)(temp&0x00FF));
		}
		setFlag('Z',(temp&0x00FF)==0x0000);
		setFlag('N',(temp&0x0080)==0x0080);
	}

	public void DEX() {
		x--;
		setFlag('Z',x==0x00);
		setFlag('N',(x&0x80)==0x80);
	}

	public void DEY() {
		y--;
		setFlag('Z',y==0x00);
		setFlag('N',(y&0x80)==0x80);
	}

	public void EOR() {
		fetch();
		a ^= fetched;
		setFlag('Z', a==0x00);
		setFlag('N', (a & 0x80)==0x80);

		additionalCycles++;
	}

	public void INC() {
		fetch();
		short temp = (short)(fetched+1);
		Bus.write(addressAbsolute, (byte)(temp&0x00FF));
		setFlag('Z',(temp&0x00FF)==0x0000);
		setFlag('N',(temp&0x0080)==0x0080);
	}

	public void INX() {
		x++;
		setFlag('Z',x==0x00);
		setFlag('N',(x&0x80)==0x80);
	}

	public void INY() {
		y++;
		setFlag('Z',y==0x00);
		setFlag('N',(y&0x80)==0x80);
	}

	public void JMP() {
		programCounter = addressAbsolute;
	}

	public void JSR() {
		programCounter--;

		Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), (byte)((programCounter>>8)&0x00FF));
		stackPointer--;
		Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), (byte)(programCounter&0x00FF));
		stackPointer--;

		programCounter = addressAbsolute;
	}

	public void LDA() {
		fetch();
		a = fetched;
		setFlag('Z',a==0x00);
		setFlag('N',(a&0x80)==0x80);
		additionalCycles++;
	}

	public void LDX() {
		fetch();
		x = fetched;
		setFlag('Z',x==0x00);
		setFlag('N',(x&0x80)==0x80);
		additionalCycles++;
	}

	public void LDY() {
		fetch();
		y = fetched;
		setFlag('Z',y==0x00);
		setFlag('N',(y&0x80)==0x80);
		additionalCycles++;
	}

	public void LSR() {
		fetch();
		setFlag('C',(fetched&0x0001)==0x0001);
		short temp = (short)((0x00FF&fetched) >> 1);
		setFlag('Z',(temp&0x00FF)==0x0000);
		setFlag('N',(temp&0x0080)==0x0080);
		if (lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.ACC) {
			a = (byte)((byte)(temp)&0x00FF);
		} else {
			Bus.write(addressAbsolute, (byte)(temp&0x00FF));
		}
	}

	public void NOP() {
		additionalCycles++;
	}

	public void ORA() {
		fetch();
		a |= fetched;
		setFlag('Z', a==0x00);
		setFlag('N', (a & 0x80)==0x80);

		additionalCycles++;
	}

	public void PHA() {
		Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), a);
		stackPointer--;
	}

	public void PHP() {
		Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), (byte)(flags|0b00110000));
		setFlag('B',false);
		setFlag('U',false);
		stackPointer--;
	}

	public void PHX() {
		Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), x);
		stackPointer--;
	}

	public void PHY() {
		Bus.write((short)(0x0100+Byte.toUnsignedInt(stackPointer)), y);
		stackPointer--;
	}

	public void PLA() {
		stackPointer++;
		a = Bus.read((short)(0x0100+Byte.toUnsignedInt(stackPointer)));
		setFlag('Z', a == 0);
		setFlag('N', (a & 0x80) == 0x80);
	}

	public void PLP() {
		stackPointer++;
		flags = Bus.read((short)(0x0100+Byte.toUnsignedInt(stackPointer)));
		setFlag('U', true);
	}

	public void PLX() {
		stackPointer++;
		x = Bus.read((short)(0x0100+Byte.toUnsignedInt(stackPointer)));
		setFlag('Z', x == 0);
		setFlag('N', (x & 0x80) == 0x80);
	}

	public void PLY() {
		stackPointer++;
		y = Bus.read((short)(0x0100+Byte.toUnsignedInt(stackPointer)));
		setFlag('Z', y == 0);
		setFlag('N', (y & 0x80) == 0x80);
	}

	public void RMB0() { RMBn(0); }
	public void RMB1() { RMBn(1); }
	public void RMB2() { RMBn(2); }
	public void RMB3() { RMBn(3); }
	public void RMB4() { RMBn(4); }
	public void RMB5() { RMBn(5); }
	public void RMB6() { RMBn(6); }
	public void RMB7() { RMBn(7); }

	private void RMBn(int n) {
		fetch();
		short temp = (short)(fetched&(0xff^(0b1<<n))); // reset the nth bit with a mask of all 1's except a 0 in the nth place
		Bus.write(addressAbsolute, (byte)(temp&0x00FF));
	}

	public void ROL() {
		fetch();
		short temp = (short)((fetched<<1) | (getFlag('C') ? 1 : 0));
		setFlag('C',(temp&0xFF00) == 0xFF00);
		setFlag('Z',(temp&0x00FF) == 0x0000);
		setFlag('N',(temp&0x0080) == 0x0080);
		if (lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.ACC) {
			a = (byte)(temp&0x00FF);
		} else {
			Bus.write(addressAbsolute, (byte)(temp&0x00FF));
		}
	}

	public void ROR() {
		fetch();
		short temp = (short)(((0x00FF&fetched)>>1) | (short)(getFlag('C') ? 0x0080 : 0));
		setFlag('C',(fetched&0x01) == 0x01);
		setFlag('Z',(temp&0x00FF) == 0x0000);
		setFlag('N',(temp&0x0080) == 0x0080);
		if (lookup[Byte.toUnsignedInt(opcode)].addressMode == AddressMode.ACC) {
			a = (byte)(temp&0x00FF);
		} else {
			Bus.write(addressAbsolute, (byte)(temp&0x00FF));
		}
	}

	public void RTI() {
		stackPointer++;
		flags = Bus.read((short)(0x100+Byte.toUnsignedInt(stackPointer)));
		flags = (byte)(flags & (getFlag('B') ? 0b11101111 : 0));
		flags = (byte)(flags & (getFlag('U') ? 0b11011111 : 0));

		stackPointer++;
		byte lo = Bus.read((short)(0x100+Byte.toUnsignedInt(stackPointer)));
		stackPointer++;
		byte hi = Bus.read((short)(0x100+Byte.toUnsignedInt(stackPointer)));
		programCounter = (short)(Byte.toUnsignedInt(lo)+256*Byte.toUnsignedInt(hi));
	}

	public void RTS() {
		stackPointer++;
		byte lo = Bus.read((short)(0x100+Byte.toUnsignedInt(stackPointer)));
		stackPointer++;
		byte hi = Bus.read((short)(0x100+Byte.toUnsignedInt(stackPointer)));
		programCounter = (short)(Byte.toUnsignedInt(lo)+256*Byte.toUnsignedInt(hi));

		programCounter++;
	}

	public void SBC() {
		fetch();
		short value = (short)(((short)fetched&0xff) ^ (short)0x00FF);
		short temp = (short)(((short)a&0xff) + value + (short)(getFlag('C') ? 1 : 0));
		setFlag('C', temp > 255);
		setFlag('Z', (temp & 0x00FF) == 0);
		setFlag('N', (temp & 0x80) == 0x80);
		setFlag('V', (~((short)a^(short)fetched) & ((short)a^(short)temp) & 0x0080)==0x0080);
		a = (byte)temp;
		additionalCycles++;
	}

	public void SEC() {
		setFlag('C',true);
	}

	public void SED() {
		setFlag('D',true);
	}

	public void SEI() {
		setFlag('I',true);
	}

	public void SMB0() { SMBn(0); }
	public void SMB1() { SMBn(1); }
	public void SMB2() { SMBn(2); }
	public void SMB3() { SMBn(3); }
	public void SMB4() { SMBn(4); }
	public void SMB5() { SMBn(5); }
	public void SMB6() { RMBn(6); }
	public void SMB7() { SMBn(7); }

	private void SMBn(int n) {
		fetch();
		short temp = (short)(fetched|(0b1<<n)); // set the nth bit
		Bus.write(addressAbsolute, (byte)(temp&0x00FF));
	}

	public void STA() {
		Bus.write(addressAbsolute, a);
	}

	public void STP() {
		stopped = true;
	}

	public void STX() {
		Bus.write(addressAbsolute, x);
	}

	public void STY() {
		Bus.write(addressAbsolute, y);
	}

	public void STZ() {
		Bus.write(addressAbsolute, (byte)0);
	}
		
	public void TAX() {
		x = a;
		setFlag('Z',x==0x00);
		setFlag('N',(x&0x80)==0x80);
	}
		
	public void TAY() {
		y = a;
		setFlag('Z',y==0x00);
		setFlag('N',(y&0x80)==0x80);
	}

	public void TRB() {
		fetch();
		short temp = (short)(a&fetched);
		setFlag('Z',(temp&0x00FF)==0x00);
		Bus.write(addressAbsolute, (byte)((temp^0x00FF)&0x00FF));
	}

	public void TSB() {
		fetch();
		short temp = (short)(a&fetched);
		setFlag('Z',(temp&0x00FF)==0x00);
		Bus.write(addressAbsolute, (byte)(temp&0x00FF));
	}

	public void TSX() {
		x = stackPointer;
		setFlag('Z',x==0x00);
		setFlag('N',(x&0x80)==0x80);
	}
		
	public void TXA() {
		a = x;
		setFlag('Z',a==0x00);
		setFlag('N',(a&0x80)==0x80);
	}
		
	public void TXS() {
		stackPointer = x;
	}
		
	public void TYA() {
		a = y;
		setFlag('Z',a==0x00);
		setFlag('N',(a&0x80)==0x80);
	}

	public void WAI() {
		waiting = true;
	}

	public void XXX() {
		if (EaterEmulator.verbose) System.out.println("Illegal Opcode at $"+Integer.toHexString(Short.toUnsignedInt(programCounter)).toUpperCase()+" (" + ROMLoader.byteToHexString(opcode) +") - "+lookup[Byte.toUnsignedInt(opcode)].opcode);
	}
}
