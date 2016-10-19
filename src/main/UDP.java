package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import main.Lab6Server.OnMessageReceived;

public class UDP extends Thread{

	private OnMessageReceived messageListener;

	public UDP(OnMessageReceived messageListener){
		this.messageListener = messageListener;
	}

	@Override
	public void run() {
		try{
			System.out.println("Starting UDP server...");
			@SuppressWarnings("resource")
			DatagramSocket serverSocket = new DatagramSocket(9898);
			byte[] receiveData = new byte[1024];
			while(true)
			{
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				String sentence = new String( receivePacket.getData());
				if (sentence != null && messageListener != null) {
					messageListener.messageReceived("UDP", sentence, receivePacket.getAddress().toString(), receivePacket.getPort());
					byte [] sendData =  new byte[1024];
					sendData = "OK".getBytes();
					DatagramPacket sendPacket =
							new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
					serverSocket.send(sendPacket);
					System.out.println("OK");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
