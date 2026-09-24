# Velvet Eclipse 0.6.28 — Catálogo Reload

## Conteúdo

- 173 Personas do Compendium do Persona 3 Reload base, com arcanos, níveis, atributos, afinidades e progressão de skills. Arsène, Izanagi e as Personas antigas continuam disponíveis.
- 345 skills registradas no total: 318 entradas deduplicadas de Reload, 16 extensões de Frei/Psi e 11 entradas anteriores do mod. Há Skill Card para cada skill, incluindo passivas, na aba criativa correspondente.
- 171 modelos provisórios em cuboides, com idle e ataques físico/mágico. Orpheus e Thanatos usam seus modelos existentes; os modelos detalhados de Arsène e Izanagi permanecem intactos. Cada nova Persona possui item de colocação na aba de modelos/NPCs.
- Passivas usam um emblema dourado próprio. A ficha explica seus efeitos; os cards também mostram descrições. Corte, impacto e perfuração aparecem separadamente nas afinidades.
- Passivas de defesa, boosts, esquiva, crítico, Counter, custo, recuperação, Growth, Shift e sobrevivência são aplicadas no combate. Trocar a Persona atualiza seus bônus. A interface recebe as passivas equipadas para exibir os custos reduzidos.

## Igor: fusão e Compendium

Interaja com Igor na Velvet Room. A tela oferece fusão normal, especial, Compendium e contribuição da party.

1. **Normal:** selecione duas Personas diferentes. O resultado usa a tabela de arcanos de Reload e níveis atuais dos ingredientes.
2. **Especial:** selecione uma das 21 receitas disponíveis, com os ingredientes exatos. As receitas incluem resultados de 3–6 ingredientes e as combinações especiais de duas Personas.
3. **Herança:** na prévia, marque de zero a duas skills dos ingredientes. Skills que o resultado já conhece não são oferecidas. Pressione **F** para confirmar; as fontes só são consumidas nessa confirmação.
4. **Party:** o proprietário escolhe a Persona e o destinatário em “Contribuir com a party”. A autorização dura três minutos e pode ser retirada com **X**. Todos precisam estar na mesma party e próximos de Igor. **R** atualiza as ofertas. O servidor revalida dono, estoque, consentimento, nível e capacidade antes de consumir os ingredientes. Ninguém pode ficar sem Persona.
5. **Compendium:** cada aquisição registra a Persona. O registro mantém maior nível, experiência e skills conhecidas, inclusive herdadas. Recomprar custa `200 + 40 × nível² + 50 × quantidade de skills` yen e exige espaço livre. A tela apresenta o preço antes de confirmar. Personas já possuídas não podem ser compradas novamente.

O nível do jogador limita o resultado de fusão. A capacidade continua começando em duas Personas e pode chegar a oito com Contratos de Igor. O Compendium não ocupa espaços do estoque. Saves anteriores são registrados automaticamente ao carregar/salvar; ingredientes consumidos continuam no registro de seu dono.

## Shuffle Time

Há 32 faixas de andares para pools de Personas e ranks de Skill Cards. As primeiras faixas concedem Personas de nível inicial e cards dos ranks baixos. Nos intervalos de andares de guardiões do jogo original, aplica-se a faixa anterior. Os sorteios não usam mais o catálogo inteiro nem elevam automaticamente uma Persona ao nível do andar.

Os dados cobrem os andares originais até 261; o limite atual da dungeon do mod permanece igual. Cards de ranks ainda fora desse limite continuam disponíveis por outros meios, incluindo aprendizado natural, herança e criativo. Esta versão não adiciona Arcana Burst. Algumas skills exclusivas sem rank de carta recebem um card criativo, mas não entram no Shuffle Time comum.

## Efeitos

- Agi: explosão de fogo; Eiga: lâminas curvas vermelhas; Zio: raio e estrela de impacto.
- Garu: espirais verdes; Bufu: cristais de gelo; Kouha: colunas e cruzes luminosas.
- Frei: órbitas nucleares e onda de impacto; Psi: ondas elípticas; Megido: explosão esférica onipotente.
- Variações da mesma família compartilham o efeito, escalado pelo poder. Golpes em área enviam o efeito para cada alvo.
- Eiga/Zio/Agi e os efeitos novos renderizam ao final do cenário, com uma rota emissiva compatível com OptiFine ou ausência do shader personalizado. Não dependem da Persona ativa do observador. Pacotes carregam posições antes da aplicação do dano.

## Adaptações explícitas

- Conteúdo base de Reload, sem adicionar as Personas de DLC ou Episode Aigis. Arsène/Izanagi existentes foram preservados; a receita especial de Arsène presente nos dados de Reload é mantida para esse personagem já autorizado.
- Eiha/Eiga e Maeiha/Maeiga são nomes unificados no mod, conforme solicitado. Os IDs antigos continuam reconhecidos. Eiga mantém o custo inicial existente.
- Frei/Psi e suas evoluções são extensões solicitadas, com ranks progressivos definidos para o mod.
- Poder e cura usam a escala de combate do mod. Skills já existentes preservam seus valores. Multi-hit sorteia a quantidade de acertos e aplica seu dano agregado em um impacto da animação.
- As sete Theurgies do protagonista incluídas no catálogo usam SP nesta versão, em vez de uma barra Theurgy; não aparecem no Shuffle comum. Armageddon usa 99 SP e deixa o usuário com 1 HP/1 SP. Não foram adicionadas skills exclusivas internas de inimigos, navegação de Fuuka ou Personas particulares dos companheiros ao Compendium do jogador.
- Os requisitos de calendário/Social Link e itens de desbloqueio do jogo original não se aplicam às fusões: valem ingredientes, nível e capacidade do jogador no mod.
- Consulte [fontes e geração](../tools/catalogue/SOURCES.md) para os dados factuais e a ressalva das probabilidades transcritas.

## Validação

Executáveis offline: `reloadCatalogueTest`, `starterPersonasTest`, `tacticalTest`. A auditoria nova percorre learnsets, descrições/efeitos, herança, transações sem consumo parcial, recompra, migração, tabelas de andares, cards e modelos carregados pelo GeckoLib. Há prévias offline em `build/reports/reload-catalogue` após executar `tools/catalogue/preview.py`.

Não houve execução nem validação visual dentro do Minecraft, conforme a preferência do projeto. A compatibilidade com o shader pack instalado ainda depende de conferência no jogo. Clientes e servidor precisam usar a mesma versão; protocolo principal 16 e protocolo de fusão 2.
