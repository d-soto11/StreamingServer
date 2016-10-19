package main;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Lab6Server {
	
	private static final Map<String, String> users = new HashMap<>();

	public static void main(String[] args) {

		OnMessageReceived tcpAuthHandler = new OnMessageReceived() {
			@Override
			public String messageReceived(String type, String message, String ip, int port) {
				if (message.contains("Register")){
					String[] data = message.split(":");
					String username = data[1];
					String pass = data[2];
					String noAuth = username+":"+pass;
					
					String token = Base64.getEncoder().encodeToString(noAuth.getBytes());
					users.put(username, token);
					
					return token;
				}
				else{
					String mDecoded = new String(Base64.getDecoder().decode(message));
					String user = mDecoded.split(":")[0];
					String uToken = users.get(user);
					
					if(uToken.equals(mDecoded)){
						return "OK";
					}
					
					return "Error: Login not succesfull";
				}
				
			}
		};
		
		TCPReceiver tcp = new TCPReceiver(tcpAuthHandler);
		tcp.start();
		
	}
	
	
	
	
	public interface OnMessageReceived {
		public String messageReceived(String type, String message, String ip, int port);
	}

}
