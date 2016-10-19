package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import main.Lab6Server.OnMessageReceived;

public class TCP extends Thread {

	private PrintWriter mOut;
	private OnMessageReceived messageListener;
	private Socket client;

	/**
	 * Constructor of the class 2
	 * @param messageListener listens for the messages
	 */
	public TCP(Socket client, OnMessageReceived messageListener) {
		this.client = client;
		this.messageListener = messageListener;
	}

	/**
	 * Method to send the messages from server to client
	 * @param message the message sent by the server
	 */
	public void sendMessage(String message){
		if (mOut != null && !mOut.checkError()) {
			mOut.println(message);
			mOut.flush();
		}
	}


	@Override
	public void run() {
		super.run();
		try {
			mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String message = in.readLine();
			while (!message.equals("exit")) {
				if (message != null && messageListener != null) {
					messageListener.messageReceived("TCP", message, client.getInetAddress().toString(), client.getPort());
					mOut.println("OK");
					System.out.println("OK");
				}
				message = in.readLine();

			}
		} catch (Exception e){
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				System.out.println("S: Error closing client");
				e.printStackTrace();
			}
			System.out.println("Client with IP:" + client.getInetAddress().getHostAddress() + " disconnected");
		}

	}
}
