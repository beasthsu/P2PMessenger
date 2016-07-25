package net.peer.video;
// David Chamulak
// 3/3/03
// Simple program to show the Use of the a WebCam in Java
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;

import net.peer.video.jmfdemo.*;

public class videoSender extends JFrame {
	/**
	 * 
	 */
	private Thread thread;
	
	private static final long serialVersionUID = 1L;
	private BufferedImage image1;
	private FrameGrabber vision1;
	private int targetPort;
	private String targetIP;
	private int Identifier;
	private boolean isOpen;
	public DatagramSocket socket;
	private InetAddress addr;
	private JButton v_button;
	
	public void paint(Graphics g) {
		if (image1 != null)
			System.out.println("In paint");
			g.drawImage(image1, 10, 30, this);
	}

	public void update(Graphics g) {
		paint(g);
	}

	class WindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			//System.exit(0);
		}
	}
	public void videoSend(String targetIP, int targetPort) throws IOException {
		DatagramPacket packet = null;
		//byte[] videoData = new byte[120];

		image1 = vision1.getBufferedImage();		
		System.out.println("height:"+image1.getHeight(this));
		System.out.println("width:"+image1.getWidth(this));
		setSize(image1.getWidth(this)+30, image1.getHeight(this)+40);
		setBackground(Color.black);

		while(isOpen) {
			image1 = vision1.getBufferedImage();
			repaint();
			//convert bufferedimage to byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			System.out.println("video sending");
			ImageIO.write( image1, "jpg", baos );
			baos.flush();
			byte[] imageInByte = new byte[19000];
			byte[] tempByte = baos.toByteArray();
			System.arraycopy(tempByte, 0, imageInByte, 0, tempByte.length);
			System.out.println("imageInByte length:"+imageInByte.length);
			imageInByte[imageInByte.length-1] = (byte)Identifier;
			System.out.println("imageInByteSize:"+imageInByte.length);
			baos.close();
			
			//sent the byte array
			
			packet = new DatagramPacket(imageInByte, imageInByte.length, addr, targetPort);
			//socket = new DatagramSocket();
			socket.send(packet);
			
			try {
				Thread.sleep(100);
			} catch(Exception e) {
				System.out.println("Doh");
			}
		}
	}
	public videoSender(final String targetIP, final int targetPort, int Identifier, JButton v_button) {
		this.targetIP = targetIP;
		this.targetPort = targetPort;
		this.Identifier = Identifier;
		this.v_button = v_button;
		//videoSend();
		try {
			this.addr = InetAddress.getByName(targetIP);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startVideo(){
		try {
			vision1 = new FrameGrabber();
			vision1.start();
		} catch (FrameGrabberException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isOpen = true;
		setVisible(true);
		addWindowListener(new WindowListener());
		setTitle("JMF Camera Server");
		
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		thread = new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				do{
					try {
						videoSend(targetIP, targetPort);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}while(true);
			}});
		thread.start();
	}
	
	@SuppressWarnings("deprecation")
	public void stopVideo() throws IOException{
		//vision1.stop();
		byte [] close_info = new byte[19000];
		close_info[18998] = 1;
		DatagramPacket packet = new DatagramPacket(close_info, close_info.length, addr, targetPort);
		socket.send(packet);
		setVisible(false);
		isOpen = false;
		thread.stop();
		v_button.setEnabled(true);
		socket.close();
	}
	
    public void SendToNext(byte[] Data){
    	try {
    			System.out.println("send to next: " + "target ip: " + addr.getHostAddress() + "	target port: " + targetPort);
    			DatagramPacket packet = new DatagramPacket(Data, Data.length, addr, targetPort);
    			
    			socket = new DatagramSocket();       // UDP socket
    			socket.send(packet);
    			
    	} catch (Exception e) {
             e.printStackTrace();
    	 }
    }

	public void setTarget(String ip, int parseInt) {
		// TODO Auto-generated method stub
		this.targetIP = ip;
		this.targetPort = parseInt;
	}
}