package net.server;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;

public class MultiChatServer {
	private ServerSocket serverSocket;
    private List<MultiChatSubServer> activePeerList;
    
    public MultiChatServer(int serverPort) throws IOException {
    	serverSocket = new ServerSocket(serverPort);
    	activePeerList = new ArrayList<MultiChatSubServer>();
    	log("multi chat server start up....");
    }
    
    // Wait for next client.
    // If there's one, add it to the client list and start a subServer thread for it.
    public void waitForNextClient() throws IOException {
    	MultiChatSubServer subServer = 
    		new MultiChatSubServer(serverSocket.accept(), this);//���ݷspeer���s�u�C	
    	addSubServer(subServer); //��speer�[�JArrayList
    	Thread thread = new Thread(subServer);
    	thread.setDaemon(true); //�I������
    	thread.start();
    	
    	broadCastActiveListToClient();	//�q���Ҧ�peers��peer login
    }
        
    public void removeDeadConnections() {
    	//�ˬd�Ҧ��w�_�u��Peers
    	Iterator<MultiChatSubServer> iterator = activePeerList.iterator();
    	List<MultiChatSubServer> deadConnections = new ArrayList<MultiChatSubServer>();
    	MultiChatSubServer subServer;
    	
    	while(iterator.hasNext()) {
    		subServer = (MultiChatSubServer) iterator.next();
    		if(subServer.isClosed())
    			deadConnections.add(subServer);
    	}
    	
    	removeSubServers(deadConnections);
    	broadCastActiveListToClient();//��w�_�u��peers�ǵ��j�a�C
    }

    public Iterator<MultiChatSubServer> getActivePeerList() {
    	removeDeadConnections();
    	return activePeerList.iterator();
    }
    
    // Remove the client According to the client number
    public void removeClientAccordingTo(int number) {
    	MultiChatSubServer subServer = (MultiChatSubServer) activePeerList.get(number);
    	shutdownSubServer(subServer);
    	synchronized(activePeerList){
    		activePeerList.remove(number);
    	}
    	log(subServer.getName() + ": removed");
    }
    
    // Broadcast to Peers
    public void broadCastToClient(String message) {
    	Iterator<MultiChatSubServer> iterator = activePeerList.iterator();
    	MultiChatSubServer subServer;
    	while(iterator.hasNext()) {
    		subServer = (MultiChatSubServer) iterator.next();
    	    subServer.sendMessageToClient(message);
    	}
    	log(message);
    }

    public void broadCastActiveListToClient(){
    	//�إ�Active list���ʥ]
    	Iterator<MultiChatSubServer> iterator = activePeerList.iterator();
    	MultiChatSubServer subServer;
    	String list = "activePeerList";
    	while(iterator.hasNext()) {
    		subServer = (MultiChatSubServer) iterator.next();
    		list = list +":"+subServer.getNickname()+"/"+subServer.getIPofPeer()
    		+"/"+subServer.getPortOfServerPart()+"/"+subServer.getPortOfListener()+
    		"/"+subServer.getPortOfVideoListener();
    	}
    	
    	System.out.println(list);
    	broadCastToClient(list);
    }
    
    // Shutdown all subServers first.
    // Then shutdown the multi chat server.
    public void shutdown() {
    	Iterator<MultiChatSubServer> iterator = activePeerList.iterator();
    	MultiChatSubServer subServer;
    	while(iterator.hasNext()) {
    		subServer = (MultiChatSubServer) iterator.next();
    		shutdownSubServer(subServer);
    	}
    	removeSubServers(activePeerList);
    	
    	try {
            serverSocket.close();
    	}
    	catch(IOException e) {
    		log("server shutdown: " + e);
    	}  
    	
    	log("multi chat server shutdown ok...");
    }
    
    public Object log(Object o) {
    	System.out.println(o);
    	return null;
    }
    
    private void addSubServer(MultiChatSubServer subServer) {
    	synchronized(activePeerList){
    		activePeerList.add(subServer);
    	}
    	log(subServer.getName() + ": added");
    }
    
    public void removeSubServers(List<MultiChatSubServer> subServers) {
    	Iterator<MultiChatSubServer> iterator = subServers.iterator();
    	MultiChatSubServer subServer;
    	while(iterator.hasNext()) {
    		subServer = (MultiChatSubServer) iterator.next();
    	    log(subServer.getName() + ": removed");
    	}
    	synchronized(activePeerList){
    		activePeerList.removeAll(subServers);
    	}
    }
    
    private void shutdownSubServer(MultiChatSubServer subServer) {
    	// The multi chat server's hascode is necessary for notify a subServer
    	// that this is a main server's command.
    	subServer.sendMessageToClient(this.hashCode() + ":quit");
    	subServer.shutdown();
    }
}