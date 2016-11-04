package org.ssdev.WettkampfManager;

import java.util.Optional;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import jssc.SerialPort;
import jssc.SerialPortException;

public class Brain extends Thread {
	SerialPort myPort = null;
	UIController myUIController = null;
	
	String myElapsedTime = null;
	String myMaximumTime = null;
	String myBrainState = null;
	
	protected ObservableList<Result> myResults = FXCollections.observableArrayList(); 
	
	public ObservableList<Result> getResults() {
		return myResults;
	}
	
	public Brain(String serialPort, UIController uicontroller) throws SerialPortException {
		if (!serialPort.isEmpty()) {
			myPort = new SerialPort(serialPort);
			myPort.openPort();
			myPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		}
		
		myUIController = uicontroller;
		
		myUIController.setResults(myResults);
	}
	
	public void run() {
		try {			
			StringBuffer curLine = new StringBuffer(256);
			String statusLine = null;
			String lastTime = null;
			
			while(true) {
				if (myPort == null) {
					// Enter Demo Mode
					
					if (myBrainState == null) {
						myBrainState = "STOP";
						
						Result r1 = new Result("1", "demo1", "3", "5", "00:00:13");
						Result r2 = new Result("2", "demo2", "1", "2", "00:01:27");
						myResults.add(r1);
						myResults.add(r2);
						
			            Platform.runLater(new Runnable() {
			                @Override
			                public void run() {
			                	myUIController.queuePushDialog(r1);
			                }
			            });
						
			            Platform.runLater(new Runnable() {
			                @Override
			                public void run() {
			                	myUIController.queuePushDialog(r2);
			                }
			            });
					}
					
					IO.sleepSilent(100);
		            continue;
				}
				
				
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
						
						if (myResults.isEmpty() && rank != 1) {
							/* Wait for a full resync if results is empty */
							continue;
						}
						
						String name = SeatMap.getInstance().getName(parts[1], parts[2]);

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
								|| !result.getTableProperty().get().equals(parts[1])
								|| !result.getSeatingProperty().get().equals(parts[2])
								|| !result.getTimeProperty().get().equals(parts[3])) {
								myResults.clear();
							} else if (!result.getNameProperty().get().equals(name)) {
								result.updateNameProperty(name);
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
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText(e.getMessage());
			Optional<ButtonType> result = alert.showAndWait();
			Platform.exit();
		}
	}
	
}
