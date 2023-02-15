package com.juse.emulator.devices.cpu.Zilog;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;

import com.juse.emulator.interfaces.Telemetry;
import com.juse.emulator.interfaces.TelemetryInfo;
import com.juse.emulator.interfaces.AddressMap;
import com.juse.emulator.interfaces.Bus;
import com.juse.emulator.interfaces.CPU;
import com.juse.emulator.interfaces.CPUInfo;
import com.juse.emulator.interfaces.ClockLine;
import com.juse.emulator.interfaces.DeviceDebugger;
import com.juse.emulator.interfaces.IOSize;
import com.juse.emulator.util.translate.Convert;
import com.juse.emulator.util.collections.LimitedSizeQueue;

/**
 * $Id: Z80.java 331 2010-09-14 10:30:13Z mviara $
 *
 * Z80 emulator.
 *
 * $Log: Z80.java,v $ Revision 1.4 2008/05/15 17:07:17 mviara Added preliminary
 * support for Z80Pack. Fixed bug in idle loop detection.
 *
 * Revision 1.3 2005/03/18 16:40:02 mviara Added support for stepper.
 *
 * Revision 1.2 2004/06/20 16:27:29 mviara Some minor change.
 *
 */

interface Z80debug
{

	public static final String[] opc1 = { "00 NOP         ", "01 LD BC,nn    ", "02 LD (BC),A   ", "03 INC BC      ",
			"04 INC B       ", "05 DEC B       ", "06 LD B,n      ", "07 RLCA        ", "08 EX AF,AF'   ", "09 ADD HL,BC   ",
			"0A LD A,(BC)   ", "0B DEC BC      ", "0C INC C       ", "0D DEC C       ", "0E LD C,n      ", "0F RRCA        ",
			"10 DJNZ,of     ", "11 LD DE,nn    ", "12 LD (DE),A   ", "13 INC DE      ", "14 INC D       ", "15 DEC D       ",
			"16 LD D,n      ", "17 RLA         ", "18 JR of       ", "19 ADD HL,DE   ", "1A LD A,(DE)   ", "1B DEC DE      ",
			"1C INC E       ", "1D DEC E       ", "1E LD E,n      ", "1F RRA         ", "20 JR NZ,of    ", "21 LD HL,nn    ",
			"22 LD (nn),HL  ", "23 INC HL      ", "24 INC H       ", "25 DEC H       ", "26 LD H,n      ", "27 DAA         ",
			"28 JR Z,of     ", "29 ADD HL,HL   ", "2A LD HL,(nn)  ", "2B DEC HL      ", "2C INC L       ", "2D DEC L       ",
			"2E LD L,n      ", "2F CPL         ", "30 JR NC,of    ", "31 LD SP,nn    ", "32 LD (nn),A   ", "33 INC SP      ",
			"34 INC (HL)    ", "35 DEC (HL)    ", "36 LD (HL),n   ", "37 SCF         ", "38 JR C,of     ", "39 ADD HL,SP   ",
			"3A LD A,(nn)   ", "3B DEC SP      ", "3C INC A       ", "3D DEC A       ", "3E LD A,n      ", "3F CCF         ",
			"40 LD B,B      ", "41 LD B,C      ", "42 LD B,D      ", "43 LD B,E      ", "44 LD B,H      ", "45 LD B,L      ",
			"46 LD B,(HL)   ", "47 LD B,A      ", "48 LD C,B      ", "49 LD C,C      ", "4A LD C,D      ", "4B LD C,E      ",
			"4C LD C,H      ", "4D LD C,L      ", "4E LD C,(HL)   ", "4F LD C,A      ", "50 LD D,B      ", "51 LD D,C      ",
			"52 LD D,D      ", "53 LD D,E      ", "54 LD D,H      ", "55 LD D,L      ", "56 LD D,(HL)   ", "57 LD D,A      ",
			"58 LD E,B      ", "59 LD E,C      ", "5A LD E,D      ", "5B LD E,E      ", "5C LD E,H      ", "5D LD E,L      ",
			"5E LD E,(HL)   ", "5F LD E,A      ", "60 LD H,B      ", "61 LD H,C      ", "62 LD H,D      ", "63 LD H,E      ",
			"64 LD H,H      ", "65 LD H,L      ", "66 LD H,(HL)   ", "67 LD H,A      ", "68 LD L,B      ", "69 LD L,C      ",
			"6A LD L,D      ", "6B LD L,E      ", "6C LD L,H      ", "6D LD L,L      ", "6E LD L,(HL)   ", "6F LD L,A      ",
			"70 LD (HL),B   ", "71 LD (HL),C   ", "72 LD (HL),D   ", "73 LD (HL),E   ", "74 LD (HL),H   ", "75 LD (HL),L   ",
			"76 HALT        ", "77 LD (HL),A   ", "78 LD A,B      ", "79 LD A,C      ", "7A LD A,D      ", "7B LD A,E      ",
			"7C LD A,H      ", "7D LD A,L      ", "7E LD A,(HL)   ", "7F LD A,A      ", "80 ADD A,B     ", "81 ADD A,C     ",
			"82 ADD A,D     ", "83 ADD A,E     ", "84 ADD A,H     ", "85 ADD A,L     ", "86 ADD A,(HL)  ", "87 ADD A,A     ",
			"88 ADC A,B     ", "89 ADC A,C     ", "8A ADC A,D     ", "8B ADC A,E     ", "8C ADC A,H     ", "8D ADC A,L     ",
			"8E ADC A,(HL)  ", "8F ADC A,A     ", "90 SUB B       ", "91 SUB C       ", "92 SUB D       ", "93 SUB E       ",
			"94 SUB H       ", "95 SUB L       ", "96 SUB (HL)    ", "97 SUB A       ", "98 SBC A,B     ", "99 SBC A,C     ",
			"9A SBC A,D     ", "9B SBC A,E     ", "9C SBC A,H     ", "9D SBC A,L     ", "9E SBC A,(HL)  ", "9F SBC A,A     ",
			"A0 AND B       ", "A1 AND C       ", "A2 AND D       ", "A3 AND E       ", "A4 AND H       ", "A5 AND L       ",
			"A6 AND (HL)    ", "A7 AND A       ", "A8 XOR B       ", "A9 XOR C       ", "AA XOR D       ", "AB XOR E       ",
			"AC XOR H       ", "AD XOR L       ", "AE XOR (HL)    ", "AF XOR A       ", "B0 OR B        ", "B1 OR C        ",
			"B2 OR D        ", "B3 OR E        ", "B4 OR H        ", "B5 OR L        ", "B6 OR (HL)     ", "B7 OR A        ",
			"B8 CP B        ", "B9 CP C        ", "BA CP D        ", "BB CP E        ", "BC CP H        ", "BD CP L        ",
			"BE CP (HL)     ", "BF CP A        ", "C0 RET NZ      ", "C1 POP BC      ", "C2 JP NZ,nn    ", "C3 JP nn       ",
			"C4 CALL NZ,nn  ", "C5 PUSH BC     ", "C6 ADD A,n     ", "C7 RST &00     ", "C8 RET Z       ", "C9 RET         ",
			"CA JP Z,nn     ", "CB -->2        ", "CC CALL Z,nn   ", "CD CALL nn     ", "CE ADC A,n     ", "CF RST &08     ",
			"D0 RET NC      ", "D1 POP DE      ", "D2 JP NC,nn    ", "D3 OUT (n),A   ", "D4 CALL NC,nn  ", "D5 PUSH DE     ",
			"D6 SUB n       ", "D7 RST &10     ", "D8 RET C       ", "D9 EXX         ", "DA JP C,nn     ", "DB IN A,(n)    ",
			"DC CALL C,nn   ", "DD -->IX       ", "DE SBC A,n     ", "DF RST &18     ", "E0 RET PO      ", "E1 POP HL      ",
			"E2 JP PO,nn    ", "E3 EX (SP),HL  ", "E4 CALL PO,nn  ", "E5 PUSH HL     ", "E6 AND n       ", "E7 RST &20     ",
			"E8 RET PE      ", "E9 JP (HL)     ", "EA JP PE,nn    ", "EB EX DE,HL    ", "EC CALL PE,nn  ", "ED -->3        ",
			"EE XOR n       ", "EF RST &28     ", "F0 RET P       ", "F1 POP AF      ", "F2 JP P,nn     ", "F3 DI          ",
			"F4 CALL P,nn   ", "F5 PUSH AF     ", "F6 OR n        ", "F7 RST &30     ", "F8 RET M       ", "F9 LD SP,HL    ",
			"FA JP M,nn     ", "FB EI          ", "FC CALL M,nn   ", "FD -->IY       ", "FE CP n        ", "FF RST &38     " };

	public static final String[] opc2 = { "00 ???????????", "01 ???????????", "02 ???????????", "03 ???????????", "04 ???????????",
			"05 ???????????", "06 ???????????", "07 ???????????", "08 ???????????", "09 ???????????", "0A ???????????",
			"0B ???????????", "0C ???????????", "0D ???????????", "0E ???????????", "0F ???????????", "10 ???????????",
			"11 ???????????", "12 ???????????", "13 ???????????", "14 ???????????", "15 ???????????", "16 ???????????",
			"17 ???????????", "18 ???????????", "19 ???????????", "1A ???????????", "1B ???????????", "1C ???????????",
			"1D ???????????", "1E ???????????", "1F ???????????", "20 ???????????", "21 ???????????", "22 ???????????",
			"23 ???????????", "24 ???????????", "25 ???????????", "26 ???????????", "27 ???????????", "28 ???????????",
			"29 ???????????", "2A ???????????", "2B ???????????", "2C ???????????", "2D ???????????", "2E ???????????",
			"2F ???????????", "30 ???????????", "31 ???????????", "32 ???????????", "33 ???????????", "34 ???????????",
			"35 ???????????", "36 ???????????", "37 ???????????", "38 ???????????", "39 ???????????", "3A ???????????",
			"3B ???????????", "3C ???????????", "3D ???????????", "3E ???????????", "3F ???????????", "40 BIT 0,B", "41 BIT 0,C",
			"42 BIT 0,D", "43 BIT 0,E", "44 BIT 0,H", "45 BIT 0,L", "46 BIT 0,(HL)", "47 BIT 0,A", "48 BIT 1,B", "49 BIT 1,C",
			"4A BIT 1,D", "4B BIT 1,E", "4C BIT 1,H", "4D BIT 1,L", "4E BIT 1,(HL)", "4F BIT 1,A", "50 BIT 2,B", "51 BIT 2,C",
			"52 BIT 2,D", "53 BIT 2,E", "54 BIT 2,H", "55 BIT 2,L", "56 BIT 2,(HL)", "57 BIT 2,A", "58 BIT 3,B", "59 BIT 3,C",
			"5A BIT 3,D", "5B BIT 3,E", "5C BIT 3,H", "5D BIT 3,L", "5E BIT 3,(HL)", "5F BIT 3,A", "60 BIT 4,B", "61 BIT 4,C",
			"62 BIT 4,D", "63 BIT 4,E", "64 BIT 4,H", "65 BIT 4,L", "66 BIT 4,(HL)", "67 BIT 4,A", "68 BIT 5,B", "69 BIT 5,C",
			"6A BIT 5,D", "6B BIT 5,E", "6C BIT 5,H", "6D BIT 5,L", "6E BIT 5,(HL)", "6F BIT 5,A", "70 BIT 6,B", "71 BIT 6,C",
			"72 BIT 6,D", "73 BIT 6,E", "74 BIT 6,H", "75 BIT 6,L", "76 BIT 6,(HL)", "77 BIT 6,A", "78 BIT 7,B", "79 BIT 7,C",
			"7A BIT 7,D", "7B BIT 7,E", "7C BIT 7,H", "7D BIT 7,L", "7E BIT 7,(HL)", "7F BIT 7,A", "80 ???????????",
			"81 ???????????", "82 ???????????", "83 ???????????", "84 ???????????", "85 ???????????", "86 ???????????",
			"87 ???????????", "88 ???????????", "89 ???????????", "8A ???????????", "8B ???????????", "8C ???????????",
			"8D ???????????", "8E ???????????", "8F ???????????", "90 ???????????", "91 ???????????", "92 ???????????",
			"93 ???????????", "94 ???????????", "95 ???????????", "96 ???????????", "97 ???????????", "98 ???????????",
			"99 ???????????", "9A ???????????", "9B ???????????", "9C ???????????", "9D ???????????", "9E ???????????",
			"9F ???????????", "A0 ???????????", "A1 ???????????", "A2 ???????????", "A3 ???????????", "A4 ???????????",
			"A5 ???????????", "A6 ???????????", "A7 ???????????", "A8 ???????????", "A9 ???????????", "AA ???????????",
			"AB ???????????", "AC ???????????", "AD ???????????", "AE ???????????", "AF ???????????", "B0 ???????????",
			"B1 ???????????", "B2 ???????????", "B3 ???????????", "B4 ???????????", "B5 ???????????", "B6 ???????????",
			"B7 ???????????", "B8 ???????????", "B9 ???????????", "BA ???????????", "BB ???????????", "BC ???????????",
			"BD ???????????", "BE ???????????", "BF ???????????", "C0 ???????????", "C1 ???????????", "C2 ???????????",
			"C3 ???????????", "C4 ???????????", "C5 ???????????", "C6 ???????????", "C7 ???????????", "C8 ???????????",
			"C9 ???????????", "CA ???????????", "CB ???????????", "CC ???????????", "CD ???????????", "CE ???????????",
			"CF ???????????", "D0 ???????????", "D1 ???????????", "D2 ???????????", "D3 ???????????", "D4 ???????????",
			"D5 ???????????", "D6 ???????????", "D7 ???????????", "D8 ???????????", "D9 ???????????", "DA ???????????",
			"DB ???????????", "DC ???????????", "DD ???????????", "DE ???????????", "DF ???????????", "E0 ???????????",
			"E1 ???????????", "E2 ???????????", "E3 ???????????", "E4 ???????????", "E5 ???????????", "E6 ???????????",
			"E7 ???????????", "E8 ???????????", "E9 ???????????", "EA ???????????", "EB ???????????", "EC ???????????",
			"ED ???????????", "EE ???????????", "EF ???????????", "F0 ???????????", "F1 ???????????", "F2 ???????????",
			"F3 ???????????", "F4 ???????????", "F5 ???????????", "F6 ???????????", "F7 ???????????", "F8 ???????????",
			"F9 ???????????", "FA ???????????", "FB ???????????", "FC ???????????", "FD ???????????", "FE ???????????",
			"FF ???????????" };

	public static final String[] opc3 = { "00 ???????????", "01 ???????????", "02 ???????????", "03 ???????????", "04 ???????????",
			"05 ???????????", "06 ???????????", "07 ???????????", "08 ???????????", "09 ???????????", "0A ???????????",
			"0B ???????????", "0C ???????????", "0D ???????????", "0E ???????????", "0F ???????????", "10 ???????????",
			"11 ???????????", "12 ???????????", "13 ???????????", "14 ???????????", "15 ???????????", "16 ???????????",
			"17 ???????????", "18 ???????????", "19 ???????????", "1A ???????????", "1B ???????????", "1C ???????????",
			"1D ???????????", "1E ???????????", "1F ???????????", "20 ???????????", "21 ???????????", "22 ???????????",
			"23 ???????????", "24 ???????????", "25 ???????????", "26 ???????????", "27 ???????????", "28 ???????????",
			"29 ???????????", "2A ???????????", "2B ???????????", "2C ???????????", "2D ???????????", "2E ???????????",
			"2F ???????????", "30 ???????????", "31 ???????????", "32 ???????????", "33 ???????????", "34 ???????????",
			"35 ???????????", "36 ???????????", "37 ???????????", "38 ???????????", "39 ???????????", "3A ???????????",
			"3B ???????????", "3C ???????????", "3D ???????????", "3E ???????????", "3F ???????????", "40 ???????????",
			"41 ???????????", "42 ???????????", "ED43 LD (nn),BC", "44 ???????????", "45 ???????????", "46 ???????????",
			"ED47 LD I,A", "48 ???????????", "49 ???????????", "4A ???????????", "4B ???????????", "4C ???????????",
			"4D ???????????", "4E ???????????", "4F ???????????", "50 ???????????", "51 ???????????", "52 ???????????",
			"ED53 LD (nn),DE", "54 ???????????", "55 ???????????", "56 ???????????", "ED57 LD A,I", "58 ???????????",
			"59 ???????????", "5A ???????????", "5B ???????????", "5C ???????????", "5D ???????????", "ED5E IM 2", "5F ???????????",
			"60 ???????????", "61 ???????????", "62 ???????????", "ED63 LD (nn),HL", "64 ???????????", "65 ???????????",
			"66 ???????????", "67 ???????????", "68 ???????????", "69 ???????????", "6A ???????????", "6B ???????????",
			"6C ???????????", "6D ???????????", "6E ???????????", "6F ???????????", "70 ???????????", "71 ???????????",
			"72 ???????????", "ED73 LD (nn),SP", "74 ???????????", "75 ???????????", "76 ???????????", "77 ???????????",
			"78 ???????????", "79 ???????????", "7A ???????????", "7B ???????????", "7C ???????????", "7D ???????????",
			"7E ???????????", "7F ???????????", "80 ???????????", "81 ???????????", "82 ???????????", "83 ???????????",
			"84 ???????????", "85 ???????????", "86 ???????????", "87 ???????????", "88 ???????????", "89 ???????????",
			"8A ???????????", "8B ???????????", "8C ???????????", "8D ???????????", "8E ???????????", "8F ???????????",
			"90 ???????????", "91 ???????????", "92 ???????????", "93 ???????????", "94 ???????????", "95 ???????????",
			"96 ???????????", "97 ???????????", "98 ???????????", "99 ???????????", "9A ???????????", "9B ???????????",
			"9C ???????????", "9D ???????????", "9E ???????????", "9F ???????????", "A0 ???????????", "A1 ???????????",
			"A2 ???????????", "A3 ???????????", "A4 ???????????", "A5 ???????????", "A6 ???????????", "A7 ???????????",
			"A8 ???????????", "A9 ???????????", "AA ???????????", "AB ???????????", "AC ???????????", "AD ???????????",
			"AE ???????????", "AF ???????????", "EDB0 LDIR", "B1 ???????????", "B2 ???????????", "B3 ???????????", "B4 ???????????",
			"B5 ???????????", "B6 ???????????", "B7 ???????????", "B8 ???????????", "B9 ???????????", "BA ???????????",
			"BB ???????????", "BC ???????????", "BD ???????????", "BE ???????????", "BF ???????????", "C0 ???????????",
			"C1 ???????????", "C2 ???????????", "C3 ???????????", "C4 ???????????", "C5 ???????????", "C6 ???????????",
			"C7 ???????????", "C8 ???????????", "C9 ???????????", "CA ???????????", "CB ???????????", "CC ???????????",
			"CD ???????????", "CE ???????????", "CF ???????????", "D0 ???????????", "D1 ???????????", "D2 ???????????",
			"D3 ???????????", "D4 ???????????", "D5 ???????????", "D6 ???????????", "D7 ???????????", "D8 ???????????",
			"D9 ???????????", "DA ???????????", "DB ???????????", "DC ???????????", "DD ???????????", "DE ???????????",
			"DF ???????????", "E0 ???????????", "E1 ???????????", "E2 ???????????", "E3 ???????????", "E4 ???????????",
			"E5 ???????????", "E6 ???????????", "E7 ???????????", "E8 ???????????", "E9 ???????????", "EA ???????????",
			"EB ???????????", "EC ???????????", "ED ???????????", "EE ???????????", "EF ???????????", "F0 ???????????",
			"F1 ???????????", "F2 ???????????", "F3 ???????????", "F4 ???????????", "F5 ???????????", "F6 ???????????",
			"F7 ???????????", "F8 ???????????", "F9 ???????????", "FA ???????????", "FB ???????????", "FC ???????????",
			"FD ???????????", "FE ???????????", "FF ???????????" };
	
