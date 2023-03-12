package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

import domain.Seller;
import domain.User;
import domain.Wine;
import domain.catalogs.UserCatalog;
import domain.catalogs.WineCatalog;

public class TintolmarketServer {

	private static final String user_database = "./src/server/files/user_database.txt";
	private static final String wine_database = "./src/server/files/wine_database.txt";
	private static final String winesellers_database = "./src/server/files/winesellers_database.txt";
	private UserCatalog userCatalog;
	private WineCatalog wineCatalog;
	private File userDB;
	private File wineDB;
	private File sellersDB;

	public static void main(String[] args) throws Exception {
		//recebe o porto como argumento, usa 12345 como default
		int port;
		if (args.length < 1) {
			port = 12345;
		} else {
			port = Integer.valueOf(args[0]);
		}
		
		//inicializa um novo servidor
		TintolmarketServer server = new TintolmarketServer();
		server.startServer(port);
	}

	public void startServer(int port) {
		ServerSocket sSoc = null;

		try {
			//perguntar ao stor
			sSoc = new ServerSocket(port);
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
		
		//Carrega a base de dados para memória
		userCatalog = new UserCatalog();
		loadUserDatabase();
		
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
		
		System.out.printf("A escutar o porto %d...\n", port);
		
		//aceita clientes e inicializa uma thread por cliente
		while (true) {
			try {
				Socket cSoc = sSoc.accept();
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

				try {
					//recebe o id e a password do cliente, por essa ordem
					clientID = (String) inStream.readObject();
					password = (String) inStream.readObject();
					
					//verifica se o cliente existe, caso negativo, adiciona-o ao catálogo
					if (!userCatalog.userExists(clientID)) {
						userCatalog.addUser(clientID, password);
						currentUser = userCatalog.getUser(clientID);
						//pode passar para fora se for usado noutro caso, desde que seja devidamente fechado
						FileWriter fw = new FileWriter(user_database, true);
					    fw.write(System.getProperty("line.separator") + currentUser.toString());
					    fw.close();
					} 
					//verifica se as credenciais do utilizador estão corretas, caso negativo, termina a interação
					else if(!userCatalog.checkPassword(clientID, password)) {
						System.out.println("Credenciais erradas");
						outStream.close();
						inStream.close();
						return;
					}
					else {
						currentUser = userCatalog.getUser(clientID);						
					}
					
					outStream.writeObject("Conexao estabelecida");
					System.out.println("A comunicar com o utilizador " + currentUser.getId());
					
					//realiza o ciclo de interação: menu->pedido do cliente->resposta do servidor
					try {						
						while (!socket.isClosed()) {
							outStream.writeObject(displayMenu());
							String request = (String) inStream.readObject();
							String reply = processRequest(request);
							outStream.writeObject(reply);
						}
					} catch (SocketException e) {
						System.out.println("Conexao com cliente terminada");
						outStream.close();
						inStream.close();
						socket.close();
						return;
					}
					
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}

				outStream.writeBoolean(false);
				outStream.close();
				inStream.close();

				socket.close();

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private String processRequest(String request) throws Exception {
			String[] words = request.split(" ");
			String command = words[0];

			// não pode ser switch porque é preciso usar disjunção e o switch não suporta
			// seria preciso o dobro das linhas por causa dos sinónimos
			if (command.equals("add") || command.equals("a")) {
				if(words.length < 3) {
					return "O comando add recebe 2 argumentos";
				}
				else {
					if(wineCatalog.wineExists(words[1])) {
						return "O vinho ja existe na base de dados!";
					}
					else {
						wineCatalog.addWine(words[1], words[2]);
						FileWriter fw = new FileWriter(wine_database, true);
					    fw.write(System.getProperty("line.separator") + wineCatalog.getWine(words[1]).toString());
					    fw.close();
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
							w.updateSeller(currentUser.getId(), price, quantity);
							updateSellerEntry(w.getId(), currentUser.getId());
							updateWineEntry(w.getId());
						}
						else{
							w.addSeller(currentUser.getId(), price, quantity);							
							FileWriter fw = new FileWriter(winesellers_database, true);
							fw.write(System.getProperty("line.separator") + w.sellerToString(currentUser.getId()));
							fw.close();
							updateWineEntry(w.getId());
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
							w.subtractStock(quantity);
							u.addBalance(s.getPrice() * quantity);
							s.setAmount(s.getAmount() - quantity);
							currentUser.subtractBalance(s.getPrice() * quantity);
							updateSellerEntry(w.getId(), u.getId());
							updateWineEntry(w.getId());
							return "Compra efetuada com sucesso!";
						}
						
					}
				}
			} else if (command.equals("wallet") || command.equals("w")) {
				return "Tem " + Integer.toString(currentUser.getBalance()) + " \u20AC na carteira!";
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
							Wine w = wineCatalog.getWine(words[1]);
							w.updateRating(rating);
							updateWineEntry(w.getId());
							return "Classificacao adicionada!";
						}
					}					
				}
			} else if (command.equals("talk") || command.equals("t")) {
				// TODO
			} else if (command.equals("read") || command.equals("r")) {
				// TODO
			} else {
				return "Comando incorreto!";
			}
			
			//nunca chega aqui
			return "Ocorreu um erro";
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
				+ System.getProperty("line.separator"));
	}
	
	private void loadUserDatabase() {
		Scanner fileScanner = null;
		String[] credentials;

		//cria um scanner para ler o ficheiro
		try {
			fileScanner = new Scanner(userDB);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//lê o ficheiro linha a linha e adiciona cada user ao catálogo local
		while(fileScanner.hasNextLine()) {
			credentials = fileScanner.nextLine().split(":");
			userCatalog.addUser(credentials[0], credentials[1]);  
		}
		
		//impressão para efeitos de teste
//		for(User u: catalog.getList()) {
//			System.out.println(u.toString());
//		}
		System.out.println("A base de dados de utilizadores foi carregada em memória!");
	}
	
	private void loadWineDatabase() {
		Scanner wFileScanner = null;
		Scanner sFileScanner = null;
		String[] info;

		//cria scanners para ler os ficheiros
		try {
			wFileScanner = new Scanner(wineDB);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
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
		
		System.out.println("A base de dados de vinhos foi carregada em memória!");
	}
	
	private void updateSellerEntry(String wine, String seller) {
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
	    
	    if(quantity != 0) {
	    	databaseContent = databaseContent.replaceAll(oldLine, wineCatalog.getWine(wine).sellerToString(seller));	    		    	
	    }
	    
	    FileWriter fw = null;
	    try {
	    	fw = new FileWriter(sellersDB);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    try {
			fw.append(databaseContent);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    try {
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void updateWineEntry(String wine) {
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
	    
	    databaseContent = databaseContent.replaceAll(oldLine, wineCatalog.getWine(wine).toString());
	    FileWriter fw = null;
	    try {
	    	fw = new FileWriter(wineDB);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    try {
			fw.append(databaseContent);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    try {
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
