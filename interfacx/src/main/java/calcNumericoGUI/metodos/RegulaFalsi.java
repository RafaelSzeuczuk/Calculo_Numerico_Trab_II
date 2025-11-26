package calcNumericoGUI.metodos;

import java.util.function.DoubleUnaryOperator;

public class RegulaFalsi {
    public static String executar(double a, double b, double delta, int n, DoubleUnaryOperator f) {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("Método da Regula Falsi\n");
        relatorio.append("Iter | a | b | x | f(x)\n");
        relatorio.append("---------------------------------------------------\n");
        double fa = f.applyAsDouble(a);
        double fb = f.applyAsDouble(b);
        String mensagemCriterio = "";
        for (int k = 1; k <= n; k++) {
            double x = (a * fb - b * fa) / (fb - fa);
            double fx = f.applyAsDouble(x);
            relatorio.append(String.format("%3d | %.6f | %.6f | %.6f | %.6f%n", k, a, b, x, fx));
            if (Math.abs(fx) < delta) {
                mensagemCriterio = "|f(x)| = " + String.format("%.6f", Math.abs(fx)) + " < " + delta;
                break;
            }
            if (Math.abs(b - a) < delta) {
                mensagemCriterio = "|b-a| = " + String.format("%.6f", Math.abs(b - a)) + " < " + delta;
                break;
            }
            if (fa * fx < 0) {
                b = x;
                fb = fx;
            } else {
                a = x;
                fa = fx;
            }
            if (k == n) mensagemCriterio = "Número máximo de iterações atingido";
        }
        relatorio.append("---------------------------------------------------\n");
        relatorio.append("Parou no critério de parada na iteração " + (Math.min(n, relatorio.toString().split("\n").length - 5)) + "\n");
        relatorio.append("Intervalo final: [" + String.format("%.6f", a) + ", " + String.format("%.6f", b) + "]\n");
        relatorio.append("Critério: " + mensagemCriterio + "\n");
        relatorio.append("f(a) = " + String.format("%.6f", fa) + ", f(b) = " + String.format("%.6f", fb) + "\n\n");
        return relatorio.toString();
    }
}