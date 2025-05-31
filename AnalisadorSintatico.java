import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class AnalisadorSintatico {
    // ========== ATRIBUTOS ==========
    private final ListIterator<String> iterator;
    private String tokenAtual;
    private boolean erroSintatico = false;
    private final Map<String, String> tabelaSimbolos;
    
    private int numeroLinha = 1;
    private int posicaoToken = 0;
    private int posicaoLinha = 1;

    // ========== CONSTRUTOR ==========
    public AnalisadorSintatico(List<String> tokens) {
        this.iterator = tokens.listIterator();
        this.tabelaSimbolos = inicializarTabelaSimbolos();
        this.avancar();
    }

    // ========== MÉTODOS PÚBLICOS ==========
    public Map<String, String> getTabelaSimbolos() {
        return tabelaSimbolos;
    }

    public boolean analisar() {
        programa();
        return !erroSintatico && tokenAtual == null;
    }

    // ========== INICIALIZAÇÃO ==========
    private Map<String, String> inicializarTabelaSimbolos() {
        Map<String, String> tabela = new HashMap<>();
        // Palavras reservadas
        tabela.put("int", "TIPO");
        tabela.put("string", "TIPO");
        tabela.put("boolean", "TIPO");
        tabela.put("byte", "TIPO");
        tabela.put("final", "MODIFICADOR");
        tabela.put("true", "BOOLEAN");
        tabela.put("false", "BOOLEAN");
        return tabela;
    }

    // ========== MANIPULAÇÃO DE TOKENS ==========
    private void avancar() {
        if (iterator.hasNext()) {
            tokenAtual = iterator.next();
            posicaoToken++;
            
            if (tokenAtual.equals("\n")) {
                numeroLinha++;
                posicaoLinha = 1;
            } else {
                posicaoLinha += tokenAtual.length();
            }
        } else {
            tokenAtual = null;
        }
    }

    private boolean consumir(String esperado) {
        if (tokenAtual != null && tokenAtual.equals(esperado)) {
            avancar();
            return true;
        }
        reportarErroSintatico(esperado, tokenAtual);
        return false;
    }

    private boolean verificar(String esperado) {
        return tokenAtual != null && tokenAtual.equals(esperado);
    }

    // ========== VERIFICAÇÕES ==========
    private boolean verificarTipo() {
        return verificar("string") || verificar("int") || verificar("byte") || verificar("boolean");
    }

    private boolean verificarConstante() {
        return verificar("true") || verificar("false") || 
               (tokenAtual != null && (tokenAtual.matches("\".*\"") || tokenAtual.matches("[0-9]+")));
    }

    private boolean ehOperadorRelacional() {
        return verificar(">=") || verificar("<=") || verificar("==") || verificar("!=")
            || verificar(">") || verificar("<");
    }

    private boolean ehIdentificadorValido() {
        return tokenAtual != null && tokenAtual.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    private String determinarTipoConstante(String valor) {
        if (valor.matches("[0-9]+")) return "INT";
        if (valor.matches("\".*\"")) return "STRING";
        if (valor.equals("true") || valor.equals("false")) return "BOOLEAN";
        return "CONSTANTE";
    }

    // ========== RELATÓRIO DE ERROS ==========
    private void reportarErroSintatico(String esperado, String encontrado) {
        System.err.printf("[ERRO SINTÁTICO] Linha %d:%d - Esperado: %s, Encontrado: %s%n",
                        numeroLinha, posicaoLinha,
                        esperado,
                        encontrado != null ? "'" + encontrado + "'" : "fim de arquivo");
        erroSintatico = true;
    }

    private void reportarErroSemantico(String mensagem, String contexto) {
        System.err.printf("[ERRO SEMÂNTICO] - %s: %s%n",
                        mensagem,
                        contexto);
        erroSintatico = true;
    }

    // ========== REGRAS GRAMATICAIS PRINCIPAIS ==========
    private void programa() {
        declaracoes();
        codigo();
    }

    private void declaracoes() {
        while (tokenAtual != null && (verificarTipo() || verificar("final"))) {
            declaracao();
        }
    }

    // ========== DECLARAÇÕES ==========
    private void declaracao() {
        if (verificar("final")) {
            declaracaoConstante();
        } else if (verificarTipo()) {
            declaracaoVariavel();
        }
    }

    private void declaracaoConstante() {
        avancar(); // consome 'final'
        
        String tipo = verificarTipo() ? tokenAtual : null;
        if (tipo != null) {
            avancar(); // consome o tipo
        }
        
        if (!ehIdentificadorValido()) {
            reportarErroSintatico("identificador", tokenAtual);
            return;
        }
        
        String id = tokenAtual;
        tabelaSimbolos.put(id, "CONSTANTE");
        avancar(); // consome ID
        
        if (!consumir("=")) return;
        
        if (!verificarConstante()) {
            reportarErroSintatico("valor constante", tokenAtual);
            return;
        }
        
        String valor = tokenAtual;
        tabelaSimbolos.put(id, determinarTipoConstante(valor));
        avancar(); // consome valor
        
        consumir(";");
    }

    private void declaracaoVariavel() {
        String tipo = tokenAtual;
        avancar(); // consome tipo
        
        if (!ehIdentificadorValido()) {
            reportarErroSintatico("identificador", tokenAtual);
            return;
        }
        
        String id = tokenAtual;
        tabelaSimbolos.put(id, tipo.toUpperCase());
        avancar(); // consome ID
        
        if (verificar("=")) {
            avancar(); // consome '='
            if (!verificarConstante()) {
                reportarErroSintatico("valor constante", tokenAtual);
                return;
            }
            avancar(); // consome valor
        }
        
        consumir(";");
    }

    // ========== ESTRUTURAS DE CONTROLE ==========
    private void codigo() {
        if (!consumir("begin")) return;
        comandos();
        consumir("end");
    }

    private void comandos() {
        while (tokenAtual != null && !verificar("end") && !verificar("else")) {
            comando();
        }
    }

    private void comando() {
        if (tokenAtual == null) return;
        
        switch (tokenAtual) {
            case "write", "writeln" -> escrever();
            case "read", "readln" -> ler();
            case "if" -> condicional();
            case "while" -> loop();
            case "{" -> comentarioChaves();
            case "/*" -> comentarioBloco();
            default -> {
                if (ehIdentificadorValido() && verificarProximo("=")) {
                    atribuicao();
                } else {
                    reportarErroSintatico("comando válido", tokenAtual);
                    avancar();
                }
            }
        }
    }

    // ========== COMANDOS DE E/S ==========
    private void escrever() {
        String comando = tokenAtual;
        avancar(); // consome write/writeln
        
        if (!consumir(",")) return;
        
        do {
            if (verificarConstante() || ehIdentificadorValido()) {
                avancar();
            } else if (verificar("(")) {
                expressao();
            } else {
                reportarErroSintatico("termo válido", tokenAtual);
                avancar();
            }

            if (!verificar(",")) break;
            avancar();
        } while (true);
        
        consumir(";");
    }

    private void ler() {
        String comando = tokenAtual;
        avancar(); // consome read/readln
        
        if (!consumir(",")) return;
        
        if (!ehIdentificadorValido()) {
            reportarErroSintatico("identificador", tokenAtual);
            return;
        }
        avancar();
        
        consumir(";");
    }

    // ========== ATRIBUIÇÕES ==========
    private void atribuicao() {
        String id = tokenAtual;
        String tipoVar = tabelaSimbolos.get(id);

        if (tipoVar == null) {
            reportarErroSemantico("variável não declarada", id);
        } else if ("CONSTANTE".equals(tipoVar)) {
            reportarErroSemantico("não é permitido atribuir à constante", id);
        }

        avancar(); // consome ID

        if (!consumir("=")) return;

        StringBuilder expr = new StringBuilder();
        boolean temRelacional = false;
        boolean valorBooleano = false;
        boolean valorInteiro = false;
        boolean valorString = false;

        while (tokenAtual != null && !verificar(";")) {
            String tokenExpr = tokenAtual;
            expr.append(tokenExpr);

            if (tokenExpr.matches("[0-9]+")) valorInteiro = true;
            else if (tokenExpr.matches("\".*\"")) valorString = true;
            else if (tokenExpr.equals("true") || tokenExpr.equals("false")) valorBooleano = true;
            else if (ehOperadorRelacional()) temRelacional = true;

            avancar();
        }

        if (tipoVar != null) {
            String exprStr = expr.toString();
            switch (tipoVar) {
                case "INT" -> {
                    if (valorString || valorBooleano) {
                        reportarErroSemantico("atribuição de tipo incompatível para int", exprStr);
                    }
                }
                case "STRING" -> {
                    if (valorInteiro || valorBooleano) {
                        reportarErroSemantico("atribuição de tipo incompatível para string", exprStr);
                    }
                }
                case "BOOLEAN" -> {
                    if (!valorBooleano || temRelacional) {
                        reportarErroSemantico("expressão inválida para booleano", exprStr);
                    }
                }
            }
        }

        consumir(";");
    }

    // ========== ESTRUTURAS DE CONTROLE ==========
    private void condicional() {
        avancar(); // consome if
        
        boolean temParenteses = verificar("(");
        if (temParenteses) {
            avancar();
        }
        
        expressaoLogica();
        
        if (temParenteses) {
            consumir(")");
        }
        
        if (verificar("begin")) {
            avancar();
            comandos();
            if (!consumir("end")) return;
            
            if (verificar("else")) {
                avancar();
                if (verificar("begin")) {
                    avancar();
                    comandos();
                    consumir("end");
                } else {
                    comando();
                }
            }
        } else {
            comando();
        }
    }

    private void loop() {
        avancar(); // consome while
        
        boolean temParenteses = verificar("(");
        if (temParenteses) {
            avancar();
        }
        
        expressaoLogica();
        
        if (temParenteses) {
            consumir(")");
        }
        
        if (verificar("begin")) {
            avancar();
            comandos();
            consumir("end");
        } else {
            comando();
        }
    }

    // ========== EXPRESSÕES ==========
    private void expressao() {
        termo();
        while (tokenAtual != null && (verificar("+") || verificar("-") || verificar("*") || verificar("/"))) {
            avancar();
            termo();
        }
    }

    private void expressaoLogica() {
        expressao();
        if (ehOperadorRelacional()) {
            avancar();
            expressao();
        }
    }

    private void termo() {
        if (tokenAtual == null) {
            reportarErroSintatico("termo válido", null);
            return;
        }
        
        if (ehIdentificadorValido() || tokenAtual.matches("[0-9]+") || 
            tokenAtual.matches("\".*\"") || verificar("true") || verificar("false")) {
            avancar();
        } else if (verificar("(")) {
            avancar();
            expressao();
            consumir(")");
        } else {
            reportarErroSintatico("termo válido", tokenAtual);
        }
    }

    // ========== COMENTÁRIOS ==========
    private void comentarioChaves() {
        avancar(); // consome '{'
        while (tokenAtual != null && !verificar("}")) {
            avancar();
        }
        consumir("}");
    }

    private void comentarioBloco() {
        avancar(); // consome '/*'
        while (tokenAtual != null && !verificar("*/")) {
            avancar();
        }
        consumir("*/");
    }

    // Adicione este método auxiliar:
    private boolean verificarProximo(String esperado) {
        if (!iterator.hasNext()) return false;
        String proximo = iterator.next();
        // Volta o iterator para não consumir o token
        ((java.util.ListIterator<String>) iterator).previous();
        return esperado.equals(proximo);
    }
}