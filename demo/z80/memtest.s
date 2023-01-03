;Memory testing program 
;
; pasmo memtest.s memtest.bin
;
display_memory:	equ	0xA000
			org	0x0000			;start of RAM, config. 0
			;call	write_newline
			ld  ix,0x0041
			ld  iy,0x001A 
			ld	a,0x00			;first test pattern
			ld	hl,display_memory	;start of test, config 0
loop_1:		
            ld	a, ixl          ;get test byte          
			ld	(hl), a			;store test byte
			inc	hl			;inc counter
			inc ix
			dec iy
			ld b, 0x00
			ld a, iyl
			cp b
			jp	nz,loop_1

			ld  ix,0x0061
			ld  iy,0x001A 
			ld	a,0x00			;first test pattern
			ld	hl,display_memory	;start of test, config 0
loop_2:		
            ld	a, ixl          ;get test byte          
			ld	(hl), a			;store test byte
			inc	hl			;inc counter
			inc ix
			dec iy
			ld b, 0x00
			ld a, iyl
			cp b
			jp	nz,loop_2



			jp 0x0000		;start of test loop
