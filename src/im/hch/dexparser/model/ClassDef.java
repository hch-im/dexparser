package im.hch.dexparser.model;

import im.hch.dexparser.model.DexFile.TypeId;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ClassDef extends Item{
	int classIdx;//dex.typeIds[classIdx]
	int accessFlags;//getAccessFlay(accessFlags)
	int superClassIdx;//dex.typeIds[classIdx]
	int interfacesOffset;
	int sourceFileIdx;//dex.stringIdx[sourceFileIdx]
	int annotationsOffset;
	int classDataOffset;
	int staticValuesOffset;
	
	short[] interfaceTypeList;
	short[] annotationTypeList;
	ClassData data;
	AnnotationsDirectory annotationsDirectory;
	EncodedArray staticValues;
	
	public static final int NO_INDEX = 0xffffffff;
			
	ClassDef(RandomAccessFile file, DexFile dexFile) throws IOException {
		super(file, dexFile);
		classIdx = readInt();
		accessFlags = readInt();
		superClassIdx = readInt();
		interfacesOffset = readInt();
		sourceFileIdx = readInt();
		annotationsOffset = readInt();			
		classDataOffset = readInt();
		staticValuesOffset = readInt();
	}	
	
	@Override
	void loadData()throws IOException{
		if(interfacesOffset > 0){
			interfaceTypeList = readTypeList(interfacesOffset);
		}				
		if(classDataOffset > 0){
			raf.seek(classDataOffset);
			data = new ClassData(raf, dex);
			data.loadData();
		}
		if(annotationsOffset > 0){
			raf.seek(annotationsOffset);
			annotationsDirectory = new AnnotationsDirectory(raf, dex);
			annotationsDirectory.loadData();
		}
		if(staticValuesOffset > 0){
			raf.seek(staticValuesOffset);
			staticValues = new EncodedArray(raf, dex);						
		}		
	}
	
	TypeId getClassType(){
		return dex.typeIds[classIdx];
	}

	TypeId getInterfaceType(int idx){
		if(interfaceTypeList == null)
			return null;
		return dex.typeIds[interfaceTypeList[idx]];
	}
	
	String getClassName(){
		TypeId type = getClassType();
		return type.getTypeLong();
	}
	
	TypeId getSuperClassType(){
		if(superClassIdx == NO_INDEX)
			return null;
		return dex.typeIds[superClassIdx];
	}
	
	String getSuperClassName(){
		TypeId type = getSuperClassType();
		if(type == null)
			return null;
		return type.getTypeLong();
	}
	
	String getSourceFile(){
		if(sourceFileIdx == NO_INDEX)
			return null;
		return dex.stringIds[sourceFileIdx].value;
	}
	
	String getAccessFlag(){
		switch(accessFlags){
			case ACC_PUBLIC:
				return "public";
			case ACC_PRIVATE:
				return "private";
			case ACC_PROTECTED:
				return "protected";
			case ACC_STATIC:
				return "static";
			case ACC_FINAL:
				return "final";
			case ACC_INTERFACE:
				return "interface";	 	 
			case ACC_ABSTRACT:
				return "abstract";
			case ACC_SYNTHETIC:
				return "synthetic";
			case ACC_ANNOTATION:
				return "annotation";	 	 
			case ACC_ENUM:
				return "enum";	 
			default:
				return "";
		}
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(".class ").append(getAccessFlag()).
		append(" ").append(getClassName());
		if(superClassIdx != NO_INDEX)	
			buf.append(" extends ").append(getSuperClassName());
		if(interfaceTypeList != null && interfaceTypeList.length > 0){
			buf.append(" implements ");
			for(int i = 0; i < interfaceTypeList.length; i++){
				buf.append(getInterfaceType(i).getTypeLong() + ",");
			}
			buf.deleteCharAt(buf.length() - 1);
		}
		buf.append("\n");
		
		if(sourceFileIdx != NO_INDEX)
			buf.append(".source ").append(getSourceFile()).append("\n");
		
		if(annotationsDirectory != null){
			buf.append("#annotations directory\n");
			buf.append(annotationsDirectory.toString()).append("\n");
		}
		
		if(staticValues != null){
			buf.append("#static fields\n");
			buf.append(staticValues.toString()).append("\n");
		}
		
		if(data != null){
			buf.append("#class data\n");
			buf.append(data.toString()).append("\n");
		}
		return buf.toString();
	}

	static class ClassData extends Item{
		ULEB128 staticFieldsSize;
		ULEB128 instanceFieldsSize;
		ULEB128 directMethodsSize;//static, private, or constructor method
		ULEB128 virtualMethodsSize;//non static, private or constructor method
		EncodedField[] staticFields = null;
		EncodedField[] instanceFields = null;
		EncodedMethod[] directMethods = null;
		EncodedMethod[] virtualMethods = null;

		ClassData(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dexFile);
			
			staticFieldsSize = readLEB128().toULEB128();
			instanceFieldsSize = readLEB128().toULEB128();
			directMethodsSize = readLEB128().toULEB128();
			virtualMethodsSize = readLEB128().toULEB128();
			
			int size = staticFieldsSize.toInt();
			staticFields = new EncodedField[size];
			for(int i = 0; i < size; i++)
				staticFields[i] = new EncodedField(raf, dex);

			size = instanceFieldsSize.toInt();
			instanceFields = new EncodedField[size];
			for(int i = 0; i < size; i++)
				instanceFields[i] = new EncodedField(raf, dex);

			size = directMethodsSize.toInt();
			directMethods = new EncodedMethod[size];
			int idx;
			for(int i = 0; i < size; i++){
				directMethods[i] = new EncodedMethod(raf, dex);
				if(i == 0)
					idx = directMethods[i].methodIdxDiff.toInt();
				else
					idx = directMethods[i].methodIdxDiff.toInt() 
						+ directMethods[0].methodIdxDiff.toInt();
				directMethods[i].methodIdx = idx; 
			}
			
			size = virtualMethodsSize.toInt();
			virtualMethods = new EncodedMethod[size];		
			for(int i = 0; i < size; i++){
				virtualMethods[i] = new EncodedMethod(raf, dex);		
				if(i == 0)
					idx = virtualMethods[i].methodIdxDiff.toInt();
				else
					idx = virtualMethods[i].methodIdxDiff.toInt() 
						+ virtualMethods[0].methodIdxDiff.toInt();
				virtualMethods[i].methodIdx = idx; 				
			}				
		}
		
		@Override
		void loadData() throws IOException {
			int size = directMethodsSize.toInt();
			for(int i = 0; i < size; i++){
				directMethods[i].loadData();
			}
			
			size = virtualMethodsSize.toInt();
			for(int i = 0; i < size; i++){
				virtualMethods[i].loadData();
			}
		}
		
		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();			
			for(EncodedField ef : staticFields){
				buf.append("\t.static_field ").append(ef.toString()).append("\n");
			}
			
			for(EncodedField ef : instanceFields){
				buf.append("\t.instance_field ").append(ef.toString()).append("\n");
			}			

			for(EncodedMethod em : directMethods){
				buf.append("\t.direct_method ").append(em.toString()).append("\n");
			}
			
			for(EncodedMethod em : virtualMethods){
				buf.append("\t.virtual_method ").append(em.toString()).append("\n");
			}			
			return buf.toString();
		}


		static class EncodedField extends Item{
			ULEB128 fieldIdxDiff;
			ULEB128 accessFlags;
			
			EncodedField(RandomAccessFile file, DexFile dexFile) throws IOException {
				super(file, dexFile);
				fieldIdxDiff = readLEB128().toULEB128();
				accessFlags = readLEB128().toULEB128();
			}
			
			String getAccessFlag(){
				int flag = accessFlags.toInt();
				switch(flag){
					case ACC_PUBLIC:
						return "public";
					case ACC_PRIVATE:
						return "private";
					case ACC_PROTECTED:
						return "protected";
					case ACC_STATIC:
						return "static";
					case ACC_FINAL:
						return "final";
					case ACC_VOLATILE:
						return "volatile";	 
					case ACC_TRANSIENT:
						return "transient";	 
					case ACC_SYNTHETIC:
						return "syntetic";
					case ACC_ENUM:
						return "enum";	 
					default:
						return "";
				}
			}

			@Override
			public String toString() {
				return getAccessFlag() + " " + dex.fieldIds[fieldIdxDiff.toInt()];
			}
		}		
		
		static class EncodedMethod extends Item{
			ULEB128 methodIdxDiff;
			ULEB128 accessFlags;
			ULEB128 codeOffset;
			
			int methodIdx;
			CodeItem codeItem; 
			
			EncodedMethod(RandomAccessFile file, DexFile dexFile) throws IOException {
				super(file, dexFile);
				methodIdxDiff = readLEB128().toULEB128();
				accessFlags = readLEB128().toULEB128();
				codeOffset = readLEB128().toULEB128();
			}

			@Override
			void loadData() throws IOException {
				int offset = codeOffset.toInt();
				if(offset == 0) return;
				raf.seek(offset);
				codeItem = new CodeItem(raf, dex);
			}
			
			String getAccessFlag(){
				int flag = accessFlags.toInt();
				switch(flag){
				case ACC_PUBLIC:
					return "public";
				case ACC_PRIVATE:
					return "private";
				case ACC_PROTECTED:
					return "protected";
				case ACC_STATIC:
					return "static";
				case ACC_FINAL:
					return "final";
				case ACC_SYNCHRONIZED:
					return "synchronized";
				case ACC_BRIDGE:
					return "bridge";
				case ACC_VARARGS:
					return "varargs";
				case ACC_NATIVE:
					return "native";
				case ACC_ABSTRACT:
					return "abstract";
				case ACC_STRICT:
					return "strictfp";
				case ACC_SYNTHETIC:
					return "synthetic";
				case ACC_CONSTRUCTOR:
					return "constructor";
				case ACC_DECLARED_SYNCHRONIZED:
					return "synchronized";
				default:
					return "";
				}
			}

			@Override
			public String toString() {
				StringBuffer buf = new StringBuffer();
				buf.append(getAccessFlag()).append(" ")
					.append(dex.methodIds[methodIdx]);
				if(codeItem != null)
					buf.append("\n\t\t").append("#code_item\n\t\t").append(codeItem.toString());
				return buf.toString();
			}
			
		}	
	}
	
	static class AnnotationsDirectory extends Item{
		int classAnnotationsOffset;
		int fieldsSize;
		int methodsSize;
		int parametersSize;
		FieldAnnotation[] fieldAnnotations;
		MethodAnnotation[] methodAnnotations;
		ParameterAnnotation[] parameterAnnotations;
		AnnotationSet classAnnotations;
		
		AnnotationsDirectory(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dexFile);
			classAnnotationsOffset = readInt();
			fieldsSize = readInt();
			methodsSize = readInt();
			parametersSize = readInt();
			
			if(fieldsSize > 0){
				fieldAnnotations = new FieldAnnotation[fieldsSize];
				for(int i = 0; i < fieldsSize; i++)
					fieldAnnotations[i] = new FieldAnnotation(raf, dex);
			}
			
			if(methodsSize > 0){
				methodAnnotations = new MethodAnnotation[methodsSize];
				for(int i = 0; i < methodsSize; i++)
					methodAnnotations[i] = new MethodAnnotation(raf, dex);
			}		
			
			if(parametersSize > 0){
				parameterAnnotations = new ParameterAnnotation[parametersSize];
				for(int i = 0; i < parametersSize; i++)
					parameterAnnotations[i] = new ParameterAnnotation(raf, dex);
			}				
		}

		@Override
		void loadData() throws IOException {
			if(classAnnotationsOffset > 0){
				raf.seek(classAnnotationsOffset);
				classAnnotations = new AnnotationSet(raf, dex);
			}
			
			for(int i = 0; i < fieldsSize; i++){
				raf.seek(fieldAnnotations[i].annotationsOffset);
				fieldAnnotations[i].annotations = new AnnotationSet(raf, dex);
				fieldAnnotations[i].annotations.loadData();
			}
			
			for(int i = 0; i < methodsSize; i++){
				raf.seek(methodAnnotations[i].annotationsOffset);
				methodAnnotations[i].annotations = new AnnotationSet(raf, dex);
				methodAnnotations[i].annotations.loadData();
			}
			
			for(int i = 0; i < parametersSize; i++){
				raf.seek(parameterAnnotations[i].annotationsOffset);
				parameterAnnotations[i].annotations = new AnnotationSet(raf, dex);
				parameterAnnotations[i].annotations.loadData();
			}
		}

		static class FieldAnnotation extends Item{
			int fieldIdx;
			int annotationsOffset;
			AnnotationSet annotations;
			
			FieldAnnotation(RandomAccessFile file, DexFile dexFile) throws IOException {
				super(file, dexFile);
				fieldIdx = readInt();
				annotationsOffset = readInt();
			}
			
		}
		
		static class MethodAnnotation extends Item{
			int methodIdx;
			int annotationsOffset;
			AnnotationSet annotations;
			
			MethodAnnotation(RandomAccessFile file, DexFile dexFile) throws IOException {
				super(file, dexFile);
				methodIdx = readInt();
				annotationsOffset = readInt();			
			}	
			
		}
		
		static class ParameterAnnotation extends Item{
			int methodIdx;
			int annotationsOffset;
			AnnotationSet annotations;
			
			ParameterAnnotation(RandomAccessFile file, DexFile dexFile) throws IOException {
				super(file, dexFile);
				methodIdx = readInt();
				annotationsOffset = readInt();			
			}				
		}
		
		static class AnnotationSet extends Item{
			int size;
			int[] offsetEntries;
			AnnotationItem[] entries;
			
			AnnotationSet(RandomAccessFile file, DexFile dexFile) throws IOException {
				super(file, dexFile);
				size = readInt();
				offsetEntries = new int[size];
				for(int i = 0; i < size; i++)
					offsetEntries[i] = readInt();
			}

			@Override
			void loadData() throws IOException {
				entries = new AnnotationItem[size];			
				for(int i = 0; i < size; i++){
					raf.seek(offsetEntries[i]);
					entries[i] = new AnnotationItem(raf, dex);		
				}
			}
			
			
		}	
		
		static class AnnotationItem extends Item{
			byte visibility;
			EncodedAnnotation annotation;
			
			AnnotationItem(RandomAccessFile file, DexFile dexFile) throws IOException {
				super(file, dexFile);
				visibility = readByte();
				annotation = new EncodedAnnotation(raf, dex);
			}		
			
			public static final int VISIBILITY_BUILD	= 0x00;
			public static final int VISIBILITY_RUNTIME	= 0x01;
			public static final int VISIBILITY_SYSTEM	= 0x02;			
		}	
	}	
}
