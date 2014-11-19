package com.sogeti.mci.mailsend.helper;

import java.util.StringTokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ConvertCSSStyles {
	
	public static String convert(String html){
		final String style = "style";
        
		String tmpHTML = html.replaceAll("&rsquo;", "##RightSimpleQuote").replaceAll("&lsquo;", "##LeftSimpleQuote")
				.replaceAll("&rdquo;", "##RightDoubleQuote").replaceAll("&ldquo;", "##LeftDoubleQuote").replaceAll("li:before","li-before");
		
		
        Document doc = Jsoup.parse(tmpHTML);
        Elements els = doc.select(style);// to get all the style elements
        for (Element e : els) {
            String styleRules = e.getAllElements().get(0).data().replaceAll("\n", "").trim(), delims = "{}";
            StringTokenizer st = new StringTokenizer(styleRules, delims);
            while (st.countTokens() > 1) {
                String selector = st.nextToken(), properties = st.nextToken();
                Elements selectedElements = doc.select(selector);
                for (Element selElem : selectedElements) {
                    String oldProperties = selElem.attr(style);
                    selElem.attr(style,
                            oldProperties.length() > 0 ? concatenateProperties(
                                    oldProperties, properties) : properties);
                }
            }
            e.remove();
        }
        
        // Replace padding in style from body element
        String pattern = "(<body.*style=\".*padding:)([^\"^;]*)([\";].*)";
		String newBodyPaddingStyle = "10pt 10pt 10pt 10pt";
        
        return doc.outerHtml().replaceAll( "##RightSimpleQuote", "&rsquo;").replaceAll("##LeftSimpleQuote", "&lsquo;")
				.replaceAll("##RightDoubleQuote", "&rdquo;").replaceAll("##LeftDoubleQuote","&ldquo;").replaceAll("li-before","li:before")
				.replace("</head>", "<style type=\"text/css\">li:before {content: \"-\"; padding-right: 5px;}</style></head>")
				.replaceFirst(pattern, "$1" + newBodyPaddingStyle + "$3");
        //return doc.toString();
	}
	
	private static String concatenateProperties(String oldProp, String newProp) {
	    oldProp = oldProp.trim();
	    if (!newProp.endsWith(";"))
	       newProp += ";";
	    return newProp + oldProp; // The existing (old) properties should take precedence.
	}
}
