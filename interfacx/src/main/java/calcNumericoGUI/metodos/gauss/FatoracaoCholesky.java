package calcNumericoGUI.metodos.gauss;
import calcNumericoGUI.metodos.UtilitariosMatriz;
import calcNumericoGUI.metodos.ResultadoSolucao;
public class FatoracaoCholesky {
    public static ResultadoSolucao resolverComCholesky(double[][] matrizA, double[] vetorB) {
        ResultadoSolucao resultado = new ResultadoSolucao();
        StringBuilder passos = new StringBuilder();
        try {
            if (!UtilitariosMatriz.verificarSistemaValido(matrizA, vetorB)) {
                return ResultadoSolucao.criarErro("Sistema inválido: matriz não é quadrada ou vetor b tem tamanho incorreto");
            }
            int n = matrizA.length;
            passos.append("=== FATORAÇÃO DE CHOLESKY ===\n");
            passos.append("Dimensão do sistema: ").append(n).append("x").append(n).append("\n\n");
            if (UtilitariosMatriz.contemNaNouInfinito(matrizA) || UtilitariosMatriz.contemNaNouInfinito(vetorB)) {
                return ResultadoSolucao.criarErro("Matriz ou vetor contém valores inválidos (NaN ou infinito)");
            }
            passos.append("Verificando simetria da matriz...\n");
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double diferenca = Math.abs(matrizA[i][j] - matrizA[j][i]);
                    if (diferenca > 1e-12) {
                        passos.append(" Matriz não é simétrica: A[").append(i).append("][").append(j).append("] = ").append(matrizA[i][j])
                                .append("  A[").append(j).append("][").append(i).append("] = ").append(matrizA[j][i]).append("\n");
                        passos.append("Diferença: ").append(diferenca).append("\n");
                        resultado.passos = passos.toString();
                        return ResultadoSolucao.criarErro("Matriz não é simétrica - Cholesky não aplicável");
                    }
                }
            }
            passos.append(" Matriz é simétrica\n\n");
            passos.append("Verificando se a matriz é definida positiva...\n");
            if (!verificarDefinidaPositiva(matrizA)) {
                passos.append(" Matriz não é definida positiva\n");
                passos.append("Cholesky requer que todos os autovalores sejam positivos\n");
                resultado.passos = passos.toString();
                return ResultadoSolucao.criarErro("Matriz não é definida positiva - Cholesky não aplicável");
            }
            passos.append(" Matriz é definida positiva\n\n");
            double[][] A = UtilitariosMatriz.copiarMatriz(matrizA);
            double[] b = UtilitariosMatriz.copiarVetor(vetorB);
            double[][] L = new double[n][n]; 
            passos.append("Iniciando fatoração L * L...\n");
            for (int i = 0; i < n; i++) {
                passos.append("--- Linha ").append(i).append(" ---\n");
                for (int j = 0; j <= i; j++) {
                    double soma = 0.0;
                    for (int k = 0; k < j; k++) {
                        soma += L[i][k] * L[j][k];
                    }
                    if (i == j) {
                        double valor = A[i][i] - soma;
                        passos.append(String.format("L[%d][%d] = (%.6f - %.6f) = (%.6f)\n", i, i, A[i][i], soma, valor));
                        if (valor <= 1e-12) {
                            passos.append(" Elemento diagonal L[").append(i).append("][").append(i).append("] = ").append(valor)
                                    .append("  0\n");
                            passos.append("Matriz não é definida positiva\n");
                            resultado.passos = passos.toString();
                            return ResultadoSolucao.criarErro("Matriz não é definida positiva - elemento diagonal  0");
                        }
                        L[i][i] = Math.sqrt(valor);
                        passos.append(String.format("L[%d][%d] = %.6f\n", i, i, L[i][i]));
                    } else {
                        if (Math.abs(L[j][j]) < 1e-12) {
                            passos.append(" Divisão por zero em L[").append(i).append("][").append(j).append("]\n");
                            resultado.passos = passos.toString();
                            return ResultadoSolucao.criarErro("Divisão por zero durante fatoração - matriz singular");
                        }
                        L[i][j] = (A[i][j] - soma) / L[j][j];
                        passos.append(String.format("L[%d][%d] = (%.6f - %.6f) / %.6f = %.6f\n",
                                i, j, A[i][j], soma, L[j][j], L[i][j]));
                    }
                    if (Double.isNaN(L[i][j]) || Double.isInfinite(L[i][j])) {
                        passos.append(" Valor inválido gerado em L[").append(i).append("][").append(j).append("]\n");
                        passos.append("Isso indica instabilidade numérica ou matriz mal condicionada\n");
                        resultado.passos = passos.toString();
                        return ResultadoSolucao.criarErro("Valor inválido durante fatoração - matriz pode ser mal condicionada");
                    }
                }
            }
            passos.append("\n Fatoração concluída\n");
            passos.append("Matriz L:\n").append(UtilitariosMatriz.matrizParaString(L)).append("\n");
            passos.append("Resolvendo L * y = b...\n");
            double[] y = new double[n];
            for (int i = 0; i < n; i++) {
                double soma = b[i];
                for (int j = 0; j < i; j++) {
                    soma -= L[i][j] * y[j];
                }
                if (Math.abs(L[i][i]) < 1e-12) {
                    passos.append(" Divisão por zero em y[").append(i).append("]\n");
                    resultado.passos = passos.toString();
                    return ResultadoSolucao.criarErro("Sistema singular - divisão por zero na substituição direta");
                }
                y[i] = soma / L[i][i];
                passos.append(String.format("y[%d] = (%.6f) / %.6f = %.6f\n", i, soma, L[i][i], y[i]));
                if (Double.isNaN(y[i]) || Double.isInfinite(y[i])) {
                    passos.append(" Valor inválido em y[").append(i).append("]\n");
                    resultado.passos = passos.toString();
                    return ResultadoSolucao.criarErro("Valor inválido durante substituição direta");
                }
            }
            passos.append("\nVetor y:\n").append(UtilitariosMatriz.vetorParaString(y)).append("\n");
            passos.append("Resolvendo L * x = y...\n");
            double[] x = new double[n];
            for (int i = n - 1; i >= 0; i--) {
                double soma = y[i];
                for (int j = i + 1; j < n; j++) {
                    soma -= L[j][i] * x[j]; 
                }
                if (Math.abs(L[i][i]) < 1e-12) {
                    passos.append(" Divisão por zero em x[").append(i).append("]\n");
                    resultado.passos = passos.toString();
                    return ResultadoSolucao.criarErro("Sistema singular - divisão por zero na substituição reversa");
                }
                x[i] = soma / L[i][i];
                passos.append(String.format("x[%d] = (%.6f) / %.6f = %.6f\n", i, soma, L[i][i], x[i]));
                if (Double.isNaN(x[i]) || Double.isInfinite(x[i])) {
                    passos.append(" Valor inválido em x[").append(i).append("]\n");
                    passos.append("Isso pode ocorrer devido a:\n");
                    passos.append("1. Matriz mal condicionada\n");
                    passos.append("2. Instabilidade numérica\n");
                    passos.append("3. Matriz não é definida positiva\n");
                    resultado.passos = passos.toString();
                    return ResultadoSolucao.criarErro("Solução contém valores NaN ou infinito - matriz pode não ser definida positiva");
                }
            }
            passos.append("\n Sistema resolvido com sucesso!\n");
            passos.append("Solução final:\n").append(UtilitariosMatriz.vetorParaString(x));
            resultado.solucao = x;
            resultado.mensagem = "Fatoração de Cholesky concluída com sucesso";
            resultado.passos = passos.toString();
            return resultado;
        } catch (Exception e) {
            resultado.passos = passos.toString();
            return ResultadoSolucao.criarErro("Erro durante fatoração de Cholesky: " + e.getMessage());
        }
    }
    private static boolean verificarDefinidaPositiva(double[][] A) {
        int n = A.length;
        for (int i = 0; i < n; i++) {
            if (A[i][i] <= 0) {
                return false;
            }
        }
        for (int k = 1; k <= n; k++) {
            double det = calcularDeterminanteMenorPrincipal(A, k);
            if (det <= 0) {
                return false;
            }
        }
        return true;
    }
    private static double calcularDeterminanteMenorPrincipal(double[][] A, int k) {
        if (k == 1) {
            return A[0][0];
        }
        switch (k) {
            case 2:
                return A[0][0] * A[1][1] - A[0][1] * A[1][0];
            case 3:
                return A[0][0] * (A[1][1] * A[2][2] - A[1][2] * A[2][1])
                        - A[0][1] * (A[1][0] * A[2][2] - A[1][2] * A[2][0])
                        + A[0][2] * (A[1][0] * A[2][1] - A[1][1] * A[2][0]);
            default:
                return 1.0; 
        }
    }
}