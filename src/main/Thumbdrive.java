package main;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Thumbdrive {

	public static void main(String[] args) {
		
		Logger mainLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		mainLogger.setLevel(Level.ALL);

		// TODO read all arguments
		RetrieveThumbnailsLinks retrieveThumbnailsLinks = new RetrieveThumbnailsLinks(
				Integer.parseInt(args[0]), args[1]);
		retrieveThumbnailsLinks.run();
		HashMap<String, String> links = retrieveThumbnailsLinks
				.getThumbnailsLinks();
		// for (String path : links.keySet()) {
		// System.out.println(path + " = " + links.get(path));
		// }
		new DownloadThumbnailsExcecutor(links).start();
	}
	
	private static void cleanUp() {
		java.io.File currentStateLinks = new java.io.File(
				Utils.CURRENT_STATE_FILE_NAME);
		currentStateLinks.delete(); //all went well, no need to keep the information anymore
	}

}
