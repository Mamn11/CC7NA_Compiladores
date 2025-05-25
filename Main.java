import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Análise léxica
        List<String> tokens = AnalisadorLexico.analisar("C:/Users/geova/OneDrive/Documentos/Estudos/Compiladores/CC7NA_Compiladores/Codigo Fonte/codigo-fonte.lc");
        AnalisadorLexico.exibirTabela();
        
        // 2. Análise sintática
        AnalisadorSintatico analisador = new AnalisadorSintatico(tokens);
        boolean sucesso = analisador.analisar();
        
        if (sucesso) {
            System.out.println("Análise sintática concluída com sucesso!");
        } else {
            System.out.println("Erros encontrados na análise sintática");
        }
    }
}