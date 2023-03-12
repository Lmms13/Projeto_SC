package domain;

import java.util.ArrayList;
import java.util.HashMap;

public class User {
	private String userId;
	private String password;
	private int balance;
	private HashMap<String, ArrayList<String>> messages;

	public User(String id, String password) {
		this.userId = id;
		this.password = password;
		this.balance = 200;
		this.messages = new HashMap<String, ArrayList<String>>();
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

	public void addBalance(int n) {
		this.balance += n;
	}

	public void subtractBalance(int n) {
		this.balance -= n;
	}

	public void addMessage(String sender, String message) {
		if (messages.containsKey(sender)) {
			messages.get(sender).add(message);
		} else {
			ArrayList<String> messageArray = new ArrayList<String>();
			messageArray.add(message);
			messages.put(sender, messageArray);
		}
	}

	public HashMap<String, ArrayList<String>> getMessages() {
		HashMap<String, ArrayList<String>> allMessages = messages;
		messages = new HashMap<String, ArrayList<String>>();
		return allMessages;
	}
}
