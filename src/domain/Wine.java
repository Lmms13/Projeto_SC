/*
Grupo 31
Luis Santos 56341
Pedro Pinto 56369
Daniel Marques 56379
*/
package domain;

import java.util.ArrayList;
import java.util.List;

public class Wine {
	private String id;
	private String image;
	private double rating;
	private int rating_count;
	private int stock;
	private List<Seller> sellers = new ArrayList<Seller>();
	
	public Wine(String id, String image) {
		this.id = id;
		this.image = image;
		this.rating = 0;
		this.rating_count = 0;
		this.stock = 0;
		this.sellers = new ArrayList<Seller>();
	}

	public String getId() {
		return this.id;
	}

	public String getImage() {
		return this.image;
	}

	public int getRating() {
		return (int) this.rating;
	}
	
	public void setRating(int rating, int rating_count) {
		this.rating = rating;
		this.rating_count = rating_count;
	}

	public void updateRating(int n) {
		rating_count++;
		rating = (rating * (rating_count - 1) + n) / (double) rating_count;
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
		return this.sellers;
	}
	
	public void addSeller(String userId, int price, int amount) {
		this.stock += amount;
		this.sellers.add(new Seller(userId, price, amount)); 
	}
	
	public void loadSeller(String userId, int price, int amount) {
		this.sellers.add(new Seller(userId, price, amount)); 
	}
	
	public Seller getSeller(String userId){
		for(Seller s: this.sellers) {
			if(s.getId().equals(userId)) {
				return s;
			}
		}
		return null;
	}
	
	public String toString() {
		return this.id + ":" + this.image + ":" + (int) this.rating + "/" + this.rating_count + ":" + this.stock; 
	}
	
	public String sellersToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("");
		for(Seller s: this.sellers) {
			sb.append(this.sellerToString(s.getId()) + System.getProperty("line.separator"));
		}
		return sb.toString();
	}
	
	public String displaySellers() {
		StringBuilder sb = new StringBuilder();
		sb.append("");
		for(Seller s: this.sellers) {
			sb.append("Utilizador: " + (s.getId() + System.getProperty("line.separator")));
			sb.append("Preco: " + (s.getPrice() + System.getProperty("line.separator")));
			sb.append("Quantidade: " + (s.getAmount() + System.getProperty("line.separator")));
			sb.append(System.getProperty("line.separator"));
		}
		return sb.toString();
	}
	
	public String sellerToString(String userId) {
		for(Seller s: this.sellers) {
			if(s.getId().equals(userId)) {
				return id + ":" + s.getId() + ":" + s.getPrice() + ":" + s.getAmount();
			}
		}
		return "";
	}
	
	public boolean sellerExists(String userId) {
		for(Seller s: this.sellers) {
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
	
	public boolean sellersAvailable() {
		return this.sellers.size() > 0;
	}
}
	
