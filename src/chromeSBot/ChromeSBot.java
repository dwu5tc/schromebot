package chromeSBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.JavascriptExecutor;

public class ChromeSBot 
{
	// grab links with http request (faster)
		// first time if refreshing on new, must check for change in elements
		// new doesn't have the item titles
	// implement a better way for people to set up the bot and their orders
	// implement a way to check that people don't share this bot
	// 
	// https://stackoverflow.com/questions/33225947/can-a-website-detect-when-you-are-using-selenium-with-chromedriver
	
	private List<Item> order = new ArrayList<Item>();
//	private List<String> links = new ArrayList<String>();
	private WebDriver driver;
	private String staleLink;
	private Elements freshLinks;
	private int sleep = 300;
	
	public static void main(String[] args) 
	{
		Scanner reader = new Scanner(System.in);
		
		System.out.println("ENTER TIME BETWEEN PAGE RESFRESHES (IN MS). 300-1000 RECOMMENDED.");
		int sleep = reader.nextInt();
		System.out.println("WILL REFRESH EVERY " + sleep + "MS.");
		ChromeSBot sBot = new ChromeSBot(sleep, false);
			
		sBot.grabStaleLink();
		
		System.out.println("ENTER 0 IF RUNNING THE JAR. ENTER ANYTHING ELSE OTHERWISE.");
		int i = reader.nextInt();	
		if (i == 0) 
		{	
			// properly implement this...
//			System.out.println("(JAR) BUILDING ORDER...");
//			String orderPath = "./order.txt";
//			try { sBot.buildOrderJar(orderPath); } 
//			catch (IOException e) { e.printStackTrace(); }
		}
		else {
			System.out.println("(NOT JAR) BUILDING ORDER...");
			String orderPath = "./src/chromeSBot/orderTest.txt";
			try { sBot.buildOrder(orderPath); } 
			catch (IOException e) { e.printStackTrace(); }
		}
		
		System.out.println("ORDER BUILT.");
		sBot.confirmOrder();
		
		System.out.println("ENTER ANYTHING TO BEGIN REFRESHING.");
		i = reader.nextInt();
		
		sBot.refreshPage();
		sBot.addToCart();
		sBot.checkout();
   
//		driver.get("http://www.stackoverflow.com");

	
		reader.close();
	}
	
