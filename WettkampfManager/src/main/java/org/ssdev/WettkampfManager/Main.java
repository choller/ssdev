package org.ssdev.WettkampfManager;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/*import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.UnsupportedCommOperationException;*/

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
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
            controller.setMainApp(this);
            
            brain = new Brain("/dev/ttyUSB0", controller);
            brain.start();
        } catch (IOException e) {
            e.printStackTrace();
		} catch (SerialPortException e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.setTitle(e.getExceptionType());
			alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));
			Optional<ButtonType> result = alert.showAndWait();
			Platform.exit();
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
