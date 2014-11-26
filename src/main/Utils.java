package main;

public class Utils {
	// public static String API_ID =
	// "712248328849-be65m63ocjt2rpctlrgcnv7c3lpr08ff.apps.googleusercontent.com";
	// public static String API_SECRET = "iOL-BGbsIxFQdullQDPu654C";
	public static String SECRET_FILE = ".secret";
	public static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	public static String FILES_OUTSIDE_FOLDER_SAVE_PATH = "files-without-folder";
	public static String CURRENT_STATE_FILE_NAME = ".hashmapRetrievedLinks";
	public static int THUMBNAIL_SIZE_PREF;

	private static boolean differsInPreferredAndStoredLink = true;

	public static boolean FORCE_RELOAD_PREF = false;

	public static String makeStringFilenameSafe(String input) {
		return input.replaceAll("\\W+", "_");
	}

	// public static String addSizeOfThumbnailPref(String thumbnailLink) {
	// if (thumbnailLink != null) {
	// return thumbnailLink + "=s" + THUMBNAIL_SIZE;
	// } else {
	// return null;
	// }
	// }
	//
	// public static String removeSizeOfThumbnailPref(String thumbnailLink) {
	// int sizePos = thumbnailLink.indexOf("=s");
	// return thumbnailLink.substring(0, sizePos);
	// }

	public static String changeSizeOfThumbnailToPref(String thumbnailLink) {
		if (differsInPreferredAndStoredLink) {
			if (thumbnailLink != null) {
				if (thumbnailLink.contains("=s")) {
					int sizePos = thumbnailLink.indexOf("=s") + 2;
					int linkThumbnailSize = Integer.parseInt(thumbnailLink
							.substring(sizePos));
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
