package test;

import main.ThumbnailPath;
import main.ThumbnailPathHolder;

public class ThumbnailPathHolderTest extends Thread {

	private ThumbnailPathHolder thumbnail;

	public ThumbnailPathHolderTest(ThumbnailPathHolder thumbnail) {
		this.thumbnail = thumbnail;
	}

	public static void main(String[] args) {
		ThumbnailPathHolder test = new ThumbnailPathHolder();
		new ThumbnailPathHolderTest(test).start();
		String path = "/test/number";
		String url = "http://www.google.se/";
		for (int i = 0; i < 6; i++) {
			test.insert(new ThumbnailPath(path + Integer.toString(i), url));
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		test.setAllLoaded();

	}

	@Override
	public void run() {
		while(true) {
			ThumbnailPath rec = thumbnail.get();
			if(rec == null) {
				System.out.println("got it all!");
				return;
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("thread: " + rec);
		}
	}

}
