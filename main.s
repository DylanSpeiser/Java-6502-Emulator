
	.setcpu		"6502"
	.debuginfo	off

; push fake return address
	lda     #$FF
	pha 
	pha
; test some instructions
	ldx     #$A0
	lda     #65
	ldy     #$C1
	dex
	inx
	inx
; put text on display device
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
	inx
	iny
	tya
	sta     $A002,X
; return back to start
	rts

	



