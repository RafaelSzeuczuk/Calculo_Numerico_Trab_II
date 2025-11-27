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

            double[][] A = UtilitariosMatriz.copiarMatriz(matrizA);
            double[] b = UtilitariosMatriz.copiarVetor(vetorB);

            passos.append("Matriz original:\n").append(UtilitariosMatriz.matrizParaString(A));
            passos.append("Vetor b: ").append(UtilitariosMatriz.vetorParaString(b)).append("\n");

            passos.append("=== TENTATIVA DE MELHORIA DE CONVERGÊNCIA ===\n");
            boolean permutou = tentarMelhorarConvergencia(A, b);
            if (permutou) {
                passos.append("✓ Permutação de linhas realizada\n");
                passos.append("Matriz após permutação:\n").append(UtilitariosMatriz.matrizParaString(A));
                passos.append("Vetor b após permutação: ").append(UtilitariosMatriz.vetorParaString(b)).append("\n");
            }

            passos.append("=== CRITÉRIOS DE CONVERGÊNCIA ===\n");
            boolean criterioLinhas = verificarCriterioLinhas(A);
            boolean criterioColunas = verificarCriterioColunas(A);
            boolean diagonalDominante = verificarDiagonalDominante(A);

            if (criterioLinhas) {
                passos.append("✓ Critério das linhas SATISFEITO\n");
            } else {
                passos.append("✗ Critério das linhas NÃO satisfeito\n");
            }

            if (criterioColunas) {
                passos.append("✓ Critério das colunas SATISFEITO\n");
            } else {
                passos.append("✗ Critério das colunas NÃO satisfeito\n");
            }

            if (diagonalDominante) {
                passos.append("✓ Matriz diagonalmente dominante\n");
            } else {
                passos.append("✗ Matriz NÃO diagonalmente dominante\n");
            }

            if (!criterioLinhas && !criterioColunas && !diagonalDominante) {
                passos.append("AVISO: Usando fator de relaxação para tentar convergência\n");
            }
            passos.append("\n");

            double[] x;
            double[] xNovo = new double[n];

            if (chuteInicial != null && chuteInicial.length == n) {
                x = UtilitariosMatriz.copiarVetor(chuteInicial);
                passos.append("Usando chute inicial fornecido\n");
            } else {
                x = new double[n];
                passos.append("Usando chute inicial zero\n");
            }

            passos.append("Chute inicial: ").append(UtilitariosMatriz.vetorParaString(x)).append("\n\n");

            // Fator de relaxação para sistemas problemáticos
            double fatorRelaxacao = 0.5;
            passos.append("Usando fator de relaxação: ").append(fatorRelaxacao).append("\n\n");

            for (int iteracao = 1; iteracao <= maximoIteracoes; iteracao++) {
                for (int i = 0; i < n; i++) {
                    double soma = b[i];
                    for (int j = 0; j < n; j++) {
                        if (j != i) {
                            soma -= A[i][j] * x[j];
                        }
                    }

                    if (Math.abs(A[i][i]) < 1e-15) {
                        passos.append("ERRO: Elemento diagonal nulo na linha ").append(i).append("\n");
                        return ResultadoSolucao.criarErro("Elemento diagonal A[" + i + "][" + i + "] é zero");
                    }

                    double novoValor = soma / A[i][i];
                    // Aplica relaxação: xNovo = (1-w)*xAntigo + w*novoValor
                    xNovo[i] = (1 - fatorRelaxacao) * x[i] + fatorRelaxacao * novoValor;

                    if (Double.isNaN(xNovo[i]) || Double.isInfinite(xNovo[i])) {
                        passos.append("ERRO: Divergência detectada na iteração ").append(iteracao)
                                .append(", linha ").append(i).append("\n");
                        return ResultadoSolucao.criarErro("Divergência detectada - valores NaN ou infinito");
                    }
                }

                double[] diferenca = UtilitariosMatriz.subtrairVetores(xNovo, x);
                double normaDiferenca = UtilitariosMatriz.normaInfinita(diferenca);
                double normaX = UtilitariosMatriz.normaInfinita(xNovo);
                double erroRelativo = normaDiferenca / Math.max(normaX, 1e-12);

                if (mostrarPassos && iteracao <= 10) {
                    passos.append(String.format(Locale.US, "Iteração %d:\n", iteracao));
                    passos.append("Solução atual: ").append(UtilitariosMatriz.vetorParaString(xNovo));
                    passos.append(String.format(Locale.US, "Diferença: %.6e\n", normaDiferenca));
                    passos.append(String.format(Locale.US, "Erro relativo: %.6e\n", erroRelativo));
                    passos.append(String.format(Locale.US, "Critério: %.6e < %.6e ? %s\n\n",
                            erroRelativo, tolerancia, erroRelativo < tolerancia ? "SIM" : "NÃO"));
                }

                System.arraycopy(xNovo, 0, x, 0, n);

                if (erroRelativo < tolerancia) {
                    passos.append("CONVERGÊNCIA ALCANÇADA!\n");
                    passos.append(String.format("Iterações totais: %d\n", iteracao));
                    passos.append(String.format("Erro relativo final: %.2e\n", erroRelativo));

                    double[] residuo = UtilitariosMatriz.subtrairVetores(
                            multiplicarMatrizVetor(matrizA, x), vetorB);
                    double normaResiduo = UtilitariosMatriz.normaInfinita(residuo);
                    passos.append(String.format("Norma do resíduo: %.2e\n", normaResiduo));

                    // Verifica se a solução faz sentido
                    passos.append("Verificação da solução:\n");
                    for (int i = 0; i < n; i++) {
                        double calculado = 0;
                        for (int j = 0; j < n; j++) {
                            calculado += matrizA[i][j] * x[j];
                        }
                        passos.append(String.format("Eq %d: %.6f ≈ %.6f (erro: %.6f)\n",
                                i, calculado, vetorB[i], Math.abs(calculado - vetorB[i])));
                    }

                    resultado.solucao = x;
                    resultado.iteracoes = iteracao;
                    resultado.mensagem = "Jacobi convergiu com sucesso";
                    resultado.passos = passos.toString();
                    return resultado;
                }

                if (UtilitariosMatriz.normaInfinita(x) > 1e6) {
                    passos.append("DIVERGÊNCIA DETECTADA - valores da solução muito grandes\n");
                    passos.append("Norma da solução: ").append(UtilitariosMatriz.normaInfinita(x)).append("\n");
                    return ResultadoSolucao.criarErro("Método divergiu - valores da solução muito grandes");
                }

                // Ajusta dinamicamente o fator de relaxação se necessário
                if (iteracao % 20 == 0 && normaDiferenca > 1e-2) {
                    fatorRelaxacao *= 0.9;
                    passos.append("Ajustando fator de relaxação para: ").append(fatorRelaxacao).append("\n");
                }
            }

            passos.append("MÁXIMO DE ITERAÇÕES ATINGIDO\n");
            passos.append("Solução aproximada:\n").append(UtilitariosMatriz.vetorParaString(x));

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

    private static boolean tentarMelhorarConvergencia(double[][] A, double[] b) {
        int n = A.length;
        boolean permutou = false;

        // Primeiro: garantir que não há zeros na diagonal
        for (int i = 0; i < n; i++) {
            if (Math.abs(A[i][i]) < 1e-10) {
                for (int k = i + 1; k < n; k++) {
                    if (Math.abs(A[k][i]) > 1e-10) {
                        // Troca linhas i e k
                        double[] tempA = A[i];
                        A[i] = A[k];
                        A[k] = tempA;

                        double tempB = b[i];
                        b[i] = b[k];
                        b[k] = tempB;
                        permutou = true;
                        break;
                    }
                }
            }
        }

        // Segundo: tentar melhorar dominância diagonal
        for (int i = 0; i < n; i++) {
            double maxVal = Math.abs(A[i][i]);
            int melhorLinha = i;

            for (int k = i + 1; k < n; k++) {
                if (Math.abs(A[k][i]) > maxVal) {
                    maxVal = Math.abs(A[k][i]);
                    melhorLinha = k;
                }
            }

            if (melhorLinha != i) {
                double[] tempA = A[i];
                A[i] = A[melhorLinha];
                A[melhorLinha] = tempA;

                double tempB = b[i];
                b[i] = b[melhorLinha];
                b[melhorLinha] = tempB;
                permutou = true;
            }
        }

        return permutou;
    }

    private static boolean verificarCriterioLinhas(double[][] A) {
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

    private static boolean verificarCriterioColunas(double[][] A) {
        int n = A.length;
        for (int j = 0; j < n; j++) {
            double soma = 0.0;
            for (int i = 0; i < n; i++) {
                if (i != j) {
                    soma += Math.abs(A[i][j]);
                }
            }
            if (Math.abs(A[j][j]) <= soma) {
                return false;
            }
        }
        return true;
    }

    private static boolean verificarDiagonalDominante(double[][] A) {
        return verificarCriterioLinhas(A) || verificarCriterioColunas(A);
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