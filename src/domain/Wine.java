package domain;

import java.util.ArrayList;
import java.util.List;

public class Wine {
	private String id;
	private String image;
	private int rating;
	private int rating_count;
	private int stock;
	private static List<Seller> sellers = new ArrayList<Seller>();
	
	public Wine(String id, String image) {
		this.id = id;
		this.image = image;
		this.rating = 0;
		this.rating_count = 0;
		this.stock = 0;
		sellers = new ArrayList<Seller>();
	}

	public String getId() {
		return this.id;
	}

	public String getImage() {
		return this.image;
	}

	public int getRating() {
		return this.rating;
	}
	
	public void setRating(int rating, int rating_count) {
		this.rating = rating;
		this.rating_count = rating_count;
	}

	public void updateRating(int n) {
		rating_count++;
		rating = (rating + n) / rating_count;
	}

	public int getStock() {
		return this.stock;
	}

	public void addStock(int n) {
		this.stock += n;
	}
	
	public void subtractStock(int n) {
		this.stock -= n;
	}
	
	public List<Seller> getSellers(){
		return sellers;
	}
	
	public void addSeller(String userId, int price, int amount) {
		this.stock += amount;
		sellers.add(new Seller(userId, price, amount)); 
//		seller.add(userId);
//		seller.add(Integer.toString(price));
//		seller.add(Integer.toString(amount));
//		sellers.add(seller);
	}
	
	public Seller getSeller(String userId){
		for(Seller s: sellers) {
			if(s.getId().equals(userId)) {
				return s;
			}
		}
		return null;
	}
	
	public String toString() {
		return this.id + ":" + this.image + ":" + this.rating + "/" + this.rating_count + ":" + this.stock; 
	}
	
	public String sellersToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("");
		for(Seller s: sellers) {
			sb.append(this.sellerToString(s.getId()) + System.getProperty("line.separator"));
		}
		return sb.toString();
	}
	
	public String sellerToString(String userId) {
		for(Seller s: sellers) {
			if(s.getId().equals(userId)) {
				return id + ":" + s.getId() + ":" + s.getPrice() + ":" + s.getAmount();
			}
		}
		return "";
	}
	
	public boolean sellerExists(String userId) {
		for(Seller s: sellers) {
			if(s.getId().equals(userId)) {
				return true;
			}
		}
		return false;
	}
	
	public void updateSeller(String userId, int price, int amount) {
		Seller s = this.getSeller(userId);
		s.setPrice(price);
		s.setAmount(amount + s.getAmount());
		this.addStock(amount); 
	}
}
	
