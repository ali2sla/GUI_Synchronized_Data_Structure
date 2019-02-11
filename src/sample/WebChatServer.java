package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WebChatServer extends Application {

    private WebChatController myController;

    // Change multicastMode to enable multi-cast
    static boolean multicastMode = true;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load View from xml description
        FXMLLoader loader = new FXMLLoader(getClass().getResource("WebChatDisplay.fxml"));
        Parent root = loader.load();

        Thread.currentThread().setName("TwoWayCommunicationController MainServer GUI Thread");

        // Display the scene
        if (multicastMode) {
            primaryStage.setTitle("TwoWayCommunicationController SERVER Multi-cast");
        } else {
            primaryStage.setTitle("TwoWayCommunicationController SERVER");
        }
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

       // myController = loader.getController();
        //myController.setServerMode();
    }

    public static void main(String[] args) {
        launch(args);
    }
}