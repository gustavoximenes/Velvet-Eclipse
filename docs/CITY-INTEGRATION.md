# Cidade e Personas — 0.6.46

## Início e navegação

Em um mundo novo, somente o anfitrião do mundo local recebe a escolha **Com a cidade / Sem a cidade**. A escolha fica salva no mundo e não pode ser alterada por convidados. Mundos já utilizados continuam com a experiência anterior. A construção acontece por etapas e retoma após reiniciar o servidor.

Com a cidade, cada jogador recebe a primeira chegada e o ponto de renascimento no quarto do dormitório. Depois disso, reconectar não o teleporta de volta. O minimapa aparece na cidade; **M** abre o mapa com os destinos e a viagem rápida. No Tártaro, M continua abrindo o mapa já existente. Viagens são recusadas durante batalha, cinematografia ou quando o ponto de chegada estiver obstruído.

As portas da escola e do dormitório abrem fisicamente. Salas, corredores, escadas e pavimentos fazem parte do mesmo edifício; caminhar pela entrada não teleporta para um cenário separado. O acesso marcado **TARTARUS**, junto a Gekkoukan, abre durante a Dark Hour; a saída do Tártaro retorna à escola. O relógio da cidade acompanha o Overworld. A porta azul ao lado de Mandragora dá acesso à Velvet Room.

Em servidor dedicado não existe um jogador criador identificado pelo Minecraft. Nesse caso, defina explicitamente o UUID responsável com `-Dvelvet.cityOwner=UUID` na inicialização do Java. O primeiro visitante não ganha essa permissão automaticamente. Cliente e servidor precisam desta versão; o canal `city` usa protocolo 2.

## Áreas e aparência integradas

A revisão 2 reconstrói os seis distritos — Gekkoukan, dormitório, Paulownia, Iwatodai, estação e Naganaki — com **265.067 blocos visíveis**. São **27 destinos** interligados por caminhada e nove balcões. As malhas urbanas e seus suportes ocultos foram substituídos por pisos, paredes, portas, vidros e escadas nativos. Uma única superfície ocupa cada célula, eliminando a sobreposição da arquitetura anterior perceptível na Dark Hour. O cenário continua na dimensão da cidade, sem substituir construções do Overworld.

O dormitório possui quatro pavimentos, com quartos dentro da mesma estrutura, recepção/lounge, cozinha, leitura, banheiros, comando e terraço. Gekkoukan possui três pavimentos e cobertura, sala 2-F, biblioteca, sala dos professores, conselho e corredores, unidos por escadas. Paulownia recebeu praça coberta, fonte e interiores de lojas; Iwatodai possui galerias em três níveis; estação e santuário têm acesso por escadas. O minimapa muda para a planta do pavimento ocupado no dormitório e na escola; M mantém mapa geral e viagem rápida.

A distribuição e o mobiliário são uma adaptação em blocos das áreas estudadas. Esta revisão não reproduz cada sala canônica nem a cidade inteira na escala exata do jogo original. Os materiais usam o renderizador normal de chunks; 139 fontes de luz nativas iluminam a construção. Reflexos e bloom dependem do shader pack e ainda precisam de conferência dentro do jogo.

### Mundos que já possuem a cidade anterior

Ao abrir um mundo com a cidade da 0.6.45, a atualização é automática e salva seu progresso. Os jogadores presentes são retirados temporariamente da obra; uma batalha ativa precisa terminar antes de continuar. O processo remove as células que ainda correspondem à planta anterior e as entidades de bloco identificadas como malhas da cidade. Blocos diferentes colocados pelos jogadores são preservados, com aviso da quantidade de conflitos ao concluir; alterações preservadas podem exigir liberar uma passagem manualmente. Recomeçar o servidor não reinicia a obra do zero.

Depois da reconstrução, os visitantes retirados voltam ao quarto e o ponto de renascimento é atualizado. Reconectar após a atualização concluída não força retorno ao dormitório. Mundos sem cidade continuam sem cidade. A planta antiga é mantida apenas como recurso de migração, não como arquitetura gerada.

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

## Enquadramento no menu

Os limites animados são medidos nos modelos reais do GeckoLib. Na ficha de status, a escala é individual: Apsaras e Quimera ocupam aproximadamente 83% da altura da tela, Jack Frost aproximadamente 53%; Forneus ocupa aproximadamente 52% da largura. São alvos de composição baseados nas referências do Reload, ajustados ao canvas de 900 × 500 do mod. Incluem fitas, chifres e cauda. O painel de atributos passa à frente da parte inferior do corpo, como na ficha original, em vez de reduzir todos os personagens ao pequeno espaço acima das barras.

A rotação mantém a escala de referência, reduzindo apenas quando necessário para evitar sair do frame. Cartas pequenas e a revelação de fusão continuam usando o encaixe da sua própria área. As escalas das entidades em combate não foram alteradas. A câmera de apresentação do Forneus também evidencia sua largura.

Referências visuais adicionais: [Apsaras na ficha do Reload](https://www.neoseeker.com/persona-3-reload/guides/Thebel), [Jack Frost na ficha do Reload](https://gameriv.com/all-personas-in-persona-3-reload/), além das capturas de Apsaras, Jack Frost, Forneus e Chimera enviadas pelo usuário. As imagens de terceiros usadas na análise não fazem parte do JAR ou do ZIP de fontes.

## Verificação e fontes

Verificações focadas offline: autorização do criador, estoques e transações reais, importação GeckoLib, arquitetura em blocos, estado persistido da migração, circulação entre os 27 destinos e nove balcões, escadas, plantas do minimapa e enquadramento das 181 Personas em todas as rotações. A auditoria de circulação usa colisões em meios blocos, altura livre de 1,8 bloco e portas abertas por interação; nenhum atalho de teleporte conta como conexão.

Não houve execução visual no Minecraft, teste multiplayer conectado ou avaliação de shader packs nesta entrega. As prévias exportadas usam a planta e os modelos do pacote, com iluminação ilustrativa; camas e água são representadas de forma simplificada no render offline. As verificações automatizadas não substituem testes dentro do jogo.

O projeto completo inclui wrapper, dependências de compilação, fontes, recursos e arquivos autorais dos modelos. `tools/approved_models/build_city.py` chama o novo gerador de blocos; `audit_city.py` audita a circulação. A antiga autoria de malhas fica preservada em `build_city_mesh_legacy.py`, mas não é usada para reconstruir a cidade atual. `import_city_release.py` exporta as Personas aprovadas; `city/export_shop_art.cjs` reconstrói a arte das lojas. O build normal do JAR usa recursos já exportados e não exige Python, Node ou acesso às imagens de referência.

Compilação: Java 17, `gradlew.bat jar sourcesJar --offline`. Verificações focadas: `gradlew.bat cityIntegrationTest cityNativeModelTest personaFramingTest --offline` e `python tools/approved_models/audit_city.py`. Para reexportar os envelopes de animação após editar um modelo: `gradlew.bat personaFramingExport --offline`.
