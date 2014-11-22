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

public class DownloadThumbnailsExcecutor {

	private HashMap<String, String> thumbnailLinks;

	public DownloadThumbnailsExcecutor(HashMap<String, String> thumbnailLinks) {
		this.thumbnailLinks = thumbnailLinks;
	}

	public void start() {
		ExecutorService exc = null;
		int nbrOfThumbnails = thumbnailLinks.size();
		int nbrOfDownloaded = 0;
		try {
			exc = Executors.newFixedThreadPool(8);
			List<Future<Boolean>> runners = new ArrayList<Future<Boolean>>(
					thumbnailLinks.size());

			for (String path : thumbnailLinks.keySet()) {
				Callable<Boolean> runner = new DownloadThumbnails(path,
						thumbnailLinks.get(path));
				Future<Boolean> future = exc.submit(runner);
				runners.add(future);
			}
			for (Future<Boolean> downloader : runners) {
				try {
					if (downloader.get()) {
						nbrOfDownloaded++;
					}
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (exc != null)
				exc.shutdown();
		}
		System.out.println("Downloaded " + nbrOfDownloaded + " out of " + nbrOfThumbnails);
	}

}
