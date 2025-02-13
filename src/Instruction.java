public class Instruction {
	public OpCode opcode;
	public AddressMode addressMode;
	public int cycles;
	public boolean wdc; // WDC extensions (65c02)
	
	public Instruction(OpCode opcode, AddressMode addressMode, int cycles, boolean wdc) {
		this.opcode = opcode;
		this.addressMode = addressMode;
		this.cycles = cycles;
		this.wdc = wdc;
	}
	
	@Override
	public String toString() {
		return opcode+","+addressMode;
	}
}

