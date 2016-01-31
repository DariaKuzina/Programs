package com.github.dariakuzina.clientpart;
import java.io.*;
import java.util.regex.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
/**Client part for chat. The idea and part of the code is taken from "Head First Java" by Kathy Sierra, Bert Bates */
public class SimpleChatClient {

	JTextArea incoming;
	JTextField outgoing;
	BufferedReader reader;
	PrintWriter writer;
	Socket mySocket;
	JFrame myFrame;
	Thread readerThread;
	JButton connectButton, disconnectButton,sendButton;
	String serverIP;
	int portNumber;
	/*public static void main(String[] args) {
		SimpleChatClient client=new SimpleChatClient();
		client.go();

	}*/
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
				InputStreamReader streamReader=new InputStreamReader(mySocket.getInputStream());
				reader=new BufferedReader(streamReader);
				writer=new PrintWriter(mySocket.getOutputStream());
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
				writer.println(outgoing.getText());
				writer.flush();
			}catch(Exception ex){
				ex.printStackTrace();
			}
			outgoing.setText("");
			outgoing.requestFocus();
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
		
		/**Asks for connection data. Tries to set connection with server till success or user's cancellation*/
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
					changeButtonsState();
					break;
				}
			} while (answer==JOptionPane.YES_OPTION);								
		}
	}
	/**Listener for disconnectButton*/
	class DisconnectButtonListener implements ActionListener{
		/**Closes client socket*/
		@Override
		public void actionPerformed(ActionEvent e){
			try{
					mySocket.close();
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}
	/**Describes thread for reading messages from server*/
	class IncomingReader implements Runnable{
		
		/**Reads messages from server while connection is set and socket is open*/
		@Override
		public void run(){
			String message=null;
			try{
				while((message=reader.readLine())!=null){
					incoming.append(message+'\n');
				}
				JOptionPane.showMessageDialog(myFrame, "You have been disconnected from the server");
			}catch (Exception ex){
				JOptionPane.showMessageDialog(myFrame, "Connection was disabled");
			}
			finally{
				try{
				incoming.append("Connection was disabled\n");
				if(!mySocket.isClosed())
				mySocket.close();
				changeButtonsState();
				}catch (Exception ex){
					ex.printStackTrace();
				}
			}
		}
	}
	

}
