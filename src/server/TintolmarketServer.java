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
import java.util.Scanner;

import domain.User;
import domain.catalogs.UserCatalog;

public class TintolmarketServer {

	private static final String user_database = "./src/server/files/user_database.txt";
	private UserCatalog userCatalog;
	private File f;

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
		f = new File(user_database);
		if (!f.exists()){
			System.out.println("Base de dados de clientes não ecnontrada!");
			return;
        }
		
		//Carrega o base de dados para memória
		userCatalog = new UserCatalog();
		loadDatabase();

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
		private User user = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("thread do server para cada cliente");
		}

		public void run() {
//			userCatalog = new UserCatalog();
			
//			File f = new File(user_database);
			
			//verifica se o ficheiro existe e carrega o base de dados para memória
//	        if (f.exists()){
//	        	loadDatabase(userCatalog, f);
//	        }
	        
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
						user = userCatalog.getUser(clientID);
						//pode passar para fora se for usado noutro caso, desde que seja devidamente fechado
						FileWriter fw = new FileWriter(user_database, true);
					    fw.write(System.getProperty("line.separator") + user.toString());
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
						user = userCatalog.getUser(clientID);						
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
				return "fizeste add mpt";
			} else if (command.equals("sell") || command.equals("s")) {
				// TODO
			} else if (command.equals("view") || command.equals("v")) {
				// TODO
			} else if (command.equals("buy") || command.equals("b")) {
				// TODO
			} else if (command.equals("wallet") || command.equals("w")) {
				// TODO
			} else if (command.equals("classify") || command.equals("c")) {
				// TODO
			} else if (command.equals("talk") || command.equals("t")) {
				// TODO
			} else if (command.equals("read") || command.equals("r")) {
				// TODO
			} else {
				return "Comando incorreto!";
			}

			// depois vê-se
			return null;
		}
	}

//	private void displayMenu() {
//		System.out.println("Utilizacao:\n" + "add <wine> <image> OU a <wine> <image>\n"
//				+ "sell <wine> <value> <quantity> OU s <wine> <value> <quantity>\n" + "view <wine> OU v <wine>\n"
//				+ "buy <wine> <seller> <quantity> OU b <wine> <seller> <quantity>\n" + "wallet OU w\n"
//				+ "talk <user> <message> OU t <user> <message>\n" + "read OU r");
//	}
	
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
	
	private void loadDatabase() {
		Scanner fileScanner = null;
		String[] credentials;

		//cria um scanner para ler o ficheiro
		try {
			fileScanner = new Scanner(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//lê o ficheiro linha a linha e adiciona cada usar ao catálogo local
		while(fileScanner.hasNextLine()) {
			credentials = fileScanner.nextLine().split(":");
			userCatalog.addUser(credentials[0], credentials[1]);  
		}
		
		//impressão para efeitos de teste
//		for(User u: catalog.getList()) {
//			System.out.println(u.toString());
//		}
		System.out.println("A base de dados foi carregada em memória!");
	}
}
