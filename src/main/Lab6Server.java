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
	private static final List<InetAddress> users_listening = new ArrayList<>();
	
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
//				else if(){
//					
//				}
				else{
					System.out.println("decoded:"+mDecoded);
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
		
//		File video1 = new File("data/avatar.mp4");
//		UDP streamer1 = new UDP(63491, video1);
//		streamer1.start();
//		video_playlist.add(streamer1);
//		
//		File video2 = new File("data/buddha.mp4");
//		UDP streamer2 = new UDP(63491, video2);
//		streamer2.start();
//		video_playlist.add(streamer2);
//		
//		File video3 = new File("data/quantum.mp4");
//		UDP streamer3 = new UDP(63491, video3);
//		streamer3.start();
//		video_playlist.add(streamer3);
		
		
	}
	
	
	public static void listenUser(InetAddress ip){
		if (!users_listening.contains(ip)){
			users_listening.add(ip);
		}
	}
	
	public static List<InetAddress> getBroadcastGroup(){
		return users_listening;
	}
	
	public static String playlist(){
		String play = "PlAYLIST";
		for (UDP stream : video_playlist) {
			play+=(":"+stream.name());
		}
		return play;
	}
	
	
	public interface OnMessageReceived {
		public String messageReceived(String type, String message, String ip, int port);
	}

}
