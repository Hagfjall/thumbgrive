package main;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

public class GoogleApi {

	private static final String API_ID = "712248328849-be65m63ocjt2rpctlrgcnv7c3lpr08ff.apps.googleusercontent.com";
	private static final String API_SECRET = "iOL-BGbsIxFQdullQDPu654C";

	private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

	private static GoogleAuthorizationCodeFlow flow = null;

	/**
	 * Exception thrown when an error occurred while retrieving credentials.
	 */
	public static class GetCredentialsException extends Exception {

		private static final long serialVersionUID = 733670638248314936L;
		protected String authorizationUrl;

		/**
		 * Construct a GetCredentialsException.
		 *
		 * @param authorizationUrl
		 *            The authorization URL to redirect the user to.
		 */
		public GetCredentialsException(String authorizationUrl) {
			this.authorizationUrl = authorizationUrl;
		}

		/**
		 * Set the authorization URL.
		 */
		public void setAuthorizationUrl(String authorizationUrl) {
			this.authorizationUrl = authorizationUrl;
		}

		/**
		 * @return the authorizationUrl
		 */
		public String getAuthorizationUrl() {
			return authorizationUrl;
		}
	}

	/**
	 * Exception thrown when a code exchange has failed.
	 */
	public static class CodeExchangeException extends GetCredentialsException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5033165528913578704L;

		/**
		 * Construct a CodeExchangeException.
		 *
		 * @param authorizationUrl
		 *            The authorization URL to redirect the user to.
		 */
		public CodeExchangeException(String authorizationUrl) {
			super(authorizationUrl);
		}

	}

	/**
	 * Exception thrown when no refresh token has been found.
	 */
	public static class NoRefreshTokenException extends GetCredentialsException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6598528379056777222L;

		/**
		 * Construct a NoRefreshTokenException.
		 *
		 * @param authorizationUrl
		 *            The authorization URL to redirect the user to.
		 */
		public NoRefreshTokenException(String authorizationUrl) {
			super(authorizationUrl);
		}

	}

	/**
	 * Retrieved stored credentials for the provided user ID.
	 *
	 * @param userId
	 *            User's ID.
	 * @return Stored Credential if found, {@code null} otherwise.
	 * @throws IOException
	 */
	public static GoogleCredential getStoredCredentialsInFile() {
		String secretFileName = Utils.SECRET_FILE;
		List<String> lines;
		try {
			lines = Files.readAllLines(Paths.get("./" + secretFileName),
					Charset.defaultCharset());
		} catch (IOException e) {
			return null;
		}
		String userId, refreshToken = null;
		for (String line : lines) {
			int index = line.indexOf(":") + 1;
			if (line.startsWith("RefreshToken:")) {
				refreshToken = line.substring(index);
			}
		}
		if (refreshToken == null) {
			System.err.println("didnt find any credentials in the file...");
			return null;
		}
		GoogleCredential credentials = new GoogleCredential.Builder()
				.setClientSecrets(API_ID, API_SECRET)
				.setJsonFactory(new JacksonFactory())
				.setTransport(new NetHttpTransport()).build()
				.setRefreshToken(refreshToken);
		return credentials;
	}

	/**
	 * Store OAuth 2.0 credentials in the application's database.
	 *
	 * @param userId
	 *            User's ID.
	 * @param credentials
	 *            The OAuth 2.0 credentials to store.
	 * @throws IOException
	 */
	private static void storeCredentials(Credential credentials)
			throws IOException {
		String secretFileName = Utils.SECRET_FILE;
		Path secretFile = Paths.get("./" + secretFileName);
		Files.write(secretFile,
				("RefreshToken:" + credentials.getRefreshToken()).getBytes());
	}

	/**
	 * Build an authorization flow and store it as a static class attribute.
	 *
	 * @return GoogleAuthorizationCodeFlow instance.
	 * @throws IOException
	 *             Unable to load client_secrets.json.
	 */
	public static GoogleAuthorizationCodeFlow getFlow() {
		if (flow == null) {
			HttpTransport httpTransport = new NetHttpTransport();
			JacksonFactory jsonFactory = new JacksonFactory();
			// GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
			// jsonFactory,
			// ApiTest.class.getResourceAsStream(CLIENTSECRETS_LOCATION));
			flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
					jsonFactory, API_ID, API_SECRET,
					Arrays.asList(DriveScopes.DRIVE_READONLY))
					.setAccessType("offline").setApprovalPrompt("auto").build();
		}
		return flow;
	}

	/**
	 * Exchange an authorization code for OAuth 2.0 credentials.
	 *
	 * @param authorizationCode
	 *            Authorization code to exchange for OAuth 2.0 credentials.
	 * @return OAuth 2.0 credentials.
	 * @throws CodeExchangeException
	 *             An error occurred.
	 */
	public static GoogleCredential exchangeCodeAndStoreCredentials(
			String authorizationCode) throws CodeExchangeException {
		try {
			GoogleAuthorizationCodeFlow flow = getFlow();
			GoogleTokenResponse response = flow
					.newTokenRequest(authorizationCode)
					.setRedirectUri(REDIRECT_URI).execute();
			// TODO can I get and credential in some other way...?
			Credential credential = flow.createAndStoreCredential(response,
					null);
			GoogleCredential credentials = new GoogleCredential.Builder()
					.setClientSecrets(API_ID, API_SECRET)
					.setJsonFactory(new JacksonFactory())
					.setTransport(new NetHttpTransport()).build()
					.setRefreshToken(credential.getRefreshToken())
					.setAccessToken(credential.getAccessToken());
			storeCredentials(credentials);
			return credentials;
		} catch (IOException e) {
			System.err.println("An error occurred: " + e);
			throw new CodeExchangeException(null);
		}
	}

	/**
	 * Send a request to the UserInfo API to retrieve the user's information.
	 *
	 * @param credentials
	 *            OAuth 2.0 credentials to authorize the request.
	 * @return User's information.
	 * @throws NoUserIdException
	 *             An error occurred.
	 */
	// public static Userinfo getUserInfo(Credential credentials)
	// throws NoUserIdException {
	// Oauth2 userInfoService = new Oauth2.Builder(new NetHttpTransport(),
	// new JacksonFactory(), credentials).build();
	// Userinfo userInfo = null;
	// try {
	// userInfo = userInfoService.userinfo().get().execute();
	// } catch (IOException e) {
	// System.err.println("An error occurred: " + e);
	// }
	// if (userInfo != null && userInfo.getId() != null) {
	// return userInfo;
	// } else {
	// throw new NoUserIdException();
	// }
	// }

	/**
	 * Retrieve the authorization URL.
	 *
	 * @param emailAddress
	 *            User's e-mail address.
	 * @param state
	 *            State for the authorization URL.
	 * @return Authorization URL to redirect the user to.
	 * @throws IOException
	 *             Unable to load client_secrets.json.
	 */
	public static String getAuthorizationUrl() {
		GoogleAuthorizationCodeRequestUrl urlBuilder = getFlow()
				.newAuthorizationUrl().setRedirectUri(REDIRECT_URI);
		// urlBuilder.set("user_id", emailAddress);
		return urlBuilder.build();
	}

	public static Drive buildService(GoogleCredential credentials) {
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();

		return new Drive.Builder(httpTransport, jsonFactory, credentials)
				.setApplicationName("Thumbgrive").build();
	}

}
