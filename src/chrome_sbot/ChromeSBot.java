package chrome_sbot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Proxy;
//import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import org.openqa.selenium.JavascriptExecutor;

public class ChromeSBot implements Runnable
{
	private Thread thread;
	private WebDriver driver;
	
	private int cartDelay;
	private String profile;
	private String proxy;
	
	private int checkoutDelay;
	private boolean autoCheckTerms = false;
	private boolean autoProcessPayment = false;
	
	private List<Item> order = new ArrayList<Item>();
	private Card card = new Card();
	
	private Elements links;
	
	private double startTime; // or should it be null?
//	private double atcTime;
//	private double checkoutTime;
	
	public ChromeSBot(int cartDelay, int checkoutDelay, String profile, JSONArray order, JSONObject card) {
		this.thread = new Thread(this, "[" + cartDelay + " " + checkoutDelay + " " + profile + "]");
		this.cartDelay = cartDelay;
		this.checkoutDelay = checkoutDelay;
		this.profile = profile;
		this.buildOrderFromJson(order);
		this.buildCardFromJson(card);
		this.confirmOrder();
		this.confirmCard();
	}
	
	public void setProxy(String proxy) {
		this.proxy = proxy;
	}
	
	public void setCheckTerms(boolean bool) {
		this.autoCheckTerms = bool;
	}
	
	public void setProcessPayment(boolean bool) {
		this.autoProcessPayment = bool;
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
	
	public void setOrder(List<Item> order) {
		this.order = order;
	}
	
	public void setLinks(Elements links) {
		this.links = links;
	}
	
	public Thread getThread() {
		return this.thread;
	}
	
	public void initChrome() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("win") > -1) {
			System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
		} else {
			System.setProperty("webdriver.chrome.driver", "chromedriver");
		} 
		
