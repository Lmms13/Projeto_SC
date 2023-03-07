package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import domain.User;
import domain.catalogs.UserCatalog;

public class TintolmarketServer {

	private static final String user_database = "/src/server/files/user_database.txt";
	private UserCatalog userCatalog;

	public static void main(String[] args) throws Exception {
		int port;
		if (args.length < 1) {
			port = 12345;
		} else {
			port = Integer.valueOf(args[0]);
		}

		// Load database to memory
		File f = new File(user_database);

		// Checking if the specified file exists or not
//        if (f.exists()){
//        	FileInputStream fileIn = new FileInputStream(user_database);
//            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
//           // users = (HashMap<String, User>) objectIn.readObject();
//            objectIn.close();
//            System.out.println("The database has been loaded!");
//        } else {
//        	users = new HashMap<String, User>();
//        }
		TintolmarketServer server = new TintolmarketServer();
		server.startServer(port);
	}

	public void startServer(int port) {
		ServerSocket sSoc = null;

		try {
			sSoc = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		System.out.printf("A escutar o porto %d...\n", port);

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
			userCatalog = new UserCatalog();
			// load users
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String clientID = null;
				String password = null;

				try {
					clientID = (String) inStream.readObject();
					password = (String) inStream.readObject();

					if (userCatalog.userExists(clientID)) {
						if (userCatalog.checkPassword(clientID, password)) {
							user = userCatalog.getUser(clientID);
							displayMenu();

							while (!socket.isClosed()) {
								outStream.writeObject(true);
								String request = (String) inStream.readObject();
								String reply = processRequest(request);
								outStream.writeObject(reply);
							}
						} else {
							System.out.println("Credenciais erradas");
						}
					} else {
						userCatalog.addUser(clientID, password);
						// adicionar ao ficheiro
					}
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}

				outStream.writeObject(false);
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
				// TODO
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
				System.out.println("Comando incorreto!");
				displayMenu();
			}

			// depois vê-se
			return null;
		}
	}

	private void displayMenu() {
		System.out.println("Utilizacao:\n" + "add <wine> <image> OU a <wine> <image>\n "
				+ "sell <wine> <value> <quantity> OU s <wine> <value> <quantity>\n" + "view <wine> OU v <wine>\n"
				+ "buy <wine> <seller> <quantity> OU b <wine> <seller> <quantity>\n" + "wallet OU w\n"
				+ "talk <user> <message> OU t <user> <message>\n" + "read OU r");
	}
}
