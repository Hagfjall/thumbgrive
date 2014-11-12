package test;

import java.io.IOException;

import main.GoogleApi;

public class GoogleApiTest {

	public static void main(String[] args) throws IOException {
		System.out.println("GoogleApiUrl: " + GoogleApi.getAuthorizationUrl());
	}

}
