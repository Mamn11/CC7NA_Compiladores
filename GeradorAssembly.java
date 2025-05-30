import java.util.List;
import java.util.Map;

public class GeradorAssembly {
    private final Map<String, String> tabelaSimbolos;
    private final List<String> tokens;

    public GeradorAssembly(Map<String, String> tabelaSimbolos, List<String> tokens) {
        this.tabelaSimbolos = tabelaSimbolos;
        this.tokens = tokens;
    }

    public String gerar() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");

        // Declaração de variáveis
        for (String var : tabelaSimbolos.keySet()) {
            String tipo = tabelaSimbolos.get(var);
            if (tipo.equals("INT")) {
                sb.append("    ").append(var).append(" DW 0\n");
            } else if (tipo.equals("STRING")) {
                sb.append("    ").append(var).append(" DB 20 DUP(?)\n");
            } else if (tipo.equals("BOOLEAN")) {
                sb.append("    ").append(var).append(" DB 0\n");
            }
        }
        sb.append("\n.code\nmain PROC\n");

        // Atribuições simples (exemplo)
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            // Exemplo: n = 0 ;
            if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                String var = token;
                if (i + 2 < tokens.size() && tokens.get(i + 1).equals("=") && tokens.get(i + 3).equals(";")) {
                    String valor = tokens.get(i + 2);
                    // Converte booleanos para 0/1
                    if (valor.equals("true")) valor = "1";
                    if (valor.equals("false")) valor = "0";
                    sb.append("    mov ").append(var).append(", ").append(valor).append("\n");
                }
            }
        }

        sb.append("    ; ...demais comandos...\n");
        sb.append("    ret\nmain ENDP\nend main\n");
        return sb.toString();
    }
}
