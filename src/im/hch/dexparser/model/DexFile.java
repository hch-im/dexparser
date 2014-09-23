package im.hch.dexparser.model;

import im.hch.dexparser.Utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

public class DexFile extends Item{	
	Header header;
	StringId[] stringIds;
	TypeId[] typeIds;
	ProtoId[] protoIds;
	FieldId[] fieldIds;
	MethodId[] methodIds;
	ClassDef[] classIds;
	
	public DexFile(RandomAccessFile rafile) throws IOException {
		super(rafile, null);
		header = new Header(raf, this);
		if(header.isValid()){
			readStrings();
			readTypeIds();
			readProtoIds();
			readFieldIds();
			readMethodIds();
			readClassDefs();
		}
	}

	private void readStrings()throws IOException{
		raf.seek(header.stringIdsOffset);
		stringIds = new StringId[header.stringIdsSize];
		for(int i = 0; i < header.stringIdsSize; i++){
			stringIds[i] = new StringId(raf, this);
		}
		//read strings
		for(int i = 0; i < header.stringIdsSize; i++){
			stringIds[i].loadData();
		}			
	}
	
	private void readTypeIds()throws IOException{
		raf.seek(header.typeIdsOffset);
		typeIds = new TypeId[header.typeIdsSize];
		for(int i = 0; i < header.typeIdsSize; i++){
			typeIds[i] = new TypeId(raf, this);
		}
	}
	
	private void readProtoIds()throws IOException{
		raf.seek(header.protoIdsOffset);
		protoIds = new ProtoId[header.protoIdsSize];
		for(int i = 0; i < header.protoIdsSize; i++){
			protoIds[i] = new ProtoId(raf, this);
		}		
		//read the parameter type list 
		for(int i = 0; i < header.protoIdsSize; i++){
			protoIds[i].loadData();
		}
	}
	
	private void readFieldIds()throws IOException{
		raf.seek(header.fieldIdsOffset);
		fieldIds = new FieldId[header.fieldIdsSize];
		for(int i = 0; i < header.fieldIdsSize; i++){
			fieldIds[i] = new FieldId(raf, this);
		}		
	}	
	
	private void readMethodIds()throws IOException{
		raf.seek(header.methodIdsOffset);
		methodIds = new MethodId[header.methodIdsSize];
		for(int i = 0; i < header.methodIdsSize; i++){
			methodIds[i] = new MethodId(raf, this);
		}		
	}	
	
	private void readClassDefs()throws IOException{
		raf.seek(header.classIdsOffset);
		classIds = new ClassDef[header.classIdsSize];
		for(int i = 0; i < header.classIdsSize; i++){
			classIds[i] = new ClassDef(raf, this);
		}
		//load class data
		for(int i = 0; i < header.classIdsSize; i++){
			classIds[i].loadData();
		}
	}

	@Override
	public String toString() {
		if(header != null)
			return header.toString();
		return super.toString();
	}

	public void printStrings(PrintWriter pw, String regex){
		for(int i = 0; i < stringIds.length; i++){
			String str = stringIds[i].toString();
			if(regex == null || str.matches(regex)){
				pw.println(i + " : " + str);
			}
		}		
		pw.flush();
	}

	public void printTypes(PrintWriter pw, String regex){
		for(int i = 0; i < typeIds.length; i++){
			String str = typeIds[i].toString();
			if(regex == null || str.matches(regex)){
				pw.println(i + " : " + str);
			}			
		}		
		pw.flush();
	}
	
	public void printProtos(PrintWriter pw, String regex){
		for(int i = 0; i < protoIds.length; i++){
			String str = protoIds[i].toString();
			if(regex == null || str.matches(regex)){
				pw.println(i + " : " + str);
			}
		}		
		pw.flush();
	}

	public void printFields(PrintWriter pw, String regex){
		for(int i = 0; i < fieldIds.length; i++){
			String str = fieldIds[i].toString();
			if(regex == null || str.matches(regex)){
				pw.println(i + " : " + str);
			}
		}		
		pw.flush();
	}	
	
	public void printMethods(PrintWriter pw, String regex){
		for(int i = 0; i < methodIds.length; i++){
			String str = methodIds[i].toString();
			if(regex == null || str.matches(regex)){
				pw.println(str);
			}
		}		
		pw.flush();
	}	

