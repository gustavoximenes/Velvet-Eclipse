# Elizabeth e equipamentos — 0.6.24

Elizabeth usa o modelo aprovado, com vestido ajustado ao corpo, cabelo branco, chapéu e livro. Ela aparece junto de Igor na Velvet Room e ao lado da porta da Velvet Room no pavilhão inicial do Tártaro. Clique com o botão direito para conversar. Os diálogos originais em português explicam os sistemas do mod; a Elizabeth do pavilhão também abre a loja em yen.

## Equipamentos e loja

- Tab → EQUIP → Gerenciar equipamentos: arma, armadura, calçado e acessório. Q/E troca de categoria; setas selecionam; Enter equipa; X remove; F filtra a categoria de arma; Page Up/Down percorre listas. O mouse também funciona.
- 635 equipamentos de **Persona 3 Reload, jogo base**: 238 armas, 113 armaduras, 86 calçados e 198 acessórios. Não inclui DLC, Episode Aigis nem trajes cosméticos.
- A coleção, os quatro espaços equipados, os consumíveis e o saldo em yen persistem após sair do jogo e após morrer. Todos os equipamentos podem ser usados por qualquer jogador, sem restrição ao personagem original.
- As peças alteram os atributos do combate RPG, sem trocar a skin ou vestir armadura visual no jogador. H mostra a categoria de arma equipada com uma pose própria. Existem modelos nativos para espada, arco, espada grande, punhos, rapieira, lança, faca, arma de fogo e machado.
- Todos os equipamentos, consumíveis e o item de colocação da Elizabeth aparecem na aba criativa Velvet Eclipse. Use o item criativo com o botão direito para adicioná-lo à coleção do Tab.
- A loja usa o catálogo e os preços do servidor. É preciso estar perto da Elizabeth comerciante e fora de combate. Comprar uma peça já possuída não cobra novamente. Um pedido de compra antigo não pode ser reutilizado depois de uma compra concluída.

## Suprimentos

Medicine, Life Stone e Bead recuperam HP. Snuff Soul, Chewing Soul e Precious Egg recuperam SP. Dis-Poison, Dis-Charm, Dis-Rage, Dis-Patra e Amrita Soda curam estados negativos. Os preços dos consumíveis foram definidos para a economia em yen deste mod.

Tab → ITEM → Suprimentos da Elizabeth abre a coleção fora da batalha. Em combate, ITEM abre a seleção própria do HUD e depois permite escolher um aliado vivo. O servidor valida quantidade, alvo, utilidade e turno antes de consumir o item. O inventário comum continua bloqueado durante o combate.

## Dados e adaptação

Os nomes, categorias, valores básicos de ataque/defesa/esquiva e efeitos foram extraídos da [tabela de equipamentos de Persona 3 Reload](https://www.megatenwiki.com/wiki/Equipment_in_Persona_3_Reload), consultada em 21/09/2026. Somente os dados factuais foram incluídos; as descrições literárias do jogo não foram copiadas. O JSON está em `src/main/resources/data/velvet_eclipse/reload_equipment.json`.

Preços originais de compra são preservados quando disponíveis. Peças que só existiam como recompensas, tesouros ou fusões passam a ser vendidas: seu preço considera valor de venda e poder da peça. Essa conversão é própria do mod.

O motor de combate tem uma escala diferente do jogo: poder básico da arma = 55% do ataque cadastrado, defesa acrescenta o fator `1 + DEF/250`, e a esquiva dos calçados contribui com até 15 pontos percentuais. Bônus de atributos, HP/SP, afinidades, resistência a dano/estados, críticos, Counter, regeneração, Invigorate, custos de habilidades, sobrevivência, abertura de batalha, Shift e EXP são tratados pelo motor do mod. Ailments usam duração curta por turnos; encanto e medo incapacitam, em vez de assumir o controle do personagem. Efeitos específicos de Orgia são adaptados à postura inicial de combate e à recuperação de estados. Proteções contra morte instantânea ficam registradas para esses ataques; atualmente o mod não possui habilidades de morte instantânea.

## Modelos e verificação

Os arquivos editáveis da Elizabeth estão em `tools/elizabeth/draft`. Os modelos das nove famílias de arma, atlas e arquivos Blockbench estão em `tools/elizabeth/weapons`. Cuboides nativos, Box UV e pivô no punho; nenhuma alteração de skin é necessária.

Validação focada offline: catálogo/recursos criativos, compras e saldo, propriedade, persistência após morte, bônus de equipamento, recuperação, regressão tática e compilação Java 17. A prévia das armas foi inspecionada por renderização offline. Esta entrega não foi testada dentro do Minecraft, conforme a preferência do projeto.
