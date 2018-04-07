package chromeSBot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
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

public class ChromeSBotThread implements Runnable
{
	private Thread thread;
	private WebDriver driver;
	
	private int cartDelay;
//	private String orderPath;
	private String configPath; // json with order and cc info
	
	private List<Item> order = new ArrayList<Item>();
	private Card card;
	private Elements links;
	
	private double startTime;
	private double elapsedTime;
	
	// set delay time between cart attempts, 
	// chrome profile to automate, 
	// path for order construction
	public ChromeSBotThread(int cartDelay, String profile, String configPath) {
		this.thread = new Thread(this, "[" + cartDelay + " " + profile + " " + configPath + "]");
		this.cartDelay = cartDelay;
//		this.orderPath = orderPath;
		this.configPath = configPath;
		this.startTime = System.nanoTime();
		System.setProperty("webdriver.chrome.driver", "chromedriver"); // chromedriver.exe for windows
		ChromeOptions options = new ChromeOptions();
		options.addArguments("user-data-dir=" + profile);
		options.addArguments("disable-infobars");
		this.driver = new ChromeDriver(options);
		try {
			System.out.println("Configuring from: " + this.configPath);
			this.configBot();
			this.confirmOrder();
			this.confirmCard();
		} catch (Exception e) {
			// handle!!!
		}
	}
	
	// for testing (no profile used)
	public ChromeSBotThread() {
		this.thread = new Thread(this);
//		this.orderPath = "test.txt";
		this.configPath = "config.txt";
		this.cartDelay = 200;
		this.startTime = System.nanoTime();
		System.setProperty("webdriver.chrome.driver", "chromedriver"); // chromedriver.exe for windows
		ChromeOptions options = new ChromeOptions();
		options.addArguments("disable-infobars");
		this.driver = new ChromeDriver(options);
		try {
			System.out.println("Configuriing from: " + this.configPath);
			this.configBot();
			this.confirmOrder();
			this.confirmCard();
		} catch (Exception e) {
			// handle!!!
		}
	}
	
	public void setStartTime(long time) {
		this.startTime = time;
	}
	
	public void setThreadName(String name) {
		this.thread.setName(name);
	}
	
	// are these even necessary???
	public void setCartDelay(int delay) {
		this.cartDelay = delay;
	}
	
	public void setConfigPath(String path) {
		this.configPath = path;
	}
	
	public void setOrder(List<Item> order) {
		this.order = order;
	}
	
	public void setLinks(Elements links) {
		this.links = links;
	}
	
	public Thread getThread() {
		return this.thread;
	}
	
	public double getElapsedTime() {
		return this.elapsedTime;
	}
	
	// read .txt file from specified path and build order
//	public void buildOrderFromTxt() throws IOException {
//		FileInputStream fis = new FileInputStream(this.orderPath);
//		BufferedReader br = new BufferedReader(new InputStreamReader(fis)); // construct bufferedreader from inputstreamreader
//
//		String line = null;
//		int itemCount = 1;
//		outer:
//		while ((line = br.readLine()) != null) {
//			line = line.trim();
//			Item item = new Item();
//			while (!line.equals("-")) { // inner
//				String[] params = line.split(":");
//				if (params.length > 1) {
//					params[0] = params[0].trim().toLowerCase();
//					params[1] = params[1].trim();
//					setField(item, params);
//				} else {
//					System.out.println("NO VALUE FOR " + params[0] + " FIELD***");
//				}
//				if ((line = br.readLine()) != null) {
//					line.trim(); // to match trim call of outer???
//					continue;
//				} else { 
//					break outer;	
//				}
//			}
//			if (item.isValid()) {
////				System.out.println("Item " + itemCount + " is valid. Appended to order.");
//				this.order.add(item);
//				itemCount++;
//			} else {
//				System.out.println("ITEM " + itemCount + " IS NOT VALID. IGNORED.***");
//				itemCount++;
//			}
//		}
//		if (this.order.size() > 0) {
//			System.out.println("Order successfully built.");
//		}
//		br.close();
//	}
	
	public void configBot() {
		String extension = null;
		String json = null;
		
		int i = this.configPath.lastIndexOf(".");
		if (i > 0) {
		    extension = this.configPath.substring(i + 1);
		}
		if (extension.equals("json")) {
			json = this.fetchJsonFromFile();
		} else {
			json = this.fetchJsonFromWeb();
		}
		JSONObject obj = new JSONObject(json);
		if (!obj.isNull("order")) {
			JSONArray jsonArrOrder = obj.getJSONArray("order");
			this.buildOrderFromJson(jsonArrOrder);
		}
		if (!obj.isNull("card")) {
			JSONObject jsonObjCard = obj.getJSONObject("card");
			this.buildCardFromJson(jsonObjCard);
		}
	}
	
