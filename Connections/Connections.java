package com.github.dariakuzina.connections;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import com.github.dariakuzina.algorithms.WeightedQuickUnion;
/**Class uses weighted-quick union algorithm to analyze graph
 * Input: text file with pairs of vertexes
 * If vertexes from new edge are already connected, this new edge will not be added to list of edges
 * In the end the list of edges will be drawn*/
public class Connections {
	JFrame mainFrame;
	JPanel background;
	DrawConnections drawPane;
	JTextArea textArea;
	int lineThickness=3;
	int borderSize=15;
	int scale=50;
	public void buildGUI(){
		mainFrame=new JFrame("Connections");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		background=new JPanel(new BorderLayout());	
		JMenuBar menuBar=new JMenuBar();
		JMenu menu=new JMenu("File");
		JMenuItem openFile=new JMenuItem("Open");
		openFile.addActionListener(new FileOpenListener());
		menu.add(openFile);
		menuBar.add(menu);
		String message="Choose file which contains:\nNumber of edges N in the first string\nN pairs of vertexes - one pair in each string\n";
		String example="Example:\n3\n0 1\n1 2\n2 0\n";
		textArea=new JTextArea(message+example);
		textArea.setEditable(false);
		mainFrame.getContentPane().add(menuBar, BorderLayout.NORTH);
		mainFrame.getContentPane().add(textArea);
		mainFrame.setSize(500, 500);
		mainFrame.setVisible(true);
	}
	public void addDrawPanel(int N){
		background.removeAll();			
		background.setBorder(new EmptyBorder(borderSize, borderSize, borderSize, borderSize));
		drawPane=new DrawConnections(lineThickness,N,scale);
		JScrollPane scrollPane=new JScrollPane(drawPane);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		background.add(scrollPane,BorderLayout.CENTER);
		mainFrame.getContentPane().remove(textArea);
		mainFrame.getContentPane().add(background,BorderLayout.CENTER);	
		mainFrame.setVisible(true);
	}
	class FileOpenListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser jfChooser=new JFileChooser();
			int res=jfChooser.showOpenDialog(mainFrame);
			File fileIn;
			if(res==JFileChooser.APPROVE_OPTION)
			{
				fileIn= jfChooser.getSelectedFile();
				start(fileIn);
			}
		}
		
	}
	void start(File fileIn){
		try {
			Scanner scanner=new Scanner(fileIn);
			int N=scanner.nextInt();
			int poz=(int)Math.sqrt(N);
			WeightedQuickUnion myWQU=new WeightedQuickUnion(N);
			addDrawPanel(N);
			while(scanner.hasNextInt()){
				int p=scanner.nextInt();
				int q=scanner.nextInt();
				if(myWQU.connected(p, q))continue;
				myWQU.union(p, q);
				drawPane.addLine((p%poz)*scale,(p/poz)*scale, (q%poz)*scale,(q/poz)*scale,p,q);
			}
			scanner.close();
		} 
		catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "File cannot be open!");
		}
	}
	public static void main(String[] args) {
		Connections test=new Connections();
		test.buildGUI();		
	}

}
