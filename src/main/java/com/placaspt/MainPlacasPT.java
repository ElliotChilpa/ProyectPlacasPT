package com.placaspt;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;


public class MainPlacasPT extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println(getClass().getResource("/com/placaspt/ui/login.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/login.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Placas PT");
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args); // Inicia JavaFX
    }
}
