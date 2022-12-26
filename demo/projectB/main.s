;
;ca65 main.s -D BASE=0x0000
;cl65 --config main.cfg  main.o --target none --start-addr 0x0000 -o main.bin;
;
	.setcpu		"65C02"
	.debuginfo	off
    	
    	
;[0000:A000] TEXT-DISPLAY
;[0000:A190] TEXT-COLORPAGE
;[0000:A320] TEXT-PALETTEPORT

;[0000:A000] TEXT-DISPLAY
;[0000:A7D0] TEXT-COLORPAGE
;[0000:AFA0] TEXT-PALETTEPORT
  	
    	
.segment  "VECTORS"

;.addr      _nmi_int    ; NMI vector
;.addr      _init       ; Reset vector
.addr       _irq_int    ; IRQ/BRK vector    	
    	
.segment  "CODE"

; push return address 0x0000 minus 1 = 0xFFFF 

	;lda     #$AA
	;lda     #$AA
	;lda     #$AA
	lda     #$FF

	pha 
	pha
; set IRQ
	SEI	
	lda #<_irq_int
    sta     $FFFE
    lda #>_irq_int	
	sta     $FFFF
	CLI
; test some instructions
	ldx     #$A0
	lda     #65
	ldy     #$C1
	dex
	inx
	inx

; push data
    lda     #$AA
	pha	
    lda     #$BB
	pha	
    lda     #$CC
	pha	
    lda     #$DD
	pha	
    lda     #$AA
	pha	
    lda     #$BB
	pha	
    lda     #$CC
	pha	
    lda     #$DD
	pha	
	
	pla
	pla
	pla
	pla
	pla
	pla
	pla
	pla

; move data
	lda     #$AB
	sta     $00F8
	lda     #$CD
	sta     $00F9
	lda     #$EF
	sta     $00FA
; put text on display device
    lda     #65
	sta     $A000
	lda     #66
	sta     $A001
	ldy     #67
	ldx     #00
	tya
	sta     $A002,X
	inx
	iny
	tya
	sta     $A002,X
	
	ldx     #80
	ldy     #32		
loop2:
	inx
	iny
	tya
	sta     $A002,X
	CMP 	#126
	BNE loop2	
	
	ldx     #80
	lda     #32		
loop3:
	sta     $A002,X
	inx
	cpx 	#$0F
	BNE loop3		


	
; print to LCD
	lda     #$00
	sta     $B000
	lda     #$0F
	sta     $B001
	lda     #$01
	sta     $B000
	lda     #'A'
	sta     $B001
; return back to start
	rts

_irq_int:
		PHA
		lda     #'A'
		sta     $00e8
		lda     #'B'
		sta     $00e9
; ---------------------------------------------------------------------------
; IRQ detected, return

irq:
		    PLA
		    RTI                    ; Return from all IRQ interrupts
