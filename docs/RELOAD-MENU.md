# 0.6.41 — Menu TAB e exploração

## Alterações

- Integra o menu aprovado: composição azul, pose principal V2 espelhada no enquadramento V6, poses Skill/Item/Equip e Evoker na mão curvada. O retrato usa a skin e o modelo slim/padrão do jogador conectado. A transparência de Item é aplicada à superfície visível, sem somar as faces internas.
- Seleção de Personas antes do inspect anterior. Abrir os detalhes não equipa automaticamente; o inspect, as afinidades, a rotação e o uso dele na fusão foram preservados.
- Skill/Item exibem integrantes reais e permitem escolher um destinatário próximo para recuperação; o servidor verifica grupo, dimensão, distância, estado, recursos e propriedade. A cura conserva a regra de quantidade/custo fora de batalha já usada pelo mod.
- Equip lista equipamentos possuídos com comparação e troca; Bestiário mantém registros, modelos e afinidades descobertas. Party mantém convites, aceite, expulsão/saída e transferências com confirmação, agora no visual azul.
- Sistema mantém opções funcionais do Minecraft, movimento reduzido e seleção de All-Out; o calendário usa o dia do mundo. Não cria salvamentos/rewind fictícios a partir dos dados demonstrativos da prévia.
- Contador da Dark Hour no alto à direita durante a batalha. Retratos da party ficam abaixo; fora do combate o contador permanece à esquerda.
- Pausa real do Minecraft congela a interpolação visual e o prazo do servidor integrado, sem descontar o intervalo ao retornar. Menus que não pausam o mundo, LAN e servidores dedicados continuam contando normalmente.
- Corrida na exploração H recebe **20% a mais de velocidade**, com modificador transitório e sem acúmulo. Caminhada e corrida fora de H mantêm os valores anteriores; sair do modo, atacar ou entrar em batalha remove o bônus.

## Controles

TAB abre/fecha. Setas selecionam, ENTER confirma e ESC volta. Q/E alternam integrantes em Skill/Item/Stats. Persona abre a seleção e depois o inspect antigo; nele Q/E alternam Persona, A/D giram o modelo e ENTER equipa. Party usa Q/E para abas; transferências requerem confirmação.

Clientes e servidor devem usar esta versão. Protocolo do menu: **6** (antes 5); principal 22, exploração 4, fusão 3 e party 2 preservados. Nenhum item ou bloco novo nesta versão.

## Validação offline — 02/10/2026

- `reloadMenuTest`: pausa prolongada/retomada/sincronização, existência do ponto de injeção no servidor integrado, velocidade efetiva/idempotência/remoção, HUD em cinco resoluções, consumo de recuperação, navegação do inspect, alvos de mouse e 14 malhas slim/padrão.
- `multiplayerTest`: aprovado (4.099 verificações existentes de reservas, turnos, seleção de alvos, cura e codecs).
- Shader GLSL compilado e executado em contexto OpenGL oculto; seis retratos nativos compostos com as camadas produzidas pelo desenho Java, incluindo mão/Evoker, pose espelhada e transparência. Os dados demonstrativos são exclusivos da auditoria.
- `equipmentTest`: falha anterior em `element-specific Endure protects correct element`, linha 42 de EquipmentAudit. Reproduzida com o JAR **0.6.40** e o mesmo teste; a lógica de Endure não foi modificada nesta entrega.
- Não houve controle do computador, execução do Minecraft ou teste com shaderpack em jogo. A auditoria offline não confirma equivalência visual in-game.

## Reprodução

JDK 17: `./gradlew.bat reloadMenuTest multiplayerTest --offline --console=plain`.

Malhas: `python tools/reload_menu/export_poses.py` (NumPy e Pillow; usa modelos do próprio projeto e apenas exporta geometria/UVs, sem incorporar skin pessoal).

GPU: compilar `tools/reload_menu/MenuGpuProbe.java` com LWJGL core/GLFW/OpenGL 3.3.1 e seus natives Windows. Executar com argumentos: diretório `src/main/resources/assets/velvet_eclipse`, caminho de uma skin 64x64 de teste e `build/reports/reload-menu`. Não abre janela visível nem controla aplicativos.