	public void printClasses(PrintWriter pw, String regex){
		for(int i = 0; i < classIds.length; i++){
			String str = classIds[i].toString();
			if(regex == null || str.matches(regex)){
				pw.println(str);
			}
		}
		pw.flush();
	}	
	
	public void printStaticStrings(PrintWriter pw){
		for(int i = 0; i < classIds.length; i++){
			ClassDef id = classIds[i];
			if(id.staticValues != null){
				EncodedArray ea = id.staticValues;
				for(int j = 0; j < ea.elements.length; j++){
					EncodedValue val = ea.elements[j];
					if(val.type == EncodedValue.VALUE_STRING)
						pw.println(stringIds[(Integer)val.value].value);
				}
			}
		}		
		pw.flush();
	}
	
	static class Header extends Item{
		public static final byte[] MAGIC_NUMBER = 
			{'d', 'e', 'x', '\n', '0', '3', '5', '\0'};
		public static final int BIG_ENDIAN = 0x78563412;
		public static final int LITTLE_ENDIAN = 0x12345678;

		byte[] magic;
		int checksum;//unsigned int
		byte[] signature = null;
		int size;
		int headerSize;
		int endianTag;
		int linkSize;
		int linkOffset;
		int mapOffset;
		int stringIdsSize;
		int stringIdsOffset;
		int typeIdsSize;
		int typeIdsOffset;
		int protoIdsSize;
		int protoIdsOffset;
		int fieldIdsSize;
		int fieldIdsOffset;
		int methodIdsSize;
		int methodIdsOffset;
		int classIdsSize;
		int classIdsOffset;
		int dataSize;
		int dataOffset;
		
		boolean valid = false;
		
		Header(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dexFile);
			magic = readBytes(8);	
			valid = Utils.bytesEqual(magic, MAGIC_NUMBER, 8);
			checksum = readInt();
			signature = readBytes(20);
			size = readInt();
			headerSize = readInt();
			endianTag = readInt();
			linkSize = readInt();
			linkOffset = readInt();
			mapOffset = readInt();
			stringIdsSize = readInt();
			stringIdsOffset = readInt();
			typeIdsSize = readInt();
			typeIdsOffset = readInt();
			protoIdsSize = readInt();
			protoIdsOffset = readInt();
			fieldIdsSize = readInt();
			fieldIdsOffset = readInt();
			methodIdsSize = readInt();
			methodIdsOffset = readInt();
			classIdsSize = readInt();
			classIdsOffset = readInt();
			dataSize = readInt();
			dataOffset = readInt();			
		}	
		
		boolean isValid(){
			return valid;
		}
		
		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			
			buf.append("checksum: 0x").append(Integer.toHexString(checksum)).append('\n');
			buf.append("signature: ").append(Utils.bytesToHexString(signature)).append('\n');
			buf.append("size: ").append(size).append('\n');
			buf.append("header size: ").append(headerSize).append('\n');
			buf.append("little endian: ").append(endianTag == LITTLE_ENDIAN).append('\n');
			buf.append("link size: ").append(linkSize).append('\n');
			buf.append("link offset: ").append(linkOffset).append('\n');
			buf.append("map offset: ").append(mapOffset).append('\n');
			buf.append("string ids size: ").append(stringIdsSize).append('\n');
			buf.append("string ids offset: ").append(stringIdsOffset).append('\n');
			buf.append("type ids size: ").append(typeIdsSize).append('\n');
			buf.append("type ids offset: ").append(typeIdsOffset).append('\n');
			buf.append("proto ids size: ").append(protoIdsSize).append('\n');
			buf.append("proto ids offset: ").append(protoIdsOffset).append('\n');
			buf.append("field ids size: ").append(fieldIdsSize).append('\n');
			buf.append("field ids offset: ").append(fieldIdsOffset).append('\n');
			buf.append("method ids size: ").append(methodIdsSize).append('\n');
			buf.append("method ids offset: ").append(methodIdsOffset).append('\n');
			buf.append("class ids size: ").append(classIdsSize).append('\n');
			buf.append("class ids offset: ").append(classIdsOffset).append('\n');		
			buf.append("data size: ").append(dataSize).append('\n');
			buf.append("data offset: ").append(dataOffset).append('\n');
			
