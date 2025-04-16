import java.util.List;

public class AnalisadorSintatico {
    private List<String> tokens;
    private int posicaoAtual;
    private int anInt;

    public AnalisadorSintatico(List<String> tokens) {
        this.tokens = tokens;
        this.posicaoAtual = 0;
    }

    public void analisar() {
        // Pula comentários iniciais
        while (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("{")) {
            pularComentario();
            pularComentario();
        }

        verificarDeclaracoes();

        if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("begin")) {
            posicaoAtual++;
            verificarBloco();

            if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("end")) {
                posicaoAtual++;
                System.out.println("Análise sintática concluída com sucesso!");
            } else {
                System.out.println("Erro sintático: Esperado 'end' no final do programa");
            }
        } else {
            System.out.println("Erro sintático: Código deve começar com 'begin'");
        }
    }


    private void pularCabecalho() {
        while (posicaoAtual < tokens.size() && !tokens.get(posicaoAtual).equals("*/")) {
            posicaoAtual++;
        }
        if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("*/")) {
            posicaoAtual++;
        }
    }




    private void verificarDeclaracoes() {
        while (posicaoAtual < tokens.size() && !tokens.get(posicaoAtual).equals("begin")) {
            String tokenAtual = tokens.get(posicaoAtual);

            // Tratamento de comentários (se necessário)
            if (tokenAtual.equals("{") || tokenAtual.equals("/*")) {
                pularComentario();
                continue;
            }

            // Verificação de declarações
            if (tokenAtual.equals("final")) {
                verificarDeclaracaoConstante();
            }
            else if (isTipoVariavel(tokenAtual)) {
                verificarDeclaracaoVariavel();
            }
            else if (!tokenAtual.equals("begin")) {
                System.out.println("Erro sintático: Token inesperado '" + tokenAtual +
                        "' nas declarações. Esperado tipo ou 'begin'");
                posicaoAtual++;
            }
        }
    }

    // Método auxiliar para verificar tipos de variáveis
    private boolean isTipoVariavel(String token) {
        return token.equals("int") || token.equals("byte") ||
                token.equals("string") || token.equals("boolean");
    }

    // Método para pular comentários
    private void pularComentario() {
        String tokenAtual = tokens.get(posicaoAtual);

        if (tokenAtual.equals("{")) {
            // Comentário de bloco com {}
            while (posicaoAtual < tokens.size() && !tokens.get(posicaoAtual).equals("}")) {
                posicaoAtual++;
            }
            if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("}")) {
                posicaoAtual++; // Consome o }
            }
        }
        else if (tokenAtual.equals("/*")) {
            // Comentário de bloco com /*
            while (posicaoAtual < tokens.size() && !tokens.get(posicaoAtual).equals("*/")) {
                posicaoAtual++;

                if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("*/")) {
                    posicaoAtual++; // Consome o */
                }
            }

        }
    }

    private void verificarDeclaracaoConstante() {
        posicaoAtual++; // Consome 'final'

        if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).matches("[A-Z][A-Z0-9_]*")) {
            String nomeConst = tokens.get(posicaoAtual);
            posicaoAtual++; // Consome nome

            if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("=")) {
                posicaoAtual++; // Consome '='

                if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).matches("\\d+")) {
                    posicaoAtual++; // Consome valor

                    if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals(";")) {
                        posicaoAtual++; // Consome ';'
                    } else {
                        System.out.println("Erro sintático: Esperado ';' após declaração de constante");
                    }
                } else {
                    System.out.println("Erro sintático: Valor inválido para constante " + nomeConst);
                }
            } else {
                System.out.println("Erro sintático: Esperado '=' após " + nomeConst);
            }
        } else {
            System.out.println("Erro sintático: Nome de constante inválido após 'final'");
        }
    }

    private void verificarDeclaracaoVariavel() {
        String tipo = tokens.get(posicaoAtual);
        posicaoAtual++; // Consome tipo

        if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            posicaoAtual++; // Consome nome

            if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals(";")) {
                posicaoAtual++; // Consome ';'
            } else {
                System.out.println("Erro sintático: Esperado ';' após declaração de " + tipo);
            }
        } else {
            System.out.println("Erro sintático: Nome de variável inválido após " + tipo);
        }
    }

    private void verificarBloco() {
        while (posicaoAtual < tokens.size() && !tokens.get(posicaoAtual).equals("end")) {
            String token = tokens.get(posicaoAtual);

            if (token.equals("{")) {
                pularComentario();
            } else if (token.equals("if")) {
                verificarIf();
            } else if (token.equals("while")) {
                verificarWhile();
            } else if (token.equals("begin")) {
                posicaoAtual++;
                verificarBloco();

                if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("end")) {
                    posicaoAtual++;
                } else {
                    System.out.println("Erro sintático: Falta 'end' para bloco aninhado");
                }
            } else if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                verificarAtribuicao();
            } else if (token.equals("write") || token.equals("writeln") || token.equals("readln")) {
                verificarComandoES();
            } else {
                posicaoAtual++;
            }
        }
    }

    private void verificarComandoES() {
        String comando = tokens.get(posicaoAtual);
        posicaoAtual++; // Consome comando

        if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals(",")) {
            posicaoAtual++; // Consome ','

            // Verifica argumentos
            while (posicaoAtual < tokens.size() && !tokens.get(posicaoAtual).equals(";")) {
                posicaoAtual++;
            }

            if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals(";")) {
                posicaoAtual++; // Consome ';'
            } else {
                System.out.println("Erro sintático: Esperado ';' após comando " + comando);
            }
        } else {
            System.out.println("Erro sintático: Esperado ',' após " + comando);
        }
    }

    private void verificarIf() {
        posicaoAtual++; // Consome 'if'

        if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("(")) {
            posicaoAtual++;
            verificarExpressao();

            if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals(")")) {
                posicaoAtual++;

                if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("begin")) {
                    posicaoAtual++;
                    verificarBloco();

                    if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("end")) {
                        posicaoAtual++;

                        if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("else")) {
                            posicaoAtual++;

                            if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("begin")) {
                                posicaoAtual++;
                                verificarBloco();

                                if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("end")) {
                                    posicaoAtual++;
                                } else {
                                    System.out.println("Erro sintático: Falta 'end' no bloco else");
                                }
                            } else {
                                System.out.println("Erro sintático: Esperado 'begin' após 'else'");
                            }
                        }
                    } else {
                        System.out.println("Erro sintático: Falta 'end' no bloco if");
                    }
                } else {
                    System.out.println("Erro sintático: Esperado 'begin' após condição if");
                }
            } else {
                System.out.println("Erro sintático: Falta ')' após condição if");
            }
        } else {
            System.out.println("Erro sintático: Esperado '(' após 'if'");
        }
    }

    private void verificarWhile() {
        posicaoAtual++; // Consome 'while'

        if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("(") ||
                posicaoAtual < tokens.size() && !tokens.get(posicaoAtual).equals("(")) {
            posicaoAtual++;
            verificarExpressao();

            if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals(")") ||
                    posicaoAtual < tokens.size() && !tokens.get(posicaoAtual).equals(")")) {
                posicaoAtual++;

                if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("begin")) {
                    posicaoAtual++;
                    verificarBloco();

                    if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("end")) {
                        posicaoAtual++;
                    } else {
                        System.out.println("Erro sintático: Falta 'end' no bloco while");
                    }
                } else {
                    System.out.println("Erro sintático: Esperado 'begin' após condição while");
                }
            } else {
                //System.out.println("Erro sintático: Falta ')' após condição while");
            }
        } else {
           // System.out.println("Erro sintático: Esperado '(' após 'while'");
        }
    }

    private void verificarAtribuicao() {
        String nomeVar = tokens.get(posicaoAtual);
        posicaoAtual++;

        if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals("=")) {
            posicaoAtual++;
            verificarExpressao();

            if (posicaoAtual < tokens.size() && tokens.get(posicaoAtual).equals(";")) {
                posicaoAtual++;
            } else {
                System.out.println("Erro sintático: Esperado ';' após atribuição");
            }
        } else {
        }
    }

    private void verificarExpressao() {
        // Implementação simplificada - avança até encontrar delimitador
        while (posicaoAtual < tokens.size() &&
                !tokens.get(posicaoAtual).equals(")") &&
                !tokens.get(posicaoAtual).equals(";") &&
                !tokens.get(posicaoAtual).equals(",")) {
            posicaoAtual++;
        }
    }

    private void verificarCondicao() {
        // Verifica a abertura do parêntese
        if (posicaoAtual >= tokens.size() || !tokens.get(posicaoAtual).equals("(")) {
            System.out.println("Erro sintático: Esperado '(' no início da condição");
            return;
        }
        posicaoAtual++; // Consome o '('

        // Verifica o primeiro operando (deve ser um identificador ou constante)
        if (posicaoAtual >= tokens.size() ||
                (!tokens.get(posicaoAtual).matches("[a-zA-Z_][a-zA-Z0-9_]*") &&
                        !tokens.get(posicaoAtual).matches("\\d+"))) {
            System.out.println("Erro sintático: Operando esperado após '('");
            return;
        }
        posicaoAtual++;

        // Verifica o operador relacional
        if (posicaoAtual >= tokens.size() ||
                !(tokens.get(posicaoAtual).equals(">")  ||
                        tokens.get(posicaoAtual).equals("<")  ||
                        tokens.get(posicaoAtual).equals(">=") ||
                        tokens.get(posicaoAtual).equals("<=") ||
                        tokens.get(posicaoAtual).equals("==") ||
                        tokens.get(posicaoAtual).equals("!="))) {
            System.out.println("Erro sintático: Operador relacional inválido");
            return;
        }
        posicaoAtual++;

        // Verifica o segundo operando
        if (posicaoAtual >= tokens.size() ||
                (!tokens.get(posicaoAtual).matches("[a-zA-Z_][a-zA-Z0-9_]*") &&
                        !tokens.get(posicaoAtual).matches("\\d+"))) {
            System.out.println("Erro sintático: Operando esperado após operador");
            return;
        }
        posicaoAtual++;

        // Verifica o fechamento do parêntese
        if (posicaoAtual >= tokens.size() || !tokens.get(posicaoAtual).equals(")")) {
            System.out.println("Erro sintático: Esperado ')' no final da condição");
            return;
        }
        posicaoAtual++; // Consome o ')'
    }}


/*
private void verificarX() {
    // 1. Verificar token atual
    if (!tokenEsperado) {
        System.out.println("Erro sintático...");
        return;
    }

    // 2. Consumir token (avançar posição)
    posicaoAtual++;

    // 3. Verificar próximos elementos
    verificarProximoElemento();
}
*/


