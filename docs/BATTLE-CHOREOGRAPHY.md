# Apresentação de combate — 0.6.32

## Uso

Em **TAB → System**, escolha uma das cinco linhas **All-Out / nome** e pressione **ENTER**. A prévia usa a skin do jogador. O estilo é salvo no servidor, permanece após reconectar ou morrer e acompanha quem iniciou o All-Out. A equipe inteira recebe o mesmo estilo e a família de arma de cada participante.

Os estilos são adaptações das referências de Makoto, Yukari, Junpei, Akihiko e Mitsuru para o corpo standard/slim do Minecraft. Não substituem a skin por um personagem do jogo. A sequência inclui formação, corrida para o centro, nuvem de impactos, fragmentos, saída do líder e quadro de vitória com cor e gesto específicos. O quadro final só aparece quando o ataque derrota todos os inimigos; caso contrário, retorna ao combate. Dano continua reservado e aplicado uma única vez no servidor.

## Câmeras e movimento

A tomada da Evoker mantém distância de aproximadamente três blocos em frente e ao lado do invocador, olhando para a altura da cintura/peito. O corte seguinte amplia o quadro para incluir a Persona atrás. O personagem se orienta ao alvo reservado. Quando a colisão comprime a tomada, a câmera procura outros ângulos com espaço livre; paredes continuam bloqueando a câmera.

Ataques físicos de inimigos usam um percurso de ida, impacto e volta. As 19 Shadows modeladas têm um ciclo de deslocamento próprio. Obstáculos e bordas limitam a aproximação; não invalidam o ataque por turnos. Magias e projéteis permanecem à distância. A Sacerdotisa mantém a regra de permanecer parada. Os subchefes do trem não têm mais a pose de ataque cancelada pelo controlador da raid.

## Efeitos

O servidor envia ID, UUID e coordenadas precisas de cada alvo atingido. Os emissores e a geometria principal seguem a posição interpolada desse alvo. Se uma morte remove a entidade, o efeito termina na última posição conhecida; a reutilização do ID numérico não o prende a outra entidade.

Photon continua responsável pelos emissores, faíscas e fumaça. Uma camada emissiva própria desenha também a forma principal das magias, para que ela não dependa exclusivamente da passagem de partículas do Photon. São anéis e chamas de Agi, lâminas vermelhas de Eiga, raio de Zio, espirais de Garu, cristais de Bufu, colunas de Kouha, órbitas de Frei, ondas de Psi e explosão de Megido, com escala por potência. A camada usa o caminho compatível com OptiFine quando detectado. Instâncias têm duração e quantidade limitadas, e são limpas na troca de mundo.

## Referências observadas

- [Combate de Persona 3 Reload](https://www.youtube.com/watch?v=5-OXq5NfnKA): invocação em plano mais aberto, Persona atrás do invocador, flash e fragmentos azuis; cortes entre ator e alvo.
- [Finais de All-Out Attack](https://www.youtube.com/watch?v=BXEHr3QFmQI): saída da nuvem, gesto individual e quadro gráfico colorido. Os cinco estilos implementados são adaptações, não reprodução quadro a quadro.

## Instalação e validação

Cliente e servidor devem usar **0.6.32-battle-choreography**, protocolo principal **18**. LDLib 1.0.52.a, Photon 1.1.17 e demais dependências anteriores permanecem nas mesmas versões. A ponte de compatibilidade de inicialização com OptiFine da 0.6.31 foi preservada. Nenhum item ou bloco novo nesta versão.

Verificações offline executadas: `battleChoreographyTest`, `reloadPresentationTest`, `tacticalTest`, `battleCommandTest` e `multiplayerTest`. Cobrem alvos separados em ataques de área, remoção/reutilização de entidades, precisão de coordenadas, colisão da câmera, poses sem saltos, salvamento de estilos, armas por participante, modelos standard/slim e geometria finita dos efeitos. Os 19 ciclos de deslocamento também foram conferidos contra seus esqueletos.

As prévias em `build/reports/choreography` são produzidas pela geometria Java com uma skin ilustrativa; não são capturas do Minecraft. Para regenerá-las: execute `battleChoreographyTest` e `python tools/presentation/choreography_preview.py` (Pillow e NumPy). Não houve teste dentro do Minecraft, conforme a dispensa vigente. A composição final com OptiFine/shaders e a fluidez em multiplayer ainda precisam ser observadas em jogo.
