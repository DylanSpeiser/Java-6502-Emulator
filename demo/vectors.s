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
  	
    	
.addr      $AABB
.addr      $0200       ; Reset vector
.addr      $CCDD
.addr      $EEFF
