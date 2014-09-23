package im.hch.dexparser.model;

public class LEB128 {
	byte[] data;

	public LEB128(byte[] bytes){
		data = bytes;
	}
	
	public ULEB128 toULEB128(){
		return new ULEB128(data);
	}
	
	public ULEB128P1 toULEB128P1(){
		return new ULEB128P1(data);
	}	

	public int toInt(){
		int result = 0, shift = 0, i;
		for(i = 0; i < data.length; i++, shift += 7){
			result |= (data[i] & 0x7f) << shift;			
		}
		if((data[i - 1] & 0x40) != 0 && shift < 32)
			result |= -(1 << shift);
		return result;
	}

	@Override
	public String toString() {
		return toInt() + "";
	}	
}
