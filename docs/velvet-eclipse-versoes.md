# Velvet Eclipse — histórico de versões

Versão instalada: 0.5.30-hud-render-fix.

Guardar cada versão futura em uma subpasta própria antes de substituir o JAR na pasta mods. Manter somente uma versão do Velvet Eclipse em mods. As três bibliotecas de animação permanecem na pasta mods.

0.5.24: versão original recebida, preservada para retorno.
0.5.25: curvas do braço suavizadas, Evoker renderizada em uma camada ligada à mão e à transformação Bendy, câmera interpolada, transição GeckoLib de 3 ticks e janela de ataque ampliada para a animação terminar. Resolução do dano permanece no tick 108. Proteção contra reinício da sequência em andamento.

0.5.26: HUD em faixas diagonais, menus com áreas de clique compartilhadas, barras suaves, efeitos elementais e de invocação; Thanatos e Wealth Hand remodelados, materiais emissivos e controlador de animação do Shadow. Evoker e câmera da 0.5.25 preservados. JAR anterior arquivado em 0.5.25; novo JAR e fontes alteradas arquivados em 0.5.26.

Validação 0.5.26: compilação Java 17 contra bibliotecas reais; parser/playback PlayerAnimator; bake de oito geometrias e parser de oito animações GeckoLib; verificador JVM; 192 casos de HUD; 75 classes, nenhuma API embutida e nenhuma entrada duplicada. Cópias instalada, entregue e arquivada têm SHA-256 B70CFB8119AA92774FA984510E09C505E1D2679923C339D3114E516CD314F8A7.

Não foi executado um teste completo de combate em cliente Forge. Não há ambiente Forge remoto disponível nesta sessão. A aparência final, multiplayer e interações com outros mods não foram validados em jogo.

0.5.27: Thanatos com malha articulada sobre o esqueleto GeckoLib, proporções e vestes refinadas, caixões suspensos, mãos detalhadas e máscara angular refeita conforme a referência. Última correção preservou as malhas dos outros 31 ossos. Evoker, câmera, HUD e código do combate da 0.5.26 mantidos. Parser/bake das oito geometrias e animações, PlayerAnimator, verificador JVM e validação da malha passaram. Nenhuma sessão de jogo foi iniciada. SHA-256: 80397D5889C1F4EB0590CB24E4FF8424402F6111215CBFFFB2531A903D6F6C59.

0.5.28: menu Tab com nove abas, tipografia Barlow, fundo e seleção animados, retrato com skin real e pose PlayerAnimator interpolada por quadro. Dados e ações validados no servidor; avanços, vínculos com aldeões, calendário do mundo e atalhos para inventário/opções. Todas as classes e assets preexistentes da 0.5.27 preservados. Validação de layout em 90 casos, lógica de recursos, PlayerAnimator a 30/60/144 FPS, codecs de fontes e pacotes reais do Minecraft aprovados. Prévias técnicas com avatar demonstrativo; nenhuma sessão Minecraft executada. SHA-256: 97FA6E3274593AFEF4C1CB184B488CB06A4A1BA687C8D9290D22CC265B1BE4CC.

Dependências mantidas: GeckoLib 4.8.4, PlayerAnimator 1.0.2-rc1+1.20, Bendy-lib 4.0.0.

0.5.29: ficha de Personas com modelo 3D GeckoLib, afinidades, atributos e descrições de habilidades; Enter equipa e X inspeciona a Persona selecionada. Seleção por ID, páginas de habilidades, rotação e transições. Atualização automática não consome o intervalo das ações. 55 casos de layout, seis vínculos de modelos/bake GeckoLib, lógica e relógios de animação aprovados. JAR reconstruído pelo script entregue com todas as entradas idênticas. Assets do Thanatos e combate preservados; sem sessão Minecraft. SHA-256: DF9402DC3A4DDA886357F1E82B9C03E28D770B2ED095B92F252CABF9AC65B8B3.

0.5.30: corrige a orientação dos polígonos da HUD e separa as profundidades do fundo, modelo e textos. Regressão reproduziu 398 triângulos descartados e cortes do Thanatos na versão anterior; verificou 11.618 triângulos e 490.176 vértices transformados. Código de combate/servidor e todos os assets preservados. Sem nova sessão Minecraft. SHA-256: 62C026A07F74DE8F8245EFEDA66860E446D7CEDC7665F2B2C954A8EC2D9588AF.


