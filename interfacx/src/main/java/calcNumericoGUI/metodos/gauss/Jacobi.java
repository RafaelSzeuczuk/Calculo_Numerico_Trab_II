package calcNumericoGUI.metodos.gauss;
import calcNumericoGUI.metodos.UtilitariosMatriz;
import calcNumericoGUI.metodos.ResultadoSolucao;
import java.util.Locale;
public class Jacobi {
    public static ResultadoSolucao resolver(double[][] matrizA, double[] vetorB, double tolerancia,
                                            int maximoIteracoes, boolean mostrarPassos, double[] chuteInicial) {
        ResultadoSolucao resultado = new ResultadoSolucao();
        StringBuilder passos = new StringBuilder();
        try {
            if (!UtilitariosMatriz.verificarSistemaValido(matrizA, vetorB)) {
                return ResultadoSolucao.criarErro("Sistema inválido: matriz não é quadrada ou vetor b tem tamanho incorreto");
            }
            int n = matrizA.length;
            passos.append("=== MÉTODO DE JACOBI ===\n");
            passos.append("Dimensão do sistema: ").append(n).append("x").append(n).append("\n");
            passos.append("Tolerância: ").append(tolerancia).append("\n");
            passos.append("Máximo de iterações: ").append(maximoIteracoes).append("\n\n");
            if (UtilitariosMatriz.contemNaNouInfinito(matrizA) || UtilitariosMatriz.contemNaNouInfinito(vetorB)) {
                return ResultadoSolucao.criarErro("Matriz ou vetor contém valores inválidos (NaN ou infinito)");
            }
            boolean diagonalDominante = verificarDiagonalDominante(matrizA);
            if (!diagonalDominante) {
                passos.append("  Matriz não é diagonalmente dominante - convergência não garantida\n");
            } else {
                passos.append(" Matriz é diagonalmente dominante - convergência garantida\n");
            }
            double[][] A = UtilitariosMatriz.copiarMatriz(matrizA);
            double[] b = UtilitariosMatriz.copiarVetor(vetorB);
            double[] x;
            double[] xNovo = new double[n];
            if (chuteInicial != null && chuteInicial.length == n) {
                x = UtilitariosMatriz.copiarVetor(chuteInicial);
                passos.append("Usando chute inicial fornecido\n");
            } else {
                x = new double[n];
                passos.append("Usando chute inicial zero\n");
            }
            passos.append("Chute inicial: ").append(UtilitariosMatriz.vetorParaString(x)).append("\n");
            for (int iteracao = 1; iteracao <= maximoIteracoes; iteracao++) {
                boolean erroDivisaoZero = false;
                for (int i = 0; i < n; i++) {
                    double soma = b[i];
                    for (int j = 0; j < n; j++) {
                        if (j != i) {
                            soma -= A[i][j] * x[j];
                        }
                    }
                    if (Math.abs(A[i][i]) < 1e-15) {
                        passos.append(" Elemento diagonal nulo na linha ").append(i)
                                .append(" - método não pode continuar\n");
                        return ResultadoSolucao.criarErro("Elemento diagonal A[" + i + "][" + i + "] é zero");
                    }
                    xNovo[i] = soma / A[i][i];
                    if (Double.isNaN(xNovo[i]) || Double.isInfinite(xNovo[i])) {
                        passos.append(" Valor inválido gerado na iteração ").append(iteracao)
                                .append(", linha ").append(i).append("\n");
                        return ResultadoSolucao.criarErro("Divergência detectada - valores NaN ou infinito");
                    }
                }
                double[] diferenca = UtilitariosMatriz.subtrairVetores(xNovo, x);
                double normaDiferenca = UtilitariosMatriz.normaInfinita(diferenca);
                if (mostrarPassos) {
                    passos.append(String.format(Locale.US, "Iteração %d:\n", iteracao));
                    passos.append("Solução atual: ").append(UtilitariosMatriz.vetorParaString(xNovo));
                    passos.append(String.format(Locale.US, "Diferença: %.10f\n", normaDiferenca));
                    passos.append(String.format(Locale.US, "Critério: %.10f < %.10f ? %s\n\n",
                            normaDiferenca, tolerancia, normaDiferenca < tolerancia ? "SIM" : "NÃO"));
                }
                System.arraycopy(xNovo, 0, x, 0, n);
                if (normaDiferenca < tolerancia) {
                    passos.append(" CONVERGÊNCIA ALCANÇADA!\n");
                    passos.append(String.format("Iterações totais: %d\n", iteracao));
                    passos.append(String.format("Erro final: %.2e\n", normaDiferenca));
                    double[] residuo = UtilitariosMatriz.subtrairVetores(
                            multiplicarMatrizVetor(matrizA, x), vetorB);
                    double normaResiduo = UtilitariosMatriz.normaInfinita(residuo);
                    passos.append(String.format("Norma do resíduo: %.2e\n", normaResiduo));
                    resultado.solucao = x;
                    resultado.iteracoes = iteracao;
                    resultado.mensagem = "Jacobi convergiu com sucesso";
                    resultado.passos = passos.toString();
                    return resultado;
                }
                if (UtilitariosMatriz.normaInfinita(x) > 1e10) {
                    passos.append(" DIVERGÊNCIA DETECTADA - valores muito grandes\n");
                    return ResultadoSolucao.criarErro("Método divergiu - valores da solução muito grandes");
                }
                if (iteracao > 10 && normaDiferenca > 1e-3) {
                    passos.append("  Possível estagnação - progresso lento\n");
                }
            }
            passos.append("  MÁXIMO DE ITERAÇÕES ATINGIDO\n");
            passos.append("Solução aproximada:\n").append(UtilitariosMatriz.vetorParaString(x));
            double[] diferencaFinal = UtilitariosMatriz.subtrairVetores(x,
                    UtilitariosMatriz.copiarVetor(chuteInicial != null ? chuteInicial : new double[n]));
            double normaDiferencaFinal = UtilitariosMatriz.normaInfinita(diferencaFinal);
            passos.append(String.format("Diferença final: %.2e\n", normaDiferencaFinal));
            double[] residuo = UtilitariosMatriz.subtrairVetores(
                    multiplicarMatrizVetor(matrizA, x), vetorB);
            double normaResiduo = UtilitariosMatriz.normaInfinita(residuo);
            passos.append(String.format("Norma do resíduo: %.2e\n", normaResiduo));
            resultado.solucao = x;
            resultado.iteracoes = maximoIteracoes;
            resultado.mensagem = "Jacobi atingiu o máximo de iterações sem convergir";
            resultado.passos = passos.toString();
            return resultado;
        } catch (Exception e) {
            resultado.passos = passos.toString();
            return ResultadoSolucao.criarErro("Erro durante método de Jacobi: " + e.getMessage());
        }
    }
    private static boolean verificarDiagonalDominante(double[][] A) {
        int n = A.length;
        for (int i = 0; i < n; i++) {
            double soma = 0.0;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    soma += Math.abs(A[i][j]);
                }
            }
            if (Math.abs(A[i][i]) <= soma) {
                return false;
            }
        }
        return true;
    }
    private static double[] multiplicarMatrizVetor(double[][] A, double[] x) {
        int n = A.length;
        double[] resultado = new double[n];
        for (int i = 0; i < n; i++) {
            double soma = 0.0;
            for (int j = 0; j < n; j++) {
                soma += A[i][j] * x[j];
            }
            resultado[i] = soma;
        }
        return resultado;
    }
}