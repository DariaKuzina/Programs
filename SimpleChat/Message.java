package com.github.dariakuzina.starter;
import java.io.Serializable;
/**Abstract class for message*/
public abstract class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	protected int type;
	protected  String textMessage;
	public int getType(){return type;}
	public String getTextMessage(){return textMessage;}
}
