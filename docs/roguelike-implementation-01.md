# Roguelike e crescimento de HP/SP — implementação 01

Atualizado em 05/11/2026. Implementado no projeto-fonte; sem gerar ou instalar JAR, sem commit/push desta etapa. A autorização para implementar o roguelike não foi tratada como aprovação da revisão Angel 04 nem de uma nova publicação.

## Integração do roguelike

Complemento de 05/11/2026: [Arcana Up e fusões supremas](arcana-up-01.md), com ranks individuais, bônus de EXP e restrição de supremas apenas no modo roguelike. A distribuição de tesouro passa a 25% Buff, 10% Up e 65% sem carta.

- `Tab → System → Roguelike` controla a gamerule `velvetRoguelike`, desligada por padrão. No mundo integrado, somente o dono pode alterar; no dedicado, o console registra o UUID do dono com `velvetrogue owner <UUID>`.
- A aba `Arcana Buffs` reúne coleção, cartas ativas e os focos de classe: DPS físico/mágico, suporte de cura/debuff e Weakness Hitter.
- Nove cartas estão registradas como itens, com tooltip, modelo, textura e categoria criativa. Tesouros recém-recolhidos do Tártaro têm 25% de chance de oferecer uma carta. Não altera os baús vanilla em geral.
- Botão direito abre confirmação; somente a confirmação aceita pelo servidor consome uma unidade e aplica o efeito. Cartas repetidas não acumulam o mesmo bônus.
- Classe e cartas ativadas persistem após morte/reconexão. Desligar o modo suspende os efeitos, preservando a coleção. Ainda não há reset de run.
- Pagamentos de HP/SP são compartilhados entre servidor e HUD. Weakness Hitter paga metade do HP efetivo de skills físicas e metade do SP ofensivo, arredondando para cima. Bônus de encontro ficam fixos até o fim da batalha.

Os valores das classes, nove cartas e limites do protótipo estão em [roguelike-prototype-01.md](roguelike-prototype-01.md). A exportação de prévias dos testes agora grava somente em `build/reports`, sem sobrescrever os ícones e modelos de produção.

## Correção de level up

Antes, o código alterava nível/EXP, mas os máximos continuavam em 120 HP e 45 SP, somados apenas ao equipamento. Agora o nível do **jogador** acrescenta **6 HP máximos e 3 SP máximos por nível**. São valores iniciais de balanceamento deste mod, não uma reprodução declarada da curva original de Persona 3 Reload.

| Nível | HP máximo | SP máximo |
| --- | ---: | ---: |
| 1 | 120 | 45 |
| 10 | 174 | 72 |
| 25 | 264 | 117 |
| 50 | 414 | 192 |
| 99 | 708 | 339 |

Valores sem equipamento. O crescimento funciona também com roguelike desligado e é independente do nível da Persona.

Ao subir por EXP, HP e SP atuais recebem somente a capacidade conquistada. Exemplo: `75/120 HP, 20/45 SP` passa a `81/126 HP, 23/48 SP` no nível 2. Não há cura completa; HP zero continua zero. Vários níveis na mesma recompensa somam seus ganhos. EXP de batalha e do Shuffle Time segue a mesma regra.

Saves antigos passam a calcular os máximos correspondentes ao nível já alcançado, preservando os recursos atuais e as bases personalizadas que estavam salvas. Os campos antigos continuam guardando a base; o crescimento é derivado do nível. Isso evita acumular bônus novamente ao salvar, reconectar ou clonar o jogador. Equipamentos continuam somando seus próprios valores uma única vez.

Comandos administrativos de nível recalculam os máximos sem curar. Reduzir o nível limita HP/SP atuais ao novo máximo; o teto continua sendo nível 99.

Durante os resultados de batalha, a HUD agora recebe os recursos e a Persona já atualizados após as recompensas, em vez da cópia anterior do combatente. A tela de resultados informa o ganho de HP/SP máximos, em inglês ou português. Encerrar o encontro durante os resultados não persiste novamente os recursos antigos sobre o progresso concedido.

## Verificação e limites

Verificações offline executadas: `playerGrowthTest`, `progressionTest`, `equipmentTest`, `shuffleTest` e `roguelikeTest`. Cobrem todos os limiares de nível 1–99, múltiplos níveis, saves antigos, repetição de save/load, clone, KO, equipamentos, snapshots de resultados, XP de Shuffle e compatibilidade dos custos com Weakness Hitter. Os testes do roguelike cobrem classes, pactos, permissões, confirmação, persistência, sorteio e interface.

Logs em `../work/player-growth-01/`; resultados estruturados em `build/reports/player-growth/validation.json` e na entrega de documentação `../outputs/roguelike-implementacao-01/validation.json`.

Minecraft e servidor real não foram executados. Não se declara teste visual dentro do jogo nem teste multiplayer ao vivo. O JAR instalado permanece `0.6.57-angel-entry`, SHA-256 `f2e02260d52a232b647415c7afb2c33b979d24c9ce4f3dace1d6450cffb65dad`.
