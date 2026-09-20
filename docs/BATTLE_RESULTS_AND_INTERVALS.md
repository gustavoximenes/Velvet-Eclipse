# Resultados, All-Out Attack e salas de intervalo — 0.6.17

## All-Out Attack

A apresentação agora aproxima o grupo na preparação, muda para a câmera atrás dos jogadores na corrida, usa um piso azul quadriculado em perspectiva, contornos cianos, fragmentos escuros/vermelhos e um clarão no impacto. A finalização possui um painel ciano com o nome do jogador e uma pose com a mão junto ao colarinho, calculada sobre o braço articulado nativo. O modelo padrão ou slim e a skin real de cada participante são preservados.

O dano continua sem elemento, atinge todos os inimigos vivos e é aplicado uma única vez no impacto reservado pelo servidor. A versão sem vitória retorna ao combate; apenas a versão letal exibe a pose de vitória e concede Shuffle Time. Esc abre a pausa do Minecraft; no singleplayer a animação também pausa. A opção de cancelar a pergunta do All-Out agora mostra N, mantendo Esc livre para a pausa.

## Resultados e bloqueio de menus

Ao encerrar a animação final, a HUD RESULT mostra EXP, nível do jogador antes/depois, progresso de EXP, nível/EXP da Persona, recompensa de Persona quando houver e Shuffle Time obtido. Mantém a recompensa anterior de 80 EXP por inimigo; o Guardião concede 240 EXP. Não inventa dinheiro ou itens adicionais: o dinheiro continua sendo obtido pelas recompensas Coins do Shuffle Time.

As recompensas são salvas uma única vez ao apresentar os resultados. O jogador permanece na instância de combate, imóvel e protegido contra dano externo, até pressionar Enter. Em multiplayer cada participante confirma individualmente, sem liberar os demais. Repetir Enter não duplica recompensas. Sair do servidor depois de receber os resultados preserva as recompensas já salvas.

Inventário, criativo, chat, Tab e demais telas ficam bloqueados durante o combate e os resultados. A tela de pausa do Minecraft continua disponível. A interação com blocos, itens, entidades e contêineres também é bloqueada no servidor. Os comandos de batalha continuam na própria HUD.

## Salas de intervalo e transição

Andares 3, 6, 9…249 usam uma planta de três salas alinhadas por um único corredor:

1. Preparação: porta permanente da Velvet Room, que retorna ao mesmo andar.
2. Arena: um Guardião imóvel sobre um selo circular no centro. Não há outras Shadows geradas neste andar.
3. Transição: escadaria para o próximo andar, liberada ao derrotar o Guardião.

Esta primeira versão do subchefe utiliza o modelo de Shadow existente ampliado, identificado como Guardião do Tártaro. Possui quatro vezes o HP da Shadow do andar e cinco níveis adicionais. Ele não persegue os jogadores. A derrota fica salva e compartilhada no mundo; depois da vitória não reaparece. Fugir ou perder permite enfrentá-lo novamente. Os andares comuns mantêm até sete Shadows, e o limite de 250 andares permanece.

Mundos existentes são atualizados ao visitar um andar de intervalo. As novas salas ocupam uma área separada do mesmo andar, evitando misturar paredes antigas com a nova planta. Jogadores nos corredores antigos são transferidos após sair do combate. A geração ocorre em lotes antes do teleporte; os demais andares não são reconstruídos.

## Teleporte

`/tp tartarus floor 3` leva ao terceiro andar. Aceita valores de 0 a 250; 0 é o hall. `/teleport tartarus floor 3` também funciona. Requer permissão de comandos equivalente ao /tp normal. O comando não interrompe combate ou resultados e aguarda a geração antes de transportar o jogador. Os outros usos do /tp são preservados.

## Validação

Auditorias offline focadas no All-Out Attack, malhas padrão/slim, ligação da mão ao colarinho, acesso às salas/portais, bloqueio/liberação da escadaria, persistência dos Guardiões, confirmação independente de resultados, codec de rede e parser do comando com o redirecionamento vanilla de /tp. Prévias de HUD, poses e planta inspecionadas offline. Não houve execução no Minecraft nem teste real com dois clientes.

Instalar a mesma versão no servidor e em todos os clientes: o protocolo principal passou para 11.
