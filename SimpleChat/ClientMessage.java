package com.github.dariakuzina.clientpart;
import com.github.dariakuzina.starter.Message;
/**Describes client's message*/
public class ClientMessage extends Message {
	private static final long serialVersionUID = 11L;
	public final static int LOGIN=0,LOGOUT=1,TEXTMESSAGE=2;
	/**Creates message
	 * @param type Type of message
	 * Legal values are: ClientMesage.LOGIN, ClientMesage.LOGOUT, ClientMesage.TEXTMESSAGE,
	 */
	public ClientMessage(int type,String message){
		this.type=type;
		textMessage=message;
	}
	public void setTextMessage(String message){
		textMessage=message;
	}
}
