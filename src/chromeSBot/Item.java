package chromeSBot;

public class Item {
	
	private int number = -1;
	private String size;
	private String url;
	
	public Item() {}
	
	public void setNumber(int number) { this.number = number; }
	
	public void setSize(String size) { this.size = size; }
	
	public void setUrl(String url) { this.url = url; }
	
	public int getNumber() { return this.number; }
	
	public String getSize() { return this.size; }
	
	public String getUrl() { return this.url; }
	
	public void printItem() 
	{
		System.out.println("Number: " + this.number);
		System.out.println("Size: " + this.size);
	}
	
	public boolean isValid()
	{
		return !(this.number == -1);
	}
}

