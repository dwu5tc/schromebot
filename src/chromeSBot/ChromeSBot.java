package chromeSBot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*	FOR TESTING
 *		/"test"[, N]
 *		- calls CSBT test constructor
 * 		- "all" shop path
 * 		- 200 cart delay, "test.txt" for the orders, no profile
 * 		- no refreshing
 *
 *		- N (optional): custom number of bots (default = 3)
 */

/*	FOR REAL
 *		"real"[, "/"/"all"/"new"[, PATH]]
 *		- calls CSBT regular constructor
 * 		- prompts user for refresh delay 
 * 		
 * 		- "/"/"all"/"new" (optional): shop path (default = "new")
 * 		- PATH (optional): custom path to .txt file to configure the cart delay, order path, profile of the bots (default = "master.txt") 
 */ 

public class ChromeSBot {
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
	
	private List<ChromeSBotThread> bots = new ArrayList<ChromeSBotThread>();
	
	private Boolean isReal;
	
	private int refreshDelay;
	private String shopPath;
	
	private String staleLink;
	private int expectedNumOfLinks; 
	
	// error handling/logging
	private List<Elements> falseLinks = new ArrayList<Elements>();
	private List<String> errors = new ArrayList<String>(); 
	private String updatedHTML;
	
	public static void main(String[] args) {	
		for (String arg : args) {
			debugPrint("arg: " + arg);
		}
		
		Scanner reader = new Scanner(System.in);
		ChromeSBot chromeSBot;
		
		if (args.length < 1) { // just for testing in eclipse
			String[] testArgs = {"test"};
			chromeSBot = new ChromeSBot(testArgs);
		} else {
			chromeSBot = new ChromeSBot(args);
		}
		
		if (chromeSBot.isReal) {
			chromeSBot.grabStaleLink();
			
			System.out.println("Enter an INTEGER to set the delay time between page refreshes (in ms). 300-800 recommended.");
//			System.out.println("Enter 0 to test sequential.");
//			System.out.println("Enter -1 to test multi-threaded.");
			int delay = reader.nextInt();
			chromeSBot.refreshDelay = delay;
			
			if (chromeSBot.refreshDelay > 0) { 
				System.out.println("Will refresh every " + chromeSBot.refreshDelay + "ms.");
				System.out.println("Enter the expected number of new items to begin running bots.");
				int num = reader.nextInt();
				chromeSBot.expectedNumOfLinks = num;
				
				chromeSBot.refreshAndGrabLinks();
//				chromeSBot.grabLinks();
				chromeSBot.run();
				for (List<Element> links : chromeSBot.falseLinks) {
					printLinkHrefs(links);
				}
				for (String error : chromeSBot.errors) {
					System.out.println(error);
				}
				System.out.println(chromeSBot.updatedHTML);
				reader.close();
				return;
			}
//			} else if (chromeSBot.refreshDelay == 0) {
//				chromeSBot.grabLinks();
//				chromeSBot.test();
//			} else if (chromeSBot.refreshDelay == -1) {
//				chromeSBot.grabLinks();
//				chromeSBot.run();
//			} else {
//				
//			}
//		} else {
//			chromeSBot.grabLinks();
//			System.out.println("Enter POSITIVE INTEGER to test multi-threaded.");
//			System.out.println("Enter NEGATIVE INTEGER to test sequential.");
//			if (reader.nextInt() > 0) {
//				chromeSBot.run();
////				System.out.println("Threads have been created.");
////				System.out.println("Enter POSITIVE INTEGER to begin running bots.");
////				if (reader.nextInt() > 0) {
////					System.out.println("Running now...");
////					chromeSBot.startAllBots();
////				}
//			} else {
//				chromeSBot.test();
//			}
		}
		reader.close();
		return;
	}
	
