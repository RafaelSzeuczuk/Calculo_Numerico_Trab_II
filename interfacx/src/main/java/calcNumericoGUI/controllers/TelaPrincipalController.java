package calcNumericoGUI.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

/**
 * Controller da tela principal com botões para navegar entre capítulos.
 */
public class TelaPrincipalController {

    @FXML
    public void abrirCapitulo2(ActionEvent event) throws IOException {
        // Carrega a view do Capítulo 2
        Parent root = FXMLLoader.load(getClass().getResource("/calcNumericoGUI/views/capitulo2.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 900, 700));
    }

    @FXML
    public void abrirCapitulo3(ActionEvent event) throws IOException {
        // Carrega a view do Capítulo 3 (página placeholder)
        Parent root = FXMLLoader.load(getClass().getResource("/calcNumericoGUI/views/capitulo3.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 900, 700));
    }

    @FXML
    public void sair() {
        // Encerra a aplicação
        System.exit(0);
    }
}
