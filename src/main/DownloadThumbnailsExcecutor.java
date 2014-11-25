package main;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class DownloadThumbnailsExcecutor {

	private final static Logger LOGGER = Logger
			.getLogger(DownloadThumbnailsExcecutor.class.getName());
	private HashMap<String, ThumbnailPath> fileIdToThumbnail;

	public DownloadThumbnailsExcecutor(
			HashMap<String, ThumbnailPath> fileIdToThumbnail) {
		this.fileIdToThumbnail = fileIdToThumbnail;
	}

	public void start() {
		ExecutorService exc = null;
		int nbrOfThumbnails = fileIdToThumbnail.size();
		int nbrOfDownloaded = 0;
		LOGGER.fine("Starting download, number of files to download is "
				+ nbrOfThumbnails);
		try {
			exc = Executors.newFixedThreadPool(1);
			List<Future<Boolean>> runners = new ArrayList<Future<Boolean>>(
					fileIdToThumbnail.size());

			Iterator<Entry<String, ThumbnailPath>> it = fileIdToThumbnail
					.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ThumbnailPath> me = it.next();
				Callable<Boolean> runner = new DownloadThumbnails(me.getValue());
				it.remove();
				Future<Boolean> future = exc.submit(runner);
				runners.add(future);
			}
			for (Future<Boolean> downloader : runners) {
				try {
					if (downloader.get()) {
						LOGGER.finest(nbrOfDownloaded + " downloaded");
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
		LOGGER.info("Downloaded " + nbrOfDownloaded + " out of "
				+ nbrOfThumbnails);
	}

}
