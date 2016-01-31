package com.github.dariakuzina;
import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
/**
 * MIDI-sequence creator. Use checkboxes to choose active instruments on every tick.
 * The idea and the biggest part of code are taken from "Head First Java" by Kathy Sierra and Bert Bates
 */
public class BoomBox {
	JPanel mainPanel;
	ArrayList<JCheckBox>checkboxList;
	Sequencer mySequencer;
	Sequence mySequence;
	Track myTrack;
	JFrame myFrame;
	String[]instrumentNames={"Bass Drum","Closed Hi-Hat","Open Hi-Hat","Acoustic Snare","Crash Cymbal","Hand Clap","High Tom","Hi Bongo","Maracas","Whistle",
			"Low Conga","Cowbell","Vibraslap","Low-mid Tom","High Agogo","Open Hi Conga"};
	int[]instruments={35,42,46,38,49,39,50,60,70,72,64,56,68,47,67,63};
	public static void main(String[] args) {
		new BoomBox().buildGUI();
	}

public void buildGUI(){	
	myFrame=new JFrame("Cyber BoomBox");
	myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	BorderLayout layout=new BorderLayout();
	JPanel background=new JPanel(layout);
	background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	checkboxList=new ArrayList<JCheckBox>();
	GridLayout buttonLayout=new GridLayout(10, 1);
	buttonLayout.setVgap(2);
	JToolBar buttonBox=new JToolBar();
	buttonBox.setLayout(buttonLayout);
	JButton start=new JButton("Start");
	start.addActionListener(new MyStartListener());
	buttonBox.add(start);
	
	JMenuBar myMenuBar=new JMenuBar();
	JMenu myMenu=new JMenu("File");	
	
	JMenuItem loadMenuItem=new JMenuItem("Load File");
	loadMenuItem.addActionListener(new MyLoadFileListener());
	myMenu.add(loadMenuItem);
	
	JMenuItem saveMenuItem=new JMenuItem("Save File");
	saveMenuItem.addActionListener(new MySaveFileListener());
	myMenu.add(saveMenuItem);
	
	myMenuBar.add(myMenu);
	myFrame.setJMenuBar(myMenuBar);
	
	JButton stop=new JButton("Stop");
	stop.addActionListener(new MyStopListener());
	buttonBox.add(stop);
	
	JButton upTempo=new JButton("Tempo Up");
	upTempo.addActionListener(new MyUpTempoListener());
	buttonBox.add(upTempo);
	
	JButton downTempo=new JButton("Tempo Down");
	downTempo.addActionListener(new MyDownTempoListener());
	buttonBox.add(downTempo);
	
	JButton clearBoxes=new JButton("Clear");	
	clearBoxes.addActionListener(new MyClearBoxesListener());
	buttonBox.add(clearBoxes);
	Box nameBox=new Box(BoxLayout.Y_AXIS);
	for(int i=0;i<16;i++){
		nameBox.add(new Label(instrumentNames[i]));
	}
	background.add(BorderLayout.EAST,buttonBox);
	background.add(BorderLayout.WEST,nameBox);
	myFrame.getContentPane().add(background);
	
	GridLayout grid=new GridLayout(16, 16);
	grid.setHgap(2);
	grid.setVgap(1);
	mainPanel=new JPanel(grid);
	background.add(BorderLayout.CENTER,mainPanel);
	
	for(int i=0;i<256;i++){
		JCheckBox c =new JCheckBox();
		c.setSelected(false);
		checkboxList.add(c);
		mainPanel.add(c);
	}
	SetUpMidi();
	
	myFrame.setBounds(50,50,300,300);
	myFrame.pack();
	myFrame.setVisible(true);
}//end of buildGUI;

public void SetUpMidi(){
	try{
		mySequencer=MidiSystem.getSequencer();
		mySequencer.open();
		mySequence=new Sequence(Sequence.PPQ, 4);
		myTrack=mySequence.createTrack();
		mySequencer.setTempoInBPM(120);
	}
	catch(Exception e){e.printStackTrace();}
}//end of SetUpMidi
public void buildTrackAndStart(){
	int[] trackList=null;
	mySequence.deleteTrack(myTrack);
	myTrack=mySequence.createTrack();
	int key;
	for(int i=0;i<16;i++){
		trackList=new int[16];
		key=instruments[i];
		for(int j=0;j<16;j++){
			JCheckBox jc=(JCheckBox)checkboxList.get(j+(16*i));
			if(jc.isSelected()){
				trackList[j]=key;
			}
			else{
				trackList[j]=0;
			}
		}
		makeTracks(trackList);
		myTrack.add(makeEvent(ShortMessage.CONTROL_CHANGE, 1, 127, 0,16));
	}
	myTrack.add(makeEvent(ShortMessage.PROGRAM_CHANGE, 9, 1, 0,15));
	try{
		mySequencer.setSequence(mySequence);
		mySequencer.setLoopCount(mySequencer.LOOP_CONTINUOUSLY);
		mySequencer.start();
		mySequencer.setTempoInBPM(120);
	}
	catch(Exception e){
		e.printStackTrace();
	}
	
}//end of buildTrackAndStart
public void makeTracks(int[]list){
	for(int i=0;i<16;i++){
		int key=list[i];
		if(key!=0){
			myTrack.add(makeEvent(ShortMessage.NOTE_ON, 9, key, 100,i));
			myTrack.add(makeEvent(ShortMessage.NOTE_OFF, 9, key, 100,i+1));
		}
	}
}//end of makeTracks
/**Creates new midi event
 * @param comd MIDI command
 * @param chan channel
 * @param one first data byte
 * @param two second data byte
 * @param tick time-stamp*/
public MidiEvent makeEvent(int comd,int chan,int one,int two,int tick){
	MidiEvent event=null;
	try{
		ShortMessage a=new ShortMessage();
		a.setMessage(comd,chan,one,two);
		event=new MidiEvent(a, tick);
	}
	catch(Exception e){
		e.printStackTrace();
	}
	return event;
}//end of makeEvent
private void saveInFile(File file){
	boolean[]checkboxState=new boolean[256];
	for (int i=0;i<256;i++) {
		JCheckBox check=checkboxList.get(i);
		if(check.isSelected()){	
			checkboxState[i]=true;
		}
	}
	try{
		FileOutputStream fileStream=new FileOutputStream(file);
		ObjectOutputStream os=new ObjectOutputStream(fileStream);
		os.writeObject(checkboxState);
		
	}catch(Exception e){
		e.printStackTrace();
	}
}//end of SaveInFile
private void restoreFromFile(File file){
	boolean[]checkboxState=null;
	try{
		FileInputStream fileIn=new FileInputStream(file);
		ObjectInputStream iStream=new ObjectInputStream(fileIn);
		checkboxState=(boolean[])iStream.readObject();
	}catch (Exception e){
		e.printStackTrace();
	}
	for (int i=0;i<256;i++) {
		JCheckBox check=checkboxList.get(i);
		if(checkboxState[i]){	
			check.setSelected(true);
		}
		else{
			check.setSelected(false);
		}
	}
	mySequencer.stop();
}//end of restoreFromFile
class MyStartListener implements ActionListener{
	public void actionPerformed(ActionEvent a){
		buildTrackAndStart();
	}
}//end of MyStartListener
class MyStopListener implements ActionListener{
	public void actionPerformed(ActionEvent a){
		mySequencer.stop();
	}
}//end of MyStopListener
class MyUpTempoListener implements ActionListener{
	public void actionPerformed(ActionEvent a){
		float tempoFactor=mySequencer.getTempoFactor();
		mySequencer.setTempoFactor((float)(tempoFactor*1.03));
	}
}//end of MyUpTempoListener
class MyDownTempoListener implements ActionListener{
	public void actionPerformed(ActionEvent a){
		float tempoFactor=mySequencer.getTempoFactor();
		mySequencer.setTempoFactor((float)(tempoFactor*0.97));
	}
}//end of MyDownTempoListener
class MyClearBoxesListener implements ActionListener{
	public void actionPerformed(ActionEvent a){
		for(JCheckBox box:checkboxList){
			box.setSelected(false);
		}
		mySequencer.stop();
	}
}//end of MyClearBoxesListener
class MyLoadFileListener implements ActionListener{
	public void actionPerformed(ActionEvent a){
			JFileChooser fileIn=new JFileChooser();
			int res=fileIn.showOpenDialog(myFrame);
			if(res==JFileChooser.APPROVE_OPTION){		
			restoreFromFile(fileIn.getSelectedFile());
			}
			
}
}//end of MyLoadFileListener
class MySaveFileListener implements ActionListener{
	public void actionPerformed(ActionEvent a){
		JFileChooser fileSave=new JFileChooser();
		int res=fileSave.showSaveDialog(myFrame);
		if(res==JFileChooser.APPROVE_OPTION){
		saveInFile(fileSave.getSelectedFile());
		}
		
	}
}//end of MySaveFileListener
}//end of BoomBox
