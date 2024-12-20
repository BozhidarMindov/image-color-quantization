package main.java.views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents the main view for the Image Color Quantizer application.
 * This class launches the JavaFX app and loads its FXML layout.
 */
public class QuantizerView extends Application {
    /**
     * The entrypoint of the application. It calls the launch method to start the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts the JavaFX application by loading the FXML layout and setting up the primary stage.
     *
     * @param primaryStage the primary stage for the JavaFX application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("QuantizerView.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setTitle("Image Color Quantizer");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
