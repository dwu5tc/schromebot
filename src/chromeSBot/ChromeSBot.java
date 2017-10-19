package chromeSBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

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
	private String staleURL;
	private int sleep = 300;
	
	public static void main(String[] args) 
	{
		Scanner reader = new Scanner(System.in);
		
		System.out.println("ENTER TIME BETWEEN PAGE RESFRESHES (IN MS). 300-1000 RECOMMENDED.");
		int sleep = reader.nextInt();
		System.out.println("WILL REFRESH EVERY " + sleep + "MS.");
		ChromeSBot sBot = new ChromeSBot(sleep);
		
		System.out.println("ENTER 0 IF RUNNING THE JAR. ANYTHING ELSE OTHERWISE.");
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
		
		System.out.println("ENTER 0 IF TESTING. ANYTHING ELSE IF THIS IS FOR REAL.");
		i = reader.nextInt();
		
		System.setProperty("webdriver.chrome.driver", "C:\\SeleniumDrivers\\10052017\\chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
//		options.addArguments("disable-infobars");
		if (i == 0) 
		{
			// testing
//			options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
			options.addArguments("user-data-dir=C:\\Users\\DeanW\\AppData\\Local\\Google\\Chrome\\DSMProfile");
		}
		else 
		{
			// real
//			options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
			options.addArguments("user-data-dir=C:\\Users\\DeanW\\AppData\\Local\\Google\\Chrome\\AutomationProfile");
		}
		options.addArguments("disable-infobars");
		WebDriver driver = new ChromeDriver(options);
		
		
		try 
		{
			Document doc = Jsoup.connect("http://www.supremenewyork.com/shop/new").get();
			Elements links = doc.select("div.turbolink_scroller a");
		
	        print("\nLinks: (%d)", links.size()); 
	        for (Element link : links) {
	            print(" * a: <%s>  (%s)", link.attr("abs:href"), link.text(), 35);
	        }
		}
		catch (Exception e)
		{
			System.out.println("wtf");
		}

        
		
		
		
		
//		driver.get("http://www.stackoverflow.com");
		String body;
		try 
		{ 
			body = sBot.sendGET("http://www.supremenewyork.com/shop/new"); 
			System.out.println(body);
			boolean isUpdated = false;
			int attemptNum = 1;
			while (!isUpdated)
			{
				String temp = sBot.sendGET("http://www.supremenewyork.com/shop/new");
				if (attemptNum < 5 && body.equals(temp)) 
				{
					System.out.println(attemptNum + " :: NOT UPDATED");
				}
				else 
				{
					isUpdated = true;
					sBot.refreshAndGrabLinks(driver);
					sBot.addToCart(driver);	
					break;
				}
				Thread.sleep(1000);
				attemptNum++;
			}
		}
		catch (Exception e) { e.printStackTrace(); }
		
	
		reader.close();
	}
	
	public ChromeSBot(int sleep) 
	{
		this.sleep = sleep;
		System.setProperty("webdriver.chrome.driver", "C:\\SeleniumDrivers\\10052017\\chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.addArguments("user-data-dir=C:\\Users\\DeanW\\AppData\\Local\\Google\\Chrome\\AutomationProfile");
		options.addArguments("disable-infobars");
		this.driver = new ChromeDriver(options);
	}
	
	private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }
	
	public void buildOrderJar(String path) throws IOException 
	{
		return;
	}
	
	public static void grabLinkz() throws IOException {

	}
	
	/*private static List<String> parseForLinks(String body) 
	{
		List<String> links = new ArrayList<String>();
		
	}*/
	
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
//			case "type": 
//				item.setType(parts[1]);
//				break;
			case "colour": 
				item.setColour(parts[1]);
				break;
			case "size": 
				item.setSize(parts[1]);
				break;
		}
		System.out.println("set field: " + parts[0] + " = " + parts[1]);
	}
	 
	// sets the order property of the sBot instance
	// reads a file from specified path
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
	
	// used by addToCart
	// navigates to URL, checks size, adds to cart
	private static void addItem(Item item, WebDriver driver)
	{
		if (item.getUrl() != null ) 
		{
			System.out.println(item.getUrl());
			driver.get(item.getUrl());
			
			// size select
			if (item.getSize() != null) 
			{
				Select sizeSelect = new Select(driver.findElement(By.id("size")));
				System.out.println(driver.getTitle() + " // Size --> " + item.getSize());
				sizeSelect.selectByVisibleText(item.getSize());
			}

			// colour select
			if (item.getColour() != null) 
			{
				WebElement colorSelect = driver.findElement(By.xpath("//a[@data-style-name='" + item.getColour()+"']"));
				System.out.println(driver.getTitle()+" // Colour --> " + item.getColour());
				//colorSelect.click();
				colorSelect.sendKeys(Keys.ENTER);
			}
			
			// add item to cart
			try 
			{
				WebElement addToCart = driver.findElement(By.xpath("//input[@value='add to cart']"));
				addToCart.sendKeys(Keys.ENTER);
				addToCart.sendKeys(Keys.ENTER);
				System.out.println(driver.getTitle() + " // Successfully Carted!");
			}
			catch (NoSuchElementException e)
			{
				System.out.println(driver.getTitle() + " // Sold Out***");
			}
		}
	}
	
	// returns URL for specific item type
	private static String getRefreshPage(Item item) 
	{
		switch (item.getType()) 
 		{
 		case "jackets":
 			return "http://www.supremenewyork.com/shop/all/jackets";
 		case "shirts":
 			return "http://www.supremenewyork.com/shop/all/shirts";
 		case "tops_sweaters":
 			return "http://www.supremenewyork.com/shop/all/tops_sweaters";
 		case "sweatshirts":
 			return "http://www.supremenewyork.com/shop/all/sweatshirts";
 		case "t-shirts":
 			return "http://www.supremenewyork.com/shop/all/t-shirts";
 		case "pants":
 			return "http://www.supremenewyork.com/shop/all/pants";
 		case "hats":
 			return "http://www.supremenewyork.com/shop/all/hats";
 		case "bags":
 			return "http://www.supremenewyork.com/shop/all/bags";
 		case "accessories":
 			return "http://www.supremenewyork.com/shop/all/accessories";
 		case "skate":
 			return "http://www.supremenewyork.com/shop/all/skate";
 		case "shoes":
 			return "http://www.supremenewyork.com/shop/all/shoes";
 		}		
 		return "http://www.supremenewyork.com/shop/all/new";
 	}
	
	// iterates through each item and calls addToCart on each
	public void addToCart(WebDriver driver) 
	{
		// cart each item
		for (Item item : this.order)
		{
			driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"T");
			addItem(item, driver);
			System.out.println("Added.");
		}
		
		// go to checkout
		driver.get("https://www.supremenewyork.com/checkout");
		System.out.println("NAVIGATING TO CHECKOUT.");
	}
	
	// check if the webstore has updated
	public boolean checkUpdate() 
	{
		return true;
	}
	
	// sends a GET request to supreme and returns the stringified HTML body
	public String sendGET(String getURL) throws IOException 
	{
		URL obj = new URL(getURL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) 
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
//			int lineNum = 0;
			while ((inputLine = in.readLine()) != null ) 
			{
				response.append(inputLine);
//				System.out.println(lineNum + ": " + inputLine);
//				lineNum++;
			}
			in.close();
			return response.toString();
		}
		else 
		{
			System.out.println("GET REQUEST FAILED.");
			return null;
		}
	}
	
	public void refreshAndGrabLinks(WebDriver driver)
	{
//		if (this.order.isEmpty()) 
//		{ 
//			System.out.println("order empty");
//			return; 
//		}
//		boolean updated = false;
//		
		driver.get("http://www.supremenewyork.com/shop/all/new");
		System.out.println(driver.getTitle());
		System.out.println("Here");
		System.out.println(driver.getPageSource());
		driver.get("http://www.supremenewyork.com/shop/all/new");
		System.out.println(driver.getTitle());
		System.out.println("Here");
		System.out.println(driver.getPageSource());
		driver.get("http://www.supremenewyork.com/shop/all/new");
		System.out.println(driver.getTitle());
		System.out.println("Here");
		System.out.println(driver.getPageSource());
//		if (!updated) 
//		{
//			int refreshCount = 0; // 
//			while (updated == false && refreshCount < 200) 
//			{
//				try 
//				{
//					driver.findElement(By.linkText(this.order.get(0).getName()));
//					updated = true;
//					break;
//				}
//				catch (NoSuchElementException e)  
//				{
//					refreshCount++;
//					try { Thread.sleep(this.sleep); } 
//					catch (InterruptedException e1) { e1.printStackTrace(); }
//					driver.navigate().refresh();
//					System.out.println("Refreshed! "+refreshCount);
//				}				
//			}
//		}
		
		// grab Urls
		for (Item item : this.order)
		{
			if (item.getNumber() != -1)
			{
				try
				{
					System.out.println(driver.getPageSource());
					WebElement itemElem = driver.findElement(By.xpath("//*[@id=\"container\"]/article["+item.getNumber()+"]/div/h1/a"));
					String itemUrl = itemElem.getAttribute("href");
					System.out.println("Got Url: " + itemUrl);
					item.setUrl(itemUrl);
//					this.links.add(itemURL);
				}
				catch (NoSuchElementException e) { e.printStackTrace(); }
			}
			else  
			{ 
				try
				{
					WebElement itemElem = driver.findElement(By.linkText(item.getName()));
					String itemUrl = itemElem.getAttribute("href");
					System.out.println("Got Url: " + itemUrl);
					item.setUrl(itemUrl);
//					this.links.add(itemURL);
				}
				catch (NoSuchElementException e) { e.printStackTrace(); }
			}	
		}
	}
}



