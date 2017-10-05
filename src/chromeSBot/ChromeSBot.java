package chromeSBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

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
	
	private List<Item> order = new ArrayList<Item>();
	private List<String> links = new ArrayList<String>();
	private int sleep = -1;
	
	public static void main(String[] args) 
	{
		//http://stackoverflow.com/questions/11908249/debugging-element-is-not-clickable-at-point-error
		
		Scanner reader = new Scanner(System.in);
		
		List<Item> testOrder = new ArrayList<Item>();
		List<Item> realOrder = new ArrayList<Item>();
		List<Item> dummyOrder = new ArrayList<Item>();
		
//		Item hanes = new Item("accessories");
//		hanes.getNumber() = 3;
//		hanes.getSize() = "Large";
//
//		Item pillow = new Item("accessories");
//		pillow.getNumber() = 1;
		
//		Item stonewash = new Item("pants");
//		stonewash.getNumber() = 26;
//		stonewash.getSize() = "34";
//		
		Item dummy = new Item("pants");
		dummy.setNumber(1);
		
		Item testOne = new Item("skate");
		testOne.setName("Supreme®/Independent® Truck");
		testOne.setNumber(1);
		
		Item testTwo = new Item("jackets");
		testTwo.setNumber(8);
		testTwo.setColour("Red");
		
		/*
		Item two = new Item("tops_sweaters");
		two.name = "Overlap Tee";
		two.getSize() = "Medium";
		two.getColour() = "Olive";
		*/
		
		Item one = new Item("shoes");
		one.setName("Supreme®/Nike Air More Uptempo");
		one.setSize("8");
//		one.getNumber() = 4;
		one.setColour("Black");
		
		testOrder.add(testOne);
		testOrder.add(testTwo);
		realOrder.add(one);
//		realOrder.add(two);
		dummyOrder.add(dummy);
		
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
			String orderPath = "./src/chromeSBot/order.txt";
			try { sBot.buildOrder(orderPath); } 
			catch (IOException e) { e.printStackTrace(); }
		}
		
		System.out.println("ORDER BUILT.");
		sBot.confirmOrder();
		
		System.out.println("ENTER 0 IF TESTING. ANYTHING ELSE IF THIS IS FOR REAL.");
		i = reader.nextInt();
		
		if (i == 0) 
		{
			// testing
			System.setProperty("webdriver.chrome.driver", "C:\\SeleniumDrivers\\chromedriver.exe");
			ChromeOptions options = new ChromeOptions();
			options.addArguments("user-data-dir=C:\\Users\\DeanW\\AppData\\Local\\Google\\Chrome\\DSMProfile");
			options.addArguments("disable-infobars");
			WebDriver driver = new ChromeDriver(options);
			sBot.addToCart(driver);
		}
		else 
		{
			// real 
			System.setProperty("webdriver.chrome.driver", "C:\\SeleniumDrivers\\chromedriver.exe");
			ChromeOptions options = new ChromeOptions();
			options.addArguments("user-data-dir=C:\\Users\\DeanW\\AppData\\Local\\Google\\Chrome\\DSMProfile");
			options.addArguments("disable-infobars");
			WebDriver driver = new ChromeDriver(options);
			sBot.addToCart(driver);
		}
		

		
//		if (i == 0) { sBot.addToCart(driver, testOrder, sleep); }
//		else { sBot.addToCart(driver, realOrder, sleep); }
			
//		dummy(driver, dummyOrder);
		
