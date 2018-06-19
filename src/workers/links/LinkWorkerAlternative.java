/**
 * 
 */
package workers.links;

import java.util.ArrayList;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import containers.Query;
import containers.concurrency.RandomGenerator;
import graphicalinterfaces.MessagePanel;
import managers.ProxyManager;
import models.TerminationLimit;
import models.WebBrowser;

/**
 * 
 * @author martin.skogholt
 *
 */
public class LinkWorkerAlternative extends LinkWorker{

	public LinkWorkerAlternative(Query query, String site, ArrayList<String> proxies, RandomGenerator gen, TerminationLimit termination, MessagePanel messenger) {
		super(query, site, proxies, gen, termination, messenger);
	}

	@Override
	public ArrayList<String> call() throws Exception {
		ArrayList<String> urls = new ArrayList<String>();

		String what = query.getWhat();
		String where = query.getWhere();

		messenger.setMessage("Initializing...");
		int max = getMax(query, proxies);

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

		return urls;
	}

	private int getMax(Query query, ArrayList<String> proxies) {
		String what = query.getWhat();
		String where = query.getWhere();
		int max = -1;

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

				while(proxyLink.contains("proxfree")) {
					index =  Math.min(proxies.size()-1, (int) ( Math.random()*((double) proxies.size())));
					proxyLink = proxies.get(index);
				}
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
