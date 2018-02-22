package chromeSBot;

import java.io.BufferedReader;
//import java.io.File;
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
//import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.JavascriptExecutor;

public class ChromeSBot 
{
	// site with firebase db to allow users to create orders 
	// sBot will build orders using a GET request to the site
	// give out keys to people
	// 1 order per key
	// order: item num, size + path to chrome profile
	// way to make sure people arent using the same key to order 
	// resale calculator on the site
	// implement better exception handling
	// https://stackoverflow.com/questions/33225947/can-a-website-detect-when-you-are-using-selenium-with-chromedriver
	
	private String version = "1.0.0";
	private List<Item> order = new ArrayList<Item>();
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
		ChromeSBot sBot = new ChromeSBot(sleep, args[0]); // false = dsmprofile, true = automationprofile or user profile
				
		sBot.grabStaleLink();
		
		System.out.println("(TXT) BUILDING ORDER...");
		String orderPath = args[1];
		try { sBot.buildOrder(orderPath); } 
		catch (IOException e) { e.printStackTrace(); }
		
		System.out.println("ORDER BUILT.");
		sBot.confirmOrder();
		
		System.out.println("ENTER A NUMBER TO BEGIN REFRESHING.");
		reader.nextInt();
		
		sBot.refreshPage();
		System.out.println("SITE HAS BEEN UPDATED. CARTING ITEMS NOW...");
		
		long startTime = System.nanoTime();
		
		sBot.addToCart();
		sBot.checkout();
		
		long endTime = System.nanoTime();
		double elapsedTime = (double)(endTime - startTime)/1000000000.00;
		
		System.out.println("ATC: " + elapsedTime + " seconds.");
   	
		reader.close();
	}
	
	// constructor
	// set refresh rate and chrome profile
	public ChromeSBot(int sleep, String profile) 
	{
		this.sleep = sleep;
		System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.addArguments("user-data-dir=" + profile);
		options.addArguments("disable-infobars");
		this.driver = new ChromeDriver(options);
	}
	
	// grab stale link and set the stale link prop
	// this link should be swapped out on shop update  
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
	
	// continually grab the links within the "turolink_scroller div" and check that stale link has been swapped out
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
				if (!links.eq(0).attr("abs:href").equals(this.staleLink)) // not equal (! for real)
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
	
	// build order from .txt file specified at the path
	public void buildOrder(String path) throws IOException
	{
		FileInputStream fis = new FileInputStream(path);	
		BufferedReader br = new BufferedReader(new InputStreamReader(fis)); // construct bufferedreader from inputstreamreader
	 
		String line = null;
		int itemCount = 1;
		outer:
		while ((line = br.readLine()) != null)
		{
			line = line.trim();
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
					System.out.println("***No value for " + parts[0] + " field***"); 
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
				System.out.println("Item " + itemCount + " is valid. Appended to order.");
				this.order.add(temp); 
				itemCount++;  
			}
			else 
			{ 
				System.out.println("Item " + itemCount + " is NOT valid. Ignored."); 
				itemCount++;
			}
		}
		br.close();
	}
	
	
	// buildOrder but from site
	public void buildOrderWeb(String path) throws IOException 
	{
		return;
	}
	
	// set the number and colour fields of each item to be added to the order prop
	// used by buildOrder
	// sets the order field
	// parts[0] should be the key and parts[1] should be the value
	private static void setField(Item item, String[] parts)
	{
		switch (parts[0]) 
		{
			case "number": 
				if (parts[1].matches("\\d+")) // regex match for digits
				{ 
					item.setNumber(Integer.parseInt(parts[1])); 
				} 
				else 
				{ 
					System.out.println("Number field is not a number."); 
				}
				break;
			case "size": 
				item.setSize(parts[1]);
				break;
		}
		System.out.println("Set " + parts[0] + " field --> " + parts[1]);
	}
	
	// prints out each order item for user to confirm
	
	// prints the order nicely for user to see
	public void confirmOrder()
	{
		int itemCount = 1;
		for (Item item : this.order) 
		{
			System.out.println("----- Item " + itemCount + " -----");
			item.printItem();
			itemCount++;
		}
	}
	
	// cart the order
	
	// iterates through each item and calls addToCart on each
	public void addToCart() 
	{
		for (Item item : this.order)
		{
			addItem(item);
			System.out.println("Added.");
		}
	}

	
	// navigate to URL, select size, add to cart (used by addToCart)
	private void addItem(Item item)
	{
		String targetLink = this.freshLinks.eq(item.getNumber()).attr("abs:href");
		if (targetLink != null ) 
		{
			this.newTab();
			// FIX THIS URL BS
			System.out.println("NAVIGATING TO: " + item.getUrl());
			this.driver.get(targetLink);
			
			if (item.getSize() != null) 
			{
				this.selectSize(item.getSize());
			}
			this.clickAddToCart();
		}
	}
	
	// open and switch to new tab
	private void newTab()
	{
		((JavascriptExecutor) this.driver).executeScript("window.open('','_blank');");
		ArrayList<String> tabs = new ArrayList<String> (this.driver.getWindowHandles());
		this.driver.switchTo().window(tabs.get(tabs.size()-1));
	}
	
	// select item size 
	private void selectSize(String size) 
	{
		try
		{
			Select sizeSelect = new Select(this.driver.findElement(By.id("s")));
			System.out.println(this.driver.getTitle() + " // Size --> " + size);
			sizeSelect.selectByVisibleText(size);
		}
		catch (Exception e)
		{
			System.out.println(this.driver.getTitle() + " // COULD NOT SELECT SIZE BY ID***");
			System.out.println("ATTEMPTING TO SELECT SIZE BY TAGNAME.");
			try 
			{
				Select sizeSelect = new Select(this.driver.findElement(By.tagName("select")));
				System.out.println(this.driver.getTitle() + " // Size --> " + size);
				sizeSelect.selectByVisibleText(size);
			}
			catch (Exception e2)
			{
				System.out.println(this.driver.getTitle() + " // COULD NOT SELECT SIZE BY TAGNAME GG***");
			}
		}
	}
	
	// click add to cart button
	private void clickAddToCart()
	{
		try 
		{
			WebElement addToCart = this.driver.findElement(By.xpath("//input[@value='add to cart']"));
			addToCart.sendKeys(Keys.ENTER);
			System.out.println(this.driver.getTitle() + " // Successfully Carted!");
		}
		catch (Exception e)
		{
			System.out.println(this.driver.getTitle() + " // COULD NOT CART ITEM***");
			System.out.println("ATTEMPTING TO CART ITEM AGAIN");
			try 
			{
				WebElement addToCart = this.driver.findElement(By.name("commit"));
				addToCart.sendKeys(Keys.ENTER);
				System.out.println(this.driver.getTitle() + " // Successfully Carted!");
			}
			catch (Exception e2)
			{
				System.out.println(this.driver.getTitle() + " // COULD NOT CART ITEM AGAIN GG***");
			}
		}
	}
	
	// navigate to checkout page
	private void checkout() 
	{
		this.newTab();
		System.out.println("NAVIGATING TO CHECKOUT.");
		this.driver.get("https://www.supremenewyork.com/checkout");
	}
}



