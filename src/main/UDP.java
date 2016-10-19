package main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class UDP extends Thread{
	
	private File video_file;
	
	private int broadcast_port;

	public UDP(int port, File f){
		this.video_file = f;
		broadcast_port = port;
	}

	@Override
	public void run() {
		try{
			
			byte[] video_chunk = new byte[1024];
			
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(video_file));
			
			DatagramSocket serverSocket = new DatagramSocket(9898);
			@SuppressWarnings("unused")
			int chunk_size = 0;
			while((chunk_size = bis.read(video_chunk)) > 0)
			{
				List<InetAddress> listeners = Lab6Server.getBroadcastGroup();
				for (InetAddress client : listeners) {
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
}
