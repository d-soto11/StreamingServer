package main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class UDP extends Thread{
	
	private File video_file;
	
	private int broadcast_port;
	
	private List<InetAddress> users_listening;
	
	public int port(){return broadcast_port;}
	public String name(){return video_file.getName();}

	public UDP(int port, File f){
		this.video_file = f;
		broadcast_port = port;
		users_listening = new ArrayList<>();
	}
	
	public void registerUser(InetAddress ip){
		users_listening.add(ip);
	}
	public void unregisterUser(InetAddress ip){
		users_listening.remove(ip);
	}

	@Override
	public void run() {
		try{
			
			byte[] video_chunk = new byte[1024];
			
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(video_file));
			
			DatagramSocket serverSocket = new DatagramSocket();
			@SuppressWarnings("unused")
			int chunk_size = 0;
			while((chunk_size = bis.read(video_chunk)) > 0)
			{
				for (InetAddress client : users_listening) {
					DatagramPacket sendPacket =
							new DatagramPacket(video_chunk, video_chunk.length, client, broadcast_port);
					serverSocket.send(sendPacket);
				}
			}
			bis.close();
			serverSocket.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void play(){
		
	}
	public void pause(){
		
	}
}
