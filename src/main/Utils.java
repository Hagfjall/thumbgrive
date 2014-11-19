package main;

public class Utils {
//	public static String API_ID = "712248328849-be65m63ocjt2rpctlrgcnv7c3lpr08ff.apps.googleusercontent.com";
//	public static String API_SECRET = "iOL-BGbsIxFQdullQDPu654C";
	public static String SECRET_FILE = ".secret";
	public static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	public static String FILES_OUTSIDE_FOLDER_SAVE_PATH = "files-without-folder";
	
	
	public static String makeStringFilenameSafe(String input) {
		return input.replaceAll("\\W+", "_");
	}
}
