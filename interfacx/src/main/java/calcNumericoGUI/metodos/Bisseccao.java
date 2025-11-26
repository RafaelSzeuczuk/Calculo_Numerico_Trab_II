package calcNumericoGUI.metodos;

import java.util.function.DoubleUnaryOperator;

public class Bisseccao {
    public static String executar(double a, double b, double delta, int n, DoubleUnaryOperator f) {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("Método da Bisseção\n");
        relatorio.append("Iter| a | b | x | f(x) | |b-a|\n");
        relatorio.append("-----------------------------------------------------------\n");
        // SALVAR RESULTADOS
        double fa = f.applyAsDouble(a);
        double fb = f.applyAsDouble(b);
        if (fa * fb > 0) {
            relatorio.append("Erro: f(a) e f(b) têm o mesmo sinal.\n\n");
            return relatorio.toString();
        }
        for (int k = 1; k <= n; k++) {
            double x = (a + b) / 2;
            double fx = f.applyAsDouble(x);
            if (fa * fx < 0) {
                b = x;
                fb = fx;
            } else {
                a = x;
                fa = fx;
            }
            double tamanhoIntervalo = Math.abs(b - a);
            relatorio.append(String.format("%3d | %.6f | %.6f | %.6f | %.6f | %.6f%n", k, a, b, x, fx, tamanhoIntervalo));
            if (tamanhoIntervalo < delta) {
                relatorio.append("-----------------------------------------------------------\n");
                relatorio.append("Parou no critério de parada na iteração " + k + "\n");
                relatorio.append("Intervalo final: [" + String.format("%.6f", a) + ", " + String.format("%.6f", b) + "]\n");
                relatorio.append("|b-a| = " + String.format("%.6f", tamanhoIntervalo) + " < " + delta + "\n");
                relatorio.append("f(a) = " + String.format("%.6f", fa) + ", f(b) = " + String.format("%.6f", fb) + "\n\n");
                break;
            }
        }
        return relatorio.toString();
    }
}
