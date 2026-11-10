# Arcana Up e fusões supremas — 05/11/2026

Implementação no projeto-fonte. Nenhum JAR gerado ou instalado nesta etapa.

## Cartas e ranks

Foram criadas 23 variantes de **Arcana Up**, uma para cada Arcana cadastrada no mod. Cada jogador começa no rank 0; confirmar o uso de uma carta consome uma unidade e aumenta somente aquela Arcana em 1 rank, até o máximo de 10. Ao atingir o limite, outra carta não é consumida. Itens de Arcana Buff e Arcana Up são distintos.

O botão direito abre uma confirmação com o rank atual, o próximo rank e o bônus de EXP. Cancelar, deixar expirar a oferta, mover a carta, entrar em combate ou desligar o modo impede a ativação. O servidor verifica item, slot, jogador e token; o cliente não envia o rank desejado. Novas cartas possuem nomes EN/PT, texturas próprias, modelos e inclusão na aba criativa de Arcanas.

Os ranks e o histórico de cartas encontradas são individuais e persistem após morte, respawn e reconexão. O menu **Tab → Arcana Buffs → Ranks de Arcana** mostra as 23 progressões. Ranks não são apagados ao desligar o roguelike; o bônus de EXP fica suspenso. O progresso de Arcana é independente dos vínculos individuais com aldeões.

Os tesouros novos do Tártaro usam um único sorteio: **25% Arcana Buff, 10% Arcana Up, 65% sem carta**. Assim, cada tesouro concede no máximo uma dessas cartas. Essa distribuição preserva os 25% de Buff anteriores e acrescenta a possibilidade de Up. As 23 variantes Up têm a mesma probabilidade dentro da sua faixa.

## EXP de fusão

Conforme a escolha do usuário, cada rank concede **10% da EXP necessária para o próximo nível da Persona resultante**, arredondada para cima. O bônus usa a Arcana do resultado e o rank de quem recebe a Persona. É aplicado uma vez ao concluir a fusão, incluindo fusões especiais e contribuições de outros jogadores.

- Rank 1: 10% da EXP do próximo nível; rank 5: 50%; rank 10: 100%.
- A prévia inclui o nível, a EXP, as habilidades resultantes e a mensagem do bônus. Abrir ou atualizar a prévia não concede EXP.
- A exigência de nível do jogador é calculada antes do bônus; a EXP conquistada pode elevar o resultado acima desse nível.
- O bônus termina no nível 99. Uma Persona já nesse nível não recebe EXP adicional.
- Invocações do Compendium e EXP de combate não recebem este bônus de fusão.
- Alterar os ranks ou o modo invalida confirmações antigas. O rank de um contribuinte não é transferido ao destinatário.

## Fusões supremas

O usuário confirmou que a exigência de rank máximo vale **somente com roguelike ativado**. No modo normal, as fusões continuam seguindo os requisitos de nível e ingredientes existentes.

A nova opção **Fusões supremas** na Velvet Room lista os resultados, seu estado bloqueado/liberado, o progresso de rank, o nível exigido e as combinações disponíveis com os ingredientes próprios e as contribuições autorizadas. A navegação usa setas para escolher a Persona, A/D para alternar combinações, R para atualizar e Enter para abrir o inspect e a herança. O menu inicial passou a ter seis opções.

Com roguelike ativo, cada suprema exige rank 10 na Arcana correspondente. O bloqueio existe nas transações do servidor e nas operações que efetivamente trocam as Personas, cobrindo fusão normal, especial, busca, comandos de fusão e cooperação. Uma receita ou uma contribuição de outro jogador não contorna a exigência. As supremas especiais foram reunidas na nova aba; a aba de fusões especiais continua com as demais receitas.

Correspondências de Reload conferidas no [catálogo de Personas](https://megamitensei.fandom.com/wiki/List_of_Persona_3_Reload_Personas) e no [guia de supremas](https://www.escapistmagazine.com/how-to-unlock-all-ultimate-personas-in-persona-3-reload/):

| Arcana | Suprema |
| --- | --- |
| Fool | Susano-o |
| Magician | Futsunushi |
| Priestess | Scathach |
| Empress | Alilat |
| Emperor | Odin |
| Hierophant | Kohryu |
| Lovers | Cybele |
| Chariot | Thor |
| Justice | Melchizedek |
| Hermit | Arahabaki |
| Fortune | Lakshmi |
| Strength | Atavaka |
| Hanged Man | Attis |
| Death | Thanatos |
| Temperance | Yurlungur |
| Devil | Beelzebub |
| Tower | Chi You |
| Star | Helel |
| Moon | Sandalphon |
| Sun | Asura |
| Judgement | Messiah |
| Aeon | Metatron |

**Orpheus Telos** aparece como entrada adicional e exige rank 10 nas 22 Arcanas com fusão, seguindo a condição de conclusão de todos os vínculos como referência. **World** mantém sua carta e seu rank, mas não tem Persona no catálogo atual do mod; portanto, não foi criada uma receita fictícia nem incluída no requisito de Telos.

Até oito combinações válidas por suprema são mostradas em cada consulta, mantendo a resposta de rede limitada. A confirmação sempre revalida ingredientes, consentimento, nível e rank. Personas já possuídas ou registradas não são apagadas por esta regra.

## Verificação

Testes offline dedicados: `arcanaUpTest` e `supremeFusionTest`. Regressões relevantes: `roguelikeTest`, `fusionTest`, `reloadCatalogueTest`, `velvetReworkTest`, `progressionTest` e `playerGrowthTest`.

Prévia de ranks, confirmação, cartas e menus exportada pelo desenho usado no mod, em inglês e português. As telas offline das supremas omitem o retrato 3D e sinalizam essa limitação. Minecraft e servidor real não foram executados.

As oito suítes acima passaram. Os testes específicos verificaram 501 condições de Arcana Up e 263 de fusões supremas; isso não representa sessões de jogo. A última verificação de interface também passou após ajustar os avisos de bloqueio.

Na raiz de trabalho `se`, as prévias estão em `outputs/arcana-up-01/index.html`, os relatórios em `outputs/arcana-up-01/validation.json` e os logs em `work/arcana-up-01/`. Protocolos do roguelike e da fusão passam, respectivamente, a 2 e 4. A versão instalada do mod permanece 0.6.57; seu SHA-256 foi conferido e permanece inalterado. JAR, instalação e publicação continuam aguardando aprovação. A revisão Angel 04 permanece separada e pendente.
