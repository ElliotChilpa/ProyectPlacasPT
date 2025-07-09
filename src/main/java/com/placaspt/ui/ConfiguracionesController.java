package com.placaspt.ui;

import com.placaspt.logic.ConfigService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class ConfiguracionesController implements MainAware {
    // ESTO ES PARA ABRIR SUB-CONTROLADORES
    private MainController mainController;

    @FXML private TextField tfSerialPort;
    @FXML private TextField tfSshHost;
    @FXML private TextField tfSshUser;
    @FXML private PasswordField pfSshPass;
    @FXML private TextField tfRemoteFile;
    @FXML private Spinner<Integer> spPolling;

    // ESTO ES PARA ABRIR SUB-Controladores
    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    @FXML
    private void initialize() {
        // Cargar valores actuales
        tfSerialPort.setText(ConfigService.getSerialPort());
        tfSshHost   .setText(ConfigService.getSshHost());
        tfSshUser   .setText(ConfigService.getSshUser());
        pfSshPass   .setText(ConfigService.getSshPassword());
        tfRemoteFile.setText(ConfigService.getSshRemoteFile());

        spPolling.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 300, ConfigService.getSshPeriodSec())
        );
    }

    @FXML
    private void onSave() {
        // Guardar en Preferences
        ConfigService.setSerialPort(tfSerialPort.getText().trim());
        ConfigService.setSshHost(tfSshHost.getText().trim());
        ConfigService.setSshUser(tfSshUser.getText().trim());
        ConfigService.setSshPassword(pfSshPass.getText());
        ConfigService.setSshRemoteFile(tfRemoteFile.getText().trim());
        ConfigService.setSshPeriodSec(spPolling.getValue());

        // Reiniciar servicios con nuevos valores
        mainController.getAppService().stopRfid();
        mainController.getAppService().startRfid(ConfigService.getSerialPort());

        mainController.getAppService().stopPlacaPolling();
        mainController.getAppService().startPlacaPolling(
                ConfigService.getSshHost(),
                ConfigService.getSshUser(),
                ConfigService.getSshPassword(),
                ConfigService.getSshRemoteFile(),
                ConfigService.getSshPeriodSec(),
                java.util.concurrent.TimeUnit.SECONDS
        );

        // Volver a la vista anterior
        //mainController.loadView("/com/placaspt/ui/estacionamientoView.fxml");
        // 3) Mostrar confirmación al usuario
        new Alert(Alert.AlertType.INFORMATION,
                "Configuración guardada correctamente.")
                .showAndWait();
    }

    @FXML
    private void onCancel() {
        mainController.loadView("/com/placaspt/ui/configuracionesView.fxml");
    }

}
