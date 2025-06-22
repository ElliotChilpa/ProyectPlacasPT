// src/main/java/com/placaspt/ui/UsuariosController.java
package com.placaspt.ui;

import com.placaspt.database.TarjetaRFIDDAO;
import com.placaspt.database.UsuariosDAO;
import com.placaspt.model.TarjetaRFIDPOJO;
import com.placaspt.model.UsuarioViewDTO;
import com.placaspt.model.UsuariosPOJO;
import com.placaspt.database.VehiculoDAO;
import com.placaspt.model.VehiculoPOJO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;

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
     * Esta sección es para cargar los datos de la tabla.
     */
    // ───────────────────────────────────────────
    // 1) Campos FXML (inyectados desde el FXML)
    // ───────────────────────────────────────────

    @FXML private TableView<UsuarioViewDTO> tblUsuarios;
    @FXML private TableColumn<UsuarioViewDTO, Integer>  colId;
    @FXML private TableColumn<UsuarioViewDTO, String>   colNombre;
    @FXML private TableColumn<UsuarioViewDTO, String>   colCorreo;
    @FXML private TableColumn<UsuarioViewDTO, String>   colTipo;
    @FXML private TableColumn<UsuarioViewDTO, String>   colRfid;
    @FXML private TableColumn<UsuarioViewDTO, String>   colPlaca;
    @FXML private TableColumn<UsuarioViewDTO, Void>     colAccion;

    /**
     * Esta función sirve para controlar la columna acciones.
     */
    //@FXML private TableColumn<UsuarioViewDTO, Void> colAccion;
    // ───────────────────────────────────────────
    // 2) Campos de apoyo (lista y DAOs)
    // ───────────────────────────────────────────

    private final ObservableList<UsuarioViewDTO> listaUsuariosView =
            FXCollections.observableArrayList();

    private final UsuariosDAO       usuarioDAO = new UsuariosDAO();
    private final TarjetaRFIDDAO    rfidDAO    = new TarjetaRFIDDAO();
    private final VehiculoDAO vehDAO     = new VehiculoDAO();

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
    private void onNuevoUsuario() {
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
     * Este metodo lo utilizamos con un DAO mas sencillo.
     */
    /**
    @FXML
    void leerUsuarioDB() {
        usuarioBD.setText("Este es un ejemplo 1111");
        UsuariosDAO.listarUsuarios();
    }*/


    /**
     * Método de inicialización que llena automaticamente la tabla.
     */
    @FXML
    private void initialize() {
        // 1) Configuro las columnas para que lean de las propiedades del DTO
        colId.    setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colTipo.  setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colRfid.  setCellValueFactory(new PropertyValueFactory<>("rfid"));
        colPlaca. setCellValueFactory(new PropertyValueFactory<>("placa"));

        // 2) Enlazo la lista al TableView
        tblUsuarios.setItems(listaUsuariosView);

        // 2) monta la columna de acciones
        montarCellFactoryAcciones();

        // 3) enlaza la lista y carga datos
        tblUsuarios.setItems(listaUsuariosView);
        //cargarUsuariosDesdeBD();

        // 3) (Opcional) Aquí puedes llamar a tu método para poblar listaUsuariosView
        cargarUsuariosDesdeBD();
    }

    /**
     * Carga todos los usuarios desde la BD, arma los DTOs de vista
     * y los pone en la lista que alimenta el TableView.
     */
    private void cargarUsuariosDesdeBD() {
        listaUsuariosView.clear();

        // 1) Traigo todos los Usuarios
        List<UsuariosPOJO> usuarios = usuarioDAO.listarUsuarios();

        for (UsuariosPOJO u : usuarios) {
            int id = u.getId();
            String nombre = u.getNombres() + " " + u.getApellidos();
            String correo = u.getCorreo();

            // 2) Determinar si es Fijo o Temporal
            boolean esFijo = usuarioDAO.existeUsuarioFijo(id);
            // necesitarás implementar este método en UsuarioDAO:
            // public boolean existeUsuarioFijo(int idUsuario) { SELECT 1 FROM UsuarioFijo WHERE FK_Usuario = ? }

            String tipo = esFijo ? "Fijo" : "Temporal";

            // 3) Obtener RFID (solo para fijos)
            String rfid = "";
            if (esFijo) {
                TarjetaRFIDPOJO t = rfidDAO.buscarPorUsuarioFijo(id);
                if (t != null) {
                    rfid = t.getIdRfid();
                }
            }


            // 4) Obtener placa (solo para fijos)
            String placa = "";
            if (esFijo) {
                VehiculoPOJO v = vehDAO.buscarPorUsuarioFijo(id);
                if (v != null) {
                    placa = v.getPlaca();
                }
            }

            // 5) Crear el DTO y añadirlo a la lista
            UsuarioViewDTO row = new UsuarioViewDTO(
                    id, nombre, correo, tipo, rfid, placa
            );
            listaUsuariosView.add(row);
        }
    }

    /**
     * Metodo para agregar botones de editar, agregar RFID, boton agregar Vehiculo, Editar Usuario
     */
    private void montarCellFactoryAcciones() {
        colAccion.setCellFactory(col -> new TableCell<UsuarioViewDTO, Void>() {
            private final HBox container = new HBox(8);
            private final Button btnEditar = new Button("✎");
            private final Button btnRfid   = new Button("📶");
            private final Button btnVeh    = new Button("🚗");

            {
                btnEditar.setOnAction(e -> {
                    UsuarioViewDTO u = getTableView().getItems().get(getIndex());
                    editarUsuario(u);
                });
                btnRfid.setOnAction(e -> {
                    UsuarioViewDTO u = getTableView().getItems().get(getIndex());
                    onAsignarRFID(u.getId());
                });
                btnVeh.setOnAction(e -> {
                    UsuarioViewDTO u = getTableView().getItems().get(getIndex());
                    onAsignarVehiculo(u.getId());
                });
                container.getChildren().addAll(btnEditar, btnRfid, btnVeh);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UsuarioViewDTO u = getTableView().getItems().get(getIndex());
                    btnRfid.setVisible("Fijo".equals(u.getTipo()));
                    // si quisieras ocultar Vehículo para temporales:
                    // btnVeh.setVisible("Fijo".equals(u.getTipo()));
                    setGraphic(container);
                }
            }
        });
    }


    /**
     * Metodos para poder abrir pestañas de edición para RFID, Usuarios, Vehiculos
     */
    private void editarUsuario(UsuarioViewDTO u) {
        // Aquí cargas tu FXML de "Agregar/Editar Usuario",
        // le pasas el POJO u.getId() para precargar datos, etc.
    }

    /**
     * Abre el diálogo para asignar o editar la tarjeta RFID.
     * Solo se invoca cuando el usuario es "Fijo".
     */
    private void onAsignarRFID(int idUsuarioFijo) {
        // Código de ejemplo:
        // FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/AddTarjetaRFIDDialog.fxml"));
        // DialogPane pane = loader.load();
        // AddTarjetaRFIDDialogController ctrl = loader.getController();
        // TarjetaRFIDPOJO existing = rfidDAO.buscarPorUsuarioFijo(idUsuarioFijo);
        // ctrl.initData(idUsuarioFijo, existing);
        // Dialog<ButtonType> dialog = new Dialog<>();
        // dialog.setDialogPane(pane);
        // dialog.showAndWait();
        // cargarUsuariosDesdeBD();
    }

    /**
     * Abre el diálogo para asignar o editar el vehículo del usuario.
     */
    private void onAsignarVehiculo(int idUsuario) {
        // De forma análoga al RFID, cargas tu FXML de vehículo,
        // obtienes VehiculoPOJO existente con vehDAO.buscarPorUsuarioFijo(idUsuario),
        // inicializas el controller, muestras el diálogo y luego recargas la tabla.
    }

    /**
     * Método para regresar.
     */
    /**@FXML
    private void onBack() {
        mainController.goBack();
    }*/
}
