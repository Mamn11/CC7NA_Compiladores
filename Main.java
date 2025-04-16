import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String caminhoArquivo = "C:/Users/mamn2/OneDrive/Documentos/Compiladores/codigo-fonte2.lc";

        // 1. Análise Léxica
        List<String> tokens = AnalisadorLexico.analisar(caminhoArquivo);
        AnalisadorLexico.exibirTabela();

        // 2. Análise Sintática
        AnalisadorSintatico analisador = new AnalisadorSintatico(tokens);
        analisador.analisar();

        // 3. Exibir tokens encontrados (para depuração)
        System.out.println("\nTokens na ordem de aparecimento:");
        System.out.println(tokens);
    }
}