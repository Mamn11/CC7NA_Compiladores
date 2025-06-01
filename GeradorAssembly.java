import java.util.List;
import java.util.Map;

public class GeradorAssembly {
    private static final String STD_OUT = "invoke StdOut, addr ";
    private static final String STD_IN = "invoke StdIn, addr ";
    private static final String MOV_EAX = "mov eax, ";
    private static final String CMP_EAX = "cmp eax, ";
    
    private final List<String> tokens;
    private final Map<String, String> tabelaSimbolos;
    private final StringBuilder codigoAssembly = new StringBuilder();
    private int pos = 0;
    private String tokenAtual;
    private int labelCount = 0;

    public GeradorAssembly(List<String> tokens, Map<String, String> tabelaSimbolos) {
        this.tokens = tokens;
        this.tabelaSimbolos = tabelaSimbolos;
        avancar();
    }

    private void avancar() {
        tokenAtual = (pos < tokens.size()) ? tokens.get(pos++) : null;
    }

    public String gerar() {
        // Cabeçalho padrão
        codigoAssembly.append(".686\n");
        codigoAssembly.append(".model flat, stdcall\n");
        codigoAssembly.append("option casemap :none\n\n");
        codigoAssembly.append("include \\masm32\\include\\windows.inc\n");
        codigoAssembly.append("include \\masm32\\include\\kernel32.inc\n");
        codigoAssembly.append("include \\masm32\\include\\masm32.inc\n");
        codigoAssembly.append("include \\masm32\\include\\msvcrt.inc\n");
        codigoAssembly.append("includelib \\masm32\\lib\\kernel32.lib\n");
        codigoAssembly.append("includelib \\masm32\\lib\\masm32.lib\n");
        codigoAssembly.append("includelib \\masm32\\lib\\msvcrt.lib\n");
        codigoAssembly.append("include \\masm32\\macros\\macros.asm\n\n");
        gerarDeclaracoes();
        gerarCodigo();
        return codigoAssembly.toString();
    }


    private void gerarDeclaracoes() {
        codigoAssembly.append(".data\n");
        codigoAssembly.append("    buffer db 20 dup(?)\n"); // Buffer para leitura de números

        while (tokenAtual != null && !tokenAtual.equals("begin")) {
            switch (tokenAtual) {
                case "int":
                    declararVariavel("dd ?"); // double word
                    break;
                case "string":
                    declararVariavel("db 100 dup(?)");
                    break;
                case "boolean":
                    declararVariavel("db ?"); // byte
                    break;
                case "final":
                    declararConstante();
                    break;
                default:
                    avancar();
            }
        }

        // Mensagens padrão com prefixo str
        codigoAssembly.append("    strNovaLinha db 0Dh,0Ah,0\n");
        codigoAssembly.append("    strTrue db \"true\",0\n");
        codigoAssembly.append("    strFalse db \"false\",0\n\n");
    }

    private void declararVariavel(String tipo) {
        avancar(); // tipo
        String nome = tokenAtual;
        avancar();
        codigoAssembly.append("    ").append(nome).append(" ").append(tipo).append("\n");
        if (tokenAtual != null && tokenAtual.equals(";")) avancar();
    }

    private void declararConstante() {
        avancar(); // final
        String nome = tokenAtual;
        avancar();
        if ("=".equals(tokenAtual)) {
            avancar();
            String valor = tokenAtual;
            avancar();
            codigoAssembly.append("    ").append(nome).append(" EQU ").append(valor).append("\n");
        }
        if (tokenAtual != null && tokenAtual.equals(";")) avancar();
    }

    private void gerarCodigo() {
        codigoAssembly.append("\n.code\n");
        codigoAssembly.append("include \\masm32\\include\\masm32rt.inc\n");
        codigoAssembly.append("start:\n");
        
        while (tokenAtual != null) {
            if ("begin".equals(tokenAtual)) {
                avancar();
                gerarBloco();
            } else {
                avancar();
            }
        }
        
        codigoAssembly.append("\n    invoke ExitProcess, 0\n");
        codigoAssembly.append("end start\n");
    }

    private void gerarBloco() {
        while (tokenAtual != null && !"end".equals(tokenAtual)) {
            switch (tokenAtual) {
                case "write":
                case "writeln":
                    gerarWrite();
                    break;
                case "readln":
                    gerarReadln();
                    break;
                case "while":
                    gerarWhile();
                    break;
                case "if":
                    gerarIf();
                    break;
                default:
                    if (isIdentificador(tokenAtual)) {
                        gerarAtribuicao();
                    } else {
                        avancar();
                    }
            }
        }
        if ("end".equals(tokenAtual)) avancar();
    }

    private void gerarWrite() {
        boolean novaLinha = "writeln".equals(tokenAtual);
        avancar();

        if (",".equals(tokenAtual)) avancar();

        String texto = tokenAtual;
        avancar();

        if (texto.startsWith("\"")) {
            String labelMsg = "str" + (labelCount++);
            String msg = texto.substring(1, texto.length() - 1);
            codigoAssembly.insert(codigoAssembly.indexOf(".code"),
                "    " + labelMsg + " db \"" + msg + "\",0\n");
            codigoAssembly.append("    invoke crt_printf, addr ").append(labelMsg).append("\n");
        } else {
            if (tabelaSimbolos.get(texto).equals("boolean")) {
                String labelEnd = "labelEnd" + (labelCount++);
                codigoAssembly.append("    mov al, ").append(texto).append("\n");
                codigoAssembly.append("    cmp al, 1\n");
                codigoAssembly.append("    je @F\n");
                codigoAssembly.append("    invoke crt_printf, addr strFalse\n");
                codigoAssembly.append("    jmp ").append(labelEnd).append("\n");
                codigoAssembly.append("@@:\n");
                codigoAssembly.append("    invoke crt_printf, addr strTrue\n");
                codigoAssembly.append(labelEnd).append(":\n");
            } else {
                codigoAssembly.append("    invoke crt_printf, addr ").append(texto).append("\n");
            }
        }

        if (novaLinha) {
            codigoAssembly.append("    ").append(STD_OUT).append("strNovaLinha\n");
        }

        if (tokenAtual != null && tokenAtual.equals(";")) avancar();
    }

