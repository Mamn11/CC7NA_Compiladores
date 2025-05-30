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
            
            // Análise Sintática e Semântica 
            System.out.println("\n[2/2] Executando análises...");
            AnalisadorSintatico analisador = new AnalisadorSintatico(tokens);
            boolean sucessoSintatico = analisador.analisar();
            
            if (!sucessoSintatico) {
                System.out.println("\n=== ANÁLISE CONCLUÍDA COM ERROS  ===");
                System.exit(1);
            }else {
                System.out.println("\n=== ANÁLISE SINTÁTICA CONCLUÍDA COM SUCESSO ===");
                
                GeradorAssembly gerador = new GeradorAssembly(analisador.getTabelaSimbolos(), tokens);
                String codigoAssembly = gerador.gerar();
                System.out.println("\n=== Assembly Gerado ===\n" + codigoAssembly);
                
                System.out.println("\n===              FIM DA ANÁLISE             ===\n");
            }

        } catch (Exception e) {
            System.err.println("\n=== ERRO ===");
            System.exit(1);
        }
    }
}