/**
 * 
 */
package tests;

import java.util.HashSet;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import managers.ProxyManager;
import models.WebBrowser;

/**
 * @author Martin
 *
 */
public class WebTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		WebBrowser web = new WebBrowser();

		int page_counter = 0;
		HtmlPage page = web.getPage("http://www.eur.nl");
		page_counter++;
		
		for(HtmlAnchor anchor : page.getAnchors()){
			page = anchor.click();
			page_counter++;
			System.out.println(page_counter + "\t" + page.getUrl().toString());
//			System.out.println(anchor.getHrefAttribute());
		}
		
		web.close();
	}

}
