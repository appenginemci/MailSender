package com.sogeti.mci.mailsend.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

//import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.sogeti.mci.mailsend.helper.LoggerDAO;
import com.sogeti.mci.mailsend.model.Mail;
import com.sogeti.mci.mailsend.security.GoogleServiceInitializator;

@Service
@Scope(value = "singleton")
public class GmailService {

	//static final Logger logger = Logger.getLogger(GmailService.class);
	
	@Autowired
	private LoggerDAO logger;

	@Autowired
	private GoogleServiceInitializator googleServiceInitializator;

	private Gmail gmail = null;

	
	public String sendMail(Mail mail, Multipart multipart){
		String result = "Your email could not be sent. Please retry later.";
		MimeMessage message = null;
		logger.debug(mail.getHash(), "starting processing email sending");
		if (gmail == null){
			gmail = googleServiceInitializator.getGmailService();
			logger.debug(mail.getHash(), "Gmail service has been initialized");
		}
		if (gmail != null){
			try {
				try {
					Properties props = new Properties();
				    Session session = Session.getDefaultInstance(props, null);
				    message = new MimeMessage(session);
					Message origMessage = gmail.users().messages().get(googleServiceInitializator.getProperties().getProperty("user.email"), mail.getMailId()).setFormat("raw").execute();
					byte[] emailBytes = Base64.decodeBase64(origMessage.getRaw());
				    /*String previousEmailContent = convert(emailBytes);
				    logger.debug(mail.getHash(), "previous email: " + previousEmailContent);*/
				   
				    
				    MimeMessage emailOrig = new MimeMessage(session, new ByteArrayInputStream(emailBytes));
				    
				    Enumeration<Header> allHeaders =  emailOrig.getAllHeaders();
					if (allHeaders != null) {
						while (allHeaders.hasMoreElements()) {
							Header header=allHeaders.nextElement();
							logger.debug(mail.getHash(), "HEADER - " + header.getName() + ":" + header.getValue());
							if(header.getName().toLowerCase().equals("References".toLowerCase())){
								message.addHeader("References", header.getValue());
								logger.debug(mail.getHash(), "Adding " + "References:" + header.getValue());
							} else if (header.getName().toLowerCase().equals("Message-ID".toLowerCase())){
								logger.debug(mail.getHash(), "Adding " + "In-Reply-To:" + header.getValue());
								logger.debug(mail.getHash(), "Adding " + "References:" + header.getValue());
								message.addHeader("References", header.getValue());
								message.setHeader("In-Reply-To", header.getValue());
							}
					    }
					}
					
				} catch (Exception e){
					logger.info(mail.getHash(), "could not retrieve header info from source mail", e);
				}
				message.setContent(multipart);
				if (mail.getSubject() != null) message.setSubject(mail.getSubject());
				if (mail.getAddressesTo() != null && mail.getAddressesTo().length > 0){
					String[] to = mail.getAddressesTo();
					for (int i=0; i < to.length; i++){
						if (!to[i].trim().equals("")) {
							InternetAddress tAddress = new InternetAddress(to[i]);
							message.addRecipient(javax.mail.Message.RecipientType.TO, tAddress);
						}
					}
				}
				if (mail.getAddressesCc() != null && mail.getAddressesCc().length > 0){
					String[] cc = mail.getAddressesCc();
					for (int i=0; i < cc.length; i++){
						if (!cc[i].trim().equals("")) {
							InternetAddress tAddress = new InternetAddress(cc[i]);
							message.addRecipient(javax.mail.Message.RecipientType.CC, tAddress);
						}
					}
				}
				if (mail.getEventAddress() != null && !mail.getEventAddress().trim().equals("")){
					InternetAddress[] tAddressReplyTo = new InternetAddress[1];
					tAddressReplyTo[0] = new InternetAddress(mail.getEventAddress().trim());
					message.setReplyTo(tAddressReplyTo);
				}
				message.setFrom(new InternetAddress(mail.getEventAddress().trim()));
				Message gmailMessage = createMessageWithEmail(message);
				if (mail.getThreadId() != null){
					gmailMessage.setThreadId(mail.getThreadId());
				}

				logger.debug(mail.getHash(), "about to call Gmail API");
				gmailMessage = gmail.users().messages().send(googleServiceInitializator.getProperties().getProperty("user.email"), gmailMessage).execute();
				//gmailMessage = gmail.users().messages().send("event.test.1@mci-group.com", gmailMessage).execute();
				//gmailMessage = gmail.users().messages().send("arnaud.landier@mci-group.com", gmailMessage).execute();
				logger.debug(mail.getHash(), gmailMessage.toPrettyString());
				result = "OK";
			} catch (AddressException ex) {
				logger.error(mail.getHash(), "One of the addresses in To or Cc is wrong \n" + mail.toString(), (Exception) ex);
				result = "Your email could not be sent, as one of the address in To or Cc was incorrect.";
			} catch (Exception e){
				logger.error(mail.getHash(), "email could not be sent", e);
			}
		} else {
			logger.error(mail.getHash(), "Gmail service could not be initialized");
		}


		return result;
	}
	
	private MimeMessage buildNewMimeMessage_AsReply(Mail mail, Multipart multipart) {
		Properties props = new Properties();
	    Session session = Session.getDefaultInstance(props, null);
	    MimeMessage message = new MimeMessage(session);
	    //MimeMessage message = null;
		if (gmail == null){
			gmail = googleServiceInitializator.getGmailService();
			logger.debug(mail.getHash(), "Gmail service has been initialized");
		}
		if (gmail != null){
			try {
					Message origMessage = gmail.users().messages().get(googleServiceInitializator.getProperties().getProperty("user.email"), mail.getMailId()).setFormat("raw").execute();
					MimeBodyPart forwardedContent = new MimeBodyPart();
				    byte[] emailBytes = Base64.decodeBase64(origMessage.getRaw());
				    MimeMessage emailOrig = new MimeMessage(session, new ByteArrayInputStream(emailBytes));
				    forwardedContent.setDataHandler(emailOrig.getDataHandler());
				    multipart.addBodyPart(forwardedContent);
				    Enumeration<Header> allHeaders =  emailOrig.getAllHeaders();
					if (allHeaders != null) {
						while (allHeaders.hasMoreElements()) {
							Header header=allHeaders.nextElement();
							logger.debug(mail.getHash(), "HEADER - " + header.getName() + ":" + header.getValue());
							if(header.getName().toLowerCase().equals("References".toLowerCase())){
								message.addHeader("References", header.getValue());
								logger.debug(mail.getHash(), "Adding " + "References:" + header.getValue());
							} else if (header.getName().toLowerCase().equals("Message-ID".toLowerCase())){
								logger.debug(mail.getHash(), "Adding " + "In-Reply-To:" + header.getValue());
								logger.debug(mail.getHash(), "Adding " + "References:" + header.getValue());
								message.addHeader("References", header.getValue());
								message.setHeader("In-Reply-To", header.getValue());
							}
					    }
					}
				    
				    //message = (MimeMessage) emailOrig.reply(false);
	
			} catch (Exception e){
				logger.info(mail.getHash(), "could not retrieve header info from source mail", e);
			}
		}
		return message;
	}

	private Message createMessageWithEmail(MimeMessage email) throws MessagingException, IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		email.writeTo(bytes);
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}
	
	private String convert(byte[] data) {
	    StringBuilder sb = new StringBuilder(data.length);
	    for (int i = 0; i < data.length; ++ i) {
	        if (data[i] < 0) throw new IllegalArgumentException();
	        sb.append((char) data[i]);
	    }
	    return sb.toString();
	}

}
