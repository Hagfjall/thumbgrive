package main;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class DownloadThumbnails implements Callable<Boolean> {
	private final static Logger LOGGER = Logger
			.getLogger(DownloadThumbnails.class.getName());

	private String path;
	private URL url;

	public DownloadThumbnails(String link, String path)
			throws MalformedURLException {
		this.path = path;
		url = new URL(link);
	}

	public String getPath() {
		return path;
	}

	@Override
	public Boolean call() throws Exception {
		URLConnection conn = url.openConnection();
		conn.connect();
		File folders = new File(path.substring(0,
				path.lastIndexOf(File.separator)));
		folders.mkdirs();
		File file = new File(path);
		long localFileSize, serverFileSize;
		if (file.exists()) {
			localFileSize = file.length();
			serverFileSize = conn.getContentLengthLong();
			if (localFileSize != serverFileSize) {
				if (!Utils.FORCE_RELOAD) {
					LOGGER.warning("'"
							+ file
							+ "' already exists but the size differs, local copy: "
							+ localFileSize + " server copy: " + serverFileSize);
				}
			}
			/*
			 * check if the --force-reload flag or similar is activated and then
			 * rewrite the file
			 */
			// TODO implement
			else {
				LOGGER.info("'" + file + "' already downloaded");
				return false;
			}
		}
		try (FileOutputStream fos = new FileOutputStream(path)) {
			ReadableByteChannel rbc = Channels
					.newChannel(conn.getInputStream());
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			LOGGER.info("'" + path + "' downloaded");
			fos.close();
			rbc.close();
			return true;
		}
	}

}
