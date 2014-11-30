package main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

public class Utils {
	public static String SECRET_FILE = ".secret";
	public static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	public static String FILES_OUTSIDE_FOLDER_SAVE_PATH = "files-without-folder";
	public static String CURRENT_STATE_FILE_NAME = ".hashmapRetrievedLinks";
	public static int THUMBNAIL_SIZE_PREF;
	public static boolean FORCE_RELOAD_PREF = false;

	private static boolean differsInPreferredAndStoredLink = true;
	private final static Logger LOGGER = Logger
			.getLogger(Utils.class.getName());

	public static String makeStringFilenameSafe(String input) {
		return input.replaceAll("\\W+", "_");
	}

	public static void writeObjectToFile(Object obj, String filename) {
		try (ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(filename))) {
			oos.writeObject(obj);
		} catch (IOException e) {
			LOGGER.warning("Could not save current state, reason: "
					+ e.toString());
		}
	}

	public static Object readObjectFromFile(String filename) {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				filename))) {
			return ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			LOGGER.warning("Could not load '" + filename + "' : "
					+ e.toString());
			return null;
		}
	}

	public static String changeSizeOfThumbnailToPref(String thumbnailLink) {
		if (differsInPreferredAndStoredLink) {
			if (thumbnailLink != null) {
				if (thumbnailLink.contains("=s")) {
					int sizePos = thumbnailLink.indexOf("=s") + 2;
					int linkThumbnailSize = Integer.parseInt(thumbnailLink
							.substring(sizePos));
					// TODO this is checked every time, only need to be checked
					// once
					if (linkThumbnailSize == THUMBNAIL_SIZE_PREF)
						differsInPreferredAndStoredLink = false;
					thumbnailLink = thumbnailLink.substring(0, sizePos)
							+ THUMBNAIL_SIZE_PREF;
				} else {
					thumbnailLink = thumbnailLink + "=s" + THUMBNAIL_SIZE_PREF;
				}
			}
		}
		return thumbnailLink;
	}
}
