package org.ssdev.WettkampfManager;

import java.io.IOException;
import java.net.URL;

/*import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.UnsupportedCommOperationException;*/

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class Main extends Application {

    private Stage primaryStage;
    private AnchorPane rootLayout;
    
    protected Brain brain = null;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("SSDeV Wettkampfmanager");

        initRootLayout();
    }
    
    public Brain getBrain() {
    	return brain;
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            URL x = Main.class.getResource("/Main.fxml");
            System.err.println(x);
            loader.setLocation(x);
            rootLayout = loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
            
            UIController controller = loader.getController();
            brain = new Brain("/dev/ttyUSB0", controller);
            brain.start();
            
            // Give the controller access to the main app.
            //UIController controller = loader.getController();
            //controller.setMainApp(this);
            
            /*while(true) {
            	IO.sleepSilent(100);
            }*/
            
            
            
        } catch (IOException e) {
            e.printStackTrace();
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
