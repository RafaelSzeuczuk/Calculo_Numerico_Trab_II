package calcNumericoGUI.metodos.gauss;
import calcNumericoGUI.metodos.UtilitariosMatriz;
import calcNumericoGUI.metodos.ResultadoSolucao;
public class GaussPivoteamentoCompleto {
    public static ResultadoSolucao resolver(double[][] matrizA, double[] vetorB) {
        ResultadoSolucao resultado = new ResultadoSolucao();
        StringBuilder passos = new StringBuilder();
        try {
            if (!UtilitariosMatriz.verificarSistemaValido(matrizA, vetorB)) {
                return ResultadoSolucao.criarErro("Sistema inválido: matriz não é quadrada ou vetor b tem tamanho incorreto");
            }
            int n = matrizA.length;
            passos.append("=== ELIMINAÇÃO DE GAUSS COM PIVOTEAMENTO COMPLETO ===\n");
            passos.append("Dimensão do sistema: ").append(n).append("x").append(n).append("\n\n");
            if (UtilitariosMatriz.contemNaNouInfinito(matrizA) || UtilitariosMatriz.contemNaNouInfinito(vetorB)) {
                return ResultadoSolucao.criarErro("Matriz ou vetor contém valores inválidos (NaN ou infinito)");
            }
            double[][] M = UtilitariosMatriz.copiarMatriz(matrizA);
            double[] v = UtilitariosMatriz.copiarVetor(vetorB);
            int[] permutacao = new int[n]; 
            for (int i = 0; i < n; i++) {
                permutacao[i] = i;
            }
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
                int linhaPivo = k, colunaPivo = k;
                double maxPivo = Math.abs(M[k][k]);
                for (int i = k; i < n; i++) {
                    for (int j = k; j < n; j++) {
                        double valorAtual = Math.abs(M[i][j]);
                        if (valorAtual > maxPivo) {
                            maxPivo = valorAtual;
                            linhaPivo = i;
                            colunaPivo = j;
                        }
                    }
                }
                passos.append(String.format("Pivô máximo encontrado: |%.6f| na posição [%d][%d]\n",
                        M[linhaPivo][colunaPivo], linhaPivo, colunaPivo));
                if (Math.abs(maxPivo) < 1e-12) {
                    passos.append(" Pivô muito próximo de zero - matriz singular\n");
                    resultado.passos = passos.toString();
                    return ResultadoSolucao.criarErro("Matriz singular ou quase singular - pivoteamento completo não resolveu");
                }
                if (linhaPivo != k) {
                    passos.append(String.format("Trocando linha %d com linha %d\n", k, linhaPivo));
                    double[] tempLinha = M[k];
                    M[k] = M[linhaPivo];
                    M[linhaPivo] = tempLinha;
                    double tempVetor = v[k];
                    v[k] = v[linhaPivo];
                    v[linhaPivo] = tempVetor;
                }
                if (colunaPivo != k) {
                    passos.append(String.format("Trocando coluna %d com coluna %d\n", k, colunaPivo));
                    for (int i = 0; i < n; i++) {
                        double temp = M[i][k];
                        M[i][k] = M[i][colunaPivo];
                        M[i][colunaPivo] = temp;
                    }
                    int tempPerm = permutacao[k];
                    permutacao[k] = permutacao[colunaPivo];
                    permutacao[colunaPivo] = tempPerm;
                }
                passos.append("Matriz após pivoteamento:\n");
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        passos.append(String.format("%10.6f ", M[i][j]));
                    }
                    passos.append("| ").append(String.format("%10.6f", v[i])).append("\n");
                }
                passos.append("\nEliminação:\n");
                for (int i = k + 1; i < n; i++) {
                    if (Math.abs(M[k][k]) < 1e-12) {
                        passos.append(" Divisão por zero detectada\n");
                        resultado.passos = passos.toString();
                        return ResultadoSolucao.criarErro("Divisão por zero durante eliminação - sistema singular");
                    }
                    double multiplicador = M[i][k] / M[k][k];
                    passos.append(String.format("m[%d][%d] = %.6f / %.6f = %.6f\n", i, k, M[i][k], M[k][k], multiplicador));
                    for (int j = k; j < n; j++) {
                        M[i][j] -= multiplicador * M[k][j];
                    }
                    v[i] -= multiplicador * v[k];
                }
                passos.append("Matriz após eliminação:\n");
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        passos.append(String.format("%10.6f ", M[i][j]));
                    }
                    passos.append("| ").append(String.format("%10.6f", v[i])).append("\n");
                }
                passos.append("\n");
            }
            if (Math.abs(M[n-1][n-1]) < 1e-12) {
                passos.append(" Último pivô muito próximo de zero - sistema singular\n");
                resultado.passos = passos.toString();
                return ResultadoSolucao.criarErro("Sistema singular - último pivô é zero");
            }
            passos.append("=== SUBSTITUIÇÃO REVERSA ===\n");
            double[] x = new double[n];
            for (int i = n - 1; i >= 0; i--) {
                double soma = v[i];
                for (int j = i + 1; j < n; j++) {
                    soma -= M[i][j] * x[j];
                }
                if (Math.abs(M[i][i]) < 1e-12) {
                    passos.append(" Divisão por zero em x[").append(i).append("]\n");
                    resultado.passos = passos.toString();
                    return ResultadoSolucao.criarErro("Divisão por zero na substituição reversa - sistema singular");
                }
                x[i] = soma / M[i][i];
                passos.append(String.format("x[%d] = (%.6f) / %.6f = %.6f\n", i, soma, M[i][i], x[i]));
            }
            passos.append("\nReordenando solução conforme permutação das colunas...\n");
            double[] solucaoFinal = new double[n];
            for (int i = 0; i < n; i++) {
                solucaoFinal[permutacao[i]] = x[i];
            }
            if (UtilitariosMatriz.contemNaNouInfinito(solucaoFinal)) {
                passos.append(" Solução contém valores inválidos\n");
                resultado.passos = passos.toString();
                return ResultadoSolucao.criarErro("Solução contém valores NaN ou infinito");
            }
            passos.append("\n Sistema resolvido com sucesso!\n");
            passos.append("Solução final:\n").append(UtilitariosMatriz.vetorParaString(solucaoFinal));
            passos.append("Permutação aplicada: ");
            for (int i = 0; i < n; i++) {
                passos.append(permutacao[i]).append(" ");
            }
            passos.append("\n");
            resultado.solucao = solucaoFinal;
            resultado.mensagem = "Eliminação de Gauss com pivoteamento completo concluída com sucesso";
            resultado.passos = passos.toString();
            return resultado;
        } catch (Exception e) {
            resultado.passos = passos.toString();
            return ResultadoSolucao.criarErro("Erro durante eliminação de Gauss com pivoteamento completo: " + e.getMessage());
        }
    }
}