package com.placaspt.ui;

import com.placaspt.logic.RS232RFID;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class RFIDController {

    @FXML private Label etiquetaLectura;
    @FXML private Button btnIniciar;

    private final RS232RFID lector = new RS232RFID();

    @FXML
    public void initialize() {
        btnIniciar.setOnAction(e -> iniciarLectura());
    }

    private void iniciarLectura() {
        boolean conectado = lector.iniciar("COM4");

        if (conectado) {
            etiquetaLectura.setText("✅ Puerto COM4 abierto. Escuchando...");
            lector.escuchar(datos -> Platform.runLater(() -> {
                System.out.println("Lectura RFID: " + datos);
                etiquetaLectura.setText("📡 Lectura: " + datos);
            }));
        } else {
            etiquetaLectura.setText("❌ No se pudo abrir COM4");
        }
    }
}

