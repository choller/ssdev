package org.ssdev.WettkampfManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
    
    private Stage myPrimaryStage;
    
    private BlockingQueue<Result> resultQueue = new LinkedBlockingQueue<Result>();
    
    private int myResultsSize = 12;
    private int myTimeSize = 96;
    private int myStateSize = 48;
    private int myBannerSize = 96;
    private double myScalingFactor = 1.0;

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

		nameExportMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fc = new FileChooser();
				fc.setTitle("Daten nach Excel exportieren");
				File exportFile = fc.showOpenDialog(myMain.getPrimaryStage());
				
				if (exportFile != null) {
					HSSFWorkbook workbook = SeatMap.getInstance().getExcelExport();
					 
					try {
					    FileOutputStream out = new FileOutputStream(exportFile);
					    workbook.write(out);
					    workbook.close();
					    out.close();
					} catch (FileNotFoundException e) {
					    e.printStackTrace();
					} catch (IOException e) {
					    e.printStackTrace();
					}
				}
			}
		});
		
		nameImportMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fc = new FileChooser();
				fc.setTitle("Daten von Excel importieren");
				File importFile = fc.showOpenDialog(myMain.getPrimaryStage());
				
				if (importFile != null) {				 
					try {
					    FileInputStream in = new FileInputStream(importFile);
						HSSFWorkbook workbook = new HSSFWorkbook(in);
					    Integer cnt = SeatMap.getInstance().doExcelImport(workbook);
					    in.close();
					    
					    Alert alert = new Alert(AlertType.INFORMATION);
					    alert.setTitle("Import erfolgreich");
					    alert.setContentText("Es wurden " + cnt.toString() + " Datensätze importiert.");
					} catch (FileNotFoundException e) {
					    e.printStackTrace();
					} catch (IOException e) {
					    e.printStackTrace();
					}
				}
			}
		});
    	
		exportMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fc = new FileChooser();
				fc.setTitle("Daten nach Excel exportieren");
				File exportFile = fc.showOpenDialog(myMain.getPrimaryStage());
				
				if (exportFile != null) {
					HSSFWorkbook workbook = new HSSFWorkbook();
					HSSFSheet sheet = workbook.createSheet("Sample sheet");
					 
					Map<String, Object[]> data = new HashMap<String, Object[]>();
					data.put("1", new Object[] {"Nummer", "Name", "Tisch", "Platz", "Zeit"});
					
					Integer cnt = 2;
					
					for (Result r : results.getItems()) {
						data.put(cnt.toString(), new Object[] {r.getRankProperty(), r.getNameProperty(), r.getTableProperty(), r.getSeatingProperty(), r.getTimeProperty()});
						cnt++;
					}
					 
					Set<String> keyset = data.keySet();
					int rownum = 0;
					for (String key : keyset) {
					    Row row = sheet.createRow(rownum++);
					    Object [] objArr = data.get(key);
					    int cellnum = 0;
					    for (Object obj : objArr) {
					        Cell cell = row.createCell(cellnum++);
					        if(obj instanceof StringProperty)
					            cell.setCellValue(((StringProperty) obj).get());
					        else if(obj instanceof String)
					            cell.setCellValue((String)obj);
					    }
					}
					 
					try {
					    FileOutputStream out = new FileOutputStream(exportFile);
					    workbook.write(out);
					    workbook.close();
					    out.close();
					} catch (FileNotFoundException e) {
					    e.printStackTrace();
					} catch (IOException e) {
					    e.printStackTrace();
					}
				}
			}
		});
		
		closeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				myMain.getBrain().shutdown();
				Platform.exit();
			}
		});
    }

    public void setMainApp(Main main) {
        myMain = main;
    }
    
    public void layoutLabelPane() {
    	results.setStyle("-fx-font-size: " + (new Double(myResultsSize * myScalingFactor)).intValue() + "pt;");
    	elapsedTime.setStyle("-fx-font-size: " + (new Double(myTimeSize * myScalingFactor)).intValue() + "pt;");
    	maximumTime.setStyle("-fx-font-size: " + (new Double(myTimeSize * myScalingFactor)).intValue() + "pt;");
    	brainState.setStyle("-fx-font-size: " + (new Double(myStateSize * myScalingFactor)).intValue() + "pt;");
    	lastName.setStyle("-fx-font-size: " + (new Double(myBannerSize * myScalingFactor)).intValue() + "pt;");
    	lastTime.setStyle("-fx-font-size: " + (new Double(myBannerSize * myScalingFactor)).intValue() + "pt;");
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        myPrimaryStage = primaryStage;
        
        layoutLabelPane();
    	
    	myPrimaryStage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.PLUS) {
					myScalingFactor += 0.5;
					layoutLabelPane();
				} else if (event.getCode() == KeyCode.MINUS) {
					if (myScalingFactor > 0.5) {
						myScalingFactor -= 0.5;
						layoutLabelPane();
					}
				}
			}
    	});
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
    		this.brainState.setFont(Font.font(null, FontWeight.BOLD, this.brainState.getFont().getSize()));
    	} else {
    		this.brainState.setStyle("-fx-text-fill: black");
    		this.brainState.setFont(Font.font(null, FontWeight.NORMAL, this.brainState.getFont().getSize()));
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
