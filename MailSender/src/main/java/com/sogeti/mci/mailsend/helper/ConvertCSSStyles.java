package com.sogeti.mci.mailsend.helper;

import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ConvertCSSStyles {
	
	public static String convert(String html){
		final String style = "style";
        
        Document doc = Jsoup.parse(html);
        Elements els = doc.select(style);// to get all the style elements
        for (Element e : els) {
            String styleRules = e.getAllElements().get(0).data().replaceAll(
                    "\n", "").trim(), delims = "{}";
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
        return doc.toString();
	}
	
	private static String concatenateProperties(String oldProp, String newProp) {
	    oldProp = oldProp.trim();
	    if (!newProp.endsWith(";"))
	       newProp += ";";
	    return newProp + oldProp; // The existing (old) properties should take precedence.
	}
}
