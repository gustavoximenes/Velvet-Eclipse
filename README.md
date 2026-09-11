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