	public String fetchJsonFromFile() throws IOException {
	    String content = "";
	    try {
	        content = new String(Files.readAllBytes(Paths.get(this.configPath)));
	    } catch (IOException e) {
	        // handle
	    }
	    return content;
	}
	
	public void fetchJsonFromWeb() {
		return;
	}
	
	// build order from JSONArray
	private void buildOrderFromJson(JSONArray jsonArr) {
		for (int i = 0; i < jsonArr.length(); i++) {
			JSONObject jsonItem = jsonArr.getJSONObject(i);
			Item item = new Item();
			if (!jsonItem.isNull("size")) {
				item.setSize(jsonItem.getString("size"));
			}
			if (!jsonItem.isNull("number") && jsonItem.getInt("number") > 0) {
				item.setNumber(jsonItem.getInt("number"));
				this.order.add(item); // only add item if 
				System.out.println("Item " + i + " is valid. Appended to order.");
			} else {
				System.out.println("INVALID NUMBER FIELD. ITEM " + i + " IGNORED.***");
			}
		}
	}
	
	// build card from JSONObject
	private void buildCardFromJson(JSONObject obj) {
		if (!obj.isNull("number") && !obj.isNull("month") && !obj.isNull("year") && !obj.isNull("cvv")) {
			this.card.setNumber(obj.getString("number"));
			this.card.setMonth(obj.getString("month"));
			this.card.setYear(obj.getString("year"));
			this.card.setCvv(obj.getString("cvv"));
		}
	}

	// set the number and size fields of each item to be added to the order
	// parts[0] should be the key and parts[1] should be the value
//	private static void setField(Item item, String[] parts) {
//		switch (parts[0]) {
//			case "number":
//				if (parts[1].matches("\\d+")) { // regex match for digits 
//					item.setNumber(Integer.parseInt(parts[1]));
////					System.out.println("Set " + parts[0] + " field --> " + parts[1]);
//				}
//				else {
//					System.out.println("NUMBER FIELD IS NOT A NUMBER.***");
//				}
//				break;
//			case "size":
//				// check proper input (like regex match for number)!!!
//				item.setSize(parts[1]);
////				System.out.println("Set " + parts[0] + " field --> " + parts[1]);
//				break;
//			default:
//				System.out.println("FIELD NEITHER NUMBER NOR SIZE. [" + parts[0] + ", " + parts[1] + "]***");
//		}
//	}

	// prints out each order item for user to confirm
	public void confirmOrder() {
		int itemCount = 1;
		for (Item item : this.order) {
			System.out.print(this.thread.getName() + " ");
			System.out.println(itemCount + ". " + item.getNumber() + " - " + item.getSize());
//			System.out.println("----- Item " + itemCount + " -----");
//			item.printItem();
			itemCount++;
		}
	}
	
	public void confirmCard() {
		System.out.print(this.thread.getName() + "");
		System.out.println(this.card.getNumber() + " - " + this.card.getMonth() + "/" + this.card.getYear() + " - " + this.card.getCvv());
	}

	// iterates through each item and calls addToCart on each
	public void cartItems() {
		for (Item item : this.order) {
			addToCart(item);
			try { 
				Thread.sleep(this.cartDelay); 
			} catch (Exception e) { 
				// handle!!! 
			}
		}
	}

	// navigate to URL, select size, add to cart
	private void addToCart(Item item) {
		String targetLink = this.links.eq(item.getNumber()).attr("abs:href");
		if (targetLink != null ) {
			this.newTab();
			System.out.print(this.thread.getName() + " ");;
			System.out.println("Navigating to: " + targetLink);
			this.driver.get(targetLink);

			if (item.getSize() != null) {
				this.selectSize(item.getSize());
			}
			this.clickAddToCart();
		}
	}

	// open and switch to new tab
	private void newTab() {
		((JavascriptExecutor) this.driver).executeScript("window.open('','_blank');");
		ArrayList<String> tabs = new ArrayList<String> (this.driver.getWindowHandles());
		this.driver.switchTo().window(tabs.get(tabs.size()-1));
	}

