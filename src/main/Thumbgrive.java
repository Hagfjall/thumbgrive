package main;

import java.io.IOException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
/**
 * Downloads the thumbnails available for the searched filetype
 * @author Fredrik Hagfjäll
 *
 *
 *	Arguments: 
 *	--download-failed = download the failed thumbnails
 *	-q filetype1 [filetype2...] = defines what search-q to use
 *  --force-redownload = download even if file exists locally
 *  -s NUMBER = size of the thumbnail, should not be bigger than //TODO check googles api
 */

public class Thumbgrive {

	public static void main(String[] args) throws IOException {
		// get the top Logger:
		Logger topLogger = java.util.logging.Logger.getLogger("");

		// Handler for console (reuse it if it already exists)
		Handler consoleHandler = null;
		// see if there is already a console handler
		for (Handler handler : topLogger.getHandlers()) {
			if (handler instanceof ConsoleHandler) {
				// found the console handler
				consoleHandler = handler;
				break;
			}
		}

		if (consoleHandler == null) {
			// there was no console handler found, create a new one
			consoleHandler = new ConsoleHandler();
			topLogger.addHandler(consoleHandler);
			FileHandler fileHandler = new FileHandler("log.txt");
			topLogger.addHandler(fileHandler);
			fileHandler.setFormatter(new SimpleFormatter());
			fileHandler.setLevel(java.util.logging.Level.ALL);
		}
		consoleHandler.setLevel(java.util.logging.Level.ALL);

		if (args[0].equals("retry")) {
			List<ThumbnailPath> failedDownloads = (List<ThumbnailPath>) Utils
					.readObjectFromFile(".failedDownloads");
			if (failedDownloads == null) {
				topLogger.warning("Could not read file .failedDownloads");
				return;
			}
			for (ThumbnailPath t : failedDownloads) {
				DownloadThumbnails.downloadFile(t.getThumbnailLink(),
						t.getPath());
			}
		}
		// TODO read all arguments
		ThumbnailPathHolder thumbnailPathHolder = new ThumbnailPathHolder();
		DownloadThumbnails downloadThumbnail = new DownloadThumbnails(
				thumbnailPathHolder);
		downloadThumbnail.start();
		RetrieveThumbnailsLinks retrieveThumbnailsLinks = new RetrieveThumbnailsLinks(
				thumbnailPathHolder, Integer.parseInt(args[0]), args[1]);
//		retrieveThumbnailsLinks.printMaps();
		retrieveThumbnailsLinks.run();
		thumbnailPathHolder.setAllLoaded();
//		retrieveThumbnailsLinks.printMaps();
	}

	private static void cleanUp() {
		java.io.File currentStateLinks = new java.io.File(
				Utils.CURRENT_STATE_FILE_NAME);
		currentStateLinks.delete(); // all went well, no need to keep the
									// information anymore
	}

}
