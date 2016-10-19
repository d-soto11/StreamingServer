package main;

public class Lab6Server {

	public static void main(String[] args) {

	}
	
	
	public interface OnMessageReceived {
		public void messageReceived(String type, String message, String ip, int port);
	}

}
