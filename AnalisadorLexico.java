import java.io.*;
import java.util.*;

public class AnalisadorLexico {
    private static final Map<String, Simbolo> tabelaSimbolos = new HashMap<>();
    private static int contadorGlobalTokens = 1;

    private static final List<String> palavrasReservadas = Arrays.asList(
            "final", "int", "byte", "string", "while", "if", "else",
            "and", "or", "not", "==", "=", "(", ")", "<", ">", ">=",
            "<=", ",", "+", "-", "*", "/", ";", "begin", "end",
            "readln", "write", "writeln", "true", "false", "boolean"
    );

    static {
        for (String palavra : palavrasReservadas) {
            tabelaSimbolos.put(palavra, new Simbolo("RESERVADA_" + contadorGlobalTokens++, null));
        }
    }

    public static List<String> analisar(String caminhoArquivo) {
        List<String> tokensEncontrados = new ArrayList<>();

        try (BufferedReader leitor = new BufferedReader(new FileReader(caminhoArquivo))) {
            StringBuilder lexemaAtual = new StringBuilder();
            boolean dentroString = false;
            boolean dentroComentarioBloco = false;
            boolean dentroComentarioChaves = false;
            char delimitadorString = '"';

            int caractere;
            while ((caractere = leitor.read()) != -1) {
                char c = (char) caractere;

                // Tratamento de strings (tem prioridade sobre comentários)
                if (dentroString) {
                    lexemaAtual.append(c);
                    if (c == delimitadorString) {
                        tokensEncontrados.add(lexemaAtual.toString());
                        inserirNaTabela(lexemaAtual.toString(), "STRING");
                        lexemaAtual.setLength(0);
                        dentroString = false;
                    }
                    continue;
                }

                // Tratamento de comentários /* */
                if (dentroComentarioBloco) {
                    if (c == '*' && leitor.ready() && (char) leitor.read() == '/') {
                        dentroComentarioBloco = false;
                    }
                    continue;
                }

                // Tratamento de comentários { }
                if (dentroComentarioChaves) {
                    if (c == '}') {
                        dentroComentarioChaves = false;
                    }
                    continue;
                }

                // Detecção de início de comentários
                if (c == '/' && leitor.ready()) {
                    char prox = (char) leitor.read();
                    if (prox == '*') {
                        dentroComentarioBloco = true;
                        continue;
                    }
                    // Se não for comentário, volta o caractere
                    lexemaAtual.append(c).append(prox);
                    continue;
                }

                if (c == '{') {
                    dentroComentarioChaves = true;
                    continue;
                }

                // Detecção de strings
                if (c == '"' || c == '\'') {
                    dentroString = true;
                    delimitadorString = c;
                    lexemaAtual.append(c);
                    continue;
                }
                if (dentroString) {
                    lexemaAtual.append(c);
                    if (c == delimitadorString) {
                        tokensEncontrados.add(lexemaAtual.toString());
                        inserirNaTabela(lexemaAtual.toString(), "STRING");
                        lexemaAtual.setLength(0);
                        dentroString = false;
                    }
                    continue;
                }

                if (c == '{') {
                    dentroComentarioChaves = true;
                    continue;
                }

                if (c == '"' || c == '\'') {
                    dentroString = true;
                    delimitadorString = c;
                    lexemaAtual.append(c);
                    continue;
                }

                if (Character.isWhitespace(c)) {
                    if (lexemaAtual.length() > 0) {
                        processarLexema(lexemaAtual.toString(), tokensEncontrados);
                        lexemaAtual.setLength(0);
                    }
                    continue;
                }

                if (isDelimitador(c)) {
                    if (lexemaAtual.length() > 0) {
                        processarLexema(lexemaAtual.toString(), tokensEncontrados);
                        lexemaAtual.setLength(0);
                    }
                    tokensEncontrados.add(String.valueOf(c));
                    continue;
                }

                lexemaAtual.append(c);
            }

            if (lexemaAtual.length() > 0) {
                processarLexema(lexemaAtual.toString(), tokensEncontrados);
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }

        return tokensEncontrados;
    }

    private static boolean isDelimitador(char c) {
        return c == '=' || c == '(' || c == ')' || c == '<' || c == '>' ||
                c == ',' || c == '+' || c == '-' || c == '*' || c == '/' ||
                c == ';' || c == '{' || c == '}';
    }

    private static void processarLexema(String lexema, List<String> tokensEncontrados) {
        if (lexema.matches("\\d+(\\.\\d+)?")) {  // Aceita números inteiros e decimais
            inserirNaTabela(lexema, "CONST");
            tokensEncontrados.add(lexema);
        }
        else if (tabelaSimbolos.containsKey(lexema)) {
            tokensEncontrados.add(lexema);
        }
        else if (lexema.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            inserirNaTabela(lexema, "ID");
            tokensEncontrados.add(lexema);
        }
        else {
            System.out.println("Erro léxico: lexema inválido - " + lexema);
        }
    }

    private static void inserirNaTabela(String lexema, String tipo) {
        if (!tabelaSimbolos.containsKey(lexema)) {
            tabelaSimbolos.put(lexema, new Simbolo(tipo + "_" + contadorGlobalTokens++, null));
        }
    }

    public static void exibirTabela() {
        System.out.println("\nTabela de Símbolos");
        System.out.println("=".repeat(60));
        System.out.printf("%-25s | %-20s | %s%n", "Lexema", "Token", "Bytes");
        System.out.println("-".repeat(60));

        tabelaSimbolos.entrySet().stream()
                .sorted((e1, e2) -> {
                    int num1 = Integer.parseInt(e1.getValue().token.split("_")[1]);
                    int num2 = Integer.parseInt(e2.getValue().token.split("_")[1]);
                    return Integer.compare(num1, num2);
                })
                .forEach(entry -> {
                    System.out.printf("%-25s | %-20s | null%n",
                            entry.getKey(), entry.getValue().token);
                });

        System.out.println("=".repeat(60));
    }}

