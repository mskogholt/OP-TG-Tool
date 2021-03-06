/**
 * 
 */
package workers.links;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import containers.Query;
import containers.concurrency.RandomGenerator;
import graphicalinterfaces.MessagePanel;
import managers.Manager;
import managers.ProxyManager;
import models.TerminationLimit;
import models.WebBrowser;

/**
 * @author martin.skogholt
 *
 */
public class LinkWorkerTelefoonGids extends LinkWorker{

	public LinkWorkerTelefoonGids(Query query, String site, ArrayList<String> proxies, RandomGenerator gen, TerminationLimit termination, MessagePanel messenger) {
		super(query, site, proxies, gen, termination, messenger);
	}

	@Override
	public ArrayList<String> call() throws Exception {
		ArrayList<String> urls = new ArrayList<String>();

		String what = query.getWhat();
		String where = query.getWhere();

		ArrayList<String> list = new ArrayList<String>();

		messenger.setMessage("Initializing...");
		int max = getMax(query, proxies);
		if(max > 0){
			if(termination.getType().equals("Page Limit")) {
				max = Math.min(max, termination.getLimit());
			}
			messenger.setMessage("Collecting " + max + " telefoongids pages of results");
			for(int i=1; i<=max; i++){
				if(where.equals("")){
					String link = "http://www.detelefoongids.nl/" + what + "/4-1/?page=" + i;
					urls.add(link);
				}else{
					String link = "http://www.detelefoongids.nl/" + what + "/" + where + "/3-1/?page=" + i;
					urls.add(link);	
				}
			}

			HashSet<String> links = new HashSet<String>();
			HashMap<String, Integer> tries = new HashMap<String, Integer>();
			for(int i=0; i<urls.size(); i++) {

				String link = urls.get(i);
				if(tries.containsKey(link)) {
					int tried = tries.get(link);
					tried++;
					tries.put(link, tried);
				} else {
					tries.put(link, 1);
				}
				if(tries.get(link)>=Manager.maxAttempts) {
					continue;
				}
				
				try {
					links.addAll(getLinks(link, gen, proxies));
					messenger.setMessage("Got " + (i+1) + " page");
				} catch(Exception e) {
					urls.add(link);
				}
				double prog = ((double) (i+1))/((double) urls.size());
				prog = prog*100;
			}
			list.addAll(links);
		}
		return list;
	}

	private HashSet<String> getLinks(String link, RandomGenerator gen, ArrayList<String> proxies) throws Exception{
		WebBrowser web = new WebBrowser();
		HashSet<String> links = new HashSet<String>();

		int selectLink = gen.getIndex();

		try {
			String proxyLink = proxies.get(selectLink);

			DomElement goButton = ProxyManager.getProxy(web, proxyLink, link);
			HtmlPage page = goButton.click();

			if(page.asText().contains("The site you are attempting to browse is on a secure connection. This proxy is not on a secure connection.")) {
				page = ProxyManager.ignoreWarning(page, web);
			}
			int succesfull = 0;
			System.out.println(page.asText());

			String text = page.asText();
			String[] pageSplit = text.split("\\r?\\n");
			for(String split : pageSplit) {
				if(split.startsWith("https://www.detelefoongids.nl/")
						&& split.endsWith("Toon nummer")){
					String href = split.replace("Toon nummer", "");
					links.add(href);
					succesfull++;
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
		return links;
	}

	private int getMax(Query query, ArrayList<String> proxies) {
		String what = query.getWhat();
		String where = query.getWhere();
		int max = -1;
		int attemptCounter = 0;
		String link = "";
		if(where.equals("")){
			link = "http://www.detelefoongids.nl/" + what + "/4-1/?page=" + 1;
		}else{
			link = "http://www.detelefoongids.nl/" + what + "/" + where + "/3-1/?page=" + 1;
		}

		messenger.setMessage("Looking for # of pages");
		while(true) {
			WebBrowser web = new WebBrowser();
			try {

				int index = Math.min(proxies.size()-1, (int) ( Math.random()*((double) proxies.size())));
				String proxyLink = proxies.get(index);
				messenger.setMessage("Attempting to parse...");

				// Random selection of anonymizer
				DomElement goButton = ProxyManager.getProxy(web, proxyLink, link);

				HtmlPage page = goButton.click();

				String text = page.asText();
				String[] pageSplit = text.split("\\r?\\n");
				for(String split : pageSplit) {
					if(split.matches("(-\\s){1}\\d+\\s+(resultaten){1}")) {
						messenger.setMessage("Got # of pages");
						String num = split.substring(2, split.indexOf("resultaten")-1);
						max = Integer.parseInt(num.trim());
					}
				}
			} catch (Exception e) {
				messenger.setMessage("Trying Again...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				attemptCounter++;
				if(attemptCounter > 100){
					break;
				}
			} finally {
				web.close();
			}
			if(max>=0) {
				break;
			}
		}

		double ratio = ((double) max)/25.0;
		max = (int) Math.ceil(ratio);
		return max;
	}
}
