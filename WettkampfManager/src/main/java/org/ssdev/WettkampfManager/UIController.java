package org.ssdev.WettkampfManager;

import java.awt.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class UIController {
	@FXML
    private Label elapsedTime;
	
	@FXML
	private Label maximumTime;
	
	@FXML
	private Label brainState;
	
	@FXML
	private TableView<Result> results;
	
	@FXML
	private TableColumn<Result, String> rankColumn;
	
	@FXML
	private TableColumn<Result, String> nameColumn;
	
	@FXML
	private TableColumn<Result, String> tableColumn;
	
	@FXML
	private TableColumn<Result, String> seatingColumn;
	
	@FXML
	private TableColumn<Result, String> timeColumn;
	
	@FXML
    private Label lastName;
	
	@FXML
	private Label lastTime;
	
	@FXML
	private MenuItem exportMenuItem;
	
	@FXML
	private MenuItem nameImportMenuItem;
	
	@FXML
	private MenuItem nameExportMenuItem;
	
	@FXML
	private MenuItem closeMenuItem;
	
    private Main myMain;
    
    private BlockingQueue<Result> resultQueue = new LinkedBlockingQueue<Result>();

    public UIController() {}

    @FXML
    private void initialize() {    	
    	rankColumn.setCellValueFactory(cellData -> cellData.getValue().getRankProperty());
    	nameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
    	tableColumn.setCellValueFactory(cellData -> cellData.getValue().getTableProperty());
    	seatingColumn.setCellValueFactory(cellData -> cellData.getValue().getSeatingProperty());
    	timeColumn.setCellValueFactory(cellData -> cellData.getValue().getTimeProperty());
    	
    	lastName.setVisible(false);
    	lastTime.setVisible(false);
    	
    	results.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
    			@Override
    			public void handle(KeyEvent event) {
    				if (event.getCode() == KeyCode.F2) {
	    				Result selectedResult = results.getSelectionModel().getSelectedItem();
	    				if (selectedResult == null) {
	    					return;
	    				}
	    				
	    				String selectedName = selectedResult.getNameProperty().getValue();
	    				String selectedSeating = selectedResult.getSeatingProperty().getValue();
	    				String selectedTable = selectedResult.getTableProperty().getValue();
	    					    				
	    				TextInputDialog dialog = new TextInputDialog(selectedName);
	    				dialog.setTitle("Teilnehmer umbenennen");
	    				dialog.setHeaderText("Tisch " + selectedTable + " Platz " + selectedSeating);
	    				dialog.setContentText("Neuer Name für Teilnehmer:");
	    				
	    				Optional<String> result = dialog.showAndWait();
	    				
	    				if (result.isPresent()) {
	    					SeatMap.getInstance().changeName(selectedTable, selectedSeating, result.get());
	    				}
    				}
    			}
    	});
    	
    }

    public void setMainApp(Main main) {
        myMain = main;
    }
    
    public void setResults(ObservableList<Result> results) {
    	this.results.setItems(results);
    }
    
    public void updateElapsedTime(String elapsedTime) {
    	this.elapsedTime.setText(elapsedTime);
    }
    
    public void updateMaximumTime(String maximumTime) {
    	this.maximumTime.setText(maximumTime);
    }
    
    public void updateBrainState(String brainState) {
    	this.brainState.setText(brainState);
    	
    	if (brainState.equals("SET_TIME")) {
    		this.brainState.setStyle("-fx-text-fill: red");
    		this.brainState.setFont(Font.font(null, FontWeight.BOLD, 48));
    	} else {
    		this.brainState.setStyle("-fx-text-fill: black");
    		this.brainState.setFont(Font.font(null, FontWeight.NORMAL, 48));
    	}
    }
    
    public void queuePushDialog(Result result) {
    	boolean wasEmpty = resultQueue.isEmpty();
    	
    	resultQueue.add(result);
    	
    	if (wasEmpty) {
    		System.err.println("Was empty");
    		triggerPushDialog();
    	}
    }
    
    public void triggerPushDialog() {
    	Result result = resultQueue.peek();
    	
    	if (result == null) {
    		return;
    	}
    	
		lastName.setText(result.getNameProperty().get());
		lastTime.setText(result.getTimeProperty().get());

		lastName.setVisible(true);
		lastTime.setVisible(true);

		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10000), ae -> {
			lastName.setVisible(false);
			lastTime.setVisible(false);
			
			resultQueue.remove();
			
			triggerPushDialog();
			
		}));
		timeline.play();
    }
    
}
