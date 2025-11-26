package calcNumericoGUI.controllers;

import calcNumericoGUI.metodos.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * Controller da tela do Capítulo 2.
 * Responsável por ler o texto de entrada (formato parecido com o txt),
 * montar os parâmetros e executar todos os métodos na ordem solicitada.
 */
public class Capitulo2Controller {

    @FXML
    private TextArea entradaTextArea;

    @FXML
    private TextArea saidaTextArea;

    @FXML
    public void voltar(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/calcNumericoGUI/views/tela_principal.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 800, 600));
    }

    @FXML
    public void limparEntrada() {
        entradaTextArea.clear();
    }

    @FXML
    public void limparSaida() {
        saidaTextArea.clear();
    }

    @FXML
    public void executarMetodos() {
        // Limpa saída anterior
        saidaTextArea.clear();

        String entrada = entradaTextArea.getText();
        if (entrada == null || entrada.trim().isEmpty()) {
            saidaTextArea.setText("Por favor, insira os parâmetros no campo de entrada no formato solicitado.");
            return;
        }

        try {
            // Verifica e extrai parâmetros usando VerificadorEntrada (mesma ideia do VerificaFuncao)
            VerificadorEntrada ve = new VerificadorEntrada(entrada);
            Map<String, String> parametros = ve.getParametros();

            // Recupera parâmetros com fallback
            double a = Double.parseDouble(parametros.getOrDefault("a", "0"));
            double b = Double.parseDouble(parametros.getOrDefault("b", "0"));
            double delta = Double.parseDouble(parametros.getOrDefault("delta", "1e-6"));
            int n = Integer.parseInt(parametros.getOrDefault("n", "50"));
            double x0 = parametros.containsKey("x0") ? Double.parseDouble(parametros.get("x0")) : (a + b) / 2.0;

            // Função f a partir da expressão
            DoubleUnaryOperator f = x -> ve.avaliar(x);

            // Derivada numérica para df
            DoubleUnaryOperator df = x -> {
                double h = 1e-6;
                return (f.applyAsDouble(x + h) - f.applyAsDouble(x - h)) / (2 * h);
            };

            StringBuilder relatorio = new StringBuilder();

            // === BISSEÇÃO ===
            relatorio.append("=== BISSEÇÃO ===\n");
            double fa = f.applyAsDouble(a);
            double fb = f.applyAsDouble(b);
            if (fa * fb < 0) {
                relatorio.append(Bisseccao.executar(a, b, delta, n, f));
            } else {
                relatorio.append("Não executado: f(a)*f(b) >= 0\n\n");
            }

            // === REGULA FALSI ===
            relatorio.append("\n\n=== REGULA FALSI ===\n");
            if (fa * fb < 0) {
                relatorio.append(RegulaFalsi.executar(a, b, delta, n, f));
            } else {
                relatorio.append("Não executado: f(a)*f(b) >= 0\n\n");
            }

            // === SECANTE ===
            relatorio.append("\n\n=== SECANTE ===\n");
            double x0_sec = a;
            double x1_sec = b;
            if (Math.abs(f.applyAsDouble(x1_sec) - f.applyAsDouble(x0_sec)) > 1e-15) {
                relatorio.append(Secante.executar(x0_sec, x1_sec, delta, n, f));
            } else {
                relatorio.append("Não executado: divisão por zero na secante\n\n");
            }

            // === NEWTON ===
            relatorio.append("\n\n=== NEWTON ===\n");
            double dfx0 = df.applyAsDouble(x0);
            if (Math.abs(dfx0) > 1e-12) {
                relatorio.append(Newton.executar(x0, delta, n, f, df));
            } else {
                relatorio.append("Não executado: derivada ≈ 0 em x0\n\n");
            }

            // === MIL (PONTO FIXO) ===
            relatorio.append("\n\n=== MIL (PONTO FIXO) ===\n");
            DoubleUnaryOperator g;
            double x0_mil = x0;
            if (parametros.containsKey("fi")) {
                String expressaoFi = parametros.get("fi").replaceAll("e\\^", "exp");
                Expression expr = new ExpressionBuilder(expressaoFi).variable("x").build();
                relatorio.append("Usando fi(x) fornecida: ").append(parametros.get("fi")).append("\n");
                g = x -> {
                    expr.setVariable("x", x);
                    return expr.evaluate();
                };
            } else {
                double derivadaNoPonto = df.applyAsDouble(x0_mil);
                double lambda = 1.0 / (1.0 + Math.abs(derivadaNoPonto));
                relatorio.append("fi(x) não fornecida. Usando g(x) = x - lambda*f(x), lambda = ")
                        .append(lambda).append("\n");
                g = x -> x - lambda * f.applyAsDouble(x);
            }
            relatorio.append(MIL.executar(x0_mil, delta, n, g));

            // Exibe o relatório final na TextArea de saída
            saidaTextArea.setText(relatorio.toString());

        } catch (Exception e) {
            // Exibe erro amigável
            saidaTextArea.setText("Erro ao executar métodos: " + e.getMessage() + "\n\nVerifique o formato da entrada.");
            e.printStackTrace();
        }
    }
}