	// select item size
	private void selectSize(String size) {
		try {
			Select sizeSelect = new Select(this.driver.findElement(By.id("s")));
			System.out.print(this.thread.getName() + " ");;
			System.out.println(this.driver.getTitle() + " // Selected size --> " + size);
			sizeSelect.selectByVisibleText(size);
		}
		catch (Exception e) {
			System.out.println(this.driver.getTitle() + " // COULD NOT SELECT SIZE BY ID***");
			System.out.print(this.thread.getName() + " ");;
			System.out.println("Attempting to select size by tagname...");
			try {
				Select sizeSelect = new Select(this.driver.findElement(By.tagName("select")));
				System.out.print(this.thread.getName() + " ");;
				System.out.println(this.driver.getTitle() + " // Selected size --> " + size);
				sizeSelect.selectByVisibleText(size);
			} catch (Exception e2) {
				System.out.print(this.thread.getName() + " ");;
				System.out.println(this.driver.getTitle() + " // COULD NOT SELECT SIZE BY TAGNAME GG***");
			}
		}
	}

	// click add to cart button
	private void clickAddToCart() {
		try {
			WebElement addToCartButton = this.driver.findElement(By.xpath("//input[@value='add to cart']"));
			addToCartButton.sendKeys(Keys.ENTER);
			System.out.print(this.thread.getName() + " ");;
			System.out.println(this.driver.getTitle() + " // Successfully carted.");
		} catch (Exception e) {
			System.out.print(this.thread.getName() + " ");;
			System.out.println(this.driver.getTitle() + " // COULD NOT CART ITEM***");
			System.out.print(this.thread.getName() + " ");;
			System.out.println("Attemping to cart item again...");
			try {
				WebElement addToCartButton = this.driver.findElement(By.name("commit"));
				addToCartButton.sendKeys(Keys.ENTER);
				System.out.print(this.thread.getName() + " ");;
				System.out.println(this.driver.getTitle() + " // Successfully carted.");
			} catch (Exception e2) {
				System.out.print(this.thread.getName() + " ");;
				System.out.println(this.driver.getTitle() + " // COULD NOT CART ITEM AGAIN GG***");
			}
		}
	}

	// navigate to checkout page
	private void checkout() {
		try { 
			Thread.sleep(this.cartDelay); 
			this.newTab();
			System.out.print(this.thread.getName() + " ");;
			System.out.println("Navigating to checkout...");
			this.driver.get("http://www.su" + "pr" + "em" + "en" + "ew" + "yo" + "rk.com" + "/chec" + "kout");
			Elements paymentElements = null;
			try {
				this.sendPaymentDetails(paymentElements);
				this.checkTerms();
				this.clickProcessPayment();
			} catch (exception E) {
				
			}
			
		} catch (Exception e) { 
			// handle!!! 
		} 
	}
	
	private void sendPaymentDetails(Elements paymentElements) {
		this.sendKeyWithFallback();
		this.selectWithFallback();
		this.selectWithFallback();
		this.sendKeyWithFallback();
	}
	
	private void sendKeyWithFallback(String id, String path, Element element, String keys) {
		try {
			
		} catch (Exception e) {
			try {
				
			} catch (Exception e2) {
				try {
					
				} catch (Exception e3) {
					
				}
			}
		}
	}
	
	private void selectWithFallback(String id, String path, Element element, String target) {
		
	}
	
	private void checkTerms() {
		try {
			
		} catch (Exception e) {
			try {
				
			} catch (Exception e2) {
				try {
					
				} catch (Exception e3) {
					
				}
			}
		}
	}
	
	private void clickProcessPayment() {
		try {
			
		} catch (Exception e) {
			try {
				
			} catch (Exception e2) {
				try {
					
				} catch (Exception e3) {
					
				}
			}
		}
	}
	
	public void test() {
		try {
			this.cartItems();
			this.checkout();
//			this.driver.get("https://www.facebook.com");
//			this.newTab();
//			this.driver.get("https://www.instagram.com");
//			this.driver.quit();
		} catch (Exception e) {
			// handle!!! 
		}
		long endTime = System.nanoTime();
		double elapsedTime = (double)(endTime - startTime)/1000000000.00;
		this.elapsedTime = elapsedTime;
		
		System.out.print(this.thread.getName() + " ");;
		System.out.println(elapsedTime + " seconds.");
		return;
	}
	
	public void run() {
		try {
			this.cartItems();
			this.checkout();
		} catch (Exception e) {
			// handle!!!
		}
		long endTime = System.nanoTime();
		double elapsedTime = (double)(endTime - startTime)/1000000000.00;

		System.out.print(this.thread.getName() + " ");;
		System.out.println(elapsedTime + " seconds.");
		return;
	}
}