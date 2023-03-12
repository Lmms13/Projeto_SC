package domain;

public class User {
	private String userId;
	private String password;
	private int balance;

	public User(String id, String password) {
		this.userId = id;
		this.password = password;
		this.balance = 200;
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
}
