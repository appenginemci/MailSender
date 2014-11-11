package com.sogeti.mci.mailsend.helper;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
//import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.drive.Drive;


/**
 * Implements methods for downloading files from a Drive Service
 * @author 
 *
 */
@Service
@Scope(value = "singleton")
public class Downloader {
	
	//static final Logger logger = Logger.getLogger(Downloader.class);
	
	@Autowired
	private LoggerDAO logger;
	
	/**
	 * Manages the download of the referenced file from Drive Service passed as parameter
	 * @throws ApplicationException
	 */
	public Multipart downloadFile(Drive service, String url, String hash) throws Exception {
		Multipart multipart = null;
		if (url != null && !url.isEmpty()) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
				MediaHttpDownloader downloader = new MediaHttpDownloader(HTTP_TRANSPORT, service.getRequestFactory().getInitializer());
				downloader.setDirectDownloadEnabled(false);
				downloader.download(new GenericUrl(url), baos);
				logger.debug(hash, "content has been downloaded from Drive");
			    String content = baos.toString();
			    String contentInlineStyles = ConvertCSSStyles.convert(content);
			    logger.debug(hash, "Downloaded string: " + baos.toString());
			    logger.debug(hash, "After style conversion: " + contentInlineStyles);
			    //message.setText(contentInlineStyles, "utf-8", "html");
			    MimeBodyPart mimeBodyPart = new MimeBodyPart();
			    mimeBodyPart.setContent(contentInlineStyles, "text/html");
			    mimeBodyPart.setHeader("Content-Type", "text/html; charset=utf-8");
			    multipart = new MimeMultipart();
			    multipart.addBodyPart(mimeBodyPart);
			    //message.setContent(content, "application/rtf; charset=utf-8");
			    logger.debug(hash, "content stored in Mime Message");
			} catch (Exception e) {
				throw e;
			} finally {
				try {
					baos.close();
				} catch (IOException ex) {
					logger.error(hash, "Failed to close output stream", ex);
				}
			}
		} 
		return multipart;
	}
	
	
	public Multipart downloadAttachment(Drive service, String url, Multipart multipart, String filename, String contentType, String hash)  throws Exception {
		
		if (multipart != null && url != null && !url.isEmpty() && filename != null && contentType != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//OutputStream outputStream = new FileOutputStream ("C:\\Temp\\tmp\\" + filename);
			try {
				HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
				MediaHttpDownloader downloader = new MediaHttpDownloader(HTTP_TRANSPORT, service.getRequestFactory().getInitializer());
				downloader.setDirectDownloadEnabled(false);
				/*downloader.download(new GenericUrl(url), outputStream);
				MimeBodyPart mimeBodyPart = new MimeBodyPart();
			    DataSource source = new FileDataSource("C:\\Temp\\tmp\\" + filename);
			    mimeBodyPart.setDataHandler(new DataHandler(source));
			    mimeBodyPart.setFileName(filename);
			    contentType = Files.probeContentType(FileSystems.getDefault().getPath("C:\\Temp\\tmp", filename));
			    multipart.addBodyPart(mimeBodyPart);*/
			    
				downloader.download(new GenericUrl(url), baos);
				MimeBodyPart mimeBodyPart = new MimeBodyPart();
				DataSource source = new ByteArrayDataSource(baos.toByteArray(), contentType);
				mimeBodyPart.setDataHandler(new DataHandler(source));
			    mimeBodyPart.setFileName(filename);
			    mimeBodyPart.setHeader("Content-Type", contentType);
			    multipart.addBodyPart(mimeBodyPart);
			   
			} catch (Exception e) {
				throw e;
			} finally {
				try {
					baos.close();
					//outputStream.close();
				} catch (IOException ex) {
					logger.error(hash, "Failed to close output stream", ex);
				}
			}
		} 
		return multipart;
	}
	
	public InputStream StringToInputStream(String messageContent){
		InputStream is = null;
		
		if(messageContent != null){
			if(!messageContent.trim().isEmpty()){
				is = new ByteArrayInputStream(messageContent.getBytes());
			}
		}
		
		return is;
	}
	
	

}
