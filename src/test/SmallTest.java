package test;

import java.io.File;
import java.io.IOException;

import main.Utils;

public class SmallTest {

	public static void main(String[] args) {
//		stringFilenameSafe();
		pathSeparator();
	}
	
	static void stringFilenameSafe() {
		System.out.println(Utils.makeStringFilenameSafe("Min enhet/C&F/Bilder/Bergen /"));
	}
	static void pathSeparator() {
		System.out.println(File.pathSeparator);
		System.out.println(File.separator);
	}
	
	static void illegalFilename() {
		String illegalFilename = "not-allowed*^Å/";
		File f = new File(illegalFilename);
		try {
			System.out.println((boolean)f.createNewFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
