# 0.6.20 — Operação Monorail

Clique com o botão direito na porta Monad fixa do andar 25 do Tártaro. Cada jogador entra na equipe clicando nessa porta; o primeiro é o líder. A HUD mostra os integrantes na ordem de entrada. Todos devem permanecer perto da porta, vivos e fora de combate. O líder confirma **Enter / Embarcar**; **Esc / Sair** abandona o lobby. A peça decorativa colocada pelo inventário criativo não inicia raids.

O limite inicial é **cinco jogadores**. `/velvetraid maxplayers <quantidade>` aceita **1 a 12**, exige permissão de operador e também funciona pelo console. O valor é salvo no mundo e vale para os próximos lobbies. Cada tentativa ocupa uma área própria da dimensão `velvet_eclipse:monorail`; outra equipe não pode entrar na batalha em andamento.

## Trem e diálogos

O interior usa piso, paredes, bancos, janelas verdes, luminárias e barras próprios. O trajeto é um único corredor: embarque, Sentinela do Trem, Condutor Sombrio e vagão final da Sacerdotisa. As divisórias dos dois encontros vão até o teto e só abrem após a vitória e a confirmação dos resultados por todos. Construção, destruição e teleporte por itens são bloqueados dentro da raid.

O diálogo inicial usa a HUD azul/ciano e a skin do participante que está falando. Primeiro fala o líder; os seguintes falam na ordem de entrada. Todos confirmam **Enter** para avançar cada fala. Os diálogos são uma adaptação original em português, sem transcrição do roteiro do jogo. As referências orientaram a investigação do trem, os confrontos antes da chefe e a aceleração durante o combate; as regras numéricas abaixo seguem o pedido do usuário. Referência consultada: [RPG Site — encontro com a Sacerdotisa no monorail](https://www.rpgsite.net/preview/14766-persona-3-reload-pax-west-2023-the-arcana-priestess-boss-and-the-new-shift-mechanic).

## Sacerdotisa

O modelo aprovado, incluindo a cabeça refeita e as tranças articuladas, usa as animações `misc.idle` e `action.attack`. A entidade fica sentada e imóvel. A animação de ataque deve terminar antes da próxima ação dos jogadores.

- O relógio começa em **05:00 ao iniciar o combate contra a chefe**. Diálogo, deslocamento e subchefes não consomem esse tempo.
- Os ataques da chefe alternam: físico no primeiro turno, gelo em área no segundo, repetindo o ciclo. O gelo atinge todos os integrantes vivos.
- A fraqueza inicial é fogo. Ao cruzar 75%, 50% e 25% de HP, ela muda para outro elemento aleatório e o relógio perde, respectivamente, **30, 30 e 60 segundos**. Um golpe que cruza mais de um limiar aplica cada penalidade uma vez. Cura não permite repetir fases.
- Fraquezas e críticos causam dano adicional, mas não derrubam nem atordoam a Sacerdotisa e não concedem 1 More ou All-Out Attack contra ela. Os dois subchefes seguem as regras normais de combate.
- O temporizador é controlado pelo servidor e continua durante seleção de ações e animações. Na vitória ele congela, inclusive na tela de resultados.
- Tempo esgotado ou derrota da equipe encerra as batalhas, devolve os integrantes para fora do trem e provoca a morte no Minecraft. Após renascer, HP/SP do mod são restaurados para permitir outra tentativa. A próxima entrada reconstrói o trajeto e reinicia os três encontros.
- Desconexão ou encerramento do servidor interrompe a tentativa e devolve a equipe, sem aplicar a penalidade de morte por tempo. Não há retomada no meio do trem. Um login após encerramento inesperado também retira o jogador da dimensão da raid.

As vitórias usam os resultados existentes, aguardando Enter. Cada subchefe concede 240 EXP e ¥500; a Sacerdotisa concede 240 EXP e ¥2.000. Integrantes caídos recuperam 1 HP ao continuar com uma equipe vitoriosa. Recompensas já recebidas não são desfeitas por uma derrota posterior.

## Recursos e entrega

Comandos de nível (operador, fora de combate e antes de entrar na raid):

```text
/velvet player level 25
/velvet persona level izanagi 25
/velvet persona level active 25
/velvet player level 40 NomeDoJogador
/velvet persona level izanagi 40 NomeDoJogador
```

Aceitam níveis de 1 a 99. Sem nome no final, alteram o próprio jogador; pelo console, informe o jogador. `active` indica a Persona equipada. O nível de jogador é o nível próprio do mod, usado nas fusões. A EXP parcial é zerada no nível escolhido. Skills já aprendidas ou herdadas são preservadas, e elevar a Persona libera as skills naturais daquele nível. O comando exige que a Persona já esteja no inventário do jogador; os níveis são salvos e os menus abertos são atualizados.

Os sete blocos do trem, o item de colocação da Sacerdotisa e o Skill Card Tempestade Glacial aparecem na aba criativa Velvet Eclipse. A figura colocada é decorativa; Shift + clique no criativo a remove. O modelo editável e suas ferramentas ficam em `tools/priestess`; `tools/raid/build_assets.py` exporta os recursos para GeckoLib. Clientes e servidor precisam usar a mesma versão. Reabra o mundo após trocar o JAR para carregar a nova dimensão.

Validação direcionada: `raidTest`, `tacticalTest` e `progressionTest`, incluindo timer, fases, fraqueza real em combate, imunidade à queda, gelo atingindo cinco combatentes, formações até 12 jogadores, divisórias, pacote de rede, recursos criativos, comandos de nível, persistência, skills herdadas e carregamento nativo de geometria/animações pelo GeckoLib. As prévias de lobby, diálogo e trem são renders offline da geometria utilizada. **Não houve execução do Minecraft nem validação visual/multiplayer com clientes reais**, conforme a preferência do projeto; esses pontos e o balanceamento permanecem para o teste no jogo.
