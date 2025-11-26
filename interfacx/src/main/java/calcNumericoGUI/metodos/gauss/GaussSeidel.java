package calcNumericoGUI.metodos.gauss;

import calcNumericoGUI.metodos.UtilitariosMatriz;
import calcNumericoGUI.metodos.ResultadoSolucao;

import java.util.Locale;

public class GaussSeidel {

    public static ResultadoSolucao resolver(double[][] matrizA, double[] vetorB, double tolerancia,
                                            int maximoIteracoes, boolean mostrarPassos, double[] chuteInicial) {

        ResultadoSolucao resultado = new ResultadoSolucao();
        StringBuilder passos = new StringBuilder();

        try {
            if (!UtilitariosMatriz.verificarSistemaValido(matrizA, vetorB)) {
                return ResultadoSolucao.criarErro("Sistema inválido: matriz não é quadrada ou vetor b tem tamanho incorreto");
            }

            int n = matrizA.length;
            passos.append("=== METODO DE GAUSS-SEIDEL ===\n");
            passos.append("Dimensao do sistema: ").append(n).append("x").append(n).append("\n");
            passos.append("Tolerancia: ").append(tolerancia).append("\n");
            passos.append("Maximo de iteracoes: ").append(maximoIteracoes).append("\n\n");

            if (UtilitariosMatriz.contemNaNouInfinito(matrizA) || UtilitariosMatriz.contemNaNouInfinito(vetorB)) {
                return ResultadoSolucao.criarErro("Matriz ou vetor contem valores invalidos (NaN ou infinito)");
            }

            boolean diagonalDominante = verificarDiagonalDominante(matrizA);
            if (!diagonalDominante) {
                passos.append("AVISO: Matriz nao e diagonalmente dominante - convergencia nao garantida\n");
            } else {
                passos.append("Matriz e diagonalmente dominante - convergencia garantida\n");
            }

            double[][] A = UtilitariosMatriz.copiarMatriz(matrizA);
            double[] b = UtilitariosMatriz.copiarVetor(vetorB);
            double[] x;

            if (chuteInicial != null && chuteInicial.length == n) {
                x = UtilitariosMatriz.copiarVetor(chuteInicial);
                passos.append("Usando chute inicial fornecido\n");
            } else {
                x = new double[n];
                passos.append("Usando chute inicial zero\n");
            }

            passos.append("Chute inicial: ").append(UtilitariosMatriz.vetorParaString(x)).append("\n");

            for (int iteracao = 1; iteracao <= maximoIteracoes; iteracao++) {
                double[] xAnterior = UtilitariosMatriz.copiarVetor(x);

                for (int i = 0; i < n; i++) {
                    double soma = b[i];

                    for (int j = 0; j < n; j++) {
                        if (j != i) {
                            soma -= A[i][j] * x[j];
                        }
                    }

                    if (Math.abs(A[i][i]) < 1e-15) {
                        passos.append("ERRO: Elemento diagonal nulo na linha ").append(i).append("\n");
                        return ResultadoSolucao.criarErro("Elemento diagonal A[" + i + "][" + i + "] e zero");
                    }

                    x[i] = soma / A[i][i];

                    if (Double.isNaN(x[i]) || Double.isInfinite(x[i])) {
                        passos.append("ERRO: Valor invalido gerado na iteracao ").append(iteracao).append(", linha ").append(i).append("\n");
                        return ResultadoSolucao.criarErro("Divergencia detectada - valores NaN ou infinito");
                    }
                }

                double[] diferenca = UtilitariosMatriz.subtrairVetores(x, xAnterior);
                double normaDiferenca = UtilitariosMatriz.normaInfinita(diferenca);
                double normaX = UtilitariosMatriz.normaInfinita(x);
                double erroRelativo = normaDiferenca / Math.max(normaX, 1e-12);

                if (mostrarPassos) {
                    passos.append(String.format(Locale.US, "Iteracao %d:\n", iteracao));
                    passos.append("Solucao atual: ").append(UtilitariosMatriz.vetorParaString(x));
                    passos.append(String.format(Locale.US, "Diferenca: %.10f\n", normaDiferenca));
                    passos.append(String.format(Locale.US, "Erro relativo: %.10f\n", erroRelativo));
                    passos.append(String.format(Locale.US, "Criterio: %.10f < %.10f ? %s\n\n",
                            erroRelativo, tolerancia, erroRelativo < tolerancia ? "SIM" : "NAO"));
                }

                if (erroRelativo < tolerancia) {
                    passos.append("CONVERGENCIA ALCANCADA!\n");
                    passos.append(String.format("Iteracoes totais: %d\n", iteracao));
                    passos.append(String.format("Erro relativo final: %.2e\n", erroRelativo));

                    double[] residuo = UtilitariosMatriz.subtrairVetores(
                            multiplicarMatrizVetor(matrizA, x), vetorB);
                    double normaResiduo = UtilitariosMatriz.normaInfinita(residuo);
                    passos.append(String.format("Norma do residuo: %.2e\n", normaResiduo));

                    resultado.solucao = x;
                    resultado.iteracoes = iteracao;
                    resultado.mensagem = "Gauss-Seidel convergiu com sucesso";
                    resultado.passos = passos.toString();
                    return resultado;
                }

                if (UtilitariosMatriz.normaInfinita(x) > 1e10) {
                    passos.append("DIVERGENCIA DETECTADA - valores muito grandes\n");
                    return ResultadoSolucao.criarErro("Metodo divergiu - valores da solucao muito grandes");
                }
            }

            passos.append("MAXIMO DE ITERACOES ATINGIDO\n");
            passos.append("Solucao aproximada:\n").append(UtilitariosMatriz.vetorParaString(x));

            double[] diferencaFinal = UtilitariosMatriz.subtrairVetores(x,
                    UtilitariosMatriz.copiarVetor(chuteInicial != null ? chuteInicial : new double[n]));
            double normaDiferencaFinal = UtilitariosMatriz.normaInfinita(diferencaFinal);
            passos.append(String.format("Diferenca final: %.2e\n", normaDiferencaFinal));

            resultado.solucao = x;
            resultado.iteracoes = maximoIteracoes;
            resultado.mensagem = "Gauss-Seidel atingiu o maximo de iteracoes sem convergir";
            resultado.passos = passos.toString();
            return resultado;

        } catch (Exception e) {
            resultado.passos = passos.toString();
            return ResultadoSolucao.criarErro("Erro durante metodo de Gauss-Seidel: " + e.getMessage());
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