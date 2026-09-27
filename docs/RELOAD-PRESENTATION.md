# Apresentação de combate — 0.6.30

Esta versão adapta a composição visual de Persona 3 Reload ao combate existente: comandos no canto inferior esquerdo, equipe à direita, destaque azul/ciano, mudanças de enquadramento durante a ação e efeitos por elemento. A referência visual foi o [vídeo indicado pelo usuário](https://www.youtube.com/watch?v=5-OXq5NfnKA), especialmente a sequência de combate e invocação por volta de 8:08–8:23. As skins e os modelos standard/slim dos próprios jogadores continuam sendo usados.

## Instalação

Minecraft 1.20.1, Forge 47.4.x, Java 17. Instale um único `velvet-eclipse-0.6.30-reload-presentation.jar` por perfil. Cliente e servidor devem atualizar juntos: o protocolo principal é **17**, com transmissão do ator, alvo, tipo da arma e SP de cada participante.

O cliente precisa destas duas bibliotecas adicionais, copiadas sem alteração dos arquivos fornecidos pelo usuário:

- `ldlib-forge-1.20.1-1.0.52.a.jar`
- `photon-forge-1.20.1-1.1.17.jar`

As dependências anteriores continuam necessárias: GeckoLib 4.8.4, Player Animator 1.0.2-rc1 e BendyLib 4.0.0. LDLib e Photon são declaradas como dependências de cliente; o código servidor não instancia seus renderizadores. Os JARs estão no diretório `dependencies/` do projeto completo e são arquivos separados do JAR do mod.

## Despertar e Evoker

O cenário preenche a tela. As barras pretas superiores, inferiores e laterais foram removidas; o fundo e as transições cobrem também telas 4:3 e ultrawide. Os cartões de escolha e legendas permanecem numa área central legível. A cena usa um buffer próprio, mantendo a correção anterior do formato de vértices.

O item Evoker reutiliza diretamente a geometria e a textura da arma já aprovada para a mão do jogador. Um renderizador de item aplica os enquadramentos de inventário, chão e primeira/terceira pessoa. O registro `model_evoker`, a receita, o acionamento do despertar e sua entrada na aba criativa permanecem disponíveis. Nenhum avatar substituto foi acrescentado.

## Efeitos e animações

`PhotonBattleEffects` cria instâncias reais de `BeamEmitter` e `ParticleEmitter` da API Photon. As famílias incluem fogo, gelo, eletricidade, escuridão, vento, luz, nuclear, psíquico, almighty, cura, suporte e dano físico. A escala acompanha o nível da magia; habilidades com vários alvos geram efeitos nas posições correspondentes.

As invocações usam colunas azuis, anéis e fragmentos luminosos. A Persona materializa-se por transparência na altura de invocação, sem subir do chão. Os modelos aprovados mantêm suas animações. Armas usam poses de sua família e o arco tem projétil visível durante o ataque básico.

O servidor continua calculando custo, dano, afinidades e turnos. No turno inimigo, o resultado é reservado antes da animação, aplicado no décimo tick e a apresentação termina no tick 28. Ataques básicos dos jogadores atingem no tick 19; skills invocadas resolvem no tick 86. As mensagens da ação são enviadas a todos os integrantes do combate. A reserva evita rolar dano uma segunda vez; uma inconsistência antiga na ordem dos sorteios de ataques de área foi corrigida.

Os efeitos têm tempo de vida limitado, descarte na troca de mundo e teto de 320 emissores por cliente. O bloom do Photon fica desativado; brilho usa materiais aditivos com teste de profundidade. Isso reduz dependências de pós-processamento, mas não constitui uma validação de compatibilidade com OptiFine ou outros renderizadores.

## HUD e câmera

A HUD de combate usa uma árvore LDLib (`WidgetGroup`, `ButtonWidget` e `ProgressWidget`) sobre o mundo. Os seis comandos mantêm os atalhos existentes e aceitam clique. A coluna exibe até cinco participantes com suas skins, HP, SP e indicação do turno; as barras interpolam as atualizações do servidor. Os seletores de skills e itens preservam suas regras e a ajuda existentes, dentro da composição da HUD. Os demais menus de exploração e Tab mantêm seus fluxos anteriores.

Uma direção de câmera compartilhada identifica quem está agindo. O enquadramento passa pelo ator, invocação e alvo; colisão com blocos limita a posição da câmera. Pequeno tremor acompanha o momento do impacto. A rotina All-Out mantém a prioridade da sua própria apresentação. Os números de dano/cura usam diferenças de HP recebidas do servidor.

## Verificação e reprodução

Verificações offline aprovadas nesta versão:

| Auditoria | Verificações | Cobertura principal |
| --- | ---: | --- |
| `reloadPresentationTest` | 13.549 | Pacotes de ator/alvo e HP/SP, trajetórias, limites dos emissores, separação dos controles em seis resoluções |
| `battleCommandTest` | 375.564 | Reserva de resultados, relógio de invocação, ataques físicos, seleção e protocolos |
| `multiplayerTest` | 4.099 | Grupos, turnos, 400 reservas de área, entrada/saída e cura de aliados |
| `awakeningTest` | 6.023.667 | Progressão inicial, persistência, tracks, projeção, áudio, receita e recompensas |
| `evokerWeaknessTest` | 4.421 | Empunhadura standard/slim, alinhamento na têmpora, fraqueza, crítico e morte por um golpe |

Execute com Java 17 e as dependências locais: `gradlew.bat battleCommandTest reloadPresentationTest multiplayerTest awakeningTest evokerWeaknessTest --offline`. A tarefa `reloadPresentationPreview` gera prévias da composição da HUD em 16:9, 4:3 e ultrawide e uma prancha da geometria dos efeitos em `build/reports/reload/`.

As prévias são renderizações offline do código de composição e dos planos geométricos. O cenário, os retratos e o glow ilustrativos não são capturas do Minecraft nem do renderizador Photon. Não houve execução do jogo, teste de GPU, medição de FPS ou sessão de vários clientes nesta entrega, conforme a dispensa de teste em jogo do projeto. A compilação e as auditorias não garantem ausência de problemas visuais em runtime.

## Bibliotecas

Photon e LowDragLib preservam seus arquivos originais e metadados. Ambos declaram GPL-3.0 nos JARs fornecidos. Autores: KilaBash (Photon); KilaBash e WarmthDawn (LDLib). Código-fonte e informações dos projetos: [Photon](https://github.com/Low-Drag-MC/Photon) e [LDLib para 1.20.1](https://github.com/Low-Drag-MC/LDLib-MultiLoader). Suas licenças são independentes dos recursos autorais do mod.
