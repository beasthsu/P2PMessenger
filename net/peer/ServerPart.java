package net.peer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerPart {
	private ServerSocket serverSocket;
	private BufferedReader peerReader;
	private PrintStream peerWriter;
	Socket socket;
    
    public ServerPart() throws IOException {
    	serverSocket = new ServerSocket((int)(Math.random()*300+1));
    }
    
    public String getPortOfServerPart(){
    	return String.valueOf(serverSocket.getLocalPort());
    }
    
    public void waitForPeer() throws IOException {
    	socket = serverSocket.accept(); //等待新peer的連線。
    	peerReader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
    	peerWriter = new PrintStream(socket.getOutputStream());
    }
    public void closeSocket(){
    	try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public String getMessageFromPeer() throws IOException{
    	return peerReader.readLine();
    }
    
    public void sendMessageToPeer(String message) {
		peerWriter.println(message);
	}
}