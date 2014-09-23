package im.hch.dexparser.model;

import im.hch.dexparser.Utils;
import im.hch.dexparser.model.DexFile.TypeId;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class Item {
	public static final int ACC_PUBLIC	= 0x1;	//public
	public static final int ACC_PRIVATE	= 0x2;	//private
	public static final int ACC_PROTECTED =	0x4;//protected
	public static final int ACC_STATIC = 0x8;	//static
	public static final int ACC_FINAL = 0x10;	//final
	public static final int ACC_SYNCHRONIZED = 0x20;//synchronized
	public static final int ACC_VOLATILE = 0x40;//volatile	 
	public static final int ACC_BRIDGE = 0x40;//bridge method
	public static final int ACC_TRANSIENT = 0x80;//transient	 
	public static final int ACC_VARARGS	= 0x80;//
	public static final int ACC_NATIVE = 0x100;//native
	public static final int ACC_INTERFACE = 0x200;//interface	 	 
	public static final int ACC_ABSTRACT = 0x400;//abstract
	public static final int ACC_STRICT = 0x800;//strictfp
	public static final int ACC_SYNTHETIC = 0x1000;//
	public static final int ACC_ANNOTATION	= 0x2000;//annotation	 	 
	public static final int ACC_ENUM = 0x4000;//enumerated	 
//	public static final int (unused)	0x8000	 	 	 
	public static final int ACC_CONSTRUCTOR	= 0x10000;//constructor method
	public static final int ACC_DECLARED_SYNCHRONIZED = 0x20000;//synchronized
	
	static RandomAccessFile raf;
	static DexFile dex;
	
	Item(RandomAccessFile file, DexFile dexFile) throws IOException{		
		raf = file;
		dex = dexFile;
	}
	
	void loadData()throws IOException{
		//implemented by items that contains offsets
	}
	
	long readLong()throws IOException{
		long val = 0;
		for(int i = 0; i < 8; i++){
			val |= (long)(raf.readByte() & 0xff) << (i * 8);
		}				
		return val;
	}
	
	int readInt()throws IOException{
		int val = 0;
		for(int i = 0; i < 4; i++){
			val |= (int)(raf.readByte() & 0xff) << (i * 8);
		}				
		return val;
	}
	
	short readShort()throws IOException{
		short val = 0;
		for(int i = 0; i < 2; i++){
			val |= (short)(raf.readByte() & 0xff) << (i * 8);
		}				
		return val;
	}	
	
	char readChar()throws IOException{
		char val = 0;
		for(int i = 0; i < 2; i++){
			val |= (char)(raf.readByte() & 0xff) << (i * 8);
		}				
		return val;
	}
	
	byte readByte()throws IOException{
		return raf.readByte();
	}	
	
	LEB128 readLEB128()throws IOException{
		byte[] bytes = new byte[5];
		int len = 0;
		bytes[len++] = raf.readByte();
		while((bytes[len - 1] & 0x80) != 0){
			bytes[len++] = raf.readByte();
		}		
		return new LEB128(Arrays.copyOfRange(bytes, 0, len));
	}
	
	String readString() throws IOException {
		int utf16Size = readLEB128().toULEB128().toInt();
		byte[] buffer = new byte[utf16Size * 3];
		int i;
		for(i = 0; i < buffer.length; i++){
			buffer[i] = raf.readByte();
			if(buffer[i] == 0) 
				break;
		}
		return new String(buffer, 0, i, "UTF-8");
	}
	   
	byte[] readBytes(int len)throws IOException{
		byte[] bytes = new byte[len];
		raf.read(bytes, 0, len);
		return bytes;
	}
	
	short[] readTypeList(int offset)throws IOException{
		raf.seek(offset);
		int size = readInt();
		short[] types = new short[size];
		for(int i = 0; i < size; i++)
			types[i] = readShort();
		return types;
	}		
	
	String decodeDescriptor(String shorty){
		switch(shorty.charAt(0)){
			case 'V':
				return "void";
			case 'Z':
				return "boolean";
			case 'B':
				return "byte";
			case 'S':
				return "short";
			case 'C':
				return "char";
			case 'I':
				return "int";
			case 'J':
				return "long";
			case 'F':
				return "float";
			case 'D':
				return "double";
			case 'L':
				return shorty.substring(1, shorty.length() - 1).replace('/', '.');
			case '[':
				return decodeDescriptor(shorty.substring(1)) + "[]";
			default:
				return "unknown";
		}
	}
	
	static class EncodedArray extends Item{
		ULEB128 size;
		EncodedValue[] elements;
		
		EncodedArray(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dex);
			size = readLEB128().toULEB128();
			int count = size.toInt();
			elements = new EncodedValue[count];
			for(int i = 0; i < count; i++)
				elements[i] = new EncodedValue(raf, dex);
		}

		@Override
		public String toString() {
			if(elements == null || elements.length == 0)
				return "";
			StringBuffer buf = new StringBuffer("\t#EncodedArray\n");
			for(EncodedValue ev : elements){
				buf.append("\t").append(ev.toString()).append("\n");
			}
			return buf.toString();
		}
				
	}	
	
	static class EncodedValue extends Item{
		int type;
		Object value;
		
		EncodedValue(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dex);
			byte b = readByte();
			type = b & 0x1f;
			int len = ((b & 0xff) >> 5) + 1;		
			byte[] bytes;
			switch(type){
			case EncodedValue.VALUE_BYTE:
				bytes = readBytes(len);
				value = Utils.bytesToByte(bytes);
				break;
			case EncodedValue.VALUE_SHORT:
				bytes = readBytes(len);
				value = Utils.bytesToShort(bytes);
				break;
			case EncodedValue.VALUE_CHAR:
				bytes = readBytes(len);
				value = Utils.bytesToChar(bytes);
				break;
			case EncodedValue.VALUE_LONG:
				bytes = readBytes(len);
				value = Utils.bytesToLong(bytes);
				break;
			case EncodedValue.VALUE_FLOAT:
				bytes = readBytes(len);
				value = Utils.bytesToFloat(bytes);
				break;
			case EncodedValue.VALUE_DOUBLE:
				bytes = readBytes(len);
				value = Utils.bytesToDouble(bytes);
				break;
			case EncodedValue.VALUE_INT:
				bytes = readBytes(len);
				value = Utils.bytesToInt(bytes);
				break;
			case EncodedValue.VALUE_STRING:
			case EncodedValue.VALUE_TYPE:
			case EncodedValue.VALUE_FIELD:
			case EncodedValue.VALUE_METHOD:
			case EncodedValue.VALUE_ENUM:
				bytes = readBytes(len);
				value = Utils.bytesToInt(bytes);
				break;
			case EncodedValue.VALUE_ARRAY:
				value = new EncodedArray(raf, dex);
				break;
			case EncodedValue.VALUE_ANNOTATION:
				value = new EncodedAnnotation(raf, dex);
				break;
			case EncodedValue.VALUE_NULL:
				value = null;
				break;
			case EncodedValue.VALUE_BOOLEAN:			
				value = ((b & 0xe0) >> 5) == 1;
				break;
			default:
				value = -1;
				System.out.println("Unknow encoded value type for: " + type);
				break;
			}
		}	
		
		String getType(){
			switch(type){
			case EncodedValue.VALUE_BYTE:
				return "byte";
			case EncodedValue.VALUE_SHORT:
				return "short";
			case EncodedValue.VALUE_CHAR:
				return "char";
			case EncodedValue.VALUE_LONG:
				return "long";
			case EncodedValue.VALUE_FLOAT:
				return "float";
			case EncodedValue.VALUE_DOUBLE:
				return "double";
			case EncodedValue.VALUE_INT:
				return "int";
			case EncodedValue.VALUE_STRING:
				return "string";				
			case EncodedValue.VALUE_TYPE:
				return "type_id";
			case EncodedValue.VALUE_FIELD:
				return "field_id";
			case EncodedValue.VALUE_METHOD:
				return "method_id";				
			case EncodedValue.VALUE_ENUM:
				return "enum";
			case EncodedValue.VALUE_ARRAY:
				return "array";
			case EncodedValue.VALUE_ANNOTATION:
				return "annotation";
			case EncodedValue.VALUE_NULL:
				return "null";
			case EncodedValue.VALUE_BOOLEAN:			
				return "boolean";
			default:
				return "unknown";
			}			
		}
				
		@Override
		public String toString() {
			switch(type){
			case EncodedValue.VALUE_BYTE:
			case EncodedValue.VALUE_SHORT:
			case EncodedValue.VALUE_CHAR:
			case EncodedValue.VALUE_LONG:
			case EncodedValue.VALUE_FLOAT:
			case EncodedValue.VALUE_DOUBLE:
			case EncodedValue.VALUE_INT:
			case EncodedValue.VALUE_BOOLEAN:			
				return getType() + " " + value;
			case EncodedValue.VALUE_STRING:
				return "string " + dex.stringIds[(Integer)value];
			case EncodedValue.VALUE_TYPE:
				return "type_id " + dex.typeIds[(Integer)value];
			case EncodedValue.VALUE_FIELD:
				return "field_id " + dex.fieldIds[(Integer)value];
			case EncodedValue.VALUE_METHOD:
				return "method_id " + dex.methodIds[(Integer)value];				
			case EncodedValue.VALUE_ENUM:
				return "enum " + dex.fieldIds[(Integer)value];
			case EncodedValue.VALUE_ARRAY:
				return "array " + value.toString();
			case EncodedValue.VALUE_ANNOTATION:
				return "annotation " + value.toString();
			case EncodedValue.VALUE_NULL:
				return "null";
			default:
				return "";
			}			
		}

		public static final int VALUE_BYTE	= 0x00;
		public static final int VALUE_SHORT	= 0x02;
		public static final int VALUE_CHAR	= 0x03;
		public static final int VALUE_INT	= 0x04;
		public static final int VALUE_LONG	= 0x06;
		public static final int VALUE_FLOAT	= 0x10;
		public static final int VALUE_DOUBLE = 0x11;
		public static final int VALUE_STRING = 0x17;
		public static final int VALUE_TYPE = 0x18;
		public static final int VALUE_FIELD	= 0x19;
		public static final int VALUE_METHOD = 0x1a;
		public static final int VALUE_ENUM	= 0x1b;
		public static final int VALUE_ARRAY	= 0x1c;
		public static final int VALUE_ANNOTATION = 0x1d;
		public static final int VALUE_NULL	= 0x1e;
		public static final int VALUE_BOOLEAN	= 0x1f;	
	}
	
	static class EncodedAnnotation extends Item{
		ULEB128 typeIdx;
		ULEB128 size;
		AnnotationElement[] elements;
		
		EncodedAnnotation(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dex);
			typeIdx = readLEB128().toULEB128();
			size = readLEB128().toULEB128();
			int count = size.toInt();
			elements = new AnnotationElement[count];		
			for(int i = 0; i < count; i++){
				elements[i] = new AnnotationElement(raf, dex);
			}		
		}

		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			TypeId type = dex.typeIds[typeIdx.toInt()];
			buf.append(type.getTypeLong()).append("\n");
			
			for(AnnotationElement ae : elements){
				buf.append("\t").append(ae.toString()).append("\n");
			}
			return buf.toString();
		}
				
	}	
	
	static class AnnotationElement extends Item{
		ULEB128 nameIdx;
		EncodedValue value;
		
		AnnotationElement(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dex);
			nameIdx = readLEB128().toULEB128();
			value = new EncodedValue(raf, dex);
		}

		@Override
		public String toString() {			
			return dex.stringIds[nameIdx.toInt()].value + " " + value.toString();
		}	
		
		
	}	
}
