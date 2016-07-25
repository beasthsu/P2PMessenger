package net.peer.whiteboard;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;

public class WhiteboardUI extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean isDragging;
	private Point currentPoint;
	private Point previousPoint;
	
	public WhiteboardUI(){
		super("Whiteboard");
		currentPoint = new Point();
		previousPoint = new Point();
		setUpUIComponent();
		setUpEventListener();
	}
	private void setUpUIComponent(){
		setSize(280, 207);
		this.setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		Container c = getContentPane();
		c.setLayout(new FlowLayout());
		c.setBackground(Color.white);
		setVisible(true);
	}
	private void setUpEventListener() {
		addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent evt){
				if(isDragging == true)
					return;
				isDragging = true;
				currentPoint.setPoint(evt.getX(), evt.getY());
				previousPoint.setPoint(currentPoint.getX(), currentPoint.getY());
			}
		});
		addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent evt){
				isDragging = false;
			}
		});
		addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent evt){
				if(isDragging == false)
					return;
				Point temp = new Point(evt.getX(),evt.getY());
				Graphics g = getGraphics();
				g.setColor(Color.blue);
				g.drawLine(previousPoint.getX(), previousPoint.getY(),
						temp.getX(), temp.getY());
				previousPoint.setPoint(temp.getX(), temp.getY());
			}
		});
	}
}
