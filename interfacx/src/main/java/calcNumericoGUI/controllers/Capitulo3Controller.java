package calcNumericoGUI.controllers;

import calcNumericoGUI.metodos.ResultadoSolucao;
import calcNumericoGUI.metodos.gauss.FatoracaoCholesky;
import calcNumericoGUI.metodos.gauss.GaussPivoteamentoCompleto;
import calcNumericoGUI.metodos.gauss.GaussEliminacaoSimples;
import calcNumericoGUI.metodos.gauss.GaussPivoteamentoParcial;
import calcNumericoGUI.metodos.gauss.Jacobi;
import calcNumericoGUI.metodos.gauss.FatoracaoLU;
import calcNumericoGUI.metodos.gauss.GaussSeidel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

public class Capitulo3Controller {

    @FXML private Spinner<Integer> spinnerN;
    @FXML private ChoiceBox<String> choiceMetodo;
    @FXML private GridPane gridMatrix;
    @FXML private TextField tfTolerance;
    @FXML private TextField tfMaxIter;
    @FXML private CheckBox cbShowSteps;
    @FXML private TextArea taOutput;

    private TextField[][] matrixFields;
    private TextField[] vectorFields;
    private TextField[] x0Fields;

    @FXML
    public void initialize() {
        spinnerN.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));
        spinnerN.setEditable(true);

        choiceMetodo.getItems().clear();
        choiceMetodo.getItems().addAll(
                "Eliminação de Gauss (sem pivoteamento)",
                "Gauss com pivoteamento parcial",
                "Gauss com pivoteamento completo",
                "Fatoração LU (Doolittle)",
                "Fatoração de Cholesky",
                "Gauss-Jacobi (iterativo)",
                "Gauss-Seidel (iterativo)"
        );
        choiceMetodo.getSelectionModel().selectFirst();

        choiceMetodo.getSelectionModel().selectedItemProperty().addListener((obs,oldv,newv) -> {
            adaptarCamposParaMetodo(newv);
        });

        gerarGrade(null);

        Platform.runLater(() -> adaptarCamposParaMetodo(choiceMetodo.getSelectionModel().getSelectedItem()));
    }

    private void adaptarCamposParaMetodo(String metodo) {
        boolean isIterativo = metodo != null && (metodo.contains("Jacobi") || metodo.contains("Seidel"));
        boolean isCholesky = metodo != null && metodo.toLowerCase().contains("cholesky");
        boolean isDireto = !isIterativo;

        tfTolerance.setDisable(!isIterativo);
        tfMaxIter.setDisable(!isIterativo);

        if (isDireto) {
            tfTolerance.setText("0.0001");
        } else {
            tfTolerance.setText("0.0001");
        }

        if (vectorFields != null && x0Fields != null) {
            for (int i = 0; i < vectorFields.length; i++) {
                vectorFields[i].setDisable(false);
                x0Fields[i].setDisable(!isIterativo);

                if (!isIterativo) {
                    x0Fields[i].setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #666666;");
                    x0Fields[i].setPromptText("Não usado");
                } else {
                    x0Fields[i].setStyle("");
                    x0Fields[i].setPromptText("x" + (i + 1) + "⁰");
                }
            }
        }

        taOutput.appendText("Selecionado: " + metodo + "\n");
        if (isCholesky) taOutput.appendText("Cholesky: checa simetria e definicao positiva.\n");
        if (isIterativo) taOutput.appendText("Método iterativo: informe x⁰ (coluna à direita).\n");
        if (isDireto) taOutput.appendText("Método direto: tolerância e x⁰ não são necessários.\n");
    }

    @FXML
    public void voltar(ActionEvent event) throws IOException {
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/calcNumericoGUI/views/tela_principal.fxml"));
        stage.getScene().setRoot(loader.load());
    }

    @FXML
    public void gerarGrade(ActionEvent event) {
        int n = spinnerN.getValue();
        gridMatrix.getChildren().clear();
        matrixFields = new TextField[n][n];
        vectorFields = new TextField[n];
        x0Fields = new TextField[n];

        for (int j = 0; j < n; j++) {
            Label lbl = new Label("a" + (1) + "," + (j + 1));
            GridPane.setConstraints(lbl, j, 0);
            gridMatrix.getChildren().add(lbl);
        }
        Label lblB = new Label("b");
        GridPane.setConstraints(lblB, n, 0);
        gridMatrix.getChildren().add(lblB);
        Label lblX0 = new Label("x⁰");
        GridPane.setConstraints(lblX0, n + 1, 0);
        gridMatrix.getChildren().add(lblX0);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                TextField tf = new TextField();
                tf.setPrefWidth(100);
                tf.setPromptText("0");
                matrixFields[i][j] = tf;
                GridPane.setConstraints(tf, j, i + 1);
                gridMatrix.getChildren().add(tf);
                GridPane.setHgrow(tf, Priority.ALWAYS);
            }
            TextField tfB = new TextField();
            tfB.setPrefWidth(100);
            tfB.setPromptText("0");
            vectorFields[i] = tfB;
            GridPane.setConstraints(tfB, n, i + 1);
            gridMatrix.getChildren().add(tfB);

            TextField tfx0 = new TextField();
            tfx0.setPrefWidth(100);
            tfx0.setPromptText("x" + (i + 1) + "⁰");
            x0Fields[i] = tfx0;
            GridPane.setConstraints(tfx0, n + 1, i + 1);
            gridMatrix.getChildren().add(tfx0);
        }

        gridMatrix.setMinWidth(Region.USE_PREF_SIZE);
        adaptarCamposParaMetodo(choiceMetodo.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void preencherZeros(ActionEvent event) {
        if (matrixFields == null) return;
        String metodo = choiceMetodo.getSelectionModel().getSelectedItem();
        boolean isIterativo = metodo.contains("Jacobi") || metodo.contains("Seidel");

        for (int i = 0; i < matrixFields.length; i++) {
            for (int j = 0; j < matrixFields.length; j++) matrixFields[i][j].setText("0");
            vectorFields[i].setText("0");
            if (isIterativo) {
                x0Fields[i].setText("0");
            }
        }
    }

    private double[][] lerMatrizA() throws NumberFormatException {
        int n = matrixFields.length;
        double[][] A = new double[n][n];
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
            String s = matrixFields[i][j].getText();
            if (s == null || s.trim().isEmpty()) s = "0";
            A[i][j] = Double.parseDouble(s.trim());
        }
        return A;
    }

    private double[] lerVetorB() throws NumberFormatException {
        int n = vectorFields.length;
        double[] b = new double[n];
        for (int i = 0; i < n; i++) {
            String s = vectorFields[i].getText();
            if (s == null || s.trim().isEmpty()) s = "0";
            b[i] = Double.parseDouble(s.trim());
        }
        return b;
    }

    private double[] lerX0() {
        int n = x0Fields.length;
        double[] x0 = new double[n];
        String metodo = choiceMetodo.getSelectionModel().getSelectedItem();
        boolean isIterativo = metodo.contains("Jacobi") || metodo.contains("Seidel");

        for (int i = 0; i < n; i++) {
            if (!isIterativo) {
                x0[i] = 0.0;
                continue;
            }

            String s = x0Fields[i].getText();
            if (s == null || s.trim().isEmpty()) s = "0";
            try {
                x0[i] = Double.parseDouble(s.trim());
            } catch (NumberFormatException e) {
                x0[i] = 0.0;
            }
        }
        return x0;
    }

    @FXML
    public void limparTudo(ActionEvent event) {
        taOutput.clear();
        if (matrixFields == null) return;
        String metodo = choiceMetodo.getSelectionModel().getSelectedItem();
        boolean isIterativo = metodo.contains("Jacobi") || metodo.contains("Seidel");

        for (int i = 0; i < matrixFields.length; i++) {
            for (int j = 0; j < matrixFields.length; j++) matrixFields[i][j].clear();
            vectorFields[i].clear();
            if (isIterativo) {
                x0Fields[i].clear();
            }
        }
    }

    @FXML
    public void executarMetodo(ActionEvent event) {
        try {
            double[][] A = lerMatrizA();
            double[] b = lerVetorB();
            double[] x0 = lerX0();
            String metodo = choiceMetodo.getSelectionModel().getSelectedItem();

            boolean isIterativo = metodo.contains("Jacobi") || metodo.contains("Seidel");

            double tol = isIterativo ? Double.parseDouble(tfTolerance.getText().trim()) : 0.0;
            int maxIter = isIterativo ? Integer.parseInt(tfMaxIter.getText().trim()) : 0;

            taOutput.clear();
            taOutput.appendText("Executando: " + metodo + "\n");

            ResultadoSolucao result = null;
            long t0 = System.nanoTime();

            switch (metodo) {
                case "Eliminação de Gauss (sem pivoteamento)":
                    result = GaussEliminacaoSimples.resolver(A, b);
                    break;
                case "Gauss com pivoteamento parcial":
                    result = GaussPivoteamentoParcial.resolver(A, b);
                    break;
                case "Gauss com pivoteamento completo":
                    result = GaussPivoteamentoCompleto.resolver(A, b);
                    break;
                case "Fatoração LU (Doolittle)":
                    result = FatoracaoLU.resolverComLU(A, b);
                    break;
                case "Fatoração de Cholesky":
                    result = FatoracaoCholesky.resolverComCholesky(A, b);
                    break;
                case "Gauss-Jacobi (iterativo)":
                    result = Jacobi.resolver(A, b, tol, maxIter, cbShowSteps.isSelected(), x0);
                    break;
                case "Gauss-Seidel (iterativo)":
                    result = GaussSeidel.resolver(A, b, tol, maxIter, cbShowSteps.isSelected(), x0);
                    break;
                default:
                    taOutput.appendText("Método não implementado.\n");
                    return;
            }

            long t1 = System.nanoTime();
            double elapsedMs = (t1 - t0) / 1e6;

            if (result == null) {
                taOutput.appendText("Resultado nulo.\n");
                return;
            }
            taOutput.appendText("Mensagem: " + result.mensagem + "\n");
            if (result.solucao != null) {
                taOutput.appendText("Solução:\n");
                for (int i = 0; i < result.solucao.length; i++)
                    taOutput.appendText(String.format(Locale.US, "x[%d] = %.12f\n", i + 1, result.solucao[i]));
            }
            if (result.iteracoes > 0) taOutput.appendText("Iterações: " + result.iteracoes + "\n");
            taOutput.appendText(String.format(Locale.US, "Tempo (ms): %.4f\n", elapsedMs));
            if (cbShowSteps.isSelected() && result.passos != null && !result.passos.isEmpty()) {
                taOutput.appendText("\nPASSO A PASSO:\n");
                taOutput.appendText(result.passos);
            }
        } catch (NumberFormatException ex) {
            taOutput.appendText("Erro na leitura de números: " + ex.getMessage() + "\n");
        } catch (Exception ex) {
            taOutput.appendText("Erro: " + ex.getMessage() + "\n");
            ex.printStackTrace();
        }
    }
}