		public static final String[] opcIX = { "00 ???????????", "01 ???????????", "02 ???????????", "03 ???????????", "04 ???????????",
			"05 ???????????", "06 ???????????", "07 ???????????", "08 ???????????", "09 ???????????", "0A ???????????",
			"0B ???????????", "0C ???????????", "0D ???????????", "0E ???????????", "0F ???????????", "10 ???????????",
			"11 ???????????", "12 ???????????", "13 ???????????", "14 ???????????", "15 ???????????", "16 ???????????",
			"17 ???????????", "18 ???????????", "19 ???????????", "1A ???????????", "1B ???????????", "1C ???????????",
			"1D ???????????", "1E ???????????", "1F ???????????", "20 ???????????", "21 LD IX,nn   ", "22 ???????????",
			"23 INC IX     ", "24 ???????????", "25 ???????????", "26 ???????????", "27 ???????????", "28 ???????????",
			"29 ???????????", "2A ???????????", "2B ???????????", "2C ???????????", "2D ???????????", "2E ???????????",
			"2F ???????????", "30 ???????????", "31 ???????????", "32 ???????????", "33 ???????????", "34 ???????????",
			"35 ???????????", "36 ???????????", "37 ???????????", "38 ???????????", "39 ???????????", "3A ???????????",
			"3B ???????????", "3C ???????????", "3D ???????????", "3E ???????????", "3F ???????????", "40 ???????????",
			"41 ???????????", "42 ???????????", "ED43 LD (nn),BC", "44 ???????????", "45 ???????????", "46 ???????????",
			"ED47 LD I,A", "48 ???????????", "49 ???????????", "4A ???????????", "4B ???????????", "4C ???????????",
			"4D ???????????", "4E ???????????", "4F ???????????", "50 ???????????", "51 ???????????", "52 ???????????",
			"ED53 LD (nn),DE", "54 ???????????", "55 ???????????", "56 ???????????", "ED57 LD A,I", "58 ???????????",
			"59 ???????????", "5A ???????????", "5B ???????????", "5C ???????????", "5D ???????????", "ED5E IM 2", "5F ???????????",
			"60 ???????????", "61 ???????????", "62 ???????????", "ED63 LD (nn),HL", "64 ???????????", "65 ???????????",
			"66 ???????????", "67 ???????????", "68 ???????????", "69 ???????????", "6A ???????????", "6B ???????????",
			"6C ???????????", "6D ???????????", "6E ???????????", "6F ???????????", "70 ???????????", "71 ???????????",
			"72 ???????????", "ED73 LD (nn),SP", "74 ???????????", "75 ???????????", "76 ???????????", "77 ???????????",
			"78 ???????????", "79 ???????????", "7A ???????????", "7B ???????????", "7C ???????????",  "7D LD A,IXL   ",
			"7E ???????????", "7F ???????????", "80 ???????????", "81 ???????????", "82 ???????????", "83 ???????????",
			"84 ???????????", "85 ???????????", "86 ???????????", "87 ???????????", "88 ???????????", "89 ???????????",
			"8A ???????????", "8B ???????????", "8C ???????????", "8D ???????????", "8E ???????????", "8F ???????????",
			"90 ???????????", "91 ???????????", "92 ???????????", "93 ???????????", "94 ???????????", "95 ???????????",
			"96 ???????????", "97 ???????????", "98 ???????????", "99 ???????????", "9A ???????????", "9B ???????????",
			"9C ???????????", "9D ???????????", "9E ???????????", "9F ???????????", "A0 ???????????", "A1 ???????????",
			"A2 ???????????", "A3 ???????????", "A4 ???????????", "A5 ???????????", "A6 ???????????", "A7 ???????????",
			"A8 ???????????", "A9 ???????????", "AA ???????????", "AB ???????????", "AC ???????????", "AD ???????????",
			"AE ???????????", "AF ???????????", "EDB0 LDIR", "B1 ???????????", "B2 ???????????", "B3 ???????????", "B4 ???????????",
			"B5 ???????????", "B6 ???????????", "B7 ???????????", "B8 ???????????", "B9 ???????????", "BA ???????????",
			"BB ???????????", "BC ???????????", "BD ???????????", "BE ???????????", "BF ???????????", "C0 ???????????",
			"C1 ???????????", "C2 ???????????", "C3 ???????????", "C4 ???????????", "C5 ???????????", "C6 ???????????",
			"C7 ???????????", "C8 ???????????", "C9 ???????????", "CA ???????????", "CB ???????????", "CC ???????????",
			"CD ???????????", "CE ???????????", "CF ???????????", "D0 ???????????", "D1 ???????????", "D2 ???????????",
			"D3 ???????????", "D4 ???????????", "D5 ???????????", "D6 ???????????", "D7 ???????????", "D8 ???????????",
			"D9 ???????????", "DA ???????????", "DB ???????????", "DC ???????????", "DD ???????????", "DE ???????????",
			"DF ???????????", "E0 ???????????", "E1 ???????????", "E2 ???????????", "E3 ???????????", "E4 ???????????",
			"E5 ???????????", "E6 ???????????", "E7 ???????????", "E8 ???????????", "E9 ???????????", "EA ???????????",
			"EB ???????????", "EC ???????????", "ED ???????????", "EE ???????????", "EF ???????????", "F0 ???????????",
			"F1 ???????????", "F2 ???????????", "F3 ???????????", "F4 ???????????", "F5 ???????????", "F6 ???????????",
			"F7 ???????????", "F8 ???????????", "F9 ???????????", "FA ???????????", "FB ???????????", "FC ???????????",
			"FD ???????????", "FE ???????????", "FF ???????????" };

		public static final String[] opcIY = { 
				"00 ???????????", "01 ???????????", "02 ???????????", "03 ???????????", "04 ???????????", "05 ???????????", "06 ???????????", "07 ???????????", 
				"08 ???????????", "09 ???????????", "0A ???????????", "0B ???????????", "0C ???????????", "0D ???????????", "0E ???????????", "0F ???????????", 
				"10 ???????????", "11 ???????????", "12 ???????????", "13 ???????????", "14 ???????????", "15 ???????????", "16 ???????????", "17 ???????????", 
				"18 ???????????", "19 ???????????", "1A ???????????", "1B ???????????", "1C ???????????", "1D ???????????", "1E ???????????", "1F ???????????", 
				"20 ???????????", "21 LD IY,nn   ", "22 ???????????", "23 INC IY     ", "24 ???????????", "25 ???????????", "26 ???????????", "27 ???????????", 
				"28 ???????????", "29 ???????????", "2A ???????????", "2B DEC IY     ", "2C ???????????", "2D ???????????", "2E ???????????", "2F ???????????", 
				"30 ???????????", "31 ???????????", "32 ???????????", "33 ???????????", "34 ???????????", "35 ???????????", "36 ???????????", "37 ???????????", 
				"38 ???????????", "39 ???????????", "3A ???????????", "3B ???????????", "3C ???????????", "3D ???????????", "3E ???????????", "3F ???????????", 
				"40 ???????????", "41 ???????????", "42 ???????????", "43 ???????????", "44 ???????????", "45 ???????????", "46 ???????????", "47 ???????????", 
				"48 ???????????", "49 ???????????", "4A ???????????", "4B ???????????", "4C ???????????", "4D ???????????", "4E ???????????", "4F ???????????", 
				"50 ???????????", "51 ???????????", "52 ???????????", "53 ???????????", "54 ???????????", "55 ???????????", "56 ???????????", "57 ???????????", 
				"58 ???????????", "59 ???????????", "5A ???????????", "5B ???????????", "5C ???????????", "5D ???????????", "5E ???????????", "5F ???????????",
				"60 ???????????", "61 ???????????", "62 ???????????", "ED63 LD (nn),HL", "64 ???????????", "65 ???????????", "66 ???????????", "67 ???????????", 
				"68 ???????????", "69 ???????????", "6A ???????????", "6B ???????????", "6C ???????????", "6D ???????????", "6E ???????????", "6F ???????????", 
				"70 ???????????", "71 ???????????",
				"72 ???????????", "73 ???????????", "74 ???????????", "75 ???????????", "76 ???????????", "77 ???????????",
				"78 ???????????", "79 ???????????", "7A ???????????", "7B ???????????", "7C ???????????",  "7D LD A,IYL",
				"7E ???????????", "7F ???????????", "80 ???????????", "81 ???????????", "82 ???????????", "83 ???????????",
				"84 ???????????", "85 ???????????", "86 ???????????", "87 ???????????", "88 ???????????", "89 ???????????",
				"8A ???????????", "8B ???????????", "8C ???????????", "8D ???????????", "8E ???????????", "8F ???????????",
				"90 ???????????", "91 ???????????", "92 ???????????", "93 ???????????", "94 ???????????", "95 ???????????",
				"96 ???????????", "97 ???????????", "98 ???????????", "99 ???????????", "9A ???????????", "9B ???????????",
				"9C ???????????", "9D ???????????", "9E ???????????", "9F ???????????", "A0 ???????????", "A1 ???????????",
				"A2 ???????????", "A3 ???????????", "A4 ???????????", "A5 ???????????", "A6 ???????????", "A7 ???????????",
				"A8 ???????????", "A9 ???????????", "AA ???????????", "AB ???????????", "AC ???????????", "AD ???????????",
				"AE ???????????", "AF ???????????", "B0 ???????????", "B1 ???????????", "B2 ???????????", "B3 ???????????", "B4 ???????????",
				"B5 ???????????", "B6 ???????????", "B7 ???????????", "B8 ???????????", "B9 ???????????", "BA ???????????",
				"BB ???????????", "BC ???????????", "BD ???????????", "BE ???????????", "BF ???????????", "C0 ???????????",
				"C1 ???????????", "C2 ???????????", "C3 ???????????", "C4 ???????????", "C5 ???????????", "C6 ???????????",
				"C7 ???????????", "C8 ???????????", "C9 ???????????", "CA ???????????", "CB ???????????", "CC ???????????",
				"CD ???????????", "CE ???????????", "CF ???????????", "D0 ???????????", "D1 ???????????", "D2 ???????????",
				"D3 ???????????", "D4 ???????????", "D5 ???????????", "D6 ???????????", "D7 ???????????", "D8 ???????????",
				"D9 ???????????", "DA ???????????", "DB ???????????", "DC ???????????", "DD ???????????", "DE ???????????",
				"DF ???????????", "E0 ???????????", "E1 ???????????", "E2 ???????????", "E3 ???????????", "E4 ???????????",
				"E5 ???????????", "E6 ???????????", "E7 ???????????", "E8 ???????????", "E9 ???????????", "EA ???????????",
				"EB ???????????", "EC ???????????", "ED ???????????", "EE ???????????", "EF ???????????", "F0 ???????????",
				"F1 ???????????", "F2 ???????????", "F3 ???????????", "F4 ???????????", "F5 ???????????", "F6 ???????????",
				"F7 ???????????", "F8 ???????????", "F9 ???????????", "FA ???????????", "FB ???????????", "FC ???????????",
				"FD ???????????", "FE ???????????", "FF ???????????" };

}

/**
 * CPUZ80 emulates a Z80's behaviour Written in 2001 by E.Duijs as a part of
 * JEmu
 *
 *
 * V1.00 + Many programs run ok. + removed generic parsing by big switch/case
 * blocks for better performance + made functions for many instructions and
 * flags + added NMI - IM0 not ok yet (is now always RST 38) - some missing
 * instructions - DAA not ok, probably due to missing/incorrect half-carry
 * updates
 *
 * V1.1 + IM0 now also supports calls to other locations (other than 0x38) + DAA
 * is corrected + Fixed many flagupdates + An interface 'driver' is passed to
 * exec() instead of a class. Makes it more flexible. + Implements interface
 * 'cpu'. - IM0 doesn't support instructions other than calls, but this is no
 * big deal... - still some missing instructions - code needs to be cleaned up -
 * debugger needs to be finished (mainly the opcode tables). - out & in need to
 * be externalized (it's added in interface 'driver' already)
 *
 * V1.2 + Flag calculations are not done in F anymore but all flags are booleans
 * (faster) + 1 Minor bugfix...
 *
 * V1.3 + Added precalculated flag tables for speed. + Removed the boolean
 * representation for flags. + The flag arrays also fixed some issues that were
 * apparently flag related. + Added some missing instructions. - Because of the
 * flag tables, it takes more memory now.
 *
 * V1.4 + Added INI and IND opcodes
 *
 * V1.41 + Removed localization of PC from fetch-decode loop to support
 * protection in for example Scramble. I'll probably remove localization of all
 * registers later. It was a speed optimization, but the result is only a very
 * minor speedup. Also, it makes further optimizations difficult if not
 * impossible.
 *
 * V1.42 + Fixed LD D,HX / LD D,LX / LD D,HY / LD D,LY
 *
 * V1.5 + Restructured fetch/decode/exec loop so that it performs better on Sun
 * JRE (HotSpot didn't compile exec() at all). + Better maintainable + Fixed one
 * index instruction.
 */

public class Z80 implements Z80debug, CPU, ClockLine, DeviceDebugger, CPUInfo
{
	public static final int IM0 = 0;
	public static final int IM1 = 1;
	public static final int IM2 = 2;

	/** Interrupt type */
	public static final int INTERRUPT_TYPE_IRQ = 0;
	/** Interrupt type */
	public static final int INTERRUPT_TYPE_NMI = 1;
	/** Interrupt type */
	public static final int INTERRUPT_TYPE_FIRQ = 2;
	/** Interrupt type */
	public static final int INTERRUPT_TYPE_IGNORE = -1;

	/** Property type */
	public static final int PROPERTY_Z80_IRQ_VECTOR = 0;

	static final boolean DEBUG = true; // fixme

	// registers
	public int A, F, B, C, D, E, H, L, I, R;
	public int A1, F1, B1, C1, D1, E1, H1, L1;
	public int PC;
	public int SP = 0x10000;
	public int IX, IY;
	public int IXYd = 0; // IX+d or IY+d

	//private int memory[] = new int[0x10000];

	// states
	public boolean state_HALT = false;
	private boolean state_INIT = false;
	// interrupt
	public int IM = 0; // interrupt mode
	public boolean IFF0 = false; // IRQ interrupt flip-flop
	public boolean IFF1 = false; // NMI interrupt flip-flop
	public int I_Vector = 0;
	public boolean IRQ = false; // interrupt request
	public boolean NMI = false; // Non-maskable interrupt
	private boolean goingToirq = false;// used to execute 1 more instruction
										// after an irq
	// Current cycle of execution
	public int cycle = 0;
	public int cycleCounter;
	// Currently executed instruction
	private int instruction = 0;
	// Misc
	private int tmp, tmp1, tmp2, tmp3;
	private int PPC = 0; // previous PC - used by debugger
	private static final int[] bitSet = { 1, 2, 4, 8, 16, 32, 64, 128 }; // lookup
																			// table
																			// for
																			// setting
																			// a
																			// bit
																			// of
																			// an
																			// 8-bit
																			// value
																			// using
																			// OR
	private static final int[] bitRes = { 254, 253, 251, 247, 239, 223, 191, 127 }; // lookup
																					// table
																					// for
																					// resetting
																					// a
																					// bit
																					// of
																					// an
																					// 8-bit
																					// value
																					// using
																					// AND
	// Debug info
	static int debugLevel = 0;
	static int debugBreakPoint = 0;
	static boolean debugEnabled = false;
	boolean startSlice = true;
	// flag tables
	private static final boolean parity[] = new boolean[256];
	private static final int SZ[] = new int[256];
	private static final int SZ_BIT[] = new int[256];
	private static final int SZP[] = new int[256];
	private static final int SZHV_inc[] = new int[256];
	private static final int SZHV_dec[] = new int[256];
	private static final int SZHVC_Add[] = new int[2 * 256 * 256];
	private static final int SZHVC_sub[] = new int[2 * 256 * 256];
	private static final int SF = 0x80;
	private static final int ZF = 0x40;
	private static final int YF = 0x20;
	private static final int HF = 0x10;
	private static final int XF = 0x08;
	private static final int VF = 0x04;
	private static final int PF = 0x04;
	private static final int NF = 0x02;
	private static final int CF = 0x01;

	private String tag;

	
	private Bus cpuBus = null;
	private List<String> history = new LimitedSizeQueue<String>(2000);
	private long startTime = System.currentTimeMillis();;
	private long clocksPerSecond = 0;
	static private TelemetryInfo telemetry = new TelemetryInfo();
	
	static
	{
		int p = 0;

		for (int i = 0; i < 256; i++)
		{
			boolean bp = true;
			for (int j = 0; j < 8; j++)
			{
				if ((i & (1 << j)) != 0)
				{
					bp = !bp;
				}
			}
			parity[i] = bp;

			p = 0;

			if ((i & 0x01) != 0)
				++p;
			if ((i & 0x02) != 0)
				++p;
			if ((i & 0x04) != 0)
				++p;
			if ((i & 0x08) != 0)
				++p;
			if ((i & 0x10) != 0)
				++p;
			if ((i & 0x20) != 0)
				++p;
			if ((i & 0x40) != 0)
				++p;
			if ((i & 0x80) != 0)
				++p;
			SZ[i] = (i != 0) ? i & 0x80 : 0x40;
			SZ[i] |= (i & (0x20 | 0x08)); /* undocumented flag bits 5+3 */
			SZ_BIT[i] = (i != 0) ? i & 0x80 : 0x40 | 0x04;
			SZ_BIT[i] |= (i & (0x20 | 0x08)); /* undocumented flag bits 5+3 */
			SZP[i] = SZ[i] | (((p & 1) != 0) ? 0 : 0x04);
			SZHV_inc[i] = SZ[i];
			if (i == 0x80)
			{
				SZHV_inc[i] |= 0x04;
			}
			if ((i & 0x0f) == 0x00)
			{
				SZHV_inc[i] |= 0x10;
			}
			SZHV_dec[i] = SZ[i] | 0x02;
			if (i == 0x7f)
			{
				SZHV_dec[i] |= 0x04;
			}
			if ((i & 0x0f) == 0x0f)
			{
				SZHV_dec[i] |= 0x10;
			}
		}

		int oldval, newval, val;
		int padd, padc, psub, psbc;
		padd = 0 * 256;
		padc = 256 * 256;
		psub = 0 * 256;
		psbc = 256 * 256;
		for (oldval = 0; oldval < 256; oldval++)
		{
			for (newval = 0; newval < 256; newval++)
			{
				/* add or adc w/o carry set */
				val = newval - oldval;

				if (newval != 0)
				{
					if ((newval & 0x80) != 0)
					{
						SZHVC_Add[padd] = SF;
					}
					else
					{
						SZHVC_Add[padd] = 0;
					}
				}
				else
				{
					SZHVC_Add[padd] = ZF;
				}

				SZHVC_Add[padd] |= (newval
						& (YF | XF)); /* undocumented flag bits 5+3 */

				if ((newval & 0x0f) < (oldval & 0x0f))
				{
					SZHVC_Add[padd] |= HF;
				}
				if (newval < oldval)
				{
					SZHVC_Add[padd] |= CF;
				}
				if (((val ^ oldval ^ 0x80) & (val ^ newval) & 0x80) != 0)
				{
					SZHVC_Add[padd] |= VF;
				}
				padd++;

				/* adc with carry set */
				val = newval - oldval - 1;
				if (newval != 0)
				{
					if ((newval & 0x80) != 0)
					{
						SZHVC_Add[padc] = SF;
					}
					else
					{
						SZHVC_Add[padc] = 0;
					}
				}
				else
				{
					SZHVC_Add[padc] = ZF;
				}

				SZHVC_Add[padc] |= (newval
						& (YF | XF)); /* undocumented flag bits 5+3 */
				if ((newval & 0x0f) <= (oldval & 0x0f))
				{
					SZHVC_Add[padc] |= HF;
				}
				if (newval <= oldval)
				{
					SZHVC_Add[padc] |= CF;
				}
				if (((val ^ oldval ^ 0x80) & (val ^ newval) & 0x80) != 0)
				{
					SZHVC_Add[padc] |= VF;
				}
				padc++;

				/* cp, sub or sbc w/o carry set */
				val = oldval - newval;
				if (newval != 0)
				{
					if ((newval & 0x80) != 0)
					{
						SZHVC_sub[psub] = NF | SF;
					}
					else
					{
						SZHVC_sub[psub] = NF;
					}
				}
				else
				{
					SZHVC_sub[psub] = NF | ZF;
				}

				SZHVC_sub[psub] |= (newval
						& (YF | XF)); /* undocumented flag bits 5+3 */
				if ((newval & 0x0f) > (oldval & 0x0f))
				{
					SZHVC_sub[psub] |= HF;
				}
				if (newval > oldval)
				{
					SZHVC_sub[psub] |= CF;
				}
				if (((val ^ oldval) & (oldval ^ newval) & 0x80) != 0)
				{
					SZHVC_sub[psub] |= VF;
				}
				psub++;

				/* sbc with carry set */
				val = oldval - newval - 1;
				if (newval != 0)
				{
					if ((newval & 0x80) != 0)
					{
						SZHVC_sub[psbc] = NF | SF;
					}
					else
					{
						SZHVC_sub[psbc] = NF;
					}
				}
				else
				{
					SZHVC_sub[psbc] = NF | ZF;
				}

				SZHVC_sub[psbc] |= (newval
						& (YF | XF)); /* undocumented flag bits 5+3 */
				if ((newval & 0x0f) >= (oldval & 0x0f))
				{
					SZHVC_sub[psbc] |= HF;
				}
				if (newval >= oldval)
				{
					SZHVC_sub[psbc] |= CF;
				}
				if (((val ^ oldval) & (oldval ^ newval) & 0x80) != 0)
				{
					SZHVC_sub[psbc] |= VF;
				}
				psbc++;
			}
		}
	
		telemetry.registerInfo = new LinkedHashMap<String,Integer>();
		telemetry.flagInfo = new LinkedHashMap<String,Integer>();
		telemetry.pointerInfo = new LinkedHashMap<String,Integer>();
		telemetry.addressInfo = new LinkedHashMap<String,Integer>();
	
	
	}

