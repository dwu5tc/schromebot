package chromeSBot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.print.attribute.DocAttributeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ChromeSBot {
	// private String version = "1.0.0";
	// multithread
	// proxies
	// taking screenshots
	// static vs nonstatic methods
	// private vs pubic methods
	// rearrange methods
	// better method namess
	// implement better exception handling
	// optimize
	// consider using enum
	// https://stackoverflow.com/questions/33225947/can-a-website-detect-when-you-are-using-selenium-with-chromedriver
	
	// handle options: regular drop (specified path), first drop (only all), already dropped (specified path), test (only all) 
	
	private List<ChromeSBotThread> bots = new ArrayList<ChromeSBotThread>();
	private int refreshDelay;
	private Boolean isReal;
	private String shopPath;
	private String staleLink;
	
	public static void main(String[] args) {
		// java -jar [NULL/"test"/"real"] [NULL/"all"/"new"] [configPath]
		
		Scanner reader = new Scanner(System.in);
		
		ChromeSBot chromeSBot;
		if (args.length < 1) {
			System.out.println("hi");
			String[] newArgs = {"test"};
			chromeSBot = new ChromeSBot(newArgs);
		} else {
			chromeSBot = new ChromeSBot(args);
		}
		
		if (chromeSBot.isReal) {
			chromeSBot.grabStaleLink();
			
			System.out.println("Enter an integer to set the delay time between page refreshes (in ms). 300-800 recommended.");
			int delay = 500; // default refresh delay
			delay = reader.nextInt();
			chromeSBot.setRefreshDelay(delay);
			System.out.println("Will refresh every " + delay + "ms.");
			
			System.out.println("Enter an integer to run bots.");
			reader.nextInt();
		} else {
			chromeSBot.grabLinks();
			System.out.println("Enter an integer to begin testing.");
			reader.nextInt();
			chromeSBot.test();
		}		
	
		reader.close();
	}
	
	// set boolean for test or real,
	// webshop path to hit,
	// path to file for bot configuration
	public ChromeSBot(String[] args)
	{	
		if (args.length < 1 || args[0] == "test") {
			this.isReal = false;
			this.shopPath = "all";
			int numOfTestBots = 3; // default number of test bots
			
			if (args.length > 1) {
				numOfTestBots = Integer.parseInt(args[1]);
			}
			
			for (int i = 0; i < numOfTestBots; i++) {
				ChromeSBotThread testBot = new ChromeSBotThread();
				testBot.setName("[test " + (i + 1) + "]");
				this.bots.add(testBot);
			}
		}
		else if (args[0] == "real") {
			this.isReal = true;
			String configPath = "master.txt"; // default config path
			String shopPath = "new"; // default shop path
			
			if (args[1] != null) {
				shopPath = args[1];
			}
			this.shopPath = shopPath;
			
			if (args[2] != null) {
				configPath = args[2];
			}
			
			try {
				this.configureBots(configPath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
//	private void setBots(List<ChromeSBotThread> bots) {
//		this.bots = bots;
//	}
	
	private void setRefreshDelay(int time) {
		this.refreshDelay = time;
	}
	
//	private void setIsReal(boolean bool) {
//		this.isReal = bool;
//	}
	
//	private void setShopPath(String path) {
//		this.shopPath = path;
//	}
	
//	private void setStaleLink(String path) {
//		this.staleLink = path;
//	}
	
	// creates
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
			
			if (this.shopPath == "new" || this.shopPath == "all") {
				links = doc.select("div.turbolink_scroller a");
			} else {
				links = doc.select("div.shop a");
			}
	
			this.staleLink = links.eq(0).attr("abs:href");
			System.out.println("STALE LINK SET: " + this.staleLink);
		} catch (Exception e) {
			System.out.println("Error in grabStaleLink.");
		}
	}

	// continually grab the links and check that stale link has been swapped out
	// updates this.freshLinks upon mismatch
	private void refreshAndGrabLinks() {
		String target = "http://www.su" + "pr" + "em" + "en" + "ew" + "yo" + "rk.com" + "/sh" + "op/" + this.shopPath;
		String linksContainer;
		if (this.shopPath == "new" || this.shopPath == "all") {
			linksContainer = "div.turbolink_scroller a";
		} else {
			linksContainer = "div.shop a";
		}
		boolean notUpdated = true;
		int attemptNum = 1;
		while (notUpdated) {
			try {
				Document doc = Jsoup.connect(target).get();
				Elements links = doc.select(linksContainer);
				if (!links.eq(0).attr("abs:href").equals(this.staleLink)) { // not equal (! for real)
					notUpdated = false;
					for (ChromeSBotThread bot : this.bots) {
						bot.setLinks(links);
					}
					System.out.println("SHOP HAS BEEN UPDATED.");
					break; // redundant
				}
				else {
					try {
						System.out.println("REFRESHING " + attemptNum + ". SHOP NOT UPDATED");
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
		if (this.shopPath == "new" || this.shopPath == "all") {
			debugPrint(this.shopPath);
			linksContainer = "div.turbolink_scroller a";
			
		}
		else {
			System.out.println("-- shop path is null --");
			linksContainer = "div.shop a";
		}
		try {
			Document doc = Jsoup.connect(target).get();
//			debugPrint(doc.html());
			Elements links = doc.select(linksContainer);
			debugPrint(links.size());
			for (Element link : links) {
				System.out.println(link.attr("abs:href"));
			}
			for (ChromeSBotThread bot : this.bots) {
				bot.setLinks(links);
			}
			System.out.println("Shop has been updated!");
		} catch (Exception e) {
			System.out.println("ERROR IN notFirstDrop.***");
		}
	}
	
	private void test() {
		for (ChromeSBotThread bot : this.bots) {
			bot.test();
		}
	}
	
	
	// move these to a utils?
	private void debugPrint(String string) {
		System.out.println("--" + string + "--");
	}
	
	private void debugPrint(int num) {
		System.out.println("--" + num + "--");
	}
}
