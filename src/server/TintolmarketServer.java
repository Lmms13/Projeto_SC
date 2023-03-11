package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;

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
			System.out.println("thread do server para cada cliente");
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
					
					//realiza o ciclo de interação: menu->pedido do cliente->resposta do servidor
					while (!socket.isClosed()) {
						outStream.writeObject(displayMenu());
						String request = (String) inStream.readObject();
						String reply = processRequest(request);
						outStream.writeObject(reply);
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
					return "cap";
				}
				else {
					if(wineCatalog.wineExists(words[1])) {
						return "cap";
					}
					else {
						wineCatalog.addWine(words[1], words[2]);
						FileWriter fw = new FileWriter(wine_database, true);
					    fw.write(System.getProperty("line.separator") + wineCatalog.getWine(words[1]).toString());
					    fw.close();
					}
					return "nice";
				}
			} else if (command.equals("sell") || command.equals("s")) {
				if(words.length < 4) {
					return "cap";
				}
				else {
					if(!wineCatalog.wineExists(words[1])) {
						return "cap";
					}
					else {
						Wine w = wineCatalog.getWine(words[1]);
						if(w.sellerExists(currentUser.getId())){
							w.updateSeller(currentUser.getId(), Integer.parseInt(words[2]), Integer.parseInt(words[3]));
							updateSellerEntry(w.getId(), currentUser.getId());
							updateWineEntry(w.getId());
						}
						else{
							w.addSeller(currentUser.getId(), Integer.parseInt(words[2]), Integer.parseInt(words[3]));							
							FileWriter fw = new FileWriter(winesellers_database, true);
							fw.write(System.getProperty("line.separator") + w.sellerToString(currentUser.getId()));
							fw.close();
							updateWineEntry(w.getId());
						}
						return "nice";
					}
				}
				
			} else if (command.equals("view") || command.equals("v")) {
				if(!wineCatalog.wineExists(words[1])) {
					return "cap";
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
			} else if (command.equals("buy") || command.equals("b")) {
				// TODO
			} else if (command.equals("wallet") || command.equals("w")) {
				return "Tem " + Integer.toString(currentUser.getBalance()) + " \u20AC na carteira!";
			} else if (command.equals("classify") || command.equals("c")) {
				// TODO
			} else if (command.equals("talk") || command.equals("t")) {
				// TODO
			} else if (command.equals("read") || command.equals("r")) {
				// TODO
			} else {
				return "Comando incorreto!";
			}

			return "cap";
		}
	}
	
	private String displayMenu() {
		return(System.getProperty("line.separator") + "Utilizacao:" 
				+ System.getProperty("line.separator") + "add <wine> <image> OU a <wine> <image>"
				+ System.getProperty("line.separator") + "sell <wine> <value> <quantity> OU s <wine> <value> <quantity>" 
				+ System.getProperty("line.separator") + "view <wine> OU v <wine>"
				+ System.getProperty("line.separator") + "buy <wine> <seller> <quantity> OU b <wine> <seller> <quantity>" 
				+ System.getProperty("line.separator") + "wallet OU w"
				+ System.getProperty("line.separator") + "talk <user> <message> OU t <user> <message>"
				+ System.getProperty("line.separator") + "read OU r"
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
			w.addSeller(info[1], Integer.parseInt(info[2]), Integer.parseInt(info[3]));
			User u = userCatalog.getUser(info[1]);
			u.addWine(w.getId(), Integer.parseInt(info[2]), Integer.parseInt(info[3]));
		}		
		
		System.out.println("A base de dados de vinhos foi carregada em memória!");
	}
	
	private void updateSellerEntry(String wine, String seller) {
		Scanner sc = null;
		String line;
		String[] words;
		String oldLine = "";
		try {
			sc = new Scanner(sellersDB);
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
	    	if(words[0].equals(wine) && words[1].equals(seller)) {
	    		oldLine = line;
	    	}
	    }
	    sc.close();
	    String databaseContent = buffer.toString();
	    
	    databaseContent = databaseContent.replaceAll(oldLine, wineCatalog.getWine(wine).sellerToString(seller));
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
