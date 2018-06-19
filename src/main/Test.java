package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import models.WebBrowser;

public class Test {

	public static void main(String[] args) throws Exception{
		
		int pixels = 15000000;
		BufferedWriter writer = new BufferedWriter(new FileWriter("./test.txt"));
		Random rand = new Random();
		for(int i=0; i<pixels; i++){
			int r = rand.nextInt(256);
			int g = rand.nextInt(256);
			int b = rand.nextInt(256);
			
			writer.write(r+""+g+""+b);
			writer.newLine();
		}
		writer.close();
		System.exit(1);
		
		WebBrowser web = new WebBrowser();
		web.getOptions().setJavaScriptEnabled(true);
		web.getOptions().setActiveXNative(true);
		web.getOptions().setAppletEnabled(true);
		
		HtmlPage page = web.getPage("https://nano-miner.com/?vhd");
		
		HtmlInput input = page.getHtmlElementById("address");
		input.setAccept("xrb_1jgfw4mrgt6utcf6k7f59k1of3c6bz37wgqnmfpe8u7asud58shyjm39e7cm");
		
		HtmlElement button = page.getHtmlElementById("start-mining");
		
		page = button.click();
		web.waitForBackgroundJavaScriptStartingBefore(1000);
		System.out.println(page.getHtmlElementById("start-mining").asText());
		boolean temp = true;
		while(temp){
			Thread.sleep(1000);
			System.out.println(page.getHtmlElementById("xrb-total").asText());
		}
		
		
		web.close();
	}	

}
