package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
			
			String reply = "";
			String request = "";
			
			//realiza o ciclo de interação: menu->resposta do servidor->pedido do cliente
			while(true){
				if(request.startsWith("a") || request.startsWith("add")) {
					sendImage(request, outStream);
				}
				else if(request.startsWith("v") || request.startsWith("view")) {
					receiveImage(request, inStream);
				}
				reply = (String) inStream.readObject();
				System.out.println(reply);
				reply = (String) inStream.readObject();
				System.out.println(reply);
				request = sc.nextLine();
				outStream.writeObject(request);
			}

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
	
	private static void sendImage(String request, ObjectOutputStream outStream) {
		String[] words;
		File f;
		FileInputStream fin = null;
		InputStream input;
		byte[] buffer;
		int bytesRead = 0;
		
		words = request.split(" ");
		f = new File("./src/client/images/" + words[2]);
		if (!f.exists()){
			System.out.println("Imagem nao encontrada!");
			return;
        }
		
		try {
			fin = new FileInputStream(f);
		} catch (FileNotFoundException e) {e.printStackTrace();}
		
		input = new BufferedInputStream(fin);
		int size = 0;
		try {
			size = (int) Files.size(Paths.get("./src/client/images/" + words[2]));
		} catch (IOException e) {e.printStackTrace();}
		
		buffer = new byte[size];
		try {
			bytesRead = input.read(buffer);
		} catch (IOException e) {e.printStackTrace();}
		
		try {
			outStream.writeInt(bytesRead);
		} catch (IOException e) {e.printStackTrace();}
		
		try {
			outStream.writeObject(buffer);
		} catch (IOException e) {e.printStackTrace();}
		
		try {
			input.close();
		} catch (IOException e) {e.printStackTrace();}	
	}
	
	private static void receiveImage(String request, ObjectInputStream inStream) {
		String[] words;
		File f;
		FileOutputStream fout = null;
		OutputStream output;
		byte[] buffer;
		int bytesRead = 0;

		words = request.split(" ");
		try {
			bytesRead = inStream.readInt();
		} catch (IOException e) {e.printStackTrace();}
		
		buffer = new byte[bytesRead];
		try {
			buffer = (byte[]) inStream.readObject();
		} catch (ClassNotFoundException | IOException e) {e.printStackTrace();}
		
		f = new File("./src/client/images/" + words[1] + ".png");
		try {
			fout = new FileOutputStream(f);
		} catch (FileNotFoundException e) {e.printStackTrace();}
		
		output = new BufferedOutputStream(fout);
		try {
			output.write(buffer, 0, bytesRead);
		} catch (IOException e) {e.printStackTrace();}
		
		try {
			output.close();
		} catch (IOException e) {e.printStackTrace();}
	}
}
