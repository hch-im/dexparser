package im.hch.dexparser;

public class Utils {
	public static boolean bytesEqual(byte[] a, byte[] b, int len){
		if(a == null && b == null) return true;
		if(a == null || b == null) return false;
		if(a.length < len || b.length < len) return false;
		
		for(int i = 0; i < len; i++)
			if(a[i] != b[i])
				return false;
		
		return true;
	}

	public static long bytesToByte(byte[] bytes){
		if(bytes == null || bytes.length == 0)
			return 0;
		return bytes[0];
	}
	
	public static long bytesToShort(byte[] bytes){
		if(bytes == null || bytes.length == 0)
			return 0;
		short val = 0;
		for(int i = 0; i < 2 && i < bytes.length; i++){
			val |= (short)(bytes[i] & 0xff) << (i * 8);
		}		
		return val;
	}
	
	public static long bytesToChar(byte[] bytes){
		if(bytes == null || bytes.length == 0)
			return 0;
		char val = 0;
		for(int i = 0; i < 2 && i < bytes.length; i++){
			val |= (char)(bytes[i] & 0xff) << (i * 8);
		}		
		return val;
	}
	
	public static int bytesToInt(byte[] bytes){
		if(bytes == null || bytes.length == 0)
			return 0;
		int val = 0;
		for(int i = 0; i < 4 && i < bytes.length; i++){
			val |= (int)(bytes[i] & 0xff) << (i * 8);
		}		
		return val;
	}
	
	public static long bytesToLong(byte[] bytes){
		if(bytes == null || bytes.length == 0)
			return 0;
		long val = 0;
		for(int i = 0; i < 8 && i < bytes.length; i++){
			val |= (long)(bytes[i] & 0xff) << (i * 8);
		}		
		return val;
	}

	public static  float bytesToFloat(byte[] bytes){
		int val = bytesToInt(bytes);
		return Float.intBitsToFloat(val);
	}
	
	public static  double bytesToDouble(byte[] bytes){
		long val = bytesToLong(bytes);
		return Double.longBitsToDouble(val);
	}	
	
	public static String bytesToHexString(byte[] bytes){
		if(bytes == null) return "";
		
		StringBuffer buf = new StringBuffer();
		int val;
		for(int i = 0; i < bytes.length; i++){
			val = (bytes[i] & 0xf0) >> 4;
			buf.append(chars.charAt(val));
			val = bytes[i] & 0x0f;
			buf.append(chars.charAt(val));
		}
			
		return buf.toString();
	}
	
	private static final String chars = "0123456789abcdef";
}
