package im.hch.dexparser;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import im.hch.dexparser.model.DexFile;

public class Main {

	public static void main(String[] args) throws FileNotFoundException {
		if(args.length < 1){
			System.out.println("please specify the dex file to parse.");
			System.exit(0);
		}
		
		DexFile dex = DexParser.parseDexFile(args[0]);		
		if(dex != null){
			PrintWriter pw = new PrintWriter("output.txt");
			System.out.println(dex.toString());
//			dex.printStrings(pw);
//			dex.printTypes(pw);
//			dex.printProtos(pw);
//			dex.printFields(pw);
//			dex.printMethods(pw);
			dex.printClasses(pw, null);
//			dex.printStaticStrings(pw);
		}
	}

}
