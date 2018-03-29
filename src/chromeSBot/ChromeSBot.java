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
import org.jsoup.select.Elements;

public class ChromeSBot {
	
	// private String version = "1.0.0";
	// multithread
	// static vs nonstatic methods
	// private vs pubic methods
	// rearrange methods
	// better method names
	// implement better exception handling
	// optimize
	// https://stackoverflow.com/questions/33225947/can-a-website-detect-when-you-are-using-selenium-with-chromedriver
	private List<ChromeSBotThread> sBots = new ArrayList<ChromeSBotThread>();
	private int refreshDelay;
	private String shopPath;
	private String staleLink;
	
	public static void main(String[] args) {
		Scanner reader = new Scanner(System.in);
		

		
		System.out.println("ENTER TIME BETWEEN PAGE RESFRESHES (IN MS). 300-1000 RECOMMENDED.");
		int delay = 500;
		delay = reader.nextInt();
		
		ChromeSBot chromeSBot = new ChromeSBot(args);
		chromeSBot.grabStaleLink();
		chromeSBot.setRefreshDelay(delay);
		
		System.out.println("WILL REFRESH EVERY " + delay + "MS.");
		
		if (args.length < 1 || args[0] == "test")
		{
			chromeSBot.grabLinks();
		}
		else 
		{
			chromeSBot.refreshAndGrabLinks();
		}
//		System.out.println("ENTER A INTEGER TO BEGIN CARTING.");
//		int lol = reader.nextInt();
//		System.out.println(lol);
		// fix this shit wtf
		chromeSBot.cart();
		
		reader.close();
	}
	
	public ChromeSBot(String[] args)
	{
		Scanner reader = new Scanner(System.in);
		
		if (args.length < 1 || args[0] == "test") 
		{
			int numOfTestBots = 3;
			if (args.length > 0) { numOfTestBots = Integer.parseInt(args[1]); }
			for (int i = 0; i < numOfTestBots; i++)
			{
				ChromeSBotThread testBot = new ChromeSBotThread();
				try 
				{
					System.out.println("(TXT) BUILDING ORDER...");
					testBot.buildOrderFromFile();
					System.out.println("ORDER BUILT");
					testBot.confirmOrder();
					System.out.println("ENTER TO CONTINUE");
					reader.nextLine();
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				this.sBots.add(testBot);
			}
		}
		else if (args[0] == "real")
		{
			for (int i = 1; i < (args.length - 1) / 3; i += 3) // skip 0 and 1
			{
				ChromeSBotThread sBot = new ChromeSBotThread(Integer.parseInt(args[i + 1]), args[i + 2], args[i + 3]); // cartDelay, orderPath, profile
				try 
				{
					System.out.println("(TXT) BUILDING ORDER...");
					sBot.buildOrderFromFile();
					System.out.println("ORDER BUILT");
					sBot.confirmOrder();
					System.out.println("ENTER TO CONTINUE");
					reader.nextLine();
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				this.sBots.add(sBot);
			}
		}
		
		reader.close();
	}
	
	private void setRefreshDelay(int delay) {
		this.refreshDelay = delay;
	}
	
	// grab stale link (first product link of the target page) and set the stale link prop
	// might be faster just to check the element and not the href???
	private void grabStaleLink()
	{
		try
		{
			String target = "http://www.su" + "pr" + "em" + "en" + "ew" + "yo" + "rk.com" + "/sh" + "op/" + this.shopPath;
			Elements links = null;
			Document doc = Jsoup.connect(target).get();
			
			if (this.shopPath == "new" || this.shopPath == "all")
			{
				links = doc.select("div.turbolink_scroller a");
			}
			else 
			{
				links = doc.select("div.shop a");
			}
	
			this.staleLink = links.eq(0).attr("abs:href");
			System.out.println("STALE LINK SET: " + this.staleLink);
		}
		catch (Exception e)
		{
			System.out.println("Error in grabStaleLink.");
		}
	}

	// continually grab the links and check that stale link has been swapped out
	// updates this.freshLinks upon mismatch
	private void refreshAndGrabLinks()
	{
		String target = "http://www.su" + "pr" + "em" + "en" + "ew" + "yo" + "rk.com" + "/sh" + "op/" + this.shopPath;
		String linksContainer;
		if (this.shopPath == "new" || this.shopPath == "all")
		{
			linksContainer = "div.turbolink_scroller a";
		}
		else 
		{
			linksContainer = "div.shop a";
		}
		boolean notUpdated = true;
		int attemptNum = 1;
		while (notUpdated)
		{
			try
			{
				Document doc = Jsoup.connect(target).get();
				Elements links = doc.select(linksContainer);
				if (!links.eq(0).attr("abs:href").equals(this.staleLink)) // not equal (! for real)
				{
					notUpdated = false;
					for (ChromeSBotThread sbot : this.sBots)
					{
						sbot.setLinks(links);
					}
					System.out.println("SHOP HAS BEEN UPDATED.");
					break; // redundant
				}
				else
				{
					try
					{
						System.out.println("REFRESHING " + attemptNum + ". SHOP NOT UPDATED");
						Thread.sleep(this.refreshDelay);
						attemptNum++;
					}
					catch (Exception e)
					{
						System.out.println("Error in nested try-catch of refreshPage.");
					}
				}
			}
			catch (Exception e)
			{
				System.out.println("Error in refreshPage.");
			}
		}
	}

	private void grabLinks()
	{
		String target = "http://www.su" + "pr" + "em" + "en" + "ew" + "yo" + "rk.com" + "/sh" + "op/" + this.shopPath;
		String linksContainer;
		if (this.shopPath == "new" || this.shopPath == "all")
		{
			linksContainer = "div.shop a";
		}
		else 
		{
			linksContainer = "div.turbolink_scroller a";
		}
		try 
		{
			Document doc = Jsoup.connect(target).get();
			Elements links = doc.select(linksContainer);
			for (ChromeSBotThread sbot : this.sBots)
			{
				sbot.setLinks(links);
			}
			System.out.println("SHOP HAS BEEN UPDATED.");
		}
		catch (Exception e)
		{
			System.out.println("Error in notFirstDrop.");
		}
	}
	
	private void cart()
	{
		for (ChromeSBotThread sBot : this.sBots)
		{
			long startTime = System.nanoTime();
			
			sBot.addToCart();
			sBot.checkout();
			
			long endTime = System.nanoTime();
			double elapsedTime = (double)(endTime - startTime)/1000000000.00;
			System.out.println("ATC: " + elapsedTime + " seconds.");
		}
	}
}
