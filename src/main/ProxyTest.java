/**
 * 
 */
package main;

import java.io.IOException;
import java.util.HashSet;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import managers.Manager;
import managers.ProxyManager;
import models.WebBrowser;

/**
 * @author Martin
 *
 */
public class ProxyTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String[] temp = {};

		HashSet<String> done = new HashSet<String>();
		for(String linkie : temp){
			done.add(linkie);
		}

		HashSet<String> proxies = new HashSet<String>();
		proxies.addAll(Manager.fillProxies());

		WebBrowser web = new WebBrowser();
		String link = "http://www.google.nl";
		for(String proxyLink : proxies){
			if(!done.contains(proxyLink)){
				System.out.print(proxyLink + "\t");
				try{
					DomElement goButton = ProxyManager.getProxy(web, proxyLink, link);
					HtmlPage page = goButton.click();

					if(page.asText().contains("The site you are attempting to browse is on a secure connection. This proxy is not on a secure connection.")) {
						page = ProxyManager.ignoreWarning(page, web);
					}

					HtmlElement el = page.getElementByName("q");

					if(el.asXml().contains("2048")){
						System.out.println("Succes1");
					}else{
						throw new Exception("Zoeken not found");
					}
				} catch(Exception e){
					e.printStackTrace();
					System.exit(1);
				}
			}
		}



	}

}
