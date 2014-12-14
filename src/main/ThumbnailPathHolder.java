package main;

public class ThumbnailPathHolder {

	private ThumbnailPath thumbnailPath;
	private boolean allLoaded = false;

	public synchronized void insert(ThumbnailPath thumbnailPath) {
		while (this.thumbnailPath != null) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.thumbnailPath = thumbnailPath;
		notifyAll();
	}

	public synchronized void setAllThumbnailLinksRetrieved() {
		allLoaded = true;
		notifyAll();
		System.out.println("all loaded = true");
	}

	public synchronized ThumbnailPath get() {
		while (thumbnailPath == null && !allLoaded) {
			try {
				wait();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (allLoaded) {
			ThumbnailPath ret = thumbnailPath;
			thumbnailPath = null;
			notifyAll();
			return ret;
		}
		ThumbnailPath ret = thumbnailPath;
		thumbnailPath = null;
		notifyAll();
		return ret;
	}

}
