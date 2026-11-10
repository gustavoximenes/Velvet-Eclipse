# Velvet Eclipse 0.6.58 — Roguelike e portais

Versão de 09/11/2026. O usuário autorizou integrar o sistema revisado e corrigir a travessia dos portais ao correr em sequência.

## Progressão

O modo começa desligado. Em **Tab → System → Roguelike**, somente o dono do mundo pode alterar a gamerule `velvetRoguelike`. Em servidor dedicado, o console define o responsável com `velvetrogue owner <UUID>`. Classe, cartas ativadas e ranks são individuais e persistem após a morte; desligar o modo suspende seus efeitos.

- DPS: +10% de dano de skills físicas ou mágicas, conforme o foco escolhido.
- Suporte: +15% de cura ou +1 turno nos debuffs de ataque, defesa e precisão.
- Weakness Hitter: metade do dano físico/mágico e metade dos custos ofensivos de SP e dos pagamentos de HP das skills físicas.
- Nove Arcana Buffs, com bônus simples ou pactos com desvantagem. Botão direito abre confirmação; cancelar preserva a carta. Buffs duplicados não acumulam.
- 23 Arcana Up, uma por Arcana, com +1 rank até 10. Cada rank acrescenta 10% da EXP do próximo nível ao resultado de uma fusão da mesma Arcana. Carta no rank máximo não é consumida.
- Tesouros novos do Tártaro: 25% Buff, 10% Up, 65% sem carta. A aba criativa contém os 32 itens com nomes e modelos.
- Aba **Fusões supremas** na Velvet Room. Com roguelike ativo, exige rank 10 da Arcana; Orpheus Telos exige as 22 Arcanas de fusão no máximo. World tem carta e rank, mas ainda não possui Persona no catálogo. Modo normal conserva os requisitos anteriores.

Detalhes: [classes e pactos](roguelike-prototype-01.md), [integração e level up](roguelike-implementation-01.md), [Arcana Up e supremas](arcana-up-01.md). Esses documentos preservam o histórico das auditorias anteriores à publicação.

## HP e SP por nível

O nível do jogador agora acrescenta **6 HP máximos e 3 SP máximos por nível**, também com roguelike desligado. A base continua 120 HP / 45 SP no nível 1, antes de equipamentos. Subir por EXP recupera somente a capacidade acrescentada, sem cura completa nem reviver quem está com HP zero.

Saves antigos calculam o ganho correspondente ao nível já alcançado sem somá-lo novamente a cada carregamento. A HUD e os resultados usam os recursos atualizados após a recompensa.

## Portais do Tártaro

O defeito vinha da combinação de dois comportamentos: durante dois segundos após uma transferência, o portal ignorava outra solicitação, mas seu bloco não tinha colisão. Era possível continuar correndo além dele. Durante a geração de um andar, afastar-se mais de seis blocos também cancelava a transferência pendente.

A superfície funcional passa a ter uma lâmina de colisão dentro da própria célula do portal, nos dois sentidos. O corpo do jogador entra na célula e aciona o teletransporte, mas não atravessa a lâmina enquanto espera. Mantendo o movimento, a detecção continua ocorrendo e tenta novamente quando termina o cooldown. O jogador pode recuar normalmente e cancelar uma espera se se afastar. O visual e os destinos são preservados.

A mesma proteção permanece quando a passagem é negada por batalha, veículo ou Guardião não derrotado. As chegadas no Tártaro e os retornos à cidade/mundo zeram a velocidade residual. Solicitações pendentes são revalidadas antes do teletransporte, incluindo jogador vivo, dimensão, distância, combate, animação de entrada e veículo.

A colisão é uma propriedade do bloco, portanto passa a valer também nos portais já construídos, sem reconstruir os andares ou apagar o progresso. Espectador continua seguindo as regras normais de ausência de colisão do Minecraft; não é tratado como travessia jogável.

## Entrada de batalha e limites

Inclui a correção preparada da orientação da faixa no piso, usando a posição da câmera para evitar leitura invertida ou espelhada. O modelo Angel permanece na revisão 03 já publicada; a revisão 04 de modelagem continua separada.

Cliente e servidor devem usar esta versão. Protocolos de roguelike/fusão: **2/4**; principal/exploração/cidade/menu: **24/5/9/7**. Mundos, outros mods, áudio e efeitos de invocação não são substituídos.

Validação offline: colisão nativa com corrida, salto, velocidades altas, aproximação pelos dois lados e espera prolongada; verificações de classes, cartas, ranks, fusões, level up, orientação de faixa, mapas e multiplayer lógico. Sem executar cliente Minecraft ou servidor real. O relatório da entrega registra os resultados e hashes; não se declara validação visual dentro do jogo.
