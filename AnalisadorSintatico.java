import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AnalisadorSintatico {
    private final Iterator<String> iterator;
    private String tokenAtual;
    private boolean erroSintatico = false;
    private Map<String, String> tabelaSimbolos = new HashMap<>();


    // Retorna a tabela de símbolos para uso no semântico
    public Map<String, String> getTabelaSimbolos() {
        return tabelaSimbolos;
    }


    public AnalisadorSintatico(List<String> tokens) {
        this.iterator = tokens.iterator();
        this.avancar();
        
        // Inicializa tabela de símbolos com palavras reservadas
        tabelaSimbolos.put("int", "TIPO");
        tabelaSimbolos.put("string", "TIPO");
        tabelaSimbolos.put("boolean", "TIPO");
        tabelaSimbolos.put("byte", "TIPO");
        tabelaSimbolos.put("final", "MODIFICADOR");
        tabelaSimbolos.put("true", "BOOLEAN");
        tabelaSimbolos.put("false", "BOOLEAN");
    }

    public boolean analisar() {
        programa();
        return !erroSintatico && tokenAtual == null;
    }

    private void avancar() {
        if (iterator.hasNext()) {
            tokenAtual = iterator.next();
        } else {
            tokenAtual = null;
        }
    }

    private boolean consumir(String esperado) {
        if (tokenAtual != null && tokenAtual.equals(esperado)) {
            avancar();
            return true;
        }
        System.err.println("Erro sintático: esperado '" + esperado + "', encontrado '" + tokenAtual + "'");
        erroSintatico = true;
        return false;
    }

    private boolean verificar(String esperado) {
        return tokenAtual != null && tokenAtual.equals(esperado);
    }

    private boolean verificarTipo() {
        return verificar("string") || verificar("int") || verificar("byte") || verificar("boolean");
    }

    private boolean verificarConstante() {
        return verificar("true") || verificar("false") || 
               (tokenAtual != null && (tokenAtual.matches("\".*\"") || tokenAtual.matches("[0-9]+")));
    }

    private boolean ehOperadorRelacional() {
        return verificar("<") || verificar(">") || verificar("<=") || 
               verificar(">=") || verificar("==") || verificar("!=");
    }

    private void programa() {
        declaracoes();
        codigo();
    }

    private void declaracoes() {
        while (tokenAtual != null && (verificarTipo() || verificar("final"))) {
            declaracao();
        }
    }

    private void declaracao() {
        boolean ehFinal = verificar("final");
        if (ehFinal) {
            avancar(); // consome 'final'
            
            // Aceita tanto 'final TIPO ID' quanto 'final ID' (com tipo inferido)
            if (verificarTipo()) {
                avancar(); // consome o tipo (se presente)
            }
            
            if (tokenAtual == null || !tokenAtual.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                System.err.println("Erro: identificador esperado após 'final'");
                erroSintatico = true;
                return;
            }
            
            // Adiciona à tabela de símbolos
            String id = tokenAtual;
            tabelaSimbolos.put(id, "CONSTANTE");
            avancar(); // consome ID
            
            if (!verificar("=")) {
                System.err.println("Erro: esperado '=' na declaração final");
                erroSintatico = true;
                return;
            }
            avancar(); // consome '='
            
            if (!verificarConstante()) {
                System.err.println("Erro: valor constante esperado");
                erroSintatico = true;
                return;
            }
            avancar(); // consome o valor
            
            if (!verificar(";")) {
                System.err.println("Erro: esperado ';' no final");
                erroSintatico = true;
                return;
            }
            avancar(); // consome ';'
        } 
        else if (verificarTipo()) {
            // Declaração de variável normal
            String tipo = tokenAtual;
            avancar(); // consome tipo
            
            if (tokenAtual == null || !tokenAtual.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                System.err.println("Erro: identificador esperado após tipo");
                erroSintatico = true;
                return;
            }
            
            // Adiciona à tabela de símbolos
            String id = tokenAtual;
            tabelaSimbolos.put(id, tipo.toUpperCase());
            avancar(); // consome ID
            
            if (verificar("=")) {
                avancar(); // consome '='
                if (!verificarConstante()) {
                    System.err.println("Erro: valor constante esperado");
                    erroSintatico = true;
                    return;
                }
                avancar(); // consome valor
            }
            
            if (!verificar(";")) {
                System.err.println("Erro: esperado ';' no final");
                erroSintatico = true;
                return;
            }
            avancar(); // consome ';'
        }
    }

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
            case "write", "writeln" -> escreve();
            case "read", "readln" -> ler();
            case "if" -> condicional();
            case "while" -> loop();
            case "{" -> comentarioChaves();
            case "/*" -> comentarioBloco();
            default -> {
                if (tokenAtual.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                    atribuicao();
                } else {
                    System.err.println("Erro sintático: comando inválido '" + tokenAtual + "'");
                    erroSintatico = true;
                    avancar();
                }
            }
        }
    }

    private void escreve() {
        String comando = tokenAtual;
        avancar(); // consome write/writeln
        
        if (!consumir(",")) {
            return;
        }
        
        // Processa argumentos
        do {
            if (verificarConstante() || tokenAtual.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                avancar();
            } else {
                expressao();
            }
            
            if (!verificar(",")) break;
            avancar(); // consome próxima vírgula
        } while (true);
        
        if (!verificar(";")) {
            System.err.println("Erro sintático: esperado ';' após " + comando);
            erroSintatico = true;
            while (tokenAtual != null && !verificar(";")) {
                avancar();
            }
            if (tokenAtual != null) avancar();
        } else {
            avancar(); // consome ';'
        }
    }

    private void ler() {
        String comando = tokenAtual;
        avancar(); // consome read/readln
        
        if (!consumir(",")) {
            return;
        }
        
        if (tokenAtual == null || !tokenAtual.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            System.err.println("Erro sintático: identificador esperado após " + comando);
            erroSintatico = true;
            return;
        }
        avancar();
        
        if (!verificar(";")) {
            System.err.println("Erro sintático: esperado ';' após " + comando);
            erroSintatico = true;
            while (tokenAtual != null && !verificar(";")) {
                avancar();
            }
            if (tokenAtual != null) avancar();
        } else {
            avancar(); // consome ';'
        }
    }

   private void atribuicao() {
    String id = tokenAtual;
    String tipoVar = tabelaSimbolos.get(id);

    if (tipoVar == null) {
        System.err.println("Erro: variável '" + id + "' não declarada");
        erroSintatico = true;
    }

    avancar(); // consome ID

    if (!consumir("=")) {
        return;
    }

    // Use expressaoLogica para permitir operadores relacionais
    expressaoLogica();

    if (!verificar(";")) {
        System.err.println("Erro sintático: esperado ';' após atribuição");
        erroSintatico = true;
        while (tokenAtual != null && !verificar(";")) {
            avancar();
        }
        if (tokenAtual != null) avancar();
    } else {
        avancar(); // consome ';'
    }
}

    private void condicional() {
        avancar(); // consome if
        
        boolean temParenteses = verificar("(");
        if (temParenteses) {
            avancar();
        }
        
        expressaoLogica();
        
        if (temParenteses && !consumir(")")) {
            return;
        }
        
        if (verificar("begin")) {
            avancar();
            comandos();
            if (!consumir("end")) {
                return;
            }
            
            if (verificar("else")) {
                avancar();
                if (verificar("begin")) {
                    avancar();
                    comandos();
                    if (!consumir("end")) {
                    }
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
        
        if (temParenteses && !consumir(")")) {
            return;
        }
        
        if (verificar("begin")) {
            avancar();
            comandos();
            if (!consumir("end")) {
            }
        } else {
            comando();
        }
    }

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
            System.err.println("Erro sintático: termo esperado, encontrado fim de arquivo");
            erroSintatico = true;
            return;
        }
        
        if (tokenAtual.matches("[a-zA-Z_][a-zA-Z0-9_]*") || 
            tokenAtual.matches("[0-9]+") || 
            tokenAtual.matches("\".*\"") || 
            verificar("true") || verificar("false")) {
            avancar();
        } else if (verificar("(")) {
            avancar();
            expressao();
            if (!consumir(")")) {
            }
        } else {
            System.err.println("Erro sintático: termo inválido '" + tokenAtual + "'");
            erroSintatico = true;
            avancar();
        }
    }

    private void comentarioChaves() {
        avancar(); // consome '{'
        while (tokenAtual != null && !verificar("}")) {
            avancar();
        }
        if (!consumir("}")) {
            System.err.println("Erro: comentário não fechado com '}'");
            erroSintatico = true;
        }
    }

    private void comentarioBloco() {
        avancar(); // consome '/*'
        while (tokenAtual != null && !verificar("*/")) {
            avancar();
        }
        if (!consumir("*/")) {
            System.err.println("Erro: comentário não fechado com '*/'");
            erroSintatico = true;
        }
    }

    public void setTabelaSimbolos(Map<String, String> tabelaSimbolos) {
        this.tabelaSimbolos = tabelaSimbolos;
    }


}