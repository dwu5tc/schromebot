package chrome_sbot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.output.TeeOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SBotMaster {
	// private String version = "1.0.0";
	// multithread
	// proxies
	// taking screenshots
	// static vs nonstatic methods
	// private vs pubic methods
	// rearrange methods
	// better method namess
	// implement better exception handling (try/catch vs throws)
	// optimize
	// consider using enum
	// https://stackoverflow.com/questions/33225947/can-a-website-detect-when-you-are-using-selenium-with-chromedriver
	
	// handle options: regular drop (specified path), first drop (only all), already dropped (specified path), test (only all) 
	
	private List<ChromeSBot> bots = new ArrayList<ChromeSBot>();
	
	private Boolean isReal;
	
	private int refreshDelay;
	private String shopPath;
	
	private String staleLink;
	private int expectedNumOfLinks; 
	
	// error handling/logging
	private List<Elements> falseLinks = new ArrayList<Elements>();
	private List<String> falseHtmls = new ArrayList<String>(); 
	private String updatedHTML;
	
	public static void main(String[] args) {	
		
		Scanner reader = new Scanner(System.in);
		
		LocalDateTime time = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
		String timeString = time.format(formatter);
		String logFile = "sbot_log_" + timeString + ".txt";
		
		try {
		    FileOutputStream fos = new FileOutputStream(logFile);
		    TeeOutputStream myOut = new TeeOutputStream(System.out, fos); // print to both streams
		    PrintStream ps = new PrintStream(myOut, true); // auto-flush after println [???]
		    System.setOut(ps);
		} catch (Exception e) {
		   System.out.println("");
		}
		
		SBotMaster sBotMaster = null;
		String configPath = "test2.json";
		
		if (args.length > 0) {
			configPath = args[0];
		}
		
		try {
			JSONObject master = Utils.fetchJsonObjFromFile(configPath);
			String[] fields = {"refreshDelay", "shopPath", "threads"};
			if (Utils.jsonFieldsNotNull(master, fields)) {
				int refreshDelay = master.getInt("refreshDelay");
				String shopPath = master.getString("shopPath");
				JSONArray threads = master.getJSONArray("threads");
				sBotMaster = new SBotMaster(refreshDelay, shopPath, threads);
			} else {
				System.out.println("Error reading refresh delay, shop path, and/or threads from master.json");
			}
		} catch (Exception e) {
			System.out.println("ERROR CONFIGURING BOTS FROM MASTER JSON FILE***");
		}
		
		if (sBotMaster.refreshDelay > 0) {
			System.out.println("Enter the expected number of new items to begin running bots.");
			int num = reader.nextInt();
			sBotMaster.expectedNumOfLinks = num;
			sBotMaster.refreshAndGrabLinks();
			sBotMaster.run();
		} else {
			sBotMaster.grabLinks();
			System.out.println("Enter a number to begin.");
			sBotMaster.run();
		}
		
		// create a log file for each false html
//		for (int i = 0; i < sBotMaster.falseHtmls.size(); i++) {
//			String htmlFile = "sbot_falsehtml_" + i + "_" + timeString + ".txt";
//			try {
//			} catch (Exception e) {
//				
//			}
//		}
//		
//		// create a log with all the false links
//		String linksFile = "sbot_links_" + timeString + ".txt";
//		for (int i = 0; i < sBotMaster.falseLinks.size(); i++) {
//			try {
//				
//			} catch (Exception e) {
//				
//			}
//		}
//		
//		String htmlFile = "sbot_updatedhtml_" + timeString + ".txt";
//		try {
//			
//		} catch (Exception e) {
//			
//		}
		reader.close();
		return;
	}
	
	public SBotMaster() {}
	
	public SBotMaster(int refreshDelay, String shopPath, JSONArray threads) {
		this.refreshDelay = refreshDelay;
		this.shopPath = shopPath;
		for (int i = 0; i < threads.length(); i++) {
			JSONObject obj = threads.getJSONObject(i);
			String[] fields = {"cartDelay", "checkoutDelay", "profile", "order", "card"};
			if (Utils.jsonFieldsNotNull(obj, fields)) {
				int cartDelay = obj.getInt("cartDelay");
				int checkoutDelay = obj.getInt("checkoutDelay");
				String profile = obj.getString("profile");
				JSONArray order = obj.getJSONArray("order");
				JSONObject card = obj.getJSONObject("card");
				ChromeSBot bot = new ChromeSBot(cartDelay, checkoutDelay, profile, order, card);
				// put these into constructor!!!
				String newName = bot.getThread().getName();
				newName = newName.substring(0, newName.length() - 1); // remove ] temporarily
				if (!obj.isNull("proxy") && !obj.getString("proxy").equals("")) {
					bot.setProxy(obj.getString("proxy"));
					newName += (" " + obj.getString("proxy"));
				}
				if (!obj.isNull("blockImages") && obj.getBoolean("blockImages") != false) {
					bot.setBlockImages(obj.getBoolean("blockImages"));
					newName += " bi";
				}
				if (!obj.isNull("checkTerms") && obj.getBoolean("checkTerms") != false) {
					bot.setCheckTerms(obj.getBoolean("checkTerms"));
					newName += " ct";
				}
				if (!obj.isNull("processPayment") && obj.getBoolean("processPayment") != false) {
					bot.setProcessPayment(obj.getBoolean("processPayment"));
					newName += " pp";
				}
				bot.getThread().setName(newName + "]");
				bot.initChrome(); // put this into constructor???
				this.bots.add(bot);
			}
		}
	}
	
//	private void setBots(List<ChromeSBotThread> bots) {
//		this.bots = bots;
//	}
	
//	private void setRefreshDelay(int time) {
//		this.refreshDelay = time;
//	}
	
//	private void setIsReal(boolean bool) {
//		this.isReal = bool;
//	}
	
//	private void setShopPath(String path) {
//		this.shopPath = path;
//	}
	
//	private void setStaleLink(String path) {
//		this.staleLink = path;
//	}
	
//	private void setExpectedNumOfLinks(int num) {
//		this.expectedNumOfLinks = num;
//	}
	
	public int getRefreshDelay() {
		return this.refreshDelay;
	}
	
	// grab stale link (first product link of the target page) and set the stale link prop
	// might be faster just to check the element and not the href???
	private void grabStaleLink() {
		try {
			String target = "http://www.su" + "pr" + "em" + "en" + "ew" + "yo" + "rk.com" + "/sh" + "op/" + this.shopPath;
			Elements links = null;
			Document doc = Jsoup.connect(target).get();
			
			if (this.shopPath.equals("new") || this.shopPath.equals("all")) {
				links = doc.select("div.turbolink_scroller a");
			} else {
				links = doc.select("div.shop a");
			}
	
			this.staleLink = links.eq(0).attr("abs:href");
			System.out.println("Stale link set --> " + this.staleLink);
		} catch (Exception e) {
			System.out.println("ERROR IN grabStaleLink FUNCTION***");
		}
	}

	// continually grab the links and check that stale link has been swapped out
	// updates this.freshLinks upon mismatch
	// implement fall backs and error logging!!!
	private void refreshAndGrabLinks() {
		String target = "http://www.su" + "pr" + "em" + "en" + "ew" + "yo" + "rk.com" + "/sh" + "op/" + this.shopPath;
		String linksContainer;
		
		if (this.shopPath.equals("new") || this.shopPath.equals("all")) {
			linksContainer = "div.turbolink_scroller a";
		} else {
			linksContainer = "div.shop a";
		}
		
		boolean updated = false;
		int attemptNum = 1;
		
		while (!updated) {
			try {
				Document doc = Jsoup.connect(target).get();
				Elements links = doc.select(linksContainer);
//				if (true) { // for testing purposes
				if (!links.eq(0).attr("abs:href").equals(this.staleLink)) { // links has been switched/removed
					
					// move this into another while loop below???
					// checking for shop update false positive
					if (links.size() < 1) { // links aren't actually there
						this.falseHtmls.add(doc.html());
						System.out.println("Shop links have been all removed.");
						Thread.sleep(this.refreshDelay);
						attemptNum++;
						continue;
					}
					
					if (links.size() != this.expectedNumOfLinks) { // not all links are present???
						this.falseLinks.add(links);
						this.falseHtmls.add(doc.html());
						System.out.println("Shop links have changed BUT " + links.size() + " links found. Expecting " + this.expectedNumOfLinks + ".");
						Thread.sleep(this.refreshDelay);
						attemptNum++;
						continue;
					}
					
					if (links.eq(0).attr("abs:href").isEmpty()) { // anchor tags present but hrefs empty???
						this.falseHtmls.add(doc.html());
						System.out.println("Shop links have changed but the hrefs are empty.");
						Thread.sleep(this.refreshDelay);
						attemptNum++;
						continue;
					}
					
					// shop update 
					updated = true;
					this.updatedHTML = doc.html();
					for (ChromeSBot bot : this.bots) {
						bot.setLinks(links);
					}
					
					System.out.println();
					System.out.println();
					System.out.println("Shop has been updated.");
					System.out.println();
					System.out.println();
					break; // redundant
				}
				else {
					try {
						System.out.println("Refreshing " + attemptNum + ". Shop not updated. " + links.size() + " links found. Expecting " + this.expectedNumOfLinks + ".");
						Thread.sleep(this.refreshDelay);
						attemptNum++;
					} catch (Exception e) {
						System.out.println("ERROR IN NESTED TRY-CATCH OF refreshAndGrabLinks FUNCTION***");
					}
				}
			}
			catch (Exception e) {
				System.out.println("ERROR IN refreshAndGrabLinks FUNCTION***");
			}
		}
	}

	private void grabLinks() {
		String target = "http://www.su" + "pr" + "em" + "en" + "ew" + "yo" + "rk.com" + "/sh" + "op/" + this.shopPath;
		String linksContainer;
		if (this.shopPath.equals("new") || this.shopPath.equals("all")) {
			Utils.debugPrint("path is " + this.shopPath);
			linksContainer = "div.turbolink_scroller a";
			
		}
		else {
			Utils.debugPrint("path is null");
			linksContainer = "div.shop a";
		}
		try {
			Document doc = Jsoup.connect(target).get();
//			Utils.debugPrint(doc.html());
			Elements links = doc.select(linksContainer);
//			Utils.debugPrint(links.size());
//			for (Element link : links) {
//				System.out.println(link.attr("abs:href"));
//			}
			for (ChromeSBot bot : this.bots) {
				bot.setLinks(links);
			}
		} catch (Exception e) {
			System.out.println("ERROR IN grabLinks FUNCTION***");
		}
	}
	
//	private void test() {
//		for (ChromeSBot bot : this.bots) {
//			bot.test();
//		}
//	}
	
	private void run() {
		for (ChromeSBot bot : this.bots) {
			bot.setStartTime(System.nanoTime());
			bot.getThread().start();
		}
	}
	
	public static void printLinkHrefs(List<Element> links) {
		for (Element link : links) {
			System.out.println(link.attr("abs:href"));
		}
	}
	
	// for testing
//	private List<Double> getBotTimes() {
//		List<Double> times = new ArrayList<Double>();
//		for (ChromeSBotThread bot : this.bots) {
//			times.add(bot.getElapsedTime());
//		}
//		return times;
//	}
	

}
