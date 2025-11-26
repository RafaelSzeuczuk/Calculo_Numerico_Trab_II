package calcNumericoGUI.metodos.gauss;
import calcNumericoGUI.metodos.UtilitariosMatriz;
import calcNumericoGUI.metodos.ResultadoSolucao;
public class GaussEliminacaoSimples {
    public static ResultadoSolucao resolver(double[][] matrizA, double[] vetorB) {
        ResultadoSolucao resultado = new ResultadoSolucao();
        StringBuilder passos = new StringBuilder();
        try {
            if (!UtilitariosMatriz.verificarSistemaValido(matrizA, vetorB)) {
                return ResultadoSolucao.criarErro("Sistema inválido: matriz não é quadrada ou vetor b tem tamanho incorreto");
            }
            int n = matrizA.length;
            passos.append("=== ELIMINAÇÃO DE GAUSS SEM PIVOTEAMENTO ===\n");
            passos.append("Dimensão do sistema: ").append(n).append("x").append(n).append("\n");
            passos.append("AVISO: Este método pode falhar se houver pivôs nulos\n\n");
            if (UtilitariosMatriz.contemNaNouInfinito(matrizA) || UtilitariosMatriz.contemNaNouInfinito(vetorB)) {
                return ResultadoSolucao.criarErro("Matriz ou vetor contém valores inválidos (NaN ou infinito)");
            }
            double[][] M = UtilitariosMatriz.copiarMatriz(matrizA);
            double[] v = UtilitariosMatriz.copiarVetor(vetorB);
            passos.append("Matriz inicial aumentada:\n");
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    passos.append(String.format("%10.6f ", M[i][j]));
                }
                passos.append("| ").append(String.format("%10.6f", v[i])).append("\n");
            }
            passos.append("\n");
            for (int k = 0; k < n - 1; k++) {
                passos.append("--- Etapa ").append(k + 1).append(" ---\n");
                passos.append(String.format("Pivô atual: M[%d][%d] = %.6f\n", k, k, M[k][k]));
                if (Math.abs(M[k][k]) < 1e-12) {
                    passos.append(" Pivô nulo ou muito pequeno detectado\n");
                    passos.append("Recomendação: use pivoteamento parcial ou completo\n");
                    resultado.passos = passos.toString();
                    return ResultadoSolucao.criarErro("Pivô nulo na posição [" + k + "][" + k + "] - use pivoteamento");
                }
                for (int i = k + 1; i < n; i++) {
                    double multiplicador = M[i][k] / M[k][k];
                    passos.append(String.format("m[%d][%d] = %.6f / %.6f = %.6f\n", i, k, M[i][k], M[k][k], multiplicador));
                    if (Math.abs(multiplicador) > 1e6) {
                        passos.append("  Multiplicador muito grande - possível instabilidade numérica\n");
                    }
                    for (int j = k; j < n; j++) {
                        double valorAntigo = M[i][j];
                        M[i][j] -= multiplicador * M[k][j];
                        passos.append(String.format("  M[%d][%d] = %.6f - %.6f * %.6f = %.6f\n",
                                i, j, valorAntigo, multiplicador, M[k][j], M[i][j]));
                    }
                    double valorAntigoV = v[i];
                    v[i] -= multiplicador * v[k];
                    passos.append(String.format("  v[%d] = %.6f - %.6f * %.6f = %.6f\n",
                            i, valorAntigoV, multiplicador, v[k], v[i]));
                }
                passos.append("Matriz após eliminação da etapa ").append(k + 1).append(":\n");
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        passos.append(String.format("%10.6f ", M[i][j]));
                    }
                    passos.append("| ").append(String.format("%10.6f", v[i])).append("\n");
                }
                passos.append("\n");
            }
            passos.append("Verificando último pivô: M[").append(n-1).append("][").append(n-1).append("] = ")
                    .append(String.format("%.6f", M[n-1][n-1])).append("\n");
            if (Math.abs(M[n-1][n-1]) < 1e-12) {
                passos.append(" Último pivô nulo - sistema singular\n");
                resultado.passos = passos.toString();
                return ResultadoSolucao.criarErro("Sistema singular - último pivô é zero");
            }
            passos.append("\n=== SUBSTITUIÇÃO REVERSA ===\n");
            double[] x = new double[n];
            for (int i = n - 1; i >= 0; i--) {
                double soma = v[i];
                passos.append(String.format("x[%d]: soma inicial = %.6f", i, soma));
                for (int j = i + 1; j < n; j++) {
                    passos.append(String.format(" - (%.6f * %.6f)", M[i][j], x[j]));
                    soma -= M[i][j] * x[j];
                }
                passos.append(String.format(" = %.6f\n", soma));
                if (Math.abs(M[i][i]) < 1e-12) {
                    passos.append(" Divisão por zero em x[").append(i).append("]\n");
                    resultado.passos = passos.toString();
                    return ResultadoSolucao.criarErro("Divisão por zero na substituição reversa - sistema singular");
                }
                x[i] = soma / M[i][i];
                passos.append(String.format("x[%d] = %.6f / %.6f = %.6f\n\n", i, soma, M[i][i], x[i]));
            }
            if (UtilitariosMatriz.contemNaNouInfinito(x)) {
                passos.append(" Solução contém valores inválidos\n");
                resultado.passos = passos.toString();
                return ResultadoSolucao.criarErro("Solução contém valores NaN ou infinito");
            }
            double[] residuo = UtilitariosMatriz.subtrairVetores(
                    multiplicarMatrizVetor(matrizA, x), vetorB);
            double normaResiduo = UtilitariosMatriz.normaInfinita(residuo);
            passos.append(" Sistema resolvido com sucesso!\n");
            passos.append("Solução final:\n").append(UtilitariosMatriz.vetorParaString(x));
            passos.append(String.format("Norma do resíduo: %.2e\n", normaResiduo));
            if (normaResiduo > 1e-6) {
                passos.append("  Resíduo relativamente alto - verifique a precisão\n");
            }
            resultado.solucao = x;
            resultado.mensagem = "Eliminação de Gauss sem pivoteamento concluída com sucesso";
            resultado.passos = passos.toString();
            return resultado;
        } catch (Exception e) {
            resultado.passos = passos.toString();
            return ResultadoSolucao.criarErro("Erro durante eliminação de Gauss: " + e.getMessage());
        }
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