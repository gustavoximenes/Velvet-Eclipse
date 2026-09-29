# Combate aprovado — versão 0.6.36

Menu de combate integrado à HUD LDLib: jogador em corpo inteiro à esquerda, comandos em leque, alvo no centro e retratos/HP/SP à direita. A câmera aplica o enquadramento aprovado e se afasta quando a formação exige; as alternativas consideram o jogador e os inimigos. A câmera da exploração continua independente.

As animações aprovadas de espada, arco, espada grande, punhos, rapieira, arma de fogo, faca, lança e machado passam a renderizar a skin real, incluindo braços slim e as camadas de roupa habilitadas. As mesmas curvas são usadas na exploração H e no combate por turnos. No combate, a aproximação e o retorno continuam sob controle do servidor; o impacto coincide com o quadro de contato de cada arma. No campo, o avanço testa colisões e chão e a entrada em batalha aguarda a recuperação do golpe.

O arco puxa a corda até a mão, solta a flecha e a faz partir do ponto de encaixe. Armas de duas mãos mantêm as duas empunhaduras; punhos apresentam duas luvas. As trilhas acompanham a arma e a arma de fogo tem clarão na boca do cano. Golpes de exploração são informados aos jogadores próximos, e a formação transmite o tipo de arma de cada aliado.

Controles preservados: H para exploração, botões numéricos de combate, Shift para passar 1 More e \ / | para acompanhar a câmera do turno. O seletor do All-Out e a cutscene de despertar permanecem. Céu verde e lua da Dark Hour com shaders preservados.

Cliente e servidor devem usar **0.6.36-approved-combat**: protocolo principal **22**, exploração **3**. Dependências mantidas, incluindo LDLib, Photon, PlayerAnimator e BendyLib.

Validação offline: `approvedCombatTest`, `reloadPresentationTest`, `battleCommandTest`, `multiplayerTest` e `reloadPresentationPreview`. A auditoria nova mede os punhos contra a malha nativa deformada, o contato dos pés, as âncoras das armas, o quadro de impacto e a inclusão da formação na câmera. Não foi executado teste dentro do Minecraft; iluminação, shaders e latência de uma sessão real não são comprovados pela prévia offline.
