/**
 * 
 */
package workers;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import containers.Adress;
import containers.HashList;
import containers.Result;
import containers.concurrency.Queue;
import containers.concurrency.RandomGenerator;
import graphicalinterfaces.MessagePanel;
import managers.ProxyManager;
import models.WebBrowser;

/**
 * @author Martin Skogholt
 *
 */
public class CompleteWorker implements Callable<HashList<Result>> {

	private String link;
	private ArrayList<String> proxies;
	private RandomGenerator gen;
	private Queue queue;
	private MessagePanel messenger;
	private static final Pattern pattern = Pattern.compile("(detelefoongids.nl){1}(/){1}\\S+(/){1}\\d+(/){1}");
	private static final Pattern idPattern = Pattern.compile("(/){1}\\d+(/){1}");

	public CompleteWorker(String link, ArrayList<String> proxies, RandomGenerator gen, 
			Queue queue, MessagePanel messenger){
		this.link = link;
		this.proxies = proxies;
		this.gen = gen;
		this.queue = queue;
		this.messenger = messenger;
	}

	@Override
	public HashList<Result> call() throws Exception {

		WebBrowser web = new WebBrowser();
		HashList<Result> results = new HashList<Result>();
		int selectLink = gen.getIndex();

		try {
			String proxyLink = proxies.get(selectLink);
			while(proxyLink.contains("proxfree")) {
				selectLink = gen.getIndex();
				proxyLink = proxies.get(selectLink);
			}

			messenger.setMessage("Getting Proxy....");

			DomElement goButton = ProxyManager.getProxy(web, proxyLink, link);

			messenger.setMessage("Got Proxy Connection");

			HtmlPage page = goButton.click();

			int succesfull = 0;
			for(HtmlAnchor anchor : page.getAnchors()) {
				String href = anchor.getHrefAttribute();
				if(anchor.getParentNode().asXml().contains("h2 id=\"")
						|| (href.contains("collapsing") && href.contains("encodedRefinement") && href.contains("collapseid"))) {
					href = this.cleanHref(href);
					
					if(anchor.getParentNode().asXml().contains("h2 id=\"")) {
						String xml = anchor.getParentNode().asXml();
						xml = xml.replace("<h2 id=\"", "");
						String id = xml.substring(0, xml.indexOf("\""));
						if(anchor.getHrefAttribute().contains(id)) {	
							try {

								HtmlPage resultPage = web.getPage(href);
								Result result = new Result(href);
								String ids = null;
								Matcher firstMatch = pattern.matcher(href);
								if(firstMatch.find()) {
									String temp = href.substring(firstMatch.start(), firstMatch.end());
									Matcher idMatch = idPattern.matcher(temp);
									if(idMatch.find()) {
										ids = temp.substring(idMatch.start()+1, idMatch.end()-1);
									}
								}

								HtmlElement title = resultPage.getHtmlElementById(ids);
								result.setCompany(title.asText());

								HtmlElement list = resultPage.getHtmlElementById("li_"+ids);

								HtmlElement adressElement = resultPage.getFirstByXPath(list.getCanonicalXPath()+"/div[1]/ul/li[1]/p");
								
								Adress adress = new Adress();
								String textual = adressElement.asText();
								String[] adressParts = textual.split(",");		
								try {
									adress.setStreet(adressParts[0]);
								} catch (Exception e) {System.err.println(e);}
								try {
									adress.setPostal(adressParts[1]);
								} catch (Exception e) {System.err.println(e);}
								try {
									adress.setCity(adressParts[2]);
								} catch (Exception e) {System.err.println(e);}

								result.addAdress(adress);

								HtmlElement phoneNumber = resultPage.getFirstByXPath(list.getCanonicalXPath()+"/div[3]/div/div[2]/div/div/ul/li[1]/div/span");
								result.addNumber(phoneNumber.asText());

								results.add(result);
								succesfull++;
							} catch (Exception e){
								System.err.println(e);
							}
						}
					}
					if(href.contains("collapsing") && href.contains("encodedRefinement") && href.contains("collapseid")){
						queue.addToQueue(href);
						succesfull++;
					}
				}
			}

			if(succesfull<=0) {
				throw new Exception("No Results!");
			}
			gen.addIndex(selectLink);
		} catch (Exception e) {
			messenger.setMessage("Caught error");
			gen.removeIndex(selectLink);
			throw e;
		} finally {
			web.close();
		}
		return results;
	}

	public String cleanHref(String href){
		href = href.replace("/browse.php?u=", "");
		href = href.replace("/fish.php?u=", "");

		href = href.replace("%2F", "/");
		href = href.replace("%3F", "?");
		href = href.replace("%3D", "=");
		href = href.replace("%26", "&");
		href = href.replace("%25", "%");
		href = href.replace("%3A", ":");

		href = href.replace("&b=28", "");
		href = href.replace("&b=4", "");
		href = href.replace("&b=12", "");

		// Webanon
		href = href.replace("s-s.", "");
		href = href.replace(".prx.webanonymizer.org", "");

		// filterbypass
		href = href.replace("/s.php?k=", "");
		href = href.replace("&b=12", "");

		// fish & xitenow
		href = href.replace("/browse.php?u=", "");
		href = href.replace("/fish.php?u=", "");
		return href;
	}
}
