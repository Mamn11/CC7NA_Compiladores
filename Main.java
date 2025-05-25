import java.util.List;

public class Main {
    public static void main(String[] args) {
        String caminhoArquivo = "C:/Users/geova/OneDrive/Documentos/Estudos/Compiladores/CC7NA_Compiladores/Codigo Fonte/codigo-fonte.lc";
        
        try {
            System.out.println("=== INÍCIO DA ANÁLISE ===");
            
            // Análise Léxica
            System.out.println("\n[1/3] Executando análise léxica...");
            List<String> tokens = AnalisadorLexico.analisar(caminhoArquivo);
            AnalisadorLexico.exibirTabela();
            
            // Análise Sintática
            System.out.println("\n[2/3] Executando análise sintática...");
            AnalisadorSintatico analisador = new AnalisadorSintatico(tokens);
            boolean sucessoSintatico = analisador.analisar();
            
            if (!sucessoSintatico) {
                System.out.println("\n=== ANÁLISE CONCLUÍDA COM ERROS SINTÁTICOS ===");
                System.exit(1);
            }
            
            // Análise Semântica
            System.out.println("\n[3/3] Executando análise semântica...");
            AnalisadorSemantico semantico = new AnalisadorSemantico(analisador.getTabelaSimbolos(), tokens);
            boolean sucessoSemantico = semantico.analisar();
            if (sucessoSemantico) {
                System.out.println("\n=== ANÁLISE CONCLUÍDA COM SUCESSO ===\n\n===        PROGRAMA VÁLIDO        ===\n\n");
            } else {     
                System.out.println("\n=== ANÁLISE CONCLUÍDA COM ERROS SEMÂNTICOS ===");
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("\n=== ERRO ===");
            System.exit(1);
        }
    }
}