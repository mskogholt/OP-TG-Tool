/**
 * 
 */
package workers.links;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
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
				HashSet<String> tempLinks = getLinks(link, gen, proxies);

				messenger.setMessage("Got " + (i+1) + " page");
				for(String tempLink : tempLinks) {
					if(tempLink.contains("collapsing") && tempLink.contains("encodedRefinement") && tempLink.contains("collapseid")){
						int counter = 0;
						while(counter<Manager.maxAttempts) {
							try {
								links.addAll(getLinks(tempLink, gen, proxies));
								break;
							} catch (Exception e) {}
							counter++;
						}
					} else {
						links.add(tempLink);
					}
				}
			} catch(Exception e) {
				urls.add(link);
			}
			double prog = ((double) (i+1))/((double) urls.size());
			prog = prog*100;
		}

		ArrayList<String> list = new ArrayList<String>();
		list.addAll(links);
		return list;
	}

	private HashSet<String> getLinks(String link, RandomGenerator gen, ArrayList<String> proxies) throws Exception{
		WebBrowser web = new WebBrowser();
		HashSet<String> links = new HashSet<String>();

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
					href = anchor.getHrefAttribute();
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

					if(anchor.getParentNode().asXml().contains("h2 id=\"")) {
						String xml = anchor.getParentNode().asXml();
						xml = xml.replace("<h2 id=\"", "");
						String id = xml.substring(0, xml.indexOf("\""));
						if(anchor.getHrefAttribute().contains(id)) {							
							links.add(href);
							succesfull++;
						}
					}
					if(href.contains("collapsing") && href.contains("encodedRefinement") && href.contains("collapseid")){
						links.add(href);
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
		return links;
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
