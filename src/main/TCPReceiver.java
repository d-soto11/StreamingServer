package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import main.Lab6Server.OnMessageReceived;


public class TCPReceiver extends Thread{
	
	public static final int SERVERPORT = 9090;
	private OnMessageReceived messageListener;
	
	public TCPReceiver(OnMessageReceived handler) {
		this.messageListener = handler;
	}
	
	@SuppressWarnings("resource")
	@Override
	public void run() {
		super.run();
		System.out.println("Starting TCP server...");
		
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(SERVERPORT);
		} catch (IOException e) {
			System.out.println("Error starting TCP Server");
			e.printStackTrace();
		}

		while (true){
			try {
				Socket client = serverSocket.accept();
				System.out.println("New connection attempt...");
				TCP mServer = new TCP(client, messageListener);
				mServer.start();
				System.out.println("Client with IP " + client.getInetAddress().getHostAddress() + " accepted");
			} catch (IOException e) {
				System.out.println("Error starting client connection");
				e.printStackTrace();
			}
		}
	}
	
	public static void downTCPServer(ServerSocket ss){
		try {
			ss.close();
		} catch (IOException e) {
			System.err.println("Error closing TCP socket");
			e.printStackTrace();
		}
	}

}
