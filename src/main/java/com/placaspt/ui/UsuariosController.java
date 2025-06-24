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
    @FXML private TableColumn<UsuarioViewDTO, Void>    colVehiculo;

    private final ObservableList<UsuarioViewDTO> listaUsuariosView =
            FXCollections.observableArrayList();

    private final UsuariosDAO    usuarioDAO = new UsuariosDAO();
    private final TarjetaRFIDDAO rfidDAO    = new TarjetaRFIDDAO();
    // Este DAO es para asignar vehiculos a los Usuarios.
    private final VehiculoDAO    vehDAO     = new VehiculoDAO();
    // Este DAO es para asignar placas a los vehiculos.
    private final VehiculoDAO    placasDAO     = new VehiculoDAO();

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
        // Columna agregada para vehiculo
        colVehiculo.setCellValueFactory(new PropertyValueFactory<>("vehiculo"));

        // 2) Montaje de la columna de acciones (Editar Usuario, Editar RFID, Editar
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

            // LLenado de columnas tabla Usuarios
            boolean esFijo = usuarioDAO.existeUsuarioFijo(id);
            String tipo = esFijo ? "Fijo" : "Temporal";

            String rfid = "";
            if (esFijo) {
                TarjetaRFIDPOJO t = rfidDAO.buscarPorUsuarioFijo(
                        usuarioDAO.obtenerIdUsuarioFijo(id)
                );
                if (t != null) rfid = t.getIdRfid();
            }

            // Para una implementación futura de placavehicular
            String placa = "";
            if (esFijo) {
                /*
                VehiculoPOJO v = vehDAO.buscarPorUsuarioFijo(
                        usuarioDAO.obtenerIdUsuarioFijo(id)
                );
                if (v != null) placa = v.getPlaca();*/
                VehiculoPOJO v = vehDAO.buscarPrimeroPorUsuarioFijo(usuarioDAO.obtenerIdUsuarioFijo(id));
                if (v != null && v.getFkIdPlaca() != null) {
                    placa = v.getFkIdPlaca();
                }
            }

            // 3) Vehículo (marca + modelo)
            String vehiculo = "";
            if (esFijo) {
                // llamamos al helper, no al método que devuelve lista
                VehiculoPOJO v = vehDAO.buscarPrimeroPorUsuarioFijo(
                        usuarioDAO.obtenerIdUsuarioFijo(id)
                );
                if (v != null) {
                    vehiculo = v.getMarca() + " " + v.getModelo();
                }
            }

            /*
            String vehiculo = "";
            if (esFijo) {
                /*
                VehiculoPOJO v = vehDAO.buscarPorUsuarioFijo(
                        usuarioDAO.obtenerIdUsuarioFijo(id)
                );
                if (v != null) placa = v.getPlaca();* /
                VehiculoPOJO v = vehDAO.buscarPrimeroPorUsuarioFijo(usuarioDAO.obtenerIdUsuarioFijo(id));
                if (v != null && v.getFkIdPlaca() != null) {
                    placa = v.getFkIdPlaca();
                }
            }*/

            listaUsuariosView.add(new UsuarioViewDTO(
                    id, nombre, correo, tipo, rfid, placa, vehiculo
            ));
        }
    }

    private void montarCellFactoryAcciones() {
        colAccion.setCellFactory(col -> new TableCell<UsuarioViewDTO, Void>() {
            private final HBox container = new HBox(8);
            private final Button btnEditar = new Button("Editar Usuario");
            //private final Button btnRfid   = new Button("");
            private final Button btnRfid   = new Button();
            private final Button btnPlaca    = new Button();
            //private final Button btnPlca    = new Button("🚗");
            private final Button btnVeh    = new Button();

            {
                // Esta función es nueva para poder Editar RFID
                container.getChildren().addAll(btnEditar, btnRfid, btnPlaca, btnVeh);

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
                // Aquí agregamos btn PLACA
                btnPlaca.setOnAction(e -> {
                    UsuarioViewDTO u = getTableView().getItems().get(getIndex());
                    onAsignarPlaca(u.getId());
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

                    //Para preguntar si tiene vehiculo.
                    boolean tieneVehiculo = !u.getVehiculo().isEmpty();

                    // Para preguntar si tiene placa
                    boolean tienePlaca = !u.getPlaca().isEmpty();

                    // Texto y tooltip dinámico para Cuando Tiene RFID
                    btnRfid.setText(tieneRfid ? "Editar RFID" : "Agregar RFID");
                    btnRfid.setTooltip(new Tooltip(tieneRfid ? "Editar RFID" : "Asignar RFID"));

                    // Texto y tooltip dinámico para Cuando tiene Vehiculo
                    btnVeh.setText(tieneVehiculo ? "Editar Vehiculo" : "Agregar Vehiculo");
                    btnVeh.setTooltip(new Tooltip(tieneVehiculo ? "Editar Vehiculo" : "Asignar Vehiculo"));

                    // Texto y tooltip dinámico para Cuando tiene Placa
                    btnPlaca.setText(tienePlaca ? "Editar Placa" : "Agregar Placa");
                    btnPlaca.setTooltip(new Tooltip(tienePlaca ? "Editar Placa" : "Asignar Placa"));

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

    /*
    @FXML
    private void onAsignarVehiculo(int idUsuario) {
        // 1) Determinar si es usuario fijo o temporal
        boolean esFijo = usuarioDAO.existeUsuarioFijo(idUsuario);
        int idRegistro;
        String tipoUsuario;
        if (esFijo) {
            idRegistro   = usuarioDAO.obtenerIdUsuarioFijo(idUsuario);
            tipoUsuario  = "Fijo";
        } else {
            // Necesitas añadir este método en UsuariosDAO:
            // public int obtenerIdUsuarioTemporal(int idUsuario) { ... }
            idRegistro   = usuarioDAO.obtenerIdUsuarioTemporal(idUsuario);
            tipoUsuario  = "Temporal";
        }

        // 2) Cargar FXML
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/placaspt/ui/AddVehiculoDialog.fxml")
            );
            AnchorPane pane = loader.load();

            // 3) Inicializar el controller
            AddVehiculoDialogController ctrl = loader.getController();
            ctrl.setMainController(mainController);

            // 4) Buscar vehículo existente (tomamos el primero si hay varios)
            List<VehiculoPOJO> vehiculos = esFijo
                    ? vehDAO.buscarPorUsuarioFijo(idRegistro)
                    : vehDAO.buscarPorUsuarioTemporal(idRegistro);

            VehiculoPOJO existente = vehiculos.isEmpty() ? null : vehiculos.get(0);

            // 5) Pasar todos los datos a initData:
            //     idUsuarioRegistro = PK en UsuarioFijo o UsuarioTemporal
            //     tipoUsuario      = "Fijo" o "Temporal"
            //     idAdministrador  = quien hace la acción (aquí lo dejamos en 1, o puedes obtenerlo de tu MainController)
            //     existente        = objeto para modo edición, o null para alta
            ctrl.initData(
                    idRegistro,
                    tipoUsuario,
                    1,
                    existente
            );

            // 6) Mostrar la vista
            mainController.setContent(pane);

        } catch (IOException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "No se pudo abrir la vista de Vehículo:\n" + ex.getMessage())
                    .showAndWait();
        }
    }*/
    @FXML
    private void onAsignarVehiculo(int idUsuario) {
        // 1) Determinar si es usuario fijo o temporal
        boolean esFijo = usuarioDAO.existeUsuarioFijo(idUsuario);
        int idRegistro = esFijo
                ? usuarioDAO.obtenerIdUsuarioFijo(idUsuario)
                : usuarioDAO.obtenerIdUsuarioTemporal(idUsuario);
        String tipoUsuario = esFijo ? "Fijo" : "Temporal";

        // 2) Cargar el FXML de AddVehiculo
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/placaspt/ui/AddVehiculoDialog.fxml")
            );
            AnchorPane pane = loader.load();

            // 3) Inicializar el controller
            AddVehiculoDialogController ctrl = loader.getController();
            ctrl.setMainController(mainController);

            // 4) Buscar vehículo existente (tomamos el primero si hay varios)
            List<VehiculoPOJO> vehiculos = esFijo
                    ? vehDAO.buscarPorUsuarioFijo(idRegistro)
                    : vehDAO.buscarPorUsuarioTemporal(idRegistro);
            VehiculoPOJO existente = vehiculos.isEmpty() ? null : vehiculos.get(0);

            // 5) Pasar datos a initData:
            //    - idRegistro: PK en UsuarioFijo o UsuarioTemporal
            //    - tipoUsuario: "Fijo" o "Temporal"
            //    - idAdministrador: quien hace la acción (ajústalo a tu lógica real)
            //    - existente: objeto para modo edición, o null para alta
            int idAdministrador = 1; // o bien: mainController.getAdministradorActual().getId()
            ctrl.initData(idRegistro, tipoUsuario, idAdministrador, existente);

            // 6) Mostrar la vista dentro de tu área de contenido
            mainController.setContent(pane);

        } catch (IOException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "No se pudo abrir la vista de Vehículo:\n" + ex.getMessage())
                    .showAndWait();
        }
    }



    @FXML
    private void onAsignarPlaca(int idUsuario) {
        // 1) ¿Fijo o Temporal?
        boolean esFijo = usuarioDAO.existeUsuarioFijo(idUsuario);
        int idRegistro = esFijo
                ? usuarioDAO.obtenerIdUsuarioFijo(idUsuario)
                : usuarioDAO.obtenerIdUsuarioTemporal(idUsuario);
        String tipoUsuario = esFijo ? "Fijo" : "Temporal";

        // 2) Recupera el vehículo (tomamos el primero si hay varios)
        List<VehiculoPOJO> vehs = esFijo
                ? vehDAO.buscarPorUsuarioFijo(idRegistro)
                : vehDAO.buscarPorUsuarioTemporal(idRegistro);

        if (vehs.isEmpty()) {
            new Alert(Alert.AlertType.ERROR,
                    "Este usuario aún no tiene un vehículo asignado.")
                    .showAndWait();
            return;
        }

        VehiculoPOJO veh = vehs.get(0);
        String placaActual = veh.getFkIdPlaca(); // puede ser null

        // 3) Cargar el FXML de AddPlacaDialog
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/placaspt/ui/AddPlacaDialog.fxml")
            );
            AnchorPane pane = loader.load();

            // 4) Inicializar el controller
            AddPlacaDialogController ctrl = loader.getController();
            ctrl.setMainController(mainController);

            // 5) Pasar datos al diálogo:
            //    - idVehiculo
            //    - placaActual (null→alta, no-null→edición)
            ctrl.initData(veh.getIdVehiculo(), placaActual);

            // 6) Mostrar en tu content area
            mainController.setContent(pane);

        } catch (IOException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "No se pudo abrir la vista de Placa:\n" + ex.getMessage())
                    .showAndWait();
        }
    }

}
