# Velvet Eclipse 0.6.27 — Arsène e Orpheus

Modelos aprovados integrados com idle e ataques, progressão de habilidades, efeitos próprios de Eiga/Agi e migração de Ember para Agi. [Habilidades, comandos e compatibilidade](docs/ARSENE-ORPHEUS.md).

# Velvet Eclipse 0.6.26 — Permissões e Contrato de Igor

Loja em etapas com Permissões de Compra, prêmios únicos do chefe do trem e dos baús fixos, capacidade inicial de duas Personas ampliável até oito, onze abas criativas e 1 More corrigido para golpes letais. [Uso, migração de saves e estoque por andar](docs/PURCHASE-PERMITS.md).

# Velvet Eclipse 0.6.25 — Party e expedição

Party, compartilhamento de itens/yen/mapa, flecha visível, cura com Elizabeth, itens de dano, ajuda F1, Dark Hour+ e ajustes de fuga/HUD. [Notas](docs/PARTY-EXPEDITION.md). A loja desta versão foi substituída pela progressão por permissões da 0.6.26.

# Velvet Eclipse 0.6.23 — Inimigos de Reload e Bestiário

19 inimigos modelados separadamente das Personas, Shadows móveis na sala e preservadas após fuga. Bestiário no Tab com modelos, encontros, derrotas e apenas afinidades descobertas. [Uso, referências e validação](docs/SHADOWS-RELOAD.md).

# Velvet Eclipse 0.6.22 — Shadows e encontros

Shadows com visão, busca e perseguição, aberturas por posicionamento, grupos de 1–5 inimigos, variante vermelha rara e recompensas em yen. Uma Shadow por sala; contador à esquerda e minimapa à direita. [Uso e validação](docs/SHADOW_ENCOUNTERS.md).

# Velvet Eclipse 0.6.21 — Mapa e exploração

Minimapa expansível por M, Shadows finitas e configuráveis, afinidades descobertas por acerto, fuga com chance de falha, chefes do trem fortalecidos e Dark Hour de 30 minutos com contador e saída automática. [Uso, comandos e validação](docs/TARTARUS_MAP.md).

# Versão 0.6.20 — Operação Monorail

Raid pela porta Monad do andar 25: trem próprio, lobby, diálogos por ordem de entrada, dois subchefes e a Sacerdotisa modelada. Timer de cinco minutos, gelo em área e fraqueza variável. Limite padrão de cinco jogadores; comando `/velvetraid maxplayers 1..12`. [Uso e validação](docs/MONORAIL_RAID.md).

# Versão 0.6.19

Correções de guardiões após teleporte e da rotação da porta de Monad. Salas de transição em todos os múltiplos de 25, inclusive nos andares já visitados. Aba criativa automática e itens de colocação de terminal, tesouro e porta. [Notas](docs/TARTARUS_FIXES_019.md).

# Versão 0.6.18

Terminais descobertos por jogador, guardiões a cada 10 andares, transições a cada 25, tesouros e renovação dos mapas comuns por Dark Hour. Proteção contra edição do Tártaro e Esc de combate corrigido. [Notas e validação](docs/TARTARUS_TERMINALS.md).

# Versão 0.6.17

All-Out Attack com novo enquadramento, arena e pose final; resultados de batalha com confirmação por Enter; bloqueio de menus em combate; salas de preparação, Guardião e transição a cada três andares. Teleporte: `/tp tartarus floor 0..250`. Veja [detalhes e validação](docs/BATTLE_RESULTS_AND_INTERVALS.md).

# Velvet Eclipse 0.6.16 — Shuffle Time

- Shuffle Time acumulável após vitória com All-Out Attack, HUD de disponibilidade e abertura por P.
- Cartas animadas de Persona, Copas, Coins, Wands e Swords, com recompensas por andar e modelos 3D das Personas.
- Carteira própria em yen no menu Tab, 19 Skill Cards utilizáveis e todos os blocos do Tartarus na aba criativa.
- Portais de retorno fora dos corredores, com reparo dos andares existentes, e Igor voltado para a frente na prévia de fusões.

Consulte [Shuffle Time: uso e validação](docs/SHUFFLE_TIME.md).

# Velvet Eclipse 0.6.15 — Tartarus

- Torre apoiada no solo, nivelamento reversível do terreno e fechamento dos segmentos.
- Portal preto para uma dimensão própria, hall com escadaria e relógio, porta permanente da Velvet Room e saída para o mundo.
- Andares procedurais 1–250, escadarias entre andares e sete vagas de Shadows por andar, compartilhadas em multiplayer, com reposição e progressão de atributos.
- Portas criadas pela Velvet Key duram 30 segundos. Portas fixas permanecem.

