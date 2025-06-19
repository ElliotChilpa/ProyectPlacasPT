// src/main/java/com/placaspt/ui/UsuariosController.java
package com.placaspt.ui;

import com.placaspt.database.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;

/**
 * Controlador de la vista de Usuarios.
 * Permite abrir sub-vistas de Usuarios Fijos y Temporales
 * usando el menú desplegable.
 */
public class UsuariosController implements MainAware {

    // Referencia inyectada por MainController
    private MainController mainController;

    // controlador de MenuButton.
    // @FXML private MenuButton btnUsuariosMenu;

    @FXML private Button btnAgregarUsuarios;

    @FXML private Label usuarioBD;

    /**
     * Recibe la referencia al MainController para poder
     * llamar a loadView(...) y cambiar la vista central.
     */
    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    /**
     * Manejador de la opción "Agregar Usuarios"
     * en el Button.
     */

    @FXML
    private void onAgregarUsuarios() {
        mainController.loadView("/com/placaspt/ui/agregarUsuarios.fxml");
    }

    /**
     * Manejador de la opción "Agregar Usuarios Fijos"z
     * en el MenuButton.
     */
    /*
    @FXML
    private void onAgregarUsuariosFijos() {
        // mainController.loadView("/com/placaspt/ui/AgregarUsuariosFijos.fxml");
    }*/

    /**
     * Manejador de la opción "Agregar Usuarios Temporales"
     * en el MenuButton.
     */
    /*
    @FXML
    private void onAgregarUsuariosTemporales() {
        // mainController.loadView("/com/placaspt/ui/AgregarUsuariosTemporales.fxml");
    }*/

    /**
     * Ejemplo de método que lee de la base de datos
     * y muestra algo en un Label.
     */
    @FXML
    void leerUsuarioDB() {
        usuarioBD.setText("Este es un ejemplo 1111");
        UsuarioDAO.listarUsuarios();
    }


    /**
     * Método de inicialización opcional.
     */
    @FXML
    private void initialize() {
        // Si necesitas poblar algo al arrancar, hazlo aquí.
    }

    /**
     * Método para regresar.
     */
    /**@FXML
    private void onBack() {
        mainController.goBack();
    }*/
}
