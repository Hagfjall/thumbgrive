package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;

public class DownloadThumbnails implements Callable<Boolean> {

	private String path;
	private URL url;

	public DownloadThumbnails(String path, String url)
			throws MalformedURLException {
		this.path = path;
		this.url = new URL(url);
	}

	@Override
	public Boolean call() throws Exception {
		File folder = new File(path.substring(0, path.lastIndexOf(File.separator)));
		folder.mkdirs();
		File file = new File(path);
		if(file.exists()) {
			System.err.println("File already exists, size: " + file.length());
//			URLConnection conn = new URLConnection();
			return false;
		}
		try (FileOutputStream fos = new FileOutputStream(path)) {
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			System.out.println("downloaded " + path);
			fos.close();
			rbc.close();
			return true;
		}
	}

}
