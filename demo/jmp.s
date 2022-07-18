;
;ca65 jmp.s -D BASE=0x0000
;cl65 jmp.o --target none --start-addr 0x0000 -o jmp.ram
;
;
	.setcpu		"6502"
	.debuginfo	off

; jmp to rom
	jmp $8000
	

	



