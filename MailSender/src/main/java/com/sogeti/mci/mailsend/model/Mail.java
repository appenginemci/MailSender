package com.sogeti.mci.mailsend.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Mail {
	String threadId;
	String mailId;
	String docId;
	String docName;
	String subject;
	String addressesTo[];
	String addressesCc[];
	String attachments[];
	String eventAddress;
	String hash = null;
	
	
	/**
	 * @return the threadId
	 */
	public String getThreadId() {
		return threadId;
	}
	/**
	 * @param threadId the threadId to set
	 */
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
	/**
	 * @return the mailId
	 */
	public String getMailId() {
		return mailId;
	}
	/**
	 * @param mailId the mailId to set
	 */
	public void setMailId(String mailId) {
		this.mailId = mailId;
	}
	/**
	 * @return the docId
	 */
	public String getDocId() {
		return docId;
	}
	/**
	 * @param docId the docId to set
	 */
	public void setDocId(String docId) {
		this.docId = docId;
	}
	/**
	 * @return the docName
	 */
	public String getDocName() {
		return docName;
	}
	/**
	 * @param docName the docName to set
	 */
	public void setDocName(String docName) {
		this.docName = docName;
	}
	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}
	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	/**
	 * @return the addressesTo
	 */
	public String[] getAddressesTo() {
		return addressesTo;
	}
	/**
	 * @param addressesTo the addressesTo to set
	 */
	public void setAddressesTo(String[] addressesTo) {
		this.addressesTo = addressesTo;
	}
	/**
	 * @return the addressesCc
	 */
	public String[] getAddressesCc() {
		return addressesCc;
	}
	/**
	 * @param addressesCc the addressesCc to set
	 */
	public void setAddressesCc(String[] addressesCc) {
		this.addressesCc = addressesCc;
	}
	
	
	/**
	 * @return the attachments
	 */
	public String[] getAttachments() {
		return attachments;
	}
	/**
	 * @param attachements the attachments to set
	 */
	public void setAttachments(String[] attachments) {
		this.attachments = attachments;
	}
	
	/**
	 * @return the eventAddress
	 */
	public String getEventAddress() {
		return eventAddress;
	}
	/**
	 * @param eventAddress the eventAddress to set
	 */
	public void setEventAddress(String eventAddress) {
		this.eventAddress = eventAddress;
	}
	public String toString(){
		String value = "threadId: " + threadId + "\nmailId: " + mailId + "\ndocId: " + docId + "\ndocName: " + docName + "\nsubject: " + subject + "\neventAddress: " + eventAddress + "\n";
		if (addressesTo.length > 0){
			for (int i = 0; i <= addressesTo.length-1; i++){
				value = value + "addressesTo: " + addressesTo[i] + "\n";
			}
		}
		if (addressesCc.length > 0){
			for (int i = 0; i <= addressesCc.length-1; i++){
				value = value + "addressesCc: " + addressesCc[i] + "\n";
			}
		}
		if (attachments.length > 0){
			for (int i = 0; i <= attachments.length-1; i++){
				value = value + "attachments: " + attachments[i] + "\n";
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		value = value + "date: " +sdf.format(new Date());
		
		return value; 
	}
	
	/**
	 * @return the hash
	 */
	public String getHash() {
		if (hash == null) {
			hash = Integer.toString(toString().hashCode());
		}
		return hash;
	}
	
	
	
}
