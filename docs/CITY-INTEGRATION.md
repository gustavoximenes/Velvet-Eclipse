# Cidade e Personas — 0.6.45

## Início e navegação

Em um mundo novo, somente o anfitrião do mundo local recebe a escolha **Com a cidade / Sem a cidade**. A escolha fica salva no mundo e não pode ser alterada por convidados. Mundos já utilizados continuam com a experiência anterior. A construção acontece por etapas e retoma após reiniciar o servidor.

Com a cidade, cada jogador recebe a primeira chegada e o ponto de renascimento no quarto do dormitório. Depois disso, reconectar não o teleporta de volta. O minimapa aparece na cidade; **M** abre o mapa com os destinos e a viagem rápida. No Tártaro, M continua abrindo o mapa já existente. Viagens são recusadas durante batalha, cinematografia ou quando o ponto de chegada estiver obstruído.

Clique com o botão direito nas entradas para entrar na escola e no dormitório. As salas de cada ala têm corredores transitáveis. O acesso marcado **TARTARUS**, junto a Gekkoukan, abre durante a Dark Hour; a saída do Tártaro retorna à escola. O relógio da cidade acompanha o Overworld. A porta azul ao lado de Mandragora dá acesso à Velvet Room.

Em servidor dedicado não existe um jogador criador identificado pelo Minecraft. Nesse caso, defina explicitamente o UUID responsável com `-Dvelvet.cityOwner=UUID` na inicialização do Java. O primeiro visitante não ganha essa permissão automaticamente. Cliente e servidor precisam desta versão; o canal `city` usa protocolo 1.

## Áreas e aparência integradas

Esta versão integra os **16 cenários já modelados**: seis exteriores (Gekkoukan, dormitório, Paulownia, Iwatodai, estação e Naganaki), sete ambientes do dormitório e três ambientes escolares. São **25 destinos**, incluindo os nove estabelecimentos. Gekkoukan e sua ala escolar permanecem na mesma dimensão e conectados por entrada e corredores, sem dimensões independentes por sala.

As malhas, cores e texturas vêm dos estudos aprovados. A distribuição dos bairros, os caminhos de ligação e as alas interiores foram adaptados à circulação em Minecraft. Os interiores formam alas conectadas; ainda não representam todos os pavimentos e ambientes canônicos listados no inventário de pesquisa. O dormitório inclui quarto, lounge, cozinha, leitura, comando, banheiros e terraço; a escola inclui sala 2-F, biblioteca e corredor. Esta entrega não deve ser descrita como a cidade completa do jogo original.

São 354 seções nativas de geometria e suportes de colisão em blocos, com alturas de 1/16. A cidade usa uma dimensão própria, sem sobrescrever construções do Overworld. Materiais e luz passam pelo renderizador de chunks do Minecraft; compatibilidade visual e reflexos específicos de cada shader ainda precisam de conferência dentro do jogo.

Os nove materiais urbanos, nove balcões e o acesso ao Tártaro aparecem na aba criativa de blocos, com modelos e nomes. As figuras das Personas continuam na aba de modelos/NPCs.

## Lojas

Quando a cidade foi escolhida, Elizabeth mantém diálogo e recuperação gratuita, mas não vende itens. A restrição também é aplicada no servidor. Sem cidade, seu comércio original continua disponível.

| Estabelecimento | Conteúdo desta versão |
|---|---|
| Aohige Pharmacy | Recuperação de HP e cura de estados |
| Tatsumi East Police | Armas, armaduras, calçados e acessórios; respeita as permissões de estoque |
| Mayoido Antiques | Suprimentos de SP e itens de dano do catálogo existente |
| Be Blue V | Lista os integrantes reais da party; cada jogador confirma sua recuperação de habilidades naturais |
| Chagall Café | Café: recupera 20 SP |
| Mandragora | Canto: registra pontos de coragem e mostra o total no atendimento |
| Game Parade | Treinamento: concede 40 XP ao jogador |
| Club Escapade | Consulta: recupera HP/SP |
| Kenneth Arnold | Vitrine, sem catálogo de compra |

Cada HUD utiliza sua própria ilustração e identificação; os valores das prévias foram substituídos pelos preços reais do catálogo. Os serviços são adaptações para os sistemas existentes do mod. Fusão de equipamentos por materiais, trabalho no café, calendário de jogos e sistema social completo não fazem parte desta integração. Os pontos de coragem ficam persistidos, mas ainda não desbloqueiam Social Links.

As transações verificam saldo, estoque, balcão, distância, dimensão e sessão. Tokens são renovados após cada atendimento, impedindo repetir um pacote antigo. Serviços de atividade são limitados a uma vez por dia; relembrar habilidades só cobra se houver habilidades naturais ausentes.

## Personas

Integra Apsaras revisão 04, Jack Frost 01, Forneus 01 e Chimera 01, com a expressão anterior do leão. Pixie 03 e Elizabeth 05 permanecem integradas.

Geometria, Box UV e atlas aprovados foram mantidos; a convenção de eixos é convertida uma vez para GeckoLib. Os ataques são ajustados à janela de combate de 2,5 s. Apsaras e Chimera têm ataques físicos próprios; Jack Frost e Forneus usam o ataque giratório aprovado também no canal físico, pois não havia outro ataque físico modelado. Jack Frost mantém o giro horizontal e Forneus o giro do corpo.

## Verificação e fontes

Verificações offline: autorização do criador, separação de estoques, compra com yen/inventário reais, importação GeckoLib, conectividade dos destinos e balcões, âncoras de geometria, limites de seção e UVs. Inclui regressões de equipamento, Pixie/Elizabeth e do renderizador do Tártaro.

Não houve execução visual no Minecraft, teste multiplayer conectado ou avaliação de shaders nesta entrega. As verificações automatizadas não substituem esses testes.

O projeto completo inclui wrapper, dependências de compilação, fontes, recursos e arquivos autorais dos modelos. Os scripts `tools/approved_models/build_city.py` (Python, NumPy e Pillow), `audit_city.py` e `import_city_release.py` permitem reconstruir/verificar os recursos sem os diretórios temporários da auditoria. `city/export_shop_art.cjs` usa Playwright em modo headless. O build normal do JAR utiliza os recursos já exportados e não exige essas ferramentas de autoria.

Compilação: Java 17, `gradlew.bat jar sourcesJar --offline`. Verificações focadas: `gradlew.bat cityIntegrationTest cityNativeModelTest equipmentTest approvedModelsTest tartarusNativeModelTest --offline`.
