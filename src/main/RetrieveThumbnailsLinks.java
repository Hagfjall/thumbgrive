package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

	private int fileCounter = 0;
	private HashMap<String, ThumbnailPath> idToThumbnails;
	private HashMap<String, String> idToTitle;
	private HashMap<String, String> idToParent;
	private ThumbnailPathHolder thumbnailPathHolder;

	public RetrieveThumbnailsLinks(ThumbnailPathHolder thumbnailPathHolder,
			int thumbnailSize, String... filetypes) {
		LOGGER.setLevel(Level.ALL);
		this.thumbnailPathHolder = thumbnailPathHolder;
		Utils.THUMBNAIL_SIZE_PREF = thumbnailSize;
		this.filetypes = filetypes;
		Object read = Utils.readObjectFromFile(Utils.CURRENT_STATE_FILE_NAME);
		if (read instanceof HashMap<?, ?>)
			idToThumbnails = (HashMap<String, ThumbnailPath>) read;
		if (idToThumbnails == null) {
			idToThumbnails = new HashMap<String, ThumbnailPath>();
			LOGGER.finer(Utils.CURRENT_STATE_FILE_NAME + " not loaded...");
		} else {
			LOGGER.finer(Utils.CURRENT_STATE_FILE_NAME + " loaded with "
					+ idToThumbnails.size() + " links");
		}
		idToTitle = new HashMap<String, String>();
		idToParent = new HashMap<String, String>();
		try {
			credentials = loadCredentials();
			service = GoogleApi.buildService(credentials);
		} catch (CodeExchangeException e) {
			LOGGER.warning("could not load the credentials... msg: "
					+ e.toString());
		}
	}

	//
	// public HashMap<String, ThumbnailPath> getThumbnailsLinks() {
	// return fileIdThumbnails;
	// }

	private GoogleCredential loadCredentials() throws CodeExchangeException {
		GoogleCredential credentials = GoogleApi.getStoredCredentialsInFile();
		if (credentials == null) { // getting all new tokens
			String url = GoogleApi.getAuthorizationUrl();
			System.out.println(url);
			System.out
					.println("Please use this url to get the key from google and paste it here");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			String responseCode;
			try {
				responseCode = br.readLine();
				br.close();
				credentials = GoogleApi
						.exchangeCodeAndStoreCredentials(responseCode);
			} catch (IOException e) {
				return credentials = null;
			}
		}
		return credentials;

	}

	private List<File> searchResult(String searchQuery) throws IOException {
		com.google.api.services.drive.Drive.Files.List request;
		request = service.files().list();

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
		return searchResult;
	}

	private void buildFolderTree(List<File> searchResult) {
		for (File file : searchResult) {
			saveStateToFile();
			String thumbnailLink = file.getThumbnailLink();
			if (thumbnailLink == null) {
				// the file aren't a image
				LOGGER.fine("'"
						+ file.getTitle()
						+ "' doesn't have a thumbnail-link. Could be because google hasn't made one yet or the file isn't an image");
				continue;
			}
			thumbnailLink = Utils.changeSizeOfThumbnailToPref(thumbnailLink);
			List<String> fullPath;
			try {
				fullPath = getFullPath(service, file.getId());
			} catch (IOException e) {
				LOGGER.warning("Did not get the full path of the file '"
						+ file.getTitle() + "'" + " reason: " + e.getMessage());
				continue;
			}

			StringBuilder filePath = new StringBuilder(256);
			for (int i = 1; i < fullPath.size(); i++) {
				String partOfFullPath = fullPath.get(i);
				if (i != fullPath.size() - 1)
					partOfFullPath = Utils
							.makeStringFilenameSafe(partOfFullPath)
							+ java.io.File.separator;
				filePath.append(partOfFullPath);
			}
			filePath.append(".jpg"); // the thumbnails from
										// google-servers
										// are always(?) in jpg
			ThumbnailPath temp = new ThumbnailPath(filePath.toString(),
					thumbnailLink);
			if(idToThumbnails.put(file.getId(), temp) == null);
			// no need to download a file twice...
				thumbnailPathHolder.insert(temp);
		}
		fileCounter = -1;
		saveStateToFile();
	}

	private List<String> getFullPath(Drive service, String fileId)
			throws IOException {
		LinkedList<String> path = new LinkedList<String>();
		String currentFileId = fileId;
		for (int depth = 0; depth < 30; depth++) {
			String parentFileId = getParentId(service, currentFileId);
			if (parentFileId == null) {
				LOGGER.finest("'" + getTitleOfId(service, currentFileId)
						+ "' gets null for it parent;");
				return path;
			}
			if (parentFileId.equals("ROOT_FOLDER")) {
				path.add(0, getTitleOfId(service, currentFileId));
				return path;
			}
			path.add(0, getTitleOfId(service, currentFileId));
			currentFileId = parentFileId;
		}
		String spath = "";
		for (String s : path) {
			spath += s + "/";
		}
		LOGGER.finer("DEPTH-EXCEPTION-PATH: " + spath);
		throw new IOException(
				"Depth of file exceeded 30 folders, a file's parent-loop?");
	}

	private String getParentId(Drive service, String id) throws IOException {
		String parentId = idToParent.get(id);
		if (parentId == null) {
			File currentFile = service.files().get(id).execute();
			// TODO not needed when the search-q is in order
			if (currentFile.getParents().size() == 0) {
				throw new IOException("'" + currentFile.getTitle()
						+ "' doesn't have any parent, archived?");
			}
			for (ParentReference parent : currentFile.getParents()) {
				if (parent.getIsRoot()) {
					parentId = parent.getId();
					idToParent.put(parentId, "ROOT_FOLDER");
					idToParent.put(id, parentId);

					// TODO this can be a problem if it's not the first parent
					// that are the root-folder... solve!
				}
			}
			parentId = currentFile.getParents().get(0).getId();
			idToParent.put(id, parentId);
		}

		return parentId;
	}

	private String getTitleOfId(Drive service, String id) throws IOException {
		String title = idToTitle.get(id);
		if (title == null) {
			File file = service.files().get(id).execute();
			title = file.getTitle();
			if (file.getMimeType().equals("application/vnd.google-apps.folder"))
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
		for (String key : idToParent.keySet()) {
			System.out.print(key + "\t = " + idToParent.get(key));
			if (idToParent.get(key).equals("ROOT_FOLDER")) {
				System.out.println(" ->\t" + getTitleOfId(service, key) + " = "
						+ idToParent.get(key));
			} else {
				System.out.println(" ->\t" + getTitleOfId(service, key) + " = "
						+ getTitleOfId(service, idToParent.get(key)));
			}
		}
		System.out.println("\tFileIdToThumbnails");
		for (String key : idToThumbnails.keySet()) {
			System.out.println(key + "\t = " + idToThumbnails.get(key));
		}
	}

	private void saveStateToFile() {
		fileCounter++;
		if (fileCounter % 20 == 0) {
			LOGGER.finer("saving recieved links so far to file");
			Utils.writeObjectToFile(idToThumbnails,
					Utils.CURRENT_STATE_FILE_NAME);
		}
	}

	public void run() {
		for (String filetype : filetypes) {
			LOGGER.info("searching for filetypes " + filetype + "");
			System.out.println("searching for filetypes " + filetype + "");
			String searchQuery = "mimeType contains " + "'" + filetype + "'"
					+ " and trashed=false";
			// TODO add this to query (and make it work) ->
			// "'root' in parents and " +
			try {
				List<File> searchResult = searchResult(searchQuery);
				buildFolderTree(searchResult);
			} catch (IOException e) {
				LOGGER.warning("Coult not get the Google API request, reson: "
						+ e.toString());
			}
		}
		thumbnailPathHolder.setAllLoaded();
	}

}
