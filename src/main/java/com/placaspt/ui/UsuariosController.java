package com.placaspt.ui;

import com.placaspt.database.TarjetaRFIDDAO;
import com.placaspt.database.UsuariosDAO;
import com.placaspt.database.VehiculoDAO;
import com.placaspt.model.TarjetaRFIDPOJO;
import com.placaspt.model.UsuarioViewDTO;
import com.placaspt.model.UsuariosPOJO;
import com.placaspt.model.VehiculoPOJO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.List;

public class UsuariosController implements MainAware {

    private MainController mainController;

    @FXML private Button btnAgregarUsuarios;

    @FXML private TableView<UsuarioViewDTO> tblUsuarios;
    @FXML private TableColumn<UsuarioViewDTO, Integer> colId;
    @FXML private TableColumn<UsuarioViewDTO, String>  colNombre;
    @FXML private TableColumn<UsuarioViewDTO, String>  colCorreo;
    @FXML private TableColumn<UsuarioViewDTO, String>  colTipo;
    @FXML private TableColumn<UsuarioViewDTO, String>  colRfid;
    @FXML private TableColumn<UsuarioViewDTO, String>  colPlaca;
    @FXML private TableColumn<UsuarioViewDTO, Void>    colAccion;

    private final ObservableList<UsuarioViewDTO> listaUsuariosView =
            FXCollections.observableArrayList();

    private final UsuariosDAO    usuarioDAO = new UsuariosDAO();
    private final TarjetaRFIDDAO rfidDAO    = new TarjetaRFIDDAO();
    private final VehiculoDAO    vehDAO     = new VehiculoDAO();

    @Override
    public void setMainController(MainController main) {
        this.mainController = main;
    }

    @FXML
    private void onNuevoUsuario() {
        mainController.loadView("/com/placaspt/ui/agregarUsuarios.fxml");
    }

    @FXML
    private void initialize() {
        // 1) Configuración de columnas
        colId.   setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colTipo. setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colRfid. setCellValueFactory(new PropertyValueFactory<>("rfid"));
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));

        // 2) Montaje de la columna de acciones (✎, 📶, 🚗)
        montarCellFactoryAcciones();

        // 3) Enlace de la lista y carga inicial de datos
        tblUsuarios.setItems(listaUsuariosView);
        cargarUsuariosDesdeBD();
    }

    private void cargarUsuariosDesdeBD() {
        listaUsuariosView.clear();
        List<UsuariosPOJO> usuarios = usuarioDAO.listarUsuarios();

        for (UsuariosPOJO u : usuarios) {
            int id = u.getId();
            String nombre = u.getNombres() + " " + u.getApellidos();
            String correo = u.getCorreo();

            boolean esFijo = usuarioDAO.existeUsuarioFijo(id);
            String tipo = esFijo ? "Fijo" : "Temporal";

            String rfid = "";
            if (esFijo) {
                TarjetaRFIDPOJO t = rfidDAO.buscarPorUsuarioFijo(
                        usuarioDAO.obtenerIdUsuarioFijo(id)
                );
                if (t != null) rfid = t.getIdRfid();
            }

            String placa = "";
            if (esFijo) {
                VehiculoPOJO v = vehDAO.buscarPorUsuarioFijo(
                        usuarioDAO.obtenerIdUsuarioFijo(id)
                );
                if (v != null) placa = v.getPlaca();
            }

            listaUsuariosView.add(new UsuarioViewDTO(
                    id, nombre, correo, tipo, rfid, placa
            ));
        }
    }

    private void montarCellFactoryAcciones() {
        colAccion.setCellFactory(col -> new TableCell<UsuarioViewDTO, Void>() {
            private final HBox container = new HBox(8);
            private final Button btnEditar = new Button("✎");
            //private final Button btnRfid   = new Button("📶");
            private final Button btnRfid   = new Button();
            private final Button btnVeh    = new Button("🚗");

            {
                // Esta función es nueva para poder Editar RFID
                container.getChildren().addAll(btnEditar, btnRfid, btnVeh);

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
                //container.getChildren().addAll(btnEditar, btnRfid, btnVeh);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UsuarioViewDTO u = getTableView().getItems().get(getIndex());
                    // Esto es para asignar propiedades al boton RFID
                    //btnRfid.setVisible("Fijo".equals(u.getTipo()));
                    // opcional: btnVeh.setVisible("Fijo".equals(u.getTipo()));
                    boolean tieneRfid = !u.getRfid().isEmpty();

                    // Texto y tooltip dinámico
                    btnRfid.setText(tieneRfid ? "Editar RFID / " : "📶");
                    btnRfid.setTooltip(new Tooltip(tieneRfid ? "Editar RFID" : "Asignar RFID"));

                    // Sólo mostrar para fijos
                    btnRfid.setVisible("Fijo".equals(u.getTipo()));
                    setGraphic(container);
                    //setGraphic(container);
                }
            }
        });
    }

    private void editarUsuario(UsuarioViewDTO u) {
        // TODO: cargar "agregarUsuarios.fxml" en modo edición
        // mainController.loadView(...); e initData(u.getId());
    }

    private void onAsignarRFID(int idUsuario) {
        // 1) Traduce ID_Usuario -> ID_Usuario_Fijo
        int idUsuarioFijo = usuarioDAO.obtenerIdUsuarioFijo(idUsuario);
        if (idUsuarioFijo < 0) {
            new Alert(Alert.AlertType.ERROR,
                    "Ese usuario no está registrado como Usuario Fijo.")
                    .showAndWait();
            return;
        }

        try {
            // 2) Carga el FXML manualmente
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/placaspt/ui/addTarjetaRFIDDialog.fxml"));
            AnchorPane pane = loader.load();

            // 3) Inicializa el controller ANTES de mostrar
            AddTarjetaRFIDDialogController ctrl = loader.getController();
            ctrl.setMainController(mainController);
            // Esto es para verificar si el id de usuario exist ID de usuarioFIjo
            TarjetaRFIDPOJO existente =
                    rfidDAO.buscarPorUsuarioFijo(idUsuarioFijo);
            ctrl.initData(idUsuarioFijo, existente);

            // 4) Muestra el pane precargado en tu contentArea
            mainController.setContent(pane);

        } catch (IOException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "No se pudo abrir la vista de RFID:\n" + ex.getMessage())
                    .showAndWait();
        }
    }


    private void onAsignarVehiculo(int idUsuario) {
        // TODO: análogo a RFID pero para vehículo
    }
}
