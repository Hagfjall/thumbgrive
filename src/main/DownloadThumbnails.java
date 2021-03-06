package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DownloadThumbnails extends Thread {
	private final static Logger LOGGER = Logger
			.getLogger(DownloadThumbnails.class.getName());

	private ThumbnailPathHolder thumbnailPathHolder;
	private List<ThumbnailPath> failedDownloads;
	private List<ThumbnailPath> succeededDownloads;

	public DownloadThumbnails(ThumbnailPathHolder thumbnailPathHolder) {
		this.thumbnailPathHolder = thumbnailPathHolder;
		failedDownloads = new ArrayList<ThumbnailPath>();
		succeededDownloads = new ArrayList<ThumbnailPath>();
	}

	@Override
	public void run() {
		while (true) {

			ThumbnailPath thumbnailPath = thumbnailPathHolder.get();
			if (thumbnailPath == null) {
				break;
			}
			String path = thumbnailPath.getPath();
			String thumbnailLink = thumbnailPath.getThumbnailLink();
			try {
				URL url = new URL(thumbnailLink);
				URLConnection conn = url.openConnection();
				conn.connect();
				int folderIndex = path.lastIndexOf(File.separator);
				if (folderIndex != -1) {
					File folders = new File(path.substring(0,
							path.lastIndexOf(File.separator)));
					folders.mkdirs();
				}
				File file = new File(path);
				if (Utils.FORCE_RELOAD_PREF) {
					file.delete();
				}
				if (!file.exists()) {
					FileOutputStream fos = new FileOutputStream(path);
					ReadableByteChannel rbc = Channels.newChannel(conn
							.getInputStream());
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					LOGGER.info("'" + path + "' downloaded");
					fos.close();
					succeededDownloads.add(thumbnailPath);
				}
			} catch (IOException e) {
				LOGGER.warning("Could not download " + path);
				failedDownloads.add(thumbnailPath);

			}
		}
		StringBuilder sb = new StringBuilder();
		for (ThumbnailPath thumbnailPath : failedDownloads) {
			sb.append(thumbnailPath);
			sb.append(System.lineSeparator());
		}
		if (failedDownloads.size() > 0) {
			LOGGER.warning("Could not download these " + failedDownloads.size()
					+ " thumbnails:\n" + sb.toString());
		}
		Utils.writeObjectToFile(failedDownloads, ".failedDownloads");
		Utils.writeObjectToFile(succeededDownloads, ".succeededDownloads");
	}

	public static void downloadFile(String link, String fileOutput)
			throws IOException {
		URLConnection conn = new URL(link).openConnection();
		FileOutputStream fos = new FileOutputStream(fileOutput);
		ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		LOGGER.info("'" + fileOutput + "' downloaded");
		fos.close();
	}
}
