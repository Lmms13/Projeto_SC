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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Tintolmarket {

	private static Charset charset = StandardCharsets.ISO_8859_1;

	static Scanner sc = new Scanner(System.in);
	private static String clientID;

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

		ObjectInputStream inStream = null;
		ObjectOutputStream outStream = null;
		System.setProperty("javax.net.ssl.trustStore", truststorePath);

		SocketFactory sf = SSLSocketFactory.getDefault();
		SSLSocket cSoc = null;
		try {
			cSoc = (SSLSocket) sf.createSocket(host, port);
			outStream = new ObjectOutputStream(cSoc.getOutputStream());
			inStream = new ObjectInputStream(cSoc.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			outStream.writeObject(clientID);

			long nonce = (long) inStream.readObject();
			boolean isRegistered = (boolean) inStream.readObject();

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

			String reply = "";
			String request = "";

			//realiza o ciclo de interacao: menu->resposta do servidor->pedido do cliente
			while(true){
				if(request.startsWith("a") || request.startsWith("add")) {
					sendImage(request, outStream);
				}
				else if(request.startsWith("v") || request.startsWith("view")) {
					receiveImage(request, inStream);
				}
				else if(request.startsWith("s") || request.startsWith("sell") ||
						request.startsWith("b") || request.startsWith("buy")) {			
					KeyStore keystore = KeyStore.getInstance("JCEKS");
					FileInputStream keystore_fis = new FileInputStream(keystorePath);
					keystore.load(keystore_fis, keystorePassword.toCharArray());
					keystore_fis.close();

					PrivateKey key = (PrivateKey) keystore.getKey(clientID, (clientID + ".key").toCharArray());
					Signature signature = Signature.getInstance("MD5withRSA");
					signature.initSign(key);
					byte[] data = request.getBytes();
					signature.update(data);
					outStream.writeObject(signature.sign());
				}

				//le resposta do servidor
				reply = (String) inStream.readObject();

				if(request.startsWith("r") || request.startsWith("read")) {
					if(!reply.equals("Nao tem mensagens para ler")) {
						KeyStore keystore = KeyStore.getInstance("JCEKS");
						FileInputStream keystore_fis = new FileInputStream(keystorePath);
						keystore.load(keystore_fis, keystorePassword.toCharArray());
						keystore_fis.close();

						PrivateKey key = (PrivateKey) keystore.getKey(clientID, (clientID + ".key").toCharArray());
						Cipher c = Cipher.getInstance("RSA");
						StringBuilder sb = new StringBuilder();
						StringBuilder enc = new StringBuilder();
						String[] lines = reply.split(System.getProperty("line.separator"));

						for(String line : lines) {
							if(line.startsWith("---") && enc.isEmpty()) {
								sb.append(line + System.getProperty("line.separator"));
							}
							else if(line.startsWith("---") && !enc.isEmpty()) {
								String encripted = enc.toString();
								enc.setLength(0);	
								c.init(Cipher.DECRYPT_MODE, key);
								sb.append(new String(c.doFinal(encripted.getBytes(charset)), charset) + System.getProperty("line.separator"));
								sb.append(line + System.getProperty("line.separator"));
							}
							else {
								enc.append(line);
							}
						}
						reply = sb.toString();
					}
				}
				System.out.println(reply);
				reply = (String) inStream.readObject();
				System.out.println(reply);
				try {
					request = sc.nextLine();
					if(request.startsWith("t") || request.startsWith("talk")) {
						String[] splitRequest = request.split(" ");
						KeyStore truststore = KeyStore.getInstance("JCEKS");
						FileInputStream truststore_fis = new FileInputStream(truststorePath);
						truststore.load(truststore_fis, keystorePassword.toCharArray());
						truststore_fis.close();

						PublicKey key = (PublicKey) truststore.getCertificate(splitRequest[1]).getPublicKey();
						Cipher c = Cipher.getInstance("RSA");
						c.init(Cipher.ENCRYPT_MODE, key);
						
						String message = String.join(" ", Arrays.copyOfRange(splitRequest, 2, splitRequest.length));
						String encrypted = new String(c.doFinal(message.getBytes(charset)), charset);	

						request = splitRequest[0] + " " + splitRequest[1] + " " + encrypted;
					}
					outStream.writeObject(request);
				} catch (NoSuchElementException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();
					System.out.println("A encerrar servico...");
				}
			}
		} catch (IOException | ClassNotFoundException | KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | InvalidKeyException | SignatureException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
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