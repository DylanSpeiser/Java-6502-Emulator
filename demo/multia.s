;
;ca65 multia.s -D BASE=0x0000
;cl65 --config multia.cfg  multia.o --target none --start-addr 0x0000 -o multia.bin;
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

.addr      _nmi_int    ; NMI vector
.addr      _init       ; Reset vector
.addr      _irq_int    ; IRQ/BRK vector
;.addr      _break    ; IRQ/BRK vector
    	
.segment  "CODE"


_init:
	CLI
_init2:
; push return address 0x0000 minus 1 = 0xFFFF 
;	lda     #$FF
;	pha
;	lda     #$01
;	pha
; set IRQ
;	SEI
;	lda #<_irq_int
;    sta     $FFFE
;    lda #>_irq_int
;	sta     $FFFF

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

	jmp     task_1

task_1:
	ldx     #80
	ldy     #65
task_1_loop:
	inx
	iny
	tya
	sta     $A004,X
	CMP 	#126
	BNE task_1_loop
; return back to start
	jmp 	task_1


task_2:
	ldx     #80
	ldy     #32
task_2_loop:
	inx
	iny
	tya
	sta     $A004,X
	CMP 	#126
	BNE task_2_loop

; return back to start
	jmp 	task_2


_nmi_int:
	rti

_break:
	pha
	phx
	phy
	lda     #'@'
	sta     $A003
	ply
	plx
	pla
	RTI

_irq_int:

	pha
	phx
	phy
	lda     #'X'
	sta     $A000
	lda     #'Z'
	sta     $A001
	ply
	plx
	pla
	RTI



