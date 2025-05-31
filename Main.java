import java.util.List;
import java.util.Map;

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
            System.out.println("\n[2/3] Executando análises...");
            AnalisadorSintatico analisador = new AnalisadorSintatico(tokens);
            boolean sucessoSintatico = analisador.analisar();
            
            if (!sucessoSintatico) {
                System.out.println("\n=== ANÁLISE CONCLUÍDA COM ERROS  ===");
                System.exit(1);
            } else {
                System.out.println("\n=== ANÁLISE SINTÁTICA CONCLUÍDA COM SUCESSO ===");
                System.out.println("\n===              FIM DA ANÁLISE             ===\n");

                // Gerar Assembly
                // Supondo que você tenha um método para obter a tabela de símbolos:
                Map<String, String> tabelaSimbolos = analisador.getTabelaSimbolos(); // Ajuste o tipo conforme necessário
                GeradorAssembly gerador = new GeradorAssembly(tokens, tabelaSimbolos);
                String codigoAssembly = gerador.gerar();

                System.out.println("\n[3/3] Gerando código Assembly...");
                System.out.println("\n=== CÓDIGO ASSEMBLY ===\n");
                System.out.println(codigoAssembly);
            }

        } catch (Exception e) {
            System.err.println("\n=== ERRO ===");
            System.exit(1);
        }
    }
}