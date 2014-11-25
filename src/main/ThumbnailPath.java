package main;

import java.io.Serializable;

public class ThumbnailPath implements Serializable{

	private static final long serialVersionUID = -8725948070214667779L;
	private String path;
	private String thumbnailLink;

	public ThumbnailPath(String path, String thumbnailLink) {
		this.path = path;
		this.thumbnailLink = thumbnailLink;
	}

	public String getPath() {
		return path;
	}

	public String getThumbnailLink() {
		return thumbnailLink;
	}

	@Override
	public String toString() {
		return thumbnailLink + "->" + path;
	}

}
