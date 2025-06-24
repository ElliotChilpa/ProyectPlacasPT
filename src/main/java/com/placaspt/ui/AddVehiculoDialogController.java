// AddVehiculoDialogController.java
package com.placaspt.ui;

import com.placaspt.database.VehiculoDAO;
import com.placaspt.model.VehiculoPOJO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class AddVehiculoDialogController implements MainAware {

    @FXML private AnchorPane root;
    @FXML private TextField tfMarca;
    @FXML private TextField tfModelo;
    @FXML private TextField tfAnio;
    @FXML private TextField tfColor;

    private MainController mainController;
    private final VehiculoDAO dao = new VehiculoDAO();

    // Datos que recibimos al abrir el diálogo:
    private Integer idVehiculoExistente;     // null => estamos en modo "Alta"
    private Integer idUsuario;               // usuario fijo o temporal
    private String  tipoUsuario;             // "Fijo" o "Temporal"
    private int     idAdministrador;         // quien realiza la acción

    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    /**
     * Inicializa el diálogo con:
     *  - idUsuario: ID en la tabla usuario (fijo o temp)
     *  - tipoUsuario: "Fijo" o "Temporal"
     *  - idAdmin: quién crea o edita
     *  - existing: POJO si estamos editando (o null si es alta)
     */
    public void initData(int idUsuario,
                         String tipoUsuario,
                         int idAdministrador,
                         VehiculoPOJO existing) {
        this.idUsuario     = idUsuario;
        this.tipoUsuario   = tipoUsuario;
        this.idAdministrador = idAdministrador;
        if (existing != null) {
            // Modo edición
            this.idVehiculoExistente = existing.getIdVehiculo();
            tfMarca.setText(existing.getMarca());
            tfModelo.setText(existing.getModelo());
            tfAnio.setText(String.valueOf(existing.getAnio()));
            tfColor.setText(existing.getColor());
        } else {
            // Modo alta
            this.idVehiculoExistente = null;
            tfMarca.clear();
            tfModelo.clear();
            tfAnio.clear();
            tfColor.clear();
        }
    }

    @FXML
    private void onGuardarVehiculo() {
        // 1) Leer y validar campos
        String marca  = tfMarca.getText().trim();
        String modelo = tfModelo.getText().trim();
        int anio;
        try {
            anio = Integer.parseInt(tfAnio.getText().trim());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Año inválido").showAndWait();
            return;
        }
        String color = tfColor.getText().trim();

        if (marca.isEmpty() || modelo.isEmpty() || color.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Todos los campos son requeridos").showAndWait();
            return;
        }

        // 2) Construir el POJO
        //    Para alta: usamos el constructor sin ID
        //    Para edición: incluimos el ID existente
        VehiculoPOJO dto;
        if (idVehiculoExistente == null) {
            dto = new VehiculoPOJO(
                    marca, modelo, anio, color,
                    null,                              // fkIdPlaca (no se asigna aquí)
                    "Fijo".equals(tipoUsuario) ? idUsuario : null,
                    "Temporal".equals(tipoUsuario) ? idUsuario : null,
                    idAdministrador
            );
        } else {
            dto = new VehiculoPOJO(
                    idVehiculoExistente,
                    marca, modelo, anio, color,
                    null,
                    "Fijo".equals(tipoUsuario) ? idUsuario : null,
                    "Temporal".equals(tipoUsuario) ? idUsuario : null,
                    idAdministrador
            );
        }

        // 3) Llamar al DAO
        boolean ok = (idVehiculoExistente == null)
                ? dao.insertarVehiculo(dto)
                : dao.actualizarVehiculo(dto);

        if (!ok) {
            new Alert(Alert.AlertType.ERROR,
                    "Error al guardar el vehículo").showAndWait();
            return;
        }

        // 4) Volver a la vista de usuarios
        mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");
    }

    @FXML
    private void goBack() {
        mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");
    }
}
