# CC7NA_Compiladores

## Descrição

Este projeto é um compilador para a linguagem LC, que traduz programas escritos em LC para Assembly (80x86). O compilador realiza análise léxica, sintática e gera um arquivo `.asm` pronto para ser montado com o MASM.

## Estrutura do Projeto

- `Main.java` — Classe principal, ponto de entrada do compilador.
- `AnalisadorLexico.java` — Responsável pela análise léxica.
- `AnalisadorSintatico.java` — Responsável pela análise sintática e semântica.
- `GeradorAssembly.java` — Responsável pela geração do código Assembly.
- `Simbolo.java` — Classe de apoio para a tabela de símbolos.
- `codigos-fonte/` — Pasta com exemplos de código-fonte LC.
- `saida-codigo/` — Pasta sugerida para saída dos arquivos `.asm`.

## Como compilar e rodar

1. **Abra o terminal na pasta do projeto**  
   Exemplo:
   ```
   cd C:\Users\geova\OneDrive\Documentos\Estudos\Compiladores\CC7NA_Compiladores
   ```

2. **Compile todos os arquivos Java**
   ```
   javac *.java
   ```

3. **Execute o compilador**
   ```
   java Main codigos-fonte\codigo-fonte.lc saida-codigo\saida-codigo.asm
   ```
   - O primeiro argumento é o caminho do arquivo fonte LC.
   - O segundo argumento é o caminho do arquivo de saída Assembly.

4. **Verifique a saída**
   - O arquivo Assembly será gerado no local especificado (ex: `saida-codigo\saida-codigo.asm`).

5. **Monte o arquivo Assembly (opcional)**
   - Use o MASM para montar o arquivo `.asm` e gerar o executável.

## Observações

- Não utilize `<` e `>` nos argumentos ao rodar o programa.
- Certifique-se de que todos os arquivos `.java` estejam compilados e que você esteja no diretório correto.
- O projeto não depende de bibliotecas externas além do Java padrão.

## Exemplo de uso

```sh
javac *.java
java Main codigos-fonte\codigo-fonte.lc saida-codigo\saida-codigo.asm
```

---
