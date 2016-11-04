package org.ssdev.WettkampfManager;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/*import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.UnsupportedCommOperationException;*/

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jssc.SerialPortException;

public class Main extends Application {

    private Stage primaryStage;
    private Pane rootLayout;
    
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
            Optional<String> result = null;
            
            do {
            	TextInputDialog dialog = new TextInputDialog("Port auswählen");
				dialog.setTitle("Port auswählen");
				dialog.setHeaderText("Seriellen Port angeben (z.B. COM4 oder /dev/ttyUSB0)");
				dialog.setContentText("Port für Verbindung:");
				result = dialog.showAndWait();
            } while(!result.isPresent());
            
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            URL x = Main.class.getResource("/Main.fxml");
            System.err.println(x);
            loader.setLocation(x);
            rootLayout = loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.setScene(scene);
            primaryStage.show();
            
            UIController controller = loader.getController();
            controller.setMainApp(this);
            controller.setPrimaryStage(primaryStage);
            
            brain = new Brain(result.get(), controller);
            
            /* 
             * Don't bother cleaning up brain thread, it doesn't need
             * any special care for shutting down
             */
            brain.setDaemon(true);
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
