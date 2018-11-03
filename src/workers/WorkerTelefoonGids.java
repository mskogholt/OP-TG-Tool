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
			DomElement goButton = ProxyManager.getProxy(web, proxyLink, link);

			HtmlPage page = goButton.click();

			String id = null;
			Matcher firstMatch = pattern.matcher(link);
			if(firstMatch.find()) {
				String temp = link.substring(firstMatch.start(), firstMatch.end());
				Matcher idMatch = idPattern.matcher(temp);
				if(idMatch.find()) {
					id = temp.substring(idMatch.start()+1, idMatch.end()-1);
				}
			}
			try{
				HtmlElement title = page.getHtmlElementById(id);
				result.setCompany(title.asText());

				HtmlElement adres = page.getFirstByXPath(title.getParentNode().getCanonicalXPath()+"/ul[1]/div[1]");

				String adresText = adres.asText();	
				Adress adress = new Adress();
				String[] adressParts = adresText.split(",");		
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
			} catch (Exception e){
				String name = link;
				name = name.replace("https://www.detelefoongids.nl/", "");
				result.setCompany(name.substring(0, name.indexOf("/")));
			}

			String pageText = page.asText();
			String[] pageSplit = pageText.split("\\r?\\n");
			for(String split : pageSplit) {
				if(split.endsWith("Toon nummer")){
					split = split.replace("Toon nummer", "");
					result.addNumber(split);
					break;
				}
			}
			if(result.getNumbers().size()<1){
				throw new Exception("No Number!" + proxyLink);
			}
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
