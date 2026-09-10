# Velvet Eclipse 0.6.4 — espada, giro do Evoker e faixa de fraqueza

## Alterações

- A espada de exploração foi inclinada para fora, na lateral direita do jogador. O punho combina inclinação lateral e vertical, diminuindo a direção para trás. A postura de golpe continua separada e interpola para o corte frontal. A corrida e a câmera da 0.6.3 foram preservadas.
- Nova sequência de invocação usando o jogador real: saque junto ao quadril, extensão lateral do braço, uma volta completa do Evoker entre os ticks 10 e 20, aproximação à têmpora, breve recuo e retorno por fora do ombro. Mantidos os tempos de disparo e de aparição da Persona.
- EvokerPose calcula ombro e cotovelo para uma trajetória contínua da mão. EvokerPlayerAnimation usa Player Animator na prioridade 900 e a mesma contagem de tempo do prop, incluindo quadros parciais. A dobradura segue a Bendy existente. Não foi adicionada API.
- A orientação do prop acompanha a deformação real da mão. O modelo slim tem a altura de ombro e o centro de mão próprios. A conversão ZYX foi calculada explicitamente porque o helper da JOML 1.10.5 local apresentava um sinal incorreto no denominador de X em rotações combinadas.
- Removida a faixa/flash que antes era disparada em toda invocação nos ticks 37–45. Agora ela é acionada somente pelo evento WeaknessHitS2C, enviado após o servidor resolver uma habilidade do jogador que realmente cause dano na fraqueza. Erros, críticos sem fraqueza, absorção, reflexão e anulação não disparam o efeito. Um golpe final também pode dispará-lo.
- A nova faixa atravessa horizontalmente a tela com entrada rápida, recortes diagonais, acentos ciano, partículas e dupla exclamação contornada. Os olhos são um recorte ampliado da skin do jogador, incluindo a camada de chapéu. O cenário continua visível acima e abaixo. Não há escurecimento de tela inteira em invocações comuns.

## Momento do efeito e compatibilidade

A faixa aparece no resultado confirmado do ataque. As regras atuais resolvem o dano ao final da sequência, no tick 108; esse tempo foi preservado. Não se prevê uma fraqueza antes de saber se o golpe acertou. A vida, SP, dano, turnos e One More mantêm o comportamento anterior.

O canal principal passou ao protocolo 3 para incluir o evento de fraqueza. Em multiplayer, cliente e servidor devem usar esta mesma versão. No mundo local, ambos são carregados pelo mesmo JAR.

## Validação

Java 17 / Gradle 8.8: compileJava build aprovado. Testes offline: 1.902 de lógica, 16.015 de apresentação, 3.544 de postura/estilo e 4.421 do Evoker/fraqueza. Incluem deformação real da Bendy, modelos nativos normal/slim, arma presa à mão, alinhamento da boca do cano com a têmpora, limite de dobra do cotovelo, giro completo e expiração/deduplicação do efeito.

Foram exercitados os resultados de todas as afinidades, erros, críticos, mortes e ação seguinte. Os 120 combates deterministas mantiveram os mesmos estados, mensagens, HP/SP e One More da 0.5.30. A comparação preserva o formato antigo de saída; o novo indicador cosmético weaknessHit é testado separadamente.

As prévias inspecionadas usam a geometria Minecraft e Bendy offline. A prévia da faixa usa olhos ilustrativos; no jogo o desenho acessa a textura real do jogador. Não foi iniciado Minecraft/Forge nem validada a composição final na GPU. As imagens não são capturas do jogo atualizado.

## Instalação e código-fonte

Reinicie o perfil yuxio test para carregar velvet-eclipse-0.6.4-evoker-weakness.jar. A 0.6.3 permanece arquivada em velvet-eclipse-versions/0.6.3. A pasta 0.6.4 contém JAR, notas, relatório de validação e projeto-fonte completo.

O ZIP inclui fontes, wrapper e dependências locais. Com Java 17: `.\gradlew.bat compileJava build`. Saída em build/libs. O projeto compila todas as fontes SRG com o plugin Java do Gradle, sem editar binariamente um JAR anterior. Não inclui ForgeGradle/runClient. Consulte source-provenance.json para a origem das fontes recuperadas.

SHA-256 do JAR: `ABB4798755DCE9AE5D16E34417170B738DBD84BD3C8923FE146BFBB1CBF42B02`