		ChromeOptions options = new ChromeOptions();
		if (this.profile != null) {
			options.addArguments("user-data-dir=" + profile);
		}	
		HashMap<String, Object> images = new HashMap<String, Object>();
		images.put("images", 1);
		HashMap<String, Object> prefs = new HashMap<String, Object>();
		prefs.put("profile.default_content_setting_values", images);
		options.setExperimentalOption("prefs", prefs);
		if (this.proxy != null) {
			System.out.println("SETTING PROXY TO " + this.proxy);
			Proxy proxy = new Proxy();
			proxy.setHttpProxy(this.proxy);
			options.setCapability("proxy", proxy);
		}
		options.addArguments("disable-infobars");
		this.driver = new ChromeDriver(options);
	}
	
	// build order from JSONArray
	private void buildOrderFromJson(JSONArray jsonOrder) {
		for (int i = 0; i < jsonOrder.length(); i++) {
			JSONObject jsonItem = jsonOrder.getJSONObject(i);
			Item item = new Item();
			if (!jsonItem.isNull("size") && !jsonItem.getString("size").equals("")) {
				item.setSize(jsonItem.getString("size"));
			}
			if (!jsonItem.isNull("number") && jsonItem.getInt("number") > -1) {
				item.setNumber(jsonItem.getInt("number"));
				this.order.add(item); // only add item if number field not empty
				System.out.println("Item " + i + " is valid. Appended to order");
			} else {
				System.out.println("INVALID NUMBER FIELD. ITEM " + i + " IGNORED***");
			}
		}
	}
	
	// build card from JSONObject
	private void buildCardFromJson(JSONObject jsonCard) {
		if (!jsonCard.isNull("number") && !jsonCard.getString("number").equals("")) {
			this.card.setNumber(jsonCard.getString("number"));			
		}
		if (!jsonCard.isNull("month") && !jsonCard.getString("month").equals("")) {	
			this.card.setMonth(jsonCard.getString("month"));
		}
		if (!jsonCard.isNull("year") && !jsonCard.getString("year").equals("")) {
			this.card.setYear(jsonCard.getString("year"));
		}
		if (!jsonCard.isNull("cvv") && !jsonCard.getString("cvv").equals("")) {
			this.card.setCvv(jsonCard.getString("cvv"));
		}
	}

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
		System.out.print(this.thread.getName() + " ");
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
		this.driver.switchTo().window(tabs.get(tabs.size() - 1));
	}

	// select item size
	private void selectSize(String size) {
		String thread = this.thread.getName();
		String title = this.driver.getTitle();
		try {
			Select sizeSelect = new Select(this.driver.findElement(By.id("s")));
			sizeSelect.selectByVisibleText(size);
			System.out.println(thread + " " + title + " // Selected size --> " + size + " (1 - id)");
		}
		catch (Exception e) {
			System.out.println(thread + " " + title + " // COULD NOT SELECT SIZE " + size + " (1 - ID)***");
			try {
				Select sizeSelect = new Select(this.driver.findElement(By.tagName("select")));
				sizeSelect.selectByVisibleText(size);
				System.out.println(thread + " " + title + " // Selected size --> " + size + " (2 - tag)");
			} catch (Exception e2) {
				System.out.println(thread + " " + title + " // COULD NOT SELECT SIZE " + size + " (2 - TAG)***");
			}
		}
	}

	// click add to cart button
	private void clickAddToCart() {
		String thread = this.thread.getName();
		String title = this.driver.getTitle();
		// implement with element and indexes???
		try {
			WebElement addToCartButton = this.driver.findElement(By.xpath("//input[@value='add to cart']"));
			addToCartButton.sendKeys(Keys.ENTER);
			System.out.println(thread + " " + title + " // Successfully carted (1 - xpath)");
		} catch (Exception e) {
			System.out.println(thread + " " + title + " // COULD NOT CART (1 - XPATH)***");
			try {
				WebElement addToCartButton = this.driver.findElement(By.name("commit"));
				addToCartButton.sendKeys(Keys.ENTER);
				System.out.println(thread + " " + title + " // Successfully carted (2 - name)");
				try {
					// implement another fallback?
					// a href="shop/"
				} catch (Exception e3) {
					
				}
			} catch (Exception e2) {
				System.out.println(thread + " " + title + " // COULD NOT CART (2 - NAME)***");
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
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println(this.driver.getPageSource());
			System.out.println();
			System.out.println();
			if (this.card.getNumber() != null || this.card.getMonth() != null || this.card.getYear() != null || this.card.getCvv() != null) {
				try {
					List<WebElement> fieldsets = this.driver.findElements(By.tagName("fieldset"));
					// get first fieldset which holds the cc form
					WebElement form = fieldsets.get(1);
					System.out.println("fieldsets " + fieldsets.size());
					if (this.card.getNumber() != null || this.card.getCvv() != null) {
						try {
							List<WebElement> inputs = form.findElements(By.tagName("input"));
							System.out.println("input elements " + inputs.size());
							if (this.card.getNumber() != null) {
								this.sendKeyWithIndex("nnaerb", inputs, 0, this.card.getNumber());
							}
							if (this.card.getCvv() != null) {
								this.sendKeyWithIndex("orcer", inputs, 1, this.card.getCvv());
							}
						} catch (Exception e) {
							System.out.println("COULD NOT FIND INPUT ELEMENTS***");
						}
					}
					
					if (this.card.getMonth() != null || this.card.getYear() != null) {
						try {
							List<WebElement> selects = form.findElements(By.tagName("select"));
							System.out.println("select elements " + selects.size());
							if (this.card.getMonth() != null) {
								this.selectWithIndex("credit_card_month", selects, 0, this.card.getMonth());
							}
							if (this.card.getYear() != null) {
								this.selectWithIndex("credit_card_year", selects, 1, this.card.getYear());
							}
						} catch (Exception e) {
							System.out.println("COULD NOT FIND SELECT ELEMENTS***");
						}
					}
				} catch (Exception e) {
				
				}
			}
			if (this.autoCheckTerms == true) {
//				this.checkTerms();
			}
			if (this.autoProcessPayment == true) {
				Thread.sleep(this.checkoutDelay);
//				this.clickProcessPayment();					
			}
			return;
		} catch (Exception e) { 
			// handle!!! 
		} 
	}
	
	private void sendKeyWithIndex(String id, List<WebElement> elements, int index, String value) {
		String thread = this.thread.getName();
		try {
			WebElement input = elements.get(index);
			input.sendKeys(value);
			System.out.println(thread + " // Autofilled " + id + " --> " + value + " (2 - index)");
		} catch (Exception e) {
			System.out.println(thread + " // COULD NOT AUTOFILL " + id + " (2 - INDEX)***");
		}
	}
	
	private void sendKeyWithFallback(String id, List<WebElement> elements, int index, String value) {
		String thread = this.thread.getName();
		try {
			WebElement input = this.driver.findElement(By.id(id));
			input.sendKeys(value);
			System.out.println(thread + " // Autofilled " + id + " --> " + value + " (1 - id)");
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println(thread + " // COULD NOT AUTOFILL " + id + " (1 - ID)***");
			this.sendKeyWithIndex(id, elements, index, value);
		}
		return;
	}
	
	private void selectWithIndex(String id, List<WebElement> elements, int index, String value) {
		String thread = this.thread.getName();
		try {
			Select select = new Select(elements.get(index));
			System.out.println(thread + " Autofilled " + id + " --> " + value + " (2 - index)");
			select.selectByVisibleText(value);
		} catch (Exception e) {
			System.out.println(thread + " COULD NOT AUTOFILL " + id + " (2 - INDEX)***");
		}
	}
	
	private void selectWithFallback(String id, List<WebElement> elements, int index, String value) {
		String thread = this.thread.getName();
		try {
			System.out.println("Attemping by id " + id);
			System.out.println(elements.size());
			for (WebElement elem : elements) {
				System.out.println("SELECT ELEMENT ID " + elem.getAttribute("id") + " SELECT ELEMENT TEXT" + elem.getText());
			}
			System.out.println("IDIDIDIDIDI " + id);
			WebElement element = this.driver.findElement(By.id(id));
			System.out.println("LKJGLKAJSGLAKSJLG " + element.getAttribute("id"));
			Select select = new Select(element);
//			Select select = new Select(this.driver.findElement(By.id(id)));
			System.out.println(thread + " Autofilled " + id + " --> " + value + " (1 - id)");
			select.selectByVisibleText(value);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println(thread + " COULD NOT AUTOFILL " + id + " (1 - ID)***");
			this.selectWithIndex(id, elements, index, value);
		}
		return;
	}
//	
//	private void checkTerms() {
//		try {
//			
//		} catch (Exception e) {
//			try {
//				
//			} catch (Exception e2) {

//			}
//		}
//		return;
//	}
//	
//	private void clickProcessPayment() {
//		try {
//			
//		} catch (Exception e) {
//			try {
//				
//			} catch (Exception e2) {

//			}
//		}
//		return;
//	}
	
//	public void test() {
//		return;
//	}
	
	public void run() {
		try {
			this.cartItems();
			
			long endTime = System.nanoTime();
			double atcTime = (double)(endTime - this.startTime)/1000000000.00;
			
			this.checkout();
			
			endTime = System.nanoTime();
			double checkoutTime = (double)(endTime - this.startTime + atcTime)/1000000000.00;
			double totalTime = atcTime + checkoutTime;
			
			System.out.println(this.thread.getName() + " ATC:" + atcTime + " // Checkout: " + checkoutTime + " // Total: " + totalTime);
		} catch (Exception e) {
			// handle!!!
		}
		return;
	}
	
//	public String toString() {
//		return this.cartDelay + " " + this.checkoutDelay + " " + this.profile + " " + this.proxy + " " + this.blockImages;
//		// print order and items???
//	}
}