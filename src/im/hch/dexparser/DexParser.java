package im.hch.dexparser;


import im.hch.dexparser.model.DexFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DexParser {
	
	public static DexFile parseDexFile(File f){
		if(f == null || !f.canRead())//file exist and can read
			return null;
		RandomAccessFile raf = null;
		try{
			raf = new RandomAccessFile(f, "r");
			return new DexFile(raf);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}finally{
			try{
				if(raf != null)
					raf.close();
				raf = null;
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
		
		return null;
	}
	
	public static DexFile parseDexFile(String filename){
		if(filename == null) return null;
		return parseDexFile(new File(filename));
	}	
}
