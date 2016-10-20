package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.CSVUtils;

public class Lab6Server {
	
	private static final String history = "data/history/log.csv";
	private static final Map<String, String> users = new HashMap<>();
	private static final Map<Integer, UDP> streamers = new HashMap<>();
	
	private static final List<UDP> video_playlist = new ArrayList<>();

	public static void main(String[] args) throws Exception {
		
		File f = new File(history);
		boolean newF = false;
		if (!f.exists()){
			f.createNewFile();
			newF = true;
		}
		final FileWriter writer = new FileWriter(f, true);

		if (newF){
			CSVUtils.writeLine(writer, Arrays.asList("LogDate", "Protocol", "Message", "IP", "Port"));
			writer.flush();
		}

		OnMessageReceived tcpAuthHandler = new OnMessageReceived() {
			@Override
			public String messageReceived(String type, String message, String ip, int port) {
				String mDecoded = new String(Base64.getDecoder().decode(message));
				
				try {
					String log = mDecoded.replace(",", "-");
					CSVUtils.writeLine(writer, Arrays.asList(new Date().toString(), type, log, ip, port+""));
					writer.flush();	
				} catch (Exception e) {
					try {
						String log = e.getMessage();
						CSVUtils.writeLine(writer, Arrays.asList(new Date().toString(), "ERROR", log));
						writer.flush();	
					} catch (Exception e2) {
						e.printStackTrace();
					}
				
				}
				
				if (mDecoded.contains("REGISTER")){
					String[] data = mDecoded.split(":");
					String username = data[1];
					String pass = data[2];
					String noAuth = username+":"+pass;
					
					String token = Base64.getEncoder().encodeToString(noAuth.getBytes());
					users.put(username, token);
					
					try {
						String log = "User register succesfull";
						CSVUtils.writeLine(writer, Arrays.asList(new Date().toString(), "SUCCESS", log));
						writer.flush();
					} catch (IOException e) {
						try {
							String log = e.getMessage();
							CSVUtils.writeLine(writer, Arrays.asList(new Date().toString(), "ERROR", log));
							writer.flush();	
						} catch (Exception e2) {
							e.printStackTrace();
						}
					}
						
					
					return token;
				}
				else if(mDecoded.contains("PLAYLIST")) {
					return playlist();
				}
				else if(mDecoded.contains("PORT")){
					String video_name = mDecoded.split(":")[1];
					int stream_port = getStreamPort(video_name);
					if (stream_port>0){
						listenUser(InetAddress.getByName(ip), ""+stream_port);
						return "PORT:"+stream_port;
					}
					else{
						return "ERROR: Video not found";
					}
				}
				else if(mDecoded.contains("PLAY")){
					String p = mDecoded.split(":")[1];
					int _port = Integer.parseInt(p);
					if (streamers.containsKey(_port)){
						streamers.get(_port).play();
					}
					else{
						return "ERROR: Video not found";
					}
				}
				else if(mDecoded.contains("PAUSE")){
					String p = mDecoded.split(":")[1];
					int _port = Integer.parseInt(p);
					if (streamers.containsKey(_port)){
						streamers.get(_port).pause();
					}
					else{
						return "ERROR: Video not found";
					}
				}
				else{
					String user = mDecoded.split(":")[0];
					String uToken = users.get(user);
					
					if(uToken.equals(message)){
						try {
							String log = "User login succesfull";
							CSVUtils.writeLine(writer, Arrays.asList(new Date().toString(), "SUCCESS", log));
							writer.flush();
						} catch (IOException e) {
							try {
								String log = e.getMessage();
								CSVUtils.writeLine(writer, Arrays.asList(new Date().toString(), "ERROR", log));
								writer.flush();	
							} catch (Exception e2) {
								e.printStackTrace();
							}
						}
						
						return "OK";
					}
					
					try {
						String log = "User login failed";
						CSVUtils.writeLine(writer, Arrays.asList(new Date().toString(), "ERROR", log));
						writer.flush();
					} catch (IOException e) {
						try {
							String log = e.getMessage();
							CSVUtils.writeLine(writer, Arrays.asList(new Date().toString(), "ERROR", log));
							writer.flush();	
						} catch (Exception e2) {
							e.printStackTrace();
						}
					}
					
					return "Error: Login not succesfull";
				}
				
			}
		};
		
		TCPReceiver tcp = new TCPReceiver(tcpAuthHandler);
		tcp.start();
		
		File video1 = new File("data/avatar.mp4");
		UDP streamer1 = new UDP(63491, video1);
		streamer1.start();
		video_playlist.add(streamer1);
		streamers.put(63491, streamer1);
		
		File video2 = new File("data/buddha.mp4");
		UDP streamer2 = new UDP(63492, video2);
		streamer2.start();
		video_playlist.add(streamer2);
		streamers.put(63492, streamer2);
		
		File video3 = new File("data/quantum.mp4");
		UDP streamer3 = new UDP(63493, video3);
		streamer3.start();
		video_playlist.add(streamer3);
		streamers.put(63493, streamer2);
		
		
	}
	
	
	public static void listenUser(InetAddress ip, String p){
		int port = Integer.parseInt(p);
		if (!streamers.containsKey(port)){
			streamers.get(port).registerUser(ip);
		}
	}
	public static void stopListeningUser(InetAddress ip, String p){
		int port = Integer.parseInt(p);
		if (!streamers.containsKey(port)){
			streamers.get(port).unregisterUser(ip);
		}
	}
	
	public static String playlist(){
		String play = "PlAYLIST";
		for (UDP stream : video_playlist) {
			play+=(":"+stream.name());
		}
		return play;
	}
	
	public static int getStreamPort(String name){
		for (UDP stream : video_playlist) {
			if (stream.name().equals(name)){
				return stream.port();
			}
		}
		return -1;
	}
	
	public interface OnMessageReceived {
		public String messageReceived(String type, String message, String ip, int port);
	}

}
