package com.github.dariakuzina.serverpart;
import java.awt.BorderLayout;
import java.io.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import com.github.dariakuzina.serverpart.ServerMessage;
import com.github.dariakuzina.starter.Message;
import com.github.dariakuzina.clientpart.ClientMessage;

import java.net.*;
import java.util.*;
/**Server part for chat. The idea and part of the code is taken from "Head First Java" by Kathy Sierra, Bert Bates*/
public class SimpleChatServer {
	int portNumber;
	ArrayList<ObjectOutputStream>writers;
	HashMap<String, Boolean>nicknames;
	JTextArea messages;
	JFrame myFrame;
	ServerSocket serverSocket=null;
	public SimpleChatServer(){
		nicknames=new HashMap<String,Boolean>();
		writers=new ArrayList<ObjectOutputStream>();
	}
	/**Handler for each connected client*/
	class ClientHandler implements Runnable{
		ObjectInputStream reader;
		ObjectOutputStream writer;
		Socket mySocket;
		String name;
		/**Creates new handler
		 * @param clientSocket Client socket*/
		public ClientHandler(Socket clientSocket){
			try{
				mySocket=clientSocket;
				writer=new ObjectOutputStream(mySocket.getOutputStream());
				reader=new ObjectInputStream(mySocket.getInputStream());				
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		/**Gets client's message and sends it to all logged-in clients
		 * When client logs in, adds him in writers' list
		 * If client has disconnected, removes him from writers list*/
		@Override
		public void run(){
			try{
				ClientMessage message;
				while(true){
					message=(ClientMessage)reader.readObject();
					if(message.getType()==ClientMessage.LOGIN){
						if(registerLogin(message.getTextMessage())){
							name=message.getTextMessage();
							writer.writeObject(new ServerMessage(ServerMessage.LOGIN_ACCEPT, name));
							tellEveryone(new ServerMessage(ServerMessage.TEXTMESSAGE, name+" has connected"));
							writers.add(writer);
							messages.append(mySocket.getInetAddress()+" has logged in as "+name +'\n');
						}							
					else {
							writer.writeObject(new ServerMessage(ServerMessage.LOGIN_DECLINE, message.getTextMessage()));
						}
					}
					else if(message.getType()==ClientMessage.LOGOUT){
						writer.writeObject(new ServerMessage(ServerMessage.LOGOUT, ""));
						break;
					}
					else if(message.getType()==ClientMessage.TEXTMESSAGE){
						messages.append("read "+message.getTextMessage()+" from "+mySocket.getInetAddress()+'\n');
						message.setTextMessage(name+": "+message.getTextMessage());
						tellEveryone(message);
					}
				}
				messages.append(mySocket.getInetAddress()+" has disconnected\n");
			}catch(Exception ex){
				messages.append("Connection with"+mySocket.getInetAddress()+" was lost\n");
			}
			finally {
				try{
					ServerMessage message=new ServerMessage(ServerMessage.TEXTMESSAGE, name+" has disconnected");
					writers.remove(writer);
					mySocket.close();
					synchronized (nicknames) {
						nicknames.remove(name);
					}
					tellEveryone(message);				
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}
		public ObjectOutputStream getOOS(){
			return writer;
		}
	}
	/**Gets port number.
	 * @return True if correct port number was received, false if the user cancelled input */
	public boolean receivePortNumber(){
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
		while(receivePortNumber()&&serverSocket==null)
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
		
		myFrame=new JFrame("Simple Chat Server");
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
		try{
			while(true){
				Socket clientSocket=serverSocket.accept();
				ClientHandler clientHandler=new ClientHandler(clientSocket);
				messages.append("Got a connection with "+clientSocket.getInetAddress()+'\n');
				Thread handlerThread=new Thread(clientHandler);
				handlerThread.start();								
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/**Sends message to each logged-in client*/
	public synchronized <T extends Message>void tellEveryone(T message){
		Iterator<ObjectOutputStream>it=writers.iterator();
		while(it.hasNext()){
			try{
				ObjectOutputStream writer=it.next();
				writer.writeObject(message);
			}catch(Exception ex){ ex.printStackTrace();	}
		}
	}
	/**Checks, if the name is unique, and if it is, assigns it to user
	 * @param name User name
	 * @return True if the name was unique and was assigned to user, false otherwise*/
	public synchronized boolean registerLogin(String name){
		if(nicknames.get(name)!=null)return false;
		nicknames.put(name, true);
		return true;
	}
}
