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
				else{
					String user = mDecoded.split(":")[0];
					String uToken = users.get(user);
					
					if(uToken.equals(mDecoded)){
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
		
		File video = new File("data/buddha.mp4");
		UDP streamer = new UDP(63491, video);
		streamer.start();
		
	}
	
	
	public static void listenUser(InetAddress ip){
		if (!users_listening.contains(ip)){
			users_listening.add(ip);
		}
	}
	
	public static List<InetAddress> getBroadcastGroup(){
		return users_listening;
	}
	
	
	public interface OnMessageReceived {
		public String messageReceived(String type, String message, String ip, int port);
	}

}
