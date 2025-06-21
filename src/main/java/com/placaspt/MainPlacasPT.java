/*package com.placaspt;

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
*/
package com.placaspt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainPlacasPT extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        Parent loginRoot = FXMLLoader.load(
                getClass().getResource("/com/placaspt/ui/login.fxml")
        );
        Scene scene = new Scene(loginRoot);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Placas PT");
        primaryStage.setMaximized(true);
        // primaryStage.setFullScreen(true); // si prefieres modo fullscreen
        primaryStage.show();
    }

    /** Permite que cualquier controlador recupere el Stage principal */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
