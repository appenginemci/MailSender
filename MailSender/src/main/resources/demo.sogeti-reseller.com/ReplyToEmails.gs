function onInstall(e){
  onOpen(e)
}

function onOpen(e) {
  DocumentApp.getUi().createAddonMenu()
      .addItem('Reply', 'createReply')
      .addItem('Reset Reply', 'resetReply')
      .addItem('Send Reply', 'sendReply')
      .addItem('Add Attachment','addAttachement')
      .addToUi();
}

function removeReplyComposer(){
 var body = DocumentApp.getActiveDocument().getBody()
 var searchType = DocumentApp.ElementType.PARAGRAPH
 var searchHeading = DocumentApp.ParagraphHeading.HEADING1
 var searchText = "Compose a Reply:"
 var searchFrom = null
 var found = "false"
 var foundPar = null
 // Search until the paragraph is found.
 while (searchFrom = body.findElement(searchType, searchFrom)) {
   var par = searchFrom.getElement().asParagraph()
   if (par.getHeading() == searchHeading) {
     if (par.getText() == searchText ) {
       // Found one
       found = "true"
       foundPar = par
       break
     }
   }
 }
  
  if (found == "true") {
    var elu = foundPar.getPreviousSibling()
    var stop = "false"
    while (stop == "false") {
      if (elu.getType() == 'PAGE_BREAK') {
        elu = elu.getPreviousSibling() 
      }
      else if (elu.getType() == 'PARAGRAPH'){
        if (elu.getText().trim() != "") {
          stop = "true"
        } else {
          elu = elu.getPreviousSibling()
        }
      }
      else {
        stop = "true"
      }
    }    
    var eld = elu.getNextSibling()
    while (eld != null) {
      if (eld.isAtDocumentEnd()){
        eld = null
      } else {
        var eldTmp = eld.getNextSibling()
        eld.removeFromParent()
        eld = eldTmp
      }
    }
  }
}

function createReply() {
  removeReplyComposer()
  var body = DocumentApp.getActiveDocument().getBody()
  body.appendPageBreak()
  var header1 = body.appendParagraph("Compose a Reply:")
  header1.setHeading(DocumentApp.ParagraphHeading.HEADING1)
  var par1 = body.appendParagraph("")
  var header2 = body.appendParagraph("To:")
  header2.setHeading(DocumentApp.ParagraphHeading.HEADING2)
  var table1 = body.appendTable()
  var tr1 = table1.appendTableRow()
  var td1 = tr1.appendTableCell(getMetadata("From : "))
  tr1.setMinimumHeight(20)
  var header2 = body.appendParagraph("Cc:")
  header2.setHeading(DocumentApp.ParagraphHeading.HEADING2)
  var table2 = body.appendTable()
  var tr2 = table2.appendTableRow()
  var returnListEmailsCc = ""
  var listEmailsTo = getMetadata("To : ")
  if (listEmailsTo == null || listEmailsTo.trim() == ""){
    returnListEmailsCc = ""
  } else {
    var emailsTo = listEmailsTo.split(",")
    for (i=0; i<emailsTo.length; i++) {
      if ( removeEmailAlias(emailsTo[i].trim().toLowerCase()) != removeEmailAlias((getMetadata("Event Email Address : ")).trim().toLowerCase())){
        if (returnListEmailsCc != "") {
          returnListEmailsCc = returnListEmailsCc + ", "
        }
        returnListEmailsCc = returnListEmailsCc + emailsTo[i].trim()
      }
    } 
  }
  var listEmailsCc = getMetadata("Cc : ")
  if (listEmailsCc == null || listEmailsTo.trim() == ""){
    returnListEmailsCc = ""
  } else {
    var emailsCc = listEmailsCc.split(",")
    for (i=0; i<emailsCc.length; i++) {
      if ( removeEmailAlias(emailsCc[i].trim().toLowerCase()) != removeEmailAlias((getMetadata("Event Email Address : ")).trim().toLowerCase())){
        if (returnListEmailsCc != "") {
          returnListEmailsCc = returnListEmailsCc + ", "
        }
        returnListEmailsCc = returnListEmailsCc + emailsCc[i].trim()
      }
    } 
  }  
  var td2 = tr2.appendTableCell(returnListEmailsCc)
  
  
  tr2.setMinimumHeight(20)
  var header3 = body.appendParagraph("Subject:")
  header3.setHeading(DocumentApp.ParagraphHeading.HEADING2)
  var table3 = body.appendTable()
  var tr3 = table3.appendTableRow()
  tr3.setMinimumHeight(20)
  var td3 = tr3.appendTableCell(getMetadata("Subject : "))
  var header4 = body.appendParagraph("Message:")
  header4.setHeading(DocumentApp.ParagraphHeading.HEADING2)
  var table4 = body.appendTable()
  var tr4 = table4.appendTableRow()
  tr4.setMinimumHeight(150)
  var td4 = tr4.appendTableCell("")
  var par4 = body.appendParagraph("")
  var header5 = body.appendParagraph("Attachments:")
  header5.setHeading(DocumentApp.ParagraphHeading.HEADING2)
  var table5 = body.appendTable()
  var par5 = body.appendParagraph("")
  var position = DocumentApp.getActiveDocument().newPosition(td4, 0)
  DocumentApp.getActiveDocument().setCursor(position)
}

