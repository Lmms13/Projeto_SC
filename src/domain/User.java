package domain;

import java.util.HashMap;

public class User {
	String userID;
	String password;
	int balance;
	// HashMap<String, Wine> wines;
	// HashMap<String, String[]> messages;

	public User(String id, String password) {
		this.userID = id;
		this.password = password;
		balance = 200;
		// wines = new HashMap<String, Wine>();
		// messages = new HashMap<String, String[]>();
	}

	public Object getID() {
		return this.userID;
	}

	public Object getPassword() {
		return this.password;
	}
}
