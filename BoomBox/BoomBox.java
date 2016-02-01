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
	private final int noteStrenght=100,	defaultTempo=120, numberOfInstruments=16, numberOfTicks=16;
	private final double tempoUpCoef=1.05,tempoDownCoef=0.95;
	String[]instrumentNames={"Bass Drum","Closed Hi-Hat","Open Hi-Hat","Acoustic Snare","Crash Cymbal","Hand Clap","High Tom","Hi Bongo","Maracas","Whistle",
			"Low Conga","Cowbell","Vibraslap","Low-mid Tom","High Agogo","Open Hi Conga"};
	int[]instruments={35,42,46,38,49,39,50,60,70,72,64,56,68,47,67,63};
	public static void main(String[] args) {
		new BoomBox().start();
	}
	/**Builds GUI and makes initial settings*/
	public void start(){
		buildGUI();
		SetUpMidi();
	}

private void buildGUI(){	
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
	
	GridLayout grid=new GridLayout(numberOfInstruments, numberOfInstruments);
	grid.setHgap(2);
	grid.setVgap(1);
	mainPanel=new JPanel(grid);
	background.add(BorderLayout.CENTER,mainPanel);
	
	for(int i=0;i<numberOfInstruments*numberOfTicks;i++){
		JCheckBox c =new JCheckBox();
		c.setSelected(false);
		checkboxList.add(c);
		mainPanel.add(c);
	}	
	myFrame.setBounds(50,50,300,300);
	myFrame.pack();
	myFrame.setVisible(true);
}
/**Creates sequencer and makes initial settings*/
private void SetUpMidi(){
	try{
		mySequencer=MidiSystem.getSequencer();
		mySequencer.open();
		mySequence=new Sequence(Sequence.PPQ, 4);
		myTrack=mySequence.createTrack();
		mySequencer.setTempoInBPM(defaultTempo);
	}
	catch(Exception e){e.printStackTrace();}
}
/**Creates track using information from checkboxes*/
private void buildTrackAndStart(){
	int[] trackList=null;
	mySequence.deleteTrack(myTrack);
	myTrack=mySequence.createTrack();
	int key;
	for(int i=0;i<numberOfInstruments;i++){
		trackList=new int[numberOfTicks];
		key=instruments[i];
		for(int j=0;j<numberOfTicks;j++){
			JCheckBox jc=(JCheckBox)checkboxList.get(j+(numberOfTicks*i));
			if(jc.isSelected()){
				trackList[j]=key;
			}
			else{
				trackList[j]=0;
			}
		}
		makeTracks(trackList);
		myTrack.add(makeEvent(ShortMessage.CONTROL_CHANGE, 1, 0, 0,numberOfTicks));
	}
	myTrack.add(makeEvent(ShortMessage.PROGRAM_CHANGE, 9, 1, 0,numberOfTicks-1));
	try{
		mySequencer.setSequence(mySequence);
		mySequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
		mySequencer.start();
		mySequencer.setTempoInBPM(defaultTempo);
	}
	catch(Exception e){
		e.printStackTrace();
	}
	
}
/**Makes track for instrument
 * @param list Contains instrument number in the i-th place, if it should play on the i-th tick and 0 otherwise*/
private void makeTracks(int[]list){
	int key;
	for(int i=0;i<numberOfTicks;i++){
		key=list[i];
		if(key!=0){
			myTrack.add(makeEvent(ShortMessage.NOTE_ON, 9, key, noteStrenght,i));
			myTrack.add(makeEvent(ShortMessage.NOTE_OFF, 9, key, noteStrenght,i+1));
		}
	}
}
/**Creates new MIDI  event
 * @param comd MIDI command
 * @param chan channel
 * @param one first data byte
 * @param two second data byte
 * @param tick time-stamp*/
private MidiEvent makeEvent(int comd,int chan,int one,int two,int tick){
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
}
/**Writes scheme as boolean array in file
 * @param file Output file*/
private void saveInFile(File file){
	boolean[]checkboxState=new boolean[numberOfInstruments*numberOfTicks];
	for (int i=0;i<numberOfInstruments*numberOfTicks;i++) {
		JCheckBox check=checkboxList.get(i);
		if(check.isSelected()){	
			checkboxState[i]=true;
		}
	}
	try{
		FileOutputStream fileStream=new FileOutputStream(file);
		ObjectOutputStream os=new ObjectOutputStream(fileStream);
		try{
			os.writeObject(checkboxState);			
		}finally{
			os.close();
		}	
		
	}catch(Exception e){
		JOptionPane.showMessageDialog(myFrame, "Can't save in this file");
	}
}
/**Reads scheme from file
 * @param file Input file*/
private void restoreFromFile(File file){
	boolean[]checkboxState=null;
	try{
		FileInputStream fileIn=new FileInputStream(file);
		ObjectInputStream iStream=new ObjectInputStream(fileIn);
		try{
			checkboxState=(boolean[])iStream.readObject();
		}finally{
			iStream.close();
		}
	}catch (Exception e){
		JOptionPane.showMessageDialog(myFrame, "Can't read from this file");
	}
	for (int i=0;i<numberOfInstruments*numberOfTicks;i++) {
		JCheckBox check=checkboxList.get(i);
		if(checkboxState[i]){	
			check.setSelected(true);
		}
		else{
			check.setSelected(false);
		}
	}
	mySequencer.stop();
}
/**Listener for start button*/
class MyStartListener implements ActionListener{
	@Override
	public void actionPerformed(ActionEvent a){
		buildTrackAndStart();
	}
}
/**Listener for stop button*/
class MyStopListener implements ActionListener{
	@Override
	public void actionPerformed(ActionEvent a){
		mySequencer.stop();
	}
}
/**Listener for upTempo button*/
class MyUpTempoListener implements ActionListener{
	@Override
	public void actionPerformed(ActionEvent a){
		float tempoFactor=mySequencer.getTempoFactor();
		mySequencer.setTempoFactor((float)(tempoFactor*tempoUpCoef));
	}
}
/**Listener for downTempo button*/
class MyDownTempoListener implements ActionListener{
	@Override
	public void actionPerformed(ActionEvent a){
		float tempoFactor=mySequencer.getTempoFactor();
		mySequencer.setTempoFactor((float)(tempoFactor*tempoDownCoef));
	}
}
/**Listener for clearBoxes button*/
class MyClearBoxesListener implements ActionListener{
	@Override
	public void actionPerformed(ActionEvent a){
		for(JCheckBox box:checkboxList){
			box.setSelected(false);
		}
		mySequencer.stop();
	}
}
/**Listener for "Load file" menu item*/
class MyLoadFileListener implements ActionListener{
	@Override
	public void actionPerformed(ActionEvent a){
			JFileChooser fileIn=new JFileChooser();
			int res=fileIn.showOpenDialog(myFrame);
			if(res==JFileChooser.APPROVE_OPTION){		
			restoreFromFile(fileIn.getSelectedFile());
			}			
}
}
/**Listener for "Save file" menu item*/
private class MySaveFileListener implements ActionListener{
	@Override
	public void actionPerformed(ActionEvent a){
		JFileChooser fileSave=new JFileChooser();
		int res=fileSave.showSaveDialog(myFrame);
		if(res==JFileChooser.APPROVE_OPTION){
		saveInFile(fileSave.getSelectedFile());
		}
		
		}
	}

}


