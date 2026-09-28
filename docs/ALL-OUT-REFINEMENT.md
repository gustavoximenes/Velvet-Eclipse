# All-Out Attack — 0.6.33

A animação aprovada nas prévias substitui a apresentação anterior. Quem iniciou o ataque aparece em close à direita, e os aliados ficam à esquerda, em profundidades diferentes. O braço livre dos aliados fica baixo e as camadas de profundidade mantêm seus braços atrás do rosto do iniciador. Cada participante mantém a própria skin, suas camadas visíveis, o modelo standard/slim e sua família de arma.

A equipe corre para o centro, surgem fumaça texturizada, impactos breves e fragmentos, e a cena volta ao combate quando há sobreviventes. O dano continua sendo aplicado uma vez no tick 84. O servidor confirma a vitória **depois** de aplicar o dano; Endure, Enduring Soul e reduções de dano não causam uma finalização prematura. Somente o resultado `PLAYERS_WON` libera a saída e a pose final do estilo escolhido. Enquanto aguarda a confirmação, o cliente mantém a cena anterior à finalização.

Em **TAB → SYSTEM → Animação All-Out**, um único seletor reúne Makoto, Yukari, Junpei, Akihiko e Mitsuru. Clique no campo ou pressione Enter para abrir; use mouse, roda ou setas para escolher e Enter/clique para confirmar. Esc ou um clique fora cancela a escolha. O estilo continua salvo no jogador e a prévia usa sua skin.

Removidos os textos superiores de identificação da cena e do mod no All-Out e no despertar. Legendas, escolha da primeira Persona e frases de finalização continuam presentes.

Cliente e servidor precisam usar a versão 0.6.33, protocolo **19**, que inclui a confirmação do resultado do All-Out. LDLib, Photon e a adaptação de compatibilidade com OptiFine permanecem iguais à versão anterior.

## Validação

- Compilação Java 17 e verificações offline de táticas e apresentação.
- All-Out com um a cinco alvos, vitória e sobreviventes; reserva e aplicação única do dano; casos com Endure, Enduring Soul e Firm Stance.
- Pacotes de início e de confirmação do resultado, estilos salvos, modelos standard/slim e geometria de poses.
- Inspeção visual offline da abertura integrada e do seletor; atlas da fumaça incluído no JAR.
- Sem teste diretamente no Minecraft, conforme a preferência registrada no projeto.

A implementação usa o mesmo desenho de cena da prévia aprovada. O renderizador nativo aplica a textura de fumaça e as skins em tempo real; GIFs e skins ilustrativas das prévias não são incluídos como conteúdo do jogo.
