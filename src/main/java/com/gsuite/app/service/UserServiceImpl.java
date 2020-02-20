package  com.gsuite.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.Users;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.reports.Reports;
import com.google.api.services.admin.reports.ReportsScopes;
import com.google.api.services.admin.reports.model.Activities;
import com.google.api.services.admin.reports.model.Activity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;



@Repository
public class UserServiceImpl implements UserService {

	@Autowired
	private MongoTemplate mongoTemplate;

	private static final String APPLICATION_NAME = "gsuite-spring";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final String CREDENTIALS_FILE_PATH = "client_secret.json";
	static List<String> SCOPES = new ArrayList<String>();

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

		SCOPES.add(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY);
		SCOPES.add(DirectoryScopes.ADMIN_DIRECTORY_USER);
		SCOPES.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP);
		SCOPES.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP_MEMBER);
		// users 
		SCOPES.add("https://www.googleapis.com/auth/admin.directory.user.readonly");
		SCOPES.add("https://www.googleapis.com/auth/admin.directory.user");
		SCOPES.add("https://www.googleapis.com/auth/drive.file");
		SCOPES.add("https://www.googleapis.com/auth/drive.metadata");
		SCOPES.add("https://www.googleapis.com/auth/drive.appdata");

		SCOPES.add("https://www.googleapis.com/auth/admin.reports.audit.readonly");

		//		java.io.File clientSecretFilePath = new java.io.File(CREDENTIALS_FOLDER, CLIENT_SECRET_FILE_NAME);
		//		if (!clientSecretFilePath.exists()) {
		//			throw new FileNotFoundException("Please copy " + CLIENT_SECRET_FILE_NAME + " to folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
		//		}
		//		InputStream in = new FileInputStream(clientSecretFilePath);
		//		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
		//		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
		//				clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(CREDENTIALS_FOLDER)).setAccessType("offline").build();
		//		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

		InputStream in = AdminSDKDirectoryQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	@Override
	public List<User> getAllUsers() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Directory service = new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME)
				.build();

		// Print the first 10 users in the domain.
		Users result = service.users().list().execute();
		List<User> users = result.getUsers();
		return users;
	}


	public List<Activity> getUserActivities() throws GeneralSecurityException, IOException {
		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Reports service = new Reports.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME)
				.build();

		// Print the last 10 login events.
		String userKey = "all";
		String applicationName = "login";
		Activities result = service.activities().list(userKey, applicationName)
				.setMaxResults(10)
				.execute();
		List<Activity> activities = result.getItems();
		return activities;
	}
}