    private void gerarReadln() {
        avancar(); // readln
        if (",".equals(tokenAtual)) avancar();
        
        String var = tokenAtual;
        avancar();
        
        if (tabelaSimbolos.get(var).equals("string")) {
            codigoAssembly.append("    invoke crt_gets, addr ").append(var).append("\n");
        } else {
            codigoAssembly.append("    invoke StdIn, addr buffer, 20\n");
            codigoAssembly.append("    invoke atodw, addr buffer\n");
            codigoAssembly.append("    mov ").append(var).append(", eax\n");
        }
        
        if (tokenAtual != null && tokenAtual.equals(";")) avancar();
    }

    private void gerarAtribuicao() {
        String var = tokenAtual;
        avancar();
        
        if ("=".equals(tokenAtual)) {
            avancar();
            String valor = tokenAtual;
            avancar();
            
            if (isOperador(tokenAtual)) {
                String operador = tokenAtual;
                avancar();
                String valor2 = tokenAtual;
                avancar();
                gerarOperacao(var, valor, operador, valor2);
            } else {
                gerarAtribuicaoSimples(var, valor);
            }
        }
        
        if (tokenAtual != null && tokenAtual.equals(";")) avancar();
    }

    private void gerarOperacao(String var, String valor1, String operador, String valor2) {
        codigoAssembly.append("    ").append(MOV_EAX).append(valor1).append("\n");
        
        switch (operador) {
            case "+":
                codigoAssembly.append("    add eax, ").append(valor2).append("\n");
                break;
            case "-":
                codigoAssembly.append("    sub eax, ").append(valor2).append("\n");
                break;
            case "*":
                codigoAssembly.append("    imul eax, ").append(valor2).append("\n");
                break;
            case "/":
                codigoAssembly.append("    cdq\n");
                codigoAssembly.append("    idiv ").append(valor2).append("\n");
                break;
        }
        
        codigoAssembly.append("    mov ").append(var).append(", eax\n");
    }

    private void gerarAtribuicaoSimples(String var, String valor) {
        String tipo = tabelaSimbolos.get(var);
        
        if ("boolean".equals(tipo)) {
            codigoAssembly.append("    mov ").append(var).append(", ")
                .append("true".equals(valor) ? "1" : "0").append("\n");
        } else {
            codigoAssembly.append("    mov ").append(var).append(", ").append(valor).append("\n");
        }
    }

    private void gerarWhile() {
        avancar(); // while
        String labelInicio = "L_while_" + (labelCount++);
        String labelFim = "L_end_while_" + (labelCount++);
        
        codigoAssembly.append("\n").append(labelInicio).append(":\n");
        gerarCondicao(labelFim);
        
        if ("begin".equals(tokenAtual)) {
            avancar();
            gerarBloco();
        }
        
        codigoAssembly.append("    jmp ").append(labelInicio).append("\n");
        codigoAssembly.append(labelFim).append(":\n");
    }

    private void gerarIf() {
        avancar(); // if
        String labelFim = "L_if_end_" + (labelCount++);
        
        gerarCondicao(labelFim);
        gerarAtribuicao();
        
        codigoAssembly.append(labelFim).append(":\n");
    }

    private void gerarCondicao(String labelFim) {
        String var1 = tokenAtual;
        avancar();
        
        if (isOperadorComparacao(tokenAtual)) {
            String operador = tokenAtual;
            avancar();
            String var2 = tokenAtual;
            avancar();
            
            codigoAssembly.append("    ").append(MOV_EAX).append(var1).append("\n");
            codigoAssembly.append("    ").append(CMP_EAX).append(var2).append("\n");
            
            switch (operador) {
                case ">=": codigoAssembly.append("    jl "); break;
                case "<=": codigoAssembly.append("    jg "); break;
                case ">": codigoAssembly.append("    jle "); break;
                case "<": codigoAssembly.append("    jge "); break;
                case "==": codigoAssembly.append("    jne "); break;
                case "!=": codigoAssembly.append("    je "); break;
            }
            
            codigoAssembly.append(labelFim).append("\n");
        } else {
            // Condição simples (apenas variável)
            codigoAssembly.append("    cmp ").append(var1).append(", 0\n");
            codigoAssembly.append("    je ").append(labelFim).append("\n");
        }
    }

    private boolean isIdentificador(String token) {
        return tabelaSimbolos.containsKey(token);
    }

    private boolean isOperador(String token) {
        return token != null && ("+".equals(token) || "-".equals(token) || 
               "*".equals(token) || "/".equals(token));
    }

    private boolean isOperadorComparacao(String token) {
        return token != null && (">=".equals(token) || "<=".equals(token) || 
               ">".equals(token) || "<".equals(token) || 
               "==".equals(token) || "!=".equals(token));
    }
}