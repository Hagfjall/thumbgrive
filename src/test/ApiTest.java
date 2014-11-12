package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import main.GoogleApi;
import main.Settings;
import main.GoogleApi.CodeExchangeException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

public class ApiTest {

	private static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

	private static GoogleAuthorizationCodeFlow flow = null;
	private static String CLIENTSECRETS_LOCATION = "client_secret.json";
	private static String emailAddress = "dic12fha@student.lu.se";

	public static void main(String[] args) throws IOException,
			CodeExchangeException {
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();

		flow = GoogleApi.getFlow();
		Credential cred = flow.loadCredential(emailAddress);
		if(cred!= null) {
			System.out.println("finns redan!");
		}
		// GoogleAuthorizationCodeFlow flow = new
		// GoogleAuthorizationCodeFlow.Builder(
		// httpTransport, jsonFactory, Settings.API_ID,
		// Settings.API_SECRET, Arrays.asList(DriveScopes.DRIVE_READONLY))
		// .setAccessType("offline").setApprovalPrompt("force").build();

		String url = GoogleApi.getAuthorizationUrl();
		// String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI)
		// .build();
		System.out
				.println("Please open the following URL in your browser then type the authorization code:");
		System.out.println("  " + url);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String code = br.readLine();

		 GoogleTokenResponse response = flow.newTokenRequest(code)
		 .setRedirectUri(REDIRECT_URI).execute();
		// GoogleCredential credential = new GoogleCredential()
		// .setFromTokenResponse(response);
		Credential credentials = GoogleApi.exchangeCodeAndStoreCredentials(code);
		GoogleApi.getFlow().createAndStoreCredential(response, emailAddress);
		GoogleCredential gCred = new GoogleCredential.Builder()
				.setClientSecrets(emailAddress, credentials.getRefreshToken())
				.build();
		// StoreInfoTest.getFlow().newTokenRequest(credentials.getRefreshToken());
		GoogleApi.storeCredentials(credentials);

		// Drive service = StoreInfoTest.buildService(credentials);
		// Create a new authorized API client
		// Drive service = new Drive.Builder(httpTransport, jsonFactory,
		// credentials).build();

		// Insert a file
		File body = new File();
		body.setTitle("My document");
		body.setDescription("A test document");
		body.setMimeType("text/plain");

		java.io.File fileContent = new java.io.File("document.txt");
		FileContent mediaContent = new FileContent("text/plain", fileContent);

		// File file = service.files().insert(body, mediaContent).execute();
		// System.out.println("path : " + service.getServicePath());
		// System.out.println("File ID: " + file.getId());
	}

	static GoogleAuthorizationCodeFlow getFlow() throws IOException {
		if (flow == null) {
			HttpTransport httpTransport = new NetHttpTransport();
			JacksonFactory jsonFactory = new JacksonFactory();
			// GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
			// jsonFactory,
			// ApiTest.class.getResourceAsStream(CLIENTSECRETS_LOCATION));
			flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
					jsonFactory, Settings.API_ID, Settings.API_SECRET,
					Arrays.asList(DriveScopes.DRIVE_READONLY))
					.setAccessType("offline").setApprovalPrompt("auto").build();
		}
		return flow;
	}

}
