# Poses, caminhada e câmera — 0.6.38

## Alterações

A espada normal mantém a curva de ataque da 0.6.37. A faca usa essa mesma base de articulações, com sua arma, alcance e duração próprios, alinhando a pose de contato ao instante de acerto da faca no servidor. Nas demais famílias, os cotovelos passam a flexionar no mesmo sentido nativo da espada; o cálculo evita inverter o eixo do braço e procura uma posição do cotovelo fora do torso. A aproximação de duas mãos à empunhadura usa limites máximos de alcance: o limite mínimo aplicado repetidamente antes podia fazer as mãos saltarem de posição. O trajeto da mão que puxa a corda do arco também foi revisto.

A locomoção do modo H usa balanço oposto de braços e pernas, flexão dos joelhos durante a passagem da perna e transporte da arma a partir da mão real. A cabeça acompanha o yaw/pitch do jogador independentemente da inclinação do torso. Quando parado, o corpo só começa a acompanhar a rotação após ultrapassar 55° de diferença. A aproximação antes de um ataque em batalha compartilha o mesmo cálculo de locomoção.

O contorno azul passa de 0,025 para 0,009 bloco e usa o corpo base. As caixas das camadas externas transparentes antes formavam molduras grossas ao redor do chapéu e das mangas; elas deixam de integrar a casca sólida. As camadas da skin continuam aparecendo no personagem.

A câmera de comandos conserva a âncora do jogador em relação ao menu. O jogador em destaque mantém uma orientação de três quartos em relação ao ponto de vista escolhido, inclusive quando uma parede exige outro ângulo. A busca de distância fica próxima da composição aprovada, entre 0,84 e 1,16 da distância de referência, substituindo o recuo de até 2,6 vezes. Alternativas de ângulo e lente acomodam obstáculos e telas estreitas. Pequenas correções de rede não recalculam a câmera. Mudança de jogador acompanhado, tela ou deslocamento real invalidam o enquadramento; uma obstrução precisa persistir por duas consultas espaçadas antes de provocar nova escolha.

## Validação

- `poseConventionsTest`: sentido e limite da flexão, posição dos cotovelos nas famílias com IK, continuidade temporal, caminhada padrão/slim, apoio dos pés, cabeça independente, contorno discreto e câmera estável por 240 atualizações com pequenas variações de posição.
- `approvedCombatTest`: geometria das nove famílias, padrão/slim, mãos e armas, tempos de impacto, roster multiplayer e enquadramento.
- `animationFixTest`: segmentos sem alongamento, contorno, âncora da HUD, inimigos enquadrados em 4:3, 16:9 e 21:9 com paredes simuladas e duração da entrada.
- `reloadPresentationTest`: HUD, câmeras e apresentação de combate.
- Inspeção de renders das malhas Java e GIF de caminhada. São renders offline com skin de exemplo, não capturas de uma sessão do Minecraft. No mod, a skin vem do próprio jogador.

A curva/classe de ataque da espada normal, recursos, compatibilidade OptiFine/LDLib, céu da Dark Hour, despertar e All-Out foram preservados. Não foi realizado teste dentro do Minecraft, conforme preferência do projeto. A aparência final com shaders, skins com camadas externas opacas e a resposta de multiplayer precisam ser confirmadas em jogo. Não há garantia de enquadramento através de paredes quando não existe um ponto de vista livre.

## Entrega

`velvet-eclipse-0.6.38-pose-camera-fix.jar`, fontes completas com wrapper, recursos e dependências. Protocolos principal 22 e exploração 4 mantidos. Use a mesma versão nos clientes e no servidor. O JAR anterior é preservado em `mods/velvet-eclipse-versions`, mantendo as demais bibliotecas do perfil.
