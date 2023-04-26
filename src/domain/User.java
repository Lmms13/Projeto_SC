/*
Grupo 31
Luis Santos 56341
Pedro Pinto 56369
Daniel Marques 56379
*/
package domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User {
	private String userId;
	private String password;
	private int balance;
	private HashMap<String, List<String>> inbox;

	public User(String id, String password) {
		this.userId = id;
		this.password = password;
		this.balance = 200;
		this.inbox = new HashMap<String, List<String>>();
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
	
	public void setBalance(int n) {
		this.balance = n;
	}
	
	public void addMessage(String sender, String message) {	
		if(this.inbox.containsKey(sender)) {
			this.inbox.get(sender).add(message);
		}
		else {
			List<String> messages = new ArrayList<String>();
			messages.add(message);
			this.inbox.put(sender, messages);
		}
	}
	
	public void loadMessages(String sender, String allMessages) {	
		String[] splitMessages = allMessages.split("%%%%%");
		if(this.inbox.containsKey(sender)) {
			for(String m: splitMessages) {
				this.inbox.get(sender).add(m);
			}
		}
		else {
			List<String> messages = new ArrayList<String>();
			for(String m: splitMessages) {
				messages.add(m);
			}
			this.inbox.put(sender, messages);
		}
	}
	
	public String displayMessages() {
		StringBuilder sb = new StringBuilder();
		for(HashMap.Entry<String, List<String>> entry: this.inbox.entrySet()) {
			sb.append("---" + entry.getKey() + "---" + System.getProperty("line.separator"));
			for(String s: entry.getValue()) {
				sb.append(s + System.getProperty("line.separator"));
			}
			sb.append(System.getProperty("line.separator"));
		}
		return sb.toString();
	}
	
	public boolean hasSender(String id) {
		return this.inbox.containsKey(id);
	}
	
	public void clearMessages() {
		this.inbox.clear();
	}
	
	public boolean hasMessages() {
		return this.inbox.size() > 0;
	}
	
	public void clearPassword() {
		this.password = null;
	}
	
	public String getMessagesFromSender(String sender) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.userId + ":::::" + sender + ":::::");
		List<String> messages = inbox.get(sender);
		for(String message : messages) {
			sb.append(message + "%%%%%");
		}
		sb.delete(sb.length() - 5, sb.length());
		return sb.toString();
	}
}
