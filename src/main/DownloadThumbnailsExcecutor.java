package main;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class DownloadThumbnailsExcecutor {

	private final static Logger LOGGER = Logger.getLogger(DownloadThumbnailsExcecutor.class
			.getName());
	private HashMap<String, String> thumbnailLinks;

	public DownloadThumbnailsExcecutor(HashMap<String, String> thumbnailLinks) {
		this.thumbnailLinks = thumbnailLinks;
	}

	public void start() {
		ExecutorService exc = null;
		int nbrOfThumbnails = thumbnailLinks.size();
		int nbrOfDownloaded = 0;
		LOGGER.fine("Starting download, number of files to download is " + nbrOfThumbnails);
		try {
			exc = Executors.newFixedThreadPool(4);
			List<Future<Boolean>> runners = new ArrayList<Future<Boolean>>(
					thumbnailLinks.size());
			
			for (String link : thumbnailLinks.keySet()) {
				Callable<Boolean> runner = new DownloadThumbnails(link,
						thumbnailLinks.get(link));
				Future<Boolean> future = exc.submit(runner);
				runners.add(future);
			}
			for (Future<Boolean> downloader : runners) {
				try {
					if (downloader.get()) {
						LOGGER.finest(nbrOfDownloaded +  " downloaded");
						nbrOfDownloaded++;
					}
				} catch (InterruptedException | ExecutionException e) {
					LOGGER.warning(e.getMessage());
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (exc != null)
				exc.shutdown();
		}
		LOGGER.info("Downloaded " + nbrOfDownloaded + " out of " + nbrOfThumbnails);
	}

}
