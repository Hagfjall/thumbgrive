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

public class RetrieveThumbnailsLinks {

	private GoogleCredential credentials;
	private String[] filetypes;
	private int thumbnailSize;
	private Drive service;

	private HashMap<String, String> thumbnailsLinks;
	private HashMap<String, String> idToTitle;
	private HashMap<String, String> fileAndParent;

	public RetrieveThumbnailsLinks(int thumbnailSize, String... filetypes) {
		this.filetypes = filetypes;
		this.thumbnailSize = thumbnailSize;
		thumbnailsLinks = new HashMap<String, String>();
		idToTitle = new HashMap<String, String>();
		fileAndParent = new HashMap<String, String>();
		try {
			credentials = loadCredentials();
			service = GoogleApi.buildService(credentials);
		} catch (CodeExchangeException | IOException e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, String> getThumbnailsLinks() {
		return thumbnailsLinks;
	}

	public void run() throws IOException {
		retrieveThumbnailsLinks();
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

	private void retrieveThumbnailsLinks() throws IOException {
		for (String filetype : filetypes) {
			System.out.print("searching for filetypes " + filetype + "");
			com.google.api.services.drive.Drive.Files.List request;
			try {
				request = service.files().list();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
			String searchQuery = "mimeType contains " + "'" + filetype + "'";
			request.setQ(searchQuery);
			List<File> searchResult = new ArrayList<File>();
			do {
				try {
					FileList files = request.execute();

					searchResult.addAll(files.getItems());
					request.setPageToken(files.getNextPageToken());
					System.out.print(".");
				} catch (IOException e) {
					System.out.println("An error occurred: " + e);
					request.setPageToken(null);
				}
			} while (request.getPageToken() != null
					&& request.getPageToken().length() > 0);
			System.out.println(" found " + searchResult.size() + " files");
			for (File file : searchResult) {
				if (file.getThumbnailLink() == null) {
					// the file aren't a image
					continue;
				}
				List<String> fullPath;
				fullPath = getFullPath(service, file.getId());
				if (fullPath.size() < 2) {
					// the file doesn't have any parent folder, is probably
					// archived or in the "share" area, do not download the
					// thumbnail
					continue;
				}
				String thumbnailLink = file.getThumbnailLink();
				if (thumbnailLink.contains("=s")) {
					int sizePos = thumbnailLink.indexOf("=s") + 2;
					int standardThumbnailSize = Integer.parseInt(thumbnailLink
							.substring(sizePos));
					if (thumbnailSize != standardThumbnailSize) {
						thumbnailLink = thumbnailLink.substring(0, sizePos)
								+ thumbnailSize;
					}
				}
				StringBuilder filePath = new StringBuilder(256);
				for (int i = 0; i < fullPath.size(); i++) {
					String partOfFullPath = fullPath.get(i);
					if (i != fullPath.size() - 1)
						partOfFullPath = Utils
								.makeStringFilenameSafe(partOfFullPath)
								+ java.io.File.separator;
					filePath.append(partOfFullPath);
				}
				filePath.append(".jpg"); // the thumbnails from google-servers
											// are always(?) in jpg
				thumbnailsLinks.put(filePath.toString(), thumbnailLink);
			}

		}

	}

	/**
	 * TODO make sure to save the id's for the different folders in order to
	 * reduce the api-calls.
	 * 
	 * @param service
	 * @param fileId
	 * @return
	 * @throws IOException
	 */
	private List<String> getFullPath(Drive service, String fileId)
			throws IOException {
		LinkedList<String> ret = new LinkedList<String>();
		String currentFileId = fileId;
		for (int depth = 0; depth < 30; depth++) {
			File currentFile = service.files().get(currentFileId).execute();
			if (currentFile.getParents().size() == 0) {
				System.out.println("'" + currentFile.getTitle()
						+ "' aren't stored in any folder.");
				return ret;
			}
			ret.add(0, currentFile.getTitle());
			for (ParentReference parent : currentFile.getParents()) {
				if (parent.getIsRoot()) {
					String folderName = getTitleOfId(service, parent.getId());
					ret.add(0, folderName);
					return ret;
				}
			}
			// taking the first parent
			currentFileId = currentFile.getParents().get(0).getId();
		}
		throw new IOException(
				"Depth of file exceeded 30 folders, a file's parent-loop?");
	}
	
//	private String getParentId(Drive service, String id) {
//		
//	}

	private String getTitleOfId(Drive service, String id) throws IOException {
		String title = idToTitle.get(id);
		if (title == null) {
			File file = service.files().get(id).execute();
			title = file.getTitle();
			idToTitle.put(id, title);
		}
		return title;
	}

}
