/*
Grupo 31
Luis Santos 56341
Pedro Pinto 56369
Daniel Marques 56379
*/
package domain.catalogs;

import java.util.ArrayList;
import java.util.List;

import domain.User;

public class UserCatalog {

	private static List<User> userCatalog;

	public UserCatalog() {
		userCatalog = new ArrayList<User>();
	}

	public void addUser(String id, String password) {
		userCatalog.add(new User(id, password));
	}

	public boolean userExists(String id) {
		for (User u : userCatalog) {
			if (id.equals(u.getId())) {
				return true;
			}
		}
		return false;
	}

	public boolean checkPassword(String id, String password) {
		for (User u : userCatalog) {
			if (id.equals(u.getId())) {
				if (password.equals(u.getPassword())) {
					return true;
				}
			}
		}
		return false;
	}

	public User getUser(String id) {
		for (User u : userCatalog) {
			if (id.equals(u.getId())) {
				return u;
			}
		}
		return null;
	}
	
	public List<User> getList(){
		return userCatalog;
	}
}
