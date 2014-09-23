package im.hch.dexparser.model;

import java.io.IOException;
import java.io.RandomAccessFile;

class Instruction extends Item{
	Operator opcode;
	Operand operandA;
	Operand operandB;
	Operand operandC;
	
	Instruction(RandomAccessFile file, DexFile dexFile) throws IOException {
		super(file, dexFile);
		opcode = new Operator(raf, dex);
		byte b;
		switch(opcode.value){
		case 0x00://ØØ|op   nop
		case 0x0e://		return-void	
			readByte();
			break;
		case 0x01://B|A|op 	move vA, vB;
		case 0x04://		move-wide vA, vB
		case 0x07://		move-object vA, vB
		case 0x21://		array-length vA, vB
			b = readByte();
			operandA = new Operand(raf, dex, b & 0x0F, 1);
			operandB = new Operand(raf, dex, (b & 0x0F) >> 4, 1);			
			break;
		case 0x02://AA|op BBBB 	move/from16 vAA, vBBBB
		case 0x05://			move-wide/from16 vAA, vBBBB			
		case 0x08://			move-object/from16 vAA, vBBBB
			operandA = new Operand(raf, dex, readByte(), 1);
			operandB = new Operand(raf, dex, readShort(), 1);						
			break;
		case 0x03://ØØ|op AAAA BBBB		move/16 vAAAA, vBBBB
		case 0x06://					move-wide/16 vAAAA, vBBBB
		case 0x09://					move-object/16 vAAAA, vBBBB
			readByte();
			operandA = new Operand(raf, dex, readShort(), 1);
			operandB = new Operand(raf, dex, readShort(), 1);									
			break;
		case 0x0a://AA|op		move-result vAA
		case 0x0b://			move-result-wide vAA
		case 0x0c://			move-result-object vAA
		case 0x0d://			move-exception vAA
		case 0x0f://			return vAA
		case 0x10://			return-wide vAA
		case 0x11://			return-object vAA
			operandA = new Operand(raf, dex, readByte(), 1);			
			break;
		case 0x12://B|A|op		const/4 vA, #+B
			b = readByte();
			operandA = new Operand(raf, dex, b & 0x0F, 1);
			operandB = new Operand(raf, dex, (b & 0x0F) >> 4, 2);	
		case 0x13://AA|op BBBB		const/16 vAA, #+BBBB
			operandA = new Operand(raf, dex, readByte(), 1);
			operandB = new Operand(raf, dex, readShort(), 2);									
			break;
		case 0x14://AA|op BBBBlo BBBBhi 	const vAA, #+BBBBBBBB
		case 0x17://						const-wide/32 vAA, #+BBBBBBBB			
			operandA = new Operand(raf, dex, readByte(), 1);
			operandB = new Operand(raf, dex, readInt(), 2);												
			break;
		case 0x15://AA|op BBBB		const/high16 vAA, #+BBBB0000
			operandA = new Operand(raf, dex, readByte(), 1);
			operandB = new Operand(raf, dex, (readShort() & 0xFFFF) << 16, 2);															
			break;
		case 0x16://AA|op BBBB		const/high16 vAA, #+BBBB
			operandA = new Operand(raf, dex, readByte(), 1);
			operandB = new Operand(raf, dex, readShort(), 2);															
			break;			
		case 0x18://AA|op BBBBlo BBBB BBBB BBBBhi		const-wide vAA, #+BBBBBBBBBBBBBBBB
			operandA = new Operand(raf, dex, readByte(), 1);
			operandB = new Operand(raf, dex, readLong(), 2);																		
			break;
		case 0x19://AA|op BBBB 		const-wide/high16 vAA, #+BBBB000000000000
			operandA = new Operand(raf, dex, readByte(), 1);
			operandB = new Operand(raf, dex, ((long)(readShort() & 0xFFFF)) << 48, 2);															
			break;		
		case 0x1a://AA|op BBBB		const-string vAA, string@BBBB
			operandA = new Operand(raf, dex, readByte(), 1);
			operandB = new Operand(raf, dex, readShort(), 4);																		
			break;
		case 0x1b://AA|op BBBBlo BBBBhi		const-string/jumbo vAA, string@BBBBBBBB
			operandA = new Operand(raf, dex, readByte(), 1);
			operandB = new Operand(raf, dex, readInt(), 4);																					
			break;
		case 0x1c://AA|op BBBB		const-class vAA, type@BBBB
		case 0x1f://				check-cast vAA, type@BBBB
		case 0x22://				new-instance vAA, type@BBBB
			operandA = new Operand(raf, dex, readByte(), 1);
			operandB = new Operand(raf, dex, readShort(), 5);																		
			break;
		case 0x1d://AA|op		monitor-enter vAA
		case 0x1e://			monitor-exit vAA
			operandA = new Operand(raf, dex, readByte(), 1);			
			break;
		case 0x20://B|A|op CCCC		instance-of vA, vB, type@CCCC
		case 0x23://				new-array vA, vB, type@CCCC
			b = readByte();
			operandA = new Operand(raf, dex, b & 0x0F, 1);
			operandB = new Operand(raf, dex, (b & 0x0F) >> 4, 1);			
			operandC = new Operand(raf, dex, readShort(), 5);						
			break;
		case 0x24://A|G|op BBBB F|E|D|C		filled-new-array {vC, vD, vE, vF, vG}, type@BBBB
			//TODO
			break;
		}
	}
	
	@Override
	public String toString() {
		String str = opcode.toString();
		if(operandA != null) str += " " + operandA.toString();
		if(operandB != null) str += "," + operandB.toString();
		return str;
	}

	static class Operator extends Item{
		byte value;
		Operator(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dexFile);
			value = readByte();
		}
		@Override
		public String toString() {
			switch(value){
			case 0x00:
				return "nop";
			case 0x01:
				return "move";
			case 0x02:
				return "move/from16";
			default:
				return "unknown";
			}
		}
		
	}
	
	static class Operand extends Item{
		Object value;
		int type;//1: v 2: #+ 3:+ 4:string@ 5:type@
		
		Operand(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dexFile);			
		}
		
		Operand(RandomAccessFile file, DexFile dexFile, Object value, int type) throws IOException {
			super(file, dexFile);
			this.value = value;
			this.type = type;
		}

		@Override
		public String toString() {
			switch(type){
			case 1:
				return "v" + value;
			default:
				return "unknown";
			}
		}		
	}
}
