package org.ssdev.WettkampfManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jssc.SerialPort;
import jssc.SerialPortException;

public class Brain extends Thread {
	SerialPort myPort = null;
	boolean myShutdown = false;
	UIController myUIController = null;
	
	String myElapsedTime = null;
	String myMaximumTime = null;
	String myBrainState = null;
	
	protected ObservableList<Result> myResults = FXCollections.observableArrayList(); 
	
	public ObservableList<Result> getResults() {
		return myResults;
	}
	
	public Brain(String serialPort, UIController uicontroller) throws SerialPortException {
		myPort = new SerialPort(serialPort);
		myPort.openPort();
		myPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		
		myUIController = uicontroller;
		
		myUIController.setResults(myResults);
	}
	
	public void shutdown() {
		myShutdown = true;
	}
	
	public void run() {
		try {			
			StringBuffer curLine = new StringBuffer(256);
			
			String statusLine = null;
			boolean seenFirstTime = false;
			
			String lastTime = null;
			
			while(!myShutdown) {
				int[] ca = myPort.readIntArray(1);
				int c = ca[0];
				
				if (c == 0) {
					continue;
				}
				
				if (c == 27) {
					String line = curLine.toString();
					curLine = new StringBuffer(256);
					
					line = line.replaceFirst("^\\[[0-9]+;[0-9]+H", "").trim();
					
					if (line.isEmpty()) {
						continue;
					}
					
					if (line.indexOf("Master Controller") >= 0) {
						statusLine = line;
						continue;
					}
					
					/* Do not process any other lines without having
                       seen the status line first for synchronization */
					if (statusLine == null) {
						continue;
					}
					
					if (line.matches("^\\d:\\d\\d:\\d\\d$")) {
						if (lastTime != null) {
							myMaximumTime = line;
							myElapsedTime = lastTime;
							
				            Platform.runLater(new Runnable() {
				                @Override
				                public void run() {
				                	myUIController.updateMaximumTime(myMaximumTime);
				                	myUIController.updateElapsedTime(myElapsedTime);
				                }
				            });
						} else {
							lastTime = line;
						}
						
						continue;
					} else {
						lastTime = null;
					}
					
					if (line.matches("^(STOP|RUN|HOLD|SET_TIME)$")) {
						if (myBrainState != null && !myBrainState.equals(line)) {
							// TODO: This is a little hacky, we reset our results list
							// because we're seeing a state change from STOP to RUN
							// but this is race-prone.
							if (myBrainState.equals("STOP") && line.equals("RUN")) {
								myResults.clear();
							}
						}
						
						
						myBrainState = line;
						
			            Platform.runLater(new Runnable() {
			                @Override
			                public void run() {
			                	myUIController.updateBrainState(myBrainState);
			                }
			            });
			            
					} else if (line.matches("^\\d+\\s+\\d+\\s+\\d+\\s+\\d:\\d\\d:\\d\\d$")) {
						String[] parts = line.split("\\s+");
						if (parts.length < 4) {
							System.err.println("Received malformed result line: " + line);
						}
						
						Integer rank = Integer.valueOf(parts[0]);
						String name = "Unknown";
						
						
						if (myResults.isEmpty() && rank != 1) {
							/* Wait for a full resync if results is empty */
							continue;
						}
						
						if (myResults.size() < rank) {
							Result result = new Result(parts[0], name, parts[1], parts[2], parts[3]);
							myResults.add(result);
							
				            Platform.runLater(new Runnable() {
				                @Override
				                public void run() {
				                	myUIController.queuePushDialog(result);
				                }
				            });
							
						} else {
							Result result = myResults.get(rank - 1);
							
							if (!result.getRankProperty().get().equals(parts[0])
								|| !result.getNameProperty().get().equals(name)
								|| !result.getTableProperty().get().equals(parts[1])
								|| !result.getSeatingProperty().get().equals(parts[2])
								|| !result.getTimeProperty().get().equals(parts[3])) {
								myResults.clear();
							}
						}
						
						//System.err.println("Rank " + parts[0] + " Table " + parts[1] + " Seating " + parts[2] + " Time " + parts[3]);
					} else if (line.startsWith("Nummer") && line.endsWith("Zeit")) {
						/* Ignore header line */
					} else {
						System.err.println("Unkown line received: " + line);
					}
				} else {
					curLine.append((char) c);
				}	
			}
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}