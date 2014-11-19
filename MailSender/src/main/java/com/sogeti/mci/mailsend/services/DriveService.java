package com.sogeti.mci.mailsend.services;

//import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import javax.mail.Multipart;

import com.sogeti.mci.mailsend.helper.Downloader;
import com.sogeti.mci.mailsend.helper.LoggerDAO;
import com.sogeti.mci.mailsend.model.Mail;
import com.sogeti.mci.mailsend.security.GoogleServiceInitializator;

@Service
@Scope(value = "singleton")
public class DriveService {

	//static final Logger logger = Logger.getLogger(DriveService.class);

	@Autowired
	private GoogleServiceInitializator googleServiceInitializator;

	@Autowired
	private Downloader downloader;
	
	@Autowired
	private LoggerDAO logger;

	private Drive drive = null;


	public Multipart getFile(Mail mail){
		String id = mail.getDocId();
		Multipart multipart = null;
		if (drive == null){
			drive = googleServiceInitializator.getDriveService();
		}
		if (drive != null){
			try {
				Drive.Files.Get request = drive.files().get(id);
				File file = request.execute();
				if(file != null){
					String url = file.getExportLinks().get("text/html");
					if(url != null && !url.trim().isEmpty()){
						logger.debug(mail.getHash(), "download URL: " + url);
						multipart = downloader.downloadFile(drive, url, mail.getHash());
					} else {
						logger.error(mail.getHash(), "the URL for retrieving the Drive doc is null or empty");
					}
				} else {
					logger.error(mail.getHash(), "The file is null");
				}
			} catch (Exception e){
				logger.error(mail.getHash(), "problem while downloading content file with id " + id,e);
				multipart = null;
			}
		} else {
			logger.error(mail.getHash(), "Drive Service could not be initialized");
		}
		if (multipart == null) logger.error(mail.getHash(), "Error while retrieving doc content from Drive");
		return multipart;
	}
	
	
	public Multipart getAttachments(Mail mail, Multipart multipart1){
		if (drive == null){
			drive = googleServiceInitializator.getDriveService();
		}
		if (drive != null){
			if (mail.getAttachments() != null && mail.getAttachments().length > 0){
				for (int i=0; i < mail.getAttachments().length; i++){
					try {
						Drive.Files.Get request = drive.files().get(mail.getAttachments()[i]);
						File file = request.execute();
						if(file != null){
							logger.debug(mail.getHash(), "URL: " + file.getDownloadUrl());
							multipart1 = downloader.downloadAttachment(drive, file.getDownloadUrl(), multipart1, file.getTitle(), file.getMimeType(), mail.getHash());
						} 
					} catch (Exception e) {
						logger.error(mail.getHash(), "Error while downloading attachment file with id: " + mail.getAttachments()[i], e);
						multipart1 = null;
					}
				}
			}
		}
		return multipart1;
	}
	
	public void deleteTempFiles(Mail mail){
		if (drive == null){
			drive = googleServiceInitializator.getDriveService();
		}
		if (drive != null){
			try {
				Drive.Files.Delete request = drive.files().delete(mail.getDocId());
				request.execute();
			} catch (Exception e){
				logger.error(mail.getHash(), "The temporary file with id " + mail.getDocId() + " could not be deleted from Drive", e);
			}
			if (mail.getAttachments() != null && mail.getAttachments().length > 0){
				for (int i=0; i < mail.getAttachments().length; i++){
					try {
						Drive.Files.Delete request = drive.files().delete(mail.getAttachments()[i]);
						request.execute();
					} catch (Exception e) {
						logger.error(mail.getHash(), "Error while deleting attachment file with id: " + mail.getAttachments()[i]);
					}
				}
			}
		} else {
			logger.error(mail.getHash(), "Drive Service could not be initialized");
		}	
	}

}
