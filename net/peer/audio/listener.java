package net.peer.audio;

import javax.sound.sampled.*;

import java.net.*;

public class listener extends Thread{
    private int portNo;
    private int peerIdentifier;
    sender audioSender;
    
    AudioInputStream ais = null;                     // input stream
    AudioFormat af = null;                           // data format is AU file
    SourceDataLine sourceDL = null;                  // output Voice device
    
    public listener(int portNo, int peerIdentifier, sender audioSender) {
        this.portNo = portNo;
        this.peerIdentifier = peerIdentifier;
        this.audioSender = audioSender;
        InitListener();
    }
    private void InitListener() {
        // encode PCM unsigned, 8000Hz, 1ch
        af = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,     // Encoding
                             6300.0F,                               // SampleRate
                             8,                                     // sampleSizeInBits
                             1,                                     // channels
                             1,                                     // frameSize
                             6300.0F,                               // frameRate
                             false);                                // bitEndian
        
        try {
            DataLine.Info infoS = new DataLine.Info(SourceDataLine.class, af);
            //get source line
            sourceDL = (SourceDataLine) AudioSystem.getLine(infoS);
            sourceDL.open(af);
            sourceDL.start();
            this.listenerFlag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean listenerFlag;

    public void CloseList() {
        sourceDL.close();
        this.listenerFlag = false;
        socket.close();
    }
    public DatagramSocket socket;
    @Override
    public void run() {
        DatagramPacket packet = null;
        
        try  {
        	int counter = 1;
        	int counter1 = 1;
            byte[] listData = new byte[61];
            socket = new DatagramSocket(this.portNo);       // start udp trans set port
            System.out.println("start receive");
            do {
                packet = new DatagramPacket(listData, listData.length);   // prepare receive packet
                socket.receive(packet);                                   // blocking waiting receive
                if(peerIdentifier != (int)listData[60]){
                	//System.out.println("Client: "+counter++);
                	sourceDL.write(listData, 0, listData.length-1);                     // output to headphone
                	audioSender.SendToNext(listData);
                }else{
                	//System.out.println("Voice Send Back"+counter1++);
                }
            }while (listenerFlag);
            packet = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}