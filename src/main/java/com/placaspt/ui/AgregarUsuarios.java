package com.placaspt.ui;

import com.placaspt.database.UsuariosDAO;
import com.placaspt.model.UsuariosPOJO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
// Esta librería se agrego en initialize por sí queremos eliminarla
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
// Para agregar archivo DNI usuario Temporal.
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import javax.sql.rowset.serial.SerialBlob;
import java.io.File;
import java.nio.file.Files;
import java.sql.Blob;
import java.time.LocalDate;
import java.io.IOException;
import java.sql.SQLException;


public class AgregarUsuarios implements MainAware{
    // Controladores FXML para el archivo FXML NOTA: Cambiar nombres mas entendibles.
    @FXML private RadioButton rbUsuarioFijo;
    @FXML private RadioButton rbUsuarioTemporal;
    @FXML private GridPane paneUsuarioTemporal;

    @FXML private TextField usuarioNombreTf;
    @FXML private TextField usuarioApellidosTf;
    @FXML private TextField usuarioCorreoTf;
    @FXML private TextField usuarioTelefonoTf;
    @FXML private TextField usuarioTempActividad;
    //@FXML private TextField tfDni;
    //@FXML private TextField tfTiempo;

    // Esta sección del codigo es para agregar documento.
    @FXML private Button btnCargarFoto;
    @FXML private ImageView dniPreview;
    private File dniFile;        // <— aquí lo declaras

    // Esta sección es para agregar fecha de inicio y fecha de fin
    @FXML private DatePicker fechaInicio;
    @FXML private DatePicker fechaFin;

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

    @FXML
    public void initialize() {
        // 1. Crear y asignar el ToggleGroup en código
        ToggleGroup userTypeGroup = new ToggleGroup();
        rbUsuarioFijo.setToggleGroup(userTypeGroup);
        rbUsuarioTemporal.setToggleGroup(userTypeGroup);

        // 2. Bind para mostrar/ocultar el panel de temporales parte del ToggleGroup
        paneUsuarioTemporal.managedProperty().bind(rbUsuarioTemporal.selectedProperty());
        paneUsuarioTemporal.visibleProperty().bind(rbUsuarioTemporal.selectedProperty());

        btnCargarFoto.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Selecciona foto de credencial");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png","*.jpg","*.jpeg")
            );
            File f = chooser.showOpenDialog(btnCargarFoto.getScene().getWindow());
            if (f != null) {
                dniFile = f;  // <— aquí guardas la referencia
                dniPreview.setImage(new Image(f.toURI().toString()));
            }
        });
    }

    /**
     * Método para regresar.
     */
    @FXML
    private void onBack() {
        mainController.goBack();
    }

    /**
     * Método para guardar.
     */
    @FXML
    private void onGuardar() {
        // 1. Crear POJO y DAO
        UsuariosPOJO u = new UsuariosPOJO(
                usuarioNombreTf.getText(),
                usuarioApellidosTf.getText(),
                usuarioCorreoTf.getText(),
                usuarioTelefonoTf.getText(),
                LocalDate.now(),
                1  // tu idAdministrador
        );
        UsuariosDAO dao = new UsuariosDAO();

        // 2. Insertar en Usuario y obtener id
        int idUsuario = dao.insertarUsuario(u);
        if (idUsuario < 1) {
            new Alert(Alert.AlertType.ERROR, "Error al crear el usuario base.").showAndWait();
            return;
        }

        // 3. Subtipo fijo o temporal
        if (rbUsuarioTemporal.isSelected()) {
            // validaciones…
            try {
                byte[] data = Files.readAllBytes(dniFile.toPath());
                Blob documento = new SerialBlob(data);

                boolean ok = dao.insertarUsuarioTemporal(
                        idUsuario,
                        usuarioTempActividad.getText(),
                        fechaInicio.getValue(),
                        fechaFin.getValue(),
                        documento
                );
                if (!ok) {
                    throw new SQLException("No se pudo insertar subregistro temporal");
                }
            } catch (IOException | SQLException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR,
                        "Error al guardar usuario temporal:\n" + ex.getMessage())
                        .showAndWait();
                return;
            }
        } else {
            dao.insertarUsuarioFijo(idUsuario);
            // — Usuario fijo —
            boolean okFijo = dao.insertarUsuarioFijo(idUsuario);
            if (!okFijo) {
                new Alert(Alert.AlertType.ERROR,
                        "Error al insertar usuario fijo.").showAndWait();
                return;
            }
        }

        // 4. Éxito
        new Alert(Alert.AlertType.INFORMATION,
                "Usuario guardado correctamente.")
                .showAndWait();
        mainController.goBack();
    }
}
