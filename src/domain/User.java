package domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User {
	private String userId;
	private String password;
	private int balance;
	private List<List<String>> wines;
	
	// HashMap<String, Wine> wines;
	// HashMap<String, String[]> messages;

	public User(String id, String password) {
		this.userId = id;
		this.password = password;
		balance = 200;
		this.wines = new ArrayList<List<String>>();
		// wines = new HashMap<String, Wine>();
		// messages = new HashMap<String, String[]>();
	}

	public String getId() {
		return this.userId;
	}

	public String getPassword() {
		return this.password;
	}
	
	public int getBalance() {
		return this.balance;
	}
	
	public String toString() {
		return this.userId + ":" + this.password;
	}
	
	public void addWine(String id, int price, int amount) {
		List<String> wine = new ArrayList<String>();
		wine.add(id);
		wine.add(Integer.toString(price));
		wine.add(Integer.toString(amount));
		wines.add(wine);
	}
	
	
}
