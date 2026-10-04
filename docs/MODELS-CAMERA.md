# 0.6.44 — Pixie, Elizabeth e câmera de combate

## Modelos

- Pixie revisão 03 e Elizabeth revisão 05 substituem as versões antigas, com a
  geometria, atlas e rig aprovados. Não há uma nova remodelagem nesta integração.
- Pixie mantém voo, ataque mágico e tapa físico, com o contato ajustado ao
  relógio do combate. Elizabeth usa o novo modelo também nas cenas da Velvet
  Room. A carta de fusão da Pixie recebeu o retrato do modelo novo.
- Fontes editáveis e exportador em `tools/approved_models`. Escalas globais
  compatíveis com o mundo; dimensões dos cuboides e Box UV preservados.

## Câmera por participante

No Tártaro, a câmera preserva o enquadramento de batalha. Somente as partes da
arquitetura e dos objetos ornamentais que interceptam a visão dos combatentes
são recortadas no cliente daquele participante. Grandes faces são subdivididas
para preservar o restante da parede. O piso, os materiais e a emissão nativa
continuam presentes.

O jogador fora do combate vê o cenário inteiro. Não há pacote que altere blocos,
remoção no servidor ou mudança na colisão física dos jogadores. O minimapa e a
planta transitável permanecem intactos. O recorte é restaurado ao encerrar a
sessão de combate, trocar de mundo ou desconectar. Aberturas já necessárias são
retidas durante as ações para evitar paredes piscando entre tomadas. O conjunto
é limitado a 32 volumes locais; apenas as seções afetadas são recompiladas.
Durante o recorte, o descarte de seções por paredes opacas é desativado somente
nesse cliente, mantendo o frustum. Assim, a parede física preservada não esconde
as seções atrás da abertura visual. A configuração anterior é restaurada no fim.

O trem usa outra câmera: posições dentro do vagão, com sondagem de paredes,
bancos e barras, ajustes de lente e orientação. O combate mantém uma tomada
estável pelo corredor durante os ataques, sem atravessar ou ocultar o vagão.
O vagão da Sacerdotisa considera sua largura maior e o volume do modelo.

A câmera recalcula quando a lista de entidades passa a estar disponível ou um
ator visual é substituído. Pequenas correções de rede não mudam a tomada;
deslocamentos relevantes passam por estabilização antes do reenquadramento.

## Inimigos visíveis

- Um participante não é ocultado por uma mensagem antiga destinada a observadores.
- A fumaça de outro encontro permanece exclusiva daquele encontro; mensagens
  de um mundo anterior são descartadas visualmente.
- Modelos vivos da própria batalha não dependem da pequena caixa física para
  o descarte por frustum, inclusive ao cair ou usar modelos grandes.
- O servidor recupera atores visuais ausentes sem recriar o combatente lógico,
  resetar HP ou ressuscitar derrotados, e distribui os novos IDs à equipe.
- Guardiões do trem não são descartados pela regra vanilla de Slimes no Pacífico.

## Verificação

Auditorias offline: recursos finais carregados pelo GeckoLib, textura e rig
comparados às revisões aprovadas; animações amostradas; isolamento entre
observador e participante; restauração da malha; chegada tardia de inimigos;
paredes, bancos, barras e portões do `TrainLayout` real nas 12 posições possíveis,
nos três encontros e nas proporções 4:3, 16:9 e 21:9. Regressões de fusões,
Velvet Room, multiplayer, raid, animações e quadros nativos do Tártaro.

A revisão visual dos modelos usa as malhas produzidas pelo GeckoLib, renderizadas
offline. Não houve teste dentro do Minecraft nem sessão real multiplayer.
O recorte usa os materiais e buffers nativos, incluindo emissão e UV, mas a
aparência final e o custo de recompilar chunks com cada shader ainda dependem
da validação no jogo. Não foi usado controle do computador.

Nenhum novo bloco/item foi criado; os itens existentes de Pixie e Elizabeth
continuam nas respectivas abas criativas. Os protocolos de rede são mantidos.
Para receber as correções visuais e de recuperação, use esta versão nos clientes
e no servidor.
