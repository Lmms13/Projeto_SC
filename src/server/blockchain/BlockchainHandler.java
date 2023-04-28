/*
Grupo 31
Luis Santos 56341
Pedro Pinto 56369
Daniel Marques 56379
*/
package server.blockchain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

public class BlockchainHandler {
	private String blockchainPath = "./src/server/blockchain/";
	private List<Block> blockchain;
	private Block currBlock;
	private MessageDigest currHash;
	
	public BlockchainHandler() {
		this.blockchain = new ArrayList<Block>();
		this.currBlock = null; 
		this.currHash= null; 
	}
	
	public void newTransaction(String trx) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, InvalidKeyException, IllegalStateException, ClassNotFoundException {
		if (currBlock == null) {
			if(blockchain.size() == 0) {
				currBlock = new Block(1L, null , trx);
				blockchain.add(currBlock);				
			}
			else {
				currBlock = blockchain.get(blockchain.size() - 1);
				newTransaction(trx);
			}
		}
		else if(currBlock.isFull()) {
			this.currHash = this.getHashOfBlock(currBlock);
			long id = currBlock.getBlockId() + 1;
			currBlock = new Block(id, this.currHash.digest(), trx);
			blockchain.add(currBlock);
		}
		else {
			currBlock.addTransaction(trx);
		}
		this.writeBlockFile(currBlock);	
	}
	
	public File getFileOfBlock(Block b) {
		return new File(blockchainPath + "block_" + Long.toString(b.getBlockId()) + ".blk");
	}

	public MessageDigest getHashOfBlock(Block b) {
		MessageDigest digest = null;
		try {
			File f = getFileOfBlock(b);
			digest = MessageDigest.getInstance("SHA-256");
			FileInputStream fis = new FileInputStream(f);
			int count;
			byte[] buf = new byte[1024];
			while ((count = fis.read(buf)) > 0) {
				digest.update(buf, 0, count);
			}
			fis.close();
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return digest;
	}
	
	public void writeBlockFile(Block b) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, InvalidKeyException, IllegalStateException, ClassNotFoundException {
		File f = getFileOfBlock(b);
		try {
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			Mac mac = Mac.getInstance("HmacSHA256");
			KeyStore keystore = KeyStore.getInstance("JCEKS");
			FileInputStream keystore_fis = new FileInputStream(blockchainPath + "server.secret");
			keystore.load(keystore_fis, "aulelas.keystore".toCharArray());
			SecretKey key = (SecretKey) keystore.getKey("secServer", "aulelas.keystore".toCharArray());
			mac.init(key);
			
			oos.writeObject(b);
			mac.update(b.toByteArray());
			oos.writeObject(mac.doFinal());

			oos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Block getBlock(int i) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(blockchainPath + "block_" + i + ".blk");
		ObjectInputStream ois = new ObjectInputStream(fis);
		Block b = (Block) ois.readObject();
		ois.close();
		fis.close();
		return b;
	}
	
	public boolean checkIntegrity() throws NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, InvalidKeyException, ClassNotFoundException, IOException {
		try {
			this.getBlock(1);
		}
		catch (FileNotFoundException e) {
			System.out.println("A blockchain nao existe");
			return false;
		}
		
		//quando so existe um bloco
		//esta incompleto por isso a integridade
		//nao pode ser verificada, mas tem de 
		//retornar true na mesma
		try {
			this.getBlock(2);
		}
		catch (FileNotFoundException e) {
			return true;
		}
		int count = 1;
		boolean compromised = false;
		Block b = null;
		Block prev = null;
		do {
			try {
				b = this.getBlock(count);
				if(count == 1) {
					compromised = compromised || !Arrays.equals(b.getPreviousHash(), "00000000".getBytes());
				}
				else {
					//to abort on last block
					this.getBlock(count+1);
					
					Mac mac = Mac.getInstance("HmacSHA256");
					KeyStore keystore = KeyStore.getInstance("JCEKS");
					FileInputStream keystore_fis = new FileInputStream(blockchainPath + "server.secret");
					keystore.load(keystore_fis, "aulelas.keystore".toCharArray());
					SecretKey key = (SecretKey) keystore.getKey("secServer", "aulelas.keystore".toCharArray());
					mac.init(key);
					
					FileInputStream fis = new FileInputStream(blockchainPath + "block_" + count + ".blk");
					ObjectInputStream ois = new ObjectInputStream(fis);
					mac.update(b.toByteArray());
					//ignore the first object because we already have it as b
					ois.readObject();

					compromised = compromised || !(b.getBlockId() == prev.getBlockId() + 1);
					compromised = compromised || !Arrays.equals(b.getPreviousHash(), this.getHashOfBlock(prev).digest());
					compromised = compromised || !Arrays.equals((byte[])ois.readObject(), mac.doFinal());		
					ois.close();
				}
				prev = b;
				count++;
			}
			catch (FileNotFoundException e) {
				System.out.println("Integridade de todos os blocos verificada");
				break;
			} catch (ClassNotFoundException | IOException e) {
				compromised = true;
				break;
			}
		}
		while(!compromised);
		return !compromised;
	}
	
	public boolean loadBlockchain() {
		//just to make sure
		this.blockchain.clear();
		
		int count = 1;
		do {
			try {
				this.blockchain.add(this.getBlock(count));
				count++;
			}
			catch (FileNotFoundException e) {
				System.out.println("A blockchain foi carregada em memoria!");
				break;
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Erro a carregar a blockchain!");
				return false;
			}
		}
		while(true);
		return this.blockchain.size() == count - 1;
	}
	
	public String getList() {
		StringBuilder sb = new StringBuilder();
		for(Block b : this.blockchain) {
			sb.append(b.transactionsToString());
		}
		return sb.toString();
	}
}