# Velvet Eclipse 0.6.8 — correção do Izanagi

Correção da conversão entre as coordenadas da prévia aprovada e o GeckoLib: pivôs, rotações dos ossos/cubos e canais das animações preservam a pose original. Isso corrige a posição das mãos, pernas e arma no idle e nos ataques mágico/físico.

As superfícies planas do rosto ganharam uma separação mínima de profundidade, preservando os retângulos da textura. Elimina a disputa entre faces sobrepostas na máscara e nos olhos. A textura aprovada e o desenho do personagem foram preservados.

Validação offline com a geometria e os keyframes carregados pelo GeckoLib; prévias geradas a partir desses dados. Não houve teste dentro do Minecraft. Cut-in, regras de fraqueza, habilidades e combate continuam com o comportamento da 0.6.7.

# Velvet Eclipse 0.6.7 — Izanagi voxel

- Modelo voxel aprovado, máscara pixelada, casaco, mãos, arma e pés detalhados.
- Idle e ataques mágico/físico integrados, sincronizados ao impacto.
- Zio com raio descendente ramificado e clarão branco/rosa no alvo.
- Habilidades iniciais de Persona 4 e efeitos de suporte de três turnos.
- Comandos `/velvet persona add/remove` e proteção da última Persona.
- Correção do ATTACK descartado por terreno que bloqueava a aproximação.
- Verificações offline, sem teste dentro do Minecraft.

# Velvet Eclipse 0.6.6 — seletor de Persona

Mod para Minecraft 1.20.1 / Forge 47.4.10, Java 17. O seletor fica no canto inferior esquerdo, deixando a arena visível à direita. O retrato mantém o modelo normal/slim, a pose com Evoker e detalhes da skin real em tons de azul, ciano e branco.

## Mudanças

- Painel e retrato menores, alinhados ao canto esquerdo, com cabeçalho, habilidades, custos, descrição e comandos na mesma escala.
- Abertura de aproximadamente 0,3 segundo: painel desliza, retrato aparece e habilidades entram em sequência. As áreas clicáveis acompanham a animação.
- Paleta tonal conserva cabelo, rosto e detalhes da roupa. Camadas externas habilitadas da skin permanecem presentes; a textura original nunca é substituída.
- Retrato usa o passe sem iluminação do Forge com profundidade e fog isolado do mundo. Corrigida a sobreposição das linhas translúcidas em escalas fracionárias.
- Mantidos combate, modelos de Personas, espada, invocação, marcador de alvo, descoberta de fraquezas e protocolo 4 da versão 0.6.5.

## Instalação e controles

Use o JAR 0.6.6 da pasta `compiled Jar`, substituindo a versão anterior do Velvet Eclipse. Reinicie o perfil para carregar a alteração. São necessários GeckoLib 4.8.4, PlayerAnimator 1.0.2-rc1 e Bendy Lib 4.0.0, nas versões Forge para Minecraft 1.20.1. Nenhum outro mod de API foi acrescentado.

Q/E troca Persona; setas escolhem habilidade; Enter seleciona e depois confirma o alvo; Esc ou botão direito volta.

## Fontes e compilação

Projeto-fonte completo, recursos, testes, wrapper e dependências locais. As fontes recuperadas e sua origem estão documentadas em `source-provenance.json`. A entrega é construída das fontes, sem edição binária do JAR.

Com Java 17 configurado em JAVA_HOME:

```powershell
./gradlew.bat check --offline
./gradlew.bat assemble --offline
```

O wrapper usa Gradle 8.8. Em uma máquina sem essa distribuição no cache, a primeira execução do wrapper precisa baixá-la. As dependências Java estão em `dependencies/`; o projeto usa nomes SRG e o plugin Java, sem ForgeGradle/runClient. No Linux, use `bash gradlew`.

A tarefa `check` executa as auditorias de lógica, câmera, postura, Evoker e comandos de combate. Com Python, Pillow e NumPy, `python tools/render_battle_previews.py` gera prévias após os testes. Elas usam skin ilustrativa e geometria real; não são capturas do Minecraft.

## Validação

Auditorias offline aprovadas: 20 combinações de resolução/escala de GUI, transição e áreas clicáveis, preservação tonal/transparência, geometria normal/slim e regressões de batalha. Os 120 combates de referência mantêm o mesmo resultado. O Minecraft não foi aberto para testes; a composição final da GPU e a skin carregada em jogo ainda precisam de conferência visual.