	// constructor
	public ChromeSBot(int sleep, boolean isReal) 
	{
		this.sleep = sleep;
		System.setProperty("webdriver.chrome.driver", "C:\\SeleniumDrivers\\10052017\\chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		if (isReal) 
		{ 
			options.addArguments("user-data-dir=C:\\Users\\DeanW\\AppData\\Local\\Google\\Chrome\\AutomationProfile"); 
		}
		else 
		{ 
			options.addArguments("user-data-dir=C:\\Users\\DeanW\\AppData\\Local\\Google\\Chrome\\DSMProfile"); 
		}
		options.addArguments("disable-infobars");
		this.driver = new ChromeDriver(options);
	}
	
	// sets this.staleLink to the first product link of the /new page
	private void grabStaleLink() 
	{
		try 
		{
			Document doc = Jsoup.connect("http://www.supremenewyork.com/shop/new").get();
			Elements links = doc.select("div.turbolink_scroller a");
			this.staleLink = links.eq(0).attr("abs:href");
			System.out.println("STALE LINK SET: " + this.staleLink);
		}
		catch (Exception e)
		{
			System.out.println("Error in grabStaleLink.");
		}
	}
	
	// continually checks the /new page for updated links
	// updates this.freshLinks when the first first product link DOESN'T match this.staleLink
	private void refreshPage()
	{
		boolean notUpdated = true;
		int attemptNum = 1;
		while (notUpdated) 
		{
			try 
			{
				Document doc = Jsoup.connect("http://www.supremenewyork.com/shop/new").get();
				Elements links = doc.select("div.turbolink_scroller a");
				if (!links.eq(0).attr("abs:href").equals(this.staleLink)) // not equal
				{
					notUpdated = false;
					this.freshLinks = links;
					System.out.println("SHOP HAS BEEN UPDATED.");
					break; // redundant
				}
				else 
				{
					try 
					{ 
						System.out.println("REFRESHING " + attemptNum + ". SHOP NOT UPDATED");
						Thread.sleep(this.sleep); 
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
	
	// sets the order property
	// reads a file from specified (hardcoded) path
	public void buildOrder(String path) throws IOException
	{
		FileInputStream fis = new FileInputStream(path);
		// construct bufferedreader from inputstreamreader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	 
		String line = null;
		int itemCount = 1;
		outer:
		while ((line = br.readLine()) != null)
		{
			line = line.trim();
//			System.out.println("\n\nITEM NUMBER: " + itemCount + "\n");
			Item temp = new Item();
			while (!line.equals("-"))
			{
				String[] parts = line.split(":");
				if (parts.length > 1) 
				{
					parts[1] = parts[1].trim();
					setField(temp, parts);
				}
				else 
				{ 
					System.out.println("No value for field: " + parts[0]); 
				}
				if ((line = br.readLine()) != null) 
				{ 
					line.trim();
					continue;
				}
				else { break outer;	}
			}
			if (temp.isValid()) 
			{ 
				System.out.println("Item " + itemCount + " is valid.");
				this.order.add(temp); 
				itemCount++;  
			}
			else 
			{ 
				System.out.println("Item " + itemCount + " is NOT valid."); 
				itemCount++;
			}
		}
		br.close();
	}
	
	// buildOrder but for the JAR 
	public void buildOrderJar(String path) throws IOException 
	{
		return;
	}
	
	// used by buildOrder
	// sets the order field
	// parts[0] should be the key and parts[1] should be the value
	private static void setField(Item item, String[] parts)
	{
		switch (parts[0]) 
		{
			case "name": 
				item.setName(parts[1]);
				break;
			case "number": 
				// regex match for digits
				if (parts[1].matches("\\d+")) { item.setNumber(Integer.parseInt(parts[1])); }
				else { System.out.println("Number field is not a number."); }
				break;
//				case "type": 
//					item.setType(parts[1]);
//					break;
			case "colour": 
				item.setColour(parts[1]);
				break;
			case "size": 
				item.setSize(parts[1]);
				break;
		}
		System.out.println("set field: " + parts[0] + " = " + parts[1]);
	}
	
	// prints the order nicely for user to see
	public void confirmOrder()
	{
		int itemCount = 1;
		for (Item item : this.order) 
		{
			System.out.println("----- Item " + itemCount + " -----");
			item.printItem();
			System.out.print("\n");
			itemCount++;
		}
	}
	
	// iterates through each item and calls addToCart on each
	public void addToCart() 
	{
		// cart each item
		for (Item item : this.order)
		{
			addItem(item);
			System.out.println("Added.");
		}
	}

	//used by addToCart
	// navigates to URL, checks size, adds to cart
	private void addItem(Item item)
	{
		String targetLink = this.freshLinks.eq(item.getNumber()).attr("abs:href");
		if (targetLink != null ) 
		{
			((JavascriptExecutor) this.driver).executeScript("window.open('','_blank');");
			ArrayList<String> tabs = new ArrayList<String> (this.driver.getWindowHandles());
			this.driver.switchTo().window(tabs.get(tabs.size()-1));
			System.out.println("NAVIGATING TO: " + item.getUrl());
			this.driver.get(targetLink);
			
			// size select
			if (item.getSize() != null) 
			{
				Select sizeSelect = new Select(this.driver.findElement(By.id("size")));
				System.out.println(this.driver.getTitle() + " // Size --> " + item.getSize());
				sizeSelect.selectByVisibleText(item.getSize());
			}

			// colour select
			if (item.getColour() != null) 
			{
				WebElement colorSelect = this.driver.findElement(By.xpath("//a[@data-style-name='" + item.getColour()+"']"));
				System.out.println(this.driver.getTitle()+" // Colour --> " + item.getColour());
				//colorSelect.click();
				colorSelect.sendKeys(Keys.ENTER);
			}
			
			// add item to cart
			try 
			{
				WebElement addToCart = this.driver.findElement(By.xpath("//input[@value='add to cart']"));
				addToCart.sendKeys(Keys.ENTER);
				addToCart.sendKeys(Keys.ENTER);
				System.out.println(this.driver.getTitle() + " // Successfully Carted!");
			}
			catch (NoSuchElementException e)
			{
				System.out.println(this.driver.getTitle() + " // Sold Out***");
			}
		}
	}
	
	private void checkout() 
	{
		newTab();
		System.out.println("NAVIGATING TO CHECKOUT.");
		this.driver.get("https://www.supremenewyork.com/checkout");
	}
	
	// opens new tab and switches to it
	private void newTab()
	{
		((JavascriptExecutor) this.driver).executeScript("window.open('','_blank');");
		ArrayList<String> tabs = new ArrayList<String> (this.driver.getWindowHandles());
		this.driver.switchTo().window(tabs.get(tabs.size()-1));
	}
}



