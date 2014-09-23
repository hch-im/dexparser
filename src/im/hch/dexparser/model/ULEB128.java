package im.hch.dexparser.model;

public class ULEB128 extends LEB128{

	public ULEB128(byte[] bytes) {
		super(bytes);
	}

	@Override
	public int toInt(){
		int result = 0, shift = 0;
		for(int i = 0; i < data.length; i++, shift += 7){
			result |= (data[i] & 0x7f) << shift;
		}		
		return result;
	}	
	
	@Override
	public String toString() {
		return toInt() + "";
	}		
}
