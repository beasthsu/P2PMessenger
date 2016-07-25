package userInterface;

import net.server.*;

import java.io.*;
import java.util.*;

public class MultiChatServerInterface {
	public static void main(String[] args) throws IOException {
		
		// Launch the multi chat server.
		final MultiChatServer multiChatServer = 
			new MultiChatServer(79);
		
		Thread thread;
		
		// Start to wait for clients
		thread = new Thread(new Runnable() {
			public void run() {
				try {
					while(true) {
				        multiChatServer.waitForNextClient();
					}
				}
				catch(IOException e) {
					System.out.println("multi chat server: " + e.toString());
				}
			}
		});
		thread.setDaemon(true);
		thread.start();

		// Rember, you should check every connection is alive in a regular time.
		// Remove the connection if it's dead.  
		thread = new Thread(new Runnable() {
			public void run() {
				try {
					while(true) {
					    Thread.sleep(50000);
				        multiChatServer.removeDeadConnections();
					}
				}
				catch(InterruptedException e) {
					System.out.println("server: " + e.toString());
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
		
		// Console management for the administrator.
		BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
		
		String adminMessage;
		
		while((adminMessage = buf.readLine()) != null) {
			
			if(adminMessage.equals("shutdown")) {
				// Shutdown the multi chat server.
				multiChatServer.shutdown();
				break;
			}
			else if(adminMessage.equals("list")) {
				// List all clients.
				Iterator<MultiChatSubServer>  iterator = multiChatServer.getActivePeerList();
				for(int i = 0; iterator.hasNext(); i++) {
					System.out.println(i + ": " + ((MultiChatSubServer) iterator.next()).getName());
				}
			}
			else if(adminMessage.startsWith("remove")) {
				// Remove the client according client numbers of input.
				String[] arguments = adminMessage.split(" ");
				for(int i = 1; i < arguments.length; i++) {
					multiChatServer.removeClientAccordingTo(Integer.parseInt(arguments[i]));
				}
			}
			else {
				// It's just a administrator's broadcast message.
				multiChatServer.broadCastToClient(adminMessage);
			}
		}
	}
}
