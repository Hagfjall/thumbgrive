package main;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Downloads the thumbnails available for the searched filetype
 * 
 * @author Fredrik Hagfjäll
 *
 *
 *         Arguments: --download-failed = download the failed thumbnails
 * 
 *         -q filetype1 [filetype2...] = defines what search-q to use
 *         --force-redownload = download even if file exists locally -s NUMBER =
 *         size of the thumbnail, should not be bigger than //TODO check googles
 *         api
 */

public class Thumbgrive {

	public static void main(String[] args1) {
		String args[] = { "600", "cr2", "-help", "-f" };

		CommandLineParser parser = new BasicParser();
		Options options = new Options();
		options.addOption("help", false, "print this message");

		Option forceReload = new Option("f", "force-redownload", false,
				"download even if file exists locally");
		Option searchQ = OptionBuilder
				.withArgName("q")
				.hasArg()
				.withDescription(
						"defines what search-query to use, see google drive API page for more information")
				.create("searchQ");
		options.addOption(forceReload);
		options.addOption(searchQ);
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("Thumbgrive", options);
				return;
			}
			if (line.hasOption("f")) {
				System.out.println("force");
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setLogger(false, java.util.logging.Level.ALL);

		// TODO read all arguments
		ThumbnailPathHolder thumbnailPathHolder = new ThumbnailPathHolder();
		DownloadThumbnails downloadThumbnail = new DownloadThumbnails(
				thumbnailPathHolder);
		downloadThumbnail.start();
		RetrieveThumbnailsLinks retrieveThumbnailsLinks = new RetrieveThumbnailsLinks(
				thumbnailPathHolder, Integer.parseInt(args[0]), args[1]);
		retrieveThumbnailsLinks.run();
		thumbnailPathHolder.setAllThumbnailLinksRetrieved();
	}

	private static void setLogger(boolean useFileLogger,
			java.util.logging.Level level) {
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
			if (useFileLogger) {
				FileHandler fileHandler;
				try {
					fileHandler = new FileHandler("log.txt");
					topLogger.addHandler(fileHandler);
					fileHandler.setFormatter(new SimpleFormatter());
					fileHandler.setLevel(java.util.logging.Level.ALL);
				} catch (SecurityException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		consoleHandler.setLevel(level);

	}

}
