package client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Tintolmarket {

	static Scanner sc = new Scanner(System.in);

	public static void main(String[] args) throws EOFException{
		if (args.length < 2) {
			System.out.println("Uso: Tintolmarket <serverAddress>[:port] <userID> [password]");
			return;
		}

		String[] serverAddress = args[0].split(":");
		String host = serverAddress[0];
		int port = 12345;
		if (serverAddress.length > 1) {
			if (serverAddress[1].matches("\\d+") && serverAddress[1].length() == 5) {
				port = Integer.valueOf(serverAddress[1]);
			}
		}
		String clientID = args[1];
		String password;
		if (args.length > 2) {
			password = args[2];
		} else {
			System.out.print("Insira a sua password: ");
			password = sc.nextLine();
		}
		Socket cSoc = null;
		ObjectInputStream inStream = null;
		ObjectOutputStream outStream = null;
		try {
			cSoc = new Socket(host, port);
			outStream = new ObjectOutputStream(cSoc.getOutputStream());
			inStream = new ObjectInputStream(cSoc.getInputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			outStream.writeObject(clientID);
			outStream.writeObject(password);
			
			while(inStream.readBoolean()){
				String reply = (String) inStream.readObject();
				System.out.println(reply);
				String request = sc.nextLine();
				outStream.writeObject(request);
			}
			//String reply = (String) inStream.readObject();
			//System.out.printf("Response %s\n", reply);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			cSoc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
