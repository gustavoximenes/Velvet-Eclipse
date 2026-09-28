# 0.6.32-battle-choreography

- Câmera da Evoker em plano de cintura, de frente em três quartos; abertura para incluir a Persona. Procura ângulos alternativos livres quando há paredes. Invocador orientado ao alvo reservado.
- Inimigos aproximam-se, atacam e retornam em ataques físicos. Colisão e apoio limitam o percurso. As 19 Shadows modeladas receberam animação de deslocamento; Sacerdotisa continua parada. Corrigida a disputa de animação dos subchefes do trem.
- Pacotes de efeitos incluem ID e UUID do alvo. O impacto acompanha seus pés interpolados e preserva a última posição após remoção, sem migrar para outro inimigo. Corpos animados de fogo, maldição, vento, gelo, luz, nuclear, psíquico e onipotente junto aos emissores Photon; raios também dispõem do caminho emissivo compatível.
- All-Out Attack: palco em tela inteira, corrida, nuvem com bordas suaves, fragmentos e saída do líder. Cinco finais inspirados em Makoto, Yukari, Junpei, Akihiko e Mitsuru, selecionáveis e salvos em System. Skin standard/slim real e família de arma de cada membro sincronizadas.
- Polígonos da HUD enviados como triângulos em lote, evitando milhares de faixas por nuvem. Protocolo principal 18; bibliotecas e ponte OptiFine/LDLib preservadas.
- Auditorias offline de apresentação, turnos, multiplayer, alvos, poses e geometrias aprovadas. Sem execução do Minecraft; as prévias offline não comprovam a renderização final com shaders.

# 0.6.31-optifine-compat

- Corrige o crash de inicialização `ldlib-common.mixins.json:ModelBakeryMixin`, causado pela troca da assinatura de `Logger.warn` no OptiFine I6 pre6.
- Ponte de compatibilidade de cliente executada antes das injeções: restaura a chamada equivalente com array de argumentos. Sem editar bibliotecas ou remover o OptiFine; sem mudanças de carregamento de modelos, HUD ou shaders.
- Regressão offline na classe real do OptiFine reconstruída e conferida pelo checksum original: 158 verificações, execução da adaptação numa classe de teste, integridade de fluxo ASM e preservação dos demais métodos.
- Mensagem de inicialização deixa de mostrar a antiga versão 0.6.28. Protocolo 17 mantido. Não houve inicialização do Minecraft nesta verificação.

# 0.6.30-reload-presentation

- Despertar sem barras internas ou laterais, incluindo fundo e fades em toda a tela; composição dos personagens preservada em telas 4:3 e ultrawide.
- Item Evoker usa a mesma geometria texturizada da arma aprovada, com renderizador próprio e posições para inventário e mãos.
- Photon emite os efeitos dos elementos, cura, suporte, golpes físicos e invocação azul. Emissores finitos com limite por cliente e limpeza na troca de mundo.
- HUD de combate em LDLib: comandos clicáveis, retratos das skins reais, coluna da equipe, HP/SP interpolados, nome da skill e números de dano/cura.
- Direção de câmera por ação compartilhada entre os participantes; close da Evoker, abertura da invocação e enquadramento do impacto. Animações por jogador e família de arma; flecha visível no ataque básico do arco.
- Personas de combate materializam-se no feixe azul a uma altura fixa. O dano do inimigo é aplicado durante sua animação e a próxima ação aguarda o fim da apresentação.
- Correção da ordem de sorteio dos resultados em ataques de área, mantendo consistência entre ações reservadas e imediatas.
- Protocolo 17. Novas dependências de cliente: LDLib 1.0.52.a e Photon 1.1.17. Auditorias offline focadas aprovadas; sem execução do Minecraft nesta entrega.

# 0.6.23-reload-shadows

- Bestiário no Tab com modelos, encontros, derrotas e afinidades descobertas persistentes por jogador.
- 19 inimigos de Reload separados das Personas, com modelos, animações e itens criativos.
- Shadows patrulham sua sala e olham para o jogador durante a busca.
- Fuga preserva a Shadow original, a semente e a vaga finita; recuperação de reservas após interrupção.
- Dano de perfuração e Skill Card Single Shot.
- Verificação offline focada de combate, fontes de encontros, modelos e Bestiário.

# Velvet Eclipse 0.6.14 — Dark Hour e correções de interface

Igor apresentado de frente no menu de fusões; nível/EXP próprios do jogador com barra de progresso; atalhos no padrão azul/ciano e guia por F1. Seletor multiplayer passa a resolver um só marcador por quadro e centraliza suas instruções.

