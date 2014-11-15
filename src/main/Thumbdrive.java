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
			new DownloadThumbnailsExcecutor(links).start();
			for(String path : links.keySet()) {
				System.out.println(path + " = " + links.get(path));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
