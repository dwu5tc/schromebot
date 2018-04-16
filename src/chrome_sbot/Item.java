package chrome_sbot;

//import java.util.ArrayList;
//import java.util.List;

public class Item {
	
	private int number;
	private String size;
	
//	private List<String> keywords = new ArrayList<String>();

	public Item() {}
	
//	public Item(int number, String size) {
//		this.number = number;
//		this.size = size;
//	}
	
	public void setNumber(int number) { this.number = number; }
	
	public void setSize(String size) { this.size = size; }
	
//	public void setKeywords(List<String> keywords) { this.keywords = keywords; }
	
	public int getNumber() { return this.number; }
	
	public String getSize() { return this.size; }
	
//	public List<String> getKeywords() { return this.keywords; }
	
	public boolean isValid() {
		return this.number > 0;
	}
	
	public static void main(String[] args) {
		Item item = new Item();
		System.out.println(item.isValid());
	}
}

