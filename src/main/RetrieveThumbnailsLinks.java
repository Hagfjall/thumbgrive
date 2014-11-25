package main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.GoogleApi.CodeExchangeException;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

public class RetrieveThumbnailsLinks {

	private final static Logger LOGGER = Logger
			.getLogger(RetrieveThumbnailsLinks.class.getName());
	private GoogleCredential credentials;
	private String[] filetypes;
	private Drive service;

	private HashMap<String, String> thumbnailsLinks;
	private HashMap<String, String> fileIdThumbnails; // TODO save all the fetched links to this map and save preriod. to disk
	private HashMap<String, String> idToTitle;
	private HashMap<String, String> fileAndParent;

	public RetrieveThumbnailsLinks(int thumbnailSize, String... filetypes) {
		LOGGER.setLevel(Level.ALL);
		this.filetypes = filetypes;
		Utils.THUMBNAIL_SIZE = thumbnailSize;
		thumbnailsLinks = loadSavedState();
		if (thumbnailsLinks == null) {
			thumbnailsLinks = new HashMap<String, String>();
			LOGGER.finer(Utils.CURRENT_STATE_FILE_NAME + " not loaded...");
		} else {
			LOGGER.finer(Utils.CURRENT_STATE_FILE_NAME + " loaded with "
					+ thumbnailsLinks.size() + " links");
		}
		idToTitle = new HashMap<String, String>();
		fileAndParent = new HashMap<String, String>();
		try {
			credentials = loadCredentials();
			service = GoogleApi.buildService(credentials);
		} catch (CodeExchangeException | IOException e) {
			e.printStackTrace();
		}
	}

	private HashMap<String, String> loadSavedState() {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				Utils.CURRENT_STATE_FILE_NAME))) {
			HashMap<String, String> thumbnailLinks = (HashMap<String, String>) ois
					.readObject();
			return thumbnailLinks;
		} catch (IOException | ClassNotFoundException e) {
			LOGGER.warning("Could not load previous state, " + e.toString());
			return null;
		}
	}

	public HashMap<String, String> getThumbnailsLinks() {
		return thumbnailsLinks;
	}

	public void run() {
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

	private void retrieveThumbnailsLinks() {
		for (String filetype : filetypes) {
			LOGGER.info("searching for filetypes " + filetype + "");
			System.out.println("searching for filetypes " + filetype + "");
			com.google.api.services.drive.Drive.Files.List request;
			try {
				request = service.files().list();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
			String searchQuery = "mimeType contains " + "'" + filetype + "'"
					+ " and trashed=false";
			request.setQ(searchQuery);
			List<File> searchResult = new ArrayList<File>();
			do {
				try {
					FileList files = request.execute();

					searchResult.addAll(files.getItems());
					request.setPageToken(files.getNextPageToken());
					System.out.print(".");
				} catch (IOException e) {
					LOGGER.warning("An error occurred during the search-query, msg: "
							+ e.toString());
					request.setPageToken(null);
				}
			} while (request.getPageToken() != null
					&& request.getPageToken().length() > 0);
			LOGGER.info(" found " + searchResult.size() + " files");

			int linkCounter = 0;
			for (File file : searchResult) {
				linkCounter++;
				if (linkCounter % 20 == 0) {
					saveStateToFile();
				}
				if (file.getThumbnailLink() == null) {
					// the file aren't a image
					LOGGER.fine("'"
							+ file.getTitle()
							+ "' doesn't have a thumbnail-link. Could be because google hasn't made one yet or the file isn't an image");
					continue;
				}
				String thumbnailLink = file.getThumbnailLink();
				thumbnailLink = Utils.removeSizeOfThumbnailPref(thumbnailLink);
				if (thumbnailsLinks.get(thumbnailLink) != null) {
					LOGGER.finer("already got the thumbnail-link for '" + file.getTitle() + "'");
					
				}
				List<String> fullPath;
				try {
					fullPath = getFullPath(service, file.getId());
				} catch (IOException e) {
					LOGGER.warning("Could not get information about the file '"
							+ file.getTitle() + "'" + " errormsg: "
							+ e.getMessage());
					continue;
				}
				if (fullPath.size() < 2) {
					// the file doesn't have any parent folder, is probably
					// archived or in the "share" area, do not download the
					// thumbnail
					LOGGER.fine("'"
							+ file.getTitle()
							+ "' does not have a parent folder, it is probably archived, will not be downloaded");
					continue;
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
				filePath.append(".jpg"); // TODO the thumbnails from
											// google-servers
											// are always(?) in jpg
				thumbnailsLinks.put(thumbnailLink, filePath.toString());
			}
			saveStateToFile();
		}

	}

	private List<String> getFullPath(Drive service, String fileId)
			throws IOException {
		LinkedList<String> ret = new LinkedList<String>();
		String currentFileId = fileId;
		for (int depth = 0; depth < 30; depth++) {
			String parentFileId = getParentId(service, currentFileId);
			if (parentFileId == null) {
				LOGGER.finest("'" + getTitleOfId(service, currentFileId)
						+ "' gets null for it parent;");
				return ret;
			}
			ret.add(0, getTitleOfId(service, currentFileId));
			currentFileId = parentFileId;
		}
		throw new IOException(
				"Depth of file exceeded 30 folders, a file's parent-loop?");
	}

	/**
	 * 
	 * @param service
	 * @param id
	 * @return the parent id or null if there
	 * @throws IOException
	 */
	private String getParentId(Drive service, String id) throws IOException {
		String parentId = fileAndParent.get(id);
		if (parentId == null) {
			File currentFile = service.files().get(id).execute();
			if (currentFile.getParents().size() == 0) {
				return null;
			}
			boolean foundRoot = false;
			for (ParentReference parent : currentFile.getParents()) {
				if (parent.getIsRoot()) {
					parentId = parent.getId();
					foundRoot = true;
					break;
				}
			}
			if (!foundRoot) {
				parentId = currentFile.getParents().get(0).getId();
			} else {
				// return null; //TODO check if this is correct next time online
			}
			fileAndParent.put(id, parentId);
		}
		return parentId;
	}

	private String getTitleOfId(Drive service, String id) throws IOException {
		String title = idToTitle.get(id);
		if (title == null) {
			File file = service.files().get(id).execute();
			title = file.getTitle();
			idToTitle.put(id, title);
		}
		return title;
	}

	public void printMaps() throws IOException {
		System.out.println("\tIdToTitle:");
		for (String key : idToTitle.keySet()) {
			System.out.println(key + " = '" + idToTitle.get(key) + "'");
		}
		System.out.println("\tfileAndParent:");
		for (String key : fileAndParent.keySet()) {
			System.out.print(key + "\t = " + fileAndParent.get(key));
			System.out.println(" ->\t" + getTitleOfId(service, key) + " = "
					+ getTitleOfId(service, fileAndParent.get(key)));
		}
		System.out.println("\tThumbnailLinks");
		for(String key : thumbnailsLinks.keySet()) {
			System.out.println(key + "\t = " + thumbnailsLinks.get(key));
		}
	}

	private void saveStateToFile() {
		LOGGER.finer("saving recieved links so far to file");
		try (ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(Utils.CURRENT_STATE_FILE_NAME))) {
			oos.writeObject(thumbnailsLinks);
		} catch (IOException e) {
			LOGGER.finer("Could not save current state, reason: "
					+ e.toString());
		}
	}
}