	/**
	 * Default constructor debugging disabled Use this constructor for use in
	 * applets
	 */
	public Z80(Bus bus)
	{
		super();
		debugLevel = 0;
		
		cpuBus = bus;
		
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	public String getTag()
	{
		return this.tag;
	}

	/**
	 * returns the currently excecuted instruction
	 */
	public final long getInstruction()
	{
		return (long) instruction;
	}

	public final void interrupt(int type, boolean irq)
	{
		if (type == INTERRUPT_TYPE_NMI)
		{
			nmi();
		}
		else if (type == INTERRUPT_TYPE_IRQ)
		{ // type ==IRQ
			irq();
		}
	}

	/**
	 * returns the program counter register (PC)
	 */
	public final int getPC()
	{
		return PC;
	}

	public final void setDebug(boolean debug)
	{
		debugEnabled = debug;
	}

	public final void setProperty(int property, int value)
	{
		if (property == PROPERTY_Z80_IRQ_VECTOR)
		{
			this.I_Vector = value;
		}
	}

	/**
	 * irq request
	 */
	public final void irq()
	{
		this.IRQ = true;
		// if (!debugDisabled)
		// { log("irq Request..."); }
	}

	/**
	 * Non-Maskable irq request
	 */
	public final void nmi()
	{
		this.NMI = true;
		// if (!debugDisabled)
		// { log("Non-Maskable irq Request..."); }
	}

	/**
	 * Reset CPU - Only resets the registers
	 */
	public final void reset()
	{
		startTime  = System.currentTimeMillis();
		
		SP = 0x10000; // it's actually 0 but since the 1st stack instruction is
						// never a POP
						// we can set it to default 0x10000 in order to prevent
						// AND-ing SP
						// with 0xffff all the time...
		PC = 0;
		A = 0;
		F = 0;
		B = 0;
		C = 0;
		D = 0;
		E = 0;
		H = 0;
		L = 0;
		I = 0;
		R = 0;
		IX = 0xffff;
		IY = 0xffff;
		cycle = 0;
		A1 = 0;
		F1 = 0;
		B1 = 0;
		C1 = 0;
		D1 = 0;
		E1 = 0;
		H1 = 0;
		L1 = 0;
		// if (!debugDisabled)
		// { log("***Z80 RESET***"); }

	}

	public final int getCycle()
	{
		return cycleCounter - cycle;

	}

	public void step()
	{
	}

	/**
	 * Execute a number of clock cycles
	 *
	 * @param cycles
	 *            number of cycles to be excecuted
	 */
	public final void exec(int cycles)
	{
		//cycleCounter += (Math.abs(cycles));
		//cycle = cycles;

		//cycle = checkInterrupt(cycle);

		if (DEBUG)
			startSlice = true;

		cycleCounter++;
		
		//while (cycle > 0)
		if(cycle < 0)
		{
			if (((System.currentTimeMillis() - startTime) / 1000) > 0)
			{
				clocksPerSecond = cycleCounter / ((System.currentTimeMillis() - startTime) / 1000);
			}			
			cycle++;			
		}
		else
		{
			step();

			// Cpu in HALT state
			if (state_HALT == true)
			{
				while (state_HALT == true)
				{
					halt();
					cycle = checkInterrupt(cycle);
					if (cycle <= 0)
						return;
					// if (state_HALT == false)
					// PC = (PC + 1 ) & 0xffff;
				}
			}

			instruction = peekb(PC);
			if(instruction < 0)
				instruction+=256;
			PPC = PC;
			PC = (PC + 1) & 0xffff;
			R++;

			//System.out.println("exec instruction:" + instruction);
			switch (instruction)
			{
			case 0x00:
				nop();
				break; // NOP ok
			case 0x01:
				ld_BC_nn();
				break; // LD BC,nn ok
			case 0x02:
				ld_BCi_A();
				break; // LD (BC),A ok
			case 0x03:
				inc_BC();
				break; // INC BC ok
			case 0x04:
				inc_B();
				break; // INC B ok
			case 0x05:
				dec_B();
				break; // DEC B ok
			case 0x06:
				ld_B_n();
				break; // LD B,n ok
			case 0x07:
				rlca();
				break; // RLCA ok
			case 0x08:
				ex_AF_AF();
				break; // EX AF,AF' ok
			case 0x09:
				add_HL_BC();
				break; // ADD HL,BC ok
			case 0x0a:
				ld_A_BCi();
				break; // LD A,(BC) ok
			case 0x0b:
				dec_BC();
				break; // DEC BC ok
			case 0x0C:
				inc_C();
				break; // INC C ok
			case 0x0D:
				dec_C();
				break; // DEC C ok
			case 0x0e:
				ld_C_n();
				break; // LD C,n ok
			case 0x0f:
				rrca();
				break; // RRCA ok
			case 0x10:
				djnz_n();
				break; // DJNZ,n ok
			case 0x11:
				ld_DE_nn();
				break; // LD DE,nn ok
			case 0x12:
				ld_DEi_A();
				break; // LD (DE),A ok
			case 0x13:
				inc_DE();
				break; // INC DE ok
			case 0x14:
				inc_D();
				break; // INC D ok
			case 0x15:
				dec_D();
				break; // DEC D ok
			case 0x16:
				ld_D_n();
				break; // LD D,n ok
			case 0x17:
				rla();
				break; // RLA
			case 0x18:
				jr_e();
				break; // JR e ok
			case 0x19:
				add_HL_DE();
				break; // ADD HL,DE ok
			case 0x1a:
				ld_A_DEi();
				break; // LD A,(DE) ok
			case 0x1b:
				dec_DE();
				break; // DEC DE ok
			case 0x1C:
				inc_E();
				break; // INC E ok
			case 0x1D:
				dec_E();
				break; // DEC E ok
			case 0x1e:
				ld_E_n();
				break; // LD E,n ok
			case 0x1f:
				rra();
				break; // RRA
			case 0x20:
				jr_NZ_e();
				break; // JR NZ,e ok
			case 0x21:
				ld_HL_nn();
				break; // LD HL,nn ok
			case 0x22:
				ld_ni_HL();
				break; // LD (nn),HL ok
			case 0x23:
				inc_HL();
				break; // INC HL ok
			case 0x24:
				inc_H();
				break; // INC H ok
			case 0x25:
				dec_H();
				break; // DEC H ok
			case 0x26:
				ld_H_n();
				break; // LD H,n ok
			case 0x27:
				daa();
				break; // DAA ok
			case 0x28:
				jr_Z_e();
				break; // JR Z,e ok
			case 0x29:
				add_HL_HL();
				break; // ADD HL,HL ok
			case 0x2a:
				ld_HL_ni();
				break; // LD HL,(nn) ok
			case 0x2b:
				dec_HL();
				break; // DEC HL ok
			case 0x2C:
				inc_L();
				break; // INC L ok
			case 0x2D:
				dec_L();
				break; // DEC L ok
			case 0x2e:
				ld_L_n();
				break; // LD L,n ok
			case 0x2f:
				cpl();
				break; // CPL ok
			case 0x30:
				jr_NC_e();
				break; // JR NC,e ok
			case 0x31:
				ld_SP_nn();
				break; // LD SP,nn ok
			case 0x32:
				ld_ni_A();
				break; // LD (nn),A ok
			case 0x33:
				inc_SP();
				break; // INC SP ok
			case 0x34:
				inc_HLi();
				break; // INC (HL) ok
			case 0x35:
				dec_HLi();
				break; // DEC (HL) ok
			case 0x36:
				ld_HLi_n();
				break; // LD (HL),n ok
			case 0x37:
				scf();
				break; // SCF ok
			case 0x38:
				jr_C_e();
				break; // JR C,e ok
			case 0x39:
				add_HL_SP();
				break; // ADD HL,SP ok
			case 0x3a:
				ld_A_ni();
				break; // LD A,(nn) ok
			case 0x3b:
				dec_SP();
				break; // DEC SP ok
			case 0x3C:
				inc_A();
				break; // INC A ok
			case 0x3D:
				dec_A();
				break; // DEC A ok
			case 0x3e:
				ld_A_n();
				break; // LD A,n ok
			case 0x3f:
				ccf();
				break; // CCF ok
			case 0x40:
				nop();
				break; // LD B,B ok
			case 0x41:
				ld_B(C, 4);
				break; // LD B,C ok
			case 0x42:
				ld_B(D, 4);
				break; // LD B,D ok
			case 0x43:
				ld_B(E, 4);
				break; // LD B,E ok
			case 0x44:
				ld_B(H, 4);
				break; // LD B,H ok
			case 0x45:
				ld_B(L, 4);
				break; // LD B,L ok
			case 0x46:
				ld_B(HLi(), 7);
				break; // LD B,(HL) ok
			case 0x47:
				ld_B(A, 4);
				break; // LD B,A ok
			case 0x48:
				ld_C(B, 4);
				break; // LD C,B ok
			case 0x49:
				nop();
				break; // LD C,C ok
			case 0x4a:
				ld_C(D, 4);
				break; // LD C,D ok
			case 0x4b:
				ld_C(E, 4);
				break; // LD C,E ok
			case 0x4c:
				ld_C(H, 4);
				break; // LD C,H ok
			case 0x4d:
				ld_C(L, 4);
				break; // LD C,L ok
			case 0x4e:
				ld_C(HLi(), 7);
				break; // LD C,(HL) ok
			case 0x4f:
				ld_C(A, 4);
				break; // LD C,A ok
			case 0x50:
				ld_D(B, 4);
				break; // LD D,B ok
			case 0x51:
				ld_D(C, 4);
				break; // LD D,C ok
			case 0x52:
				nop();
				break; // LD D,D ok
			case 0x53:
				ld_D(E, 4);
				break; // LD D,E ok
			case 0x54:
				ld_D(H, 4);
				break; // LD D,H ok
			case 0x55:
				ld_D(L, 4);
				break; // LD D,L ok
			case 0x56:
				ld_D(HLi(), 7);
				break; // LD D,(HL) ok
			case 0x57:
				ld_D(A, 4);
				break; // LD D,A ok
			case 0x58:
				ld_E(B, 4);
				break; // LD E,B ok
			case 0x59:
				ld_E(C, 4);
				break; // LD E,C ok
			case 0x5a:
				ld_E(D, 4);
				break; // LD E,D ok
			case 0x5b:
				nop();
				break; // LD E,E ok
			case 0x5c:
				ld_E(H, 4);
				break; // LD E,H ok
			case 0x5d:
				ld_E(L, 4);
				break; // LD E,L ok
			case 0x5e:
				ld_E(HLi(), 7);
				break; // LD E,(HL) ok
			case 0x5f:
				ld_E(A, 4);
				break; // LD E,A ok
			case 0x60:
				ld_H(B, 4);
				break; // LD H,B ok
			case 0x61:
				ld_H(C, 4);
				break; // LD H,C ok
			case 0x62:
				ld_H(D, 4);
				break; // LD H,D ok
			case 0x63:
				ld_H(E, 4);
				break; // LD H,E ok
			case 0x64:
				nop();
				break; // LD H,H ok
			case 0x65:
				ld_H(L, 4);
				break; // LD H,L ok
			case 0x66:
				ld_H(HLi(), 7);
				break; // LD H,(HL) ok
			case 0x67:
				ld_H(A, 4);
				break; // LD H,A ok
			case 0x68:
				ld_L(B, 4);
				break; // LD L,B ok
			case 0x69:
				ld_L(C, 4);
				break; // LD L,C ok
			case 0x6a:
				ld_L(D, 4);
				break; // LD L,D ok
			case 0x6b:
				ld_L(E, 4);
				break; // LD L,E ok
			case 0x6c:
				ld_L(H, 4);
				break; // LD L,H ok
			case 0x6d:
				nop();
				break; // LD L,L ok
			case 0x6e:
				ld_L(HLi(), 7);
				break; // LD L,(HL) ok
			case 0x6f:
				ld_L(A, 4);
				break; // LD L,A ok
			case 0x70:
				ld_HLi(B, 7);
				break; // LD (HL),B ok
			case 0x71:
				ld_HLi(C, 7);
				break; // LD (HL),C ok
			case 0x72:
				ld_HLi(D, 7);
				break; // LD (HL),D ok
			case 0x73:
				ld_HLi(E, 7);
				break; // LD (HL),E ok
			case 0x74:
				ld_HLi(H, 7);
				break; // LD (HL),H ok
			case 0x75:
				ld_HLi(L, 7);
				break; // LD (HL),L ok
			case 0x76:
				halt();
				break; // HALT ok
			case 0x77:
				ld_HLi(A, 7);
				break; // LD (HL),A ok
			case 0x78:
				ld_A(B, 4);
				break; // LD A,B ok
			case 0x79:
				ld_A(C, 4);
				break; // LD A,C ok
			case 0x7a:
				ld_A(D, 4);
				break; // LD A,D ok
			case 0x7b:
				ld_A(E, 4);
				break; // LD A,E ok
			case 0x7c:
				ld_A(H, 4);
				break; // LD A,H ok
			case 0x7d:
				ld_A(L, 4);
				break; // LD A,L ok
			case 0x7e:
				ld_A(HLi(), 7);
				break; // LD A,(HL) ok
			case 0x7f:
				nop();
				break; // LD A,A ok
			case 0x80:
				add_A(B, 4);
				break; // ADD A,B ok
			case 0x81:
				add_A(C, 4);
				break; // ADD A,C ok
			case 0x82:
				add_A(D, 4);
				break; // ADD A,D ok
			case 0x83:
				add_A(E, 4);
				break; // ADD A,E ok
			case 0x84:
				add_A(H, 4);
				break; // ADD A,H ok
			case 0x85:
				add_A(L, 4);
				break; // ADD A,L ok
			case 0x86:
				add_A(HLi(), 7);
				break; // ADD A,(HL) ok
			case 0x87:
				add_A(A, 4);
				break; // ADD A,A ok
			case 0x88:
				adc_A(B, 4);
				break; // ADC A,B ok
			case 0x89:
				adc_A(C, 4);
				break; // ADC A,C ok
			case 0x8a:
				adc_A(D, 4);
				break; // ADC A,D ok
			case 0x8b:
				adc_A(E, 4);
				break; // ADC A,E ok
			case 0x8c:
				adc_A(H, 4);
				break; // ADC A,H ok
			case 0x8d:
				adc_A(L, 4);
				break; // ADC A,L ok
			case 0x8e:
				adc_A(HLi(), 7);
				break; // ADC A,(HL) ok
			case 0x8f:
				adc_A(A, 4);
				break; // ADC A,A ok
			case 0x90:
				sub_A(B, 4);
				break; // SUB A,B ok
			case 0x91:
				sub_A(C, 4);
				break; // SUB A,C ok
			case 0x92:
				sub_A(D, 4);
				break; // SUB A,D ok
			case 0x93:
				sub_A(E, 4);
				break; // SUB A,E ok
			case 0x94:
				sub_A(H, 4);
				break; // SUB A,H ok
			case 0x95:
				sub_A(L, 4);
				break; // SUB A,L ok
			case 0x96:
				sub_A(HLi(), 7);
				break; // SUB A,(HL) ok
			case 0x97:
				sub_A(A, 4);
				break; // SUB A,A ok
			case 0x98:
				sbc_A(B, 4);
				break; // SBC A,B ok
			case 0x99:
				sbc_A(C, 4);
				break; // SBC A,C ok
			case 0x9a:
				sbc_A(D, 4);
				break; // SBC A,D ok
			case 0x9b:
				sbc_A(E, 4);
				break; // SBC A,E ok
			case 0x9c:
				sbc_A(H, 4);
				break; // SBC A,H ok
			case 0x9d:
				sbc_A(L, 4);
				break; // SBC A,L ok
			case 0x9e:
				sbc_A(HLi(), 7);
				break; // SBC A,(HL) ok
			case 0x9f:
				sbc_A(A, 4);
				break; // SBC A,A ok
			case 0xa0:
				and_A(B, 4);
				break; // AND B ok
			case 0xa1:
				and_A(C, 4);
				break; // AND C ok
			case 0xa2:
				and_A(D, 4);
				break; // AND D ok
			case 0xa3:
				and_A(E, 4);
				break; // AND E ok
			case 0xa4:
				and_A(H, 4);
				break; // AND H ok
			case 0xa5:
				and_A(L, 4);
				break; // AND L ok
			case 0xa6:
				and_A(HLi(), 7);
				break; // AND (HL) ok
			case 0xa7:
				and_A(A, 4);
				break; // AND A ok
			case 0xa8:
				xor_A(B, 4);
				break; // XOR B ok
			case 0xa9:
				xor_A(C, 4);
				break; // XOR C ok
			case 0xaa:
				xor_A(D, 4);
				break; // XOR D ok
			case 0xab:
				xor_A(E, 4);
				break; // XOR E ok
			case 0xac:
				xor_A(H, 4);
				break; // XOR H ok
			case 0xad:
				xor_A(L, 4);
				break; // XOR L ok
			case 0xae:
				xor_A(HLi(), 7);
				break; // XOR (HL) ok
			case 0xaf:
				xor_A(A, 4);
				break; // XOR A ok
			case 0xb0:
				or_A(B, 4);
				break; // OR B ok
			case 0xb1:
				or_A(C, 4);
				break; // OR C ok
			case 0xb2:
				or_A(D, 4);
				break; // OR D ok
			case 0xb3:
				or_A(E, 4);
				break; // OR E ok
			case 0xb4:
				or_A(H, 4);
				break; // OR H ok
			case 0xb5:
				or_A(L, 4);
				break; // OR L ok
			case 0xb6:
				or_A(HLi(), 7);
				break; // OR (HL) ok
			case 0xb7:
				or_A(A, 4);
				break; // OR A ok
			case 0xb8:
				cp_A(B, 4);
				break; // CP B ok
			case 0xb9:
				cp_A(C, 4);
				break; // CP C ok
			case 0xba:
				cp_A(D, 4);
				break; // CP D ok
			case 0xbb:
				cp_A(E, 4);
				break; // CP E ok
			case 0xbc:
				cp_A(H, 4);
				break; // CP H ok
			case 0xbd:
				cp_A(L, 4);
				break; // CP L ok
			case 0xbe:
				cp_A(HLi(), 7);
				break; // CP A,(HL)ok
			case 0xbf:
				cp_A(A, 4);
				break; // CP A ok
			case 0xc0:
				ret_NZ();
				break; // RET NZ ok
			case 0xc1:
				pop_BC();
				break; // POP BC ok
			case 0xc2:
				jp_NZ_nn();
				break; // JP NZ,nn ok
			case 0xc3:
				jp_nn();
				break; // JP nn ok
			case 0xc4:
				call_NZ_nn();
				break;// CALL NZ,nn ok
			case 0xc5:
				push_BC();
				break; // PUSH BC ok
			case 0xc6:
				add_A_n();
				break; // ADD A,n ok
			case 0xc7:
				rst(0x00);
				break; // RST $00 ok
			case 0xc8:
				ret_Z();
				break; // RET Z ok
			case 0xc9:
				ret();
				break; // RET ok
			case 0xca:
				jp_Z_nn();
				break; // JP Z,nn ok
			case 0xcb:
				prefix_BC();
				break; // Opcodes with $CB prefix opcodes
			case 0xcc:
				call_Z_nn();
				break; // CALL Z,nn ok
			case 0xcd:
				call_nn();
				break; // CALL nn ok
			case 0xce:
				adc_A_n();
				break; // ADC A,n ok
			case 0xcf:
				rst(0x08);
				break; // RST $08 ok
			case 0xd0:
				ret_NC();
				break; // RET NC ok
			case 0xd1:
				pop_DE();
				break; // POP DE ok
			case 0xd2:
				jp_NC_nn();
				break; // JP NC,nn ok
			case 0xd3:
				out_n_A();
				break; // OUT (n),A ok
			case 0xd4:
				call_NC_nn();
				break; // CALL NC,nn ok
			case 0xd5:
				push_DE();
				break; // PUSH DE ok
			case 0xd6:
				sub_A_n();
				break; // SUB A,n ok
			case 0xd7:
				rst(0x10);
				break; // RST $10 ok
			case 0xd8:
				ret_C();
				break; // RET C ok
			case 0xd9:
				exx();
				break; // EXX ok
			case 0xda:
				jp_C_nn();
				break; // JP C,nn ok
			case 0xdb: // in_A_n(); break; // IN A,(n) ok
				cycle -= 11;
				A = in(peekb(PC++), A);
				break;
			case 0xdc:
				call_C_nn();
				break; // CALL C,nn ok
			case 0xdd:
				IX = execXY(IX);
				break; // Opcodes with $DD prefix
			case 0xde:
				sbc_A_n();
				break; // SBC A,n ok
			case 0xdf:
				rst(0x18);
				break; // RST $18 ok
			case 0xe0:
				ret_PO();
				break; // RET PO ok
			case 0xe1:
				pop_HL();
				break; // POP HL ok
			case 0xe2:
				jp_PO_nn();
				break; // JP PO,nn ok
			case 0xe3:
				ex_SPi_HL();
				break; // EX (SP),HL ok
			case 0xe4:
				call_PO_nn();
				break;// CALL PO,nn ok
			case 0xe5:
				push_HL();
				break; // PUSH HL ok
			case 0xe6:
				and_A_n();
				break; // AND A,n ok
			case 0xe7:
				rst(0x20);
				break; // RST $20 ok
			case 0xe8:
				ret_PE();
				break; // RET PE ok
			case 0xe9:
				jp_HLi();
				break; // JP (HL) ok
			case 0xea:
				jp_PE_nn();
				break; // JP PE,nn ok
			case 0xeb:
				ex_DE_HL();
				break; // EX DE,HL ok
			case 0xec:
				call_PE_nn();
				break;// CALL PE,nn ok
			case 0xed:
				prefix_ED();
				break; // Opcodes with $ED prefix
			case 0xee:
				xor_n();
				break; // XOR A,n ok
			case 0xef:
				rst(0x28);
				break; // RST $28 ok
			case 0xf0:
				ret_P();
				break; // RET P ok
			case 0xf1:
				pop_AF();
				break; // POP AF ok
			case 0xf2:
				jp_P_nn();
				break; // JP P,nn ok
			case 0xf3:
				di();
				break; // DI ok
			case 0xf4:
				call_P_nn();
				break; // CALL P,nn ok
			case 0xf5:
				push_AF();
				break; // PUSH AF ok
			case 0xf6:
				or_n();
				break; // OR A,n ok
			case 0xf7:
				rst(0x30);
				break; // RST $30 ok
			case 0xf8:
				ret_M();
				break; // RET M ok
			case 0xf9:
				ld_SP_HL();
				break; // LD SP,HL ok
			case 0xfa:
				jp_M_nn();
				break; // JP M,nn ok
			case 0xfb:
				ei();
				break; // EI ok
			case 0xfc:
				call_M_nn();
				break; // CALL M,nn ok
			case 0xfd:
				IY = execXY(IY);
				break; // Opcodes with $FD prefix
			case 0xfe:
				cp_n();
				break; // CP A,n ok
			case 0xff:
				rst(0x38);
				break; // RST $38 ok
			}

			if (DEBUG)
				debug(instruction, PPC, A, F, B, C, D, E, H, L, SP, IX, IY, I, cycle);

			/*
			for(int i=cycle;i<0;i++)
			{
				//System.out.println("Instruction cycle:" + i);				
			}
			
			break;
			*/

		
		} // end if (cycle ==0)

	} // end void exec()

	/** $ED prefix opcodes */
	private final void prefix_ED()
	{
		int tmp1, tmp2, tmp3;
		instruction = (instruction << 8) + peekb(PC);
		PC = (PC + 1) & 0xffff;
		switch (instruction & 0xff)
		{
		case 0x40:
			cycle -= 12;
			B = in(C, B);
			break; // IN B,(C) ok
		case 0x41:
			cycle -= 12;
			out(C, B);
			break; // OUT (C),B ok
		case 0x42:
			cycle -= 15;
			sbcHL(BC());
			break; // SBC HL,BC ok
		case 0x43:
			cycle -= 20;
			pokew(peekw(PC), BC());
			PC += 2;
			break;// LD (nn),BC
		case 0x44:
			cycle -= 13;
			tmp1 = A;
			A = 0;
			A = subA_8(tmp1, A);
			break; // NEG
		case 0x45:
			cycle -= 14;
			PC = peekw(SP);
			SP += 2;
			IFF0 = IFF1; // RET N
			cycle = checkInterrupt(cycle);
			break;
		case 0x46:
			cycle -= 8;
			IM = 0;
			break; // IM 0
		case 0x47:
			cycle -= 9;
			I = A;
			break; // LD I,A

		case 0x48:
			cycle -= 12;
			C = in(C, B);
			break; // IN C,(C) ok
		case 0x49:
			cycle -= 12;
			out(C, D);
			break; // OUT (C),D ok
		case 0x4a:
			cycle -= 15;
			adcHL(BC());
			break; // ADC HL,BC
		case 0x4b:
			cycle -= 20;
			B = peekb(peekw(PC) + 1);
			C = peekb(peekw(PC));
			PC += 2;
			break; // LD BC,(nn)
		case 0x4c:
			cycle -= 13;
			tmp1 = A;
			A = 0;
			A = subA_8(tmp1, A);
			break; // NEG
		case 0x4d:
			cycle -= 14;
			PC = peekw(SP);
			SP += 2;
			break; // RETI
		case 0x4e:
			cycle -= 8;
			IM = 0;
			break; // IM 0
		case 0x4f:
			cycle -= 9;
			R = A;
			break; // LD R,A

		case 0x50:
			cycle -= 12;
			D = in(C, B);
			break; // IN D,(C) ok
		case 0x51:
			cycle -= 12;
			out(C, D);
			break; // OUT (C),E
		case 0x52:
			cycle -= 15;
			sbcHL(DE());
			break; // SBC HL,DE ok
		case 0x53:
			cycle -= 20;
			pokew(peekw(PC), DE());
			PC += 2;
			break;// LD (nn),DE
		case 0x54:
			cycle -= 13;
			tmp1 = A;
			A = 0;
			A = subA_8(tmp1, A);
			break; // NEG
		case 0x55:
			cycle -= 14;
			PC = peekw(SP);
			SP += 2;
			IFF0 = IFF1; // RET N
			cycle = checkInterrupt(cycle);
			break;
		case 0x56:
			cycle -= 8; // System.out.println("IM 1");
			IM = 1;
			break; // IM 1
		case 0x57:
			cycle -= 9;
			A = I;
			break; // LD A,I

		case 0x58:
			cycle -= 12;
			E = in(C, B);
			break; // IN E,(C) ok
		case 0x59:
			cycle -= 12;
			out(C, E);
			break; // OUT (C),E
		case 0x5a:
			cycle -= 15;
			adcHL(DE());
			break; // ADC HL,DE
		case 0x5B:
			cycle -= 20;
			D = peekb(peekw(PC) + 1);
			E = peekb(peekw(PC));
			PC += 2;
			break; // LD DE,(nn)
		case 0x5c:
			cycle -= 13;
			tmp1 = A;
			A = 0;
			A = subA_8(tmp1, A);
			break; // NEG
		case 0x5d:
			cycle -= 14;
			PC = peekw(SP);
			SP += 2;
			break; // RETI
		case 0x5e:
			cycle -= 8;
			IM = 2;
			break; // IM 2
		case 0x5f:
			cycle -= 9;
			A = R();
			break; // LD A,R

		case 0x60:
			cycle -= 12;
			H = in(C, B);
			break; // IN H,(C) ok
		case 0x61:
			cycle -= 12;
			out(C, H);
			break; // OUT (C),H
		case 0x62:
			cycle -= 15;
			sbcHL(HL());
			break; // SBC HL,HL ok
		case 0x63:
			cycle -= 20;
			pokew(peekw(PC), HL());
			PC += 2;
			break; // LD (nn),HL
		case 0x64:
			cycle -= 13;
			tmp1 = A;
			A = 0;
			A = subA_8(tmp1, A);
			break; // NEG
		case 0x65:
			cycle -= 14;
			PC = peekw(SP);
			SP += 2;
			IFF0 = IFF1; // RET N
			cycle = checkInterrupt(cycle);
			break;
		case 0x66:
			cycle -= 8;
			IM = 0;
			break; // IM 0
		case 0x67:
			cycle -= 18;
			A = rrd_A(A, H, L);
			break; // RRD

		case 0x68:
			cycle -= 12;
			L = in(C, B);
			break; // IN L,(C) ok
		case 0x69:
			cycle -= 12;
			out(C, L);
			break; // OUT (C),L
		case 0x6a:
			cycle -= 15;
			adcHL(HL());
			break; // ADC HL,HL
		case 0x6B:
			cycle -= 20;
			H = peekb(peekw(PC) + 1);
			L = peekb(peekw(PC));
			PC += 2;
			break; // LD HL,(nn)
		case 0x6c:
			cycle -= 13;
			tmp1 = A;
			A = 0;
			A = subA_8(tmp1, A);
			break; // NEG
		case 0x6d:
			cycle -= 14;
			PC = peekw(SP);
			SP += 2;
			break; // RETI
		case 0x6e:
			cycle -= 8;
			IM = 0;
			break; // IM 0
		case 0x6f:
			cycle -= 18;
			A = rld_A(A, H, L);
			break; // RLD

		case 0x72:
			cycle -= 15;
			sbcHL(SP);
			break; // SBC HL,SP ok
		case 0x73:
			cycle -= 20;
			pokew(peekw(PC), SP);
			PC += 2;
			break; // LD (nn),SP

		case 0x78:
			cycle -= 12;
			A = in(C, B);
			break; // IN A,(C)
		case 0x79:
			cycle -= 11;
			out(C, A, B);
			break; // OUT (C),A
		case 0x7a:
			cycle -= 15;
			adcHL(SP);
			break; // ADC HL,SP
		case 0x7B:
			cycle -= 20;
			SP = peekw(peekw(PC));
			PC += 2;
			break; // LD SP,(nn)

		case 0xa0:
			cycle -= 21; // LDI
			tmp1 = DE();
			tmp2 = HL();
			tmp3 = BC();
			pokeb(tmp1, peekb(tmp2++));
			tmp1 = (tmp1 + 1) & 0xffff;
			tmp3 = (tmp3 - 1) & 0xffff;
			E = tmp1 & 0xff;
			D = tmp1 >> 8;
			L = tmp2 & 0xff;
			H = tmp2 >> 8;
			C = tmp3 & 0xff;
			B = tmp3 >> 8;
			F &= 0x80 | 0x40 | 0x01;
			if (tmp3 != 0)
			{
				F |= 0x04;
			}
			break;
		case 0xa1:
			cycle -= 21; // CPI
			tmp1 = HL();
			tmp2 = BC();
			int value = peekb(tmp1);
			int result = (A - value) & 0xff;
			tmp1++;
			tmp2++;
			F = (F & 1) | (SZ[result] & ~(0x28)) | ((A ^ value ^ result) & 0x10) | 0x02;
			if ((F & 0x10) != 0)
			{
				result = (result - 1) & 0xff;
			}
			if ((result & 0x02) != 0)
			{
				F |= 0x20;
			}
			if ((result & 0x08) != 0)
			{
				F |= 0x08;
			}
			if (tmp2 != 0)
			{
				F |= 0x04;
			}
			H = tmp1 >> 8;
			L = tmp1 & 0xff;
			B = tmp2 >> 8;
			C = tmp2 & 0xff;
			break;
		case 0xa2:
			cycle -= 16; // INI
			tmp1 = HL();
			B = (B - 1) & 0xff;
			pokeb(tmp1++, in(B, A));
			tmp1 &= 0xffff;
			H = tmp1 >> 8;
			L = tmp1 & 0xff;
			if (B == 0)
			{
				F |= 0x40;
			}
			else
			{
				F &= ~0x40;
			}
			F |= 2;
			break;
		case 0xa3:
			cycle -= 16; // OUTI
			tmp1 = HL();
			out(B, peekb(tmp1++));
			B = (B - 1) & 0xff;
			H = tmp1 >> 8;
			L = tmp1 & 0xff;
			if (B == 0)
			{
				F |= 0x40;
			}
			else
			{
				F &= ~0x40;
			}
			F |= 2;
			break;
		case 0xa8:
			cycle -= 21; // LDD
			tmp1 = DE();
			tmp2 = HL();
			tmp3 = BC();
			pokeb(tmp1, peekb(tmp2--));
			tmp1 = (tmp1 - 1) & 0xffff;
			tmp3 = (tmp3 - 1) & 0xffff;
			E = tmp1 & 0xff;
			D = tmp1 >> 8;
			L = tmp2 & 0xff;
			H = tmp2 >> 8;
			C = tmp3 & 0xff;
			B = tmp3 >> 8;
			F &= 0x80 | 0x40 | 0x01;
			if (tmp3 != 0)
			{
				F |= 0x04;
			}
			break;
		case 0xaa:
			cycle -= 16; // IND
			tmp1 = HL();
			B = (B - 1) & 0xff;
			pokeb(tmp1--, in(B, A));
			tmp1 &= 0xffff;
			H = tmp1 >> 8;
			L = tmp1 & 0xff;
			if (B == 0)
			{
				F |= 0x40;
			}
			else
			{
				F &= ~0x40;
			}
			F |= 2;
			break;
		case 0xb0:
			cycle = cycle - (5 + 16 * (BC()));
			tmp1 = DE();
			tmp2 = HL(); // LDIR - ok
			for (int n = (BC()); n > 0; n--)
			{
				pokeb(tmp1, peekb(tmp2));
				tmp1 = (tmp1 + 1) & 0xffff;
				tmp2 = (tmp2 + 1) & 0xffff;
			}
			E = tmp1 & 0xff;
			D = tmp1 >> 8;
			B = C = 0;
			L = tmp2 & 0xff;
			H = tmp2 >> 8;
			F &= 0xe9;
			break;
		case 0xb1:
			tmp2 = F & 1;
			tmp1 = BC();
			tmp3 = HL(); // CPIR - ok
			cycle -= 4;
			cpA_8(peekb(tmp3), A);
			tmp3++;
			tmp1--;
			H = tmp3 >> 8;
			L = tmp3 & 0xff;
			B = tmp1 >> 8;
			C = tmp1 & 0xff;
			F = (F & 0xfe) | tmp2;
			if ((tmp1 != 0) && (F & 0x40) == 0)
			{
				PC -= 2;
				F |= 4;
				cycle -= 21;
			}
			else
			{
				F &= 0xfb;
				cycle -= 16;
			}
			break;
		case 0xb3:
			cycle = cycle - (5 + 16 * B); // OTIR
			while (B > 0)
			{
				tmp1 = HL();
				out(B, peekb(tmp1++));
				B = (B - 1) & 0xff;
				H = tmp1 >> 8;
				L = tmp1 & 0xff;
				if (B == 0)
				{
					F |= 0x40;
				}
				else
				{
					F &= ~0x40;
				}
			}
			F |= 2;
			break;

		case 0xb8:
			cycle = cycle - (5 + 16 * (BC()));
			tmp1 = DE();
			tmp2 = HL(); // LDDR - ok
			for (int n = (BC()); n > 0; n--)
			{
				pokeb(tmp1--, peekb(tmp2--));
			}
			E = tmp1 & 0xff;
			D = tmp1 >> 8;
			B = C = 0;
			L = tmp2 & 0xff;
			H = tmp2 >> 8;
			F &= 0xe9;
			break;

		case 0xb9:
			tmp2 = F & 1;
			tmp1 = BC();
			tmp3 = HL(); // CPIR - ok
			cycle -= 4;
			cpA_8(peekb(tmp3), A);
			tmp3--;
			tmp1--;
			H = tmp3 >> 8;
			L = tmp3 & 0xff;
			B = tmp1 >> 8;
			C = tmp1 & 0xff;
			F = (F & 0xfe) | tmp2;
			if ((tmp1 != 0) && (F & 0x40) == 0)
			{
				PC -= 2;
				F |= 4;
				cycle -= 21;
			}
			else
			{
				F &= 0xfb;
				cycle -= 16;
			}
			break;

		default:
			error(instruction, PPC);
			break; // Not implemented
		}
	}

	/** $CB prefix opcodes */
	private final void prefix_BC()
	{
		instruction = (instruction << 8) + peekb(PC++);
		switch (instruction & 0xff)
		{
		case 0x00:
			cycle -= 8;
			B = rlc(B);
			break;
		case 0x01:
			cycle -= 8;
			C = rlc(C);
			break;
		case 0x02:
			cycle -= 8;
			D = rlc(D);
			break;
		case 0x03:
			cycle -= 8;
			E = rlc(E);
			break;
		case 0x04:
			cycle -= 8;
			H = rlc(H);
			break;
		case 0x05:
			cycle -= 8;
			L = rlc(L);
			break;
		case 0x06:
			cycle -= 15;
			pokeb(HL(), rlc(HLi()));
			break;
		case 0x07:
			cycle -= 8;
			A = rlc(A);
			break;

		case 0x08:
			cycle -= 8;
			B = rrc(B);
			break;
		case 0x09:
			cycle -= 8;
			C = rrc(C);
			break;
		case 0x0a:
			cycle -= 8;
			D = rrc(D);
			break;
		case 0x0b:
			cycle -= 8;
			E = rrc(E);
			break;
		case 0x0c:
			cycle -= 8;
			H = rrc(H);
			break;
		case 0x0d:
			cycle -= 8;
			L = rrc(L);
			break;
		case 0x0e:
			cycle -= 15;
			pokeb(HL(), rrc(HLi()));
			break;
		case 0x0f:
			cycle -= 8;
			A = rrc(A);
			break;

		case 0x10:
			cycle -= 8;
			B = rl(B);
			break;
		case 0x11:
			cycle -= 8;
			C = rl(C);
			break;
		case 0x12:
			cycle -= 8;
			D = rl(D);
			break;
		case 0x13:
			cycle -= 8;
			E = rl(E);
			break;
		case 0x14:
			cycle -= 8;
			H = rl(H);
			break;
		case 0x15:
			cycle -= 8;
			L = rl(L);
			break;
		case 0x16:
			cycle -= 15;
			pokeb((HL()), rl(HLi()));
			break;
		case 0x17:
			cycle -= 8;
			A = rl(A);
			break;

		case 0x18:
			cycle -= 8;
			B = rr(B);
			break;
		case 0x19:
			cycle -= 8;
			C = rr(C);
			break;
		case 0x1A:
			cycle -= 8;
			D = rr(D);
			break;
		case 0x1B:
			cycle -= 8;
			E = rr(E);
			break;
		case 0x1C:
			cycle -= 8;
			H = rr(H);
			break;
		case 0x1D:
			cycle -= 8;
			L = rr(L);
			break;
		case 0x1E:
			cycle -= 15;
			pokeb((HL()), rr(HLi()));
			break;
		case 0x1F:
			cycle -= 8;
			A = rr(A);
			break;

		case 0x20:
			cycle -= 8;
			B = sla(B);
			break;
		case 0x21:
			cycle -= 8;
			C = sla(C);
			break;
		case 0x22:
			cycle -= 8;
			D = sla(D);
			break;
		case 0x23:
			cycle -= 8;
			E = sla(E);
			break;
		case 0x24:
			cycle -= 8;
			H = sla(H);
			break;
		case 0x25:
			cycle -= 8;
			L = sla(L);
			break;
		case 0x26:
			cycle -= 12;
			pokeb((HL()), sla(HLi()));
			break;
		case 0x27:
			cycle -= 8;
			A = sla(A);
			break;

		case 0x28:
			cycle -= 8;
			B = sra(B);
			break;
		case 0x29:
			cycle -= 8;
			C = sra(C);
			break;
		case 0x2a:
			cycle -= 8;
			D = sra(D);
			break;
		case 0x2b:
			cycle -= 8;
			E = sra(E);
			break;
		case 0x2c:
			cycle -= 8;
			H = sra(H);
			break;
		case 0x2d:
			cycle -= 8;
			L = sra(L);
			break;
		case 0x2e:
			cycle -= 12;
			pokeb((HL()), sra(HLi()));
			break;
		case 0x2f:
			cycle -= 8;
			A = sra(A);
			break;

		case 0x30:
			cycle -= 8;
			B = sll(B);
			break;
		case 0x31:
			cycle -= 8;
			C = sll(C);
			break;
		case 0x32:
			cycle -= 8;
			D = sll(D);
			break;
		case 0x33:
			cycle -= 8;
			E = sll(E);
			break;
		case 0x34:
			cycle -= 8;
			H = sll(H);
			break;
		case 0x35:
			cycle -= 8;
			L = sll(L);
			break;
		case 0x36:
			cycle -= 12;
			pokeb((HL()), sll(HLi()));
			break;
		case 0x37:
			cycle -= 8;
			A = sll(A);
			break;

		case 0x38:
			cycle -= 8;
			B = srl(B);
			break;
		case 0x39:
			cycle -= 8;
			C = srl(C);
			break;
		case 0x3a:
			cycle -= 8;
			D = srl(D);
			break;
		case 0x3b:
			cycle -= 8;
			E = srl(E);
			break;
		case 0x3c:
			cycle -= 8;
			H = srl(H);
			break;
		case 0x3d:
			cycle -= 8;
			L = srl(L);
			break;
		case 0x3e:
			cycle -= 12;
			pokeb((HL()), srl(HLi()));
			break;
		case 0x3f:
			cycle -= 8;
			A = srl(A);
			break;

		case 0x40:
			cycle -= 8;
			bit(0, B);
			break;
		case 0x41:
			cycle -= 8;
			bit(0, C);
			break;
		case 0x42:
			cycle -= 8;
			bit(0, D);
			break;
		case 0x43:
			cycle -= 8;
			bit(0, E);
			break;
		case 0x44:
			cycle -= 8;
			bit(0, H);
			break;
		case 0x45:
			cycle -= 8;
			bit(0, L);
			break;
		case 0x46:
			cycle -= 12;
			bit(0, HLi());
			break;
		case 0x47:
			cycle -= 8;
			bit(0, A);
			break;

		case 0x48:
			cycle -= 8;
			bit(1, B);
			break;
		case 0x49:
			cycle -= 8;
			bit(1, C);
			break;
		case 0x4a:
			cycle -= 8;
			bit(1, D);
			break;
		case 0x4b:
			cycle -= 8;
			bit(1, E);
			break;
		case 0x4c:
			cycle -= 8;
			bit(1, H);
			break;
		case 0x4d:
			cycle -= 8;
			bit(1, L);
			break;
		case 0x4e:
			cycle -= 12;
			bit(1, HLi());
			break;
		case 0x4f:
			cycle -= 8;
			bit(1, A);
			break;

		case 0x50:
			cycle -= 8;
			bit(2, B);
			break;
		case 0x51:
			cycle -= 8;
			bit(2, C);
			break;
		case 0x52:
			cycle -= 8;
			bit(2, D);
			break;
		case 0x53:
			cycle -= 8;
			bit(2, E);
			break;
		case 0x54:
			cycle -= 8;
			bit(2, H);
			break;
		case 0x55:
			cycle -= 8;
			bit(2, L);
			break;
		case 0x56:
			cycle -= 12;
			bit(2, HLi());
			break;
		case 0x57:
			cycle -= 8;
			bit(2, A);
			break;

		case 0x58:
			cycle -= 8;
			bit(3, B);
			break;
		case 0x59:
			cycle -= 8;
			bit(3, C);
			break;
		case 0x5a:
			cycle -= 8;
			bit(3, D);
			break;
		case 0x5b:
			cycle -= 8;
			bit(3, E);
			break;
		case 0x5c:
			cycle -= 8;
			bit(3, H);
			break;
		case 0x5d:
			cycle -= 8;
			bit(3, L);
			break;
		case 0x5e:
			cycle -= 12;
			bit(3, HLi());
			break;
		case 0x5f:
			cycle -= 8;
			bit(3, A);
			break;

		case 0x60:
			cycle -= 8;
			bit(4, B);
			break;
		case 0x61:
			cycle -= 8;
			bit(4, C);
			break;
		case 0x62:
			cycle -= 8;
			bit(4, D);
			break;
		case 0x63:
			cycle -= 8;
			bit(4, E);
			break;
		case 0x64:
			cycle -= 8;
			bit(4, H);
			break;
		case 0x65:
			cycle -= 8;
			bit(4, L);
			break;
		case 0x66:
			cycle -= 12;
			bit(4, HLi());
			break;
		case 0x67:
			cycle -= 8;
			bit(4, A);
			break;

		case 0x68:
			cycle -= 8;
			bit(5, B);
			break;
		case 0x69:
			cycle -= 8;
			bit(5, C);
			break;
		case 0x6a:
			cycle -= 8;
			bit(5, D);
			break;
		case 0x6b:
			cycle -= 8;
			bit(5, E);
			break;
		case 0x6c:
			cycle -= 8;
			bit(5, H);
			break;
		case 0x6d:
			cycle -= 8;
			bit(5, L);
			break;
		case 0x6e:
			cycle -= 12;
			bit(5, HLi());
			break;
		case 0x6f:
			cycle -= 8;
			bit(5, A);
			break;

		case 0x70:
			cycle -= 8;
			bit(6, B);
			break;
		case 0x71:
			cycle -= 8;
			bit(6, C);
			break;
		case 0x72:
			cycle -= 8;
			bit(6, D);
			break;
		case 0x73:
			cycle -= 8;
			bit(6, E);
			break;
		case 0x74:
			cycle -= 8;
			bit(6, H);
			break;
		case 0x75:
			cycle -= 8;
			bit(6, L);
			break;
		case 0x76:
			cycle -= 12;
			bit(6, HLi());
			break;
		case 0x77:
			cycle -= 8;
			bit(6, A);
			break;

		case 0x78:
			cycle -= 8;
			bit(7, B);
			break;
		case 0x79:
			cycle -= 8;
			bit(7, C);
			break;
		case 0x7a:
			cycle -= 8;
			bit(7, D);
			break;
		case 0x7b:
			cycle -= 8;
			bit(7, E);
			break;
		case 0x7c:
			cycle -= 8;
			bit(7, H);
			break;
		case 0x7d:
			cycle -= 8;
			bit(7, L);
			break;
		case 0x7e:
			cycle -= 12;
			bit(7, HLi());
			break;
		case 0x7f:
			cycle -= 8;
			bit(7, A);
			break;

		case 0x80:
			cycle -= 8;
			B = res(0, B);
			break;
		case 0x81:
			cycle -= 8;
			C = res(0, C);
			break;
		case 0x82:
			cycle -= 8;
			D = res(0, D);
			break;
		case 0x83:
			cycle -= 8;
			E = res(0, E);
			break;
		case 0x84:
			cycle -= 8;
			H = res(0, H);
			break;
		case 0x85:
			cycle -= 8;
			L = res(0, L);
			break;
		case 0x86:
			cycle -= 12;
			pokeb(HL(), res(0, HLi()));
			break;
		case 0x87:
			cycle -= 8;
			A = res(0, A);
			break;

		case 0x88:
			cycle -= 8;
			B = res(1, B);
			break;
		case 0x89:
			cycle -= 8;
			C = res(1, C);
			break;
		case 0x8a:
			cycle -= 8;
			D = res(1, D);
			break;
		case 0x8b:
			cycle -= 8;
			E = res(1, E);
			break;
		case 0x8c:
			cycle -= 8;
			H = res(1, H);
			break;
		case 0x8d:
			cycle -= 8;
			L = res(1, L);
			break;
		case 0x8e:
			cycle -= 12;
			pokeb(HL(), res(1, HLi()));
			break;
		case 0x8f:
			cycle -= 8;
			A = res(1, A);
			break;

		case 0x90:
			cycle -= 8;
			B = res(2, B);
			break;
		case 0x91:
			cycle -= 8;
			C = res(2, C);
			break;
		case 0x92:
			cycle -= 8;
			D = res(2, D);
			break;
		case 0x93:
			cycle -= 8;
			E = res(2, E);
			break;
		case 0x94:
			cycle -= 8;
			H = res(2, H);
			break;
		case 0x95:
			cycle -= 8;
			L = res(2, L);
			break;
		case 0x96:
			cycle -= 12;
			pokeb(HL(), res(2, HLi()));
			break;
		case 0x97:
			cycle -= 8;
			A = res(2, A);
			break;

		case 0x98:
			cycle -= 8;
			B = res(3, B);
			break;
		case 0x99:
			cycle -= 8;
			C = res(3, C);
			break;
		case 0x9a:
			cycle -= 8;
			D = res(3, D);
			break;
		case 0x9b:
			cycle -= 8;
			E = res(3, E);
			break;
		case 0x9c:
			cycle -= 8;
			H = res(3, H);
			break;
		case 0x9d:
			cycle -= 8;
			L = res(3, L);
			break;
		case 0x9e:
			cycle -= 12;
			pokeb(HL(), res(3, HLi()));
			break;
		case 0x9f:
			cycle -= 8;
			A = res(3, A);
			break;

		case 0xa0:
			cycle -= 8;
			B = res(4, B);
			break;
		case 0xa1:
			cycle -= 8;
			C = res(4, C);
			break;
		case 0xa2:
			cycle -= 8;
			D = res(4, D);
			break;
		case 0xa3:
			cycle -= 8;
			E = res(4, E);
			break;
		case 0xa4:
			cycle -= 8;
			H = res(4, H);
			break;
		case 0xa5:
			cycle -= 8;
			L = res(4, L);
			break;
		case 0xa6:
			cycle -= 12;
			pokeb(HL(), res(4, HLi()));
			break;
		case 0xa7:
			cycle -= 8;
			A = res(4, A);
			break;

		case 0xa8:
			cycle -= 8;
			B = res(5, B);
			break;
		case 0xa9:
			cycle -= 8;
			C = res(5, C);
			break;
		case 0xaa:
			cycle -= 8;
			D = res(5, D);
			break;
		case 0xab:
			cycle -= 8;
			E = res(5, E);
			break;
		case 0xac:
			cycle -= 8;
			H = res(5, H);
			break;
		case 0xad:
			cycle -= 8;
			L = res(5, L);
			break;
		case 0xae:
			cycle -= 12;
			pokeb(HL(), res(5, HLi()));
			break;
		case 0xaf:
			cycle -= 8;
			A = res(5, A);
			break;

		case 0xb0:
			cycle -= 8;
			B = res(6, B);
			break;
		case 0xb1:
			cycle -= 8;
			C = res(6, C);
			break;
		case 0xb2:
			cycle -= 8;
			D = res(6, D);
			break;
		case 0xb3:
			cycle -= 8;
			E = res(6, E);
			break;
		case 0xb4:
			cycle -= 8;
			H = res(6, H);
			break;
		case 0xb5:
			cycle -= 8;
			L = res(6, L);
			break;
		case 0xb6:
			cycle -= 12;
			pokeb(HL(), res(6, HLi()));
			break;
		case 0xb7:
			cycle -= 8;
			A = res(6, A);
			break;

		case 0xb8:
			cycle -= 8;
			B = res(7, B);
			break;
		case 0xb9:
			cycle -= 8;
			C = res(7, C);
			break;
		case 0xba:
			cycle -= 8;
			D = res(7, D);
			break;
		case 0xbb:
			cycle -= 8;
			E = res(7, E);
			break;
		case 0xbc:
			cycle -= 8;
			H = res(7, H);
			break;
		case 0xbd:
			cycle -= 8;
			L = res(7, L);
			break;
		case 0xbe:
			cycle -= 12;
			pokeb(HL(), res(7, HLi()));
			break;
		case 0xbf:
			cycle -= 8;
			A = res(7, A);
			break;

		case 0xc0:
			cycle -= 8;
			B = set(0, B);
			break;
		case 0xc1:
			cycle -= 8;
			C = set(0, C);
			break;
		case 0xc2:
			cycle -= 8;
			D = set(0, D);
			break;
		case 0xc3:
			cycle -= 8;
			E = set(0, E);
			break;
		case 0xc4:
			cycle -= 8;
			H = set(0, H);
			break;
		case 0xc5:
			cycle -= 8;
			L = set(0, L);
			break;
		case 0xc6:
			cycle -= 12;
			pokeb(HL(), set(0, HLi()));
			break;
		case 0xc7:
			cycle -= 8;
			A = set(0, A);
			break;

		case 0xc8:
			cycle -= 8;
			B = set(1, B);
			break;
		case 0xc9:
			cycle -= 8;
			C = set(1, C);
			break;
		case 0xca:
			cycle -= 8;
			D = set(1, D);
			break;
		case 0xcb:
			cycle -= 8;
			E = set(1, E);
			break;
		case 0xcc:
			cycle -= 8;
			H = set(1, H);
			break;
		case 0xcd:
			cycle -= 8;
			L = set(1, L);
			break;
		case 0xce:
			cycle -= 12;
			pokeb(HL(), set(1, HLi()));
			break;
		case 0xcf:
			cycle -= 8;
			A = set(1, A);
			break;

		case 0xd0:
			cycle -= 8;
			B = set(2, B);
			break;
		case 0xd1:
			cycle -= 8;
			C = set(2, C);
			break;
		case 0xd2:
			cycle -= 8;
			D = set(2, D);
			break;
		case 0xd3:
			cycle -= 8;
			E = set(2, E);
			break;
		case 0xd4:
			cycle -= 8;
			H = set(2, H);
			break;
		case 0xd5:
			cycle -= 8;
			L = set(2, L);
			break;
		case 0xd6:
			cycle -= 12;
			pokeb(HL(), set(2, HLi()));
			break;
		case 0xd7:
			cycle -= 8;
			A = set(2, A);
			break;

		case 0xd8:
			cycle -= 8;
			B = set(3, B);
			break;
		case 0xd9:
			cycle -= 8;
			C = set(3, C);
			break;
		case 0xda:
			cycle -= 8;
			D = set(3, D);
			break;
		case 0xdb:
			cycle -= 8;
			E = set(3, E);
			break;
		case 0xdc:
			cycle -= 8;
			H = set(3, H);
			break;
		case 0xdd:
			cycle -= 8;
			L = set(3, L);
			break;
		case 0xde:
			cycle -= 12;
			pokeb(HL(), set(3, HLi()));
			break;
		case 0xdf:
			cycle -= 8;
			A = set(3, A);
			break;

		case 0xe0:
			cycle -= 8;
			B = set(4, B);
			break;
		case 0xe1:
			cycle -= 8;
			C = set(4, C);
			break;
		case 0xe2:
			cycle -= 8;
			D = set(4, D);
			break;
		case 0xe3:
			cycle -= 8;
			E = set(4, E);
			break;
		case 0xe4:
			cycle -= 8;
			H = set(4, H);
			break;
		case 0xe5:
			cycle -= 8;
			L = set(4, L);
			break;
		case 0xe6:
			cycle -= 12;
			pokeb(HL(), set(4, HLi()));
			break;
		case 0xe7:
			cycle -= 8;
			A = set(4, A);
			break;

		case 0xe8:
			cycle -= 8;
			B = set(5, B);
			break;
		case 0xe9:
			cycle -= 8;
			C = set(5, C);
			break;
		case 0xea:
			cycle -= 8;
			D = set(5, D);
			break;
		case 0xeb:
			cycle -= 8;
			E = set(5, E);
			break;
		case 0xec:
			cycle -= 8;
			H = set(5, H);
			break;
		case 0xed:
			cycle -= 8;
			L = set(5, L);
			break;
		case 0xee:
			cycle -= 12;
			pokeb(HL(), set(5, HLi()));
			break;
		case 0xef:
			cycle -= 8;
			A = set(5, A);
			break;

		case 0xf0:
			cycle -= 8;
			B = set(6, B);
			break;
		case 0xf1:
			cycle -= 8;
			C = set(6, C);
			break;
		case 0xf2:
			cycle -= 8;
			D = set(6, D);
			break;
		case 0xf3:
			cycle -= 8;
			E = set(6, E);
			break;
		case 0xf4:
			cycle -= 8;
			H = set(6, H);
			break;
		case 0xf5:
			cycle -= 8;
			L = set(6, L);
			break;
		case 0xf6:
			cycle -= 12;
			pokeb(HL(), set(6, HLi()));
			break;
		case 0xf7:
			cycle -= 8;
			A = set(6, A);
			break;

		case 0xf8:
			cycle -= 8;
			B = set(7, B);
			break;
		case 0xf9:
			cycle -= 8;
			C = set(7, C);
			break;
		case 0xfa:
			cycle -= 8;
			D = set(7, D);
			break;
		case 0xfb:
			cycle -= 8;
			E = set(7, E);
			break;
		case 0xfc:
			cycle -= 8;
			H = set(7, H);
			break;
		case 0xfd:
			cycle -= 8;
			L = set(7, L);
			break;
		case 0xfe:
			cycle -= 12;
			pokeb(HL(), set(7, HLi()));
			break;
		case 0xff:
			cycle -= 8;
			A = set(7, A);
			break;
		}
	}

	/** $DD and $FD prefix opcodes (index instructions) */
	private final int execXY(int XY)
	{
		int tmp1, tmp2, tmp3;
		instruction = (instruction << 8) + peekb(PC++);
		IXYd = (XY + sign(peekb(PC))) & 0xffff;
		switch (instruction & 0xff)
		{
		case 0x00:
		case 0x01:
		case 0x02:
		case 0x03:
		case 0x04:
		case 0x05:
		case 0x06:
		case 0x07:
		case 0x08:
			error(instruction, PPC);
			break; // Not implemented
		case 0x09:
			cycle -= 15;
			XY = add16(XY, BC());
			break; // ADD XY,BC ok
		case 0x0a:
		case 0x0b:
		case 0x0c:
		case 0x0d:
		case 0x0e:
		case 0x0f:
		case 0x10:
		case 0x11:
		case 0x12:
		case 0x13:
		case 0x14:
		case 0x15:
		case 0x16:
		case 0x17:
		case 0x18:
			error(instruction, PPC);
			break; // Not implemented
		case 0x19:
			cycle -= 15;
			XY = add16(XY, DE());
			break; // ADD XY,DE ok
		case 0x1a:
		case 0x1b:
		case 0x1c:
		case 0x1d:
		case 0x1e:
		case 0x1f:
		case 0x20:
			error(instruction, PPC);
			break; // Not implemented
		case 0x21:
			cycle -= 14;
			XY = peekw(PC);
			PC += 2;
			break; // LD XY,nn ok
		case 0x22:
			cycle -= 20;
			pokew(peekw(PC), XY);
			PC += 2;
			break; // LD (nn),XY ok
		case 0x23:
			cycle -= 10;
			XY = (XY + 1) & 0xffff;
			break; // INC XY ok
		case 0x24:
		case 0x25:
			error(instruction, PPC);
			break; // Not implemented
		case 0x26:
			cycle -= 11;
			XY = (XY & 0x00ff) | (peekb(PC++) << 8);
			break; // LD HXY,n ok
		case 0x27:
		case 0x28:
			error(instruction, PPC);
			break; // Not implemented
		case 0x29:
			cycle -= 15;
			XY = add16(XY, XY);
			break; // ADD XY,XY ok
		case 0x2a:
			cycle -= 20;
			XY = peekw(peekw(PC));
			PC += 2;
			break; // LD XY,(nn) ok
		case 0x2b:
			cycle -= 10;
			XY = (XY - 1) & 0xffff;
			break; // DEC XY ok
		case 0x2c:
			cycle -= 15;
			XY = incL16(XY);
			break;
		case 0x2d:
			cycle -= 15;
			XY = decL16(XY);
			break;
		case 0x2e:
			cycle -= 11;
			XY = (XY & 0xff00) | peekb(PC++);
			break;
		case 0x2f:
		case 0x30:
		case 0x31:
		case 0x32:
		case 0x33:
			error(instruction, PPC);
			break; // Not implemented
		case 0x34:
			cycle -= 15;
			pokeb(IXYd, inc8(peekb(IXYd)));
			PC++;
			break; // INC (XY+d) ok
		case 0x35:
			cycle -= 15;
			pokeb(IXYd, dec8(peekb(IXYd)));
			PC++;
			break; // DEC (XY+d) ok
		case 0x36:
			cycle -= 19;
			pokeb(IXYd, peekb(PC + 1));
			PC += 2;
			break; // LD (XY+d),n
		case 0x37:
		case 0x38:
			error(instruction, PPC);
			break; // Not implemented
		case 0x39:
			cycle -= 15;
			tmp2 = XY;
			XY = (XY + SP) & 0xffff;
			if (XY < tmp2)
			{
				F = (F & 0xFD) | 1;
			}
			else
			{
				F = (F & 0xFD);
			}
			break;// ADD XY,SP ok
		case 0x3a:
		case 0x3b:
		case 0x3c:
		case 0x3d:
		case 0x3e:
		case 0x3f:
		case 0x40:
		case 0x41:
		case 0x42:
		case 0x43:
		case 0x44:
		case 0x45:
			error(instruction, PPC);
			break; // Not implemented
		case 0x46:
			cycle -= 11;
			B = peekb(IXYd);
			PC++;
			break; // LD B,(XY+d) ok
		case 0x47:
		case 0x48:
		case 0x49:
		case 0x4a:
		case 0x4b:
			error(instruction, PPC);
			break; // Not implemented
		case 0x4c:
			cycle -= 11;
			C = (XY >> 8);
			break;
		case 0x4d:
			cycle -= 11;
			C = (XY & 0xff);
			break;
		case 0x4e:
			cycle -= 11;
			C = peekb(IXYd);
			PC++;
			break; // LD C,(XY+d) ok
		case 0x4f:
		case 0x50:
		case 0x51:
		case 0x52:
		case 0x53:
			error(instruction, PPC);
			break; // Not implemented
		case 0x54:
			cycle -= 11;
			D = (XY >> 8);
			break;
		case 0x55:
			cycle -= 11;
			D = (XY & 0xff);
			break;
		case 0x56:
			cycle -= 11;
			D = peekb(IXYd);
			PC++;
			break; // LD D,(XY+d) ok
		case 0x57:
		case 0x58:
		case 0x59:
		case 0x5a:
		case 0x5b:
			error(instruction, PPC);
			break; // Not implemented
		case 0x5c:
			cycle -= 11;
			E = (XY >> 8);
			break;
		case 0x5d:
			cycle -= 11;
			E = (XY & 0xff);
			break;
		case 0x5e:
			cycle -= 11;
			E = peekb(IXYd);
			PC++;
			break; // LD E,(XY+d) ok
		case 0x5f:
			error(instruction, PPC);
			break; // Not implemented
		case 0x60:
			cycle -= 11;
			XY = ldXYH_8(XY, B);
			break;
		case 0x61:
			cycle -= 11;
			XY = ldXYH_8(XY, C);
			break;
		case 0x62:
			cycle -= 11;
			XY = ldXYH_8(XY, D);
			break;
		case 0x63:
			cycle -= 11;
			XY = ldXYH_8(XY, E);
			break;
		case 0x64:
		case 0x65:
			error(instruction, PPC);
			break; // Not implemented
		case 0x66:
			cycle -= 11;
			H = peekb(IXYd);
			PC++;
			break; // LD H,(XY+d) ok
		case 0x67:
			cycle -= 11;
			XY = (XY & 0xff) | (A << 8);
			break;
		case 0x68:
			cycle -= 11;
			XY = ldXYL_8(XY, B);
			break;
		case 0x69:
			cycle -= 11;
			XY = ldXYL_8(XY, C);
			break;
		case 0x6a:
			cycle -= 11;
			XY = ldXYL_8(XY, D);
			break;
		case 0x6b:
			cycle -= 11;
			XY = ldXYL_8(XY, E);
			break;
		case 0x6c:
		case 0x6d:
			error(instruction, PPC);
			break; // Not implemented
		case 0x6e:
			cycle -= 11;
			L = peekb(IXYd);
			PC++;
			break; // LD L,(XY+d) ok
		case 0x6f:
			cycle -= 11;
			XY = (XY & 0xff00) | A;
			break;
		case 0x70:
			cycle -= 11;
			pokeb(IXYd, B);
			PC++;
			break; // LD (XY+d),B ok
		case 0x71:
			cycle -= 11;
			pokeb(IXYd, C);
			PC++;
			break; // LD (XY+d),C ok
		case 0x72:
			cycle -= 11;
			pokeb(IXYd, D);
			PC++;
			break; // LD (XY+d),D ok
		case 0x73:
			cycle -= 11;
			pokeb(IXYd, E);
			PC++;
			break; // LD (XY+d),E ok
		case 0x74:
			cycle -= 11;
			pokeb(IXYd, H);
			PC++;
			break; // LD (XY+d),H ok
		case 0x75:
			cycle -= 11;
			pokeb(IXYd, L);
			PC++;
			break; // LD (XY+d),L ok
		case 0x77:
			cycle -= 11;
			pokeb(IXYd, A);
			PC++;
			break; // LD (XY+d),A ok
		case 0x78:
		case 0x79:
		case 0x7a:
		case 0x7b:
			error(instruction, PPC);
			break; // Not implemented
		case 0x7c:
			cycle -= 11;
			A = (XY & 0xff00) >> 8;
			break;
		case 0x7d:
			cycle -= 11;
			A = XY & 0xff;
			break;
		case 0x7e:
			cycle -= 11;
			A = peekb(IXYd);
			PC++;
			break; // LD A,(XY+d) ok
		case 0x7f:
		case 0x80:
		case 0x81:
		case 0x82:
		case 0x83:
			error(instruction, PPC);
			break; // Not implemented
		case 0x84:
			cycle -= 11;
			A = addA_8((XY >> 8), A);
			break;
		case 0x85:
			cycle -= 11;
			A = addA_8((XY & 0xff), A);
			break;
		case 0x86:
			cycle -= 11;
			A = addA_8(peekb(IXYd), A);
			PC++;
			break; // ADD A,(XY+d) ok
		case 0x87:
		case 0x88:
		case 0x89:
		case 0x8a:
		case 0x8b:
		case 0x8c:
		case 0x8d:
			error(instruction, PPC);
			break; // Not implemented
		case 0x8e:
			cycle -= 11;
			A = adcA_8(peekb(IXYd), A);
			PC++;
			break; // ADC A,(XY+d) ok
		case 0x8f:
		case 0x90:
		case 0x91:
		case 0x92:
		case 0x93:
			error(instruction, PPC);
			break; // Not implemented
		case 0x94:
			cycle -= 11;
			A = subA_8(XY >> 8, A);
			break;
		case 0x95:
			cycle -= 11;
			A = subA_8(XY & 0xff, A);
			break;
		case 0x96:
			cycle -= 11;
			A = subA_8(peekb(IXYd), A);
			PC++;
			break; // SUB A,(XY+d) ok
		case 0x97:
		case 0x98:
		case 0x99:
		case 0x9a:
		case 0x9b:
		case 0x9c:
		case 0x9d:
			error(instruction, PPC);
			break; // Not implemented
		case 0x9e:
			cycle -= 11;
			A = sbcA_8(peekb(IXYd), A);
			PC++;
			break; // SBC A,(XY+d) ok
		case 0x9f:
		case 0xa0:
		case 0xa1:
		case 0xa2:
		case 0xa3:
		case 0xa4:
		case 0xa5:
			error(instruction, PPC);
			break; // Not implemented
		case 0xa6:
			cycle -= 11;
			A = andA(peekb(IXYd), A);
			PC++;
			break; // AND (XY+d) ok
		case 0xa7:
		case 0xa8:
		case 0xa9:
		case 0xaa:
		case 0xab:
			error(instruction, PPC);
			break; // Not implemented
		case 0xac:
			cycle -= 11;
			A = xorA(XY >> 8, A);
			break;
		case 0xad:
			error(instruction, PPC);
			break; // Not implemented
		case 0xae:
			cycle -= 11;
			A = xorA(peekb(IXYd), A);
			PC++;
			break; // XOR (XY+d) ok
		case 0xaf:
		case 0xb0:
		case 0xb1:
		case 0xb2:
		case 0xb3:
		case 0xb4:
		case 0xb5:
			error(instruction, PPC);
			break; // Not implemented
		case 0xb6:
			cycle -= 11;
			A = orA(peekb(IXYd), A);
			PC++;
			break; // OR (XY+d) ok
		case 0xb7:
		case 0xb8:
		case 0xb9:
		case 0xba:
		case 0xbb:
		case 0xbc:
		case 0xbd:
			error(instruction, PPC);
			break; // Not implemented
		case 0xbe:
			cycle -= 11;
			cpA_8(peekb(IXYd), A);
			PC++;
			break; // CP (XY+d) ok
		case 0xcb: // ---------------------------Second
					// table-----------------------------
			IXYd = XY + sign(peekb(PC++));
			instruction = (instruction << 8) + peekb(PC++);
			switch (instruction & 0xff)
			{
			case 0x06:
				cycle -= 15;
				pokeb(IXYd, rlc(peekb(IXYd)));
				break; // RLC (IXYd) ok
			case 0x0E:
				cycle -= 15;
				pokeb(IXYd, rrc(peekb(IXYd)));
				break; // RRC (IXYd) ok
			case 0x16:
				cycle -= 12;
				pokeb(IXYd, rl(peekb(IXYd)));
				break; // RL (IXYd) ok
			case 0x1E:
				cycle -= 12;
				pokeb(IXYd, rr(peekb(IXYd)));
				break; // RR (IXYd) ok
			case 0x26:
				cycle -= 12;
				pokeb(IXYd, sla(peekb(IXYd)));
				break; // SLA (IXYd) ok
			case 0x2e:
				cycle -= 12;
				pokeb(IXYd, sra(peekb(IXYd)));
				break; // SRA (IXYd) ok
			case 0x36:
				cycle -= 12;
				pokeb(IXYd, sll(peekb(IXYd)));
				break; // SLL (IXYd) ok
			case 0x3e:
				cycle -= 12;
				pokeb(IXYd, srl(peekb(IXYd)));
				break; // SRL (IXYd) ok
			case 0x40:
			case 0x41:
			case 0x42:
			case 0x43:
			case 0x44:
			case 0x45:
			case 0x46:
			case 0x47:
				cycle -= 12;
				bit(0, peekb(IXYd));
				break;
			case 0x48:
			case 0x49:
			case 0x4a:
			case 0x4b:
			case 0x4c:
			case 0x4d:
			case 0x4e:
			case 0x4f:
				cycle -= 12;
				bit(1, peekb(IXYd));
				break;
			case 0x50:
			case 0x51:
			case 0x52:
			case 0x53:
			case 0x54:
			case 0x55:
			case 0x56:
			case 0x57:
				cycle -= 12;
				bit(2, peekb(IXYd));
				break;
			case 0x58:
			case 0x59:
			case 0x5a:
			case 0x5b:
			case 0x5c:
			case 0x5d:
			case 0x5e:
			case 0x5f:
				cycle -= 12;
				bit(3, peekb(IXYd));
				break;
			case 0x60:
			case 0x61:
			case 0x62:
			case 0x63:
			case 0x64:
			case 0x65:
			case 0x66:
			case 0x67:
				cycle -= 12;
				bit(4, peekb(IXYd));
				break;
			case 0x68:
			case 0x69:
			case 0x6a:
			case 0x6b:
			case 0x6c:
			case 0x6d:
			case 0x6e:
			case 0x6f:
				cycle -= 12;
				bit(5, peekb(IXYd));
				break;
			case 0x70:
			case 0x71:
			case 0x72:
			case 0x73:
			case 0x74:
			case 0x75:
			case 0x76:
			case 0x77:
				cycle -= 12;
				bit(6, peekb(IXYd));
				break;
			case 0x78:
			case 0x79:
			case 0x7a:
			case 0x7b:
			case 0x7c:
			case 0x7d:
			case 0x7e:
			case 0x7f:
				cycle -= 12;
				bit(7, peekb(IXYd));
				break;
			case 0x86:
				cycle -= 12;
				pokeb(IXYd, res(0, peekb(IXYd)));
				break;
			case 0x8e:
				cycle -= 12;
				pokeb(IXYd, res(1, peekb(IXYd)));
				break;
			case 0x96:
				cycle -= 12;
				pokeb(IXYd, res(2, peekb(IXYd)));
				break;
			case 0x9e:
				cycle -= 12;
				pokeb(IXYd, res(3, peekb(IXYd)));
				break;
			case 0xa6:
				cycle -= 12;
				pokeb(IXYd, res(4, peekb(IXYd)));
				break;
			case 0xae:
				cycle -= 12;
				pokeb(IXYd, res(5, peekb(IXYd)));
				break;
			case 0xb6:
				cycle -= 12;
				pokeb(IXYd, res(6, peekb(IXYd)));
				break;
			case 0xbe:
				cycle -= 12;
				pokeb(IXYd, res(7, peekb(IXYd)));
				break;
			case 0xc6:
				cycle -= 12;
				pokeb(IXYd, set(0, peekb(IXYd)));
				break;
			case 0xce:
				cycle -= 12;
				pokeb(IXYd, set(1, peekb(IXYd)));
				break;
			case 0xd6:
				cycle -= 12;
				pokeb(IXYd, set(2, peekb(IXYd)));
				break;
			case 0xde:
				cycle -= 12;
				pokeb(IXYd, set(3, peekb(IXYd)));
				break;
			case 0xe6:
				cycle -= 12;
				pokeb(IXYd, set(4, peekb(IXYd)));
				break;
			case 0xee:
				cycle -= 12;
				pokeb(IXYd, set(5, peekb(IXYd)));
				break;
			// case 0xf4: cycle-=12; pokeb(IXYd,set(6,peekb(IXYd))); //
			// undocumented
			// H=peekb(IXYd); break;
			case 0xf6:
				cycle -= 12;
				pokeb(IXYd, set(6, peekb(IXYd)));
				break;
			case 0xfe:
				cycle -= 12;
				pokeb(IXYd, set(7, peekb(IXYd)));
				break;

			default:
				error(instruction, PPC);
				break; // Not implemented
			}
			break;
		case 0xe1:
			cycle -= 14;
			XY = pop();
			break; // POP XY ok
		case 0xe3:
			cycle -= 23;
			tmp1 = peekw(SP);
			pokew(SP, XY);
			XY = tmp1;
			break; // EX (SP),XY ok
		case 0xe5:
			cycle -= 14;
			pokew(SP - 2, XY);
			SP -= 2;
			break; // PUSH XY ok
		case 0xe9:
			cycle -= 8;
			PC = XY;
			break; // JP (XY) ok
		case 0xeb:
			cycle -= 8;
			tmp1 = DE();
			D = XY >> 8;
			E = XY & 0xff;
			XY = tmp1;
			break; // EX DE,XY ok
		case 0xf9:
			cycle -= 10;
			SP = XY;
			break; // LD SP,XY ok

		default:
			error(instruction, PPC);
			break; // Not implemented
		}
		return XY;
	}

	/** NOP */
	private final void nop()
	{
		cycle -= 4;
	}

	/** LD BC, nn */
	private final void ld_BC_nn()
	{
		cycle -= 10;
		C = peekb(PC++);
		B = peekb(PC++);
	}

	/** LD (BC),A */
	private final void ld_BCi_A()
	{
		cycle -= 7;
		pokeb(BC(), A);
	}

	/** INC BC */
	private final void inc_BC()
	{
		cycle -= 6;
		BC((BC() + 1) & 0xffff);
	}

	/** INC B */
	private final void inc_B()
	{
		cycle -= 4;
		B = inc8(B);
	}

	/** DEC B */
	private final void dec_B()
	{
		cycle -= 4;
		B = dec8(B);
	}

	/** LD B,n */
	private final void ld_B_n()
	{
		cycle -= 7;
		B = peekb(PC++);
	}

	/** RLCA */
	private final void rlca()
	{
		cycle -= 4;
		A = rlc_A(A);
	}

	/** EX AF,AF' */
	public final void ex_AF_AF()
	{
		cycle -= 4;
		tmp = A;
		A = A1;
		A1 = tmp;
		tmp = F;
		F = F1;
		F1 = tmp;
	}

	/** ADD HL,BC */
	private final void add_HL_BC()
	{
		cycle -= 11;
		HL(add16(HL(), BC()));
	}

	/** LD A,(BC) */
	private final void ld_A_BCi()
	{
		cycle -= 7;
		A = peekb(BC());
	}

	/** DEC BC */
	private final void dec_BC()
	{
		cycle -= 6;
		BC((BC() - 1) & 0xffff);
	}

	/** INC C */
	private final void inc_C()
	{
		cycle -= 4;
		C = inc8(C);
	}

	/** DEC C */
	private final void dec_C()
	{
		cycle -= 4;
		C = dec8(C);
	}

	/** LD C,n */
	private final void ld_C_n()
	{
		cycle -= 7;
		C = peekb(PC++);
	}

	/** RRCA */
	private final void rrca()
	{
		cycle -= 4;
		A = rrc_A(A);
	}

	/** DJNZ,n */
	private final void djnz_n()
	{
		B = (B - 1) & 0xff;
		if (B != 0)
		{
			cycle -= 13;
			PC += sign(peekb(PC));
			PC++;
		}
		else
		{
			cycle -= 8;
			PC++;
		}
	}

	/** LD DE,nn */
	private final void ld_DE_nn()
	{
		cycle -= 10;
		E = peekb(PC++);
		D = peekb(PC++);
	}

	/** LD (DE),A */
	private final void ld_DEi_A()
	{
		cycle -= 7;
		pokeb(DE(), A);
	}

	/** INC DE */
	private final void inc_DE()
	{
		cycle -= 6;
		DE((DE() + 1) & 0xffff);
	}

	/** INC D */
	private final void inc_D()
	{
		cycle -= 4;
		D = inc8(D);
	}

	/** DEC D */
	private final void dec_D()
	{
		cycle -= 4;
		D = dec8(D);
	}

	/** LD D,n */
	private final void ld_D_n()
	{
		cycle -= 7;
		D = peekb(PC++);
	}

	/** RLA */
	private final void rla()
	{
		cycle -= 4;
		A = rl_A(A);
	}

	/** JR e */
	private final void jr_e()
	{
		cycle -= 12;
		PC += sign(peekb(PC));
		PC++;
	}

	/** ADD HL,DE */
	private final void add_HL_DE()
	{
		cycle -= 11;
		HL(add16(HL(), DE()));
	}

	/** LD A,(DE) */
	private final void ld_A_DEi()
	{
		cycle -= 7;
		A = peekb(DE());
	}

	/** DEC DE */
	private final void dec_DE()
	{
		cycle -= 6;
		DE((DE() - 1) & 0xffff);
	}

	/** INC E */
	private final void inc_E()
	{
		cycle -= 4;
		E = inc8(E);
	}

	/** DEC E */
	private final void dec_E()
	{
		cycle -= 4;
		E = dec8(E);
	}

	/** LD E,n */
	private final void ld_E_n()
	{
		cycle -= 7;
		E = peekb(PC++);
	}

	/** RRA */
	private final void rra()
	{
		cycle -= 4;
		A = rr_A(A);
	}

	/** JR NZ,e */
	private final void jr_NZ_e()
	{
		if ((F & ZF) == 0)
		{
			cycle -= 12;
			PC += sign(peekb(PC));
			PC++;
		}
		else
		{
			PC++;
			cycle -= 7;
		}
	}

	/** LD HL,nn */
	private final void ld_HL_nn()
	{
		cycle -= 10;
		L = peekb(PC++);
		H = peekb(PC++);
	}

	/** LD (nn),HL */
	private final void ld_ni_HL()
	{
		cycle -= 16;
		ld_ea_ind16(HL());
	}

	/** INC HL */
	private final void inc_HL()
	{
		cycle -= 6;
		HL((HL() + 1) & 0xffff);
	}

	/** INC H */
	private final void inc_H()
	{
		cycle -= 4;
		H = inc8(H);
	}

	/** DEC H */
	private final void dec_H()
	{
		cycle -= 4;
		H = dec8(H);
	}

	/** LD H,n */
	private final void ld_H_n()
	{
		cycle -= 7;
		H = peekb(PC++);
	}

	/** DAA */
	private final void daa()
	{
		cycle -= 4;
		tmp1 = A;
		tmp2 = 0;
		tmp3 = (F & 1);
		int tmp = tmp3;
		if (((F & 0x10) != 0) || ((tmp1 & 0x0f) > 0x09))
		{
			tmp2 |= 0x06;
		}
		if ((tmp3 == 1) || (tmp1 > 0x9f) || ((tmp1 > 0x8f) && ((tmp1 & 0x0f) > 0x09)))
		{
			tmp2 |= 0x60;
			tmp = 1;
		}
		if (tmp1 > 0x99)
		{
			tmp = 1;
		}
		if ((F & 0x02) != 0)
		{
			cycle -= 4;
			A = subA_8(tmp2, A);
		}
		else
		{
			cycle -= 4;
			A = addA_8(tmp2, A);
		}
		F = (F & 0xfe) | tmp;
		if (parity[A])
		{
			F = (F & 0xfb) | 4;
		}
		else
		{
			F = (F & 0xfb);
		}
	}

	/** JR Z,e */
	private final void jr_Z_e()
	{
		if ((F & ZF) != 0)
		{
			cycle -= 12;
			PC += sign(peekb(PC));
			PC++;
		}
		else
		{
			PC++;
			cycle -= 7;
		}
	}

	/** ADD HL,HL */
	private final void add_HL_HL()
	{
		cycle -= 11;
		int hl = HL();
		HL(add16(hl, hl));
	}

	/** LD HL,(nn) */
	private final void ld_HL_ni()
	{
		cycle -= 16;
		int ea = peekw(PC);
		H = peekb(ea + 1);
		L = peekb(ea);
		PC += 2;
	}

	/** DEC HL */
	private final void dec_HL()
	{
		cycle -= 6;
		HL((HL() - 1) & 0xffff);
	}

	/** INC L */
	private final void inc_L()
	{
		cycle -= 4;
		L = inc8(L);
	}

	/** DEC L */
	private final void dec_L()
	{
		cycle -= 4;
		L = dec8(L);
	}

	/** LD L,n */
	private final void ld_L_n()
	{
		cycle -= 7;
		L = peekb(PC++);
	}

	/** CPL */
	private final void cpl()
	{
		cycle -= 4;
		A ^= 0xff;
		F = (F & (0xc5)) | 0x12 | (A & (0x28));
	}

	/** JR NC,e */
	private final void jr_NC_e()
	{
		if ((F & CF) == 0)
		{
			cycle -= 12;
			PC += sign(peekb(PC));
			PC++;
		}
		else
		{
			PC++;
			cycle -= 7;
		}
	}

	/** LD SP,nn */
	private final void ld_SP_nn()
	{
		cycle -= 10;
		SP = peekw(PC);
		PC += 2;
	}

	/** LD (nn),A */
	private final void ld_ni_A()
	{
		cycle -= 13;
		ld_ea_ind8(A);
	}

	/** INC SP */
	private final void inc_SP()
	{
		cycle -= 6;
		SP = (SP + 1) & 0xffff;
	}

	/** INC (HL) */
	private final void inc_HLi()
	{
		cycle -= 11;
		int hl = HL();
		pokeb(hl, inc8(peekb(hl)));
	}

	/** DEC (HL) */
	private final void dec_HLi()
	{
		cycle -= 11;
		int hl = HL();
		pokeb(hl, dec8(peekb(hl)));
	}

	/** LD (HL),n */
	private final void ld_HLi_n()
	{
		cycle -= 10;
		pokeb(HL(), peekb(PC++));
	}

	/** SCF */
	private final void scf()
	{
		cycle -= 4;
		F = (F & 0xc4) | 1 | (A & (0x28));
	}

	/** JR C,e */
	private final void jr_C_e()
	{
		if ((F & CF) != 0)
		{
			cycle -= 12;
			PC += sign(peekb(PC));
			PC++;
		}
		else
		{
			PC++;
			cycle -= 7;
		}
	}

	/** ADD HL,SP */
	private final void add_HL_SP()
	{
		cycle -= 11;
		HL(add16(HL(), SP));
	}

	/** LD A,(nn) */
	private final void ld_A_ni()
	{
		cycle -= 13;
		A = peekb(peekw(PC));
		PC += 2;
	}

	/** DEC SP */
	private final void dec_SP()
	{
		cycle -= 6;
		SP = (SP - 1) & 0xffff;
	}

	/** INC A */
	private final void inc_A()
	{
		cycle -= 4;
		A = inc8(A);
	}

	/** DEC A */
	private final void dec_A()
	{
		cycle -= 4;
		A = dec8(A);
	}

	/** LD A,n */
	private final void ld_A_n()
	{
		cycle -= 7;
		A = peekb(PC++);
	}

	/** CCF */
	private final void ccf()
	{
		cycle -= 4;
		F = ((F & 0xc5) | ((F & 1) << 4) | (A & 0x28)) ^ 1;
	}

	/** HALT */
	private final void halt()
	{
		cycle -= 4;
		state_HALT = true;
		goingToirq = false;
		// PC--;
		cycle = 0;
	}

	/** RET NZ */
	private final void ret_NZ()
	{
		if ((F & ZF) == 0)
		{
			cycle -= 11;
			PC = pop();
		}
		else
		{
			cycle -= 5;
		}
	}

	/** POP BC */
	private final void pop_BC()
	{
		cycle -= 10;
		C = peekb(SP++);
		B = peekb(SP++);
	}

	/** JP NZ,nn */
	private final void jp_NZ_nn()
	{
		cycle -= 10;
		if ((F & ZF) == 0)
		{
			PC = peekw(PC);
		}
		else
		{
			PC += 2;
		}
	}

	/** JP nn */
	private final void jp_nn()
	{
		cycle -= 10;
		PC = peekw(PC);
	}

	/** CALL NZ,nn */
	private final void call_NZ_nn()
	{
		if ((F & ZF) == 0)
		{
			cycle -= 17;
			push(PC + 2);
			PC = peekw(PC);
		}
		else
		{
			cycle -= 10;
			PC += 2;
		}
	}

	/** PUSH BC */
	private final void push_BC()
	{
		cycle -= 11;
		push(BC());
	}

	/** ADD A,n */
	private final void add_A_n()
	{
		cycle -= 7;
		A = addA_8(peekb(PC++), A);
	}

	/** RET Z */
	private final void ret_Z()
	{
		if ((F & ZF) != 0)
		{
			cycle -= 11;
			PC = pop();
		}
		else
		{
			cycle -= 5;
		}
	}

	/** RET */
	private final void ret()
	{
		cycle -= 10;
		PC = pop();
	}

	/** JP Z,nn */
	private final void jp_Z_nn()
	{
		cycle -= 10;
		if ((F & ZF) != 0)
		{
			PC = peekw(PC);
		}
		else
		{
			PC += 2;
		}
	}

	/** CALL Z,nn */
	private final void call_Z_nn()
	{
		if ((F & ZF) != 0)
		{
			cycle -= 17;
			push(PC + 2);
			PC = peekw(PC);
		}
		else
		{
			cycle -= 10;
			PC += 2;
		}
	}

	/** CALL nn */
	private final void call_nn()
	{
		cycle -= 17;
		push(PC + 2);
		PC = peekw(PC);
	}

	/** ADC A,n */
	private final void adc_A_n()
	{
		cycle -= 7;
		A = adcA_8(peekb(PC++), A);
	}

	/** RET NC */
	private final void ret_NC()
	{
		if ((F & CF) == 0)
		{
			cycle -= 11;
			PC = pop();
		}
		else
		{
			cycle -= 5;
		}
	}

	/** POP DE */
	private final void pop_DE()
	{
		cycle -= 10;
		E = peekb(SP++);
		D = peekb(SP++);
	}

	/** JP NC,nn */
	private final void jp_NC_nn()
	{
		cycle -= 10;
		if ((F & CF) == 0)
		{
			PC = peekw(PC);
		}
		else
		{
			PC += 2;
		}
	}

	/** OUT (n),A */
	private final void out_n_A()
	{
		cycle -= 11;
		out(peekb(PC++), A);
	}

	/** CALL NC, nn */
	private final void call_NC_nn()
	{
		if ((F & CF) == 0)
		{
			cycle -= 17;
			push(PC + 2);
			PC = peekw(PC);
		}
		else
		{
			cycle -= 10;
			PC += 2;
		}
	}

	/** PUSH DE */
	private final void push_DE()
	{
		cycle -= 11;
		push(DE());
	}

	/** SUB A,n */
	private final void sub_A_n()
	{
		cycle -= 7;
		A = subA_8(peekb(PC++), A);
	}

	/** RET C */
	private final void ret_C()
	{
		if ((F & CF) != 0)
		{
			cycle -= 11;
			PC = pop();
		}
		else
		{
			cycle -= 5;
		}
	}

	/** EXX */
	public final void exx()
	{
		cycle -= 4;
		tmp = B;
		B = B1;
		B1 = tmp;
		tmp = C;
		C = C1;
		C1 = tmp;
		tmp = D;
		D = D1;
		D1 = tmp;
		tmp = E;
		E = E1;
		E1 = tmp;
		tmp = H;
		H = H1;
		H1 = tmp;
		tmp = L;
		L = L1;
		L1 = tmp;
	}

	/** JP C,nn */
	private final void jp_C_nn()
	{
		cycle -= 10;
		if ((F & CF) != 0)
		{
			PC = peekw(PC);
		}
		else
		{
			PC += 2;
		}
	}

	/* IN A,(n) */
	private final void in_A_n()
	{
		cycle -= 11;
		A = in(peekb(PC++), A);
	}

	/** CALL C,nn */
	private final void call_C_nn()
	{
		if ((F & CF) != 0)
		{
			cycle -= 17;
			push(PC + 2);
			PC = peekw(PC);
		}
		else
		{
			cycle -= 10;
			PC += 2;
		}
	}

	/** SBC A,n */
	private final void sbc_A_n()
	{
		cycle -= 7;
		A = sbcA_8(peekb(PC++), A);
	}

	/** RET PO */
	private final void ret_PO()
	{
		if ((F & PF) == 0)
		{
			cycle -= 11;
			PC = peekw(SP);
			SP += 2;
		}
		else
		{
			cycle -= 5;
		}
	}

	/** POP HL */
	private final void pop_HL()
	{
		cycle -= 10;
		L = peekb(SP++);
		H = peekb(SP++);
	}

	/** JP PO,nn */
	private final void jp_PO_nn()
	{
		cycle -= 10;
		if ((F & PF) == 0)
		{
			PC = peekw(PC);
		}
		else
		{
			PC += 2;
		}
	}

	/** EX (SP),HL */
	private final void ex_SPi_HL()
	{
		cycle -= 19;
		tmp = peekb(SP + 1);
		pokeb(SP + 1, H);
		H = tmp;
		tmp = peekb(SP);
		pokeb(SP, L);
		L = tmp;
	}

	/** CALL PO,nn */
	private final void call_PO_nn()
	{
		if ((F & PF) == 0)
		{
			cycle -= 17;
			push(PC + 2);
			PC = peekw(PC);
		}
		else
		{
			cycle -= 10;
			PC += 2;
		}
	}

	/** PUSH HL */
	private final void push_HL()
	{
		cycle -= 11;
		push(HL());
	}

	/** AND A,n */
	private final void and_A_n()
	{
		cycle -= 7;
		A = andA(peekb(PC++), A);
	}

	/** RET PE */
	private final void ret_PE()
	{
		if ((F & PF) != 0)
		{
			cycle -= 11;
			PC = pop();
		}
		else
		{
			cycle -= 5;
		}
	}

	/** JP (HL) */
	private final void jp_HLi()
	{
		cycle -= 4;
		PC = HL();
	}

	/** JP PE,nn */
	private final void jp_PE_nn()
	{
		cycle -= 10;
		if ((F & PF) != 0)
		{
			PC = peekw(PC);
		}
		else
		{
			PC += 2;
		}
	}

	/** EX DE,HL */
	private final void ex_DE_HL()
	{
		cycle -= 4;
		tmp = D;
		D = H;
		H = tmp;
		tmp = E;
		E = L;
		L = tmp;
	}

	/** CALL PE,nn */
	private final void call_PE_nn()
	{
		if ((F & PF) != 0)
		{
			cycle -= 17;
			push(PC + 2);
			PC = peekw(PC);
		}
		else
		{
			cycle -= 10;
			PC += 2;
		}
	}

	/** XOR A,n */
	private final void xor_n()
	{
		cycle -= 7;
		A = xorA(peekb(PC++), A);
	}

	/** RET P */
	private final void ret_P()
	{
		if ((F & SF) == 0)
		{
			cycle -= 11;
			PC = pop();
		}
		else
		{
			cycle -= 5;
		}
	}

	/** POP AF */
	private final void pop_AF()
	{
		cycle -= 10;
		F = peekb(SP++);
		A = peekb(SP++);
	}

	/** JP P,nn */
	private final void jp_P_nn()
	{
		cycle -= 10;
		if ((F & SF) == 0)
		{
			PC = peekw(PC);
		}
		else
		{
			PC += 2;
		}
	}

	/** DI */
	private final void di()
	{
		cycle -= 4;
		IFF0 = IFF1 = false;
	}

	/** CALL P,nn */
	private final void call_P_nn()
	{
		if ((F & SF) == 0)
		{
			cycle -= 17;
			push(PC + 2);
			PC = peekw(PC);
		}
		else
		{
			cycle -= 10;
			PC += 2;
		}
	}

	/** PUSH AF */
	private final void push_AF()
	{
		cycle -= 11;
		push(AF());
	}

	/** OR A,n */
	private final void or_n()
	{
		cycle -= 7;
		A = orA(peekb(PC++), A);
	}

	/** RET M */
	private final void ret_M()
	{
		if ((F & SF) != 0)
		{
			cycle -= 11;
			PC = pop();
		}
		else
		{
			cycle -= 5;
		}
	}

	/** LD SP,HL */
	private final void ld_SP_HL()
	{
		cycle -= 6;
		SP = HL();
	}

	/** JP M,nn */
	private final void jp_M_nn()
	{
		cycle -= 10;
		if ((F & SF) != 0)
		{
			PC = peekw(PC);
		}
		else
		{
			PC += 2;
		}
	}

	/** EI */
	private final void ei()
	{
		cycle -= 4;
		IFF0 = IFF1 = true;
		goingToirq = true;
		cycle = checkInterrupt(cycle);
	}

	/** CALL M,nn */
	private final void call_M_nn()
	{
		if ((F & SF) != 0)
		{
			cycle -= 17;
			push(PC + 2);
			PC = peekw(PC);
		}
		else
		{
			cycle -= 10;
			PC += 2;
		}
	}

	/** CP A,n */
	private final void cp_n()
	{
		cycle -= 7;
		cpA_8(peekb(PC++), A);
	}

	private final int AF()
	{
		return (A << 8) | F;
	}

	private final int BC()
	{
		return (B << 8) | C;
	}

	private final int DE()
	{
		return (D << 8) | E;
	}

	private final int HL()
	{
		return (H << 8) | L;
	}

	private final int HLi()
	{
		return peekb(HL());
	}

	public final void AF(int nn)
	{
		A = nn >> 8;
		F = nn & 0xff;
	}

	public final void BC(int nn)
	{
		B = nn >> 8;
		C = nn & 0xff;
	}

	public final void DE(int nn)
	{
		D = nn >> 8;
		E = nn & 0xff;
	}

	public final void IX(int nn)
	{
		IX = nn & 0xffff;
	}

	public final void IY(int nn)
	{
		IY = nn & 0xffff;
	}

	public final void HL(int nn)
	{
		H = nn >> 8;
		L = nn & 0xff;
	}

	private final void push(int nn)
	{
		SP = (SP - 2) & 0xffff;
		pokew(SP, nn);
	}

	public final int pop()
	{
		int nn = peekw(SP);
		SP = (SP + 2) & 0xffff;
		return nn;
	}

	private final int sign(int nn)
	{
		return nn - ((nn & 128) << 1);
	}

	private final void rst(int ea)
	{
		cycle -= 11;
		push(PC);
		PC = ea;
	}

	private final void ld_A(int n, int cycles)
	{
		cycle -= cycles;
		A = n;
	}

	private final void ld_B(int n, int cycles)
	{
		cycle -= cycles;
		B = n;
	}

	private final void ld_C(int n, int cycles)
	{
		cycle -= cycles;
		C = n;
	}

	private final void ld_D(int n, int cycles)
	{
		cycle -= cycles;
		D = n;
	}

	private final void ld_E(int n, int cycles)
	{
		cycle -= cycles;
		E = n;
	}

	private final void ld_H(int n, int cycles)
	{
		cycle -= cycles;
		H = n;
	}

	private final void ld_L(int n, int cycles)
	{
		cycle -= cycles;
		L = n;
	}

	private final void ld_HLi(int n, int cycles)
	{
		cycle -= cycles;
		pokeb(HL(), n);
	}

	/** LD (ea),nn */
	private final void ld_ea_ind16(int nn)
	{
		pokew(peekw(PC), nn);
		PC += 2;
	}

	/** LD (ea),n */
	private final void ld_ea_ind8(int n)
	{
		pokeb(peekw(PC), n);
		PC += 2;
	}

	/**
	 * Add value to Accu and set flags accordingly
	 */
	private final int addA_8(int value, int A)
	{
		tmp = (A + value) & 0xff;
		F = SZHVC_Add[(A << 8) | tmp];
		return tmp;
	}

	/**
	 * Add to accu and count cycles
	 */
	private final void add_A(int n, int c)
	{
		cycle -= c;
		A = addA_8(n, A);
	}

	/**
	 * Add value with carry to Accu and set flags accordingly
	 */
	private final int adcA_8(int value, int A)
	{
		int c = F & 1;
		int result = (A + value + c) & 0xff;
		F = SZHVC_Add[(c << 16) | (A << 8) | result];
		return result;
	}

	/**
	 * Add to accu and count cycles
	 */
	private final void adc_A(int n, int c)
	{
		cycle -= c;
		A = adcA_8(n, A);
	}

	/**
	 * 8 bit increment
	 */
	private final int inc8(int value)
	{
		value = (value + 1) & 0xff;
		F = (F & 1) | SZHV_inc[value];
		return value;
	}

	/**
	 * 8 bit decrement
	 */
	private final int dec8(int value)
	{
		value = (value - 1) & 0xff;
		F = (F & 1) | SZHV_dec[value];
		return (value);
	}

	/**
	 * Compare value with Accu
	 */
	private final void cpA_8(int value, int A)
	{
		int result = (A - value) & 0xff;
		F = SZHVC_sub[(A << 8) | result];
	}

	/**
	 * Compare with accu and count cycles
	 */
	private final void cp_A(int n, int c)
	{
		cycle -= c;
		cpA_8(n, A);
	}

	/**
	 * Subtract value from Accu and set flags accordingly
	 */
	private final int subA_8(int value, int A)
	{
		int result = (A - value) & 0xff;
		F = SZHVC_sub[(A << 8) | result];
		return result;
	}

	/**
	 * Subtract from accu and count cycles
	 */
	private final void sub_A(int n, int c)
	{
		cycle -= c;
		A = subA_8(n, A);
	}

	/**
	 * Subtract value with carry from Accu and set flags accordingly
	 */
	private final int sbcA_8(int value, int A)
	{
		int c = F & 1;
		int result = (A - value - c) & 0xff;
		F = SZHVC_sub[(c << 16) | (A << 8) | result];
		return result;
	}

	/**
	 * Subtract from accu and count cycles
	 */
	private final void sbc_A(int n, int c)
	{
		cycle -= c;
		A = sbcA_8(n, A);
	}

	/**
	 * 16bit Add
	 */
	private final int add16(int a, int b)
	{
		int result = a + b;
		F = (F & 0xc4) | (((a ^ result ^ b) >> 8) & 0x10) | ((result >> 16) & 1);
		return (result & 0xffff);
	}

	/**
	 * SBC HL,nn
	 */
	private final void sbcHL(int value)
	{
		int _HLD = HL();
		int result = _HLD - value - (F & 1);
		F = (((_HLD ^ result ^ value) >> 8) & 0x10) | 0x02 | ((result >> 16) & 1) | ((result >> 8) & 0x80)
				| (((result & 0xffff) != 0) ? 0 : 0x40) | (((value ^ _HLD) & (_HLD ^ result) & 0x8000) >> 13);
		H = (result >> 8) & 0xff;
		L = result & 0xff;
	}

	/**
	 * ADC HL,nn
	 */
	private final void adcHL(int value)
	{
		int _HLD = HL();
		int result = _HLD + value + (F & 1);
		F = (((_HLD ^ result ^ value) >> 8) & 0x10) | ((result >> 16) & 1) | ((result >> 8) & 0x80)
				| (((result & 0xffff) != 0) ? 0 : 0x40) | (((value ^ _HLD ^ 0x8000) & (value ^ result) & 0x8000) >> 13);
		H = (result >> 8) & 0xff;
		L = result & 0xff;
	}

	/**
	 * Shift right - ok
	 */
	private final int srl(int value)
	{
		int c = value & 0x01;
		value = (value >> 1) & 0xff;
		F = SZP[value] | c;
		return value;
	}

	/**
	 * Shift left - ok
	 */
	private final int sla(int value)
	{
		int c = (value & 0x80) >> 7;
		value = (value << 1) & 0xff;
		F = SZP[value] | c;
		return value;
	}

	/**
	 * Shift left and insert a 1
	 */
	private final int sll(int value)
	{
		int c = (value & 0x80) >> 7;
		value = ((value << 1) | 1) & 0xff;
		F = SZP[value] | c;
		return value;
	}

	/**
	 * Shift right while keeping the correct sign
	 */
	private final int sra(int value)
	{
		int c = value & 0x01;
		value = (value >> 1) | (value & 128);
		F = SZP[value] | c;
		return value;
	}

	/**
	 * 9-bit left rotate - ok
	 */
	private final int rl(int value)
	{
		int c = (value & 0x80) >> 7;
		value = ((value << 1) | (F & 1)) & 0xff;
		F = SZP[value] | c;
		return value;
	}

	/**
	 * 9-bit left rotate A - ok
	 */
	private final int rl_A(int A)
	{
		int old = A;
		A = ((A << 1) | (F & 1)) & 0xff; // rotate
		F = (F & 0xec) | (old >> 7);
		return A;
	}

	/**
	 * 9-bit right rotate
	 */
	private final int rr(int value)
	{
		int c = (value & 0x01);
		value = ((value >> 1) | (F << 7)) & 0xff;
		F = SZP[value] | c;
		return value;
	}

	/**
	 * 9-bit right rotate A
	 */
	private final int rr_A(int A)
	{
		int old = A;
		A = ((A >> 1) | (F & 1) << 7) & 0xff; // rotate
		F = (F & 0xec) | (old & 1);
		return A;
	}

	/**
	 * left rotate - ok
	 */
	private final int rlc(int value)
	{
		int c = (value & 0x80) >> 7;
		value = ((value << 1) | (value >> 7)) & 0xff;
		F = SZP[value] | c;
		return value;
	}

	/**
	 * left rotate - ok
	 */
	private final int rlc_A(int A)
	{
		F = (F & 0xec) | (A >> 7);
		// F = (F & 0xc5) | ((A >> 7) | (A & 0x28)); // including undocumented
		// flags
		return ((A << 1) + ((A & 128) >> 7)) & 0xff; // rotate
	}

	/**
	 * right rotate - ok
	 */
	private final int rrc(int value)
	{
		int c = (value & 0x01);
		value = ((value >> 1) | (value << 7)) & 0xff;
		F = SZP[value] | c;
		return value;
	}

	/**
	 * right rotate A - ok
	 */
	private final int rrc_A(int A)
	{
		F = (F & 0xec) | (A & 0x29);
		return ((A >> 1) + ((A & 1) << 7)) & 0xff; // rotate
	}

	/**
	 * RLD
	 */
	private final int rld_A(int A, int H, int L)
	{
		int result = A;
		int t = peekb(HL());
		int q = t;

		t = (t << 4) | (result & 0x0f);
		result = (result & 0xf0) | (q >> 4);
		pokeb(HL(), (t & 0xff));

		F = (F & 1) | SZP[result];

		return result;
	}

	/**
	 * RRD
	 */
	private final int rrd_A(int A, int H, int L)
	{
		int result = A;
		int t = peekb(HL());
		int q = t;

		t = (t >> 4) | (result << 4);
		result = (result & 0xf0) | (q & 0x0f);
		pokeb(HL(), t & 0xff);

		F = (F & 1) | SZP[result];

		return result;
	}

	/**
	 * test specified bit - ok
	 */
	private final void bit(int bitNumber, int value)
	{
		F = (F & 1) | 0x10 | SZ_BIT[value & bitSet[bitNumber]];
	}

	/**
	 * set specified bit - ok
	 */
	private final int set(int bitNumber, int value)
	{
		value = value | bitSet[bitNumber];
		return value;
	}

	/**
	 * reset specified bit - ok
	 */
	private final int res(int bitNumber, int value)
	{
		value = value & bitRes[bitNumber];
		return value;
	}

	/**
	 * AND - ok
	 */
	private final int andA(int value, int A)
	{
		A &= value;
		F = SZP[A] | 0x10;
		return A;
	}

	private final void and_A(int n, int c)
	{
		cycle -= c;
		A = andA(n, A);
	}

	/**
	 * OR - ok
	 */
	private final int orA(int value, int A)
	{
		A |= value;
		F = SZP[A];
		return A;
	}

	private final void or_A(int n, int c)
	{
		cycle -= c;
		A = orA(n, A);
	}

	/**
	 * XOR - ok
	 */
	private final int xorA(int value, int A)
	{
		A ^= value;
		F = SZP[A];
		return A;
	}

	private final void xor_A(int n, int c)
	{
		cycle -= c;
		A = xorA(n, A);
	}

	/**
	 * OUT
	 */
	private final void out(int port, int value)
	{
		outb(port, value, 0);
	}

	private final void out(int port, int value, int state)
	{
		outb(port, value, state);
	}

	/**
	 * IN
	 */
	private final int in(int port, int hi)
	{
		int in = inb(port, hi);
		F = (F & 1) | SZP[A];
		return in;
	}

	public void outb(int port, int value, int status)
	{
	}

	public int inb(int port, int hi)
	{
		return 0xff;
	}

	public int peekb(int add)
	{
		return this.cpuBus.read((short) add);
	}

	public void pokeb(int add, int value)
	{
		this.cpuBus.write((short) add, (byte)(value & 0xff));
	}

	public int peekw(int add)
	{
		int value = peekb(add);
		value |= peekb(add + 1) << 8;

		return value;
	}

	public void pokew(int add, int value)
	{
		pokeb(add, value);
		pokeb(add + 1, value >> 8);
	}

	/**
	 * Undocumented
	 */
	private final int incL16(int value)
	{
		return (value & 0xff00) | inc8(value & 0xff);
	}

	private final int decL16(int value)
	{
		return (value & 0xff00) | dec8(value & 0xff);
	}

	private final int ldXYH_8(int val16, int val8)
	{
		return (val16 & 0xff) | (val8 << 8);
	}

	private final int ldXYL_8(int val16, int val8)
	{
		return (val16 & 0xff00) | val8;
	}

	/**
	 * R access
	 */
	private int R()
	{
		return (R & 0xff);
	}

	/**
	 * check interrupt
	 */
	private int checkInterrupt(int cycle)
	{
		if (NMI || (IFF0 && IRQ))
		{
			if (NMI)
			{ // Take NMI
				// if (!goingToirq) {
				// if (!debugDisabled) { log("...CPUZ80 takes non maskable
				// interrupt"); }
				state_HALT = false;

				IFF1 = IFF0;
				NMI = IFF0 = false;

				push(PC);
				PC = 0x0066; // ...and jump to 0x0066

				cycle -= 13;
				// } else {
				// goingToirq = false; /* CPU has to execute 1 more instruction
				// */
				// }
			}

			if (IFF0 && IRQ)
			{ // Take interrupt if enabled
				// System.out.println("...CPUZ80 takes interrupt using interrupt
				// mode "+Integer.toString(IM));
				state_HALT = false;

				switch (IM)
				{
				case 0: // IM0 --> exec 1-byte instruction. Only calls are
						// supported.
					IRQ = IFF0 = false;
					push(PC);
					if (I_Vector == 0 || I_Vector == 255)
					{
						PC = 0x0038;
					}
					else
					{
						PC = I_Vector;
					}
					cycle -= 13;
					break;
				case 1: // IM1 --> RST &38
					IRQ = IFF0 = false;
					push(PC);
					PC = 0x0038;
					cycle -= 13; // RST &38 = 11 cycles + 2 cycles
					break;
				case 2: // IM2 --> Call I:Vector
					IRQ = IFF0 = false;
					push(PC);
					PC = peekw((I << 8) | I_Vector);
					cycle = cycle - 19; // Call = 17 cycles + 2 cycles
					break;
				default:
					break;
				}
				// } else {
				// goingToirq = false; // CPU has to execute 1 more instruction
				// }
			}
		}
		return cycle;
	}

	/**
	 * Illegal instruction
	 */
	private final void error(int instruction, int address)
	{
		System.out.println(
				"CPU error: illegal instruction $" + Integer.toHexString(instruction) + " at $" + Integer.toHexString(address));
		System.exit(1);
	}

	private void debug(int instruction, int PPC, int A, int F, int B, int C, int D, int E, int H, int L, int SP, int IX, int IY,
			int I, int cycles)
	{

		if (PPC == debugBreakPoint)
		{
			debugEnabled = true;
		}

		if (debugEnabled)
		{
			if (startSlice)
			{
				//System.out.println("*** " + tag + " ***");
				startSlice = false;
			}
			//System.out.println("instruction[" + instruction + "]:" + AddressMap.toHexAddress(instruction,IOSize.IO8Bit));
			
			int opcgroup = instruction & 65280;
			String dataLog = "";
			
			dataLog = (Convert.padStringWithZeroes(Integer.toHexString(PC).toUpperCase(), 4) + ": ");
			
			//dataLog = AddressMap.toHexAddress(PPC,IOSize.IO16Bit) + " : " + 
			//          AddressMap.toHexAddress(peekb(PPC + 0),IOSize.IO8Bit) + ","	+ 
			//          AddressMap.toHexAddress(peekb(PPC + 1),IOSize.IO8Bit) + "," + 
			//          AddressMap.toHexAddress(peekb(PPC + 2),IOSize.IO8Bit) + "," + 
			//          AddressMap.toHexAddress(peekb(PPC + 3),IOSize.IO8Bit) + "    \t";
			if (instruction < 256)
			{
				if(instruction < 0)
					instruction+=256;
				
				try
				{
					//if(opc1[instruction].length() == 0)
					//	dataLog = dataLog + opc1[instruction];

					dataLog = dataLog + "00" + opc1[instruction];
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if (opcgroup == (237 << 8))
			{
				dataLog = dataLog + "00" +  opc3[(instruction & 0xff)];
			}
			else if (opcgroup == (203 << 8))
			{
				dataLog = dataLog + "00" + opc2[(instruction & 0xff)];
			}
			else if (opcgroup == (221 << 8))
			{
				dataLog = dataLog + "DD" +  opcIX[(instruction & 0xff)];
			}
			else if (opcgroup == (0xFD << 8))
			{
				dataLog = dataLog + "FD" +  opcIY[(instruction & 0xff)];
			}				
			/*
			dataLog = dataLog + "   AF:" + Integer.toHexString(AF()) + " BC:" + Integer.toHexString(BC()) + " DE:"
					+ Integer.toHexString(DE()) + " HL:" + Integer.toHexString(HL()) + " SP:" + Integer.toHexString(SP) + " IX:"
					+ Integer.toHexString(IX) + " IY:" + Integer.toHexString(IY) + " I:" + Integer.toHexString(I) + " Cycles:"
					+ Integer.toString(cycles);
			System.out.println(dataLog);
			*/
			if(dataLog.length() > 0)
				history.add(dataLog);

		}
	}

	@Override
	public String[] getFeatures()
	{
		return new String[0];
	}

	@Override
	public boolean isEnabled(String feature)
	{
		return false;
	}

	@Override
	public void setEnabled(String feature, boolean state)
	{
	}

	@Override
	public void pulse()
	{
		this.clock();	
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return "Z80";
	}

	@Override
	public void interrupt()
	{
		// TODO Auto-generated method stub		
	}

	@Override
	public void clock()
	{
		exec(1);
	}

	@Override
	public Telemetry getTelemetry()
	{
		telemetry.registerInfo.put(" A",(A & 0x00FF));
		telemetry.registerInfo.put(" R",(R & 0x00FF));
		telemetry.registerInfo.put(" B",(B & 0x00FF));
		telemetry.registerInfo.put(" C",(C & 0x00FF));
		telemetry.registerInfo.put(" D",(D & 0x00FF));
		telemetry.registerInfo.put(" E",(E & 0x00FF));
		telemetry.registerInfo.put(" H",(H & 0x00FF));
		telemetry.registerInfo.put(" L",(L & 0x00FF));
		telemetry.registerInfo.put("IX",(IX & 0xFFFF));
		telemetry.registerInfo.put("IY",(IY & 0xFFFF));
		
		
		telemetry.flagInfo.put("SZXHYPAC",F);
		
		telemetry.a = (byte) (A & 0x00FF) ;
		telemetry.flags = (byte) (F & 0x00FF) ;
		telemetry.x = (byte) (H & 0x00FF);
		telemetry.y = (byte) (L & 0x00FF);
		
		telemetry.addressAbsolute = (short)((B << 8) | C);
		telemetry.registerInfo.put("BC",(int)telemetry.addressAbsolute);
		
		telemetry.addressRelative = (short)((D << 8) | E);
		telemetry.registerInfo.put("DE",(int)telemetry.addressRelative);
		
		telemetry.opcode = (byte)(instruction & 0xff);
		telemetry.opcodeName = "";
		
		telemetry.programCounter = (short) PPC;
		telemetry.stackPointer = (byte) (SP & 0x00FF);
		
		telemetry.clocks = cycleCounter;
		telemetry.clocksPerSecond = clocksPerSecond;
		telemetry.cycles = Math.abs(cycle);
		telemetry.history = history;	
		
		return telemetry;
	}

	@Override
	public void setBus(Bus bus)
	{
		this.cpuBus = bus;
		
	}

	@Override
	public IOSize getAddressableSize()
	{
		return IOSize.IO16Bit;
	}

	@Override
	public IOSize getDataSize()
	{
		return IOSize.IO8Bit;
	}

	@Override
	public boolean isOddAlignmentValid(IOSize size)
	{
		return true;
	}

}; // end class declaration
