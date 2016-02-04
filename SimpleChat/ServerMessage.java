package com.github.dariakuzina.serverpart;

import com.github.dariakuzina.starter.Message;
/**Describes client's message*/
public class ServerMessage extends Message {
	private static final long serialVersionUID = 11L;
	public final static int LOGIN_ACCEPT=0,LOGIN_DECLINE=1,TEXTMESSAGE=2,LOGOUT=3;
	/**Creates message
	 * @param type Type of message
	 * Legal values are: ServerMessage.LOGIN_ACCEPT, ServerMessage.LOGIN_DECLINE, ServerMessage.TEXTMESSAGE,
	 */
	public ServerMessage(int type,String message){
		this.type=type;
		textMessage=message;
	}

}
