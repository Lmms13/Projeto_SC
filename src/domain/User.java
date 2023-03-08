package domain;

import java.util.HashMap;

public class User {
	private String userID;
	private String password;
	private  int balance;
	// HashMap<String, Wine> wines;
	// HashMap<String, String[]> messages;

	public User(String id, String password) {
		this.userID = id;
		this.password = password;
		balance = 200;
		// wines = new HashMap<String, Wine>();
		// messages = new HashMap<String, String[]>();
	}

	public String getID() {
		return this.userID;
	}

	public String getPassword() {
		return this.password;
	}
	
	public int getBalance() {
		return this.balance;
	}
	
	public String toString() {
		return this.userID + ":" + this.password;
	}
	
	
}
