package calcNumericoGUI.metodos;

import java.util.function.DoubleUnaryOperator;

public class Newton {
    public static String executar(double x0, double tolerancia, int maximoIteracoes, DoubleUnaryOperator f, DoubleUnaryOperator df) {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("Método de Newton-Raphson\n");
        relatorio.append(String.format("%-5s %-14s %-14s %-14s%n", "Iter", "x", "f(x)", "f'(x)"));
        relatorio.append("------------------------------------------------\n");
        double x = x0;
        double fx = f.applyAsDouble(x);
        double dfx = df.applyAsDouble(x);
        String mensagemCriterio = "";
        relatorio.append(String.format("%-5d %-14.8f %-14.8f %-14.8f%n", 0, x, fx, dfx));
        for (int k = 1; k <= maximoIteracoes; k++) {
            if (Math.abs(dfx) < 1e-15) {
                mensagemCriterio = "Derivada ≈ 0 - método não pode continuar";
                break;
            }
            double novoX = x - fx / dfx;
            double fNovoX = f.applyAsDouble(novoX);
            double dfNovoX = df.applyAsDouble(novoX);
            relatorio.append(String.format("%-5d %-14.8f %-14.8f %-14.8f%n", k, novoX, fNovoX, dfNovoX));
            if (Math.abs(fNovoX) < tolerancia) {
                mensagemCriterio = "|f(x)| = " + String.format("%.8f", Math.abs(fNovoX)) + " < " + tolerancia;
                break;
            }
            if (Math.abs(novoX - x) < tolerancia) {
                mensagemCriterio = "|x_new - x| = " + String.format("%.8f", Math.abs(novoX - x)) + " < " + tolerancia;
                break;
            }
            x = novoX;
            fx = fNovoX;
            dfx = dfNovoX;
            if (k == maximoIteracoes) mensagemCriterio = "Número máximo de iterações atingido";
        }
        relatorio.append("------------------------------------------------\n");
        relatorio.append("Critério de parada satisfeito.\n");
        relatorio.append("Critério: " + mensagemCriterio + "\n");
        relatorio.append("Raiz aproximada: x = " + String.format("%.8f", x) + "\n");
        relatorio.append("f(x) = " + String.format("%.8f", fx) + ", f'(x) = " + String.format("%.8f", dfx) + "\n\n");
        return relatorio.toString();
    }
}
