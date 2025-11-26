package calcNumericoGUI.metodos;

import java.util.function.DoubleUnaryOperator;

public class Secante {
    public static String executar(double x0, double x1, double tolerancia, int maximoIteracoes, DoubleUnaryOperator f) {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("Método da Secante\n");
        relatorio.append(String.format("%-5s %-14s %-14s %-14s %-14s%n", "Iter", "x_prev", "x_curr", "x_new", "f(x_new)"));
        relatorio.append("------------------------------------------------------------\n");

        double xAnterior = x0;
        double xAtual = x1;
        double fAnterior = f.applyAsDouble(xAnterior);
        double fAtual = f.applyAsDouble(xAtual);
        double ultimoX = xAtual;
        String mensagemCriterio = "";

        for (int k = 1; k <= maximoIteracoes; k++) {
            double denominador = (fAtual - fAnterior);
            if (Math.abs(denominador) < 1e-15) {
                mensagemCriterio = "Divisão por zero - f(xAtual) ≈ f(xAnterior)";
                break;
            }

            double novoX = xAtual - fAtual * (xAtual - xAnterior) / denominador;
            double fNovoX = f.applyAsDouble(novoX);

            relatorio.append(String.format("%-5d %-14.8f %-14.8f %-14.8f %-14.8f%n",
                    k, xAnterior, xAtual, novoX, fNovoX));

            ultimoX = novoX;

            if (Math.abs(fNovoX) < tolerancia) {
                mensagemCriterio = "|f(x)| = " + String.format("%.8f", Math.abs(fNovoX)) + " < " + tolerancia;
                break;
            }
            if (Math.abs(novoX - xAtual) < tolerancia) {
                mensagemCriterio = "|novoX - xAtual| = " + String.format("%.8f", Math.abs(novoX - xAtual)) + " < " + tolerancia;
                break;
            }

            xAnterior = xAtual;
            fAnterior = fAtual;
            xAtual = novoX;
            fAtual = fNovoX;

            if (k == maximoIteracoes) {
                mensagemCriterio = "Número máximo de iterações atingido";
            }
        }

        relatorio.append("------------------------------------------------------------\n");
        relatorio.append("Critério de parada satisfeito.\n");
        relatorio.append("Critério: " + mensagemCriterio + "\n");
        relatorio.append("Última aproximação: x = " + String.format("%.8f", ultimoX) + "\n");
        relatorio.append("f(x) = " + String.format("%.8f", f.applyAsDouble(ultimoX)) + "\n\n");

        return relatorio.toString();
    }
}