/**
 * 
 */
package workers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import containers.Adress;
import containers.Result;
import containers.concurrency.RandomGenerator;
import graphicalinterfaces.MessagePanel;
import managers.ProxyManager;
import models.TerminationLimit;
import models.WebBrowser;

/**
 * @author martin.skogholt
 *
 */
public class WorkerTelefoonGids extends Worker{

	private static final Pattern pattern = Pattern.compile("(detelefoongids.nl){1}(/){1}\\S+(/){1}\\d+(/){1}");
	private static final Pattern idPattern = Pattern.compile("(/){1}\\d+(/){1}");

	public WorkerTelefoonGids(String link, String site, ArrayList<String> proxies, RandomGenerator gen, 
			TerminationLimit termination, MessagePanel messenger) {
		super(link, site, proxies, gen, termination, messenger);
	}

	@Override
	public Result call() throws Exception {

		Result result = processLink(link);
		return result;

	}

	public Result processLink(String link) throws Exception{

		Result result = new Result(link);
		WebBrowser web = new WebBrowser();
		int index = gen.getIndex();

		try{

			String proxyLink = proxies.get(index);
			while(proxyLink.contains("proxfree")) {
				index = gen.getIndex();
				proxyLink = proxies.get(index);
			}

			DomElement goButton = ProxyManager.getProxy(web, proxyLink, link);

			HtmlPage resultPage = goButton.click();

			String id = null;
			Matcher firstMatch = pattern.matcher(link);
			if(firstMatch.find()) {
				String temp = link.substring(firstMatch.start(), firstMatch.end());
				Matcher idMatch = idPattern.matcher(temp);
				if(idMatch.find()) {
					id = temp.substring(idMatch.start()+1, idMatch.end()-1);
				}
			}

			HtmlElement title = resultPage.getHtmlElementById(id);
			result.setCompany(title.asText());

			HtmlElement list = resultPage.getHtmlElementById("li_"+id);

			HtmlElement adressElement = resultPage.getFirstByXPath(list.getCanonicalXPath()+"/div[1]/ul/li[1]/p");
			//*[@id="content"]/div/div[2]/div/div[3]/div/section/div/div[1]/h1
			//*[@id="content"]/div/div[2]/div/div[3]/div/section/div/div[1]/ul/li/p

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
			gen.addIndex(index);
		} catch (Exception e) {
			gen.removeIndex(index);
			throw e;
		} finally{
			web.close();
		}
		return result;
	}
}
