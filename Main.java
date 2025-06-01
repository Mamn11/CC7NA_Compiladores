import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java LC <arquivo_fonte.lc> <arquivo_saida.asm>");
            System.exit(1);
        }
        String caminhoArquivo = args[0];
        String nomeSaida = args[1];
        
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
                
                Files.write(Paths.get(nomeSaida), codigoAssembly.getBytes());
                System.out.println("Arquivo Assembly gerado: " + nomeSaida);
            }

        } catch (Exception e) {
            System.err.println("\n=== ERRO ===");
            System.exit(1);
        }
    }
}