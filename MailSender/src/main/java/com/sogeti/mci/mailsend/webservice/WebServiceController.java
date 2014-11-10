package com.sogeti.mci.mailsend.webservice;

import javax.mail.Multipart;

 
//import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sogeti.mci.mailsend.helper.LoggerDAO;
import com.sogeti.mci.mailsend.model.Mail;
import com.sogeti.mci.mailsend.services.DriveService;
import com.sogeti.mci.mailsend.services.GmailService;

@RestController
@RequestMapping( value = "/mail" )
public class WebServiceController {

	//static final Logger logger = Logger.getLogger(WebServiceController.class);
	
	@Autowired
	private LoggerDAO logger;
	
	@Autowired
	private DriveService driveService;
	
	@Autowired
	private GmailService gmailService;

	
	@RequestMapping(value = "/send", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String sendMail(@RequestBody Mail mail) {  
		String result = "Your email could not be sent. Please retry later.";
		try {  
			logger.info(mail.getHash(), mail.toString());
			if (mail.getDocId() != null){
				Multipart multipart = driveService.getFile(mail);
				if (multipart != null){
					multipart = driveService.getAttachments(mail, multipart);
					if (multipart != null){	
						result = gmailService.sendMail(mail, multipart);	
						logger.info(mail.getHash(), "Send mail result: " + result);
						if(result == "OK") {
							driveService.deleteTempFiles(mail);
							logger.debug(mail.getHash(), "The following email was sent: \n" + mail.toString());
						} else {
							logger.error(mail.getHash(), "Error while sending this email: " + mail.toString() );
						}
					} else {
						logger.error(mail.getHash(), "Error while downloading attachments for this email " + mail.toString() );
					}
				} else {
					logger.error(mail.getHash(), "Error while getting document content for this email: " + mail.toString());
				}
			} else {
				logger.error(mail.getHash(), "Document id is null");
			}
		} catch (Exception e) {  
			logger.error(mail.getHash(), "Error while sending this email: " + mail.toString(),e);
		}  
		return result;
	}
	
}

