package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import main.Lab6Server.OnMessageReceived;


public class TCPReceiver extends Thread{

	public static final int SERVERPORT = 55055;
	private OnMessageReceived messageListener;

	private static final int queue_max_size = 1000;
	private static int queue_size = 0;
	private static double response_time = 1000;

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
				while (queue_size < queue_max_size){
					long t1 = System.currentTimeMillis();
					Socket client = serverSocket.accept();
					queue_size++;
					TCP mServer = new TCP(client, messageListener);
					mServer.start();
					response_time = 0.85*response_time + 0.15*(System.currentTimeMillis()-t1);
				}
				yield();
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

	public static void downClient(){
		queue_size--;
	}

}
