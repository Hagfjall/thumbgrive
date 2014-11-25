package main;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class Thumbdrive {

	public static void main(String[] args) throws IOException {
	    //get the top Logger:
	    Logger topLogger = java.util.logging.Logger.getLogger("");

	    // Handler for console (reuse it if it already exists)
	    Handler consoleHandler = null;
	    //see if there is already a console handler
	    for (Handler handler : topLogger.getHandlers()) {
	        if (handler instanceof ConsoleHandler) {
	            //found the console handler
	            consoleHandler = handler;
	            break;
	        }
	    }


	    if (consoleHandler == null) {
	        //there was no console handler found, create a new one
	        consoleHandler = new ConsoleHandler();
	        topLogger.addHandler(consoleHandler);
	    }
	    //set the console handler to fine:
	    consoleHandler.setLevel(java.util.logging.Level.ALL);

		// TODO read all arguments
		RetrieveThumbnailsLinks retrieveThumbnailsLinks = new RetrieveThumbnailsLinks(
				Integer.parseInt(args[0]), args[1]);
		retrieveThumbnailsLinks.printMaps();
		retrieveThumbnailsLinks.run();
		retrieveThumbnailsLinks.printMaps();
		HashMap<String, String> links = retrieveThumbnailsLinks
				.getThumbnailsLinks();
		// for (String path : links.keySet()) {
		// System.out.println(path + " = " + links.get(path));
		// }
//		new DownloadThumbnailsExcecutor(links).start();
	}
	
	private static void cleanUp() {
		java.io.File currentStateLinks = new java.io.File(
				Utils.CURRENT_STATE_FILE_NAME);
		currentStateLinks.delete(); //all went well, no need to keep the information anymore
	}

}