## Publicação de versões

Ao finalizar cada versão, publicar neste repositório as fontes e o JAR compilado em `compiled Jar`, preservando o histórico e os JARs anteriores. Preferências persistentes em `AGENTS.md`. No perfil local `yuxio test`, a versão anterior fica em `velvet-eclipse-versions/0.6.5`. Ver `CHANGELOG.md` para as alterações de combate da 0.6.5.


# Velvet Eclipse 0.6.5 — comandos de combate

- Faixa de olhos durante a invocação confirmada pelo servidor, antes do dano. Câmera frontal e fragmentação a partir do tick 38, junto ao gatilho. Mantidos o giro e a pose do Evoker. A faixa termina no tick 51; o dano de Persona ocorre no impacto dos efeitos, tick 86.
- O servidor reserva uma única rolagem válida sem gastar HP/SP nem aplicar dano antecipadamente. Erros e ataques sem fraqueza não mostram a faixa. Ataques comuns de espada nunca a mostram, mesmo causando fraqueza.
- Ataque comum com aproximação, corte no tick 19 e retorno até o tick 44. Verificação de colisão do corpo antes do movimento; um caminho bloqueado exige outra ação. Golpes finais preservam o encerramento da animação. A espada permanece visível na espera de combate e usa o elemento SLASH, com compatibilidade com as afinidades físicas antigas.
- Seletor de habilidades com painel azul diagonal, retrato do jogador em azul/branco, Evoker na têmpora, custos, descrições, destaque de seleção e troca de Personas por Q/E. Usa geometria normal/slim e uma cópia da textura da skin, sem modificar a skin original.
- Selecione a habilidade ou ATTACK, confira o alvo e confirme com ENTER ou clique. SETAS escolhem habilidades; Q/E troca Persona; ESC ou botão direito volta. O cursor fica livre durante a batalha e é restaurado na saída.
- A espada de espera fica inclinada para fora, para a frente e para baixo; a transição do punho e a volta da câmera após o corte são suaves. A seleção respeita a página de habilidades e os atalhos ficam suspensos enquanto o chat ou outra tela estiver aberto.
- Removidos os painéis fixos de HP dos inimigos. O marcador acompanha o ator selecionado na arena e troca o ícone conforme o elemento. “?” indica elemento ainda não observado; “! WEAK” indica fraqueza conhecida; outros resultados conhecidos não têm aviso extra. Descobertas são persistidas por tipo de inimigo e elemento; erros não revelam afinidade.

## Validação e limites

Compilação Java 17/Gradle 8.8 offline aprovada. Passaram as auditorias de lógica, apresentação, postura, Evoker e comandos de combate. Comparações entre resolução imediata e reservada cobriram 1.920 cenários; os 120 combates antigos mantiveram os mesmos resultados. Foram verificados codecs, custos, cancelamento, colisão matemática da trajetória, tempos de animação, transparência da paleta, 20 resoluções/escalas e geometria Bendy normal/slim.

Não foi aberto Minecraft/Forge. A composição final na GPU, o carregamento da skin e o comportamento da câmera em jogo ainda precisam de conferência visual no perfil. As prévias offline usam skin ilustrativa, não são capturas do jogo atualizado. O formato atual continua sendo uma batalha com um inimigo; o marcador seleciona esse ator.

Não foi instalado novo mod de API. O projeto inclui a biblioteca lwjgl-opengl 3.3.1 já distribuída pelo Minecraft para compilar o acesso à textura. Cliente e servidor precisam desta versão, pois o canal principal passou ao protocolo 4.

A regra de fechamento aos 95% de uso foi registrada em AGENTS.md. O JAR final foi gerado após a conclusão das alterações e da revisão offline.

## Arquivos

JAR: velvet-eclipse-0.6.5-battle-command.jar. Reinicie o perfil yuxio test para carregá-lo.
A versão 0.6.4 permanece arquivada em velvet-eclipse-versions/0.6.4.
O ZIP do projeto contém todas as fontes, recursos, wrapper Gradle, testes e dependências locais. Com Java 17: `./gradlew.bat build --offline`. O projeto usa fontes SRG e o plugin Java, sem ForgeGradle/runClient. Não utiliza edição binária de JAR como fonte de entrega. Consulte source-provenance.json.

SHA-256: `724D1BD5666C240D2145907ACD8345D98D54149A19C1EEBA6F585E8F1905493E`
