package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import main.GoogleApi.CodeExchangeException;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

public class GdriveThumbnails {

	private GoogleCredential credentials;
	private String[] filetypes;
	private int thumbnailSize;

	public GdriveThumbnails(int thumbnailSize, String... filetypes) {
		this.filetypes = filetypes;
		this.thumbnailSize = thumbnailSize;
		try {
			credentials = loadCredentials();
		} catch (CodeExchangeException | IOException e) {
			e.printStackTrace();
		}
	}

	private GoogleCredential loadCredentials() throws CodeExchangeException,
			IOException {
		GoogleCredential credentials = GoogleApi.getStoredCredentialsInFile();
		if (credentials == null) { // getting all new tokens
			String url = GoogleApi.getAuthorizationUrl();
			System.out.println(url);
			System.out
					.println("Please use this url to get the key from google and paste it here");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			String responseCode = br.readLine();
			br.close();
			credentials = GoogleApi
					.exchangeCodeAndStoreCredentials(responseCode);
		}
		return credentials;

	}

	private GoogleCredential requestUserForCredentials()
			throws CodeExchangeException, IOException {
		String url = GoogleApi.getAuthorizationUrl();
		System.out.println(url);
		System.out
				.println("Please use this url to get the key from google and paste it here");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String responseCode = br.readLine();
		br.close();
		return GoogleApi.exchangeCodeAndStoreCredentials(responseCode);
	}

	public static void main(String[] args) throws IOException {
		// TODO read all arguments
		GdriveThumbnails instance = new GdriveThumbnails(220, "CR2");
		instance.downloadThumbnails();
	}

	private void downloadThumbnails() {
		for (String filetype : filetypes) {
			System.out.println("searching for filetypes " + filetype);
			listFiles(filetype);
		}
	}

	private void listFiles(String filetype) {
		Drive service = GoogleApi.buildService(credentials); //
		com.google.api.services.drive.Drive.Files.List request;
		try {
			request = service.files().list();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		String searchQuery = "mimeType contains ";
		request.setQ(searchQuery + "'" + filetype + "'");
		List<File> result = new ArrayList<File>();
		do {
			try {
				FileList files = request.execute();

				result.addAll(files.getItems());
				request.setPageToken(files.getNextPageToken());
			} catch (IOException e) {
				System.out.println("An error occurred: " + e);
				request.setPageToken(null);
			}
		} while (request.getPageToken() != null
				&& request.getPageToken().length() > 0);
		for (File file : result) {
			System.out.println("id: " + file.getId());
			System.out.println("Title: " + file.getTitle());
			System.out.println("in folder: ");
			ArrayList<String> parentFolders = new ArrayList<String>();
			for (int i = 0; i < file.getParents().size(); i++) {
				parentFolders.add(file.getParents().get(i).getId());
			}
			// TODO recursive way of going to the root-folder, chosing only the
			// first way every time
			for (String parentFolder : parentFolders) {
				try {
					File parentFolderFile = service.files().get(parentFolder)
							.execute();
					System.out.println("\t" + parentFolderFile.getTitle());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			System.out.println("Thmbnail: " + file.getThumbnailLink());
		}
	}

	private List<String> getParentFolderId(Drive service, String fileId)
			throws IOException {
		File parentFolder = service.files().get(fileId).execute();
		for(ParentReference parent : parentFolder.getParents()) {
			if(parent.getIsRoot()) {
				List<String> ret = new ArrayList<String>();
				ret.add(parent.getId());
				return ret;
		}
		return null;

	}

}