Primeira implementação da Dark Hour no Overworld, entre 18000 e 23999 ticks. Inclui `/time set dark_hour`, relógio branco/verde, céu e iluminação próprios, manchas vermelhas, congelamento reversível de mobs e caixões. Shadows de encontros naturais só surgem nesse horário e desaparecem ao amanhecer.

Tártaro temporário com blocos próprios, construção animada em camadas, topo no limite do mundo e escadaria de entrada. Adapta a base ao terreno e aparece a até 64 blocos, limitado pelo alcance de chunks e priorizando o anfitrião. `/locate tartarus` e `/locate structure velvet_eclipse:tartarus` mostram a entrada; servidores dedicados podem configurar o dono com `/velvet tartarus owner <jogador>`. A torre acompanha mudanças do jogador prioritário e registra seus blocos para limpeza ao amanhecer/reinício. Personas e efeitos de invocação são excluídos dos caixões por tipo real, inclusive no cliente. Ver `docs/DARK_HOUR.md`. Protocolo de rede 10; nenhuma biblioteca nova.

Validação offline das 11 auditorias. A execução no Minecraft continua dispensada pelo usuário e não foi realizada.

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

- Modelo voxel aprovado, máscara pixelada, casaco, mãos, arma e pés detalhados.
- Idle e ataques mágico/físico integrados, sincronizados ao impacto.
- Zio com raio descendente ramificado e clarão branco/rosa no alvo.
- Habilidades iniciais de Persona 4 e efeitos de suporte de três turnos.
- Comandos `/velvet persona add/remove` e proteção da última Persona.
- Correção do ATTACK descartado por terreno que bloqueava a aproximação.
- Verificações offline, sem teste dentro do Minecraft.

# Velvet Eclipse 0.6.6 — seletor de Persona

Mod para Minecraft 1.20.1 / Forge 47.4.10, Java 17. O seletor fica no canto inferior esquerdo, deixando a arena visível à direita. O retrato mantém o modelo normal/slim, a pose com Evoker e detalhes da skin real em tons de azul, ciano e branco.

## Mudanças

- Painel e retrato menores, alinhados ao canto esquerdo, com cabeçalho, habilidades, custos, descrição e comandos na mesma escala.
- Abertura de aproximadamente 0,3 segundo: painel desliza, retrato aparece e habilidades entram em sequência. As áreas clicáveis acompanham a animação.
- Paleta tonal conserva cabelo, rosto e detalhes da roupa. Camadas externas habilitadas da skin permanecem presentes; a textura original nunca é substituída.
- Retrato usa o passe sem iluminação do Forge com profundidade e fog isolado do mundo. Corrigida a sobreposição das linhas translúcidas em escalas fracionárias.
- Mantidos combate, modelos de Personas, espada, invocação, marcador de alvo, descoberta de fraquezas e protocolo 4 da versão 0.6.5.

## Instalação e controles

Use o JAR 0.6.6 da pasta `compiled Jar`, substituindo a versão anterior do Velvet Eclipse. Reinicie o perfil para carregar a alteração. São necessários GeckoLib 4.8.4, PlayerAnimator 1.0.2-rc1 e Bendy Lib 4.0.0, nas versões Forge para Minecraft 1.20.1. Nenhum outro mod de API foi acrescentado.

Q/E troca Persona; setas escolhem habilidade; Enter seleciona e depois confirma o alvo; Esc ou botão direito volta.

## Fontes e compilação

Projeto-fonte completo, recursos, testes, wrapper e dependências locais. As fontes recuperadas e sua origem estão documentadas em `source-provenance.json`. A entrega é construída das fontes, sem edição binária do JAR.

Com Java 17 configurado em JAVA_HOME:

```powershell
./gradlew.bat check --offline
./gradlew.bat assemble --offline
```

O wrapper usa Gradle 8.8. Em uma máquina sem essa distribuição no cache, a primeira execução do wrapper precisa baixá-la. As dependências Java estão em `dependencies/`; o projeto usa nomes SRG e o plugin Java, sem ForgeGradle/runClient. No Linux, use `bash gradlew`.

A tarefa `check` executa as auditorias de lógica, câmera, postura, Evoker e comandos de combate. Com Python, Pillow e NumPy, `python tools/render_battle_previews.py` gera prévias após os testes. Elas usam skin ilustrativa e geometria real; não são capturas do Minecraft.

## Validação

