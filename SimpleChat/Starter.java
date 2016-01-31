package com.github.dariakuzina.starter;

import javax.swing.*;
import com.github.dariakuzina.clientpart.SimpleChatClient;
import com.github.dariakuzina.serverpart.SimpleChatServer;
/**Provides choice between client and server mode*/
public class Starter {
	/**Starts the application and asks for application mode*/
	public static void main(String[] args) {		
		String[]options={"Client mode","Server mode"};
		int answer=JOptionPane.showOptionDialog(null,"Please, choose apllication mode","Choose mode",JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,null,options,null);
		if(answer==JOptionPane.YES_OPTION){
			new SimpleChatClient().go();
		}
		else if(answer==JOptionPane.NO_OPTION) {
			new SimpleChatServer().go();
		}
		
	}

}
