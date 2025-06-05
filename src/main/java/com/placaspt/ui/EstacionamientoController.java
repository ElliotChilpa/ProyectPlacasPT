/*
package com.placaspt.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.*;

public class EstacionamientoController {

    @FXML
    private TableView<Estacionamiento> tablaEstacionamiento;

    @FXML
    private TableColumn<Estacionamiento, String> colNombre;

    @FXML
    private TableColumn<Estacionamiento, String> colEstado;

    @FXML
    private TableColumn<Estacionamiento, String> colPlacas;

    @FXML
    private TableColumn<Estacionamiento, String> colTipo;

    @FXML
    private TableColumn<Estacionamiento, String> colHorario;

    @FXML
    private TableColumn<Estacionamiento, String> colId;

    @FXML
    private TextField buscarField;

    @FXML
    private Button volverButton;

    private ObservableList<Estacionamiento> listaEstacionamiento = FXCollections.observableArrayList();

    private final String URL = "jdbc:mariadb://localhost:3306/tu_base_de_datos";
    private final String USER = "root";
    private final String PASSWORD = "";

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatosDesdeBD();
        agregarFiltroBusqueda();
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colPlacas.setCellValueFactory(new PropertyValueFactory<>("placas"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horario"));
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    }

    private void cargarDatosDesdeBD() {
        listaEstacionamiento.clear();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT nombre, estado, placas, tipo, horario, id FROM estacionamiento";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                listaEstacionamiento.add(new Estacionamiento(
                        rs.getString("nombre"),
                        rs.getString("estado"),
                        rs.getString("placas"),
                        rs.getString("tipo"),
                        rs.getString("horario"),
                        rs.getString("id")
                ));
            }
            tablaEstacionamiento.setItems(listaEstacionamiento);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void agregarFiltroBusqueda() {
        buscarField.textProperty().addListener((obs, oldVal, newVal) -> {
            ObservableList<Estacionamiento> filtrada = FXCollections.observableArrayList();
            for (Estacionamiento e : listaEstacionamiento) {
                if (e.getId().contains(newVal)) {
                    filtrada.add(e);
                }
            }
            tablaEstacionamiento.setItems(filtrada);
        });
    }

    @FXML
    private void volverAlDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/placaspt/ui/dashboard.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) tablaEstacionamiento.getScene().getWindow();
            currentStage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
*/
