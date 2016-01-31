package com.github.dariakuzina.serverpart;
import java.awt.BorderLayout;
import java.io.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import java.net.*;
import java.util.*;
/**Server part for chat. The idea and part of the code is taken from "Head First Java" by Kathy Sierra, Bert Bates*/
public class SimpleChatServer {
	int portNumber;
	ArrayList<PrintWriter>writers;
	JTextArea messages;
	JFrame myFrame;
	ServerSocket serverSocket=null;
	/*public static void main(String[] args) {	
		new SimpleChatServer().go();
	}*/
	/**Handler for each connected client*/
	class ClientHandler implements Runnable{
		BufferedReader reader;
		PrintWriter output;
		Socket mySocket;
		/**Creates new handler
		 * @param clientSocket Client socket*/
		public ClientHandler(Socket clientSocket){
			try{
				mySocket=clientSocket;
				InputStreamReader istrReader=new InputStreamReader(mySocket.getInputStream());
				reader=new BufferedReader(istrReader);
				output=new PrintWriter(mySocket.getOutputStream());
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		/**Gets client's message and sends it to all connected clients
		 * If client was disconnected, removes him from writers list*/
		@Override
		public void run(){
			String message;
			try{
				while((message=reader.readLine())!=null){
					messages.append("read "+message+" from "+mySocket.getInetAddress()+'\n');
					tellEveryone(mySocket.getLocalAddress()+": "+message);
				}
				messages.append(mySocket.getInetAddress()+" has disconnected\n");
			}catch(Exception ex){
				messages.append("Connection with"+mySocket.getInetAddress()+" was lost\n");
			}
			finally {
				writers.remove(output);
			}
		}
		
	}
	/**Gets port number.
	 * @return True if correct port number was received, false if the user cancelled input */
	public boolean getPortNumber(){
		portNumber=0;
		String portNum;
		while((portNum=JOptionPane.showInputDialog(null, "Enter port number"))!=null){
			try{
				portNumber=Integer.parseInt(portNum);
				if(portNumber<1||portNumber>65535)throw new Exception();
				return true;
			}catch(Exception ex){
				JOptionPane.showMessageDialog(null,"Port number is not valid. Enter number from 1 to 65 535");
			}
		}
		return false;
	}
	/**Creates server socket with given portNumber. If fails, asks for another portNumber */
	public boolean createServerSocket(){
		while(getPortNumber()&&serverSocket==null)
		{
			try{
				serverSocket=new ServerSocket(portNumber);
				return true;
			}
			catch (BindException bex){
				JOptionPane.showMessageDialog(null,"Cannot create socket with port "+portNumber+'\n'+"Perhaps, the port is in use, or the requested local address could not be assigned.");
			}
			catch(Exception ex){
				JOptionPane.showMessageDialog(null,"Cannot create socket with port "+portNumber);
			}
		}
		return false;
	}
	/**Builds server GUI*/
	public void buildGUI(){
		
		myFrame=new JFrame("Simple chat server");
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		messages=new JTextArea(15,20);
		messages.setLineWrap(true);
		messages.setWrapStyleWord(true);
		messages.setEditable(false);
		JScrollPane qScroller=new JScrollPane(messages);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		myFrame.getContentPane().add(qScroller, BorderLayout.WEST);
		myFrame.setSize(400, 500);
		myFrame.setVisible(true);	
	}
	/**Starts server. Registers clients.*/
	public void go(){	
		if(!createServerSocket())System.exit(0);
		buildGUI();
		messages.append("Server with port number "+portNumber+" socket was succesfully created\n");
		writers=new ArrayList<PrintWriter>();
		try{
			while(true){
				Socket clientSocket=serverSocket.accept();
				PrintWriter writer=new PrintWriter(clientSocket.getOutputStream());
				writers.add(writer);
				Thread handlerThread=new Thread(new ClientHandler(clientSocket));
				handlerThread.start();				
				messages.append("Got a connection with "+clientSocket.getInetAddress()+'\n');				
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/**Sends message to each connected client*/
	public void tellEveryone(String message){
		Iterator<PrintWriter>it=writers.iterator();
		while(it.hasNext()){
			try{
				PrintWriter writer=it.next();
				writer.println(message);
				writer.flush();
			}catch(Exception ex){ ex.printStackTrace();	}
		}
	}
}
