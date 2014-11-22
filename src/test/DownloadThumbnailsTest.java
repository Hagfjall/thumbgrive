package test;

import java.util.HashMap;

import main.DownloadThumbnailsExcecutor;

public class DownloadThumbnailsTest {

	public static void main(String[] args) {
		String[] paths = {
				"Min_enhet/Bilder_test/d_ligtnamnp_folder_/IMG_0157.CR2",
				"Min_enhet/Bilder_test/Subfolder/IMG_0045.CR2",
				"Min_enhet/Bilder_test/IMG_0088.CR2" };
		String[] links = {
				"https://lh4.googleusercontent.com/mm5djketltCjyGgEtG0AbiSXDK7XvQNtS5i3aWBIuz_vNPczSWMTcfqOEevQe6bGPAy0FQ=s600",
				"https://lh5.googleusercontent.com/WBoxbeucvu2QgIy4p7gkTjjrPFyWsbyTi3-Tp3oIwb4ig0sh4QuqeRFg703ZQfxbmSjXTg=s600",
				"https://lh6.googleusercontent.com/-j9c1Yxtwsti8f4zbt2cpQXMdMRvlFRau_Gif6T94NHNXmZP5HAofqPIF6ZCsY7y6b0IkA=s600" };
		HashMap<String,String> thumbnails = new HashMap<String,String>();
		for(int i = 0; i< 3; i++) {
			thumbnails.put(paths[i], links[i]);
		}
		new DownloadThumbnailsExcecutor(thumbnails).start();
	}
}
