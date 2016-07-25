package net.peer;

public class PeerInfo{
	private String nickname;
	private String ip;
	private String portOfServerPart;
	private String portOfListener;
	private String portOfVideoListener;
	
	public PeerInfo(){
		this("","","","","");
	}
	public PeerInfo(String nickname, String ip, String portOfServerPart,
					String portOfListener,String portOfVideoListener){
		this.nickname = nickname;
		this.ip = ip;
		this.portOfServerPart = portOfServerPart;
		this.portOfListener = portOfListener;
		this.portOfVideoListener = portOfVideoListener;
	}
	public void setNickname(String nickname){
		this.nickname = nickname;
	}
	public void setIP(String ip){
		this.ip = ip;
	}
	public void setPortOfServerPart(String portOfServerPart){
		this.portOfServerPart = portOfServerPart;
	}
	public void setPortOfListener(String portOfListener){
		this.portOfListener = portOfListener;
	}
	public void setPortOfVideoListener(String portOfVideoListener){
		this.portOfVideoListener = portOfVideoListener;
	}
	public String getNickname(){
		return nickname;
	}
	public String getIP(){
		return ip;
	}
	public String getPortOfServerPart(){
		return portOfServerPart;
	}
	public String getPortOfListener(){
		return portOfListener;
	}
	public String getPortOfVideoListener(){
		return portOfVideoListener;
	}
	public void printInfo(){
		System.out.println("Nickname:"+nickname+" IP:"+ip+" Port of server part:"+portOfServerPart+
				" Audio listener port:"+portOfListener+" video listener port:"+portOfVideoListener);
	}
}