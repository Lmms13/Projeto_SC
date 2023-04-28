/*
Grupo 31
Luis Santos 56341
Pedro Pinto 56369
Daniel Marques 56379
*/
package server.blockchain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Block implements Serializable {
	
	private static final long serialVersionUID = 6529685098267757690L;
	byte[] prev_hash;
	private String prev_hash_string;
	private long id;
	private long n_trx;
	private List<String> transactions;
	private byte[] byteArray;
	private byte[] signature;
	
	public Block(long id, byte[] prev_hash, String trx) {
		this.id = id;
		this.n_trx = 1;
		if(prev_hash != null) {
			this.prev_hash = prev_hash;
			this.prev_hash_string = Base64.getEncoder().encodeToString(this.prev_hash);
		}
		else {
			this.prev_hash = "00000000".getBytes();
			this.prev_hash_string = "00000000";
		}
		this.transactions = new ArrayList<String>();
		this.transactions.add(trx);
		this.signature = null;
	}
	
	public String toString() {
		return this.prev_hash_string + System.getProperty("line.separator")
		+ "blk_id = " + this.id + System.getProperty("line.separator")
		+ "n_trx = " +  this.n_trx + System.getProperty("line.separator")
		+ this.transactionsToString();	
	}
	
	public String transactionsToString() {
		StringBuilder sb = new StringBuilder();
		for(String t : this.transactions) {
			sb.append(t + System.getProperty("line.separator"));
		}
		return sb.toString();
	}
	
	public void addTransaction(String trx) {
		this.transactions.add(trx);
		this.n_trx++;
	}
	
	public boolean isFull() {
		return this.n_trx >= 5;
	}

	public long getBlockId() {
		return this.id;
	}
	
	public byte[] getPreviousHash() {
		return this.prev_hash;
	}
	
	public void setSignature(byte[] mac) {
		this.signature = mac;
	}
	
	public byte[] getSignature() {
		return this.signature;
	}
	
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this);
		oos.flush();
		byte[] data = bos.toByteArray();
		return data;
	}
	
	public void setByteArray(byte[] data) {
		this.byteArray = data;
	}
	
	public byte[] getByteArray() {
		return this.byteArray;
	}
	
	
	
	
}
