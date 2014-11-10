package com.sogeti.mci.mailsend.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
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
		Properties props = new Properties();
	    Session session = Session.getDefaultInstance(props, null);
	    MimeMessage message = new MimeMessage(session);
	    logger.debug(mail.getHash(), "starting processing email sending");
		if(mail != null && message != null){
			if (gmail == null){
				gmail = googleServiceInitializator.getGmailService();
				logger.debug(mail.getHash(), "Gmail service has been initialized");
			}
			if (gmail != null){
				try {
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
					if (mail.getEventAddress() != null && mail.getEventAddress().trim() != ""){
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
					gmailMessage = gmail.users().messages().send("apps.engine@mci-group.com", gmailMessage).execute();
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
		} else {
			if (message == null) logger.error(mail.getHash(), "Error while retrieving the content of the email");
		}

		return result;
	}

	private Message createMessageWithEmail(MimeMessage email) throws MessagingException, IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		email.writeTo(bytes);
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}

}
