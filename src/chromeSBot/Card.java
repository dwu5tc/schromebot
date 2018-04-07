package chromeSBot;

public class Card {
	
	private String number;
	private String month;
	private String year;
	private String cvv;
	
	public Card() {}
	
	public void setNumber(String number) { this.number = number; }
	
	public void setMonth(String month) { this.month = month; }
	
	public void setYear(String year) { this.year = year; }
	
	public void setCvv(String cvv) { this.cvv = cvv; }
	
	public String getNumber() { return this.number; }
	
	public String getMonth() { return this.month; }
	
	public String getYear() { return this.year; }
	
	public String getCvv() { return this.cvv; }
}
