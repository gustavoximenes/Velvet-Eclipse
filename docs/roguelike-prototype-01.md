# Roguelike — protótipo 01

Preparado em 04/11/2026, no projeto-fonte. Sem gerar, instalar ou publicar um JAR.

Implementação autorizada e revisada em 05/11/2026, junto da correção do crescimento de HP/SP. O fluxo abaixo está conectado aos sistemas do mod no código-fonte. Detalhes da integração e dos testes: [roguelike-implementation-01.md](roguelike-implementation-01.md). O JAR permanece pendente de aprovação.

## Fluxo

O complemento [Arcana Up e fusões supremas](arcana-up-01.md) acrescenta um segundo tipo de carta e descreve os ranks e requisitos aprovados após este protótipo.

1. O dono abre **Tab → System → Roguelike**. A gamerule `velvetRoguelike` começa em `false`.
2. Cada jogador escolhe seu próprio foco na nova aba **Arcana Buffs → Class focus**.
3. Cada baú de tesouro do Tártaro recém-recolhido tem **25% de chance** de conceder uma carta física aleatória entre nove tipos. Reabrir um baú já recolhido não oferece outra tentativa.
4. O tooltip do item informa bônus e desvantagem. Usar a carta em mão com o botão direito abre a confirmação. Cancelar preserva o item; confirmar consome uma unidade e ativa o bônus.
5. A aba mostra a coleção e quais bônus estão ativos. Encontrar ou carregar o item não ativa o efeito. Cartas repetidas não acumulam o mesmo bônus e não são consumidas novamente.

Se o inventário estiver cheio, a carta cai junto ao jogador. Novos itens possuem ícones, modelos e uma aba criativa **Velvet Eclipse · Arcana Buffs**.

## Classes

| Classe e foco | Efeito inicial |
| --- | --- |
| DPS físico | +10% de dano de skills físicas |
| DPS mágico | +10% de dano de skills mágicas |
| Suporte / cura | +15% de cura das skills, inclusive pelo menu fora de combate |
| Suporte / debuffs | +1 turno nos debuffs de ataque, defesa e precisão |
| Weakness Hitter | −50% de dano físico/mágico, incluindo ataque básico; −50% do custo de SP das skills ofensivas e do **HP efetivamente pago** pelas skills físicas |

Os descontos do Weakness Hitter foram aplicados a HP e SP conforme sua confirmação. Custos fracionários arredondam para cima, com mínimo de 1 quando já existia custo. Custo zero permanece zero. Uma skill física nunca pode consumir o último HP. Spell Master e Arms Master são calculados primeiro; o desconto da classe entra uma única vez depois deles. O menu de combate usa exatamente o mesmo cálculo de custo do servidor.

A redução não altera fraquezas, queda, One More ou regras de All-Out e Shuffle Time. Ela torna mais viável usar várias fraquezas, sem garantir Shuffle Time em todo combate. All-Out, itens ofensivos e itens de cura não recebem os modificadores de dano/cura da classe. Instakills continuam sendo efeitos de chance, sem um número de dano que possa ser reduzido pela metade.

O jogador começa sem classe. Trocar foco é permitido fora de batalha, raid, despertar e preparação de encontro. Este primeiro protótipo não cobra pela troca e não possui talentos, níveis de classe ou reinício da run.

## Arcanas

| Arcana | Bônus | Desvantagem |
| --- | --- | --- |
| I — Mago / Centelha Arcana | +8% de dano de skills mágicas | Nenhuma |
| II — Sacerdotisa / Graça Serena | +10% de cura das skills | Nenhuma |
| VII — Carro / Imparável | +35% de dano de ataques físicos e mágicos | Sem One More e sem receber Shift; participa do All-Out iniciado por aliado |
| IX — Eremita / Dúvida Persistente | +1 turno nos debuffs de ataque, defesa e precisão | Nenhuma |
| XI — Força / Mão Firme | +8% de dano de skills físicas | Nenhuma |
| XII — Enforcado / Graça Altruísta | +30% de cura das skills | −15% de dano de ataques físicos e mágicos |
| XIV — Temperança / Fôlego Medido | −10% no custo de SP das skills ofensivas | Nenhuma |
| XV — Diabo / Poder Imprudente | +25% de dano de ataques físicos e mágicos | +20% de dano recebido |
| XVII — Estrela / Abrigo Sereno | −8% de dano recebido | Nenhuma |

