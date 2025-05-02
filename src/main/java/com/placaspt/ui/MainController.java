package com.placaspt.ui;

import com.placaspt.logic.RaspSSH;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML private Label printmessage;
    @FXML private TextFlow textflow;

    @FXML void onReadFile() {
        //Simulación de lectura de archivo en raspberry
        String host = "192.168.100.2"; // 🧠 IP de tu Raspberry
        String user = "placasPT";            // Usuario SSH
        String pass = "8586";     // Contraseña SSH
        String archivo = "/home/placasPT/credenciales.txt"; // Ruta real del archivo

        String resultado = RaspSSH.leerArchivoRasp(host, user, pass, archivo);

        //Alert alert = new Alert(Alert.AlertType.INFORMATION);
        //alert.setTitle("Resultado desde Raspberry Pi");
        //alert.setHeaderText("Contenido del archivo:");
        //alert.setContentText(resultado);
        //alert.showAndWait();


        // CON TEXT FLOW SOLO
        //textflow.getChildren().clear();
        //textflow.getChildren().add(new Label(resultado));

        // CON FORMATO TEXTFLOW pero CONSOLA
        //Label contenido = new Label(resultado);
        //contenido.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13;");
        //textflow.getChildren().add(contenido);

        //textflow.getChildren().clear();
        //textflow.getChildren().add(contenido);

        textflow.getChildren().clear();

        String[] lineas = resultado.split("\n");
        for (String linea : lineas) {
            Text texto = new Text(linea + "\n");
            texto.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13;");
            textflow.getChildren().add(texto);
        }
    }
    @FXML
    void abrirRFID() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/rfid.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Lector RFID - COM4");
            stage.setScene(new Scene(root));
            stage.show();

            // Cerrar esta ventana (main.fxml)
            Stage thisStage = (Stage) printmessage.getScene().getWindow();
            thisStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
