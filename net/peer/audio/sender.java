package net.peer.audio;

import java.net.*;

import javax.sound.sampled.*;

public class sender extends Thread{
    private int portNo;
    private String addrIP;
    private int peerIdentifier;
    InetAddress addr;
    boolean recordFlag,stop; 
    public DatagramSocket socket;          // udp socket
    
    public sender(String addrIP, int portNo, int peerIdentifier){
        this.addrIP = addrIP;
        this.portNo = portNo;
        this.peerIdentifier = peerIdentifier;
        stop = true;
        try {
			addr = InetAddress.getByName(this.addrIP);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        InitRecord();
    }
    
    TargetDataLine targetDL = null;                 // input Voice device
    private void InitRecord() {
        // encode PCM unsigned, 8000Hz, 1ch
        AudioFormat af = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,     // Encoding
                             6300.0F,                               // SampleRate
                             8,                                     // sampleSizeInBits
                             1,                                     // channels
                             1,                                     // frameSize
                             6300.0F,                               // frameRate
                             false);                                // bitEndian
        
        try {
            DataLine.Info infoT = new DataLine.Info(TargetDataLine.class, af);
            //get target line
            targetDL = (TargetDataLine) AudioSystem.getLine(infoT);
            targetDL.open(af);
            targetDL.start();
            
            recordFlag = true;
            
        } catch (Exception e) {
            System.out.println("" + e);
            throw new ArithmeticException();
        }
        System.out.println("Init Record OK...");
    }
    
    public void CloseRecord() {
        targetDL.close();
        recordFlag = false;
        stop = false;
    }
    public void stopRecord(){
    	stop = true;
    }
    public void restart(){
    	stop = false;
    }
    public void SendToNext(byte[] audioData){
    	try {
    		DatagramPacket packet = new DatagramPacket(audioData, audioData.length, addr, portNo);
    		socket = new DatagramSocket();       // UDP socket
    		socket.send(packet);
    	 } catch (Exception e) {
             e.printStackTrace();
    	 }
    }
    @Override
    public void run() {
        System.out.println("start output");
        try {
        	int counter = 1;
            DatagramPacket packet = null;
            do {	// trans data from target data line
            	while(stop){}
            		
            	byte[] audioData = new byte[61];
            	audioData[60]= (byte) peerIdentifier;
            	int byteRead = targetDL.read(audioData, 0, audioData.length-1);       // audio Record data
            	if (byteRead > 0) {
            		packet = new DatagramPacket(audioData, audioData.length, addr, portNo);
            		socket = new DatagramSocket();       // UDP socket
            		socket.send(packet);
            		System.out.println("Server: "+counter++);
            	}
            } while(recordFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}