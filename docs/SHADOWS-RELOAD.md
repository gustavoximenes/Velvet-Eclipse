# 0.6.23 — Inimigos de Reload e Bestiário

## Uso

Abra **Tab → BESTIÁRIO**. Use as setas ou a roda para escolher um inimigo. A ficha mostra o modelo, maior nível encontrado, total de encontros e derrotas. Apenas inimigos revelados em uma batalha entram na lista. Elementos ainda não acertados continuam com **?**; o resultado observado aparece como FRACO, NEUTRO, RESISTE, ANULA, REPELE ou ABSORVE.

Os registros são individuais e persistem ao reconectar, morrer ou renovar a Dark Hour. Derrotas são contadas por inimigo vencido, uma vez por combate, para os participantes presentes. Fugir não acrescenta derrotas; inimigos eliminados antes da fuga continuam registrados. Observações antigas compatíveis são importadas sem inventar contagens históricas de vitórias. A Sacerdotisa mostra as afinidades observadas, com aviso de que sua fraqueza muda durante o combate.

## Correções das Shadows

- Encontros comuns usam exclusivamente o catálogo de inimigos, separado das Personas invocáveis. Não há Izanagi, Thanatos ou outras Personas como adversários sorteados.
- A Shadow vaga dentro de sua sala, mantendo o limite de uma por sala. Durante a interrogação ela se volta diretamente para o jogador localizado.
- A Shadow original fica reservada durante o combate e retorna visível ao fugir, com a mesma identidade e semente do encontro. A vaga finita do andar só é consumida por vitória. Há oito segundos de tolerância após a fuga.
- A reserva permanece enquanto outro participante continua lutando. Interrupção/reinício restaura reservas órfãs; a migração reconhece o comportamento de descarte da versão anterior.
- Ataques básicos distinguem corte, impacto e perfuração. Magias e One More dos inimigos continuam no sistema de turnos; inimigos não realizam All-Out Attack.

## Catálogo e modelos

Foram adicionados 19 inimigos dedicados, com cuboides, ossos articulados, Box UV, texturas próprias e animações `misc.idle`, `action.physical` e `action.magic`. Os arquivos editáveis `.bbmodel`, geometrias, texturas, animações e prévias estão em `tools/shadow_bestiary/draft`. O gerador usa o mesmo fluxo de coordenadas/exportação Bedrock dos modelos de Igor e Izanagi. São interpretações estilizadas em cubos das aparências de Reload, não malhas ou texturas extraídas do jogo.

| Inimigo | Faixa original | Fraquezas |
| --- | --- | --- |
| Cowardly Maya | 2–19 | Corte, fogo |
| Muttering Tiara | 2–21 | Vento |
| Merciless Maya | 3–9 | Gelo |
| Magic Hand | 7–21 | Fogo |
| Obsessed Cupid | 9–21 | Gelo |
| Trance Twins | 12–21 | Eletricidade, luz |
| Wealth Hand | 12–21 | Impacto |
| Resolved Checkmate | 12–21 | Eletricidade |
| Grave Beetle | 14–21 | Vento |
| Heat Balance | 23–34 | Eletricidade, vento |
| Frivolous Maya | 23–37 | Impacto, gelo |
| Treasure Hand | 23–41 | Luz |
| Yearning Checkmate | 23–41 | Trevas |
| Laughing Table | 24–34 | Vento |
| Grieving Tiara | 24–41 | Fogo |
| Black Raven | 25–37 | Luz |
| Ruthless Ice Raven | Guardião original 5 | Fogo |
| Barbaric Beast Wheel | Guardião original 11 | Vento |
| Swift Axle | Guardião original 17 | Eletricidade |

O mod mantém as regras solicitadas: guardiões a cada 10 andares e transições a cada 25. Os três guardiões acima alternam nessas salas; nunca entram no sorteio comum. O andar 1 usa o conjunto inicial do 2, o 22 usa o final de Thebel, e acima do 25 o conjunto inicial de Arqa continua com escalonamento de atributos. O andar 25 permanece uma sala segura; Black Raven está disponível no conjunto usado a partir dali. Grupos contêm 1–5 inimigos, podendo repetir espécies. Mãos raras e Checkmates têm peso reduzido.

HP/SP base, afinidades e faixas vêm dos dados de Reload; atributos derivados e habilidades são adaptações ao sistema de combate já existente. O catálogo completo com resistências está em `shadow/EnemyCatalog.java`. O Bestiário não transmite esse catálogo secreto: recebe somente observações do jogador.

Todos os 19 modelos têm itens de colocação com nome e ícone na aba criativa Velvet Eclipse. São expositores de modelo. O Skill Card **Single Shot** também está na aba e usa perfuração. O conteúdo de raid existente mantém seus modelos próprios.

## Referências consultadas

- [Tabela de Shadows e andares de Persona 3 Reload](https://aqiu384.github.io/megaten-database/p3r/tartarus): faixas, níveis, HP/SP e afinidades.
- [Shadows in Persona 3 Reload](https://megatenwiki.com/wiki/Shadows_in_Persona_3_Reload): famílias e aparências, incluindo páginas individuais dos inimigos.
- [Thebel em Persona 3 Reload](https://www.neoseeker.com/persona-3-reload/guides/Thebel): contexto dos primeiros andares.

## Verificações e compatibilidade

Auditoria focada: `shadowTest bestiaryTest tacticalTest`. Inclui seleção por andar sem Personas/chefes, dano físico por tipo, manutenção da vaga e semente após fuga/interrupção, persistência e privacidade do Bestiário, menu, carregamento nativo de geometrias/animações e recursos criativos. Prévia offline dos modelos e da tela do Bestiário inspecionada. Sem teste dentro do Minecraft ou multiplayer real, conforme preferência do projeto.

Cliente e servidor precisam deste mesmo JAR: protocolo principal 14 e menu 4. Nenhum mundo, configuração do jogador ou outro mod precisa ser apagado. Reservas e dados existentes são preservados/migrados conforme descrito acima.
