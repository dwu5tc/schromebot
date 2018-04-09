package chrome_sbot;

public class Item {
	
	private int number;
	private String size;
	private String url;
	
	public Item() {}
	
	public void setNumber(int number) { this.number = number; }
	
	public void setSize(String size) { this.size = size; }
	
	public void setUrl(String url) { this.url = url; }
	
	public int getNumber() { return this.number; }
	
	public String getSize() { return this.size; }
	
	public String getUrl() { return this.url; }
	
	public boolean isValid() {
		return this.number > 0;
	}
	
	public static void main(String[] args) {
		Item item = new Item();
		System.out.println(item.isValid());
	}
}

