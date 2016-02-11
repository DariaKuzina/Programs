package com.github.dariakuzina.connections;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JComponent;
public class DrawConnections extends JComponent {
	private static final long serialVersionUID = 1L;
	int thickness,prefSize;
	int borderSize=10;
	int N;
	class Point{
		int x;
		int y;
		int number;
		Point(int x,int y,int number) {
			this.number=number;
			this.x=x;
			this.y=y;
		}
	}
	class Line{
		Point p1,p2;
		 Line(int x1,int y1,int x2,int y2,int from, int to) {
			p1=new Point(x1, y1,from);
			 p2=new Point(x2, y2,to);
			}
	}
	ConcurrentLinkedQueue<Line>lines;
	public DrawConnections() {
		lines=new ConcurrentLinkedQueue<Line>();
	}
	public DrawConnections(int thickness,int N,int scale){
		this();
		this.thickness=thickness;
		this.N=N;
		prefSize=(int)Math.sqrt(N)*scale;
		setPreferredSize(new Dimension(prefSize, prefSize));
	}
	void addLine(int x1,int y1,int x2,int y2,int from,int to){
		
		lines.add(new Line(x1, y1, x2, y2,from,to));	
		if(lines.size()>=N)
			repaint();
	}
	public void paint(Graphics g){
		Graphics2D gr2d=(Graphics2D)g;
		gr2d.setStroke(new BasicStroke(thickness,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
		gr2d.translate(borderSize, borderSize);
		int offset=thickness/2;
		for(Line line:lines)
		{
			gr2d.drawLine(line.p1.x, line.p1.y,line.p2.x, line.p2.y);
			gr2d.drawOval(line.p1.x-offset, line.p1.y-offset, thickness, thickness);
			gr2d.drawOval(line.p2.x-offset, line.p2.y-offset, thickness, thickness);
			gr2d.drawString(Integer.toString(line.p1.number), line.p1.x+offset, line.p1.y-offset);
			gr2d.drawString(Integer.toString(line.p2.number), line.p2.x+offset, line.p2.y-offset);
		}
			
	}
	/*
	public static void main(String[] args) {
		
		JFrame mainFrame=new JFrame("Connections");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		DrawConnections drawPane=new DrawConnections(5);
		mainFrame.getContentPane().add(drawPane, BorderLayout.CENTER);
		mainFrame.setSize(500, 500);
		mainFrame.setVisible(true);
		int s=5;
		for(int i=0;i<5;i++){
			int x1=(int)(i%s)*100;
			int x2=(int)((i+5)%s*100);
			int y1=(int)(i/s)*100;
			int y2=(int)((i+5)/s*100);
			drawPane.addLine(x1, y1, x2, y2);
		}
		drawPane.addLine(0, 100, 400, 100);
	}*/

}
