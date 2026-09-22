# Velvet Eclipse 0.6.26 — Permissões e Contrato de Igor

## Loja de Elizabeth

O estoque agora depende de permissões pessoais, usadas com o botão direito fora de combate. A primeira Permissão de Compra é recebida por cada participante na sua primeira vitória contra a Sacerdotisa do trem, acessível pela porta do andar 25. Uma permissão distinta está no primeiro baú de cada sala fixa de 50 a 250, a cada 25 andares.

As permissões registradas sobrevivem à morte, reconexão e troca de Dark Hour. Cada permissão só é registrada uma vez. Uma permissão obtida fora de ordem fica registrada, mas não pula as anteriores. Repetições preservam o item. Ao completar uma etapa, Elizabeth avisa o jogador que novos equipamentos estão disponíveis.

Foram conferidos 250 equipamentos vendidos na delegacia de **Persona 3 Reload — jogo base**. Os 635 equipamentos continuam no catálogo criativo; itens exclusivos de fusão, baús ou recompensas não entram automaticamente na loja. Os consumíveis existentes permanecem disponíveis.

| Permissão | Período de estoque de Reload | Novos equipamentos | Total acumulado |
| --- | --- | ---: | ---: |
| Inicial | Antes da operação do trem | 17 | 17 |
| Chefe 25F | 10/05 | 21 | 38 |
| Baú 50F | 14/06 | 12 | 50 |
| Baú 75F | 18/07 | 25 | 75 |
| Baú 100F | 08/08 | 29 | 104 |
| Baú 125F | 10/09 | 35 | 139 |
| Baú 150F | 06/10 | 28 | 167 |
| Baú 175F | 06/11 | 29 | 196 |
| Baú 200F | 10/12 | 26 | 222 |
| Baú 225F | 01/01 | 28 | 250 |
| Baú 250F | Estoque final já completo | 0 | 250 |

Essa é uma adaptação dos períodos de campanha para os andares do mod, não uma alegação de que toda data de reposição coincide com o dia seguinte ao chefe. Equipamentos sem data na fonte, associados à entrada de integrantes, acompanham a etapa mais próxima: Akihiko em maio, Mitsuru em junho, Aigis em julho, Koromaru em agosto e Ken/Shinjiro em setembro. A permissão de 250F continua sendo coletável/registrável sem inventar uma expansão de estoque inexistente no jogo base.

Fonte dos fatos: [Police Station — megaten-database](https://aqiu384.github.io/megaten-database/p3r/overworld.html), conferida com [Police Station — GameFAQs](https://gamefaqs.gamespot.com/pc/409941-persona-3-reload/faqs/81170/police-station). A tabela normalizada está em `data/velvet_eclipse/reload_shop.json`. `tools/progression/import_shop.py` reproduz a importação a partir do HTML público baixado; rejeita nomes e preços que divergem do catálogo de equipamentos.

## Prêmios sem duplicação ou perda por falta de espaço

Cada jogador recebe a permissão 25F e **um Contrato de Igor** uma única vez na primeira vitória. O registro de entrega é persistido no mundo por UUID e não é reiniciado pela raid, morte ou Dark Hour. Se faltar espaço real no inventário, os itens ficam em uma fila persistente e são entregues após liberar espaço, fora de combate. Não são descartados no chão.

O primeiro baú dos andares 50–250 continua disponível para entregar uma permissão a cada jogador, inclusive em salas já saqueadas antes da atualização. Yen e tônicos do baú continuam sendo recolhidos uma única vez no mundo. O minimapa deixa de marcar a permissão quando o próprio jogador a recebe. Os outros baús mantêm o comportamento anterior.

Para saves antigos, jogadores com vitória registrada contra `raid_2` no bestiário recebem os dois prêmios uma única vez ao entrar. A antiga liberação global da loja não concede progresso a jogadores que não participaram dessa vitória. Equipamentos já comprados permanecem na coleção.

## Personas

- Capacidade inicial: **2**, incluindo a Persona inicial.
- Usar **Contrato de Igor** consome um item e amplia a capacidade em **1**, até **8**. No máximo, o item é preservado.
- Existe um contrato por primeira vitória no chefe do trem. Contratos adicionais estão na aba criativa de progressão; não foram adicionados outros drops automáticos.
- Todas as aquisições respeitam a capacidade. Se estiver cheia, a carta de Persona do Shuffle Time e a rodada são preservadas. Fortalecer uma Persona que já está no grupo continua permitido.
- Fusões funcionam com o grupo cheio, pois substituem duas Personas por uma.
- Saves anteriores preservam Personas excedentes, níveis e habilidades em uma reserva de migração. O menu Tab mostra a capacidade e identifica `RESERVA`. ENTER troca uma reservada pela equipada quando o grupo está cheio, ou a traz para uma vaga livre. Somente as Personas no grupo podem ser usadas em combate ou fusão; novas aquisições não alimentam a reserva.

## Criativo e combate

Onze abas separam progressão/ferramentas, Skill Cards, modelos de inimigos, personagens, blocos, objetos do Tártaro, armas, armaduras, calçados, acessórios e consumíveis. A classificação percorre todo o registro do mod e mantém itens novos visíveis, com uma categoria geral como fallback.

Golpes de fraqueza ou críticos que derrotam instantaneamente um alvo concedem **1 More** quando ainda há combate. A regra funciona com habilidades, ataques básicos e itens de dano, incluindo ataques em área. Alvos já derrubados e chefes imunes não concedem um novo bônus; repelir/absorver/anular ou um golpe neutro sem crítico também não. A morte do último inimigo leva à vitória, sem turno extra vazio. A preparação da animação continua sem aplicar dano antecipadamente.

## Validação

Auditorias offline focadas na progressão, combate e regressões de fusão. Não houve execução de Minecraft nem validação visual em servidor, conforme a preferência do projeto. Cliente e servidor precisam usar esta versão, pois os pacotes de menu/equipamentos receberam novos campos.
