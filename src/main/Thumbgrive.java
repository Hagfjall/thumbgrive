package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import main.GoogleApi.CodeExchangeException;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

public class Thumbgrive {

	private GoogleCredential credentials;
	private String[] filetypes;
	private int thumbnailSize;
	private Drive service;

	private Map<String, List<String>> thumbnailsLinks;

	public Thumbgrive(int thumbnailSize, String... filetypes) {
		this.filetypes = filetypes;
		this.thumbnailSize = thumbnailSize;
		thumbnailsLinks = new HashMap<String, List<String>>();
		try {
			credentials = loadCredentials();
			service = GoogleApi.buildService(credentials);
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
		Thumbgrive thumbdrive = new Thumbgrive(220, "CR2");
		thumbdrive.retrieveThumbnailsLinks();

	}

	private void retrieveThumbnailsLinks() {
		for (String filetype : filetypes) {
			System.out.println("searching for filetypes " + filetype);
			listFilesAddToList(filetype);
		}
	}

	private void downloadThumbnails() {
		// TODO implement method
	}

	private void listFilesAddToList(String filetype) {
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
			System.out.println("Title: " + file.getTitle());
			System.out.println("id: " + file.getId());
			System.out.println("subfolders: ");
			LinkedList<String> parentFolders;
			try {
				parentFolders = getParentFoldersId(service, file.getId());
				if (parentFolders.size() == 0)
					continue;
				StringBuilder sb = new StringBuilder(256);
				for (String folder : parentFolders) {
					sb.append(getTitleOfId(service, folder));
					sb.append(java.io.File.separator);
				}
				System.out.println("stringbuilder: " + sb);
				//TODO add this path and link to the map (but make it a multimap first
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
			System.out.println("Thmbnail: " + file.getThumbnailLink());
		}
	}

	private LinkedList<String> getParentFoldersId(Drive service, String fileId)
			throws IOException {
		LinkedList<String> ret = new LinkedList<String>();
		String currentParentFolderId = fileId;
		while (true) {
			File parentFolder = service.files().get(currentParentFolderId)
					.execute();
			if (parentFolder.getParents().size() == 0) {
				return ret;
			}
			for (ParentReference parent : parentFolder.getParents()) {
				if (parent.getIsRoot()) {
					ret.add(0, parent.getId());
					return ret;
				}
			}
			currentParentFolderId = parentFolder.getParents().get(0).getId();
			ret.add(0, currentParentFolderId);
		}
	}

	private String getTitleOfId(Drive service, String id) throws IOException {
		File file = service.files().get(id).execute();
		return file.getTitle();
	}

}
