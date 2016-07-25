package net.peer;

import java.net.*;
import java.io.*;

public class ClientPart {
	private Socket socket;
    private BufferedReader serverReader;
    private PrintStream serverWriter;
	
	public void connectToServer(String serverAddr, int serverPort) throws IOException {
		InetAddress serverInetAddr = InetAddress.getByName(serverAddr); 
		socket = new Socket(serverInetAddr, serverPort);
		
		serverReader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
    	serverWriter = new PrintStream(socket.getOutputStream());
	}
	
	public String getIP(){
		String temp =socket.getLocalAddress().toString().substring(1);
		return temp;
	}
    public String getServerMessage() throws IOException {
    	return serverReader.readLine();
    }
    
    public void sendMessageToServer(String message) {
    	serverWriter.println(message);
    }
    
    public void closeConnection() {
    	try {
            socket.close();
    	}
    	catch(IOException e) {
    		
    	}    	
    }
}