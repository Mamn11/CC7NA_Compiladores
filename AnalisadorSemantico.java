import java.util.List;
import java.util.Map;

public class AnalisadorSemantico {
    private final Map<String, String> tabelaSimbolos;
    private final List<String> tokens;
    private boolean erroSemantico = false;

    public AnalisadorSemantico(Map<String, String> tabelaSimbolos, List<String> tokens) {
        this.tabelaSimbolos = tabelaSimbolos;
        this.tokens = tokens;
    }

    public boolean analisar() {
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            // Verifica atribuição: ID = EXPRESSAO ;
            if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                String id = token;
                String tipo = tabelaSimbolos.get(id);

                // Verifica se é uma atribuição
                if (i + 1 < tokens.size() && tokens.get(i + 1).equals("=")) {
                    // Não permite atribuição a constantes
                    if ("CONSTANTE".equals(tipo)) {
                        // Só acusa erro se não for a declaração original (ou seja, se já existe na tabela)
                        if (i > 0 && !(tokens.get(i - 1).equals("final"))) {
                            System.err.println("Erro semântico: não é permitido atribuir à constante '" + id + "'");
                            erroSemantico = true;
                        }
                    }
                    // Verifica se variável foi declarada
                    if (tipo == null) {
                        System.err.println("Erro semântico: variável '" + id + "' não declarada.");
                        erroSemantico = true;
                    }
                    // Não faz checagem de tipo para expressão, apenas para literais
                    // (Opcional: você pode tentar analisar a expressão para checar tipos, mas isso é mais complexo)
                } else if (!tabelaSimbolos.containsKey(id) && !isPalavraReservada(id)) {
                    // Uso de variável não declarada
                    System.err.println("Erro semântico: variável '" + id + "' não declarada.");
                    erroSemantico = true;
                }
            }
        }
        return !erroSemantico;
    }

    private boolean isPalavraReservada(String token) {
        return token.equals("int") || token.equals("string") || token.equals("boolean") ||
               token.equals("byte") || token.equals("final") || token.equals("true") ||
               token.equals("false") || token.equals("if") || token.equals("else") ||
               token.equals("while") || token.equals("begin") || token.equals("end") ||
               token.equals("write") || token.equals("writeln") || token.equals("read") ||
               token.equals("readln");
    }
}