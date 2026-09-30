# Correções de animação e comandos — 0.6.37

## Causas e mudanças

O rig customizado da 0.6.36 não aplicava a escala uniforme 0,9375 usada pelo PlayerRenderer do Minecraft 1.20.1. Além dessa diferença na troca de renderizador, a deformação do Bendy alterava as faces próximas de cotovelos em ângulos fechados. RigidSkinLimb divide a geometria nativa e os UVs em dois segmentos de tamanho fixo, com tampas na articulação. As rotações aprovadas e as posições das mãos continuam sendo usadas, sem converter os quaternions para Euler.

O mesmo rig agora cobre repouso, locomoção e ataques do modo H; o golpe remoto recebido por rede não reinicia a animação do próprio jogador. A silhueta de entrada também usa esse rig, sem reaproveitar o PlayerModel compartilhado com modificadores antigos. A limpeza da animação de Evoker ocorre antes de escolher o renderizador do ataque.

A câmera podia cair no enquadramento genérico centrado nos inimigos após uma colisão. O algoritmo de comandos passa a recompor a orientação em torno de uma âncora do jogador no mesmo espaço virtual 640×360 da HUD. Alternativas de distância e ângulo mantêm essa âncora. Foi removida a segunda colisão que alterava o resultado depois da composição. Em terreno sem nenhum ponto de vista totalmente livre, escolhe o candidato com mais alvos visíveis; não modifica blocos nem promete enquadramento impossível através de paredes.

CommandOutline desenha a casca invertida e expandida da malha do jogador enquadrado, em azul e com profundidade. Não depende do pós-processamento de entidades com glowing. Skins, braços finos e camadas habilitadas do jogador são respeitados. A iluminação final ainda depende do shader ativo.

O servidor antes reservava a Shadow no acerto, mas aguardava a recuperação inteira e mais 32 ticks de entrada. Agora começa a entrada no contato válido da arma ou chegada da flecha, e a transição termina em 10 ticks (0,5 s a 20 TPS). Reservas, colisões e decisão de encontro continuam no servidor. Latência de rede e TPS baixos ainda podem afetar o tempo observado.

## Verificações executadas

- `approvedCombatTest`: nove famílias, modelos padrão/slim, apoio dos pés, âncoras de mãos/armas, tempos de impacto e roster de rede.
- `animationFixTest`: comprimentos e diagonais dos segmentos invariantes entre −110° e 120°, roupas, câmeras em 4:3/16:9/21:9 com paredes simuladas, alinhamento ao menu, limites dos inimigos, geometria invertida do contorno e duração da entrada.
- `reloadPresentationTest`: enquadramento, alvos e HUD responsiva.
- `logicTest`: regras de exploração, reservas e regressão da lógica de batalha.
- Inspeção de imagens renderizadas offline a partir das malhas Java e das câmeras atuais. Essas imagens não são capturas do Minecraft.

Não foi realizado teste dentro do Minecraft, conforme preferência do projeto. A aparência com cada shader e a resposta real de uma sessão multiplayer precisam de validação em jogo. O código e os recursos de céu da Dark Hour, despertar e All-Out não foram alterados nesta versão.

## Entrega

JAR `velvet-eclipse-0.6.37-animation-fix.jar` e ZIP completo das fontes com wrapper, recursos e dependências. Cliente e servidor na mesma versão, protocolo principal 22 e exploração 4. O JAR anterior fica em `mods/velvet-eclipse-versions`; as outras bibliotecas do perfil são preservadas.