Veja [Tartarus: uso e validação](docs/TARTARUS.md). Auditoria focada `tartarusTest` e compilação Java 17 concluídas; teste dentro do Minecraft a cargo do usuário.

# Velvet Eclipse 0.6.14 — Dark Hour

- Igor voltado para o jogador no menu de fusões; painel próprio de nível/EXP, barra de progresso e ajuda por F1.
- Seletor de alvos com um único marcador e uma única camada de instruções, respeitando o dono do turno em multiplayer.
- Dark Hour no fim do ciclo noturno: relógio animado, céu verde, lua amarelada, caixões e geração de Shadows restrita ao período.
- Tártaro com surgimento em camadas, escadaria e entrada, atingindo o limite de altura do mundo. Aparece perto do jogador, dentro do alcance de chunks, priorizando o anfitrião.

Teste com `/time set 17800` ou `/time set dark_hour`; localize a torre com `/locate tartarus` ou `/locate structure velvet_eclipse:tartarus`. Servidores dedicados podem definir `/velvet tartarus owner <jogador>`. Consulte [comportamento, escopo e validação](docs/DARK_HOUR.md).

# Velvet Eclipse 0.6.11 — Velvet Room

- O convite All-Out Attack oculta a HUD de combate e as sobreposições comuns. A interface permanece oculta durante a confirmação e a sequência; retorna ao cancelar ou ao terminar o ataque. Cliques durante a confirmação não acionam opções encobertas.
- Formação inicial mais preparada, braço de vitória estendido com dobra menor e novo enquadramento: a cabeça inteira permanece visível nas proporções 4:3, 16:9 e ultrawide. A sequência continua usando o modelo standard/slim e a skin real de cada jogador.
- Aba criativa **Velvet Eclipse** com a **Velvet Key** e os blocos da sala. Chave própria em cuboides, emblema pixelado, haste e dentes geométricos conectados.
- Com a chave na mão, use o botão direito diante de chão firme e dois blocos de altura livres. Uma porta azul aparece à frente. Clique com o botão direito na porta para abri-la e atravesse.
- A porta leva à dimensão própria `velvet_eclipse:velvet_room`: salão azul, piso e molduras, grades ornamentais, mesa e cadeiras. A construção é salva no mundo e não sobrescreve a sala a cada entrada.
- O jogador chega sentado em uma cadeira livre, com câmera por trás e ao lado. Há cinco lugares para visitantes; se todos estiverem ocupados, a entrada permanece possível em pé. **SHIFT** levanta; o botão direito numa cadeira permite sentar novamente.
- A porta atrás das cadeiras retorna ao mundo e às proximidades da entrada de origem de cada jogador. A saída procura espaço livre com chão e evita água e o próprio portal. Assentos vazios são removidos; destruir uma cadeira libera seu ocupante.
- Encontros e geração automática de Shadows ficam suspensos dentro da Velvet Room. Esta versão prepara o espaço para fusões futuras, sem adicionar NPCs ou um novo sistema de fusão.
- Izanagi, suas animações aprovadas, VFX de Zio, fraqueza/cut-in e regras de batalha anteriores preservados. Cliente e servidor devem usar a 0.6.11 (protocolo 7, por causa dos novos registros e da dimensão).

## Verificação

As nove auditorias offline passaram. A auditoria da Velvet Room inclui 17.632 verificações: dados da dimensão, 22 modelos, piso/teto completos, assentos, 32 variantes da porta, passagem de um jogador de 0,6 × 1,8 bloco pelas duas dobradiças e geometria real standard/slim das poses. As oito auditorias anteriores também passaram. Prévias da sala, da chave e da porta foram renderizadas a partir da planta e dos JSONs de produção.

A execução dentro do Minecraft foi dispensada pelo usuário. Não houve teste de teleporte numa partida nem sessão real com vários clientes nesta revisão; as verificações offline não substituem esses testes. O carregador completo Forge depende do launcher e de suas transformações de acesso e não foi executado pelo teste isolado de recursos.

A regra de fechamento antecipado por uso da sessão foi revogada: todas as correções e verificações foram concluídas antes de gerar o JAR final.

# Velvet Eclipse 0.6.10 — 1 More, Baton Pass e All-Out Attack

