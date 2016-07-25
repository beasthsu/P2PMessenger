package net.peer.video;
// David Chamulak
// 3/3/03
// Simple program to show the Use of the a WebCam in Java
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;

public class videoListener extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage image1;
	private int Identifier;
	public DatagramSocket socket;
	private int myPort;
	private videoSender videoSender;
	private JButton v_button;
	private Thread thread;
	
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

	public videoListener(int myPort, int Identifier, videoSender videoSender, JButton v_button) {
		this.myPort = myPort;
		this.Identifier = Identifier;
		this.videoSender = videoSender;
		this.v_button = v_button;
		//videoSend();
	}
	public void recvDisplay() {
		addWindowListener(new WindowListener());
		setTitle("JMF Camera Client");
		int count = 0;
		System.out.println("In recvDisplay");
		byte[] listData = new byte[19000];
		DatagramPacket packet = null;
		
		while (true) {
			packet = new DatagramPacket(listData, listData.length);
			try {
				int counter1 = 1;
				socket.receive(packet);
				System.out.println("count:"+count++);
				if(listData[listData.length-2] != 1){
					if(listData[listData.length-1] != Identifier){
						v_button.setEnabled(false);
						InputStream in = new ByteArrayInputStream(listData);
						image1 = ImageIO.read(in);
						setSize(image1.getWidth(this)+30, image1.getHeight(this)+40);
						setVisible(true);
						repaint();
						setBackground(Color.black);						
						System.out.println("count:"+count++);
						videoSender.SendToNext(listData);
					}
					else{
						System.out.println("Video Send Back" + counter1++);
					}
				}
				else{
					videoSender.SendToNext(listData);
					this.setVisible(false);
					v_button.setEnabled(true);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	public void startVideo(){
		try {
			socket = new DatagramSocket(myPort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		thread = new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				do{
					recvDisplay();
				}while(true);
			}});
		thread.start();
	}
	
	@SuppressWarnings("deprecation")
	public void stopVideo() {
		thread.stop();
		// TODO Auto-generated method stub
		v_button.setEnabled(true);
		setVisible(false);
		socket.close();
	}
}