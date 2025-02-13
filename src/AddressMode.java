public enum AddressMode {
	ACC,    // OPC A or OPC			accumulator (implied)				
	ABS,    // OPC $LLHH			absolute							
	ABX,    // OPC $LLHH,X			absolute, x-indexed					
	ABY,    // OPC $LLHH,Y			absolute, y-indexed					
	IMM,    // OPC #$BB				immediate							
	IMP,    // OPC					implied								
	IND,    // OPC ($LLHH)			indirect							
	IZX,    // OPC ($LL,X)			indirect zeropage, x-indexed		
	IZY,    // OPC ($LL),Y			indirect zeropage, y-indexd			
	REL,    // OPC $BB				relative							
	ZPP,    // OPC $LL				zeropage							
	ZPX,    // OPC $LL,X			zeropage, x-indexed					
	ZPY,    // OPC $LL,Y			zeropage, y-indexed					

	// WDC 65c02 ADDITIONS //

	ZPI,    // OPC $LLHH			zeropage indirect					
	IAX,    // OPC ($LLHH,X)		indirect, x-indexed					
}
