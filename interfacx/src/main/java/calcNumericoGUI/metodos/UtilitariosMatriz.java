package calcNumericoGUI.metodos;

import java.util.Locale;

public class UtilitariosMatriz {

    public static double[][] copiarMatriz(double[][] A) {
        if (A == null || A.length == 0) {
            throw new IllegalArgumentException("Matriz não pode ser nula ou vazia");
        }
        int n = A.length;
        double[][] B = new double[n][n];
        for (int i = 0; i < n; i++) {
            if (A[i] == null || A[i].length != n) {
                throw new IllegalArgumentException("Matriz deve ser quadrada e completa");
            }
            System.arraycopy(A[i], 0, B[i], 0, n);
        }
        return B;
    }

    public static double[] copiarVetor(double[] v) {
        if (v == null) {
            throw new IllegalArgumentException("Vetor não pode ser nulo");
        }
        double[] novoVetor = new double[v.length];
        System.arraycopy(v, 0, novoVetor, 0, v.length);
        return novoVetor;
    }

    public static String matrizParaString(double[][] A) {
        if (A == null || A.length == 0) {
            return "Matriz vazia ou nula";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < A.length; i++) {
            if (A[i] == null) {
                sb.append("Linha ").append(i).append(" nula\n");
                continue;
            }
            for (int j = 0; j < A[i].length; j++) {
                sb.append(String.format(Locale.US, "%12.6f ", A[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String vetorParaString(double[] v) {
        if (v == null) {
            return "Vetor nulo";
        }
        StringBuilder sb = new StringBuilder();
        for (double x : v) {
            if (Double.isNaN(x) || Double.isInfinite(x)) {
                sb.append(String.format(Locale.US, "%12s ", "ERRO"));
            } else {
                sb.append(String.format(Locale.US, "%12.6f ", x));
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    public static double[] subtrairVetores(double[] a, double[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Vetores não podem ser nulos");
        }
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vetores devem ter o mesmo tamanho");
        }
        double[] resultado = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            resultado[i] = a[i] - b[i];
        }
        return resultado;
    }

    public static double normaInfinita(double[] v) {
        if (v == null || v.length == 0) {
            throw new IllegalArgumentException("Vetor não pode ser nulo ou vazio");
        }
        double max = 0;
        for (double x : v) {
            if (Double.isNaN(x) || Double.isInfinite(x)) {
                return Double.NaN;
            }
            max = Math.max(max, Math.abs(x));
        }
        return max;
    }

    public static boolean verificarMatrizQuadrada(double[][] A) {
        if (A == null || A.length == 0) return false;
        int n = A.length;
        for (int i = 0; i < n; i++) {
            if (A[i] == null || A[i].length != n) return false;
        }
        return true;
    }

    public static boolean verificarSistemaValido(double[][] A, double[] b) {
        if (!verificarMatrizQuadrada(A)) return false;
        if (b == null || b.length != A.length) return false;
        return true;
    }

    public static boolean contemNaNouInfinito(double[][] A) {
        if (A == null) return true;
        for (double[] linha : A) {
            if (linha == null) return true;
            for (double valor : linha) {
                if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean contemNaNouInfinito(double[] v) {
        if (v == null) return true;
        for (double valor : v) {
            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                return true;
            }
        }
        return false;
    }
}