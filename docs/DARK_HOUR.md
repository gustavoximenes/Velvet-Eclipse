# Dark Hour — 0.6.14

A Dark Hour ocupa os ticks 18000–23999 do Overworld: dia, noite, hora oculta e amanhecer. O relógio de transição começa em 17800, dez segundos antes. A passagem do tempo continua usando o relógio e o gamerule do Minecraft; pausar o ciclo também pausa esse período. Dormir mantém o salto de horário normal do Minecraft.

## Teste no jogo

- `/time set 17800`: acompanhe o relógio branco até a virada verde.
- `/time set dark_hour` (ou `darkhour`): entre diretamente na Dark Hour.
- `/locate tartarus` ou `/locate structure velvet_eclipse:tartarus`: consulte a entrada da torre e suas coordenadas clicáveis. `/velvet tartarus` continua disponível.
- Em mundos locais/LAN, o anfitrião tem prioridade. Em servidor dedicado, configure `/velvet tartarus owner <jogador>` com permissão de administrador. Sem configuração, um operador online tem prioridade sobre os demais jogadores.
- `/time set day`: confira a volta do céu normal, dos mobs e o desaparecimento da torre.
- No menu de fusões, confira o rosto de Igor, o nível próprio do jogador, EXP e ajuda por F1. ESC fecha a ajuda. A barra não usa a experiência vanilla nem o nível da Persona.
- Em dois clientes, confirme que só o dono do turno seleciona alvos e que o marcador selecionado não se duplica ao sobrepor o cursor a outro inimigo.

## Conteúdo desta primeira implementação

Céu e nuvens verdes, lua amarelada, iluminação esverdeada e manchas vermelhas localizadas são desenhados pelo mod. Mobs comuns ficam imóveis, protegidos de dano e representados por caixões. Seus dados, equipamentos e estado anterior de IA são preservados, inclusive ao salvar e carregar o mundo. Jogadores, Shadows, Personas e efeitos de invocação ficam excluídos por sua classe real, sem depender de tags que não são sincronizadas com o cliente.

Shadows de encontros naturais surgem apenas nesse período e desaparecem ao amanhecer. Invocações explícitas de teste e inimigos de batalhas mantêm seus ciclos próprios.

O Tártaro usa blocos próprios, volumes escalonados, espiral externa, torreões, selos luminosos e uma entrada com frontão, colunas e escadaria. Surge por camadas até o último bloco permitido pelo mundo (Y 319 no Overworld padrão). Sua base adapta a altura à superfície, inclusive árvores e relevo irregular, sem exigir uma planície. O centro aparece a no máximo 64 blocos do jogador prioritário, respeitando a menor distância de chunks entre cliente e servidor. Se o anfitrião entrar ou se afastar para fora desse alcance, a torre é recolhida e reaparece próxima dele. Apenas jogadores presentes no Overworld participam da escolha; o dono offline ou em outra dimensão dá lugar aos demais.

Posição, progresso, dono configurado e a lista dos blocos colocados são salvos. Não substitui blocos preexistentes; o amanhecer remove apenas os blocos temporários registrados. Jogadores apoiados na estrutura são movidos para uma posição externa antes da remoção para evitar quedas. O limite do mundo e o espaço vertical disponível continuam sendo respeitados; uma borda muito pequena ou terreno já no teto pode impedir a construção completa.

Esta etapa permite entrar no salão térreo. Andares jogáveis, exploração interna, chefes e progressão do Tártaro ficam para próximas etapas.

## Verificação

Auditorias offline cobrem exclusão de Personas/efeitos/Shadows, prioridade do anfitrião, 11.532 candidatos dentro do alcance, registro de `/locate` sem substituir os argumentos vanilla, persistência/migração, limites do relógio, 25 plantas da torre, escadaria, passagem da entrada, teto do mundo, modelos e texturas. As auditorias anteriores de batalha, fusões, multiplayer e Velvet Room também foram executadas. As prévias são renderizações offline da geometria/HUD, não capturas do jogo. Não houve sessão real do Minecraft ou teste com dois clientes nesta entrega.

Cliente e servidor precisam da mesma versão; canal principal de rede atualizado para protocolo 10. Sem dependência adicional.
