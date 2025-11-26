package calcNumericoGUI.metodos;

import java.util.function.DoubleUnaryOperator;

public class MIL {
    public static String executar(double x0, double delta, int n, DoubleUnaryOperator g) {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("Método Iterativo Linear (Ponto Fixo - MIL)\n");
        relatorio.append("x₀ = ").append(x0).append(", δ = ").append(delta).append(", n max = ").append(n).append("\n");
        double h = 1e-6;
        boolean converge = true;
        double maxDerivada = 0.0;
        for (int i = 0; i < 5; i++) {
            double xi = x0 + (i - 2) * 0.25;
            try {
                double deriv = (g.applyAsDouble(xi + h) - g.applyAsDouble(xi - h)) / (2 * h);
                if (Math.abs(deriv) > maxDerivada) maxDerivada = Math.abs(deriv);
                if (Math.abs(deriv) >= 1) converge = false;
            } catch (Exception e) {
                converge = false;
                break;
            }
        }
        if (!converge) {
            relatorio.append("NÃO EXECUTADO: |g'(x)| = ").append(String.format("%.6f", maxDerivada))
                    .append(" ≥ 1 → função de iteração instável\n");
            relatorio.append("A função φ(x) não satisfaz as condições de convergência do MIL\n\n");
            return relatorio.toString();
        }
        relatorio.append("Condição |g'(x)| < 1 satisfeita (max = ").append(String.format("%.6f", maxDerivada))
                .append(") → executando...\n");
        relatorio.append("Iter | xₖ | g(xₖ) | |xₖ₊₁ - xₖ|\n");
        relatorio.append("---------------------------------------------\n");
        double x = x0;
        String criterio = "Número máximo de iterações atingido";
        for (int k = 1; k <= n; k++) {
            double xAnterior = x;
            double gx;
            try {
                gx = g.applyAsDouble(xAnterior);
            } catch (Exception e) {
                relatorio.append("Erro ao calcular g(x) na iteração ").append(k).append("\n");
                criterio = "Erro na função";
                break;
            }
            double diff = Math.abs(gx - xAnterior);
            relatorio.append(String.format("%3d | %10.6f | %10.6f | %10.6f%n", k, xAnterior, gx, diff));
            if (diff < delta) {
                criterio = String.format("|xₖ₊₁ - xₖ| = %.6f < δ = %.6f", diff, delta);
                x = gx;
                break;
            }
            if (diff > 1e6) {
                criterio = "Divergência detectada: diferenças crescentes";
                break;
            }
            x = gx;
        }
        relatorio.append("---------------------------------------------\n");
        relatorio.append("Critério de parada: ").append(criterio).append("\n");
        relatorio.append("Aproximação do ponto fixo: x = ").append(String.format("%.6f", x)).append("\n");
        try {
            double gxFinal = g.applyAsDouble(x);
            relatorio.append("g(x) = ").append(String.format("%.6f", gxFinal)).append("\n");
            relatorio.append("|g(x) - x| = ").append(String.format("%.6f", Math.abs(gxFinal - x))).append("\n");
        } catch (Exception e) {
            relatorio.append("Erro ao calcular g(x) final\n");
        }
        return relatorio.toString();
    }
}