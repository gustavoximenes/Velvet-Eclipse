# 0.6.35 — Câmera de combate, ataques e Dark Hour

## Câmera e animações

A câmera de comandos parte de trás e ao lado do ombro do jogador ativo, a 1,85 bloco de altura. Ela procura o enquadramento mais próximo que contenha os limites dos inimigos vivos, recuando quando a formação é larga e procurando alternativas quando há obstáculos. A colisão é calculada a partir do jogador. A câmera de exploração H permanece como na 0.6.34.

Ataques passam por preparação, golpe e recuperação. A espada tem antecipação curta, corte rápido, giro do tronco, contrapeso do braço livre e apoio das pernas. As demais famílias preservam seus gestos próprios e recebem o movimento corporal revisado. No combate, a aproximação física começa após a antecipação e o retorno usa passos para trás. Arcos e armas de fogo atacam parados. A puxada do arco termina antes da flecha sair; no combate, a flecha sai no tick 16 e chega no impacto do tick 19. O servidor continua decidindo o dano.

Referência visual: os vídeos de exploração e Shift enviados pelo usuário, especialmente a exploração após 00:42 e o corte da espada próximo de 00:59,7 no primeiro vídeo. Foram inspecionados quadros dos arquivos locais e poses exportadas da geometria usada pelo mod. O jogo continua usando o modelo e a skin do próprio jogador.

## Mira do arco e alerta das Shadows

Com arco equipado e H ativo, um losango marca o ponto de chegada e uma linha pontilhada mostra o caminho da flecha. Verde indica uma Shadow sob a mira; ciano indica o fim do alcance ou um obstáculo. A mira fica no mundo para corresponder à origem real da flecha em terceira pessoa. O cálculo considera altura, parede e alvo mais próximo, com alcance de dez blocos e trajetória reta, como o projétil existente. Não há mais seleção automática de uma Shadow fora dessa linha.

O alerta de interrogação congela a orientação da Shadow por 30 ticks, equivalentes a 1,5 segundo em um servidor a 20 TPS. Depois disso ela começa a virar gradualmente. Alternar entre busca e reconhecimento durante o mesmo alerta não reinicia a espera. A perseguição continua exigindo reconhecimento contínuo.

## Céu com shaders

O renderizador normal da Dark Hour continua disponível. Com OptiFine e shader ativo, uma etapa adicional depois da composição final do shader aplica o céu verde, nuvens e a lua grande usando a profundidade da cena. Pixels com geometria à frente são descartados pelo efeito. O gancho é opcional e não carrega OptiFine como dependência obrigatória.

Os campos e a etapa de renderização foram conferidos no OptiFine 1.20.1 I6 pre6 presente no perfil. Os JARs de OptiFine, LDLib e Photon permanecem originais. A solução não altera a iluminação do terreno definida pelo shader pack. Packs que escrevam profundidade especial no céu podem exigir adaptação; a combinação final no Minecraft não foi testada nesta entrega.

## Validação e instalação

- Câmera em diferentes proporções de tela e orientações, formação com até cinco inimigos, mira e continuidade de poses: `combatPolishTest`.
- Regressões de apresentação, reconhecimento das Shadows, coreografia e correção de inicialização OptiFine/LDLib aprovadas offline.
- GLSL compilado e renderizado em OpenGL com uma janela oculta na Radeon RX 6600: 136.800 pixels de primeiro plano preservados, céu verde e lua presentes. Ferramenta reproduzível em `tools/presentation/ShaderSkyProbe.java`; isso não equivale a um teste dentro do Minecraft.
- Sem novos itens, blocos ou dependências nesta versão. Conteúdo e abas criativas preservados.

Cliente e servidor devem usar **0.6.35-combat-polish**, protocolo **21**, para manter mira e tempos de ataque compatíveis. Entrega com JAR e ZIP completo do projeto. O JAR anterior é preservado em `mods/velvet-eclipse-versions` do perfil `yuxio test`; apenas o JAR Velvet Eclipse é substituído.
