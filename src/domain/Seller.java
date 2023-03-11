package domain;

public class Seller {
	private String userId; 
	private int price; 
	private int amount;
	
	public Seller(String userId, int price, int amount) {
		this.userId = userId;
		this.price = price;
		this.amount = amount;
	}

	public String getId() {
		return userId;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
	
}
