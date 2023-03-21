/*
Grupo 31
Luis Santos 56341
Pedro Pinto 56369
Daniel Marques 56379
*/
package domain.catalogs;

import java.util.ArrayList;
import java.util.List;

import domain.Wine;

public class WineCatalog {
	
	private static List<Wine> wineCatalog;
	
	public WineCatalog() {
		wineCatalog = new ArrayList<Wine>();
	}
	
	public void addWine(String id, String image) {
		wineCatalog.add(new Wine(id, image));
	}
	
	public void loadWine(String id, String image, int rating, int rating_count, int stock) {
		Wine w = new Wine(id, image);
		wineCatalog.add(w);
		w.setRating(rating, rating_count);
		w.addStock(stock);
	}
	
	public boolean wineExists(String id) {
		for (Wine w : wineCatalog) {
			if (id.equals(w.getId())) {
				return true;
			}
		}
		return false;
	}
	
	public Wine getWine(String id) {
		for (Wine w : wineCatalog) {
			if (id.equals(w.getId())) {
				return w;
			}
		}
		return null;
	}

}
