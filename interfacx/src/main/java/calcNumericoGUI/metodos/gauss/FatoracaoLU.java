package calcNumericoGUI.metodos.gauss;
import calcNumericoGUI.metodos.UtilitariosMatriz;
import calcNumericoGUI.metodos.ResultadoSolucao;
public class FatoracaoLU {
    public static ResultadoSolucao resolverComLU(double[][] matrizA, double[] vetorB) {
        ResultadoSolucao resultado = new ResultadoSolucao();
        StringBuilder passos = new StringBuilder();
        try {
            if (!UtilitariosMatriz.verificarSistemaValido(matrizA, vetorB)) {
                return ResultadoSolucao.criarErro("Sistema inválido: matriz não é quadrada ou vetor b tem tamanho incorreto");
            }
            int n = matrizA.length;
            passos.append("=== FATORAÇÃO LU (DOOLITTLE) ===\n");
            passos.append("Dimensão do sistema: ").append(n).append("x").append(n).append("\n\n");
            if (UtilitariosMatriz.contemNaNouInfinito(matrizA) || UtilitariosMatriz.contemNaNouInfinito(vetorB)) {
                return ResultadoSolucao.criarErro("Matriz ou vetor contém valores inválidos (NaN ou infinito)");
            }
            double[][] A = UtilitariosMatriz.copiarMatriz(matrizA);
            double[] b = UtilitariosMatriz.copiarVetor(vetorB);
            double[][] L = new double[n][n];
            double[][] U = new double[n][n];
            passos.append("Iniciando fatoração LU...\n");
            passos.append("Matriz A inicial:\n").append(UtilitariosMatriz.matrizParaString(A)).append("\n");
            for (int i = 0; i < n; i++) {
                L[i][i] = 1.0;
            }
            for (int j = 0; j < n; j++) {
                passos.append("--- Coluna ").append(j).append(" ---\n");
                for (int i = 0; i <= j; i++) {
                    double soma = 0.0;
                    for (int k = 0; k < i; k++) {
                        soma += L[i][k] * U[k][j];
                    }
                    U[i][j] = A[i][j] - soma;
                    passos.append(String.format("U[%d][%d] = %.6f - %.6f = %.6f\n",
                            i, j, A[i][j], soma, U[i][j]));
                    if (Double.isNaN(U[i][j]) || Double.isInfinite(U[i][j])) {
                        passos.append(" Valor inválido em U[").append(i).append("][").append(j).append("]\n");
                        return ResultadoSolucao.criarErro("Valor inválido gerado durante fatoração LU");
                    }
                }
                for (int i = j + 1; i < n; i++) {
                    double soma = 0.0;
                    for (int k = 0; k < j; k++) {
                        soma += L[i][k] * U[k][j];
                    }
                    if (Math.abs(U[j][j]) < 1e-12) {
                        passos.append(" Divisão por zero em L[").append(i).append("][").append(j).append("]\n");
                        passos.append("U[").append(j).append("][").append(j).append("] = ").append(U[j][j]).append("\n");
                        passos.append("Matriz singular ou precisa de pivoteamento\n");
                        return ResultadoSolucao.criarErro("Divisão por zero durante fatoração LU - matriz singular");
                    }
                    L[i][j] = (A[i][j] - soma) / U[j][j];
                    passos.append(String.format("L[%d][%d] = (%.6f - %.6f) / %.6f = %.6f\n",
                            i, j, A[i][j], soma, U[j][j], L[i][j]));
                    if (Double.isNaN(L[i][j]) || Double.isInfinite(L[i][j])) {
                        passos.append(" Valor inválido em L[").append(i).append("][").append(j).append("]\n");
                        return ResultadoSolucao.criarErro("Valor inválido gerado durante fatoração LU");
                    }
                }
            }
            passos.append("\n Fatoração LU concluída\n");
            passos.append("Matriz L:\n").append(UtilitariosMatriz.matrizParaString(L));
            passos.append("Matriz U:\n").append(UtilitariosMatriz.matrizParaString(U));
            passos.append("\nValidando fatoração L*U...\n");
            if (!validarFatoracaoLU(matrizA, L, U, passos)) {
                passos.append("  Fatoração LU pode conter erros numéricos\n");
            }
            passos.append("\nResolvendo L * y = b...\n");
            double[] y = new double[n];
            for (int i = 0; i < n; i++) {
                double soma = b[i];
                for (int j = 0; j < i; j++) {
                    soma -= L[i][j] * y[j];
                }
                y[i] = soma;
                passos.append(String.format("y[%d] = %.6f\n", i, y[i]));
                if (Double.isNaN(y[i]) || Double.isInfinite(y[i])) {
                    passos.append(" Valor inválido em y[").append(i).append("]\n");
                    return ResultadoSolucao.criarErro("Valor inválido durante substituição direta");
                }
            }
            passos.append("\nVetor y:\n").append(UtilitariosMatriz.vetorParaString(y));
            passos.append("\nResolvendo U * x = y...\n");
            double[] x = new double[n];
            for (int i = n - 1; i >= 0; i--) {
                double soma = y[i];
                for (int j = i + 1; j < n; j++) {
                    soma -= U[i][j] * x[j];
                }
                if (Math.abs(U[i][i]) < 1e-12) {
                    passos.append(" Divisão por zero em x[").append(i).append("]\n");
                    passos.append("U[").append(i).append("][").append(i).append("] = ").append(U[i][i]).append("\n");
                    return ResultadoSolucao.criarErro("Divisão por zero durante substituição reversa - matriz singular");
                }
                x[i] = soma / U[i][i];
                passos.append(String.format("x[%d] = %.6f / %.6f = %.6f\n", i, soma, U[i][i], x[i]));
                if (Double.isNaN(x[i]) || Double.isInfinite(x[i])) {
                    passos.append(" Valor inválido em x[").append(i).append("]\n");
                    return ResultadoSolucao.criarErro("Valor inválido durante substituição reversa");
                }
            }
            if (UtilitariosMatriz.contemNaNouInfinito(x)) {
                passos.append(" Solução contém valores inválidos\n");
                return ResultadoSolucao.criarErro("Solução contém valores NaN ou infinito");
            }
            passos.append("\n Sistema resolvido com sucesso!\n");
            passos.append("Solução final:\n").append(UtilitariosMatriz.vetorParaString(x));
            double[] residuo = UtilitariosMatriz.subtrairVetores(
                    multiplicarMatrizVetor(matrizA, x), vetorB);
            double normaResiduo = UtilitariosMatriz.normaInfinita(residuo);
            passos.append(String.format("Norma do resíduo: %.2e\n", normaResiduo));
            resultado.solucao = x;
            resultado.mensagem = "Fatoração LU concluída com sucesso";
            resultado.passos = passos.toString();
            return resultado;
        } catch (Exception e) {
            resultado.passos = passos.toString();
            return ResultadoSolucao.criarErro("Erro durante fatoração LU: " + e.getMessage());
        }
    }
    private static boolean validarFatoracaoLU(double[][] A, double[][] L, double[][] U, StringBuilder passos) {
        int n = A.length;
        double tolerancia = 1e-8;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double soma = 0.0;
                for (int k = 0; k < n; k++) {
                    soma += L[i][k] * U[k][j];
                }
                double erro = Math.abs(A[i][j] - soma);
                if (erro > tolerancia) {
                    passos.append(String.format("Erro em A[%d][%d]: esperado=%.6f, obtido=%.6f, erro=%.2e\n",
                            i, j, A[i][j], soma, erro));
                    return false;
                }
            }
        }
        passos.append(" Fatoração L*U válida (erro < ").append(tolerancia).append(")\n");
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