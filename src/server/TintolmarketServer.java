/*
Grupo 31
Luis Santos 56341
Pedro Pinto 56369
Daniel Marques 56379
*/
package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import domain.Seller;
import domain.User;
import domain.Wine;
import domain.catalogs.UserCatalog;
import domain.catalogs.WineCatalog;
import server.blockchain.BlockchainHandler;

public class TintolmarketServer {

	private static final String user_database = "./src/server/files/user_database.txt";
	private static final String wine_database = "./src/server/files/wine_database.txt";
	private static final String winesellers_database = "./src/server/files/winesellers_database.txt";
	private static final String inbox_database = "./src/server/files/inbox_database.txt";
	private static final String balance_database = "./src/server/files/balance_database.txt";
	private UserCatalog userCatalog;
	private WineCatalog wineCatalog;
	private File userDB;
	private File wineDB;
	private File sellersDB;
	private File inboxDB;
	private File balanceDB;
	private File encryptedUserDB;
	private File decryptedUserDB;
	
	private String cypherPassword;
	private String keystorePath;
	private String keystorePassword;
	
	private BlockchainHandler blockchain = new BlockchainHandler();
	
	public static void main(String[] args) throws Exception {
//		File f = new File("./src/toDelete.txt");
//		FileOutputStream fos = new FileOutputStream(f);
//		fos.write("yoooo".getBytes());
//		fos.flush();
//		fos.close();
//		f.delete();
		//recebe o porto como argumento, usa 12345 como default
		int port;
		String cypherPassword;
		String keystorePath;
		String keystorePassword;
		if (args.length == 3) {
			port = 12345;
			cypherPassword = args[0];
			keystorePath = "./src/server/files/" + args[1];
			keystorePassword = args[2];
		} else if(args.length >= 4){ 
			port = Integer.valueOf(args[0]);
			cypherPassword = args[1];
			keystorePath = "./src/server/files/" + args[2];
			//keystorePath = keystorePath.concat(args[2]);
			keystorePassword = args[3];
		}
		else {
			System.out.println("Uso: TintolmarketServer <port> <password-cifra> <keystore> <password-keystore>");
			return;
		}
		
//		File fff = new File(keystorePath);
//		File ffff = new File("./keystore.server"); //abddadas		
		//inicializa um novo servidor
		TintolmarketServer server = new TintolmarketServer();
		server.startServer(port, cypherPassword, keystorePath, keystorePassword);
	}