			return buf.toString();
		}	
	}	
	
	static class StringId extends Item{
		int offset;		//the offset of the string in the data section
		String value;	//the value of the string
		
		StringId(RandomAccessFile raf, DexFile dexFile) throws IOException {
			super(raf, dexFile);
			offset = readInt();
		}

		@Override
		void loadData() throws IOException {
			raf.seek(offset);
			value = readString();			
		}

		@Override
		public String toString() {
			return value;
		}		
	}	
	
	static class FieldId extends Item{
		int classIdx;	//dex.typeIds[classIdx]
		int typeIdx;	//dex.typeIds[typeIdx]
		int nameIdx;	//dex.stringIds[nameIdx]

		FieldId(RandomAccessFile raf, DexFile dexFile) throws IOException {
			super(raf, dexFile);
			classIdx = readShort();
			typeIdx = readShort();
			nameIdx = readInt();
		}

		TypeId getClassType(){
			return dex.typeIds[classIdx];
		}
		
		TypeId getType(){
			return dex.typeIds[typeIdx];
		}
		
		String getName(){
			return dex.stringIds[nameIdx].value;
		}
		
		@Override
		public String toString() {
			TypeId type = getType();
			return type.getTypeLong() + " " + getName();
		}		
	}	

	/**
	 * TypeDescriptor Semantics
	 * 
	 * V	void; only valid for return types
	 * Z	boolean
	 * B	byte
	 * S	short
	 * C	char
	 * I	int
	 * J	long
	 * F	float
	 * D	double
	 * Lfully/qualified/Name;	the class fully.qualified.Name
	 * [descriptor	array of descriptor, usable recursively for 
	 * 		arrays-of-arrays, though it is invalid to have more 
	 * 		than 255 dimensions.
	 */
	static class TypeId extends Item{
		int descriptorIdx;	//dex.stringIds[descriptorIdx]

		TypeId(RandomAccessFile raf, DexFile dexFile) throws IOException {
			super(raf, dexFile);
			this.descriptorIdx = readInt();
		}	
		
		String getTypeShorty(){
			return dex.stringIds[descriptorIdx].value;
		}

		String getTypeLong(){
			return decodeDescriptor(getTypeShorty());
		}
		
		@Override
		public String toString() {
			return getTypeLong();
		}		
	}	
	
	static class MethodId extends Item{
		int classIdx;//dex.typeIds[classIdx]
		int protoIdx;//dex.protoIds[protoIdx]
		int nameIdx;//dex.stringIds[nameIdx]

		MethodId(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dexFile);
			classIdx = readShort();
			protoIdx = readShort();
			nameIdx = readInt();
		}
		
		TypeId getClassType(){
			return dex.typeIds[classIdx];
		}
		
		String getName(){
			return dex.stringIds[nameIdx].value;
		}
		
		ProtoId getProto(){
			return dex.protoIds[protoIdx];
		}

		@Override
		public String toString() {
			ProtoId proto = getProto();
			StringBuffer buf = new StringBuffer();
			buf.append(proto.getReturnType()).append(" ")
			.append(getName()).append('(');
			if(proto.parameterTypeList != null)
				for(int i = 0; i < proto.parameterTypeList.length; i++){
					TypeId type = dex.typeIds[proto.parameterTypeList[i]];
					if(i > 0) buf.append(", ");
					buf.append(type.toString());
				}
			buf.append(')');
			return buf.toString();
		}		
	}	
	
	static class ProtoId extends Item{
		int shortyIdx;//dex.stringIds[shortyIdx]
		int returnTypeIdx;//dex.typesIds[returnTypeIdx]
		int parametersOffset;
		short[] parameterTypeList;

		ProtoId(RandomAccessFile raf, DexFile dexFile) throws IOException {
			super(raf, dexFile);
			shortyIdx = readInt();
			returnTypeIdx = readInt();
			parametersOffset = readInt();		
		}

		@Override
		void loadData() throws IOException {
			if(parametersOffset > 0)//has parameter
				parameterTypeList = readTypeList(parametersOffset);
		}		
		
		String getShortyDescription(){
			return dex.stringIds[shortyIdx].value;
		}
		
		TypeId getReturnType(){
			return dex.typeIds[returnTypeIdx];
		}

		@Override
		public String toString() {
			return getShortyDescription();
		}
				
	}	
}
