public class Instruction {
	public String opcode;
	public String addressMode;
	public int cycles;
	
	public Instruction(String opcode, String addressMode, int cycles) {
		this.opcode = opcode;
		this.addressMode = addressMode;
		this.cycles = cycles;
	}
	
	@Override
	public String toString() {
		return opcode+","+addressMode;
	}
}