	public void startServer(int port, String cypherPassword, String keystorePath, String keystorePassword) {
		this.cypherPassword = cypherPassword;
		this.keystorePath = keystorePath;
		this.keystorePassword = keystorePassword;
		ServerSocket sSoc = null;
		System.setProperty("javax.net.ssl.keyStore", keystorePath);
		System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
		ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
		SSLServerSocket ss =  null;

		try {
		//	sSoc = new ServerSocket(port);
			ss =  (SSLServerSocket) ssf.createServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		//verifica se o ficheiro de base de dados de clientes existe.
		userDB = new File(user_database);
		if (!userDB.exists()){
			System.out.println("Base de dados de clientes não encontrada!");
			return;
        }
		
		encryptedUserDB = new File("./src/server/files/user_database.cif");
		if (!encryptedUserDB.exists()){
			System.out.println("Base de dados de clientes cifrada não encontrada!");
			return;
		}
		
		decryptedUserDB = new File("./src/server/files/new_user_database.txt");
		
		startEncryptUserDatabase();
		//verifica se o ficheiro de base de dados de saldo existe.
		balanceDB = new File(balance_database);
		if (!balanceDB.exists()){
			System.out.println("Base de dados de saldo não encontrada!");
			return;
        }
		
		//verifica se o ficheiro de base de dados de clientes existe.
		inboxDB = new File(inbox_database);
		if (!inboxDB.exists()){
			System.out.println("Base de dados de caixa de mensagens não encontrada!");
			return;
        }
		
		//Carrega a base de dados para memória
		userCatalog = new UserCatalog();
		//decryptedUserDB = NEW fILE
		decyptUserDatabase();
		loadUserDatabase();
		encryptUserDatabase();
		decryptedUserDB.delete();
		
		//verifica se o ficheiro de base de dados de vinhos existe.
		wineDB = new File(wine_database);
		if (!wineDB.exists()){
			System.out.println("Base de dados de vinhos não encontrada!");
			return;
        }
		
		//verifica se o ficheiro de base de dados de vendedores existe.
		sellersDB = new File(winesellers_database);
		if (!sellersDB.exists()){
			System.out.println("Base de dados de vendedores não encontrada!");
			return;
		}
		
		//Carrega a base de dados para memória
		wineCatalog = new WineCatalog();
		loadWineDatabase();
		
		try {
			if(blockchain.checkIntegrity()) {
				blockchain.loadBlockchain();
			}
		} catch (UnrecoverableKeyException | InvalidKeyException | NoSuchAlgorithmException | KeyStoreException
				| CertificateException | ClassNotFoundException | IOException e1) {
			System.out.println("Ocorreu um erro a aceder a blockchain");
			e1.printStackTrace();
		}	
		
		System.out.printf("A escutar o porto %d...\n", port);
		
		//aceita clientes e inicializa uma thread por cliente
		while (true) {
			try {
				//Socket cSoc = sSoc.accept();
				Socket cSoc = ss.accept();
				ServerThread newServerThread = new ServerThread(cSoc);
				newServerThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		// sSoc.close();
	}

	class ServerThread extends Thread {

		private Socket socket = null;
		private User currentUser = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("Conexao com cliente estabelecida");
		}

		public void run() {
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String clientID = null;
				String password = null;
				
//				encryptUserDatabase();
//				decyptUserDatabase();

				try {
					//recebe o id e a password do cliente, por essa ordem
					clientID = (String) inStream.readObject();
				//	password = (String) inStream.readObject();
					long nonce = new Random().nextLong();
					outStream.writeObject(nonce);
					//outStream.writeLong(nonce);
					
					outStream.writeObject(userCatalog.userExists(clientID));
//					outStream.writeBoolean(userCatalog.userExists(clientID));
					
					//System.out.println("gaz");
					long clientNonce = inStream.readLong();
					byte[] clientNonceBytes = (byte[]) inStream.readObject();
					byte[] clientSignature = (byte[]) inStream.readObject();
					
					if(!userCatalog.userExists(clientID)) {
//						long clientNonce = inStream.readLong();
//						byte[] clientNonceBytes = (byte[]) inStream.readObject();
//						byte[] clientSignature = (byte[]) inStream.readObject();
						Certificate clientCert = (Certificate) inStream.readObject();
						File f = new File("./src/server/files/" + clientID + ".cer");
						byte[] buf = clientCert.getEncoded();
						FileOutputStream fos = new FileOutputStream(f);
						fos.write(buf);
						fos.close();
						if(nonce == clientNonce) {
//							KeyStore keystore = KeyStore.getInstance("JCEKS");
//							FileInputStream keystore_fis = new FileInputStream(keystorePath);
//							keystore.load(keystore_fis, keystorePassword.toCharArray());
							PublicKey pk = clientCert.getPublicKey();
							Signature signature = Signature.getInstance("MD5withRSA");
							signature.initVerify(pk);
							signature.update(clientNonceBytes);
							outStream.writeObject(signature.verify(clientSignature));
							
							userCatalog.addUser(clientID, "./src/server/files/" + clientID + ".cer");
							currentUser = userCatalog.getUser(clientID);
							
							decyptUserDatabase();
							FileWriter fw = new FileWriter(decryptedUserDB, true);
							fw.write(System.getProperty("line.separator") + currentUser.toString());
							fw.close();
							decryptedUserDB.delete();
							
							FileWriter fw1 = new FileWriter(balance_database, true);
							fw1.write(System.getProperty("line.separator") + currentUser.getId() + ":" + currentUser.getBalance());
							fw1.close();
							encryptUserDatabase();
							
						}
					}
					else {
//						long clientNonce = inStream.readLong();
//						byte[] clientNonceBytes = (byte[]) inStream.readObject();
//						byte[] clientSignature = (byte[]) inStream.readObject();
						if(nonce == clientNonce) {
							File f = new File("./src/server/files/" + clientID + ".cer");
							FileInputStream fis = new FileInputStream(f);
							//byte[] buf = fis.read();
							CertificateFactory cf = CertificateFactory.getInstance("X509");
							Certificate cert = cf.generateCertificate(fis);
//							KeyStore keystore = KeyStore.getInstance("JCEKS");
//							FileInputStream keystore_fis = new FileInputStream(keystorePath);
//							keystore.load(keystore_fis, keystorePassword.toCharArray());
//							Certificate cert = keystore.getCertificate(clientID);
							PublicKey pk = cert.getPublicKey();
							Signature signature = Signature.getInstance("MD5withRSA");
							signature.initVerify(pk);
							signature.update(clientNonceBytes);
							outStream.writeObject(signature.verify(clientSignature));
							
							currentUser = userCatalog.getUser(clientID);

						}
					}
					
//					if(true) {
//						return;
//					}
					
					
					//verifica se o cliente existe, caso negativo, adiciona-o ao catálogo
//					if (!userCatalog.userExists(clientID)) {
//						//indica que nao houve problemas na autenticacao
//						outStream.writeBoolean(true);
//						synchronized(this){
//							userCatalog.addUser(clientID, password);
//							currentUser = userCatalog.getUser(clientID);
//							
//							FileWriter fw = new FileWriter(user_database, true);
//							fw.write(System.getProperty("line.separator") + currentUser.toString());
//							fw.close();
//							
//							FileWriter fw1 = new FileWriter(balance_database, true);
//							fw1.write(System.getProperty("line.separator") + currentUser.getId() + ":" + currentUser.getBalance());
//							fw1.close();
//						}
//					} 
//					else {
//						//verifica se as credenciais do utilizador estão corretas
//						if(!userCatalog.checkPassword(clientID, password)) {
//							//se o user se tinha autenticado, saiu, e agora voltou a autenticar-se
//							//como as passwords sao apagadas da memoria depois da autenticacao
//							//e prciso voltar a carregar os dados para verificar se o cliente escreveu
//							//a password mal ou se e um cliente que esta a retornar antes do servidor se desligar 
//							loadUserDatabase();
//							
//							//se a password ainda for diferente da existente na base de dados, volta a pedi-la	
//							while(!userCatalog.checkPassword(clientID, password)) {
//								//indica que houve problemas na autenticacao
//								outStream.writeBoolean(false);
//								outStream.writeObject("Password incorreta. Insira a password de novo:");
//								password = (String) inStream.readObject();						
//							}
//						}
//						//indica que nao houve problemas na autenticacao
//						outStream.writeBoolean(true);
//						currentUser = userCatalog.getUser(clientID);						
//					}
					
					//limpa a password do cliente em memoria para nao estar em plaintext e
					//apenas estar disponivel na base de dados, encriptada
//					synchronized (this) {
//						currentUser.clearPassword();
//					}
//					password = null;						
					
					outStream.writeObject("Conexao estabelecida");
					System.out.println("A comunicar com o utilizador " + currentUser.getId());
					
					String reply = "";
					String request = "";
					
					//realiza o ciclo de interação: menu->pedido do cliente->resposta do servidor
					try {						
						while (!socket.isClosed()) {
							outStream.writeObject(displayMenu());
							request = (String) inStream.readObject();
							if(request.startsWith("a") || request.startsWith("add")) {
								receiveImage(request, inStream);
							}
							else if(request.startsWith("v") || request.startsWith("view")) {
								sendImage(request, outStream);
							}
							reply = processRequest(request);
							outStream.writeObject(reply);
						}
					} catch (SocketException e) {
						System.out.println("Conexao com cliente terminada");
						outStream.close();
						inStream.close();
						socket.close();
						return;
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | CertificateException | InvalidKeyException | SignatureException e) {
					System.out.println("Conexao com cliente terminada");
					outStream.close();
					inStream.close();
					socket.close();
					return;
				}
				outStream.writeBoolean(false);
				outStream.close();
				inStream.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private String processRequest(String request) throws Exception {
			String[] words = request.split(" ");
			String command = words[0];

			if (command.equals("add") || command.equals("a")) {
				if(words.length < 3) {
					return "O comando add recebe 2 argumentos";
				}
				else {
					if(wineCatalog.wineExists(words[1])) {
						return "O vinho ja existe na base de dados!";
					}
					else {
						synchronized(this){
							wineCatalog.addWine(words[1], words[2]);
							FileWriter fw = new FileWriter(wine_database, true);
							fw.write(System.getProperty("line.separator") + wineCatalog.getWine(words[1]).toString());
							fw.close();						
						}
					}
					return "Vinho adicionado a base de dados!";
				}
			} else if (command.equals("sell") || command.equals("s")) {
				if(words.length < 4) {
					return "O comando sell recebe 3 argumentos";
				}
				else {
					if(!wineCatalog.wineExists(words[1])) {
						return "O vinho nao existe na base de dados!";
					}
					else if(!words[2].matches("\\d+") || !words[3].matches("\\d+")){
						return "O preco e quantidade sao inteiros!";
					}
					else {
						Wine w = wineCatalog.getWine(words[1]);
						int price =  Integer.parseInt(words[2]);
						int quantity =  Integer.parseInt(words[3]);
						if(w.sellerExists(currentUser.getId())){
							synchronized(this){								
								w.updateSeller(currentUser.getId(), price, quantity);
								updateSellerEntry(w.getId(), currentUser.getId());
								updateWineEntry(w.getId());
							}
						}
						else{
							synchronized(this){
								w.addSeller(currentUser.getId(), price, quantity);							
								FileWriter fw = new FileWriter(winesellers_database, true);
								fw.write(System.getProperty("line.separator") + w.sellerToString(currentUser.getId()));
								fw.flush();
								fw.close();
								updateWineEntry(w.getId());
							}
						}
						synchronized (this) {
							blockchain.newTransaction(w.getId() + ":" + quantity + ":" + price + ":" + currentUser.getId());							
						}
						return "Vinho colocado a venda com sucesso!";
					}
				}
				
			} else if (command.equals("view") || command.equals("v")) {
				if(words.length < 2) {
					return "O comando view recebe 1 argumento";
				}
				else {
					if(!wineCatalog.wineExists(words[1])) {
						return "O vinho nao existe na base de dados!";
					}
					else {
						Wine w = wineCatalog.getWine(words[1]);
						if(w.sellersAvailable()) {
							return "---Vinho " + w.getId() + "---" + System.getProperty("line.separator") +
									"Imagem: " + w.getImage() + System.getProperty("line.separator") +
									"Classificacao: " + w.getRating() + System.getProperty("line.separator") +
									System.getProperty("line.separator") +
									"Vendedores: " + System.getProperty("line.separator") +
									w.displaySellers();	
						}
						else {
							return "---Vinho " + w.getId() + "---" + System.getProperty("line.separator") +
									"Imagem: " + w.getImage() + System.getProperty("line.separator") +
									"Classificacao: " + w.getRating();
						}	
					}
				}
			} else if (command.equals("buy") || command.equals("b")) {
				if(words.length < 4) {
					return "O comando buy recebe 3 argumentos";
				}
				else if(!wineCatalog.wineExists(words[1])) {
					return "O vinho nao existe na base de dados!";
				}
				else if(!words[3].matches("\\d+")) {
					return "A quantidade tem de ser um inteiro!";
				}
				else if(!userCatalog.userExists(words[2])) {
					return "O vendedor nao existe na base de dados!";
				}
				else {
					Wine w = wineCatalog.getWine(words[1]);
					if(!w.sellerExists(words[2])) {
						return "O utilizador nao esta a vender esse vinho!";
					}
					else {
						User u = userCatalog.getUser(words[2]);
						Seller s = w.getSeller(u.getId());
						int quantity = Integer.parseInt(words[3]);
						if(quantity > s.getAmount()) {
							return "Este vendedor nao vende unidades suficientes";
						}
						else if(currentUser.getBalance() < (s.getPrice() * quantity)) {
							return "Nao tem dinheiro suficiente na carteira!";
						}
						else {
							synchronized(this){
								w.subtractStock(quantity);
								u.addBalance(s.getPrice() * quantity);
								s.setAmount(s.getAmount() - quantity);
								currentUser.subtractBalance(s.getPrice() * quantity);
								updateSellerEntry(w.getId(), u.getId());
								updateWineEntry(w.getId());
								updateBalanceEntry(u.getId(), currentUser.getId());
								
								blockchain.newTransaction(w.getId() + ":" + quantity + ":" + s.getPrice() + ":" + currentUser.getId());							
								
								return "Compra efetuada com sucesso!";
							}
						}
						
					}
				}
			} else if (command.equals("wallet") || command.equals("w")) {
				//no eclipse funcionava com \u20AC para euros mas na command line nao
				return "Tem " + Integer.toString(currentUser.getBalance()) + " euros na carteira!";
			} else if (command.equals("classify") || command.equals("c")) {
				if(words.length < 3) {
					return "O comando classify recebe 2 argumentos";
				}
				else {
					if(!words[2].matches("\\d+")) {
						return "A classificacao tem de ser um inteiro entre 1 e 5";
					}
					else {
						int rating = Integer.parseInt(words[2]);
						if(!wineCatalog.wineExists(words[1])) {
							return "O vinho nao existe na base de dados!";
						}
						else if(rating < 1 || rating > 5) {
							return "A classificacao tem de ser um inteiro entre 1 e 5";
						}
						else {
							synchronized(this){								
								Wine w = wineCatalog.getWine(words[1]);
								w.updateRating(rating);
								updateWineEntry(w.getId());
								return "Classificacao adicionada!";
							}
						}
					}					
				}
			} else if (command.equals("talk") || command.equals("t")) {
				if(words.length < 3) {
					return "O comando talk recebe 2 argumentos";
				}
				else if(!userCatalog.userExists(words[1])) {
					return "O utilizador nao existe na base de dados!";	
				}
				else {
					synchronized (this) {					
						User u = userCatalog.getUser(words[1]);
						String message = String.join(" ", Arrays.copyOfRange(words, 2, words.length));
						if(u.hasSender(currentUser.getId())) {
							u.addMessage(currentUser.getId(), message);
							updateInboxEntry(u.getId(), currentUser.getId(), message);
						}
						else {
							u.addMessage(currentUser.getId(), message);
							FileWriter fw = new FileWriter(inbox_database, true);
							fw.write(System.getProperty("line.separator") + u.getId() + ":" + currentUser.getId() + ":" + message);
							fw.close();						
						}
					}
					return "Mensagem enviada com sucesso!";
				}
				
			} else if (command.equals("read") || command.equals("r")) {
				synchronized (this) {	
					if(!currentUser.hasMessages()) {
						return "Nao tem mensagens para ler";
					}
					else {
						String fullInbox = currentUser.displayMessages();
						currentUser.clearMessages();
						updateInboxEntry(currentUser.getId(), "none", "none");
						return fullInbox;
					}
				}				
			} else if (command.equals("list") || command.equals("l")){
				return "Lista de todas as transacoes:" + System.getProperty("line.separator") + blockchain.getList();
				
			} else {
				return "Comando incorreto!";
			}
		}
	}
	
	private String displayMenu() {
		return(System.getProperty("line.separator") + "Utilizacao:" 
				+ System.getProperty("line.separator") + "add <wine> <image>"
				+ System.getProperty("line.separator") + "sell <wine> <value> <quantity>" 
				+ System.getProperty("line.separator") + "view <wine>"
				+ System.getProperty("line.separator") + "buy <wine> <seller> <quantity>" 
				+ System.getProperty("line.separator") + "wallet"
				+ System.getProperty("line.separator") + "classify <wine> <stars>"
				+ System.getProperty("line.separator") + "talk <user> <message>"
				+ System.getProperty("line.separator") + "read"
				+ System.getProperty("line.separator") + "list"
				+ System.getProperty("line.separator"));
	}
	
	private void loadUserDatabase() {
		Scanner uFileScanner = null;
		Scanner iFileScanner = null;
		Scanner bFileScanner = null;
		String[] credentials;
		String[] info;

		//cria um scanner para ler o ficheiro
		try {
//			uFileScanner = new Scanner(userDB);
			uFileScanner = new Scanner(decryptedUserDB);
			bFileScanner = new Scanner(balanceDB);
			iFileScanner = new Scanner(inboxDB);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if(!userCatalog.isEmpty()) {
			synchronized (this) {
				userCatalog.clear();				
			}
		}
		
		//lê o ficheiro linha a linha e adiciona cada user ao catálogo local
		while(uFileScanner.hasNextLine()) {
			credentials = uFileScanner.nextLine().split(":");
			userCatalog.addUser(credentials[0], credentials[1]);  
		}
		
		while(bFileScanner.hasNextLine()) {
			info = bFileScanner.nextLine().split(":");
			userCatalog.getUser(info[0]).setBalance(Integer.parseInt(info[1]));
		}
		
		while(iFileScanner.hasNextLine()) {
			info = iFileScanner.nextLine().split(":");
			userCatalog.getUser(info[0]).loadMessages(info[1], info[2]);
		}
		
		uFileScanner.close();
		iFileScanner.close();
		bFileScanner.close();
		
		System.out.println("A base de dados de utilizadores foi carregada em memória!");
	}
	
	private void loadWineDatabase() {
		Scanner wFileScanner = null;
		Scanner sFileScanner = null;
		String[] info;

		//cria scanners para ler os ficheiros
		try {
			wFileScanner = new Scanner(wineDB);
			sFileScanner = new Scanner(sellersDB);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//lê o ficheiro linha a linha e adiciona cada vinho ao catálogo local
		while(wFileScanner.hasNextLine()) {
			info = wFileScanner.nextLine().split(":");
			String[] ratings = info[2].split("/"); 
			wineCatalog.loadWine(info[0], info[1], Integer.parseInt(ratings[0]), Integer.parseInt(ratings[1]), Integer.parseInt(info[3]));  
		}
		
		//lê o ficheiro linha a linha e liga o vinho ao(s) respetivo(s) utilizador(es) que o(s) vende(m)
		while(sFileScanner.hasNextLine()) {
			info = sFileScanner.nextLine().split(":");
			Wine w = wineCatalog.getWine(info[0]);
			w.loadSeller(info[1], Integer.parseInt(info[2]), Integer.parseInt(info[3]));
		}		
		
		wFileScanner.close();
		sFileScanner.close();
		
		System.out.println("A base de dados de vinhos foi carregada em memória!");
	}
	
	private synchronized void updateSellerEntry(String wine, String seller) {
		Scanner sc = null;
		String line;
		String[] words;
		String oldLine = "";
		int quantity = wineCatalog.getWine(wine).getSeller(seller).getAmount();
		try {
			sc = new Scanner(sellersDB);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	    StringBuffer buffer = new StringBuffer();

	    while (sc.hasNextLine()) {
	    	line = sc.nextLine();
	    	words = line.split(":");
	    	
	    	if(words[0].equals(wine) && words[1].equals(seller) && quantity == 0) {
	    		continue;
	    	}
	    	
	    	if(!sc.hasNextLine()) {
	    		buffer.append(line);
	    	}
	    	else{
	    		buffer.append(line + System.getProperty("line.separator"));
	    	}
	    	if(words[0].equals(wine) && words[1].equals(seller)) {
	    		oldLine = line;
	    	}
	    }
	    sc.close();
	    String databaseContent = buffer.toString();
	    
	    if(quantity != 0 && oldLine.length() > 0) {
	    	databaseContent = databaseContent.replaceAll(oldLine, wineCatalog.getWine(wine).sellerToString(seller));	    		    	
	    }
	    
	    if(databaseContent.endsWith(System.getProperty("line.separator"))) {
	    	databaseContent = databaseContent.replaceAll("(\r\n?|\n)$", "");
	    }
	    
	    FileWriter fw = null;
	    try {
	    	fw = new FileWriter(sellersDB);
	    	fw.append(databaseContent);
	    	//salvaguarda para a remocao de uma linha da base de dados em runtime
	    	if(oldLine.length() == 0 && quantity != 0) {
	    		fw.append(System.getProperty("line.separator") + wineCatalog.getWine(wine).sellerToString(seller));
	    	}
	    	fw.flush();
	    	fw.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	private synchronized void updateWineEntry(String wine) {
		Scanner sc = null;
		String line;
		String[] words;
		String oldLine = "";
		try {
			sc = new Scanner(wineDB);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	    StringBuffer buffer = new StringBuffer();

	    while (sc.hasNextLine()) {
	    	line = sc.nextLine();
	    	words = line.split(":");

	    	if(!sc.hasNextLine()) {
	    		buffer.append(line);
	    	}
	    	else{
	    		buffer.append(line + System.getProperty("line.separator"));
	    	}
	    	if(words[0].equals(wine)) {
	    		oldLine = line;
	    	}
	    }
	    sc.close();
	    String databaseContent = buffer.toString();
	    
	    if(oldLine.length() > 0) {
	    	databaseContent = databaseContent.replaceAll(oldLine, wineCatalog.getWine(wine).toString());	    	
	    }
	    FileWriter fw = null;
	    try {
	    	fw = new FileWriter(wineDB);
	    	fw.append(databaseContent);
	    	//salvaguarda para a remocao de uma linha da base de dados em runtime
	        if(oldLine.length() == 0) {
		    	fw.append(System.getProperty("line.separator") + wineCatalog.getWine(wine).toString());	    	
		    }
	    	fw.flush();
	    	fw.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }    
	}
	
	private synchronized void updateInboxEntry(String recipient, String sender, String message) {
		Scanner sc = null;
		String line;
		String[] words;
		String oldLine = "";
		boolean clear = !userCatalog.getUser(recipient).hasMessages();
		try {
			sc = new Scanner(inboxDB);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	    StringBuffer buffer = new StringBuffer();

	    while (sc.hasNextLine()) {
	    	line = sc.nextLine();
	    	words = line.split(":");
	    	
	    	if(words[0].equals(recipient) && clear) {
	    		continue;
	    	}
	    	if(!sc.hasNextLine()) {
	    		buffer.append(line);
	    	}
	    	else{
	    		buffer.append(line + System.getProperty("line.separator"));
	    	}
	    	if(words[0].equals(recipient) && words[1].equals(sender)) {
	    		oldLine = line;
	    	}
	    }
	    sc.close();
	    String databaseContent = buffer.toString();
	    
	    if(!clear && oldLine.length() > 0) {
	    	databaseContent = databaseContent.replaceAll(oldLine, userCatalog.getUser(recipient).getMessagesFromSender(sender));	   
	    	
	    	/*o replaceAll adiciona '?' quando a mensagem anterior acabava em '?'
	    	 * e nao sei porque, mas eventualmente rebentava e nao dava para adicionar
	    	 * mais mensagens a essa linha. Se nao houver nenhum '?' numa mensagem 
	    	 * anterior funciona tudo normalmente*/
	    	databaseContent = databaseContent.replaceAll(message + "\\?", message);
	    
	    	/*tratamento muito especifico do erro explicado em cima, a unica solucao
	    	 *foi apagar a linha e escrever de novo no fim atraves dos dados em memoria.
	    	 * Este erro nao afetava a memoria, apenas a base de dados, e a 
	    	 * ordem nao interessa na base de dados*/
	    	if(!databaseContent.contains(message)) {
	    		StringBuilder sb = new StringBuilder();
	    		String[] lines = databaseContent.split(System.getProperty("line.separator"));
	    		for(String l : lines) {
	    			if(!l.contains(oldLine)) {
	    				sb.append(l + System.getProperty("line.separator"));
	    			}
	    		}
	    		databaseContent = sb.toString();
	    		oldLine = "";
	    	}
	    }
	    
	    if(databaseContent.endsWith(System.getProperty("line.separator"))) {
	    	databaseContent = databaseContent.replaceAll("(\r\n?|\n)$", "");
	    }
	    
	    FileWriter fw = null;
	    try {
	    	fw = new FileWriter(inboxDB);
	    	fw.append(databaseContent);
	    	//salvaguarda para a remocao de uma linha da base de dados em runtime
	    	//tambem e usado para tratar o erro em cima
	    	if(oldLine.length() == 0 && !clear) {
	    		fw.append(System.getProperty("line.separator") + userCatalog.getUser(recipient).getMessagesFromSender(sender));
	    	}
	    	fw.flush();
	    	fw.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	private synchronized void updateBalanceEntry(String seller, String buyer) {
		Scanner sc = null;
		String line;
		String[] words;
		String oldLine1 = "";
		String oldLine2 = "";
		
		try {
			sc = new Scanner(balanceDB);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	    StringBuffer buffer = new StringBuffer();

	    while (sc.hasNextLine()) {
	    	line = sc.nextLine();
	    	words = line.split(":");

	    	if(!sc.hasNextLine()) {
	    		buffer.append(line);
	    	}
	    	else{
	    		buffer.append(line + System.getProperty("line.separator"));
	    	}
	    	if(words[0].equals(seller)) {
	    		oldLine1 = line;
	    	}
	    	if(words[0].equals(buyer)) {
	    		oldLine2 = line;
	    	}
	    }
	    sc.close();
	    String databaseContent = buffer.toString();
	    
	    if(oldLine1.length() > 0) {
	    	databaseContent = databaseContent.replaceAll(oldLine1, seller + ":" + userCatalog.getUser(seller).getBalance());	    	 	
	    }
	    if(oldLine2.length() > 0) {
	    	databaseContent = databaseContent.replaceAll(oldLine2, buyer + ":" + userCatalog.getUser(buyer).getBalance());	    		    	
	    }
	    
	    FileWriter fw = null;
	    try {
	    	fw = new FileWriter(balanceDB);
	    	fw.append(databaseContent);
	    	//salvaguarda para a remocao de uma linha da base de dados em runtime
	    	if(oldLine1.length() == 0) {
	    		fw.append(System.getProperty("line.separator") + seller + ":" + userCatalog.getUser(seller).getBalance());
	    	}
	    	//salvaguarda para a remocao de uma linha da base de dados em runtime
	    	if(oldLine2.length() == 0) {
	    		fw.append(System.getProperty("line.separator") + buyer + ":" + userCatalog.getUser(buyer).getBalance());
	    	}
	    	fw.flush();
	    	fw.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	private void sendImage(String request, ObjectOutputStream outStream) {
		String[] words;
		File f;
		FileInputStream fin = null;
		InputStream input;
		byte[] buffer;
		int bytesRead = 0;
		
		words = request.split(" ");
		f = new File("./src/server/images/" + wineCatalog.getWine(words[1]).getImage());
		if (!f.exists()){
			System.out.println("Imagem nao encontrada!");
			return;
		}

		try {
			fin = new FileInputStream(f);
			input = new BufferedInputStream(fin);
			
			int size = 0;
			size = (int) Files.size(Paths.get(f.getPath()));
			buffer = new byte[size];
			bytesRead = input.read(buffer);
			
			outStream.writeInt(bytesRead);
			outStream.writeObject(buffer);

			input.close();
		} catch (IOException e) {e.printStackTrace();}	
	}
	
	private void receiveImage(String request, ObjectInputStream inStream) {
		String[] words;
		File f;
		FileOutputStream fout = null;
		OutputStream output;
		byte[] buffer;
		int bytesRead = 0;

		words = request.split(" ");
		try {
			bytesRead = inStream.readInt();
			buffer = new byte[bytesRead];
			buffer = (byte[]) inStream.readObject();
			
			f = new File("./src/server/images/" + words[2]);
			fout = new FileOutputStream(f);
			output = new BufferedOutputStream(fout);
			output.write(buffer, 0, bytesRead);

			output.close();
		} catch (IOException | ClassNotFoundException e) {e.printStackTrace();}
	}
	
	public void startEncryptUserDatabase() {
		try {		
		byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0xf2 };
//		File f = new File("./src/server/files/new_user_database.txt");
		FileInputStream fis = new FileInputStream(userDB);
		//ByteArrayInputStream bais = new ByteArrayInputStream(fis);
		PBEKeySpec keySpec = new PBEKeySpec(cypherPassword.toCharArray(), salt, 20); // pass, salt, iterations
		SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
		SecretKey key = kf.generateSecret(keySpec);
		
		Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] enc = c.doFinal(fis.readAllBytes());
		byte[] params = c.getParameters().getEncoded();
		
		//File f1 = new File("./src/server/files/user_database.cif");
		FileOutputStream fos1 = new FileOutputStream(encryptedUserDB);
		fos1.write(enc);
		
		File f2 = new File("./src/server/files/cypher_params.txt");
		FileOutputStream fos2 = new FileOutputStream(f2);
		fos2.write(params);
		
		fis.close();
		fos1.flush();
		fos1.close();
		fos2.flush();
		fos2.close();
		
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}
	}
	
	public void encryptUserDatabase() {
		try {		
		byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0xf2 };
//		File f = new File("./src/server/files/new_user_database.txt");
		FileInputStream fis = new FileInputStream(decryptedUserDB);
		//ByteArrayInputStream bais = new ByteArrayInputStream(fis);
		PBEKeySpec keySpec = new PBEKeySpec(cypherPassword.toCharArray(), salt, 20); // pass, salt, iterations
		SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
		SecretKey key = kf.generateSecret(keySpec);
		
		Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] enc = c.doFinal(fis.readAllBytes());
		byte[] params = c.getParameters().getEncoded();
		
		//File f1 = new File("./src/server/files/user_database.cif");
		FileOutputStream fos1 = new FileOutputStream(encryptedUserDB);
		fos1.write(enc);
		
		File f2 = new File("./src/server/files/cypher_params.txt");
		FileOutputStream fos2 = new FileOutputStream(f2);
		fos2.write(params);
		
		fis.close();
		fos1.flush();
		fos1.close();
		fos2.flush();
		fos2.close();
		
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}
	}
	
	public void decyptUserDatabase() {
		try {		
		byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0xf2 };
//		File f = new File(user_database);
//		FileInputStream fis = new FileInputStream(f);
		//ByteArrayInputStream bais = new ByteArrayInputStream(fis);
		PBEKeySpec keySpec = new PBEKeySpec(cypherPassword.toCharArray(), salt, 20); // pass, salt, iterations
		SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
		SecretKey key = kf.generateSecret(keySpec);
		
	//	File f1 = encryptedUserDB;
		FileInputStream fis1 = new FileInputStream(encryptedUserDB);
		byte[] data = fis1.readAllBytes();
		
		File f2 = new File("./src/server/files/cypher_params.txt");
		FileInputStream fis2 = new FileInputStream(f2);
		byte[] params = fis2.readAllBytes();
		
		AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
		p.init(params);
		Cipher d = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
		d.init(Cipher.DECRYPT_MODE, key, p);
		byte[] dec = d.doFinal(data);
		
		//File f3 = new File("./src/server/files/new_user_database.txt");
		FileOutputStream fos = new FileOutputStream(decryptedUserDB);
		fos.write(dec);
		fos.flush();
		fos.close();
		
//		Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
//		c.init(Cipher.ENCRYPT_MODE, key);
//		byte[] enc = c.doFinal(fis.readAllBytes());
//		byte[] params = c.getParameters().getEncoded();
		
//		File f1 = new File("./src/server/files/user_database.cif");
//		FileOutputStream fos1 = new FileOutputStream(f1);
//		fos1.write(enc);
		
//		File f2 = new File("./src/server/files/cypher_params.txt");
//		FileOutputStream fos2 = new FileOutputStream(f2);
//		fos2.write(params);
		
		//fis.close();
	//	fis1.flush();
		fis1.close();
		//fos2.flush();
		fis2.close();
		//return f3;
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			//return null;
		}
	}
}