function resetReply(){
  removeReplyComposer()
  createReply()
}

function sendReply() {
  var body = DocumentApp.getActiveDocument().getBody()
  var errMessage = ""
  var addressesTo = null
  var addressesCc = null
  var subject = ""

  var toCell = getTableContent("To:", DocumentApp.ParagraphHeading.HEADING2) 
  if (toCell == null) {
    errMessage = "Incorrect mail form. Please refresh the mail form through 'Reset Reply' menu item, and write again your reply."
  }

  var ccCell = getTableContent("Cc:", DocumentApp.ParagraphHeading.HEADING2) 
  if (ccCell == null) {
    errMessage = "Incorrect mail form. Please refresh the mail form through 'Reset Reply' menu item, and write again your reply."
  }
  var subjectCell = getTableContent("Subject:", DocumentApp.ParagraphHeading.HEADING2) 
  if (subjectCell == null) {
    errMessage = "Incorrect mail form. Please refresh the mail form through 'Reset Reply' menu item, and write again your reply."
  }
  var messageCell = getTableContent("Message:", DocumentApp.ParagraphHeading.HEADING2)
  if (messageCell == null) {
    errMessage = "Incorrect mail form. Please refresh the mail form through 'Reset Reply' menu item, and write again your reply."
  }
  var attTab = getTable("Attachments:", DocumentApp.ParagraphHeading.HEADING2) 
  if (attTab == null) {
    errMessage = "Incorrect mail form. Please refresh the mail form through 'Reset Reply' menu item, and write again your reply."
  }
  var threadId = getMetadata("Thread Id : ")
  var mailId = getMetadata("Mail Id : ")
  
  var tempFolderId = getMetadata("Temporary Folder : ").trim()
  var tempFolder = checkFolder(tempFolderId)
  if (tempFolder == null) {
    errMessage = "The temporary folder is no longer accessible, please contact your administrator. \nSorry, your email could not be sent."
  }
  if (errMessage != "") {
    showAlert(errMessage) 
  } else {
    
    var replyTo = removeEmailAlias((getMetadata("Event Email Address : ")).trim())
    if (!validateEmail(replyTo)){
      errMessage = "The reply to address in the field 'Event Email Address is incorrect."
    } 
    if (errMessage != "") {
      showAlert(errMessage) 
    } else {    
      var toString = getSimpleCellContent(toCell)
      if (toString == "error-result-on-several-lines") {
        errMessage = "The addresses in field 'To' must be filled into a single line. Please correct."
        showAlert(errMessage) 
      } 
      else if (toString.trim() == "") {
        errMessage = "Please fill in destination addresses in field 'To'"
        showAlert(errMessage) 
      } 
      else {
        addressesTo = toString.split(",")
        for (i=0; i<addressesTo.length; i++) {
          if (!validateEmail(removeEmailAlias(addressesTo[i].trim()))) {
            errMessage = "Incorrect email address '" + addressesTo[i] + "' in field 'To'. Email addresses must be separated by ','."
            break
          } else {
            addressesTo[i] = removeEmailAlias(addressesTo[i].trim())
          }
        }
        if (errMessage != "") {
          showAlert(errMessage) 
        } else {
          var ccString = getSimpleCellContent(ccCell)
          if (ccString == "error-result-on-several-lines") {
            errMessage = "The addresses in field 'Cc' must be filled into a single line. Please correct."
            showAlert(errMessage)
          } 
          else {
            if (ccString.trim() == "") {
              addressesCc = new Array()
            } else {
              addressesCc = ccString.split(",")
              for (i=0; i<addressesCc.length; i++) {
                if (!validateEmail(removeEmailAlias(addressesCc[i].trim()))) { 
                  errMessage = "Incorrect email address '" + addressesCc[i] + "' in field 'Cc'. Email addresses must be separated by ','."
                  break
                } else {
                  addressesCc[i] = removeEmailAlias(addressesCc[i].trim())
                }
              }
            }
            if (errMessage != "") {
              showAlert(errMessage)
            } 
            else { 
              var attachmentsId = new Array()
              var idCount = 0
              var attachmentTab = getTable("Attachments:", DocumentApp.ParagraphHeading.HEADING2) 
              if (attachmentTab == null) {
                errMessage = "Incorrect mail form. Please refresh the mail form through 'Reset Reply' menu item, and write again your reply."
              } else {
                var nRows = attachmentTab.getNumRows()
                if (nRows > 0) {
                  for (i=0; i<nRows; i++) {
                    var nCells = attachmentTab.getRow(i).getNumCells()
                    if (nCells == 2){
                      var id = attachmentTab.getRow(i).getCell(1).getText().trim()
                      if (id != "") {
                        attachmentsId[idCount] = id
                        idCount++
                      }
                    }
                  }
                }
              }     
              if (errMessage != "") {
                showAlert(errMessage) 
              } else {
                subject = getSimpleCellContent(subjectCell)
                if (subject == "error-result-on-several-lines") {
                  errMessage = "The subject must be filled into a single line. Please correct."
                  showAlert(errMessage)
                } 
                if (errMessage != "") {
                  showAlert(errMessage) 
                } else {
                  var contentChildren = messageCell.getNumChildren()
                  var newDoc = DocumentApp.create('tempMail-' + getTimestamp())
                  var newBody = newDoc.getBody()
                  if (contentChildren > 0) {
                    var childCount = 0
                    for (j=0; j<contentChildren; j++) {
                      var childType = messageCell.getChild(j).getType()
                      if (childType == 'PARAGRAPH') {
                        var el = messageCell.getChild(j).asParagraph().copy()
                        var image = null
                        var childNumber = messageCell.getChild(j).asParagraph().getNumChildren()
                        if (childNumber > 0) {
                          for (k=0; k<childNumber; k++) {
                            if ( messageCell.getChild(j).asParagraph().getChild(k).getType() == 'INLINE_IMAGE') {
                              image = messageCell.getChild(j).asParagraph().getChild(k).asInlineImage().getBlob()
                              el.getChild(k).removeFromParent()
                            }
                          }
                        }
                        newBody.insertParagraph(childCount, el)
                        childCount++
                          if (image != null) {
                            newBody.insertImage(childCount, image)
                            childCount++
                          }
                      } else if (childType == 'TABLE') {
                        var el = messageCell.getChild(j).asTable().copy()
                        newBody.insertTable(childCount, el)
                        childCount++
                      } else if (childType == 'INLINE_IMAGE') { 
                        var el = messageCell.getChild(j).asInlineImage().getBlob()
                        newBody.insertImage(childCount, el)
                        childCount++
                      } else if (childType == 'LIST_ITEM') { 
                        var el = messageCell.getChild(j).asListItem().copy()
                        newBody.insertListItem(childCount, el)
                        childCount++
                      } else if (childType == 'TEXT') { 
                        var el = messageCell.getChild(j).asText().getText()
                        newBody.insertParagraph(childIndex, el)
                        childCount++
                      }
                    }
                  }
                  var newDocId = newDoc.getId()
                  newDoc.saveAndClose()
                  var newFile = DriveApp.getFileById(newDocId)
                  tempFolder.addFile(newFile)
                  DriveApp.removeFile(newFile)
                  //newFile = newFile.setOwner("mci.project@demo.sogeti-reseller.com")
       
                  var jsonObject = new Object()
                  jsonObject.threadId = threadId
                  jsonObject.mailId = mailId
                  jsonObject.docId = newDocId
                  jsonObject.docName = newDoc.getName()
                  jsonObject.addressesTo = addressesTo
                  jsonObject.addressesCc = addressesCc
                  jsonObject.subject = subject
                  jsonObject.eventAddress = replyTo
                  jsonObject.attachments = attachmentsId
                  //showAlert(JSON.stringify(jsonObject))
                  callWebservice(jsonObject)
                }
              }
            }
          }
        }
      }
    }
  }
}
      