- Marcador preso ao alvo confirmado pelo servidor e oculto durante o close de invocação, sem selecionar o próprio jogador pelo mouse durante a animação.
- Zio usa malha e shader emissivo próprios: raios brancos/lilás ramificados e impacto branco/rosa, sem partículas nativas do Minecraft. Sem dependência adicional.
- 1 More tem aviso animado e bloqueia a troca de Persona, inclusive por comando. SHIFT abre o seletor de aliados para Baton Pass; o receptor recebe +50% de dano na ação bônus, multiplicador independente dos buffs comuns. Não é possível devolver o bônus a quem já participou da cadeia. O turno regular do receptor é preservado.
- O primeiro acerto de fraqueza derruba o inimigo, com estrelas amarelas orbitando. Apenas um segundo acerto da mesma fraqueza antes da recuperação faz perder a próxima ação.
- Todos os inimigos vivos caídos liberam o convite All-Out Attack: ENTER confirma, ESC cancela. O ataque não possui elemento, atinge todos os inimigos do combate e não causa dano aos aliados. Não consome HP/SP.
- Transição azul, formação com o iniciador no centro e modelos/skins reais da equipe, corrida para o centro e nuvem de golpes. Só uma vitória completa exibe a finalização e a pose de vitória; sobreviventes retornam diretamente ao combate. Entrada de jogadores suspensa durante a sequência.
- Modelo, textura e animações aprovados do Izanagi preservados. Cut-in continua condicionado à fraqueza de ataques de Persona.
- Cliente e servidor precisam desta mesma versão: protocolo 6.

Validação: oito auditorias offline, incluindo 11.091 verificações novas de combate, codecs e continuidade das poses; regressões anteriores preservadas. Prévias de HUD geradas do código. O shader também passou por compilação, link e renderização em contexto OpenGL oculto na Radeon RX 6600, incluindo núcleo, halo e transparência. Não houve execução dentro do Minecraft nem sessão real com dois clientes.

# Velvet Eclipse 0.6.9 — combates em grupo

- `/velvet battle demo` mantém um inimigo. `/velvet battle demo 1` até `/velvet battle demo 5` define a quantidade; valores fora desse intervalo são rejeitados.
- Cada inimigo tem identidade, HP, conhecimento de afinidades e alvo próprios. Setas esquerda/direita ou cima/baixo alternam os alvos; ENTER confirma. Também é possível selecionar e confirmar pelo mouse.
- Jogadores próximos veem uma nuvem de fumaça escondendo os inimigos. A até 9 blocos do centro do encontro aparece o convite: **J** entra, **N** dispensa até se afastar e se aproximar novamente.
- Ao entrar, o jogador ocupa um espaço livre ao lado do grupo e entra no fim da ordem existente. Há uma única sequência de turnos e uma única resolução de dano. A entrada é recusada se não houver posição segura próxima. Sair/fugir/desconectar remove somente aquele jogador; o restante da equipe continua.
- HP/SP e recompensas são persistidos para cada participante. Vitória concede 80 EXP por inimigo a cada participante que permaneceu na batalha. Todos precisam usar esta mesma versão (protocolo 5), inclusive o servidor.
- Buffs e curas abrem o seletor de aliados. Dia cura 50 HP do aliado escolhido; Media cura 40 HP de todos os aliados vivos. Sylphid possui Dia no nível 3 e aprende Media no nível 6. O conjunto inicial de habilidades do Izanagi foi preservado.
- Zio e outros ataques elétricos recebem raios brancos com halo lilás, convergindo sobre o alvo, e uma estrela de impacto branca/rosa. O efeito é sincronizado para os participantes, inclusive em ataques de área.
- O modelo, textura e animações aprovados do Izanagi foram preservados. A regra do cut-in continua exclusiva de ataques de Persona que acertam uma fraqueza.

Validação: sete auditorias offline, incluindo 4.099 verificações de grupos, 400 reservas de ataques em área, seleção de aliados, comandos e serialização de rede, além das regressões anteriores. Não houve teste dentro do Minecraft nem sessão real com dois clientes; essa validação prática ainda precisa ser feita no jogo.

# Velvet Eclipse 0.6.8 — correção do Izanagi

Correção da conversão entre as coordenadas da prévia aprovada e o GeckoLib: pivôs, rotações dos ossos/cubos e canais das animações preservam a pose original. Isso corrige a posição das mãos, pernas e arma no idle e nos ataques mágico/físico.

As superfícies planas do rosto ganharam uma separação mínima de profundidade, preservando os retângulos da textura. Elimina a disputa entre faces sobrepostas na máscara e nos olhos. A textura aprovada e o desenho do personagem foram preservados.

Validação offline com a geometria e os keyframes carregados pelo GeckoLib; prévias geradas a partir desses dados. Não houve teste dentro do Minecraft. Cut-in, regras de fraqueza, habilidades e combate continuam com o comportamento da 0.6.7.

# Velvet Eclipse 0.6.7 — Izanagi voxel

Minecraft 1.20.1 / Forge 47.4.10 / Java 17. Dependências: GeckoLib 4.8.4, PlayerAnimator 1.0.2-rc1 e Bendy Lib 4.0.0.

## Izanagi

