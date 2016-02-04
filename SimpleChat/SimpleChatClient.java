package com.github.dariakuzina.clientpart;
import java.io.*;
import java.util.regex.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.Border;
import com.github.dariakuzina.serverpart.ServerMessage;
import java.awt.*;
import java.awt.event.*;
/**Client part for chat. The idea and part of the code is taken from "Head First Java" by Kathy Sierra, Bert Bates */
public class SimpleChatClient {

	JTextArea incoming;
	JTextField outgoing;
	InputStream iStream;
	OutputStream oStream;
	ObjectInputStream reader;
	ObjectOutputStream writer;
	Socket mySocket;
	JFrame myFrame;
	Thread readerThread;
	JButton connectButton, disconnectButton,sendButton;
	String serverIP,nickname;
	int portNumber;
	/**Builds GUI when client starts*/
	public void go(){		
		
		myFrame=new JFrame("Simple Chat Client");
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel mainPanel=new JPanel();
		incoming=new JTextArea(15,20);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		JScrollPane qScroller=new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		outgoing=new JTextField(20);
		JToolBar buttons=new JToolBar();
		buttons.setLayout(new GridLayout(2, 1));
		
		connectButton=new JButton("Connect");
		connectButton.addActionListener(new ConnectButtonListener());		
		disconnectButton=new JButton("Disconnect");
		disconnectButton.addActionListener(new DisconnectButtonListener());
		disconnectButton.setEnabled(false);
		
		buttons.add(connectButton);
		buttons.add(disconnectButton);
		Border emptyBorder=BorderFactory.createEmptyBorder();
		buttons.setBorder(emptyBorder);
		
		sendButton=new JButton("Send");
		sendButton.addActionListener(new SendButtonListener());
		sendButton.setEnabled(false);
				
		mainPanel.add(qScroller,BorderLayout.WEST);
		mainPanel.add(buttons,BorderLayout.EAST);	
		mainPanel.add(outgoing,BorderLayout.WEST);
		
		mainPanel.add(sendButton);
		myFrame.getContentPane().add(mainPanel,BorderLayout.CENTER);
		myFrame.setSize(400, 500);
		myFrame.setVisible(true);		
		
	}
	/**Creates server socket
	 * @param ip Server IP
	 * @param port Port number
	 * @return True if socket was successfully created, false otherwise*/
	private boolean setUpNetworking(String ip,int port) {
		
			try{
				mySocket=new Socket(ip, port);	
				iStream=mySocket.getInputStream();
				oStream=mySocket.getOutputStream();
				writer=new ObjectOutputStream(oStream);
				reader=new ObjectInputStream(iStream);				
				incoming.append("Connection established"+'\n');	
				return true;
			}catch(Exception ex){				
				return false;
			}
		
	}
	/**Reads connection data from TextFields and checks it
	 * @param ipField Field with IP address
	 * @param portField Field with port number
	 * @return True if data is correct, false otherwise*/
	private boolean getConnectionData(JTextField ipField, JTextField portField){
		try{
			serverIP=ipField.getText();
			portNumber=Integer.parseInt(portField.getText());
			if(portNumber<1||portNumber>65535)throw new Exception();
			Pattern ipPattern=Pattern.compile("(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}");
			Matcher matcherIp=ipPattern.matcher(serverIP);
			if(!matcherIp.matches())throw new Exception();
			return true;
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(myFrame,"IP format: d.d.d.d where d is a number between 0 and 255\nPort format: number between 1 and 65 535");
			return false;
		}
	}
	/**Creates dialog to ask user about connection data. Dialog repeats, while data is incorrect and user doesn't cancel input
	 * @return True if correct data was received, false otherwise */
	private boolean askForConnectionData(){
	
		JTextField ipField = new JTextField(15);
	    JTextField portField = new JTextField(5);
	    Object[] fields = {
	    	    "Server IP:", ipField,
	    	    "Port:", portField
	    	};
	    while (JOptionPane.showConfirmDialog(myFrame,fields,"Enter data connection",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
			if(getConnectionData(ipField, portField))
				return true;
			} 
		return false;
	}
	/**Listener for sendButton*/
	class SendButtonListener implements ActionListener{
		
		/**Sends user message to server*/
		@Override
		public void actionPerformed(ActionEvent e){
			try{
				ClientMessage message=new ClientMessage(ClientMessage.TEXTMESSAGE, outgoing.getText());
				writer.writeObject(message);
			}catch(Exception ex){
				JOptionPane.showMessageDialog(myFrame, "Connection was corrupted");
			}
			outgoing.setText("");
			outgoing.requestFocus();
		}
	}
	/**Gets user's input and sends login query to server*/
	public void logIn(){
		String name;
		if((name=JOptionPane.showInputDialog(myFrame, "Choose your nickname"))!=null){
			try{
				writer.writeObject(new ClientMessage(ClientMessage.LOGIN, name));
			}catch(Exception ex){
				JOptionPane.showMessageDialog(myFrame, "Connection was corrupted");
			}
		}
	}
	/**Change state of connectButton, disconnectButton, sendButton to opposite */
	private void changeButtonsState(){
		connectButton.setEnabled(!connectButton.isEnabled());
		disconnectButton.setEnabled(!disconnectButton.isEnabled());
		sendButton.setEnabled(!sendButton.isEnabled());
	}
	/**Listener for connectButton.*/
	class ConnectButtonListener implements ActionListener{
		
		/**Asks for connection data. Tries to set connection with server till success or user's cancellation. Logs in client*/
		@Override
		public void actionPerformed(ActionEvent ev){
			int answer;
			if(!askForConnectionData())return;
			String header="Cannot create connection";
			String message="Cannot create connection with socket "+serverIP+":"+portNumber+"\nWould you like to try again?";
			String[]options={"Yes","No"};
			do {
				if(!setUpNetworking(serverIP, portNumber))
					answer=JOptionPane.showOptionDialog(myFrame, message, header, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,options, null);
				else {
					readerThread=new Thread(new IncomingReader());
					readerThread.start();
					logIn();
					break;
				}
			} while (answer==JOptionPane.YES_OPTION);								
		}
	}
	/**Listener for disconnectButton*/
	class DisconnectButtonListener implements ActionListener{
		/**Sends LOGOUT message to server*/
		@Override
		public void actionPerformed(ActionEvent e){
			try{
					ClientMessage message=new ClientMessage(ClientMessage.LOGOUT, "");
					writer.writeObject(message);
				}catch (Exception ex){
					JOptionPane.showMessageDialog(myFrame, "Connection was corrupted");
			}
		}
	}
	/**Describes thread for reading messages from server*/
	class IncomingReader implements Runnable{
		
		/**Reads messages from server till LOGOUT message*/
		@Override
		public void run(){
			
			try{
				Object message;
				while(true)	
				{	
					message=(Object)reader.readObject();
					if(message instanceof ClientMessage){
						incoming.append(((ClientMessage) message).getTextMessage()+'\n');
					}
					else if((message instanceof ServerMessage)&&((ServerMessage)message).getType()==ServerMessage.LOGIN_ACCEPT){
						nickname=((ServerMessage)message).getTextMessage();
						incoming.append("Welcome "+nickname+"!\n");
						changeButtonsState();
					}
					else if ((message instanceof ServerMessage)&&((ServerMessage)message).getType()==ServerMessage.LOGIN_DECLINE){
						JOptionPane.showMessageDialog(myFrame, "Nickname "+((ServerMessage)message).getTextMessage()+" is already used. Please, choose another nickname");
						logIn();
					}
					else if((message instanceof ServerMessage)&&((ServerMessage)message).getType()==ServerMessage.LOGOUT){
						break;
					}
					else if(message instanceof ServerMessage){
						incoming.append("Server message:"+((ServerMessage) message).getTextMessage()+'\n');
					}	
					
				}
				JOptionPane.showMessageDialog(myFrame, "You have been disconnected from the server");
			}catch (Exception ex){
				JOptionPane.showMessageDialog(myFrame, "Connection was disabled");
			}
			finally{
				try{
				incoming.append("Connection was disabled\n");
				mySocket.close();
				changeButtonsState();
				}catch (Exception ex){
					ex.printStackTrace();
				}
			}
		}
	}
	

}
