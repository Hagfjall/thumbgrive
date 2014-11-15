package test;

import main.GoogleApi;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

public class ApiTest {

	public static void main(String[] args) {
		GoogleCredential credentials = GoogleApi.getStoredCredentialsInFile();
		if (credentials != null) {
			System.out.println("refreshToken (read from file): "
					+ credentials.getRefreshToken());
		}
	}

}