function getTimestamp(){
  var formattedDate = Utilities.formatDate(new Date(), "GMT+1", "yyyyMMdd.HHmmss");
  return formattedDate
}

function getDate(){
  var formattedDate = Utilities.formatDate(new Date(), "GMT+1", "dd.MM.yyyy");
  return formattedDate
}

function getTime(){
  var formattedDate = Utilities.formatDate(new Date(), "GMT+1", "HH:mm:ss");
  return formattedDate
}

function showAlert(message) {
  var result = DocumentApp.getUi().alert(message);
}

function validateEmail(email) { 
    var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
} 

function removeEmailAlias(email) {
  var returnEmail = email
  var regExp = /<([^>]+)>/;
  var matches = regExp.exec(email);
  if (matches != null && matches.length > 0) {
    returnEmail = matches[1];
  }
  return returnEmail
}

function validateUrl(s) {
  var regexp = /(https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
  return regexp.test(s);
}


function validateAttachments(attachments){
  var returnMessage = ""
  if (attachments.length > 0) {
    for (i=0; i<attachments.length; i++) {
      if (!validateUrl(attachments[i])) {
        returnMessage = "Error in attachments field: " + attachments[i] + " is not a valid URL.\n"
      } 
      var urlFragments = new Array()
      urlFragments = attachments[i].match(/[^\/]+/g)
      var l = urlFragments.length
      if (l < 5) {
        returnMessage = "Error in attachments field: " + attachments[i] + " is not a valid URL."
      } else {
         var file = null
         try {
           file = DriveApp.getFileById(urlFragments[l-2]);
         } catch(e) {
           returnMessage = "Error in attachments field: the following file cannot be accessed \n" + attachments[i]
         }
         if (file == null) {
           returnMessage = "Error in attachments field: the following file cannot be accessed \n" + attachments[i]
         }
      }
    }
  }
  return returnMessage
}

function getTableContent(labelString, style) {
  var body = DocumentApp.getActiveDocument().getBody()
  var searchType = DocumentApp.ElementType.PARAGRAPH
  var searchFrom = null
  var found = "false"
  var foundPar = null
  var foundTab = null
  var returnCell = null
  while (searchFrom = body.findElement(searchType, searchFrom)) {
    var par = searchFrom.getElement().asParagraph()
    if (par.getHeading() == style) {
      if (par.getText().trim() == labelString ) {
        foundPar = par
      }
    }
  }
  if(foundPar != null){
    if(!foundPar.isAtDocumentEnd()){
      var el = foundPar.getNextSibling()
      while (found == "false") {
        if (el.getType() == 'TABLE') {
          found = "true"
          foundTab = el
        } else if (el.isAtDocumentEnd()) {
          break 
        } else {
          el = el.getNextSibling()
        }
      }
    }
  }
      
  if (found == "true") {
    var table = foundTab.asTable()
    if (table.getNumRows() == 1) {
      if (table.getRow(0).getNumCells() == 1){
        returnCell = foundTab.asTable().getCell(0, 0)
      }
    }
  }
  return returnCell
}

function getTable(labelString, style) {
  var body = DocumentApp.getActiveDocument().getBody()
  var searchType = DocumentApp.ElementType.PARAGRAPH
  var searchFrom = null
  var found = "false"
  var foundPar = null
  var foundTab = null
  var returnTab = null
  while (searchFrom = body.findElement(searchType, searchFrom)) {
    var par = searchFrom.getElement().asParagraph()
    if (par.getHeading() == style) {
      if (par.getText().trim() == labelString ) {
        foundPar = par
      }
    }
  }
  if(foundPar != null){
    if(!foundPar.isAtDocumentEnd()){
      var el = foundPar.getNextSibling()
      while (found == "false") {
        if (el.getType() == 'TABLE') {
          found = "true"
          foundTab = el
        } else if (el.isAtDocumentEnd()) {
          break 
        } else {
          el = el.getNextSibling()
        }
      }
    }
  }   
  if (found == "true") {
    returnTab = foundTab.asTable()
  }
  return returnTab
}

function getSimpleCellContent(cell){
  var result = null
  var contentChildren = cell.getNumChildren()
  if (contentChildren == 0) {
    result = ""
  } else if (contentChildren > 1) {
    result="error-result-on-several-lines"
  } else {
    result = ""
    var childCount = 0
    var childType = cell.getChild(0).getType()
    if (childType == 'PARAGRAPH') {
      result = cell.getChild(0).getText()
    }
  }
  return result
}
    

function getMultiLinesCellContent(cell){
  var result = new Array()
  var contentChildren = cell.getNumChildren()
  var resultCount = 0
  if (contentChildren > 0) {
    for (i=0; i<contentChildren; i++) {
      var childType = cell.getChild(i).getType()
      if (childType == 'PARAGRAPH') {
        var content = cell.getChild(i).getText().trim()
        if (content != "") {
          result[resultCount] = cell.getChild(i).getText().trim()
          resultCount++
        }
      }
    }
  }
  return result
}


function getMetadata(searchString) {
  var body = DocumentApp.getActiveDocument().getBody()
  var searchFrom = null
  var foundText = null
  var result = ""
  var rangeEl = body.findText(searchString, searchFrom)
  if (rangeEl != null) {
    foundText = rangeEl.getElement().asText().copy()
    if(foundText != null){
     var extract = foundText.deleteText(0,rangeEl.getEndOffsetInclusive()).getText()
     if (extract.charAt(0) == '\r' || extract.charAt(0) == '\n' || extract.charAt(0) == '\r\n') {
       result = ""
     } else {
       result = extract.match(/[^\r\n]+/g)[0]
     }
    }
  }
  return result
}

function getParagraphStartString(searchString) {
  var body = DocumentApp.getActiveDocument().getBody()
  var searchType = DocumentApp.ElementType.PARAGRAPH
  var searchFrom = null
  var foundPar = null
  while ((searchFrom = body.findElement(searchType, searchFrom)) && foundPar == null) {
    var par = searchFrom.getElement().asParagraph()
    if (containsString(par.getText(), searchString)) {
      foundPar = par
    }
  }
  return foundPar
}

function containsString(baseString, searchString) {
  return baseString.indexOf(searchString) != -1 
}

function callWebservice(json){
  var url = "https://smooth-tesla-778.appspot.com/mail/send";
  var payload = JSON.stringify(json);
  var headers = { "Accept":"application/json", 
              "Content-Type":"application/json", 
              "Authorization":"Basic _authcode_"
             };
  var options = { "method":"POST",
             "contentType" : "application/json",
            "headers": headers,
            "payload" : payload
           }; 
  
  var response = "Your email was not sent. Please retry later."
  try {
    response = UrlFetchApp.fetch(url, options);
  } catch(e) {
     response = "The mail server is not available.\nYour email was not sent. Please retry later."
  }
  if (response == "OK") {
    var par1 = getParagraphStartString("Reply by :")
    if (par1 != null){
      par1.replaceText("Reply by : .*$", "Reply by : " + Session.getActiveUser().getEmail() + " ")
    }
    var par2 = getParagraphStartString("Reply date :")
    if (par2 != null){
      par2.replaceText("Reply date : .*$", "Reply date : " + getDate() + " at " + getTime() + " ")
    }
    var par3 = getParagraphStartString("Status :")
    if (par3 != null){
      par3.replaceText("Status : .*$", "Status : reply sent" + " ")
    }
    showDialog("Your email has been sent");
  } else {
    showAlert(response)
  }
}


function addAttachement(){
  var uiInstance = HtmlService.createHtmlOutputFromFile('uploader').setTitle('Add Attachments').setHeight(100)
  DocumentApp.getUi().showSidebar(uiInstance);
}

function processFile(formObject) {
  var attachmentsFolderId = getMetadata("Attachments Folder : ").trim()
    var tempFolderId = getMetadata("Temporary Folder : ").trim()
  var tempFolder = checkFolder(tempFolderId)
  if (tempFolder == null) {
    showAlert("The temporary folder is no longer accessible, please contact your administrator. \nSorry, your email could not be sent.")
  } else {
    var formBlob = formObject.myFile;
    var driveFile = tempFolder.createFile(formBlob);
    driveFile = driveFile.setOwner("mci.project@demo.sogeti-reseller.com")
    var attachmentTab = getTable("Attachments:", DocumentApp.ParagraphHeading.HEADING2) 
    if (attachmentTab == null) {
      showAlert("Incorrect mail form. Please refresh the mail form through 'Reset Reply' menu item, and write again your reply.")
    } else {
      var tr1 = attachmentTab.appendTableRow()
      var td1 = tr1.appendTableCell(driveFile.getName())
      var td2 = tr1.appendTableCell(driveFile.getId())
      }
    showAlert("The attachment has been added")
  }
}

function checkFolder(id){
  var returnFolder = null 
  try {
    returnFolder = DriveApp.getFolderById(id)
    if (returnFolder != null) {
      if (returnFolder.getAccess(Session.getActiveUser().getEmail()) != DriveApp.Permission.EDIT && returnFolder.getAccess(Session.getActiveUser().getEmail()) != DriveApp.Permission.OWNER){
        returnFolder = null
      }
    }
  } catch(e) {
    showAlert("Permissions missing for folder " + id +". Ask your administrator.")
  }
  return returnFolder
}

function showDialog(message) {
var html = HtmlService.createHtmlOutput('<html><head/><body><div style="font-family: Arial">' 
                                            + message + '</div>' 
                                            + '<div><input type="button" value="Close" onclick="google.script.host.close()" /></div></body></html>')
                      .setWidth(200)
                      .setHeight(70);
  DocumentApp.getUi().showModalDialog(html,"Success:");
}