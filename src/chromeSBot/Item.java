package chromeSBot;

// import java.lang.reflect.Field;

public class Item {
	
	private String type;
	private int number = -1;
	private String name;
	private String colour;
	private String size;
	
	public Item() {}
	
	public Item(String type)
	{
		this.type = type;
	}
	
	// construct with name
	public Item(String type, String name, 
			String colour, String size) 
	{
		this.type = type;
		this.name = name;
		this.colour = colour;
		this.size = size;
	}
	
	// construct with number
	public Item(String type, int number, 
			String colour, String size) 
	{
		this.type = type;
		this.number = number;
		this.colour = colour;
		this.size = size;
	}
	
	public void setType(String type) { this.type = type; }
	
	public void setNumber(int number) { this.number = number; }
	
	public void setName(String name) { this.name = name; }
	
	public void setColour(String colour) { this.colour = colour; }
	
	public void setSize(String size) { this.size = size; }
	
	public String getType() { return this.type;	}
	
	public int getNumber() { return this.number; }
	
	public String getName() { return this.name; }
	
	public String getColour() { return this.colour; }
	
	public String getSize() { return this.size; }
	
	public void printItem() 
	{
		System.out.println("Name: " + this.name);
		System.out.println("Number: " + this.number);
		System.out.println("Type: " + this.type);
		System.out.println("Colour: " + this.colour);
		System.out.println("Size: " + this.size);
	}
	
	public boolean isValid()
	{
		return !(this.name == null && this.number == -1);
	}
	
	// useless function 
//	public int numOfNullFields()
//	{
//		Field fields[] = this.getClass().getDeclaredFields();
//		int nullFields = 0;
//		for (Field f : fields)
//		{
//			try
//			{
//				Object value = f.get(this);
//				if (value == null) { nullFields++; }
//			}
//			catch (Exception e) { e.printStackTrace(); }
//		}
//		return nullFields;
//	}

	// for testing
//	public static void main(String[] args) {
//		Item dummy = new Item();
//		dummy.setSize("lol");
////		dummy.setNumber(5);
//		dummy.printItem();
//		System.out.println(dummy.isValid());
//	}
}

