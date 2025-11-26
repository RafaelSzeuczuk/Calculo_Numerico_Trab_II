package calcNumericoGUI.metodos;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * VerificadorEntrada - semelhante ao VerificaFuncao original, mas lê a partir
 * de uma string (conteúdo da TextArea) ao invés de um arquivo.
 *
 * Formato esperado (cada linha contendo "chave: valor"):
 * funcao: e^(-x^2)-cos(x)
 * fi: cos(x)-e^(-x^2) + x
 * a: 1
 * b: 2
 * delta:0.001
 * n: 50
 */
public class VerificadorEntrada {

    private Map<String, String> parametros = new HashMap<>();
    private String expressaoFuncaoRaw;

    public VerificadorEntrada(String conteudo) {
        lerConteudo(conteudo);
    }

    private void lerConteudo(String conteudo) {
        String[] linhas = conteudo.split("\\r?\\n");
        for (String linha : linhas) {
            if (linha == null) continue;
            linha = linha.trim();
            if (linha.isEmpty()) continue;
            if (linha.contains(":")) {
                String[] partes = linha.split(":", 2);
                String chave = partes[0].trim().toLowerCase();
                String valor = partes[1].trim();
                parametros.put(chave, valor);
                if (chave.equals("funcao")) {
                    expressaoFuncaoRaw = valor;
                }
            }
        }
    }

    public Map<String, String> getParametros() {
        return parametros;
    }

    /**
     * Avalia a função 'funcao' no ponto x. Converte e^ para exp se necessário.
     */
    public double avaliar(double x) {
        String stringFuncao = parametros.get("funcao");
        if (stringFuncao == null) {
            throw new RuntimeException("Parâmetro 'funcao' não encontrado.");
        }
        // substitui notações comuns para exp4j
        stringFuncao = stringFuncao.replaceAll("e\\^", "exp");
        // exp4j aceita 'exp(x)' e funções trigonométricas
        Expression expressao = new ExpressionBuilder(stringFuncao)
                .variable("x")
                .build()
                .setVariable("x", x);
        return expressao.evaluate();
    }
}