//		driver.get("https://www.supremenewyork.com/checkout");
//
//		System.out.println("DONE.");
		reader.close();

	}
	
	public ChromeSBot() {}
	
	public ChromeSBot(int sleep) 
	{
		this.sleep = sleep;
	}
	
	public void buildOrderJar(String path) throws IOException 
	{
		return;
	}
	
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
	
	public void buildOrder(String path) throws IOException
	{
		FileInputStream fis = new FileInputStream(path);
		// construct bufferedreader from inputstreamreader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	 
		String line = null;
		int itemCount = 1;
		outer:
		while ((line = br.readLine().trim()) != null)
		{
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
	
	private static void addItem(Item item, WebDriver driver)
	{
		if (item.getUrl() != null ) 
		{
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

	public void addToCart(WebDriver driver) 
	{
		// cart each item
		for (Item item : this.order)
		{
			addItem(item, driver);
		}
		
		// go to checkout
		driver.get("https://www.supremenewyork.com/checkout");
		System.out.println("NAVIGATING TO CHEKOUT.");
	}
	
	public void refreshAndGrabLinks(WebDriver driver)
	{
		if (this.order.isEmpty()) { return; }
		boolean updated = false;
		
		driver.get("http://www.supremenewyork.com/shop/all/new");
		if (!updated) 
		{
			int refreshCount = 0; // 
			while (updated == false && refreshCount < 200) 
			{
				try 
				{
					driver.findElement(By.linkText(this.order.get(0).getName()));
					updated = true;
					break;
				}
				catch (NoSuchElementException e)  
				{
					refreshCount++;
					try { Thread.sleep(this.sleep); } 
					catch (InterruptedException e1) { e1.printStackTrace(); }
					driver.navigate().refresh();
					System.out.println("Refreshed! "+refreshCount);
				}				
			}
		}
		// grab Urls
		for (Item item : this.order)
		{
//			WebElement itemElem = driver.findElement(By.linkText(item.getName()));
			WebElement itemElem = driver.findElement(By.xpath("//*[@id=\"container\"]/article["+item.getNumber()+"]/div/h1/a"));
			String itemUrl = itemElem.getAttribute("href");
//			this.links.add(itemURL);
			item.setUrl(itemUrl);
		}
	}
	
	/*
	// returns the URL based on item type
	public static String buildURL(String type) {
		switch (type) 
		{
		case "shoes":
			return "http://www.supremenewyork.com/shop/all/shoes";
		case "sweatshirts":
			return "http://www.supremenewyork.com/shop/all/sweatshirts";
		case "t-shirts":
			return "http://www.supremenewyork.com/shop/all/t-shirts";
		case "jackets":
			return "http://www.supremenewyork.com/shop/all/jackets";
		case "accessories":
			return "http://www.supremenewyork.com/shop/all/accessories";
		case "hats":
			return "http://www.supremenewyork.com/shop/all/hats";
		case "tops_sweaters":
			return "http://www.supremenewyork.com/shop/all/tops_sweaters";
		case "shirts":
			return "http://www.supremenewyork.com/shop/all/shirts";
		case "bags":
			return "http://www.supremenewyork.com/shop/all/bags";
		case "pants":
			return "http://www.supremenewyork.com/shop/all/pants";
		case "skate":
			return "http://www.supremenewyork.com/shop/all/skate";
		}		
		return "http://www.supremenewyork.com/shop/all/new";
	}
	*/
	
	public void addToCart(WebDriver driver, List<Item> order, int sleepTime) 
	{

		if (order.isEmpty()) { return; } 

		boolean updated = false;
		Iterator<Item> iterator = order.iterator();

		while (iterator.hasNext())
		{	
			Item curr = iterator.next();
			
			driver.get(buildURL(curr.getType()));
			/*
			 *	STORE NOT YET UPDATED	
			 */ 

			if (!updated) 
			{
				int count = 0;
				while (updated == false && count < 2000) 
				{
					try 
					{
						WebElement item = driver.findElement(By.linkText(curr.getName()));
						//WebElement item = driver.findElement(By.xpath("//*[@id=\"container\"]/article["+curr.getNumber()+"]/div/h1/a"));
						String item_URL = item.getAttribute("href");
						driver.get(item_URL);
						
//						try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
						
//						item.sendKeys(Keys.ENTER);
//						item.click();
						updated = true;
						break;
					}
					catch (NoSuchElementException e)  
					{
						count++;
						
						try { Thread.sleep(sleepTime); } 
						catch (InterruptedException e1) { e1.printStackTrace(); }
						driver.navigate().refresh();
						
						System.out.println("Refreshed! "+count);
					}				
				}
				
//				driver.get("http://www.supremenewyork.com/shop/accessories/l1csjivrd/i9co4nsrv");
				
				if (curr.getColour() != null) 
				{
					WebElement colorSelect = driver.findElement(By.xpath("//a[@data-style-name='"+curr.getColour()+"']"));
					System.out.println(driver.getTitle()+" // Colour --> "+curr.getColour());
					//colorSelect.click();
					colorSelect.sendKeys(Keys.ENTER);
				}
				
				if (curr.getSize() != null) 
				{
					Select sizeSelect = new Select(driver.findElement(By.id("size")));
					System.out.println(driver.getTitle()+" // Size --> "+curr.getSize());
					sizeSelect.selectByVisibleText(curr.getSize());
				}
				
				try 
				{
					WebElement addToCart = driver.findElement(By.xpath("//input[@value='add to cart']"));
					//addToCart.click();
					addToCart.sendKeys(Keys.ENTER);
					addToCart.sendKeys(Keys.ENTER);
					System.out.println(driver.getTitle()+" // Successfully Carted!");
				}
				catch (NoSuchElementException e)
				{
					System.out.println(driver.getTitle()+" // Sold Out***");
				}
			}
			/*
			 *	STORE HAS NOW BEEN UPDATED	
			 */ 
			else 
			{
				WebElement item = driver.findElement(By.xpath("//*[@id=\"container\"]/article["+curr.getNumber()+"]/div/h1/a"));
				String item_URL = item.getAttribute("href");
				driver.get(item_URL);
				
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
				
				if (curr.getColour() != null) 
				{
					WebElement colorSelect = driver.findElement(By.xpath("//a[@data-style-name='"+curr.getColour()+"']"));
					System.out.println(driver.getTitle()+" // Colour --> "+curr.getColour());
					//colorSelect.click();
					colorSelect.sendKeys(Keys.ENTER);
				}
				
				if (curr.getSize() != null) 
				{
					Select sizeSelect = new Select(driver.findElement(By.id("size")));
					System.out.println(driver.getTitle()+" // Size --> "+curr.getSize());
					sizeSelect.selectByVisibleText(curr.getSize());
				}
				
				try 
				{
					WebElement addToCart = driver.findElement(By.xpath("//input[@value='add to cart']"));
					//addToCart.click();
					addToCart.sendKeys(Keys.ENTER);
					addToCart.sendKeys(Keys.ENTER);
					System.out.println(driver.getTitle()+" // Successfully Carted!");
				}
				catch (NoSuchElementException e)
				{
					System.out.println(driver.getTitle()+" // Sold Out***");
				}
			}
		}
	}

	public void dummy(WebDriver driver, List<Item> order) {
	
		if (order.isEmpty()) { return; } 
	
		Iterator<Item> iterator = order.iterator();
	
		while (iterator.hasNext())
		{	
			Item curr = iterator.next();
			
			driver.get(buildURL(curr.getType()));
	
			/*
			 *	STORE NOT YET UPDATED	
			 */ 
//			if (!updated) 
//			{
//				int count = 0;
//				while (updated == false && count < 2000) 
//				{
//					try 
//					{
//						WebElement item = driver.findElement(By.xpath("//*[@id=\"container\"]/article["+curr.getNumber()+"]/div/h1/a"));
//						String item_URL = item.getAttribute("href");
//						driver.get(item_URL);
//						
//	//					try {
//	//						Thread.sleep(1000);
//	//					} catch (InterruptedException e1) {
//	//						// TODO Auto-generated catch block
//	//						e1.printStackTrace();
//	//					}
//						
//	//					item.sendKeys(Keys.ENTER);
//	//					item.click();
//						updated = true;
//						break;
//					}
//					catch (NoSuchElementException e)  
//					{
//						count++;
//						driver.navigate().refresh();
//						System.out.println("Refreshed! "+count);
//					}				
//				}
//				
//	//			driver.get("http://www.supremenewyork.com/shop/accessories/l1csjivrd/i9co4nsrv");
//				
//				if (curr.getColour() != null) 
//				{
//					WebElement colorSelect = driver.findElement(By.xpath("//a[@data-style-name='"+curr.getColour()+"']"));
//					System.out.println(driver.getTitle()+" COLOUR OPTION 1");
//					//colorSelect.click();
//					colorSelect.sendKeys(Keys.ENTER);
//				}
//				
//				if (curr.getSize() != null) 
//				{
//					Select sizeSelect = new Select(driver.findElement(By.id("size")));
//					System.out.println(driver.getTitle()+" SIZE OPTION 1");
//					sizeSelect.selectByVisibleText(curr.getSize());
//				}
//				
//				try 
//				{
//					WebElement addToCart = driver.findElement(By.xpath("//input[@value='add to cart']"));
//					//addToCart.click();
//					addToCart.sendKeys(Keys.ENTER);
//					addToCart.sendKeys(Keys.ENTER);
//				}
//				catch (NoSuchElementException e)
//				{
//					System.out.println("SOLD OUT");
//				}
//			}
//			/*
//			 *	STORE HAS NOW BEEN UPDATED	
//			 */ 
//			else 
//			{
//				WebElement item = driver.findElement(By.xpath("//*[@id=\"container\"]/article["+curr.getNumber()+"]/div/h1/a"));
//				String item_URL = item.getAttribute("href");
//				driver.get(item_URL);
//				
//	//			try {
//	//				Thread.sleep(1000);
//	//			} catch (InterruptedException e1) {
//	//				// TODO Auto-generated catch block
//	//				e1.printStackTrace();
//	//			}
//				
//				if (curr.getColour() != null) 
//				{
//					WebElement colorSelect = driver.findElement(By.xpath("//a[@data-style-name='"+curr.getColour()+"']"));
//					System.out.println(driver.getTitle()+" COLOUR OPTION 2");
//					//colorSelect.click();
//					colorSelect.sendKeys(Keys.ENTER);
//				}
//				
//				if (curr.getSize() != null) 
//				{
//					Select sizeSelect = new Select(driver.findElement(By.id("size")));
//					System.out.println(driver.getTitle()+" SIZE OPTION 2");
//					sizeSelect.selectByVisibleText(curr.getSize());
//				}
//				
//				try 
//				{
//					WebElement addToCart = driver.findElement(By.xpath("//input[@value='add to cart']"));
//					//addToCart.click();
//					addToCart.sendKeys(Keys.ENTER);
//					addToCart.sendKeys(Keys.ENTER);
//				}
//				catch (NoSuchElementException e)
//				{
//					System.out.println("SOLD OUT");
//				}
			}
		}
}



