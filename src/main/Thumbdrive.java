package main;

import java.io.IOException;
import java.util.HashMap;

public class Thumbdrive {

	public static void main(String[] args) {
		// TODO read all arguments
		RetrieveThumbnailsLinks retrieveThumbnailsLinks = new RetrieveThumbnailsLinks(
				600, "CR2");
		try {
			retrieveThumbnailsLinks.run();
			HashMap<String, String> links = retrieveThumbnailsLinks
					.getThumbnailsLinks();
			for(String path : links.keySet()) {
				System.out.println(path + " = " + links.get(path));
			}
			new DownloadThumbnailsExcecutor(links).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