O modelo aprovado substitui integralmente a malha arredondada anterior: 123 cuboides/planos nativos, 34 ossos e atlas pixelado de 512×512. Máscara segmentada com pupilas internas, casaco longo contínuo com forro vermelho, mãos articuladas, pés estreitos, fitas e lâmina presa à mão. Os olhos têm máscara emissiva.

O menu usa o idle de 4,8 segundos. A animação mágica recolhe e abre os braços; a física prepara um corte horizontal com apoio das duas mãos. As poses aprovadas foram mantidas, com tempos ajustados ao impacto do combate. Arma, casaco e fitas acompanham os ossos. A escala global preserva o atlas e é compartilhada pelo menu e pelo mundo.

Zio mostra raios ramificados brancos com halo lilás descendo sobre o inimigo, seguidos de um clarão branco/rosa e faíscas no impacto. O efeito não cria uma entidade de relâmpago que cause fogo ou dano adicional.

## Comandos e habilidades

- `/velvet persona add izanagi`: adiciona Izanagi à seleção.
- `/velvet persona equip izanagi`: equipa fora de combate; durante a batalha, Q/E troca Persona no seletor.
- `/velvet persona remove izanagi`: remove fora de combate. Outra Persona é equipada se necessário; a última Persona não pode ser removida.
- `give` continua disponível; add/remove oferecem sugestões de IDs.

| Habilidade | Nível | Custo | Efeito |
| --- | --- | --- | --- |
| Zio | 1 | 4 SP | Dano elétrico leve a um inimigo |
| Cleave | 1 | 5% HP | Dano físico leve a um inimigo |
| Rakukaja | 1 | 12 SP | Aumenta a defesa de um aliado por 3 turnos |
| Rakunda | 3 | 12 SP | Reduz a defesa de um inimigo por 3 turnos |
| Tarukaja | 5 | 12 SP | Aumenta o ataque de um aliado por 3 turnos |

Conjunto inicial baseado em [Persona 4](https://persona4.wikidot.com/persona), conferido com a [tabela de Izanagi](https://aqiu384.github.io/megaten-fusion-tool/p4g/personas/Izanagi). O cálculo de dano e EXP continuam adaptados ao mod. Cleave do Izanagi usa ID `izanagi_cleave`, preservando o balanceamento das Personas antigas. A família Mazio/Zionga/Mazionga/Ziodyne/Maziodyne permanece no registro, sem ser concedida automaticamente a este Izanagi inicial.

Rakukaja/Tarukaja afetam o jogador no combate atual de um aliado: selecionar a habilidade confirma esse alvo. Rakunda usa a seleção de inimigo. Buffs não causam dano, não descobrem fraquezas e não ativam cut-in. Repetir renova a duração sem acumular multiplicadores; Rakukaja e Rakunda se cancelam. Ataque recebe ×1,33; defesa recebe ×1,33 ou ×0,66 no cálculo de dano. A duração avança com os turnos do alvo, sem consumir um turno extra em ONE MORE.

## Correção do ATTACK

Terreno bloqueado ou falta de apoio no chão não descarta mais um ataque válido. O personagem avança pelo trecho livre e executa o golpe. Quando o terreno encurta a aproximação, o efeito de corte alcança o alvo. Obstáculos surgidos durante a animação interrompem o deslocamento, preservando o impacto reservado. Mantidos dano cortante, impacto no tick 19, retorno e ausência de cut-in para ataques normais, inclusive quando acertam fraqueza.

## Compilação e autoria

```powershell
./gradlew.bat check --offline
./gradlew.bat assemble --offline
```

JAVA_HOME deve apontar para Java 17. Fontes, recursos, testes, dependências locais e wrapper Gradle 8.8 incluídos. O primeiro uso do wrapper precisa baixar Gradle se não estiver no cache. Projeto com nomes SRG e plugin Java, sem ForgeGradle/runClient. A origem das fontes recuperadas está em `source-provenance.json`.

Com Python, Pillow e NumPy, `python tools/izanagi/export_model.py` gera os recursos finais. `python tools/izanagi/render_study.py --animate` gera prévias de autoria (tempos anteriores à adaptação ao combate). Padrão de modelagem em [tools/izanagi/MODELING.md](tools/izanagi/MODELING.md).

As verificações offline cobrem leitura real da geometria pelo GeckoLib, cuboides, animações, habilidades iniciais, suporte, comandos, aproximação bloqueada, golpes em grupo e regressões anteriores. Não houve teste dentro do Minecraft. As prévias são renders dos recursos, não capturas do jogo.

Fontes e JAR de cada versão são publicados na pasta `compiled Jar` de https://github.com/gustavoximenes/Velvet-Eclipse. A instalação no perfil `yuxio test` arquiva a versão anterior e verifica os hashes. Reinicie o perfil para carregar o novo JAR; cliente e servidor devem usar a mesma versão.