Bônus diferentes podem coexistir. Percentuais das cartas do mesmo atributo são somados, depois combinados com o multiplicador da classe. Exemplo: DPS mágico com Mago = `1,10 × 1,08 = 1,188`, antes dos arredondamentos e demais regras do combate. Suporte de debuff com Eremita totaliza cinco turnos. Os bônus de dano recebido não alteram instakill, veneno ou pagamentos de HP.

## Persistência e autoridade

- Classe, histórico de cartas coletadas e bônus ativados são salvos por jogador e preservados na morte, no respawn, ao sair e ao reconectar. Cartas físicas ainda não usadas seguem as regras normais do inventário do Minecraft.
- Desligar o modo suspende todos os bônus e drops de Arcanas; o histórico permanece. Reativar restaura os efeitos.
- Mundo local/LAN: o dono é o UUID do criador do mundo integrado. Ser operador ou líder da party não concede essa autoridade.
- Servidor dedicado: o console define o dono com `velvetrogue owner <UUID>`. A configuração é salva no mundo. Também é possível informar `-Dvelvet.roguelikeOwner=<UUID>` na inicialização, se ainda não houver dono salvo.
- O console pode definir a gamerule; comandos de jogadores, inclusive com OP, são verificados pelo dono. A verificação inclui `/execute ... run gamerule velvetRoguelike ...`.
- Alterar o modo fica bloqueado enquanto houver jogador em combate ou raid. Os bônus usados num encontro são uma cópia fixa do estado no início da batalha.
- A confirmação usa um token individual que expira após 1.200 ticks, é utilizável uma única vez e revalida carta, slot, modo e estado de combate no servidor. O cliente não escolhe o efeito nem o item a consumir.

## Visual

Nova coleção com cartas, números romanos e símbolos vetoriais próprios inspirados na apresentação de tarô da série. As ilustrações são simplificadas para esta primeira revisão; não são cópias das texturas oficiais. Bênçãos e pactos exibem suas consequências na coleção e na confirmação.

Interface em inglês e português, mantendo inglês como padrão. A opção **Arcana Buffs** foi acrescentada ao menu Tab; o espaçamento da lista foi ajustado para caber a nova entrada.

## Verificação

Executados offline: `roguelikeTest`, `personaTimingTest` e `difficultyTest`. As verificações exercitam classes, custos e arredondamento, pagamento atômico, debuffs, dano, fraquezas/One More/Shift/All-Out, persistência, normalização, ofertas, tokens, autoridade, sorteio, codecs e áreas clicáveis. As telas foram exportadas pelo desenho usado na interface do mod e inspecionadas em português e inglês.

O resultado atualizado está em `validation.json`. Os testes de dificuldade existentes passaram com 3.891 verificações, e os de sincronização do dano após a animação com 8.407. Esses resultados são verificações offline; não foi executado Minecraft nem validado um servidor real.

## Limites desta etapa

O drop está conectado aos tesouros do Tártaro, não a todos os baús vanilla. Os valores de bônus e a chance de 25% são propostas de balanceamento para revisão. Não há reset de run, remoção individual de pactos, troca de cartas por moeda ou empilhamento de duplicatas neste protótipo. Não houve alteração no JAR instalado, commit ou push. A revisão Angel 04 e a correção da faixa de entrada de batalha continuam no estado pendente de aprovação anterior.