Auditorias offline aprovadas: 20 combinações de resolução/escala de GUI, transição e áreas clicáveis, preservação tonal/transparência, geometria normal/slim e regressões de batalha. Os 120 combates de referência mantêm o mesmo resultado. O Minecraft não foi aberto para testes; a composição final da GPU e a skin carregada em jogo ainda precisam de conferência visual.

## Publicação de versões

Ao finalizar cada versão, publicar neste repositório as fontes e o JAR compilado em `compiled Jar`, preservando o histórico e os JARs anteriores. Preferências persistentes em `AGENTS.md`. No perfil local `yuxio test`, a versão anterior fica em `velvet-eclipse-versions/0.6.5`. Ver `CHANGELOG.md` para as alterações de combate da 0.6.5.


# Velvet Eclipse 0.6.5 — comandos de combate

- Faixa de olhos durante a invocação confirmada pelo servidor, antes do dano. Câmera frontal e fragmentação a partir do tick 38, junto ao gatilho. Mantidos o giro e a pose do Evoker. A faixa termina no tick 51; o dano de Persona ocorre no impacto dos efeitos, tick 86.
- O servidor reserva uma única rolagem válida sem gastar HP/SP nem aplicar dano antecipadamente. Erros e ataques sem fraqueza não mostram a faixa. Ataques comuns de espada nunca a mostram, mesmo causando fraqueza.
- Ataque comum com aproximação, corte no tick 19 e retorno até o tick 44. Verificação de colisão do corpo antes do movimento; um caminho bloqueado exige outra ação. Golpes finais preservam o encerramento da animação. A espada permanece visível na espera de combate e usa o elemento SLASH, com compatibilidade com as afinidades físicas antigas.
- Seletor de habilidades com painel azul diagonal, retrato do jogador em azul/branco, Evoker na têmpora, custos, descrições, destaque de seleção e troca de Personas por Q/E. Usa geometria normal/slim e uma cópia da textura da skin, sem modificar a skin original.
- Selecione a habilidade ou ATTACK, confira o alvo e confirme com ENTER ou clique. SETAS escolhem habilidades; Q/E troca Persona; ESC ou botão direito volta. O cursor fica livre durante a batalha e é restaurado na saída.
- A espada de espera fica inclinada para fora, para a frente e para baixo; a transição do punho e a volta da câmera após o corte são suaves. A seleção respeita a página de habilidades e os atalhos ficam suspensos enquanto o chat ou outra tela estiver aberto.
- Removidos os painéis fixos de HP dos inimigos. O marcador acompanha o ator selecionado na arena e troca o ícone conforme o elemento. “?” indica elemento ainda não observado; “! WEAK” indica fraqueza conhecida; outros resultados conhecidos não têm aviso extra. Descobertas são persistidas por tipo de inimigo e elemento; erros não revelam afinidade.

## Validação e limites

Compilação Java 17/Gradle 8.8 offline aprovada. Passaram as auditorias de lógica, apresentação, postura, Evoker e comandos de combate. Comparações entre resolução imediata e reservada cobriram 1.920 cenários; os 120 combates antigos mantiveram os mesmos resultados. Foram verificados codecs, custos, cancelamento, colisão matemática da trajetória, tempos de animação, transparência da paleta, 20 resoluções/escalas e geometria Bendy normal/slim.

Não foi aberto Minecraft/Forge. A composição final na GPU, o carregamento da skin e o comportamento da câmera em jogo ainda precisam de conferência visual no perfil. As prévias offline usam skin ilustrativa, não são capturas do jogo atualizado. O formato atual continua sendo uma batalha com um inimigo; o marcador seleciona esse ator.

Não foi instalado novo mod de API. O projeto inclui a biblioteca lwjgl-opengl 3.3.1 já distribuída pelo Minecraft para compilar o acesso à textura. Cliente e servidor precisam desta versão, pois o canal principal passou ao protocolo 4.

A regra de fechamento aos 95% de uso foi registrada em AGENTS.md. O JAR final foi gerado após a conclusão das alterações e da revisão offline.

## Arquivos

JAR: velvet-eclipse-0.6.5-battle-command.jar. Reinicie o perfil yuxio test para carregá-lo.
A versão 0.6.4 permanece arquivada em velvet-eclipse-versions/0.6.4.
O ZIP do projeto contém todas as fontes, recursos, wrapper Gradle, testes e dependências locais. Com Java 17: `./gradlew.bat build --offline`. O projeto usa fontes SRG e o plugin Java, sem ForgeGradle/runClient. Não utiliza edição binária de JAR como fonte de entrega. Consulte source-provenance.json.

SHA-256: `724D1BD5666C240D2145907ACD8345D98D54149A19C1EEBA6F585E8F1905493E`
