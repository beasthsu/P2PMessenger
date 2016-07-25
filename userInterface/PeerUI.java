package userInterface;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import net.peer.*;
import net.peer.audio.*;
import net.peer.whiteboard.WhiteboardUI;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import net.peer.video.*;
import net.peer.video.jmfdemo.FrameGrabberException;

public class PeerUI extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Container container;
    private JTextField nickNameTextField, serverAddrTextField, serverPortTextField;
    private JButton connectToServerBtn, sharedFile, phone, whiteBorad, video, selectPeer;
    private JTextArea messageTextArea, typeInTextArea;
    private DefaultListModel MActivePeerList, MGroupList;
    private JList JActivePeerList;
    
	private ArrayList<PeerInfo> activePeerList, groupList;
	private PeerInfo self;
	
    private ClientPart chatClient, clientPart;
    private ServerPart serverPart;
    
    private boolean callFlag = false;
    private boolean v_callFlag = false;
    private boolean magic_flag = true;
    private sender audioSender;
    private listener audioListener;
    private videoSender videoSender;
    private videoListener videoListener;
    
	public PeerUI() throws IOException {
	    super("Peer User Interface");
	    self = new PeerInfo();
	    serverModel();
	    
	    MActivePeerList = new DefaultListModel();
	    MGroupList = new DefaultListModel();
	    
	
	    setUpUIComponent();
	    setUpEventListener();
	    setVisible(true);
	    phone.setEnabled(false);
	    sharedFile.setEnabled(false);
	    //whiteBorad.setEnabled(false);
	    video.setEnabled(false);
	    int portOfListener = (int)(Math.random()*300+1200);
	    self.setPortOfListener(String.valueOf(portOfListener));
	    
	    int portOfVideoListener = (int)(Math.random()*300+2000);
	    self.setPortOfVideoListener(String.valueOf(portOfVideoListener));
	    
	    serverAddrTextField.setText("localhost");
	    serverPortTextField.setText("79");
	    activePeerList = new ArrayList<PeerInfo>();
	    groupList = new ArrayList<PeerInfo>();
	}
	private void serverModel() throws IOException{
		serverPart = new ServerPart();
		String port = serverPart.getPortOfServerPart();
	    self.setPortOfServerPart(port); 
		listenforPeer();
		
	}
	//boolean flag = true;
	private void listenforPeer(){
		//等待其它的peer連線。
		Thread thread;
		thread = new Thread(new Runnable() {
			public void run() {
				while(true){
					try {
						serverPart.waitForPeer();
						//flag = true;
						selectPeer.setEnabled(false);
						phone.setEnabled(true);
						sharedFile.setEnabled(true);
						whiteBorad.setEnabled(true);
						video.setEnabled(true);
				
						String Message, groupMessage;
						groupMessage = serverPart.getMessageFromPeer();
						setGroupList(groupMessage);
						if(!groupMessage.startsWith(self.getNickname()))
							buildRing(groupMessage);
           	        	while((Message = serverPart.getMessageFromPeer()) != null){
           	        		if(!Message.startsWith(self.getNickname())){
           	        			messageTextArea.append(Message + "\n");
           	        			messageTextArea.setCaretPosition(messageTextArea.getText().length());
           	        			clientPart.sendMessageToServer(Message);
           	        		}
           	        	}
					
					}
					catch(IOException e) {
						System.out.println("multi chat server: " + e.toString());
					}
				}
			}
		});
		thread.start();
	}
	private void setGroupList(String groupMessage){
		String[] tokens = groupMessage.split(":");
		int index;
		PeerInfo temp;
		
		MGroupList.clear();
		groupList.clear();
		for(int i=1; i<=(tokens.length - 1); i++){
			index = Integer.parseInt(tokens[i]);
			temp = activePeerList.get(index);
			groupList.add(temp);
			MGroupList.addElement(temp.getNickname());
		}
		this.repaint();
	}
	
	private void setUpUIComponent() {
		setSize(560, 415);
		this.setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		container = getContentPane();
		
		JPanel panelNorth = new JPanel();
		JPanel panelCenter = new JPanel();
		JPanel panelEAST = new JPanel();
		JPanel panelSouth = new JPanel();
		
        panelNorth.setLayout(new FlowLayout(FlowLayout.LEFT));
        panelNorth.add(new JLabel("Nickname"));
        panelNorth.add(nickNameTextField = new JTextField(5));
        panelNorth.add(new JLabel("Server IP"));
        panelNorth.add(serverAddrTextField = new JTextField(10));
        panelNorth.add(new JLabel("Server Port"));
        panelNorth.add(serverPortTextField = new JTextField(5));
        panelNorth.add(connectToServerBtn = new JButton("Connect"));
        
        panelCenter.setLayout(new FlowLayout(FlowLayout.LEFT));
        messageTextArea = new JTextArea(13, 33);
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setEditable(false);
        panelCenter.add(new JScrollPane(messageTextArea)); 
         
        typeInTextArea = new JTextArea(3, 33);
        typeInTextArea.setLineWrap(true);
        typeInTextArea.setEditable(false);
        panelCenter.add(new JScrollPane(typeInTextArea));
        
        panelEAST.setLayout(new BoxLayout(panelEAST, BoxLayout.Y_AXIS));
        panelEAST.add(new JLabel("Active peers"));
        
        JActivePeerList = new JList(MActivePeerList);
        JActivePeerList.setVisibleRowCount(6);
        JScrollPane scroll =new JScrollPane(JActivePeerList);
        scroll.setPreferredSize(new Dimension(170,100));
        panelEAST.add(scroll); 
        
        panelEAST.add(new JLabel("In Meetings"));
        JList JGroupList = new JList(MGroupList);
        JGroupList.setVisibleRowCount(6);
        scroll =new JScrollPane(JGroupList);
        scroll.setPreferredSize(new Dimension(170,100));
        panelEAST.add(scroll); 
        
        panelSouth.add(sharedFile = new JButton("SharedFile"));
        panelSouth.add(phone = new JButton("Phone"));
        panelSouth.add(whiteBorad = new JButton("WhiteBorad"));
        panelSouth.add(video = new JButton("Video"));
        //panelSouth.add();
        panelSouth.add(selectPeer = new JButton("Select Peer"));

        container.add(panelNorth, BorderLayout.NORTH);
        container.add(panelCenter, BorderLayout.CENTER);
        container.add(panelEAST, BorderLayout.EAST);
        container.add(panelSouth, BorderLayout.SOUTH);
        
    }
	int preLength = 1;
	private void maintainActivePeerList(String message){
		//維護Active Peer List的正確性。
		String[] tokens = message.split(":");
		String[] temps;
		
		MActivePeerList.clear();
		activePeerList.clear();
		for(int i=1; i<=(tokens.length - 1); i++){
			temps = tokens[i].split("/");
			activePeerList.add(new PeerInfo(temps[0],temps[1],temps[2],temps[3],temps[4]));
			MActivePeerList.addElement(temps[0]);
		}
		if(preLength != activePeerList.size()){
			if(groupList.size()!= 1){
				boolean allStillAlive = checkGroupMemberStillAlive();
				if(!allStillAlive){
					serverPart.closeSocket();
					audioListener.CloseList();
					audioSender.CloseRecord();
					if(v_callFlag == true){
						try {
							videoSender.stopVideo();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}	
					    System.out.println("Close Video");
					    video.setText("Video");
					    v_callFlag = false;
					}
					videoListener.stopVideo();
				    
					//flag = false;
					if(getElementIndex(groupList,self.getNickname()) == 0){
						String groupMessage = makeGroupMessage();
						try {
							buildRing(groupMessage);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			preLength = activePeerList.size();
		}
		this.repaint();
	}
	public boolean checkGroupMemberStillAlive(){
		boolean allStillAlive = true;
		Iterator <PeerInfo> iterator = groupList.iterator();
		ArrayList<PeerInfo> deadConnections = new ArrayList<PeerInfo>();
		PeerInfo temp;
		
		while(iterator.hasNext()) {
			temp = (PeerInfo) iterator.next();
			if(getElementIndex(activePeerList, temp.getNickname())== -1) {
				allStillAlive = false;
				deadConnections.add(temp);
				int index = getElementIndex(groupList,temp.getNickname());
				MGroupList.remove(index);
			}			
		}
		groupList.removeAll(deadConnections);
		return allStillAlive;
	}
	public static int getElementIndex(ArrayList<PeerInfo> array, String target) {
		int index = -1;
		Iterator <PeerInfo> iterator = array.iterator();
		PeerInfo temp;
		while(iterator.hasNext()) {
			temp = (PeerInfo) iterator.next();
			if( target.equals(temp.getNickname())) {
				index = array.indexOf(temp);
				break;
			}			
		}
		return index;
	}
	/*
	public void deleteFromGroupList(String item) {
		int indexOfMGroupList = MGroupList.indexOf(item);
		if(indexOfMGroupList >= 0){
			MGroupList.remove(indexOfMGroupList);
		}
	}*/
	private void setUpEventListener() {
		connectToServerBtn.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                    	connectToServerBtn.setEnabled(false);
                    	chatClient = new ClientPart();
               
                		messageTextArea.append("hello! every body...\n");
                		
                        new Thread(new Runnable(){
                            public void run() {
                            	try {
                          	        chatClient.connectToServer(serverAddrTextField.getText(), Integer.parseInt(serverPortTextField.getText()));
                          	        messageTextArea.append("hello! welcome you....\n");
                          	        typeInTextArea.setEditable(true);
                          	        
                          	        String nickname = nickNameTextField.getText();
                          	        String message = nickname+"/"+self.getPortOfServerPart()+"/"+self.getPortOfListener()+"/"+
                          	        				self.getPortOfVideoListener();
                          	        chatClient.sendMessageToServer(message);
                          	        
                          	        self.setNickname(nickname);
                          	        self.setIP(chatClient.getIP());
                          	      
                          	        groupList.add(self);
                          	        MGroupList.addElement(self.getNickname());
                          	        
                          	        int count = 1;
                          	        
                          	        String serverHashCode = chatClient.getServerMessage();
                          	        String serverMessage;
                          	        System.out.println(serverHashCode);
                          	        while((serverMessage = chatClient.getServerMessage()) != null) {
                          	      	    // The multi chat server's hascode is necessary for notify a subServer
                          	      	    // that this is a main server's command.
                          	        	if(serverMessage.startsWith(serverHashCode)) {
                          	        		// The main server want to quit this connection.
                          	        		if(serverMessage.endsWith("quit"))
                          	        			throw new IOException("The Server has dropped your connection....");
                          	        	}
                          	        	if(serverMessage.startsWith("activePeerList")){
                          	        		System.out.println(serverMessage+" "+count++);
                          	        		maintainActivePeerList(serverMessage);
                          	        	}else{
                
                          	        		messageTextArea.append(serverMessage + "\n");
                          	        		messageTextArea.setCaretPosition(messageTextArea.getText().length());
                          	        	}
                          	        }
                            	}
                            	catch(IOException ex) {
                            		chatClient.closeConnection();
                            		JOptionPane.showMessageDialog(null, ex.getMessage(),
                                            "info", JOptionPane.INFORMATION_MESSAGE);
                            		connectToServerBtn.setEnabled(true);
                            		typeInTextArea.setEditable(false);
                            	}
                            }
                        }).start();
                    }
                }
            );
		
		typeInTextArea.addKeyListener(
				new KeyListener() {
					public void keyPressed(KeyEvent e) {}
			        public void keyTyped(KeyEvent e) {}
			        public void keyReleased(KeyEvent e) {
			        	if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			        		String message = nickNameTextField.getText() + " > " + typeInTextArea.getText();
			        		typeInTextArea.setText("");
			        		messageTextArea.append(message);
			        		messageTextArea.setCaretPosition(messageTextArea.getText().length());
			        		clientPart.sendMessageToServer(message.replace('\n', ' '));
			        		//chatClient.sendMessageToServer(message.replace('\n', ' '));
			        	}
			        }
				}
			);
		selectPeer.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectPeer.setEnabled(false);
						

						if( !JActivePeerList.isSelectionEmpty()){
							buildGroupList();
							String groupMessage = makeGroupMessage();
							try {
								buildRing(groupMessage);
							} catch (NumberFormatException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
		);
		phone.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (callFlag == false) {
							audioSender.restart();
							phone.setText("disconnect");
							callFlag = true;
						} else {
							audioSender.stopRecord();
						    System.out.println("Close Connect");
						    phone.setText("phone");
						    callFlag = false;
						}
					}
				}
		);
		
		video.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (v_callFlag == false) {
							videoSender.startVideo();							
							video.setText("Close Video");
							v_callFlag = true;
						} else {
							try {
								videoSender.stopVideo();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}	
						    System.out.println("Close Video");
						    video.setText("Video");
						    v_callFlag = false;
						    videoListener.stopVideo();
						}
					}
				}
		);
		
		whiteBorad.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						new WhiteboardUI();
					}
				}
		);
	}
	private String makeGroupMessage(){
		Iterator <PeerInfo> iterator = groupList.iterator();
		PeerInfo temp;
		
		String groupMessage = self.getNickname();
		while(iterator.hasNext()) {
			temp = (PeerInfo) iterator.next();
			groupMessage = groupMessage+":"+getElementIndex(activePeerList, temp.getNickname());
		}
		return groupMessage;
	}
	private void buildGroupList(){
		int itemIndex = 0, index =0,inserIndex = 0,insert;
		Object[] items = JActivePeerList.getSelectedValues();
		int alreadyIn[] = new int[groupList.size()];
		
		Iterator <PeerInfo> iterator = groupList.iterator();
		PeerInfo temp;
		while(iterator.hasNext()) {
			temp = (PeerInfo) iterator.next();
			alreadyIn[index] = getElementIndex(activePeerList, temp.getNickname());
			index++;
		}
		index = 0; inserIndex =0;
		while(itemIndex < items.length){
			if(!MGroupList.contains(items[itemIndex].toString())){
				insert = getElementIndex(activePeerList,items[itemIndex].toString());
				if(insert < alreadyIn[index]){
					groupList.add(inserIndex,activePeerList.get(insert));
					MGroupList.add(inserIndex, activePeerList.get(insert).getNickname());
					itemIndex++;
				}else{
					if(index < alreadyIn.length -1){
						index++;
					}else{
						break;
					}
				}
				inserIndex++;
			}else{
				itemIndex++;
			}
		}
		while(itemIndex < items.length){
			insert = getElementIndex(activePeerList,items[itemIndex].toString());
			groupList.add(activePeerList.get(insert));
			MGroupList.addElement(activePeerList.get(insert).getNickname());
			itemIndex++;
		}
	}
	private void buildRing(String groupMessage) throws NumberFormatException, IOException{
		clientPart = new ClientPart();
		
		PeerInfo targetPeer;
		targetPeer = findNextPeer(groupList);
		System.out.println("connect to " + targetPeer.getNickname());
		
		clientPart.connectToServer(targetPeer.getIP(), Integer.parseInt(targetPeer.getPortOfServerPart()));
	    clientPart.sendMessageToServer(groupMessage);
	    
	    buildAudioRing(targetPeer);
	    buildVideoRing(targetPeer);
	}
	private void buildAudioRing(PeerInfo targetPeer){
		int identifer = getElementIndex(activePeerList, self.getNickname());
		
		// collection IP and connection IP use Port 1200 UDP protocol 
		audioSender = new sender(targetPeer.getIP(), Integer.parseInt(targetPeer.getPortOfListener())
								, identifer);
		audioSender.start();
		
        System.out.println("Connection to: " + targetPeer.getNickname());
		
		audioListener = new listener(Integer.parseInt(self.getPortOfListener()),identifer,audioSender);
		audioListener.start(); 
	}
	private void buildVideoRing(PeerInfo targetPeer){
		int identifer = getElementIndex(activePeerList, self.getNickname());
		
		if(magic_flag)
			videoSender = new videoSender(targetPeer.getIP(), Integer.parseInt(targetPeer.getPortOfVideoListener())
								, identifer, video);
		else
			videoSender.setTarget(targetPeer.getIP(), Integer.parseInt(targetPeer.getPortOfVideoListener()));
		magic_flag = false;
		videoListener = new videoListener(Integer.parseInt(self.getPortOfVideoListener()),identifer, videoSender, video);
		videoListener.startVideo();
        System.out.println("Connection to: " + targetPeer.getNickname());
	}	
	
	private PeerInfo findNextPeer(ArrayList<PeerInfo> array){
		int index = -1,size;
		size = array.size();
		Iterator <PeerInfo> iterator = array.iterator();
		PeerInfo temp;
		while(iterator.hasNext()) {
			temp = (PeerInfo) iterator.next();
			if( (self.getNickname()).equals(temp.getNickname())) {
				index = array.indexOf(temp);
				break;
				
			}			
		}
		index = (index+1)%size;
		return array.get(index);
	}
	/*private void connectToPeer(String ip, int port) throws IOException{

			SimpleConnectionClient client;
			client = new SimpleConnectionClient();
			client.connectToServer(ip, port);
			
  	        client.sendMessageToServer("request groupList");
  	        String list = client.getServerMessage();
  	        String[] tokens = list.split(":");
  	        String[] temps;
  	        
  	        for(int i=1; i<=(tokens.length - 1); i++){
  	        	temps = tokens[i].split("/");
  	        	activePeerList.add(new PeerInfo(temps[0],temps[1],temps[2]));
  	        	MActivePeerList.addElement(temps[0]);
  	        }
    	
    }*/
	public static void main(String[] args) throws IOException {
		new PeerUI();
	}
}