## 0.6.0 — exploracao e HUD
H ativa exploracao, espada e movimento via Player Animator/Bendy, entrada reservada no servidor e HUD de comandos. Projeto-fonte completo reconstruido. Build/testes offline aprovados; jogo ainda nao validado. SHA256: `270EC6B2F64520015A2A02FB2F70A57884F0516BDDDBBEA9F4CD8757F4ABE1A0`.


## 0.6.1 — camera, modelo do jogador e rosto
Corrige referencia vertical da camera, colisoes da camera de batalha, poses de exploracao, usa modelo/skin reais na transicao e rosto na HUD. Somente vida do inimigo durante habilidades. Testes offline aprovados. SHA256: `22D7D337C4DB4C4EE39AB319C77C6B3601353802E7E6B1C4AB1B5E0683623540`.


## 0.6.2 — comandos, braços e entrada azul
Faixa de comandos abaixo da HUD, braços/cotovelos voltados para a frente e silhueta azul do modelo do jogador. Testes offline aprovados. SHA256: `2829AE0BDB1338045C14B00E0827761169EC4B534050CF9251A9FF34CF1B90BA`.


## 0.6.3 — corrida natural
Espada baixa junto ao quadril, passada e joelhos coordenados, menos oscilação do corpo. Testes offline aprovados. SHA256: `25A9F7871C31D93AB265AFEF9EE1C33A8571001031C0AADECB14D86534E4D3A9`.


## 0.6.4 — Evoker e fraqueza
Espada inclinada para a lateral, giro do Evoker com pose calculada da mão e faixa de olhos somente no acerto confirmado de fraqueza. Protocolo 3. Testes offline aprovados. SHA256: `ABB4798755DCE9AE5D16E34417170B738DBD84BD3C8923FE146BFBB1CBF42B02`.

## 0.6.5 — comandos de combate
Faixa antes do impacto, quebra no gatilho, espada com aproximação/retorno, seletor de Persona e marcadores de afinidade. Protocolo 4. Testes offline aprovados. SHA256: `724D1BD5666C240D2145907ACD8345D98D54149A19C1EEBA6F585E8F1905493E`.

## 0.6.6 — seletor de Persona no canto
Menu menor no canto esquerdo, paleta tonal da skin, abertura animada e correção de faixas em escala fracionária. Fontes e JAR publicados no GitHub. Testes offline aprovados. SHA256: `9A84F37F7E86F4238DE1985A8624FCCA1A5F1F19C5E5C0830FE784670F40C6A9`.

## 0.6.7 — Izanagi voxel
Modelo voxel aprovado, idle e ataques, comandos add/remove, habilidades iniciais de Persona 4, suporte funcional, raio descendente e correção do ATTACK bloqueado por terreno. Testes offline aprovados. SHA256: `6BD5551A4D818ACBD26BCBF28574FBBF6992BE0020EA7DB2ACCA66A2B0B71C29`.

## 0.6.8 — correção do Izanagi
Conversão da pose/animações para GeckoLib e remoção da sobreposição das faces. SHA256: C0208E8B67D22C934DE68747A7BE895907F1ED23FE3574FD3A3434DF63568D4D

## 0.6.9 — combates em grupo
Até cinco inimigos, multiplayer cooperativo, alvo aliado, Zio. SHA256: 7EB4DA3C6D279C0524B23B50F57FE2A4F3B60DA8843E985D2CF2DEF510B0D6B1

## 0.6.10 — táticas
1 More, Baton Pass, All-Out Attack sem elemento e VFX próprio de Zio. SHA256: 0BDC6E0A90CF348285F53DD971E900A748306B76C12F585C9202923B4279B192

## 0.6.11 — Velvet Room
HUD exclusiva do All-Out, poses corrigidas, chave/porta voxel, dimensão e assentos. SHA256: A1BA9D0388B3952CB47263B8356DFAB3E7E04D92D5F777A4F4DC8C1CCC05B52F

## 0.6.12 — fusão e Igor
Igor sentado na Velvet Room, pergunta de fusão, modos de duas Personas, busca por resultado, roster possuído, prévia com ficha e modelo 3D, nível próprio do jogador e transação atômica. SHA256: 398EC49186F3834780979B1125A3460FB393DEF538A2D6A2FB8558E1D00C91B7
