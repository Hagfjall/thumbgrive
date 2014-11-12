package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import main.GoogleApi;
import main.GoogleApi.CodeExchangeException;
import main.GoogleApi.NoRefreshTokenException;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class RefreshTokenTest {

	private static String emailAddress = "dic12fha@student.lu.se";
	
	static List<File> result = new ArrayList<File>();

	public static void main(String[] args) throws IOException, CodeExchangeException, NoRefreshTokenException {
		GoogleCredential credentials = GoogleApi.getStoredCredentials(emailAddress);
		System.out.println("accessToken: " + credentials.getAccessToken());
		System.out.println("refreshToken: " + credentials.getRefreshToken());
		Drive service = StoreInfoTest.buildService(credentials);
		com.google.api.services.drive.Drive.Files.List request = service.files().list();
		request.setQ(arg0)

	    do {
	      try {
	        FileList files = request.execute();

	        result.addAll(files.getItems());
	        request.setPageToken(files.getNextPageToken());
	      } catch (IOException e) {
	        System.out.println("An error occurred: " + e);
	        request.setPageToken(null);
	      }
	    } while (request.getPageToken() != null &&
	             request.getPageToken().length() > 0);
	    for(File file: result) {
	    	System.out.println("id: " + file.getId());
	    }

		
//        response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
//        refreshToken = response.getRefreshToken();                
//
//        GoogleCredential credential = new GoogleCredential().setAccessToken(response.getAccessToken());        
//
//        client = new Drive.Builder(httpTransport, jsonFactory, credential).build();   
//        GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
//                .setJsonFactory(jsonFactory)
//                .setClientSecrets(Settings.API_ID, Settings.API_SECRET)
//                .build()
//                .setFromTokenResponse(response);
		/**
		 * 
		// System.out.println("Write the refreshToken:");
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(System.in));
		String refreshToken = "ya29.uQCAB8Sce2-jOsYAI6q_ruIODHj-6_wIuYFMRwTdG4ChwzQidzSZ5YBXVYOzsvq7weLAmQCnyCkcfg";
		TokenResponse response = new GoogleRefreshTokenRequest(
				new NetHttpTransport(), new JacksonFactory(), refreshToken,
				Settings.API_ID, Settings.API_SECRET).execute();
		 GoogleCredential gCred = new GoogleCredential()
		 .setFromTokenResponse(response);
//		GoogleCredential gCred = new GoogleCredential.Builder()
//				.setClientSecrets(Settings.API_ID, Settings.API_SECRET)
//				.setServiceAccountUser(emailAddress).build();
		System.out.println("userId: " + gCred.getServiceAccountId());
		System.out.println(gCred.getAccessToken());
		System.out.println(gCred.getRefreshToken());
		System.out.println(gCred.getServiceAccountUser());
		// System.out.println(gCred.g
		 * */
	}

}
