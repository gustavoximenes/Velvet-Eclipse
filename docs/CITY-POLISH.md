# 0.6.48 — Dormitório, Paulownia e Tártaro na Gekkoukan

Integra as propostas aprovadas `dormitorio-auditoria-02`, `paulownia-auditoria-01` e `paulownia-huds-02`. A correção de inclinação do Forneus da 0.6.47 permanece, assim como a integração anterior de Pixie, Elizabeth, Apsaras, Jack Frost e Chimera.

## Cidade

- Dormitório único com quatro pavimentos, escadas, quartos e áreas comuns, sala de comando com seis computadores, cobertura, terraço e horta. As folhas da entrada ficam abertas; a circulação entre pavimentos usa degraus, patamares e passagem de retorno.
- Paulownia circular com fonte, cobertura translúcida, nove lojas, acesso sob a escada à Velvet Room e Mandragora no piso superior.
- Geometria dividida em blocos de um metro. Cada bloco tem seus próprios vértices, colisão, iluminação e recorte; não há uma malha do prédio inteiro sobre paredes invisíveis. Colisões giradas usam faixas conservadoras de 1/8 de bloco.
- Plantas por piso no minimapa, pontos de chegada revisados e dezesseis materiais novos na aba criativa de blocos, além do módulo de arquitetura.
- A revisão 3 atualiza os dois distritos. O processo preserva blocos diferentes do projeto anterior, retoma após reiniciar o servidor e muda a primeira chegada/renascimento para o novo quarto. Não cria uma segunda cidade.

## Dark Hour

O Tártaro surge no terreno da Gekkoukan, substituindo a escola durante a Dark Hour. O pátio conecta-se à entrada da torre. Ao fim do período, a escola retorna. A substituição é compartilhada pelos jogadores da cidade e usa um registro persistente dos estados dos blocos e dos dados dos blocos com inventário. O registro também permite terminar a restauração após reinício ou interrupção da transformação.

Jogadores dentro da área são levados à entrada do campus durante construção/restauração; batalhas em andamento adiam essa movimentação. A região temporária fica protegida contra quebra, colocação e explosões para preservar o conteúdo salvo. Destinos internos da escola ficam indisponíveis enquanto ela está substituída. O minimapa apresenta o terreno do Tártaro durante a Dark Hour.

## Lojas

Os nove estabelecimentos usam a cidade real ao fundo, com elementos de interface próprios. Teclado: setas para selecionar, Enter para confirmar e Esc para voltar; roda do mouse e clique também selecionam. Na Aohige, esquerda/direita ajustam a quantidade antes de confirmar.

- **Aohige:** lista de consumíveis, estoque do jogador, quantidade e preço total.
- **Tatsumi East Police:** categorias, comparação com o equipamento atual, compras e venda de equipamentos guardados. Equipamentos em uso não podem ser vendidos.
- **Mayoido:** equipamento por materiais, cartas e itens. As receitas são uma adaptação ao inventário existente do mod: duas gemas elementais correspondentes por carta básica; equipamentos do catálogo desbloqueado usam gemas Agilao. Os materiais fictícios da prévia não foram adicionados como saldo gratuito.
- **Be Blue V:** membros reais da party, modelo/skin do membro presente no cliente, habilidades atuais e reaprendizagem individual de habilidades naturais, com validação de posse e nível no servidor.
- **Chagall, Mandragora, Game Parade e Escapade:** escolhas e diálogo próprios, preservando custos, efeitos e limitação diária já existentes no mod.
- **Kenneth Arnold:** vitrine, como na proposta.

Compras, vendas, materiais, estoque, disponibilidade e pagamento são verificados no servidor. Tokens de atendimento são renovados após cada operação; pedidos antigos não repetem transações. O protocolo de cidade passa a **3**, portanto cliente e servidor precisam desta versão. Os demais menus mantêm seu desenho e comportamento.

## Verificação e reprodução

Verificações offline: `cityPolishTest`, `cityNativeModelTest`, `cityIntegrationTest`, `personaFramingTest` e circulação usando os volumes exportados. A primeira produz treze imagens diretamente do código de desenho dos menus. São prévias offline; não são capturas do Minecraft.

`tools/city_polish/export.py` converte as geometrias aprovadas e preservadas em `approved/` para os recursos do jogo. `resources.py` gera os modelos e nomes dos materiais criativos. `audit_collision.py` verifica rotas, degraus e todos os pontos de chegada dos dois distritos. `export_art.cjs` usa Playwright/Edge sem janela para exportar somente a decoração das interfaces aprovadas. As amostras HTML em `shop_art/` são fontes gráficas da prévia; não são dados usados nas compras do jogo.

Os blocos são renderizados nos buffers normais do Minecraft, com passes opacos e translúcidos separados, emissão local e luz ambiente normal. Não foram feitos testes dentro do Minecraft nem com shader packs; brilho, reflexos e sombras variam conforme o shader. A falha geral preexistente de `BestiaryAudit` (asserção de dez abas) continua fora do escopo; foram executadas as suítes focadas acima.
