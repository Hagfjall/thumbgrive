package main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;

public class DownloadThumbnail implements Callable<Boolean> {

	private String path;
	private URL url;

	public DownloadThumbnail(String path, String url)
			throws MalformedURLException {
		this.path = path;
		this.url = new URL(url);
	}

	@Override
	public Boolean call() throws Exception {
		try (FileOutputStream fos = new FileOutputStream(path)) {
			// ReadableByteChannel rbc = Channels
			// .newChannel(url.openStream());
			// fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			System.out.println("downloaded " + path);
			fos.close();
			return true;
		}
	}

}
