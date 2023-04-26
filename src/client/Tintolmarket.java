/*
Grupo 31
Luis Santos 56341
Pedro Pinto 56369
Daniel Marques 56379
*/
package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.crypto.SecretKey;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Tintolmarket {

	static Scanner sc = new Scanner(System.in);
	private static String clientID;
	private static String password;

	public static void main(String[] args) throws EOFException{
		String truststorePath;
		String keystorePath;
		String keystorePassword;
		if (args.length < 5) {
			System.out.println("Uso: Tintolmarket <serverAddress>:[port] <truststore> <keystore> <password-keystore> <userID>");
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
		truststorePath = "./src/client/files/" + args[1];
		keystorePath = "./src/client/files/" + args[2];
		keystorePassword = args[3];
		clientID = args[4];
//		if (args.length > 2) {
//			password = args[2];
//		} else {
//			System.out.print("Insira a sua password: ");
//			password = sc.nextLine();
//		}
		//Socket cSoc = null;
		ObjectInputStream inStream = null;
		ObjectOutputStream outStream = null;
//		System.setProperty("javax.net.ssl.keyStore", keystorePath);
//		System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
		System.setProperty("javax.net.ssl.trustStore", truststorePath);
//		System.setProperty("javax.net.ssl.trustStorePassword", "changeit"); 

		SocketFactory sf = SSLSocketFactory.getDefault();
		SSLSocket s = null;
		try {
			s = (SSLSocket) sf.createSocket(host, port);
			//s.startHandshake();
			//s.startHandshake();
		//	cSoc = new Socket(host, port);
//			outStream = new ObjectOutputStream(cSoc.getOutputStream());
//			inStream = new ObjectInputStream(cSoc.getInputStream());
			outStream = new ObjectOutputStream(s.getOutputStream());
			inStream = new ObjectInputStream(s.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			outStream.writeObject(clientID);
			
			//long nonce = inStream.readLong();
			long nonce = (long) inStream.readObject();
			boolean isRegistered = (boolean) inStream.readObject();
			
			System.out.println("here");
			if(!isRegistered) {
				KeyStore keystore = KeyStore.getInstance("JCEKS");
				FileInputStream keystore_fis = new FileInputStream(keystorePath);
				keystore.load(keystore_fis, keystorePassword.toCharArray());
				PrivateKey key = (PrivateKey) keystore.getKey(clientID, (clientID + ".key").toCharArray());
				Signature signature = Signature.getInstance("MD5withRSA");
				Certificate cert = keystore.getCertificate(clientID);
				signature.initSign(key);
				ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
			    buffer.putLong(nonce);
			    byte nonceBytes[] = buffer.array();
				signature.update(nonceBytes);
				outStream.writeLong(nonce);
				outStream.writeObject(nonceBytes);
				outStream.writeObject(signature.sign());
				outStream.writeObject(cert);
				
			}
			else {
				KeyStore keystore = KeyStore.getInstance("JCEKS");
				FileInputStream keystore_fis = new FileInputStream(keystorePath);
				keystore.load(keystore_fis, keystorePassword.toCharArray());
				PrivateKey key = (PrivateKey) keystore.getKey(clientID, (clientID + ".key").toCharArray());
				Signature signature = Signature.getInstance("MD5withRSA");
				signature.initSign(key);
				ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
			    buffer.putLong(nonce);
			    byte nonceBytes[] = buffer.array();
				signature.update(nonceBytes);
				outStream.writeLong(nonce);
				outStream.writeObject(nonceBytes);
				outStream.writeObject(signature.sign());
			}
			
			if(!(boolean)inStream.readObject()) {
				System.out.println("Ocorreu um erro na autenticacao");
				return;
			}
			else {
				System.out.println("Autenticacao bem sucedida");
			}
			//outStream.writeObject(password);
			
			//verifica se o servidor enviou sinal de password incorreta. Se sim, voltar a inserir
//			while(!inStream.readBoolean()) {
//				System.out.println((String) inStream.readObject());
//				try {					
//					password = sc.nextLine();
//					outStream.writeObject(password);
//				} catch (NoSuchElementException e) {
//					System.out.println("A encerrar servico...");
//				}
//			}

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
				else if(request.startsWith("s") || request.startsWith("sell") ||
						request.startsWith("s") || request.startsWith("sell")) {			
					KeyStore keystore = KeyStore.getInstance("JCEKS");
					FileInputStream keystore_fis = new FileInputStream(keystorePath);
					keystore.load(keystore_fis, keystorePassword.toCharArray());
					PrivateKey key = (PrivateKey) keystore.getKey("pedro", ("pedro" + ".key").toCharArray());
					Signature signature = Signature.getInstance("MD5withRSA");
					signature.initSign(key);
					byte[] data = request.getBytes();
					signature.update(data);
					outStream.writeObject(signature.sign());
				}
				reply = (String) inStream.readObject();
				System.out.println(reply);
				reply = (String) inStream.readObject();
				System.out.println(reply);
				try {
					request = sc.nextLine();					
					outStream.writeObject(request);
				} catch (NoSuchElementException e) {
					System.out.println("A encerrar servico...");
				}
			}
		} catch (IOException | ClassNotFoundException | KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
		}
		try {
			s.close();
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
		f = new File("./src/client/images/" + clientID + "/" + words[2]);
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
		}catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	private static void receiveImage(String request, ObjectInputStream inStream) {
		String[] words;
		File f;
		File dir;
		FileOutputStream fout = null;
		OutputStream output;
		byte[] buffer;
		int bytesRead = 0;

		words = request.split(" ");
		try {
			bytesRead = inStream.readInt();
			buffer = new byte[bytesRead];
			buffer = (byte[]) inStream.readObject();
			
			dir = new File("./src/client/images/" + clientID);
			dir.mkdirs();
			f = new File(dir.getPath() + "/" + words[1] + ".png");
			
			fout = new FileOutputStream(f);
			output = new BufferedOutputStream(fout);
			output.write(buffer, 0, bytesRead);
			
			output.close();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
}
