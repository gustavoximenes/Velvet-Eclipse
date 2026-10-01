# Velvet Eclipse 0.6.39 — Velvet Room, fusões e invocação

Empacotamento autorizado em 01/10/2026. Minecraft 1.20.1 / Forge 47.4.x. Use a mesma versão nos clientes e no servidor: fusão 3, party 2, principal 22 e exploração 4.

## Velvet Room e invocação

- Entrada com corredor/portas e Elizabeth, ambiente com gradis, sofá, mesa e Igor. A mesma geometria aparece nos menus e na decoração da dimensão. Modelos padrão/slim e skin real do jogador são usados durante as cenas.
- Menu em cinco cartas: normal, especial, busca por resultado, Compêndio e contribuição. Compêndio com sala ao fundo e painel inclinado; herança e resultado final usam `PersonaSheetDesign`, o inspect existente do TAB.
- Busca por resultado considera o nível real de cada contribuição, mantém todas as combinações distintas por resultado e pagina os resultados. Ofertas ainda exigem consentimento e confirmação autoritativa; a fusão só anima após o servidor confirmar o consumo. Respostas grandes usam compressão com limites de tamanho.
- Fusão com Igor: dança breve, alinhamento horizontal, voo de 4 a 5 segundos, impacto na testa, recuo rígido da cabeça e emissão azul na nuca acompanhando sua transformação. Revelação da Persona resultante diante dos gradis/relógio e retorno ao inspect com habilidades herdadas. Pular a cena não repete a transação.
- `/velvet maximum_player <quantidade>` persiste por mundo, padrão 4, intervalo técnico 1–64; limita grupos/admissão em combates do mod. Não altera as vagas do servidor. Raids respeitam também seu próprio teto configurado. Reduzir o limite preserva participantes existentes.
- Cadeiras correspondem ao limite, com ocupação sequencial. Excesso entra em pé com a transição e interage com Igor por botão direito. SHIFT fecha o menu e desmonta; a porta traseira retorna ao mundo. Ao reduzir o limite, cadeiras ocupadas permanecem até serem liberadas; a dimensão não encolhe sobre jogadores.
- Invocação: Persona a 0,64 bloco atrás e 1,10 bloco acima da base do jogador; um círculo de fogo azul no ponto médio, com chamas curvas, partículas e dissipação sincronizada com a sequência de combate. Removido o disparo visual antigo que duplicava o efeito de chão.
- Iluminação: piso, paredes e detalhes emitem luz de nível 15, dimensão com luz ambiente, decoração/NPCs com renderização iluminada. Para OptiFine, correção localizada das sombras após a composição do shader, somente na Velvet Room. O tratamento do céu da Dark Hour foi preservado.

## Poses de espera

- `BattleCommandPose` separa a postura de comandos do primeiro quadro do ataque. A referência é a postura lateral aprovada na prévia 0.6.36, com torso/cabeça/pernas da espada e mãos baixas.
- A lança deixa de usar a guarda de ataque com os braços cruzados no peito. As outras famílias recebem posições de transporte a partir da mesma base; o arco mantém folga até o piso, e o machado tem inclinação própria para não atravessar o chão.
- A orientação parte da formação inimiga. O jogador destacado pode receber uma compensação de até 25° quando a câmera desvia de uma parede, preservando o perfil da prévia. Foi retirada a orientação frontal de 35° usada na 0.6.38.
- Ataques, locomoção de exploração, HUD e câmera de exploração não foram alterados nesta etapa.

Verificações: `commandPoseTest` (183 verificações de postura, padrão/slim, mãos, empunhadura e piso) e `animationFixTest` (14.312 verificações, incluindo enquadramentos com paredes). Renders offline das nove armas em três posições de câmera. Não foi realizado teste dentro do Minecraft.

## Validação

- Compilação das classes Java e auditorias focadas aprovadas antes do empacotamento.
- `velvetReworkTest`: 154.001 verificações geométricas/lógicas; grupo, cadeiras, busca, contribuições, confirmação, herança, rede e círculo de chamas.
- `velvetRoomTest`: 31.582 verificações de recursos, dimensão, cadeiras, colisões e portas.
- `commandPoseTest`: 183 verificações; preservadas as correções anteriores de espera.
- `fusionTest`: 1.787 verificações / 78 pares. `optifineCompatTest`: 26 verificações sintéticas/vanilla da compatibilidade existente.
- `tools/velvet_room/audit_assets.py`: recursos presentes, matrizes finitas, ossos rígidos sem alteração de escala, voo horizontal e nuca ligada à cabeça.
- `tools/velvet_room/LightingProbe.java`: compilação/execução GLSL em contexto GPU oculto na Radeon RX 6600; 98.304 canais de pixels comparados com a curva esperada. Nenhum controle da interface.

## Limites da validação

As imagens de auditoria são renders offline dos recursos integrados, não capturas do Minecraft. O teste do shader executou o GLSL em um contexto GPU oculto e não carregou um pacote de shaders no jogo. A aparência final com Complementary, a migração física de uma sala já visitada e o multiplayer real precisam ser conferidos dentro do cliente. Não se afirma identidade visual in-game antes dessa conferência.

O tratamento após a composição é específico para OptiFine. Outros renderizadores recebem a iluminação física e dos modelos, mas seus pacotes não foram testados. Nenhuma automação de mouse, teclado ou interface foi utilizada.

## Entrega

JAR `velvet-eclipse-0.6.39-velvet-room-rework.jar` e ZIP completo do projeto, incluindo fontes, recursos, ferramentas, Gradle wrapper e dependências locais. Os hashes acompanham a entrega. A versão anterior é preservada em `mods/velvet-eclipse-versions`; somente o JAR deste mod é substituído no perfil de teste.
