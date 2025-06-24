package com.placaspt.ui;

import com.placaspt.database.PlacasDAO;
import com.placaspt.database.VehiculoDAO;
import com.placaspt.model.PlacasPOJO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class AddPlacaDialogController implements MainAware {

    @FXML private AnchorPane root;
    @FXML private TextField tfPlacaId;
    @FXML private TextField tfEntidad;
    @FXML private CheckBox chkVigente;

    private MainController mainController;
    private final PlacasDAO placaDAO = new PlacasDAO();
    private final VehiculoDAO       vehDAO   = new VehiculoDAO();

    private int    idVehiculo;
    private String placaActual;  // null → alta, no-null → edición

    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    /**
     * @param idVehiculo    PK del vehículo a modificar
     * @param placaActual   ID_Placa existente; null si es una nueva
     */
    public void initData(int idVehiculo, String placaActual) {
        this.idVehiculo   = idVehiculo;
        this.placaActual  = placaActual;

        if (placaActual != null) {
            // Precargar datos de la placa
            var p = placaDAO.buscarPorId(placaActual);
            tfPlacaId.setText(p.getIdPlaca());
            tfEntidad.setText(p.getEntidadFederativa());
            chkVigente.setSelected(p.isEstadoVigencia());
        } else {
            // Alta
            tfPlacaId.clear();
            tfEntidad.clear();
            chkVigente.setSelected(true);
        }
    }

    /*
    @FXML
    private void onGuardarPlaca() {
        // 1) Leer y validar los campos del formulario
        String nuevaPlaca = tfPlacaId.getText().trim();
        String entidad    = tfEntidad.getText().trim();
        boolean vigente   = chkVigente.isSelected();

        if (nuevaPlaca.isEmpty() || entidad.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Todos los campos son requeridos")
                    .showAndWait();
            return;
        }

        // 2) Construir el DTO de placa
        PlacasPOJO dto = new PlacasPOJO(nuevaPlaca, entidad, vigente);

        // 3) Insertar o actualizar en placavehicular
        boolean ok1;
        if (placaActual == null) {
            // alta
            ok1 = placaDAO.insertarPlaca(dto);
        } else {
            // edición
            ok1 = placaDAO.actualizarPlaca(dto);
        }

        // 4) Si fue una alta de placa, enlazarla al vehículo
        boolean ok2 = true;
        if (placaActual == null && ok1) {
            ok2 = vehDAO.asignarPlacaAVehiculo(idVehiculo, nuevaPlaca);
        }

        // 5) Comprobar que ambos pasos salieran bien
        if (ok1 && ok2) {
            // Volver a la vista de usuarios
            mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");
        } else {
            new Alert(Alert.AlertType.ERROR,
                    "Error al guardar la placa (o al asignarla al vehículo).")
                    .showAndWait();
        }
    }*/
    @FXML
    private void onGuardarPlaca() {
        String nuevaPlaca = tfPlacaId.getText().trim();
        String entidad    = tfEntidad.getText().trim();
        boolean vigente   = chkVigente.isSelected();

        // … validaciones …

        PlacasPOJO dto = new PlacasPOJO(nuevaPlaca, entidad, vigente);

        boolean ok;
        if (placaActual == null) {
            // alta (inserta + asigna)
            ok = placaDAO.insertarPlaca(dto)
                    && vehDAO.asignarPlacaAVehiculo(idVehiculo, nuevaPlaca);

        } else if (!placaActual.equals(nuevaPlaca)) {
            // renombrar: cambia PK y actualiza FK en vehiculo
            ok = placaDAO.renombrarPlaca(placaActual, nuevaPlaca);
        } else {
            // edición simple (solo campos no PK)
            ok = placaDAO.actualizarPlaca(dto);
        }

        if (ok) {
            mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");
        } else {
            new Alert(Alert.AlertType.ERROR,
                    "Error al guardar la placa (o al renombrarla).")
                    .showAndWait();
        }
    }



    @FXML
    private void onBack() {
        mainController.loadView("/com/placaspt/ui/UsuariosView.fxml");
    }
}
