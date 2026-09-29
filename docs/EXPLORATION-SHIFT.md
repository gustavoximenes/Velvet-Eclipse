# 0.6.34 — Exploração, formação de combate e Shift

## Controles e apresentação

- **H:** exploração com câmera mais centralizada, afastamento suave durante a corrida, inclinação do tronco, passada mais marcada e arma baixa. A orientação visual acompanha o deslocamento, sem alterar os comandos de movimento enviados ao servidor.
- **Shift:** durante seu 1 More, toque e solte Shift para escolher um aliado. A passagem mostra quem entrega e quem recebe o turno, corta entre os dois jogadores e destaca o destinatário com luzes verticais. Os participantes recebem a mesma sequência. O próximo comando fica bloqueado durante os três segundos da apresentação; o bônus existente de dano +50% é preservado.
- **Tecla `\` / `|`:** durante o combate, alterna entre sua câmera e a câmera do combatente em ação. A tecla física funciona sem Shift; Shift + essa tecla também funciona sem abrir a passagem de turno. Pode ser remapeada em **Opções → Controles → Velvet Eclipse → Seguir câmera do turno**. O modo começa desativado; o rodapé mostra o modo e o nome do combatente em ação. O Shift compartilhado continua visível para todos.
- **Invocação:** coroa irregular de chamas azuis e brancas nos pés da Persona, fragmentos ascendentes e materialização gradual na posição de invocação. O corpo permanece legível entre as partículas; o efeito não desloca a Persona a partir do chão.

## Terreno e enquadramento

A formação procura uma disposição completa antes de criar os inimigos. Cada posição precisa de chão de apoio sob a área do modelo, altura livre, separação dos demais inimigos e linha de visão. Paredes, buracos e terreno perigoso excluem posições candidatas. A busca pode girar ou reorganizar a formação, sem modificar blocos.

Se não houver espaço para todos, o combate não começa e a Shadow permanece disponível. Durante as pausas entre ações, a formação é revisada; se o terreno mudou, ela procura novas posições. Se nenhuma disposição segura existir, o combate é interrompido e a Shadow é liberada. Isso evita usar posições sem apoio como alternativa. A arena própria do trem mantém sua formação específica.

A câmera de comandos considera os pés, os lados e a altura dos combatentes vivos e procura ângulos alternativos quando há obstáculos. Em locais fechados, a colisão da câmera tem prioridade: não é possível garantir visão simultânea de todos em qualquer construção. A câmera de ação mantém os cortes de apresentação; o enquadramento do grupo é usado na seleção de comandos.

## Referências observadas

Foram examinados quadros dos dois MP4s enviados pelo usuário, sem incluir esses vídeos na distribuição:

- [Exploração em Persona 3 Reload](https://www.youtube.com/watch?v=5-OXq5NfnKA), **00:42–00:54**: jogador centralizado, corrida inclinada, braço da arma recuado e acompanhamento da câmera. Os trechos de combate e a imagem enviada orientaram as chamas e os fragmentos da invocação.
- [Shift em Persona 3 Reload](https://www.youtube.com/watch?v=h7wWdF8QPZI), **00:34–00:41**, **01:56–02:03** e **02:56–03:07**: alternância de planos, gesto de recebimento, faixas azuis e clarões verticais.

As poses usam os modelos e as skins reais dos jogadores. A direção foi adaptada às proporções e à geometria de Minecraft.

## Validação e instalação

Auditorias offline cobrem formação de 1–5 inimigos, obstáculos e ausência de chão, enquadramento em quatro proporções de tela, continuidade das poses, geometria finita da invocação, pacotes compartilhados, regras do 1 More e passagem de turno. A geometria do efeito e as disposições de terreno foram também renderizadas e inspecionadas offline.

Não houve execução do Minecraft nesta entrega. Os testes offline não confirmam o resultado final com shaders, a experiência de uma sessão multiplayer ou a aparência em todos os terrenos.

Cliente e servidor precisam da **0.6.34-exploration-shift**, protocolo **20**. Mantidas as dependências e a ponte de compatibilidade com OptiFine da versão anterior. O JAR anterior é preservado em `mods/velvet-eclipse-versions` no perfil `yuxio test`; somente o JAR Velvet Eclipse é substituído.
