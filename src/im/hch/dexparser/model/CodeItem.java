package im.hch.dexparser.model;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Hashtable;

public class CodeItem extends Item{
	short registersSize;
	short insSize;
	short outsSize;
	short triesSize;
	int debugInfoOffset;
	int insnsSize;
	byte[] insns;//the byte codes
	short padding;
	TryItem[] tries;
	EncodedCatchHandler[] handlers;
	
	DebugInfo debugInfo;
	
	CodeItem(RandomAccessFile file, DexFile dexFile) throws IOException {
		super(file, dexFile);
		registersSize = readShort();
		insSize = readShort();
		outsSize = readShort();
		triesSize = readShort();
		debugInfoOffset = readInt();
		insnsSize = readInt();
		insns = readBytes(insnsSize * 2);

		if(triesSize > 0 && insnsSize %2 == 1)
			padding = readShort();
		
		if(triesSize > 0){
			tries = new TryItem[triesSize];
			for(int i = 0; i < triesSize; i++){
				tries[i] = new TryItem(raf, dex);
			}
			
			//read handlers
			long handlersStart = raf.getFilePointer(), handlerOffset;			
			int size = readLEB128().toULEB128().toInt();
			handlers = new EncodedCatchHandler[size];
			Hashtable<Integer, EncodedCatchHandler> table = 
					new Hashtable<Integer, EncodedCatchHandler>();
			for(int i = 0; i < size; i++){
				handlerOffset = raf.getFilePointer();
				handlers[i] = new EncodedCatchHandler(raf, dex);
				table.put((int)(handlerOffset - handlersStart), handlers[i]);
			}
			
			for(int i = 0; i < triesSize; i++){
				tries[i].handler = table.get(tries[i].handlerOffset);
			}			
		}		
	}	
	
	@Override
	void loadData() throws IOException {
		if(debugInfoOffset > 0){
			raf.seek(debugInfoOffset);
			debugInfo = new DebugInfo(raf, dex);
		}
	}

	static class TryItem extends Item{
		int startAddr;
		short insnCount;
		short handlerOffset;
		EncodedCatchHandler handler;
		
		TryItem(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dexFile);
			startAddr = readInt();
			insnCount = readShort();
			handlerOffset = readShort();
		}
	}
	
	static class EncodedCatchHandler extends Item{
		LEB128 size;
		EncodedTypeAddrPair[] handlers;
		ULEB128 catchAllAddr;
		
		EncodedCatchHandler(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dexFile);
			size = readLEB128();
			int handlerSize = Math.abs(size.toInt());
			handlers = new EncodedTypeAddrPair[handlerSize];
			for(int j = 0; j < handlerSize; j++){
				handlers[j] = new EncodedTypeAddrPair(raf, dex);
			}
			if(size.toInt() <= 0)
				catchAllAddr = readLEB128().toULEB128();
		}	
	}

	static class EncodedTypeAddrPair extends Item{
		ULEB128 typeIdx;
		ULEB128 addr;
		
		EncodedTypeAddrPair(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dex);
			typeIdx = readLEB128().toULEB128();
			addr = readLEB128().toULEB128(); 
		}	
	}
	
	static class DebugInfo extends Item{
		ULEB128 lineStart;
		ULEB128 parametersSize;
		ULEB128P1[] parameterNames;
		
		DebugInfo(RandomAccessFile file, DexFile dexFile) throws IOException {
			super(file, dexFile);
			lineStart = readLEB128().toULEB128();
			parametersSize = readLEB128().toULEB128();
			int size = parametersSize.toInt();
			parameterNames = new ULEB128P1[size];
			for(int i = 0; i < size ; i++)
				parameterNames[i] = readLEB128().toULEB128P1();
			// TODO Read debug info state machine			
		}

		@Override
		void loadData() throws IOException {
			// TODO Auto-generated method stub
			super.loadData();
		}
		
	}
}
