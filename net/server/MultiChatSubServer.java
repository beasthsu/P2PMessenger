package net.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MultiChatSubServer implements Runnable {
	private Socket socket;
	private String portOfServerPart;
	private String portOfListener;
	private String portOfVideoListener;
	private String nickname;
	
	private MultiChatServer parent;
	
	private BufferedReader clientReader;
	private PrintStream clientWriter;
	
	public MultiChatSubServer(Socket socket, MultiChatServer parent) throws IOException {
		this.socket = socket;
		this.parent = parent;
		
		portOfServerPart = "";
		nickname = "";
		portOfListener = "";
		portOfVideoListener = "";
		clientReader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
    	clientWriter = new PrintStream(socket.getOutputStream());
    	
    	// Send a identification of the server to client.
    	// It's used for the server's command to client.
    	// Here, I just use the hashcode of multi chat server.
    	sendMessageToClient("" + parent.hashCode());
	}
	
	// Get SubServer's name.
	// Here, I'm using the client's address. 
	public String getName() {
		return socket.getRemoteSocketAddress().toString();
	}
	
	public String getNickname(){
		while(nickname.isEmpty()){
			//wait until value ready.
		}
		return nickname;
	}
	
	public String getIPofPeer(){
		//�^��peer��IP
		String temp = socket.getRemoteSocketAddress().toString();
		String[] tokens = temp.split(":");
		temp = tokens[0].substring(1);
		return temp;
	}
	
	public String getPortOfServerPart(){
		//���oPeer Server�ݪ�Port number
		while(portOfServerPart.isEmpty()){
			//wait until value ready.
		}
		return portOfServerPart;
	}
	public String getPortOfListener(){
		while(portOfListener.isEmpty()){
			//wait until value ready.
		}
		return portOfListener;
	}
	public String getPortOfVideoListener(){
		while(portOfVideoListener.isEmpty()){
			//wait until value ready.
		}
		return portOfVideoListener;
	}
	public void run() {
		try {
			String message;
			
			message = clientReader.readLine();
			String[] tokens = message.split("/");
			
			//�opeer���o���T����
			nickname = tokens[0];
			portOfServerPart = tokens[1];
			portOfListener = tokens[2];
			portOfVideoListener = tokens[3];
			
			// Get the client's message and then broadcase it.
			while((message = clientReader.readLine()) != null) {
				parent.broadCastToClient(message);
				parent.log(getName() +  ": " + message);
			}
		}
		catch(IOException e) {
			parent.log(getName() +  ": " + e);
			
			//�q���Ҧ�peers��peer logout
			List<MultiChatSubServer> deadConnections = new ArrayList<MultiChatSubServer>();
			deadConnections.add(this);
			parent.removeSubServers(deadConnections);
			parent.broadCastActiveListToClient();
		}
		finally {
			shutdown();
		}
	
	}
	
	public void sendMessageToClient(String message) {
		clientWriter.println(message);
	}
	
	public boolean isClosed() {
		return socket.isClosed();
	}
	
    public void shutdown() {
    	try {
            socket.close();
    	}
    	catch(IOException e) {
    		parent.log(getName() +  ": " + e);
    	}    	
    }
    
}
