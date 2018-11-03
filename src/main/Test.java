package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import containers.Adress;
import containers.Query;
import containers.Result;
import containers.concurrency.RandomGenerator;
import managers.Manager;
import managers.ProxyManager;
import models.WebBrowser;

public class Test {
	private static final Pattern pattern = Pattern.compile("(detelefoongids.nl){1}(/){1}\\S+(/){1}\\d+(/){1}");
	private static final Pattern idPattern = Pattern.compile("(/){1}\\d+(/){1}");

	public static void main(String[] args) throws Exception{

		//		String test = "Toon 4787 resultaten";
		//		String test = "- 4787 resultaten";
		//		String regex = "(-\\s){1}\\d+\\s+(resultaten){1}";
		//		System.out.println(test.matches(regex));
		//		System.exit(1);

		ArrayList<String> proxies = Manager.fillProxies();
		RandomGenerator gen = new RandomGenerator(proxies.size());

		String what = "Bakker";
		String where = "";
		int max = -1;
		int attemptCounter = 0;
		String link = "";
		if(where.equals("")){
			link = "http://www.detelefoongids.nl/" + what + "/4-1/?page=" + 1;
		}else{
			link = "http://www.detelefoongids.nl/" + what + "/" + where + "/3-1/?page=" + 1;
		}

		while(true) {
			WebBrowser web = new WebBrowser();
			try {

				int index = Math.min(proxies.size()-1, (int) ( Math.random()*((double) proxies.size())));
				String proxyLink = proxies.get(index);

				// Random selection of anonymizer
				DomElement goButton = ProxyManager.getProxy(web, proxyLink, link);

				HtmlPage page = goButton.click();
				
				System.out.println(page.asText());
				String text = page.asText();
				String[] pageSplit = text.split("\\r?\\n");
				for(String split : pageSplit) {
					if(split.matches("(-\\s){1}\\d+\\s+(resultaten){1}")) {
						String num = split.substring(2, split.indexOf("resultaten")-1);
						max = Integer.parseInt(num.trim());
					}
				}
			} catch (Exception e) {
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
		System.out.println(max);

//		String link = "https://www.detelefoongids.nl/bakkerij-vd-grijn/20992690/5-1/?sn=209926900183-633573";
//
//		Result result = new Result(link);
//		WebBrowser web = new WebBrowser();
//		int index = gen.getIndex();
//
//		String proxyLink = proxies.get(index);
//		DomElement goButton = ProxyManager.getProxy(web, proxyLink, link);
//
//		HtmlPage resultPage = goButton.click();
//		System.out.println(resultPage.asText());
//
//		String id = null;
//		Matcher firstMatch = pattern.matcher(link);
//		if(firstMatch.find()) {
//			String temp = link.substring(firstMatch.start(), firstMatch.end());
//			Matcher idMatch = idPattern.matcher(temp);
//			if(idMatch.find()) {
//				id = temp.substring(idMatch.start()+1, idMatch.end()-1);
//			}
//		}
//		HtmlElement title = resultPage.getHtmlElementById(id);
//		result.setCompany(title.asText());
//
//		HtmlElement adres = resultPage.getFirstByXPath(title.getParentNode().getCanonicalXPath()+"/ul[1]/div[1]");
//
//		String adresText = adres.asText();	
//		Adress adress = new Adress();
//		String[] adressParts = adresText.split(",");		
//		try {
//			adress.setStreet(adressParts[0]);
//		} catch (Exception e) {System.err.println(e);}
//		try {
//			adress.setPostal(adressParts[1]);
//		} catch (Exception e) {System.err.println(e);}
//		try {
//			adress.setCity(adressParts[2]);
//		} catch (Exception e) {System.err.println(e);}
//
//		result.addAdress(adress);
//
//		String pageText = resultPage.asText();
//		String[] pageSplit = pageText.split("\\r?\\n");
//		for(String split : pageSplit) {
//			if(split.endsWith("Toon nummer")){
//				split = split.replace("Toon nummer", "");
//				result.addNumber(split);
//				break;
//			}
//		}
//
//		System.out.println(result);
//		web.close();

	}
}
