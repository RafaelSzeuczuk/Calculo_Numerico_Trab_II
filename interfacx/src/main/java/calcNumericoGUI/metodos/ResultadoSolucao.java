package calcNumericoGUI.metodos;

public class ResultadoSolucao {
    public double[] solucao;
    public int iteracoes;
    public String mensagem;
    public String passos;

    public ResultadoSolucao() {
        this.solucao = null;
        this.iteracoes = 0;
        this.mensagem = "";
        this.passos = "";
    }

    public ResultadoSolucao(double[] solucao, String mensagem) {
        this.solucao = solucao;
        this.iteracoes = 0;
        this.mensagem = mensagem;
        this.passos = "";
    }

    public ResultadoSolucao(double[] solucao, int iteracoes, String mensagem) {
        this.solucao = solucao;
        this.iteracoes = iteracoes;
        this.mensagem = mensagem;
        this.passos = "";
    }

    public ResultadoSolucao(double[] solucao, int iteracoes, String mensagem, String passos) {
        this.solucao = solucao;
        this.iteracoes = iteracoes;
        this.mensagem = mensagem;
        this.passos = passos;
    }

    // Método para verificar se a solução é válida
    public boolean solucaoValida() {
        if (solucao == null) return false;
        for (double valor : solucao) {
            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                return false;
            }
        }
        return true;
    }

    // Método para criar resultado de erro
    public static ResultadoSolucao criarErro(String mensagemErro) {
        ResultadoSolucao resultado = new ResultadoSolucao();
        resultado.mensagem = "ERRO: " + mensagemErro;
        resultado.solucao = null;
        return resultado;
    }

    // Método para criar resultado de sucesso
    public static ResultadoSolucao criarSucesso(double[] solucao, String mensagem) {
        return new ResultadoSolucao(solucao, mensagem);
    }

    // Método para criar resultado iterativo
    public static ResultadoSolucao criarIterativo(double[] solucao, int iteracoes, String mensagem) {
        return new ResultadoSolucao(solucao, iteracoes, mensagem);
    }
}