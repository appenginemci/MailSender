package com.sogeti.mci.mailsend.security;

import java.io.File;

import java.util.ArrayList;
import java.util.Properties;

//import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.sogeti.mci.mailsend.helper.LoggerDAO;

@Service
@Scope(value = "singleton")
public class GoogleServiceInitializator {
	//static final Logger logger = Logger.getLogger(GoogleServiceInitializator.class);
	
	@Autowired
	private LoggerDAO logger;
	
	private GoogleCredential googleCredentialDrive = null;
	private GoogleCredential googleCredentialGmail = null;

	public Drive getDriveService(){
		Drive drive = null;
		if (googleCredentialDrive == null) {
			logger.debug("", "About to generate new credentials for Google Services");
			googleCredentialDrive = generateGoogleCredentialDrive();
		}
		if(googleCredentialDrive != null){
			logger.debug("", "Valid credential for Google Services retrieved. About to instantiate Drive Service.");
			drive = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), googleCredentialDrive).setApplicationName("MailSender").build();
		} else {
			logger.error("", "Could not get valid credential for Google Services");
		}

		return drive;
	}

	public Gmail getGmailService(){
		Gmail gmail = null;
		if (googleCredentialGmail == null) {
			logger.debug("", "About to generate new credentials for Google Services");
			googleCredentialGmail = generateGoogleCredentialGmail();
		}
		if(googleCredentialGmail != null){
			logger.debug("", "Valid credential for Google Services retrieved. About to instantiate Gmail Service.");
			gmail = new Gmail.Builder(new NetHttpTransport(), new JacksonFactory(), googleCredentialGmail).setApplicationName("MailSender").build();
		} else {
			logger.error("", "Could not get valid credential for Google Services");
		}

		return gmail;
	}

	
	private GoogleCredential generateGoogleCredentialDrive(){
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		GoogleCredential googleCredential = null;
		Properties properties = new Properties();
		try {
			properties.load(GoogleServiceInitializator.class.getResourceAsStream("/drive-api-settings.properties"));
			logger.debug("", "Certificate Path: " + "/" + properties.getProperty("service.certificate.path"));
			logger.debug("", "Service Account Email: " + properties.getProperty("service.account.email"));
			File pk12File = new File(GoogleServiceInitializator.class.getResource("/" + properties.getProperty("service.certificate.path")).toURI());
			
			googleCredential = new GoogleCredential.Builder()
			.setTransport(httpTransport)
			.setJsonFactory(jsonFactory)
			.setServiceAccountId(properties.getProperty("service.account.email"))
			.setServiceAccountScopes(getScopeDrive())
			.setServiceAccountUser(properties.getProperty("user.email"))
			.setServiceAccountPrivateKeyFromP12File(pk12File)
			.build();		
			if (googleCredential == null) logger.error("", "Failed to get credential for Google Services");
		} catch (Exception e) {
			logger.error("", "Failed to initialize Google Services", e);
		}
		return googleCredential;
	}

	private GoogleCredential generateGoogleCredentialGmail(){
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		GoogleCredential googleCredential = null;
		Properties properties = new Properties();
		try {
			properties.load(GoogleServiceInitializator.class.getResourceAsStream("/drive-api-settings.properties"));
			logger.debug("", "Certificate Path: " + "/" + properties.getProperty("service.certificate.path"));
			logger.debug("", "Service Account Email: " + properties.getProperty("service.account.email"));
			File pk12File = new File(GoogleServiceInitializator.class.getResource("/" + properties.getProperty("service.certificate.path")).toURI());
			
			googleCredential = new GoogleCredential.Builder()
			.setTransport(httpTransport)
			.setJsonFactory(jsonFactory)
			.setServiceAccountId(properties.getProperty("service.account.email"))
			.setServiceAccountScopes(getScopeGmail())
			.setServiceAccountUser("apps.engine@mci-group.com")
			//.setServiceAccountUser("event.test.1@mci-group.com")
			//.setServiceAccountUser("arnaud.landier@mci-group.com")
			//.setServiceAccountUser(properties.getProperty("user.email"))
			.setServiceAccountPrivateKeyFromP12File(pk12File)
			.build();		
			if (googleCredential == null) logger.error("", "Failed to get credential for Google Services");
		} catch (Exception e) {
			logger.error("", "Failed to initialize Google Services", e);
		}
		return googleCredential;
	}

	private static ArrayList<String> getScopeDrive(){
		ArrayList<String> scopes = new ArrayList<String>();
		scopes.add(DriveScopes.DRIVE);
		return scopes;
	}
	
	private static ArrayList<String> getScopeGmail(){
		ArrayList<String> scopes = new ArrayList<String>();
		scopes.add(GmailScopes.GMAIL_READONLY);
		scopes.add(GmailScopes.GMAIL_COMPOSE);
		return scopes;
	}
	

}

