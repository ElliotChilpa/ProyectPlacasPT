package com.placaspt.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class AgregarUsuarios implements MainAware{

    // Referencia inyectada por MainController
    private MainController mainController;
    @FXML private Button btnAgregar;

    /**
     * Recibe la referencia al MainController para poder
     * llamar a loadView(...) y cambiar la vista central.
     */
    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    /**
     * Método para regresar.
     */
    @FXML
    private void onBack() {
    mainController.goBack();
    }

}
