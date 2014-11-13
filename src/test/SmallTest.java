package test;

import java.io.File;
import java.io.IOException;

public class SmallTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

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
