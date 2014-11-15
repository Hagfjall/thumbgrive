package main;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
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
		try {
			exc = Executors.newFixedThreadPool(8);
			List<Future<Boolean>> runners = new ArrayList<Future<Boolean>>(
					thumbnailLinks.size());

			long startTime = System.currentTimeMillis();
			for (String path : thumbnailLinks.keySet()) {
				Callable<Boolean> runner = new DownloadThumbnail(path,
						thumbnailLinks.get(path));
				Future<Boolean> future = exc.submit(runner);
				runners.add(future);
			}
			// int downloaded = countDownloads(runners);
			// long stopTime = System.currentTimeMillis();
			// if (downloaded == links.size())
			// System.out.println("Downloaded all " + args[1] + "s files in "
			// + (stopTime - startTime) + "s");
			// else {
			// System.out.println("Downloaded only " + downloaded + " of "
			// + links.size() + " " + args[1] + "s in "
			// + (stopTime - startTime) + "s");
			// }
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (exc != null)
				exc.shutdown();
		}
	}

}
