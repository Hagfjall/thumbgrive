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

	public static void main(String[] args) {
		// String args[] = { "600", "cr2","-q", "a very long line" ,"-f"};

		CommandLineParser parser = new BasicParser();
		Options options = new Options();
		options.addOption("help", false, "print this message");
		Option forceReloadOption = new Option("f", "force-redownload", false,
				"download even if file exists locally");
		Option searchQOption = new Option(
				"q",
				"defines what filetypes to retrieve thumbnails for"
						+ "will use default (all images) if nothing i specified");
		Option sizeOption = new Option("s",
				"sets the thumbnail size, default = 600");
		Option logToFileOption = new Option("log", false,
				"Save the log to log.txt");
		sizeOption.setArgs(1);
		searchQOption.setArgs(Option.UNLIMITED_VALUES);

		options.addOption(forceReloadOption);
		options.addOption(searchQOption);
		options.addOption(sizeOption);
		options.addOption(logToFileOption);
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help")) {
				new HelpFormatter().printHelp("Thumbgrive", options);
				return;
			}
			Utils.FORCE_RELOAD_PREF = line.hasOption("f");
			if (line.hasOption("q")) {
				Utils.RETRIEVE_THUMBNAILS_FOR_THIS_FILETYPES = line
						.getOptionValues("q");
			} else {
				Utils.RETRIEVE_THUMBNAILS_FOR_THIS_FILETYPES = new String[1];
				Utils.RETRIEVE_THUMBNAILS_FOR_THIS_FILETYPES[0] = "images";
			}
			if (line.hasOption("s")) {
				// TODO really check that this method returns 600 if the user
				// havn't set any -s flag, hate coding offline...
				Utils.THUMBNAIL_SIZE_PREF = Integer.parseInt(line
						.getOptionValue("s", "600"));
			}
			setLogger(line.hasOption("log"), java.util.logging.Level.ALL);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			java.util.logging.Logger.getLogger("").warning("Wrong arguments!");
			new HelpFormatter().printHelp("Thumbgrive", options);
			return;

		}

		ThumbnailPathHolder thumbnailPathHolder = new ThumbnailPathHolder();
		new DownloadThumbnails(thumbnailPathHolder).start();
		RetrieveThumbnailsLinks retrieveThumbnailsLinks = new RetrieveThumbnailsLinks(
				thumbnailPathHolder, Utils.THUMBNAIL_SIZE_PREF);
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
