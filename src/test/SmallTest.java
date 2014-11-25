package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import main.Utils;

public class SmallTest {

	public static void main(String[] args) throws IOException {
		// stringFilenameSafe();
		// pathSeparator();
		System.out.println(Utils.removeSizeOfThumbnailPref("https://lh4.googleusercontent.com/hVnLoEqTVp3c-xvV-iolZ5jDm9l1a44kfAElIBwMv2oiMnSi-7MDGh4pP0c9wJpfSDSlGw=s220"));
//		HashMap<String, String> links = makeHashMap();
//		writeObjectToFile(links);
//		links.put("test", "new values");
//		writeObjectToFile(links);
	}
	
	static HashMap<String, String> makeHashMap() {
		String[] paths = {
				"Min_enhet/Bilder_test/d_ligtnamnp_folder_/IMG_0157.CR2",
				"Min_enhet/Bilder_test/Subfolder/IMG_0045.CR2",
				"Min_enhet/Bilder_test/IMG_0088.CR2" };
		String[] links = {
				"https://lh4.googleusercontent.com/mm5djketltCjyGgEtG0AbiSXDK7XvQNtS5i3aWBIuz_vNPczSWMTcfqOEevQe6bGPAy0FQ=s600",
				"https://lh5.googleusercontent.com/WBoxbeucvu2QgIy4p7gkTjjrPFyWsbyTi3-Tp3oIwb4ig0sh4QuqeRFg703ZQfxbmSjXTg=s600",
				"https://lh6.googleusercontent.com/-j9c1Yxtwsti8f4zbt2cpQXMdMRvlFRau_Gif6T94NHNXmZP5HAofqPIF6ZCsY7y6b0IkA=s600" };
		HashMap<String, String> ret = new HashMap<String, String>();
		for (int i = 0; i < 3; i++) {
			ret.put(links[i], paths[i]);
		}
		return ret;
	}

	static void writeObjectToFile(HashMap<String, String> thumbnails) {
		try (ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(Utils.CURRENT_STATE_FILE_NAME))) {
			oos.writeObject(thumbnails);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	static void stringFilenameSafe() {
		System.out.println(Utils.makeStringFilenameSafe("bärs^/ .2"));
	}

	static void pathSeparator() {
		System.out.println(File.pathSeparator);
		System.out.println(File.separator);
	}

	static void illegalFilename() {
		String illegalFilename = "not-allowed*^Å/";
		File f = new File(illegalFilename);
		try {
			System.out.println((boolean) f.createNewFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