	// set boolean for test or real,
	// webshop path to hit,
	// path to file for bot configuration
	// implement better argument reading!!!
	// implement factory???
	public ChromeSBot(String[] args)
	{	
		if (args.length < 1 || args[0].equals("test")) {
			debugPrint("in test");
			this.isReal = false;
			this.shopPath = "all";
			int numOfTestBots = 3; // default number of test bots
			
			if (args.length >= 2) {
				numOfTestBots = Integer.parseInt(args[1]);
			}
			
			for (int i = 0; i < numOfTestBots; i++) {
				ChromeSBotThread testBot = new ChromeSBotThread();
				testBot.setThreadName("[test " + (i + 1) + "]");
				this.bots.add(testBot);
			}
		}
		else if (args[0].equals("real")) {
			debugPrint("in real");
			this.isReal = true;
			String configPath = "master.txt"; // default config path
			String shopPath = "new"; // default shop path
			
			if (args.length >= 2) { // more readable way of doing this???
				if (args[1].equals("/")) {
					shopPath = "";
				} else if (args[1].equals("new") || args[1].equals("all")) {
					shopPath = args[1];
				}
			}
			this.shopPath = shopPath;
			
			if (args.length >= 3) {
				configPath = args[2];
			}
			try {
				this.configureBots(configPath);
			} catch (Exception e) {
				// error handle
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
	
	// 
	private void configureBots(String configPath) throws IOException {
		FileInputStream fis = new FileInputStream(configPath);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis)); // construct bufferedreader from inputstreamreader
			
		String line = null;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			String[] args = line.split(" ");
			if (args.length == 3) {
				ChromeSBotThread bot = new ChromeSBotThread(Integer.parseInt(args[0]), args[1], args[2]); // cartDelay, profile, orderPath
				this.bots.add(bot);
			} else {
				// error handle...
			}
		}
		br.close();
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
			System.out.println("Error in grabStaleLink.");
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
//				if (true) { // for testing
				if (!links.eq(0).attr("abs:href").equals(this.staleLink)) { // link has been switched/removed
					// check for shop update false positive
					// move this into another while loop below???
					if (links.size() < 1) {
						this.errors.add(doc.html());
						System.out.println("SHOP LINKS HAVE BEEN REMOVED. 0 LINKS FOUND.");
						Thread.sleep(this.refreshDelay);
						attemptNum++;
						continue;
					}
					if (links.size() != this.expectedNumOfLinks) { 
						this.falseLinks.add(links);
						this.errors.add(doc.html());
						System.out.println("SHOP LINKS CHANGED BUT STILL NOT UPDATED. " + links.size() + " LINKS FOUND. EXPECTING " + this.expectedNumOfLinks + ".");
						Thread.sleep(this.refreshDelay);
						attemptNum++;
						continue;
					}
					if (links.eq(0).attr("abs:href").isEmpty()) {
						this.errors.add(doc.html());
						System.out.println("SHOP LINKS HAVE BEEN UPDATED BUT THE HREFS ARE EMPTY.");
						Thread.sleep(this.refreshDelay);
						attemptNum++;
						continue;
					}
					// shop update 
					updated = true;
					this.updatedHTML = doc.html();
					for (ChromeSBotThread bot : this.bots) {
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
						System.out.println("REFRESHING " + attemptNum + ". SHOP NOT UPDATED. " + links.size() + " LINKS FOUND. EXPECTING " + this.expectedNumOfLinks + ".");
						Thread.sleep(this.refreshDelay);
						attemptNum++;
					} catch (Exception e) {
						System.out.println("Error in nested try-catch of refreshPage.");
					}
				}
			}
			catch (Exception e) {
				System.out.println("Error in refreshPage.");
			}
		}
	}

	private void grabLinks() {
		String target = "http://www.su" + "pr" + "em" + "en" + "ew" + "yo" + "rk.com" + "/sh" + "op/" + this.shopPath;
		String linksContainer;
		debugPrint(target);
		if (this.shopPath.equals("new") || this.shopPath.equals("all")) {
			debugPrint("path is " + this.shopPath);
			linksContainer = "div.turbolink_scroller a";
			
		}
		else {
			debugPrint("path is null");
			linksContainer = "div.shop a";
		}
		try {
			Document doc = Jsoup.connect(target).get();
//			debugPrint(doc.html());
			Elements links = doc.select(linksContainer);
//			debugPrint(links.size());
//			for (Element link : links) {
//				System.out.println(link.attr("abs:href"));
//			}
			for (ChromeSBotThread bot : this.bots) {
				bot.setLinks(links);
			}
			System.out.println("Shop has been updated!");
		} catch (Exception e) {
			System.out.println("ERROR IN notFirstDrop.***");
		}
	}
	
	private void test() { // serial testing
		for (ChromeSBotThread bot : this.bots) {
			bot.test();
		}
	}
	
	private void run() {
		for (ChromeSBotThread bot : this.bots) {
			bot.setStartTime(System.nanoTime());
			bot.getThread().start();
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
	
	// move these to a utils?
	public static void debugPrint(String string) {
		System.out.println("-- " + string + " --");
	}
	
	public static void debugPrint(int num) {
		System.out.println("-- " + num + " --");
	}
	
	public static void printLinkHrefs(List<Element> links) {
		for (Element link : links) {
			System.out.println(link.attr("abs:href"));
		}
	}
}
